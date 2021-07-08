package com.itorix.apiwiz.monitor.agent.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.monitor.agent.dao.MonitorAgentExecutorDao;
import com.itorix.apiwiz.monitor.agent.dao.MonitorAgentExecutorSQLDao;
import com.itorix.apiwiz.monitor.agent.db.MonitorAgentExecutorEntity;
import com.itorix.apiwiz.monitor.agent.executor.model.EmailTemplate;
import com.itorix.apiwiz.monitor.agent.executor.model.RequestModel;
import com.itorix.apiwiz.monitor.agent.executor.model.TenantContext;
import com.itorix.apiwiz.monitor.agent.executor.validators.JsonValidator;
import com.itorix.apiwiz.monitor.agent.executor.validators.ResponseValidator;
import com.itorix.apiwiz.monitor.agent.executor.validators.XmlValidator;
import com.itorix.apiwiz.monitor.agent.logging.LoggerService;
import com.itorix.apiwiz.monitor.agent.util.RSAEncryption;
import com.itorix.apiwiz.monitor.model.ExecutionContext;
import com.itorix.apiwiz.monitor.model.NotificationDetails;
import com.itorix.apiwiz.monitor.model.Variables;
import com.itorix.apiwiz.monitor.model.collection.MonitorCollections;
import com.itorix.apiwiz.monitor.model.collection.Notifications;
import com.itorix.apiwiz.monitor.model.collection.Schedulers;
import com.itorix.apiwiz.monitor.model.execute.ExecutionResult;
import com.itorix.apiwiz.monitor.model.request.Header;
import com.itorix.apiwiz.monitor.model.request.MonitorRequest;
import com.itorix.apiwiz.monitor.model.request.Response;
import com.itorix.apiwiz.monitor.model.request.Variable;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.itorix.apiwiz.monitor.agent.util.MonitorAgentConstants.*;

@SuppressWarnings("unused")
@Component
@Slf4j
@FieldDefaults(level=AccessLevel.PRIVATE)
public class MonitorAgentRunner {

	private static final Logger logger = LoggerFactory.getLogger(MonitorAgentRunner.class);

	@Autowired
	private MonitorAgentExecutorDao dao;

	@Autowired
	private MonitorAgentExecutorSQLDao sqlDao;

	@Autowired
	private MonitorAgentHelper monitorAgentHelper;

	@Value("${http.timeout}")
	int globalTimeout;

	@Autowired
	LoggerService loggerService;

	@Autowired
	private EmailContentParser emailContentParser;

	RestTemplate restTemplate = new RestTemplate();

	RSAEncryption rsaEncryption;

	@Value("${itorix.notification.agent:null}")
	private String notificationAgentPath;

	@Value("${itorix.notification.agent.contextPath:null}")
	private String notificationContextPath;

	@Value("${itorix.core.security.apikey:null}")
	private String apiKey;

	@PostConstruct
	private void setRSAKey(){
		try {
			rsaEncryption = new RSAEncryption();
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			logger.error("error creating rsaEncryption", e);
		}
	}

	private static final String NOTIFICATION_AGENT_NOTIFY = "/v1/notification";
	private static final String API_KEY_NAME = "x-apikey";
	private static final String TENANT_ID = "tenantId";

	public enum API {
		GET, PUT, POST, DELETE, OPTIONS, PATCH;
	}

	public void run(ExecutionContext context) {
		TenantContext.setCurrentTenant(context.getTenant());
		loggerService.logServiceRequest();
		try {
			executeMonitorRequests(context, globalTimeout);
		} catch (Exception ex) {
			logger.error("error when executing monitor requests", ex);
		} finally {
			sqlDao.updateStatusForTestExecutionId(context.getExecutionId(),
					MonitorAgentExecutorEntity.STATUSES.COMPLETED.getValue());
			loggerService.logServiceResponse(context);
		}
	}

