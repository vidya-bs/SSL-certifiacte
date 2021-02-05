package com.itorix.apiwiz.monitor.agent.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.monitor.model.ExecutionContext;

import brave.Tracer;
import brave.propagation.TraceContext;

@Component
public class LoggerService {

	private Logger logger = LoggerFactory.getLogger(LoggingContext.class);

	@Value("${itorix.core.aws.admin.url}")
	private String awsURL;

	@Value("${itorix.core.aws.pod.url:null}")
	private String awsPodURL;

	@Autowired
	private Tracer tracer;

	private String region;
	private String availabilityZone;
	private String privateIp;
	private String podHostName = null;

	private static final String MONITOR_AGENT_RUNNER_CLASS = "MonitorAgentRunner.class";

	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static final String MONITOR_AGENT = "monitorAgent";

	@Autowired
	ObjectMapper objectmapper;

	private static Logger log = LoggerFactory.getLogger(LoggerService.class);

	@PostConstruct
	public void initLoggingDetails() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		headers.set("key", "ae621919-e9cf-42eb-9b31-400a62e7b9af");
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
		try {
			ResponseEntity<String> response = restTemplate.exchange(awsURL, HttpMethod.GET, requestEntity,
					new ParameterizedTypeReference<String>() {
					});
			JsonNode json = new ObjectMapper().readTree(response.getBody());
			region = json.get("region").asText();
			availabilityZone = json.get("availabilityZone").asText();
			privateIp = json.get("privateIp").asText();
			getPodHost();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void getPodHost() {
		if (awsPodURL != null) {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
			try {
				ResponseEntity<String> response = restTemplate.exchange(awsPodURL, HttpMethod.GET, requestEntity,
						new ParameterizedTypeReference<String>() {
						});
				podHostName = response.getBody();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public void logMethod(Map<String, String> logMessage) {
		try {
			String message = logMessage.entrySet().stream().map(v -> v.getKey() + "=" + v.getValue())
					.collect(Collectors.joining("||"));
			log.info(message);
		} catch (Exception e) {
			log.error("Error occured while Service request/response");
		}
	}

	public void logServiceRequest() {
		try {
			TraceContext span = tracer.currentSpan().context();
			Date date = new Date();
			DateFormat df = new SimpleDateFormat(DATE_FORMAT);
			df.setTimeZone(TimeZone.getDefault());
			Map<String, String> logMessage = new HashMap<String, String>();
			logMessage.put("timestamp", String.valueOf(System.currentTimeMillis()));
			logMessage.put("date", df.format(date));
			// logMessage.put("guid",
			// String.valueOf(Span.idToHex(span.getTraceId())));
			logMessage.put("guid", String.valueOf(Long.toHexString(span.traceId())));
			logMessage.put("regionCode", region);
			logMessage.put("availabilityZone", availabilityZone);
			logMessage.put("privateIp", privateIp);
			logMessage.put("podHost", podHostName);
			logMessage.put("applicationName", MONITOR_AGENT);
			logMessage.put("serviceClassName", MONITOR_AGENT_RUNNER_CLASS);
			LoggingContext.setLogMap(logMessage);

		} catch (Exception e) {
			log.error("Error occured while logging Service Request");
		}
	}

	public void logServiceResponse(ExecutionContext context) {
		Map<String, String> logMessage = LoggingContext.getLogMap();
		logMessage.put("collectionId", context.getCollectionId());
		logMessage.put("schedulerId", context.getSchedulerId());
		logMessage.put("workspaceId", context.getTenant());
		logMessage.put("responseTime", String.valueOf(System.currentTimeMillis()));
		logMethod(logMessage);
	}
}
