package com.itorix.apiwiz.marketing.dao;

import java.security.NoSuchAlgorithmException;

import javax.annotation.PostConstruct;
import javax.crypto.NoSuchPaddingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.common.util.mail.EmailTemplate;
import com.itorix.apiwiz.marketing.contactus.model.ContactUsNotification;
import com.itorix.apiwiz.marketing.contactus.model.RequestModel;
import com.itorix.apiwiz.marketing.contactus.model.RequestModel.Type;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ContactUsDao {
	
	@Autowired
	private ApplicationProperties applicationProperties;
	
	private static final String API_KEY_NAME = "x-apikey";
	private static final String NOTIFICATION_AGENT_NOTIFY = "/v1/notification";
	
	@Value("${itorix.notification.agent:null}")
	private String notificationAgentPath;

	@Value("${itorix.notification.agent.contextPath:null}")
	private String notificationContextPath;

	RSAEncryption rsaEncryption;

	@PostConstruct
	private void setRSAKey(){
		try {
			rsaEncryption = new RSAEncryption();
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			log.error("error creating rsaEncryption", e);
		}
	}
	
	public void invokeNotificationAgent(ContactUsNotification contactUsNotification) {
		try{
			RequestModel requestModel = new RequestModel();
			EmailTemplate emailTemplate = new EmailTemplate();
			emailTemplate.setBody(contactUsNotification.getEmailContent().getBody().toHTML());
			emailTemplate.setToMailId(contactUsNotification.getEmailContent().getToMailId());
			emailTemplate.setSubject(contactUsNotification.getEmailContent().getSubject());
			requestModel.setEmailContent(emailTemplate);
			requestModel.setType(Type.email);
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.set(API_KEY_NAME, rsaEncryption.decryptText(applicationProperties.getApiKey()));
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<RequestModel> httpEntity = new HttpEntity<>(requestModel, headers);
			String monitorUrl = notificationAgentPath + notificationContextPath + NOTIFICATION_AGENT_NOTIFY;
			ResponseEntity<String> result = restTemplate.postForEntity(monitorUrl, httpEntity, String.class);
			if (!result.getStatusCode().is2xxSuccessful()) {
				log.error("error returned from monitor agent", result.getBody());
			}
		} catch (Exception e) {
			log.error("error returned from monitor agent", e);
		}
	}
	
}
