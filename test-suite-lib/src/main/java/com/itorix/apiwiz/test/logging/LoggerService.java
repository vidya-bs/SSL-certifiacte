package com.itorix.apiwiz.test.logging;

import brave.Tracer;
import brave.propagation.TraceContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.test.executor.beans.ExecutionContext;
import com.itorix.apiwiz.test.executor.beans.TestSuiteResponse;
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
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Component
public class LoggerService {


	private Logger logger = LoggerFactory.getLogger(LoggingContext.class);

	@Value("${itorix.core.aws.admin.url:null}")
	private String awsURL;

	@Value("${itorix.core.aws.pod.url:null}")
	private String awsPodURL;

	@Autowired
	private Tracer tracer;


	private String region= null;
	private String availabilityZone= null;
	private String privateIp= null;
	private String podHostName= null;

	private static final String TEST_RUNNER_CLASS = "TestRunner.class";

	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static final String TEST_SUITE_AGENT = "testSuiteAgent";

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
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		getPodHost();
	}

	private  void getPodHost() {
		if(awsPodURL != null){
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<Object> requestEntity = new HttpEntity<>( headers);
			try {
				ResponseEntity<String> response = restTemplate.exchange(awsPodURL, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<String>() {});
				podHostName = response.getBody();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}


	public void logMethod(Map<String, String> logMessage) {
		try {
			String message = logMessage.entrySet().stream().map(v -> v.getKey() + "=" + v.getValue()).collect(Collectors.joining("||"));
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
			log.debug("span object {} " , span);
			log.debug("span.getTraceId() {} " , span.traceId());
			logMessage.put("guid", String.valueOf(Long.toHexString(span.traceId())));
			//logMessage.put("guid", UUID.randomUUID().toString());
			logMessage.put("regionCode", region);
			logMessage.put("availabilityZone", availabilityZone);
			logMessage.put("privateIp", privateIp);
			logMessage.put("podHost", podHostName);
			logMessage.put("applicationName", TEST_SUITE_AGENT);
			logMessage.put("serviceClassName", TEST_RUNNER_CLASS);
			LoggingContext.setLogMap(logMessage);

		} catch (Exception e) {
			log.error("Error occured while logging Service Request",e);
		}
	}

	public void logServiceResponse(TestSuiteResponse response, ExecutionContext context) {
		Map<String, String> logMessage = LoggingContext.getLogMap();

		logMessage.put("testStatus", response != null && StringUtils.hasText(response.getStatus()) ? response.getStatus() : "");
		logMessage.put("testsuiteId", response != null && StringUtils.hasText(response.getTestSuiteId()) ? response.getTestSuiteId() : "");
		logMessage.put("environmentId", response != null && StringUtils.hasText(response.getConfigId()) ? response.getConfigId():"");
		logMessage.put("workspaceId", context.getTenant());
		logMessage.put("responseTime", String.valueOf(System.currentTimeMillis()));
		logMethod(logMessage);
	}

}