	public void executeMonitorRequests(ExecutionContext context, int globalTimeout) {

		Schedulers schedulers = dao.getScheduler(context.getCollectionId(), context.getSchedulerId());
		MonitorCollections collection = dao.getMonitorCollections(context.getCollectionId(), context.getSchedulerId());
		if (schedulers == null || schedulers.isPause()) {
			return;
		}

		if (schedulers.getTimeout() > 0) {
			globalTimeout = (int) schedulers.getTimeout();
		}

		Variables vars = dao.getVariablesById(schedulers.getEnvironmentId());
		List<MonitorRequest> monitorRequests = dao.getMonitorRequests(context.getCollectionId());
		if (CollectionUtils.isEmpty(monitorRequests) || vars == null || schedulers == null) {
			logger.error("monitorRequests or variables or schedulers is null");
			return;
		}
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, String> encryptedVariables = null;
		List<Header> encryptedVariableHeaders = null;

		try {
			Variables clonedVariables = objectMapper.readValue(objectMapper.writeValueAsString(vars), Variables.class);
			encryptedVariables = getGlobalVars(clonedVariables);
			encryptedVariableHeaders = clonedVariables.getVariables();

			List<Header> headerVariables = vars.getVariables();
			for (Header variable : headerVariables) {
				if (variable.isEncryption()) {
					variable.setValue((new RSAEncryption()).decryptText(variable.getValue()));
				}
			}

		} catch (Exception e) {
			logger.error("exception occured during creating variables for collection id {} , scheduler id {} ",
					context.getCollectionId(), context.getSchedulerId());
		}

		Map<String, String> globalVars = getGlobalVars(vars);

		List<String> requestSequence = dao.getRequestSequence(context.getCollectionId());
		for (String sequence : requestSequence) {

			Optional<MonitorRequest> requestOptional = monitorRequests.stream().filter(s -> s.getId().equals(sequence))
					.findFirst();
			if (!requestOptional.isPresent()) {
				continue;
			}
			MonitorRequest monitorRequest = requestOptional.get();
			if (monitorRequest.isPause()) {
				continue;
			}
			HttpResponse response = null;
			Map<String, Integer> testStatus = new HashMap<>();
			long executionStart = 0;
			ExecutionResult result = new ExecutionResult();
			try {

				String sslReference = monitorAgentHelper.fillTemplate(monitorRequest.getSslReference(), globalVars);
				SSLConnectionSocketFactory sslConnectionFactory = dao.getSSLConnectionFactory(sslReference);
				executionStart = System.currentTimeMillis();
				response = monitorAgentHelper.invokeMonitorApi(monitorRequest, globalVars, encryptedVariables, testStatus,
						sslConnectionFactory, globalTimeout);

			} catch (Exception ex) {
				logger.error("Error executing {}", monitorRequest.getName(), ex);
				result.setStatus("Failed");
				List<NotificationDetails> notificationDetails = dao.getNotificationDetails(context.getTenant(),
						context.getCollectionId());
				for (NotificationDetails notificationDetail : notificationDetails) {
					Map<String, String> notificationData = new HashMap<>();
					notificationData.put(STATUS, "Failed");
					notificationData.put(SCHEDULER_ID, monitorRequest.getName());
					invokeNotificationAgent(notificationDetail, SUMMARY_NOTIFICATION, notificationData);
				}
			} finally {

				BeanUtils.copyProperties(monitorRequest, result);

				result.setCollectionId(context.getCollectionId());
				result.setSchedulerId(context.getSchedulerId());
				result.setRequestId(monitorRequest.getId());
				long latency = System.currentTimeMillis() - executionStart;
				result.setLatency(latency);
				result.setExecutedTime(System.currentTimeMillis());
				if (response != null) {
					result.setStatusCode(response.getStatusLine().getStatusCode());
				}

				if (response != null
						&& HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful()) {
					result.setStatus("Success");
					if(latency > monitorRequest.getExpectedLatency()) {
						result.setStatus("Failed");
						List<NotificationDetails> notificationDetails = dao.getNotificationDetails(context.getTenant(),
								context.getCollectionId());
						for (NotificationDetails notificationDetail : notificationDetails) {
							Map<String, String> notificationData = new HashMap<>();
							notificationData.put(STATUS, "Failed");
							notificationData.put(SCHEDULER_ID, monitorRequest.getName());
							notificationData.put(EXPECTED_LATENCY, String.valueOf(monitorRequest.getExpectedLatency()));
							notificationData.put(MEASURED_LATENCY, String.valueOf(latency));
							invokeNotificationAgent(notificationDetail, LATENCY_THRESHOLD_BREACH, notificationData);
						}
					}

				} else if (!StringUtils.hasText(result.getStatus())) {
					result.setStatus("Failed");
					List<NotificationDetails> notificationDetails = dao.getNotificationDetails(context.getTenant(),
							context.getCollectionId());
					for (NotificationDetails notificationDetail : notificationDetails) {
						Map<String, String> notificationData = new HashMap<>();
						notificationData.put(STATUS, "Failed");
						notificationData.put(SCHEDULER_ID, monitorRequest.getName());
						invokeNotificationAgent(notificationDetail,SUMMARY_NOTIFICATION, notificationData);
					}
				}
				replaceResponseVariables(result, monitorRequest, response);
				dao.createExecutionResult(result);
			}
		}
	}

