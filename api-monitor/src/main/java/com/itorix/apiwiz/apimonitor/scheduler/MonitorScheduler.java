package com.itorix.apiwiz.apimonitor.scheduler;

import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.crypto.NoSuchPaddingException;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itorix.apiwiz.apimonitor.dao.ApiMonitorDAO;
import com.itorix.apiwiz.apimonitor.model.NotificationDetails;
import com.itorix.apiwiz.apimonitor.model.RequestModel;
import com.itorix.apiwiz.apimonitor.model.RequestModel.Type;
import com.itorix.apiwiz.apimonitor.model.collection.MonitorCollections;
import com.itorix.apiwiz.apimonitor.model.collection.Notifications;
import com.itorix.apiwiz.apimonitor.model.collection.Schedulers;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.common.util.mail.EmailTemplate;
import com.itorix.apiwiz.identitymanagement.model.TenantContext;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCursor;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@FieldDefaults(level=AccessLevel.PRIVATE)
public class MonitorScheduler {

	private static final String MONITOR_AGENT_EXECUTE = "/v1/execute";
	private static final String NOTIFICATION_AGENT_NOTIFY = "/v1/notification";

	@Autowired
	ApiMonitorDAO apiMonitorDAO;

	@Value("${itorix.monitor.agent:null}")
	private String monitorSuitAgentPath;

	@Value("${itorix.monitor.agent.contextPath:null}")
	private String monitorAgentContextPath;

	@Value("${itorix.notification.agent:null}")
	private String notificationAgentPath;

	@Value("${itorix.notification.agent.contextPath:null}")
	private String notificationContextPath;

	@Value("${server.ssl.key-alias:null}")
	private String keyAlias;

	@Value("${server.ssl.key-store-password:null}")
	private String keyStorepassword;

	@Value("${server.ssl.key-password:null}")
	private String keypassword;

	@Value("${itorix.app.monitor.summary.report.email.subject:null}")
	private String subject;

	@Value("${server.ssl.key-store:null}")
	private String keyStoreFilePath;

	@Value("${itorix.app.monitor.summary.report.email.body:null}")
	private String emailBody;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private ResourceLoader resourceLoader;

	RSAEncryption rsaEncryption;

	@Autowired
	private MongoProperties mongoProperties;
	private static final String API_KEY_NAME = "x-apikey";
	private static final String TENANT_ID = "tenantId";

	@PostConstruct
	private void setRSAKey(){
		try {
			rsaEncryption = new RSAEncryption();
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			log.error("error creating rsaEncryption", e);
		}
	}

	public RestTemplate getRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();

		KeyStore keyStore;
		HttpComponentsClientHttpRequestFactory requestFactory = null;

