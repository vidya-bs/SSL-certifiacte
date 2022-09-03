package com.itorix.apiwiz.common.util.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
public class Schedule {
	private static final Logger logger = LoggerFactory.getLogger(Schedule.class);

	public static boolean isSchedulable(String enabled, String primary, String host) {
		boolean scheduleEnabled = false;
		if (enabled.equals("true"))
			if (primary.equals("true"))
				scheduleEnabled = true;
			else
				scheduleEnabled = isPrimaryInactive(host);
		else
			scheduleEnabled = false;
		return scheduleEnabled;
	}

	private static boolean isPrimaryInactive(String host) {
		boolean primaryinActive = false;
		try {
			HttpHeaders headers = new HttpHeaders();
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<String> requestEntity = new HttpEntity<>(headers);
			logger.debug("Making a call to {}", host);
			ResponseEntity<String> response = restTemplate.exchange(host, HttpMethod.GET, requestEntity, String.class);
			if (!response.getStatusCode().is2xxSuccessful())
				primaryinActive = true;
		} catch (Exception e) {
			primaryinActive = true;
		}
		return primaryinActive;
	}
}
