package com.itorix.hyggee.mockserver.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.itorix.hyggee.mockserver.callback.WebSocketClientRegistry;
import com.itorix.hyggee.mockserver.client.serialization.*;
import com.itorix.hyggee.mockserver.client.serialization.java.ExpectationToJavaSerializer;
import com.itorix.hyggee.mockserver.client.serialization.java.HttpRequestToJavaSerializer;
import com.itorix.hyggee.mockserver.client.serialization.model.ExpectationVO;
import com.itorix.hyggee.mockserver.client.serialization.model.GroupVO;
import com.itorix.hyggee.mockserver.filters.MockServerEventLog;
import com.itorix.hyggee.mockserver.log.model.LogEntry;
import com.itorix.hyggee.mockserver.log.model.MessageLogEntry;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.model.*;
import com.itorix.hyggee.mockserver.responsewriter.ResponseWriter;
import com.itorix.hyggee.mockserver.scheduler.Scheduler;
import com.itorix.hyggee.mockserver.verify.Verification;
import com.itorix.hyggee.mockserver.verify.VerificationSequence;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import static com.google.common.net.MediaType.*;
import static com.itorix.hyggee.mockserver.log.model.MessageLogEntry.LogMessageType.*;
import static com.itorix.hyggee.mockserver.model.HttpRequest.request;
import static com.itorix.hyggee.mockserver.model.HttpResponse.response;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *   
 */
@Component("httpStateHandler")
public class HttpStateHandler {

	public static final String LOG_SEPARATOR = "\n------------------------------------\n";
	public static final String PATH_PREFIX = "/mockserver";
	private final MockServerEventLog mockServerLog;
	private final Scheduler scheduler;
	// mockserver

	private MockServerLogger mockServerLogger = new MockServerLogger(LoggerFactory.getLogger(this.getClass()), this);
	private WebSocketClientRegistry webSocketClientRegistry = new WebSocketClientRegistry();
	// serializers
	private HttpRequestSerializer httpRequestSerializer = new HttpRequestSerializer(mockServerLogger);
	private ExpectationSerializer expectationSerializer ;
	private HttpRequestToJavaSerializer httpRequestToJavaSerializer = new HttpRequestToJavaSerializer();
	private ExpectationToJavaSerializer expectationToJavaSerializer = new ExpectationToJavaSerializer();
	private VerificationSerializer verificationSerializer = new VerificationSerializer(mockServerLogger);
	private VerificationSequenceSerializer verificationSequenceSerializer = new VerificationSequenceSerializer(mockServerLogger);
	private ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();
	
	private DataPersist dataPersist;
	
	private MockServerMatcher mockServerMatcher;


	
	public void setDataPersist(DataPersist dataPersist){
		this.dataPersist = dataPersist;
		mockServerMatcher = new MockServerMatcher(mockServerLogger, null, dataPersist);
		expectationSerializer = new ExpectationSerializer(mockServerLogger, dataPersist);
	}
	

	public HttpStateHandler(Scheduler scheduler) {
		this.scheduler = scheduler;
		mockServerLog = new MockServerEventLog(mockServerLogger, scheduler);
		//mockServerMatcher = new MockServerMatcher(mockServerLogger, scheduler);
	}
	
	public HttpStateHandler(Scheduler scheduler, DataPersist dataPersist) {
		this.dataPersist = dataPersist;
		this.scheduler = scheduler;
		mockServerLog = new MockServerEventLog(mockServerLogger, scheduler);
		//mockServerMatcher = new MockServerMatcher(mockServerLogger, scheduler);
	}
	
	public HttpStateHandler() {
		this.scheduler = new Scheduler();
		mockServerLog = new MockServerEventLog(mockServerLogger, scheduler);
		//mockServerMatcher = new MockServerMatcher(mockServerLogger, scheduler);
	}

	public MockServerLogger getMockServerLogger() {
		return mockServerLogger;
	}

