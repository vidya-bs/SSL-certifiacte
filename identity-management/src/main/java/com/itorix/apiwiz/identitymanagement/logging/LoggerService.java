package com.itorix.apiwiz.identitymanagement.logging;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.sleuth.Span;
//import org.springframework.cloud.sleuth.SpanAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.identitymanagement.model.UserSession;

import brave.Tracer;
import brave.propagation.TraceContext;

@Component
public class LoggerService {

//	@Autowired
//	private SpanAccessor spanAccessor;

	@Autowired
	private Tracer tracer;

	@Autowired
	ObjectMapper objectmapper;
	@Autowired
	private ApplicationProperties applicationProperties;

	private Map<String, String> logMap;

	private static Logger log = LoggerFactory.getLogger(LoggerService.class);

	@Async
	public void logMethod(String serviceName, String operationName, String traceId, Map<String, String> header,
			String serviceRequest, String serviceResponse, HashMap<String, String> keyValuePair, String elapsedTime,
			String serviceStatus, String statusCode, String messageCode, String messageDescription) {
		try {
			StringBuffer logString = new StringBuffer();
			String Application_Name = "itorix";
			String Space_Name = "cloud";
			logString.append("||" + "GUID=" + traceId + "||" + "Application_Name=" + Application_Name + "||"
					+ "Space_Name=" + Space_Name + "||" + "Service_Name=" + serviceName + "||" + "Operation_Name="
					+ operationName + "||");
			Date date = new Date();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			df.setTimeZone(TimeZone.getDefault());
			logString.append("Date_Time=" + df.format(date) + "||");
			if (header != null) {
				Map<String, String> map = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
				map.putAll(header);
				for (String key : map.keySet()) {
					logString.append(key + "=" + map.get(key) + "||");
				}
			}

			if (keyValuePair != null && !keyValuePair.isEmpty() && keyValuePair.keySet() != null) {
				for (String key : keyValuePair.keySet()) {
					logString.append(key.toUpperCase() + "=" + keyValuePair.get(key) + "||");
				}
			}
			if (serviceStatus != null) {
				logString.append("Service_Status=" + serviceStatus + "||");
			}
			if (statusCode != null) {
				logString.append("Status_Code=" + statusCode + "||");
			}
			if (messageCode != null) {
				logString.append("Message_Code=" + messageCode + "||");
			}
			if (messageDescription != null) {
				logString.append("Message_Description=" + messageDescription + "||");
			}
			if (elapsedTime != null) {
				logString.append("Elapsed_Time=" + elapsedTime + "||");
			}
			if (serviceRequest != null) {
				logString.append("ServiceRequest=" + serviceRequest + "||");
			}
			if (serviceResponse != null) {
				logString.append("ServiceResponse=" + serviceResponse + "||");
			}

			//log.info(logString.toString());

		} catch (Exception e) {
			log.error("Error occured while Service request/response");
		}
	}


	@Async
	public void logMethod(Map<String, String> logMessage ) {
		try {
			StringBuffer logString = new StringBuffer();
			logString.append("date=" + logMessage.get("date") + "||");
			logMessage.remove("date");
			logString.append("guid=" + logMessage.get("guid") + "||");
			logMessage.remove("guid");
			String interactionId = logMessage.get("interactionId");
			if(interactionId == null)
			interactionId = UUID.randomUUID().toString().replace("-", "");
			logString.append("interactionId=" + interactionId + "||");
			logMessage.remove("interactionId");
			logString.append("responseTime=" + logMessage.get("responseTime") + "||");
			logMessage.remove("responseTime");
			logString.append("moduleName=" + logMessage.get("moduleName") + "||");
			logMessage.remove("moduleName");
			logString.append("resourceName=" + logMessage.get("resourceName") + "||");
			logMessage.remove("resourceName");
			logString.append("availabilityZone=" + logMessage.get("availabilityZone") + "||");
			logMessage.remove("availabilityZone");
			logString.append("serviceClassName=" + logMessage.get("serviceClassName") + "||");
			logMessage.remove("serviceClassName");
			logString.append("responseStatusCode=" + logMessage.get("responseStatusCode") + "||");
			logMessage.remove("responseStatusCode");
			logString.append("regionCode=" + logMessage.get("regionCode") + "||");
			logMessage.remove("regionCode");
			logString.append("podHost=" + logMessage.get("podHost") + "||");
			logMessage.remove("podHost");
			logString.append("podIP=" + logMessage.get("podIP") + "||");
			logMessage.remove("podIP");
			logString.append("clientIP=" + logMessage.get("clientIP") + "||");
			logMessage.remove("clientIP");
			logString.append("userEmail=" + logMessage.get("userEmail") + "||");
			logMessage.remove("userEmail");
			logString.append("applicationName=" + logMessage.get("applicationName") + "||");
			logMessage.remove("applicationName");
			logString.append("timestamp=" + logMessage.get("timestamp") + "||");
			logMessage.remove("timestamp");
			logString.append("workspaceId=" + logMessage.get("workspaceId") + "||");
			logMessage.remove("workspaceId");
			String requestURI = logMessage.get("requestURI");
			if(requestURI == null)
				requestURI = "NA";
			logString.append("requestURI=" + requestURI + "||");
			logMessage.remove("requestURI");
			String Message_Description = logMessage.get("Message_Description");
			if(Message_Description == null)
				Message_Description = "NA";
			logString.append("messageDescription=" + Message_Description + "||");
			logMessage.remove("Message_Description");
			String responseBody = logMessage.get("responseBody");
			if(responseBody == null || responseBody == "")
				Message_Description = "NA";
			logString.append("responseBody=" + responseBody + "||");
			logMessage.remove("responseBody");
			String responseHeaders = logMessage.get("responseHeaders");
			if(responseHeaders == null)
				responseHeaders = "NA";
			logString.append("responseHeaders=" + responseHeaders + "||");
			logMessage.remove("responseHeaders");
			String requestPayload = logMessage.get("requestPayload");
			if(requestPayload == null)
				requestPayload = "NA";
			logString.append("requestPayload=" + requestPayload + "||");
			logMessage.remove("requestPayload");
			String requestHeaders = logMessage.get("requestHeaders");
			if(requestHeaders == null)
				requestHeaders = "NA";
			logString.append("requestHeaders=" + requestHeaders + "||");
			logMessage.remove("requestHeaders");
			
			log.info(logString.toString());
		} catch (Exception e) {
			log.error("Error occured while Service request/response");
		}
	}

