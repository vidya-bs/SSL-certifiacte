package com.itorix.mockserver.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.SpanAccessor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.mockserver.common.model.MockLog;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MockLogger {

	private ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private SpanAccessor spanAccessor;

	@Value("${itorix.core.aws.admin.url:null}")
	private String awsURL;

	@Value("${itorix.core.aws.pod.url:null}")
	private String awsPodURL;

	private String region= null;
	private String availabilityZone= null;
	private String privateIp= null;
	private String podHostName= null;

	@PostConstruct
	public void initLoggingDetails() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		headers.set("key", "ae621919-e9cf-42eb-9b31-400a62e7b9af");
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
		try {
			ResponseEntity<String> response = restTemplate.exchange(awsURL, HttpMethod.GET, requestEntity,
					new ParameterizedTypeReference<String>() {});
			JsonNode json = new ObjectMapper().readTree(response.getBody());
			region = json.get("region").asText();
			availabilityZone = json.get("availabilityZone").asText();
			privateIp = json.get("privateIp").asText();
			getPodHost();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
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
				log.error(e.getMessage(), e);
			}
		}
	}


	@SuppressWarnings("rawtypes")
	public void info(Map logMap) {
		StringBuffer logString = new StringBuffer();
		logString.append("date=" + logMap.get("date") + "||");
		logMap.remove("date");
		logString.append("guid=" + logMap.get("guid") + "||");
		logMap.remove("guid");
		logString.append("region=" + region + "||");
		logString.append("availabilityZone=" + availabilityZone + "||");
		logString.append("privateIp=" + privateIp + "||");
		logString.append("podHost=" + podHostName + "||");
		logString.append("clientIp=" + logMap.get("clientIp") + "||");
		logMap.remove("clientIp");
		logString.append("path=" + logMap.get("path") + "||");
		logMap.remove("path");
		logString.append("logMessage=" + logMap.get("logMessage") + "||");
		logMap.remove("logMessage");
		log.info(logString.toString());
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map getLogData(MockLog mockLog) {
		Map logMap = new HashMap();
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getDefault());
		logMap.put("date", df.format(date));
		logMap.put("guid", spanAccessor.getCurrentSpan().getTraceId());
		logMap.put("clientIp", mockLog.getClientIp());
		logMap.put("path", df.format(date));
		try {
			logMap.put("logMessage", objectMapper.writeValueAsString(mockLog));
		} catch (JsonProcessingException e) {
		}
		return logMap;
	}

}
