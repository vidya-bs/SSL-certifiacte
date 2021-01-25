package com.itorix.mockserver.controller;

import static com.itorix.hyggee.mockserver.mock.HttpStateHandler.PATH_PREFIX;
import static com.itorix.hyggee.mockserver.model.HttpResponse.response;
import static com.itorix.hyggee.mockserver.model.PortBinding.portBinding;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_IMPLEMENTED;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.SpanAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.MediaType;
import com.itorix.hyggee.mockserver.client.serialization.ObjectMapperFactory;
import com.itorix.hyggee.mockserver.client.serialization.PortBindingSerializer;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.mappers.HttpServletRequestToMockServerRequestDecoder;
import com.itorix.hyggee.mockserver.mock.HttpStateHandler;
import com.itorix.hyggee.mockserver.mock.action.ActionHandler;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.HttpResponse;
import com.itorix.hyggee.mockserver.responsewriter.ResponseWriter;
import com.itorix.hyggee.mockserver.scheduler.Scheduler;
import com.itorix.hyggee.mockserver.server.ServletResponseWriter;
import com.itorix.mockserver.dto.DataPersist;
import com.itorix.mockserver.logging.model.MockLog;

@CrossOrigin
@RestController
@Component
public class Controller {

	@Autowired
	private SpanAccessor spanAccessor;
	
	@Autowired
	DataPersist dataPersist;
	
	private Scheduler scheduler = new Scheduler();

	private ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();

	HttpStateHandler httpStateHandler = new HttpStateHandler(scheduler, dataPersist);

	private MockServerLogger mockServerLogger = httpStateHandler.getMockServerLogger();

	// serializers
	private PortBindingSerializer portBindingSerializer  = new PortBindingSerializer(mockServerLogger);
	
	// mappers
	private HttpServletRequestToMockServerRequestDecoder httpServletRequestToMockServerRequestDecoder = new HttpServletRequestToMockServerRequestDecoder();

	// mockserver
	private ActionHandler actionHandler = new ActionHandler(httpStateHandler, null);

	@Override
	protected void finalize() {
		scheduler.shutdown();
	}

	@PostConstruct
	private void init(){
		httpStateHandler.setDataPersist(dataPersist);
	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/**")
	public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		
		ResponseWriter responseWriter = new ServletResponseWriter(httpServletResponse);
		HttpRequest request = null;
		HttpResponse finalResponse = null;
		String expectationId = null;
		Map responseMap = null;
		boolean isSuccess = false;
		try {

			request = httpServletRequestToMockServerRequestDecoder.mapHttpServletRequestToMockServerRequest(httpServletRequest);
			if (!httpStateHandler.handle(request, responseWriter, true)) {

				if (request.getPath().getValue().equals("/_mockserver_callback_websocket")) {

					responseWriter.writeResponse(request, NOT_IMPLEMENTED, "ExpectationResponseCallback and ExpectationForwardCallback is not supported by MockServer deployed as a WAR", "text/plain");

				} else if (request.matches("PUT", PATH_PREFIX + "/status", "/status")) {

					responseWriter.writeResponse(request, OK, portBindingSerializer.serialize(portBinding(httpServletRequest.getLocalPort())), "application/json");

				} else if (request.matches("PUT", PATH_PREFIX + "/bind", "/bind")) {

					responseWriter.writeResponse(request, NOT_IMPLEMENTED);

				} else if (request.matches("PUT", PATH_PREFIX + "/stop", "/stop")) {

					responseWriter.writeResponse(request, NOT_IMPLEMENTED);

				} else {
					String portExtension = "";
					if (!(httpServletRequest.getLocalPort() == 443 && httpServletRequest.isSecure() || httpServletRequest.getLocalPort() == 80)) {
						portExtension = ":" + httpServletRequest.getLocalPort();
					}
					 
					responseMap= actionHandler.processRequest(request, finalResponse, responseWriter, null, ImmutableSet.of(
							httpServletRequest.getLocalAddr() + portExtension,
							"localhost" + portExtension,
							"127.0.0.1" + portExtension
							), false, true, isSuccess);
					finalResponse = (HttpResponse) responseMap.get("response");
					expectationId = (String)responseMap.get("expectationId");
					isSuccess = (boolean)responseMap.get("success");
				}
			}
		} catch (IllegalArgumentException iae) {
			mockServerLogger.error(request, "exception processing: {} error: {}", request, iae.getMessage());
			responseWriter.writeResponse(request, BAD_REQUEST, iae.getMessage(), MediaType.create("text", "plain").toString());
		} catch (Exception e) {
			mockServerLogger.error(request, e, "exception processing " + request);
			responseWriter.writeResponse(request, response().withStatusCode(BAD_REQUEST.code()).withBody(e.getMessage()), true);
		}
		finally {
			MockLog mockLog = new MockLog();
			mockLog.setClientIp(httpServletRequest.getRemoteAddr());
			mockLog.setPath(request.getPath().getValue());
			mockLog.setTraceId(spanAccessor.getCurrentSpan().getTraceId());
			mockLog.setWasMatched(isSuccess);
			mockLog.setLoggedTime(System.currentTimeMillis());
			mockLog.setExpectationId(expectationId);
			
			if(expectationId !=null) {
				String expectationName = dataPersist.getExpectationName(expectationId);
				String groupId = dataPersist.getExpectation(expectationId).getGroupId();
				String groupName = dataPersist.getGroup(groupId).getName();
				mockLog.setExpectationName(expectationName);
				mockLog.setGroupId(groupId);
				mockLog.setGroupName(groupName);
			}
			try {
				mockLog.setHttpRequest(objectMapper.writeValueAsString(request));
				mockLog.setHttpResponse(objectMapper.writeValueAsString(finalResponse));
				mockServerLogger.info(getLogData(mockLog));
				this.dataPersist.addLogEntry(mockLog);
			} catch (Exception e) {
			}
		}
	}

	private Map getLogData(MockLog mockLog){
		Map logMap = new HashMap();
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getDefault());
		logMap.put("date", df.format(date));
		logMap.put("guid", spanAccessor.getCurrentSpan().getTraceId());
		logMap.put("clientIp", mockLog.getClientIp());
		logMap.put("path", df.format(date));
		try {
			logMap.put("logMessage",  objectMapper.writeValueAsString(mockLog));
		} catch (JsonProcessingException e) {
		}
		return logMap;
	}
}
