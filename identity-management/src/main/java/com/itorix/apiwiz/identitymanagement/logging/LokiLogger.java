package com.itorix.apiwiz.identitymanagement.logging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.identitymanagement.model.errorlog.ErrorLog;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LokiLogger {

	@Value("${itorix.loki.agent.url:@null}")
	private String notificationAgentPath;

	@Autowired
	private ObjectMapper mapper;

	public void postLog(ErrorLog errorLog) {
		if (notificationAgentPath != null) {
			String stream = "{\"streams\":[{\"stream\":{\"application\":\"appValue\"},\"values\":[[\"epocTime\",\"content\"]]}]}";
			try {
				String content = mapper.writeValueAsString(errorLog).replaceAll("\"", "\\\\\\\"");
				content = content.replaceAll("\"", "\\\\\\\"");
				log.info(content);
				stream = stream.replaceAll("appValue", errorLog.getApplicationName()).replaceAll("content", content)
						.replaceAll("epocTime", String.valueOf(System.currentTimeMillis()) + "000000");
				log.info(stream);
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				RestTemplate restTemplate = new RestTemplate();
				HttpEntity<Object> requestEntity = new HttpEntity<>(stream, headers);
				log.debug("Making a call to {}", notificationAgentPath);
				ResponseEntity<String> response = restTemplate.exchange(notificationAgentPath, HttpMethod.POST,
						requestEntity, new ParameterizedTypeReference<String>() {
						});
				response.getBody();
			} catch (Exception e) {
				log.error("error connecting to loki: ", e);
			}
		}
	}
}