	@Async
	public void logServiceRequest(String serviceName, String operationName, Object request,
			Map<String, String> requestHeader, HashMap<String, String> keyValuePair, UserSession userSession) {
		try {
			TraceContext span = tracer.currentSpan().context();
			Date date = new Date();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			df.setTimeZone(TimeZone.getDefault());
			Map<String, String> logMessage = new HashMap<String, String>();
			logMessage.put("date", df.format(date));
			logMessage.put("timestamp", String.valueOf(System.currentTimeMillis()));
			logMessage.put("guid" , String.valueOf(Long.toHexString(span.traceId())));
			logMessage.put("regionCode", applicationProperties.getRegion());
			logMessage.put("availabilityZone", applicationProperties.getAvailabilityZone());
			logMessage.put("podHost", applicationProperties.getPodHost());
			logMessage.put("podIP", applicationProperties.getPodIP());
			logMessage.put("applicationName","itorixApp");
			logMessage.put("serviceClassName" , serviceName);
			logMessage.put("resourceName", operationName);
			logMessage.put("clientIP",  keyValuePair.get("clientIP"));

			if (requestHeader != null) {
				Map<String, String> map = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
				map.putAll(requestHeader);
				logMessage.put("interactionId", map.get("INTERACTIONID"));
			}
			else{
				logMessage.put("interactionId", null);
			}
			if(userSession != null){
				logMessage.put("workspaceId", userSession.getWorkspaceId());
				logMessage.put("userEmail", userSession.getEmail());
			}
			else{
				logMessage.put("workspaceId", null);
				logMessage.put("userEmail", null);
			}
			logMessage.put("moduleName", getModuleName(serviceName));
			this.logMap = logMessage;
		} catch (Exception e) {
			log.error("Error occured while logging Service Request");
		}
	}

	private String getModuleName(String serviceName){
		try{
			String[] tokens = serviceName.split("\\.");
			return tokens[3];
		}catch(Exception e){
			return null;
		}
	}

	@Async
	public void logServiceResponse(String serviceName, String operationName, Object response, long elapsedTime,
			HashMap<String, String> keyValuePair, HttpStatus httpStatus, String requestBody, Map<String, String> requestHeader) {
		try {
			//Map<String, String> logMessage = ServiceRequestContextHolder.getContext().getLogMessage();
			Map<String, String> logMessage = this.logMap;
			logMessage.put("responseTime", String.valueOf(elapsedTime));
			logMessage.put("responseStatusCode", String.valueOf(httpStatus.value()));
			if(keyValuePair != null)
				logMessage.put("responseHeaders", objectmapper.writeValueAsString(keyValuePair));
			if(response != null)
				logMessage.put("resposeBody", objectmapper.writeValueAsString(response));
			if(requestHeader != null)
				logMessage.put("requestHeaders", objectmapper.writeValueAsString(requestHeader));
			if(requestBody != null && requestBody != "")
				logMessage.put("requestBody", requestBody);
			logMethod(logMessage);
			//			logMethod(serviceName, operationName, Span.idToHex(span.getTraceId()), null, null,
			//			objectmapper.writeValueAsString(response), keyValuePair, String.valueOf(elapsedTime),
			//			httpStatus.name(), String.valueOf(httpStatus.value()), null, null);
		} catch (Exception e) {
			log.error("Error occured while logging Service Response");
		}
	}

