package com.itorix.apiwiz.identitymanagement.logging;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContextHolder;
import com.itorix.apiwiz.identitymanagement.model.UserSession;

public class LoggerAspect {
	private static Logger logger = LoggerFactory.getLogger(LoggerAspect.class);
	@Autowired
	LoggerService loggerService;

	@Autowired
	HttpServletResponse httpServletResponse;

	// @Before("(within(com.itorix.hyggee..*.service..*) ||
	// within(com.itorix.hyggee..*.serviceImpl..*)) && execution(public *
	// *(..))")
	@Before("execution(* com.itorix.apiwiz..*.service..*(..)) || execution(* com.itorix.apiwiz..*.serviceImpl..*(..))")
	public void logControllerInput(JoinPoint joinPoint) throws IOException {
		try{

			HashMap<String, String> keyValuePair = new HashMap<>();
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
					.getRequest();
			String ip = request.getRemoteAddr();
			String remoteAddress = request.getHeader("X-FORWARDED-FOR");
			remoteAddress = request.getRemoteAddr();
			keyValuePair.put("clientIP", remoteAddress);
			Map<String, String> headerMap = getHeadersInfo(request);
			String className = null;
			String methodName = null;

			if (joinPoint != null && joinPoint.getTarget() != null && joinPoint.getTarget().getClass() != null) {
				className = joinPoint.getTarget().getClass().getName();
			}
			if (joinPoint != null && joinPoint.getSignature() != null) {
				methodName = joinPoint.getSignature().getName();
			}
			request.setAttribute("interactionid", headerMap.get("interactionid"));
			headerMap.put("X-FORWARDED-FOR", remoteAddress);
			request.setAttribute("startTime", System.currentTimeMillis());
			String body = null;
			UserSession userSession = ServiceRequestContextHolder.getContext().getUserSessionToken();
			loggerService.logServiceRequest(className, methodName, body, headerMap, keyValuePair, userSession);
		}catch (Exception e){
			logger.error("Error occured while logging Service Request - ", e.getMessage());
		}
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

	private Map<String, String> getHeadersInfo(HttpServletRequest request) {
		HashMap<String, String> map = new HashMap<String, String>();
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

	@AfterReturning(pointcut = "execution(public * com.itorix.apiwiz..*.service.*.*(..)) || execution(public * com.itorix.apiwiz..*.serviceImpl.*.*(..))", returning = "result")
	public void loggingMethodResponse(JoinPoint joinPoint, Object result) throws IOException {
		try{
			RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
			HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
			HttpServletResponse response = ((ServletRequestAttributes) requestAttributes).getResponse();
			String className = null;
			String methodName = null;
			HashMap<String, String> headerMap = new HashMap<>();
			if (joinPoint != null && joinPoint.getTarget() != null && joinPoint.getTarget().getClass() != null) {
				className = joinPoint.getTarget().getClass().getName();
			}
			if (joinPoint != null && joinPoint.getSignature() != null) {
				methodName = joinPoint.getSignature().getName();
			}
			ResponseEntity responseEntity = null;
			if (result instanceof ResponseEntity) {
				responseEntity = (ResponseEntity) result;
			}
			HttpStatus httpStatus = null;
			boolean isSuccess = false;
			if (responseEntity != null) {
				httpStatus = responseEntity.getStatusCode();
				if (httpStatus.is2xxSuccessful())
					isSuccess = true;
			}
			Object responseBody = null;
			String reqbody = "";
			Map<String, String> requestHeaderMap = null;
			if (isSuccess == false) {
				headerMap = getHeadersInfo(response);
				responseBody = result;
				requestHeaderMap = getHeadersInfo(request);
			}
			String interactionId = (String) request.getAttribute("interactionid");
			Long elapsedTime = System.currentTimeMillis();
			UserSession userSession = ServiceRequestContextHolder.getContext().getUserSessionToken();
			loggerService.logServiceResponse(className, methodName, responseBody, elapsedTime, headerMap, httpStatus,
					reqbody, requestHeaderMap, request, userSession, interactionId);
		}catch (Exception e){
			logger.error("Error occured while logging Service Request - ", e.getMessage());
		}
	}

	@AfterReturning(pointcut = "execution(public * com.itorix.apiwiz.common.model.exception..handle*(..))", returning = "result")
	public void loggingErrorResponse(JoinPoint joinPoint, Object result) {
		try {
			logger.debug("Inside loggingErrorResponse...[{}] , [{}]", joinPoint, result);
			RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
			HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
			HttpServletResponse response = ((ServletRequestAttributes) requestAttributes).getResponse();
			String className = null;
			String methodName = null;

			if (joinPoint != null && joinPoint.getTarget() != null
					&& joinPoint.getTarget().getClass() != null) {
				className = joinPoint.getTarget().getClass().getName();
			}
			if (joinPoint != null && joinPoint.getSignature() != null) {
				methodName = joinPoint.getSignature().getName();
			}
			ResponseEntity responseEntity = null;
			if (result instanceof ResponseEntity) {
				responseEntity = (ResponseEntity) result;
			}
			HttpStatus httpStatus = null;
			if (responseEntity != null) {
				httpStatus = responseEntity.getStatusCode();
			}
			Map<String, String> requestHeaderMap = getHeadersInfo(request);
			String interactionId = (String) request.getAttribute("interactionid");
			UserSession userSession = ServiceRequestContextHolder.getContext().getUserSessionToken();
			loggerService.logServiceResponse(className, methodName, result, System.currentTimeMillis(), null, httpStatus,
					null, requestHeaderMap, request, userSession, interactionId);
		} catch (Exception ex){
			logger.error("Error occured while logging Service Request - ", ex.getMessage());
		}
	}
}