	private Map<String, String> getGlobalVars(Variables vars) {
		return monitorAgentHelper.computeHeaders(vars.getVariables(), null);
	}



	private void replaceResponseVariables(ExecutionResult result, MonitorRequest monitorRequest,
			HttpResponse httpResponse) {

		ResponseValidator validator = null;
		Response actualResponse = result.getResponse();
		Response response = monitorRequest.getResponse();
		if (response.getBody() != null && response.getBody().getType() != null) {
			if (response.getBody() != null && response.getBody().getData() != null
					&& !response.getBody().getData().isEmpty()) {
				if (response.getBody().getType().equalsIgnoreCase("json")) {
					validator = new JsonValidator(response.getBody().getData());
				}
				if (response.getBody().getType().equalsIgnoreCase("xml")) {
					try {
						validator = new XmlValidator(response.getBody().getData());
					} catch (ParserConfigurationException | SAXException | IOException e) {
						logger.error("error when replace ResponseVariables in monitor agent", e);
					}
				}
			}
		}

		Map<String, String> headerMap = actualResponse.getHeaders();

		if (actualResponse.getVariables() != null) {
			for (Variable variable : actualResponse.getVariables()) {
				if (variable.getReference() != null) {
					if (headerMap !=null && variable.getReference().equalsIgnoreCase("headers")) {
						variable.setRunTimevalue(headerMap.get(variable.getValue()));
					} else if (variable.getReference().equalsIgnoreCase("body")) {
						try {
							if (validator != null)
								variable.setRunTimevalue(validator.getAttributeValue(variable.getValue()).toString());
						} catch (Exception e) {
							logger.error("error when getting Attribute Value", e);
						}
					}
					if (variable.getReference().equalsIgnoreCase("status")) {
						if (variable.getValue().equals("code"))
							variable.setRunTimevalue(Integer.toString(httpResponse.getStatusLine().getStatusCode()));
						else
							variable.setRunTimevalue(httpResponse.getStatusLine().getReasonPhrase());
					}
				}
			}
		}

	}

	private void invokeNotificationAgent(NotificationDetails notificationDetail, String notificationType, Map<String, String> notificationData) {

		if (!StringUtils.hasText(notificationAgentPath)) {
			log.error("Maintain notificationAgentPath in property file");
			return;
		}

		if (CollectionUtils.isEmpty(notificationDetail.getNotifications())) {
			return;
		}
		for (Notifications notification : notificationDetail.getNotifications()) {
			RequestModel requestModel = new RequestModel();
			try {
				if (!CollectionUtils.isEmpty(notification.getEmails())) {
					String[] emailContentToReplace = emailContentParser.getRelevantEmailContent(notificationDetail, notificationData);
					String mailBody = emailContentParser.getEmailBody(notificationType, emailContentToReplace);

					EmailTemplate emailTemplate = new EmailTemplate();
					emailTemplate.setBody(mailBody);
					emailTemplate.setToMailId(notification.getEmails());
					String mailSubject = emailContentParser.getEmailSubject(notificationType, notificationDetail.getWorkspaceName(),
							notificationDetail.getCollectionname(), notificationDetail.getEnvironmentName());
					emailTemplate.setSubject(mailSubject);
					requestModel.setEmailContent(emailTemplate);
					requestModel.setType(RequestModel.Type.email);
				}

				HttpHeaders headers = new HttpHeaders();
				headers.set(TENANT_ID, TenantContext.getCurrentTenant());
				headers.set(API_KEY_NAME, rsaEncryption.decryptText(apiKey));
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