		try {
			keyStore = KeyStore.getInstance("jks");
			Resource storeFile = resourceLoader.getResource(keyStoreFilePath);
			keyStore.load(storeFile.getInputStream(), keyStorepassword.toCharArray());
			SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
					new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy())
							.loadKeyMaterial(keyStore, keypassword.toCharArray()).build(),
					NoopHostnameVerifier.INSTANCE);
			HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
			requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
			restTemplate.setRequestFactory(requestFactory);
		} catch (Exception exception) {
			log.error("Exception Occured while creating restTemplate " + exception);
			return new RestTemplate();
		}
		return restTemplate;
	}

	@Scheduled(cron = "0 0/5 * * * *")
	public void scheduleMonitorExecution()
			throws JsonProcessingException, JSONException, InterruptedException, ItorixException {
		try (MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoProperties.getUri()));) {
			MongoCursor<String> dbsCursor = mongoClient.listDatabaseNames().iterator();
			while (dbsCursor.hasNext()) {

				TenantContext.setCurrentTenant(dbsCursor.next());
				List<MonitorCollections> collections = apiMonitorDAO.getCollections();
				if (!CollectionUtils.isEmpty(collections)) {
					for (MonitorCollections monitorCollection : collections) {
						List<Schedulers> schedulers = monitorCollection.getSchedulers();
						if (!CollectionUtils.isEmpty(schedulers)) {
							for (Schedulers scheduler : schedulers) {
								Instant nextExecutionTime = Instant.ofEpochMilli(scheduler.getLastExecutionTime())
										.plusSeconds(60 * scheduler.getInterval());

								if (nextExecutionTime.isBefore(Instant.now())) {
									invokeMonitorAgent(monitorCollection.getId(), scheduler.getId());
									apiMonitorDAO.updateLastExecution(monitorCollection.getId(), scheduler.getId());
								}
							}
						}
					}
				}
			}
		}
	}

	private void invokeMonitorAgent(String collectionId, String schedulerId) {

		try {
			if (!StringUtils.hasText(monitorSuitAgentPath)) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-Api-2"), "Monitor-Api-2");
			}

			RestTemplate restTemplate = getRestTemplate();

			HttpHeaders headers = new HttpHeaders();
			headers.set(TENANT_ID, TenantContext.getCurrentTenant());
			headers.set(API_KEY_NAME, rsaEncryption.decryptText(applicationProperties.getApiKey()));
			headers.setContentType(MediaType.APPLICATION_JSON);

			Map<String, String> body = new HashMap<>();
			body.put("collectionId", collectionId);
			body.put("schedulerId", schedulerId);
			HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(body, headers);
			String monitorUrl = monitorSuitAgentPath + monitorAgentContextPath + MONITOR_AGENT_EXECUTE;
			ResponseEntity<String> result = restTemplate.postForEntity(monitorUrl , httpEntity, String.class);
			if (!result.getStatusCode().is2xxSuccessful()) {
				log.error("error returned from monitor agent", result.getBody());
			}
		} catch (Exception e) {
			log.error("error returned from monitor agent",e);
		}

	}


	@Scheduled(cron = "0 0 1 * * *")
	public void sendNotification()
			throws JsonProcessingException, JSONException, InterruptedException, ItorixException {
		try (MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoProperties.getUri()));) {
			MongoCursor<String> dbsCursor = mongoClient.listDatabaseNames().iterator();
			while (dbsCursor.hasNext()) {
				String workSpace = dbsCursor.next();
				TenantContext.setCurrentTenant(workSpace);
				List<NotificationDetails> notificationDetails = apiMonitorDAO.getNotificationDetails(workSpace);
				for (NotificationDetails notificationDetail : notificationDetails) {
					invokeNotificationAgent(notificationDetail);
				}
			}
		}
	}

	private void invokeNotificationAgent(NotificationDetails notificationDetail) {

		if (!StringUtils.hasText(notificationAgentPath)) {
			log.error("Maintain notificationAgentPath in property file");
			return;
		}

		if(CollectionUtils.isEmpty(notificationDetail.getNotifications())){
			return;
		}
		for (Notifications notification : notificationDetail.getNotifications()) {
			RequestModel requestModel = new RequestModel();
			try {
				if (!CollectionUtils.isEmpty(notification.getEmails())) {
					String mailBody = MessageFormat.format(emailBody , notificationDetail.getWorkspaceName(),
							notificationDetail.getCollectionname(),notificationDetail.getEnvironmentName() , notificationDetail.getDate(),
							notificationDetail.getDailyUptime(),notificationDetail.getDailyLatency(),notificationDetail.getAvgUptime(),notificationDetail.getAvgLatency()
							,notificationDetail.getSchedulerId());

					EmailTemplate emailTemplate = new EmailTemplate();
					emailTemplate.setBody(mailBody);
					emailTemplate.setToMailId(notification.getEmails());
					String mailSubject = MessageFormat.format(subject , notificationDetail.getWorkspaceName(),
							notificationDetail.getCollectionname(),notificationDetail.getEnvironmentName());
					emailTemplate.setSubject(mailSubject);
					requestModel.setEmailContent(emailTemplate);
					requestModel.setType(Type.email);
				}

				RestTemplate restTemplate = getRestTemplate();
				HttpHeaders headers = new HttpHeaders();
				headers.set(TENANT_ID, TenantContext.getCurrentTenant());
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
}