	@Async
	public void logException(String serviceName, String operationName, long elapsedTime, HttpStatus httpStatus,
			String messageCode, String messageDescription, final HttpServletResponse response, final HttpServletRequest request) {
		try {
			TraceContext span = tracer.currentSpan().context();
			if(logMap == null){
				this.logMap = new HashMap<String, String>();
				Date date = new Date();
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				df.setTimeZone(TimeZone.getDefault());
				logMap.put("date", df.format(date));
				logMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
				logMap.put("guid" , String.valueOf(Long.toHexString(span.traceId())));
				logMap.put("regionCode", applicationProperties.getRegion());
				logMap.put("availabilityZone", applicationProperties.getAvailabilityZone());
				logMap.put("applicationName","itorixApp");
				logMap.put("serviceClassName" , serviceName);
				logMap.put("resourceName", operationName);
			}
			HashMap<String, String> headerMap = getHeadersInfo(response);
			headerMap.put("interactionid", (String)request.getAttribute("interactionid"));
			String responseBody = "Message_Code=" + messageCode + "||Message_Description=" + messageDescription;
			String reqbody= null;

			Map<String, String> requestHeaderMap = getHeadersInfo(request);
			String uri = request.getRequestURI();

			logMap.put("responseTime", String.valueOf(elapsedTime));
			logMap.put("responseStatusCode", String.valueOf(httpStatus.value()));
			logMap.put("responseHeaders", objectmapper.writeValueAsString(headerMap));
			logMap.put("responseBody", responseBody);
			logMap.put("requestHeaders", objectmapper.writeValueAsString(requestHeaderMap));
			logMap.put("requestPayload", reqbody);
			logMap.put("requestURI", uri);
			logMethod(logMap);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			log.error("Error occured while logging Service Response");
		}
	}

	@Async
	public void backendLogging(String GUID, String backendName, String backendRequest, String backendResponse,
			String status, String elapsedTime, String errorMessage) {
		try {
			StringBuffer serviceResponse = new StringBuffer();
			String Application_Name = "itorix";
			String Space_Name = "cloud";
			serviceResponse.append("||" + "GUID=" + GUID + "||" + "Application_Name=" + Application_Name + "||"
					+ "Space_Name=" + Space_Name + "||");
			if (backendName != null) {
				serviceResponse.append("Backend_Name=" + backendName + "||");
			}
			if (status != null) {
				serviceResponse.append(backendName + "_" + "Service_Status=" + status + "||");
			}
			if (elapsedTime != null) {
				serviceResponse.append(backendName + "_" + "Elapsed_Time=" + elapsedTime + "||");
			}
			if (backendRequest != null) {
				serviceResponse.append(backendName + "_" + "Backend_Request=" + backendRequest + "||");
			}
			if (backendResponse != null) {
				serviceResponse.append(backendName + "_" + "Backend_Response=" + backendResponse + "||");
			}
			if (errorMessage != null) {
				serviceResponse.append(backendName + "_" + "Backend_Exception=" + errorMessage);
			}

			//log.info(serviceResponse.toString().replaceAll("\\\\", ""));

		} catch (Exception e) {
			log.error("Error occured while logging backend request/response");
		}
	}

	@Async
	public void logBackendMessage(String backendName, Object backendRequest, Object backendResponse, String status,
			Long elapsedTime, String errorMessage) {
		String elapsedTimeString = null;
		String req = null;
		String res = null;
		try {
			//Span span = spanAccessor.getCurrentSpan();
			String span = UUID.randomUUID().toString();
			if (elapsedTime != null) {
				elapsedTimeString = String.valueOf(elapsedTime);
			}
			if (backendResponse instanceof String) {
				req = (String) backendRequest;
				res = (String) backendResponse;
			} else {
				req = (backendRequest != null) ? objectmapper.writeValueAsString(backendRequest) : null;
				res = (backendResponse != null) ? objectmapper.writeValueAsString(backendResponse) : null;
			}
//			backendLogging(Span.idToHex(span.getTraceId()), backendName, req, res, status, elapsedTimeString,
//					errorMessage);
			backendLogging(span, backendName, req, res, status, elapsedTimeString,errorMessage);
		} catch (Exception e) {
			log.error("Error occured while logging Service Response");
		}
	}

	private Map<String, String> getHeadersInfo(HttpServletRequest request) {
		Map<String, String> map = new HashMap<String, String>();
		if (request != null) {
			Enumeration headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String key = (String) headerNames.nextElement();
				String value = request.getHeader(key);
				map.put(key, value);
			}
		}
		return map;
	}
	private HashMap<String, String> getHeadersInfo(HttpServletResponse response) {
		HashMap<String, String> map = new HashMap<String, String>();
		if (response != null) {
			Collection<String> collection = response.getHeaderNames();
			Iterator<String> headerNames = collection.iterator();
			while (headerNames.hasNext()) {
				String key = (String) headerNames.next();
				String value = response.getHeader(key);
				map.put(key, value);
			}
		}
		return map;
	}
	private String extractPostRequestBody(HttpServletRequest request) throws IOException {
		if ("POST".equalsIgnoreCase(request.getMethod())) {
			Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		}
		if ("PUT".equalsIgnoreCase(request.getMethod())) {
			Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		}
		return "";
	}

}