	public void clear(HttpRequest request) {
		HttpRequest requestMatcher = null;
		if (!Strings.isNullOrEmpty(request.getBodyAsString())) {
			requestMatcher = httpRequestSerializer.deserialize(request.getBodyAsString());
		}
		try {
			ClearType retrieveType = ClearType.valueOf(StringUtils.defaultIfEmpty(request.getFirstQueryStringParameter("type").toUpperCase(), "ALL"));
			switch (retrieveType) {
			case LOG:
				mockServerLog.clear(requestMatcher);
				mockServerLogger.debug(CLEARED, requestMatcher, "clearing recorded requests and logs that match:{}", (requestMatcher == null ? "{}" : requestMatcher));
				break;
			case EXPECTATIONS:
				mockServerMatcher.clear(requestMatcher);
				mockServerLogger.debug(CLEARED, requestMatcher, "clearing expectations that match:{}", (requestMatcher == null ? "{}" : requestMatcher));
				break;
			case ALL:
				mockServerLog.clear(requestMatcher);
				mockServerMatcher.clear(requestMatcher);
				mockServerLogger.debug(CLEARED, requestMatcher, "clearing expectations and request logs that match:{}", (requestMatcher == null ? "{}" : requestMatcher));
				break;
			}
		} catch (IllegalArgumentException iae) {
			throw new IllegalArgumentException("\"" + request.getFirstQueryStringParameter("type") + "\" is not a valid value for \"type\" parameter, only the following values are supported " + Lists.transform(Arrays.asList(ClearType.values()), new Function<ClearType, String>() {
				public String apply(ClearType input) {
					return input.name().toLowerCase();
				}
			}));
		}
	}

	public void reset() {
		mockServerMatcher.reset();
		mockServerLog.reset();
		mockServerLogger.debug(CLEARED, "resetting all expectations and request logs");
	}

	public void add(Expectation... expectations) {
		for (Expectation expectation : expectations) {
			mockServerMatcher.add(expectation);
			mockServerLogger.debug(CREATED_EXPECTATION, expectation.getHttpRequest(), "creating expectation:{}", expectation.clone());
		}
	}

