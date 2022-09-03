package com.itorix.apiwiz.notification.agent.logging;

import brave.Tracer;
import brave.propagation.TraceContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private String region = null;
    private String availabilityZone = null;
    private String privateIp = null;
    private String podHostName = null;

    private static final String NOTIFICATION_AGENT_RUNNER_CLASS = "NotificationAgentRunner.class";

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final String NOTIFICATION_AGENT = "notificationAgent";

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
            log.debug("Making a call to {}",awsURL);
            ResponseEntity<String> response = restTemplate.exchange(awsURL, HttpMethod.GET, requestEntity,
                    new ParameterizedTypeReference<String>() {
                    });
            JsonNode json = new ObjectMapper().readTree(response.getBody());
            region = json.get("region").asText();
            availabilityZone = json.get("availabilityZone").asText();
            privateIp = json.get("privateIp").asText();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        getPodHost();
    }

    private void getPodHost() {
        if (awsPodURL != null) {
            log.debug("Getting Pod Host");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
            try {
                log.debug("Making a call to {}",awsPodURL);
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
            log.error("Error occurred while Service request/response");
        }
    }

    public void logServiceRequest() {
        try {
            TraceContext span = tracer.newTrace().context();
            Date date = new Date();
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setTimeZone(TimeZone.getDefault());
            Map<String, String> logMessage = new HashMap();
            logMessage.put("timestamp", String.valueOf(System.currentTimeMillis()));
            logMessage.put("date", df.format(date));
            logMessage.put("guid", Long.toHexString(span.traceId()));
            logMessage.put("regionCode", region);
            logMessage.put("availabilityZone", availabilityZone);
            logMessage.put("privateIp", privateIp);
            logMessage.put("podHost", podHostName);
            logMessage.put("applicationName", NOTIFICATION_AGENT);
            logMessage.put("serviceClassName", NOTIFICATION_AGENT_RUNNER_CLASS);
            LoggingContext.setLogMap(logMessage);

        } catch (Exception e) {
            log.error("Error occurred while logging Service Request", e);
        }
    }

    public void logServiceResponse() {
        Map<String, String> logMessage = LoggingContext.getLogMap();
        logMessage.put("responseTime", String.valueOf(System.currentTimeMillis()));
        logMethod(logMessage);
    }
}