	public ActionHandleResponse firstMatchingExpectation(HttpRequest request) {
		mockServerMatcher.reloadData();
		if (mockServerMatcher.isEmpty()) {
			mockServerLogger.debug(EXPECTATION_NOT_MATCHED, request, "no active expectations");
			return null;
		} else {
			MockServerMatcherResponse mockServerMatcherResponse = mockServerMatcher.firstMatchingExpectation(request);
			List<ClosestMatcher> matchers = getMatchigData(mockServerMatcherResponse.getPartialMatches());
			ActionHandleResponse actionHandleResponse = new ActionHandleResponse();
			actionHandleResponse.setExpectation(mockServerMatcherResponse.getExpectation());
			actionHandleResponse.setMatchers(matchers);
			return actionHandleResponse;
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<ClosestMatcher> getMatchigData(Map partialMatches){
		List <ClosestMatcher> matchers = new ArrayList();
		Iterator it = partialMatches.entrySet().iterator();
		while (it.hasNext()) {
			ClosestMatcher matcher = new ClosestMatcher();
			Map.Entry pair = (Map.Entry)it.next();
			ExpectationVO expectationVO = this.dataPersist.getExpectation((String)pair.getKey());
			GroupVO groupVO = this.dataPersist.getGroup(expectationVO.getGroupId());
			matcher.setExpectationId(expectationVO.getId());
			matcher.setReason((String)pair.getValue());
			matcher.setExpectationName(expectationVO.getName());
			matcher.setGroupName(groupVO.getName());
			matchers.add(matcher);
		}
		if(matchers.size()>0)
			return matchers;
		else
		return null;
	}

	public void log(LogEntry logEntry) {
		if (mockServerLog != null) {
			mockServerLog.add(logEntry);
		}
	}

	public HttpResponse retrieve(HttpRequest request) {
		HttpRequest httpRequest = null;
		if (!Strings.isNullOrEmpty(request.getBodyAsString())) {
			httpRequest = httpRequestSerializer.deserialize(request.getBodyAsString());
		}
		HttpResponse response = response();
		try {
			Format format = Format.valueOf(StringUtils.defaultIfEmpty(request.getFirstQueryStringParameter("format").toUpperCase(), "JSON"));
			RetrieveType retrieveType = RetrieveType.valueOf(StringUtils.defaultIfEmpty(request.getFirstQueryStringParameter("type").toUpperCase(), "REQUESTS"));
			switch (retrieveType) {
			case LOGS: {
				mockServerLogger.debug(RETRIEVED, httpRequest, "retrieving " + retrieveType.name().toLowerCase() + " that match:{}", (httpRequest == null ? request() : httpRequest));
				List<MessageLogEntry> retrievedMessages = mockServerLog.retrieveMessages(httpRequest);
				StringBuilder stringBuffer = new StringBuilder();
				for (int i = 0; i < retrievedMessages.size(); i++) {
					MessageLogEntry messageLogEntry = retrievedMessages.get(i);
					stringBuffer
					.append(messageLogEntry.getTimeStamp())
					.append(" - ")
					.append(messageLogEntry.getMessage());
					if (i < retrievedMessages.size() - 1) {
						stringBuffer.append(LOG_SEPARATOR);
					}
				}
				stringBuffer.append("\n");
				response.withBody(stringBuffer.toString(), PLAIN_TEXT_UTF_8);
				break;
			}
			case REQUESTS: {
				mockServerLogger.debug(RETRIEVED, httpRequest, "retrieving " + retrieveType.name().toLowerCase() + " in " + format.name().toLowerCase() + " that match:{}", (httpRequest == null ? request() : httpRequest));
				List<HttpRequest> httpRequests = mockServerLog.retrieveRequests(httpRequest);
				switch (format) {
				case JAVA:
					response.withBody(httpRequestToJavaSerializer.serialize(httpRequests), create("application", "java").withCharset(UTF_8));
					break;
				case JSON:
					response.withBody(httpRequestSerializer.serialize(httpRequests), JSON_UTF_8);
					break;
				}
				break;
			}
			case RECORDED_EXPECTATIONS: {
				mockServerLogger.debug(RETRIEVED, httpRequest, "retrieving " + retrieveType.name().toLowerCase() + " in " + format.name().toLowerCase() + " that match:{}", (httpRequest == null ? request() : httpRequest));
				List<Expectation> expectations = mockServerLog.retrieveExpectations(httpRequest);
				switch (format) {
				case JAVA:
					response.withBody(expectationToJavaSerializer.serialize(expectations), create("application", "java").withCharset(UTF_8));
					break;
				case JSON:
					response.withBody(expectationSerializer.serialize(expectations), JSON_UTF_8);
					break;
				}
				break;
			}
			case ACTIVE_EXPECTATIONS: {
				mockServerLogger.debug(RETRIEVED, httpRequest, "retrieving " + retrieveType.name().toLowerCase() + " in " + format.name().toLowerCase() + " that match:{}", (httpRequest == null ? request() : httpRequest));
				List<Expectation> expectations = mockServerMatcher.retrieveExpectations(httpRequest);
				switch (format) {
				case JAVA:
					response.withBody(expectationToJavaSerializer.serialize(expectations), create("application", "java").withCharset(UTF_8));
					break;
				case JSON:
					response.withBody(expectationSerializer.serialize(expectations), JSON_UTF_8);
					break;
				}
				break;
			}
			}
		} catch (IllegalArgumentException iae) {
			if (iae.getMessage().contains(RetrieveType.class.getSimpleName())) {
				throw new IllegalArgumentException("\"" + request.getFirstQueryStringParameter("type") + "\" is not a valid value for \"type\" parameter, only the following values are supported " + Lists.transform(Arrays.asList(RetrieveType.values()), new Function<RetrieveType, String>() {
					public String apply(RetrieveType input) {
						return input.name().toLowerCase();
					}
				}));
			} else {
				throw new IllegalArgumentException("\"" + request.getFirstQueryStringParameter("format") + "\" is not a valid value for \"format\" parameter, only the following values are supported " + Lists.transform(Arrays.asList(Format.values()), new Function<Format, String>() {
					public String apply(Format input) {
						return input.name().toLowerCase();
					}
				}));
			}
		}

		return response.withStatusCode(200);
	}

	public String verify(Verification verification) {
		return mockServerLog.verify(verification);
	}

	public String verify(VerificationSequence verification) {
		return mockServerLog.verify(verification);
	}

	public boolean handle(HttpRequest request, ResponseWriter responseWriter, boolean warDeployment) {
		mockServerLogger.trace(request, "received request:{}", request);

		if (request.matches("PUT", PATH_PREFIX + "/expectation", "/expectation")) {

			for (Expectation expectation : expectationSerializer.deserializeArray(request.getBodyAsString(),request)) {
				if (!warDeployment || validateSupportedFeatures(expectation, request, responseWriter)) {
					add(expectation);
				}
			}
			responseWriter.writeResponse(request, CREATED);

		} else if (request.matches("GET", PATH_PREFIX + "/expectation", "/expectation")) {

			List <ExpectationVO> expectationDTOs =dataPersist.getExpectationDTOs();
			HttpResponse response = null;
			try {
				response = HttpResponse.response(new StringBody(objectMapper.writeValueAsString(expectationDTOs)).getValue());
				response.withHeader(new Header("content-type","applacation/json"));
				request.withBody(objectMapper.writeValueAsString(expectationDTOs));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			responseWriter.writeResponse(request, response, false);

		} else if (request.matches("PUT", PATH_PREFIX + "/clear", "/clear")) {

			clear(request);
			responseWriter.writeResponse(request, OK);

		} else if (request.matches("PUT", PATH_PREFIX + "/reset", "/reset")) {

			reset();
			responseWriter.writeResponse(request, OK);

		} else if (request.matches("PUT", PATH_PREFIX + "/retrieve", "/retrieve")) {

			responseWriter.writeResponse(request, retrieve(request), true);

		} else if (request.matches("PUT", PATH_PREFIX + "/verify", "/verify")) {

			Verification verification = verificationSerializer.deserialize(request.getBodyAsString());
			mockServerLogger.debug(VERIFICATION, verification.getHttpRequest(), "verifying requests that match:{}", verification);
			String result = verify(verification);
			if (StringUtils.isEmpty(result)) {
				responseWriter.writeResponse(request, ACCEPTED);
			} else {
				responseWriter.writeResponse(request, NOT_ACCEPTABLE, result, create("text", "plain").toString());
			}

		} else if (request.matches("PUT", PATH_PREFIX + "/verifySequence", "/verifySequence")) {

			VerificationSequence verificationSequence = verificationSequenceSerializer.deserialize(request.getBodyAsString());
			//mockServerLogger.debug(VERIFICATION, verificationSequence.getHttpRequests(), "verifying sequence that match:{}", verificationSequence);
			String result = verify(verificationSequence);
			if (StringUtils.isEmpty(result)) {
				responseWriter.writeResponse(request, ACCEPTED);
			} else {
				responseWriter.writeResponse(request, NOT_ACCEPTABLE, result, create("text", "plain").toString());
			}

		} else {
			return false;
		}
		return true;
	}

	private boolean validateSupportedFeatures(Expectation expectation, HttpRequest request, ResponseWriter responseWriter) {
		boolean valid = true;
		Action action = expectation.getAction();
		String NOT_SUPPORTED_MESSAGE = " is not supported by MockServer deployed as a WAR due to limitations in the JEE specification; use mockserver-netty to enable these features";
		if (action instanceof HttpResponse && ((HttpResponse) action).getConnectionOptions() != null) {
			responseWriter.writeResponse(request, response("ConnectionOptions" + NOT_SUPPORTED_MESSAGE), true);
			valid = false;
		} else if (action instanceof HttpObjectCallback) {
			responseWriter.writeResponse(request, response("HttpObjectCallback" + NOT_SUPPORTED_MESSAGE), true);
			valid = false;
		} else if (action instanceof HttpError) {
			responseWriter.writeResponse(request, response("HttpError" + NOT_SUPPORTED_MESSAGE), true);
			valid = false;
		}
		return valid;
	}

	public WebSocketClientRegistry getWebSocketClientRegistry() {
		return webSocketClientRegistry;
	}

	public MockServerMatcher getMockServerMatcher() {
		return mockServerMatcher;
	}

	public MockServerEventLog getMockServerLog() {
		return mockServerLog;
	}

	public Scheduler getScheduler() {
		return scheduler;
	}
}
