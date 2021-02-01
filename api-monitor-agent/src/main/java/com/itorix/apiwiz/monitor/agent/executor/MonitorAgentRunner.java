package com.itorix.apiwiz.monitor.agent.executor;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.itorix.apiwiz.monitor.agent.api.factory.APIFactory;
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
import com.itorix.apiwiz.monitor.model.request.FormParam;
import com.itorix.apiwiz.monitor.model.request.Header;
import com.itorix.apiwiz.monitor.model.request.MonitorRequest;
import com.itorix.apiwiz.monitor.model.request.QueryParam;
import com.itorix.apiwiz.monitor.model.request.Response;
import com.itorix.apiwiz.monitor.model.request.Variable;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unused")
@Component
@Slf4j
@FieldDefaults(level=AccessLevel.PRIVATE)
public class MonitorAgentRunner {

	private final static Logger logger = LoggerFactory.getLogger(MonitorAgentRunner.class);

	private static final MustacheFactory mf = new DefaultMustacheFactory();

	@Autowired
	private MonitorAgentExecutorDao dao;

	@Autowired
	private MonitorAgentExecutorSQLDao sqlDao;

	@Value("${http.timeout}")
	int globalTimeout;

	@Autowired
	LoggerService loggerService;

	RestTemplate restTemplate = new RestTemplate();

	RSAEncryption rsaEncryption;

	@Value("${itorix.notification.agent:null}")
	private String notificationAgentPath;

	@Value("${itorix.notification.agent.contextPath:null}")
	private String notificationContextPath;

	@Value("${itorix.app.monitor.error.report.email.subject}")
	private String subject;

	@Value("${itorix.notification.failed.mail.body:null}")
	private String body;

	@Value("${itorix.app.monitor.summary.report.email.body}")
	private String emailBody;

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
		//monitorRequests.sort(Comparator.comparing(MonitorRequest::getSequenceId));
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
			Map<String, Integer> testStatus = new HashMap<String, Integer>();
			long executionStart = 0;
			ExecutionResult result = new ExecutionResult();
			try {

				String sslReference = fillTemplate(monitorRequest.getSslReference(), globalVars);
				SSLConnectionSocketFactory sslConnectionFactory = dao.getSSLConnectionFactory(sslReference);
				executionStart = System.currentTimeMillis();
				response = invokeMonitorApi(monitorRequest, globalVars, encryptedVariables, testStatus,
						sslConnectionFactory, globalTimeout);

			} catch (Exception ex) {
				logger.error("Error executing {}", monitorRequest.getName(), ex);
				result.setStatus("Failed");
				List<NotificationDetails> notificationDetails = dao.getNotificationDetails(context.getTenant(),
						context.getCollectionId());
				for (NotificationDetails notificationDetail : notificationDetails) {
					invokeNotificationAgent(notificationDetail,"Failed",monitorRequest.getName());
				}
			} finally {

				BeanUtils.copyProperties(monitorRequest, result);

				result.setCollectionId(context.getCollectionId());
				result.setSchedulerId(context.getSchedulerId());
				result.setRequestId(monitorRequest.getId());
				result.setLatency(System.currentTimeMillis() - executionStart);
				result.setExecutedTime(System.currentTimeMillis());
				if (response != null) {
					result.setStatusCode(response.getStatusLine().getStatusCode());
				}

				if (response != null
						&& HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful()) {
					result.setStatus("Success");
				} else if (!StringUtils.hasText(result.getStatus())) {
					result.setStatus("Failed");
					List<NotificationDetails> notificationDetails = dao.getNotificationDetails(context.getTenant(),
							context.getCollectionId());
					for (NotificationDetails notificationDetail : notificationDetails) {
						invokeNotificationAgent(notificationDetail,"Failed",monitorRequest.getName());
					}
				}
				replaceResponseVariables(result, monitorRequest, response);
				dao.createExecutionResult(result);
			}
		}
	}

	private Map<String, String> getGlobalVars(Variables vars) {
		return computeHeaders(vars.getVariables(), null);
	}

	public HttpResponse invokeMonitorApi(MonitorRequest monitorRequest, Map<String, String> globalVars,
			Map<String, String> encryptedVariables, Map<String, Integer> testStatus,
			SSLConnectionSocketFactory sslConnectionFactory, int timeout) throws Exception {

		if (monitorRequest.getVerb() == null) {
			logger.error("verb is null for {} ", monitorRequest.getId());
			return null;
		}

		monitorRequest.setPath(fillTemplate(monitorRequest.getPath(), globalVars));
		String path = monitorRequest.getPath();
		path = computeQueryParams(path, monitorRequest.getRequest().getQueryParams(), globalVars);

		monitorRequest.setSchemes(fillTemplate(monitorRequest.getSchemes(), globalVars));
		monitorRequest.setHost(fillTemplate(monitorRequest.getHost(), globalVars));
		monitorRequest.setPort(fillTemplate(monitorRequest.getPort(), globalVars));

		path = monitorRequest.getSchemes() + "://" + monitorRequest.getHost() + ":" + monitorRequest.getPort() + path;

		if (path != null) {
			monitorRequest.setPath(path);
		}

		Map<String, String> headers = computeHeaders(monitorRequest.getRequest().getHeaders(), globalVars);
		HttpResponse response = null;
		String reqBody = null;

		if (monitorRequest.getRequest() != null && monitorRequest.getRequest().getBody() != null
				&& monitorRequest.getRequest().getBody().getData() != null) {
			reqBody = fillTemplate(monitorRequest.getRequest().getBody().getData(), globalVars);
			String reqBodyToSet = fillTemplate(monitorRequest.getRequest().getBody().getData(), encryptedVariables);

			monitorRequest.getRequest().getBody().setData(reqBodyToSet);
		}

		if (monitorRequest.getVerb().equalsIgnoreCase(API.GET.toString())) {
			response = APIFactory.invokeGet(path, headers, monitorRequest.getName(), sslConnectionFactory, timeout);
		} else if (monitorRequest.getVerb().equalsIgnoreCase(API.POST.toString())) {

			String content = null;
			if (monitorRequest.getRequest() != null
					&& (!CollectionUtils.isEmpty(monitorRequest.getRequest().getFormParams())
							|| !CollectionUtils.isEmpty(monitorRequest.getRequest().getFormURLEncoded()))) {

				List<NameValuePair> generateNameValuePairs = null;
				if (!CollectionUtils.isEmpty(monitorRequest.getRequest().getFormParams())) {
					generateNameValuePairs = generateNameValuePairs(monitorRequest.getRequest().getFormParams(),
							globalVars);
					content = "multi-part";
				} else {
					generateNameValuePairs = generateNameValuePairs(monitorRequest.getRequest().getFormURLEncoded(),
							globalVars);
					content = "form-url-encoded";
				}
				response = APIFactory.invokePost(path, headers, generateNameValuePairs, content, reqBody,
						monitorRequest.getName(), sslConnectionFactory, timeout);
			} else {
				response = APIFactory.invokePost(path, headers, null, content, reqBody, monitorRequest.getName(),
						sslConnectionFactory, timeout);
			}
		} else if (monitorRequest.getVerb().equalsIgnoreCase(API.PUT.toString())) {

			String contentType = null;
			if (monitorRequest.getRequest() != null
					&& (!CollectionUtils.isEmpty(monitorRequest.getRequest().getFormParams())
							|| !CollectionUtils.isEmpty(monitorRequest.getRequest().getFormURLEncoded()))) {

				List<NameValuePair> generateNameValuePairs = null;
				if (!CollectionUtils.isEmpty(monitorRequest.getRequest().getFormParams())) {
					generateNameValuePairs = generateNameValuePairs(monitorRequest.getRequest().getFormParams(),
							globalVars);
					contentType = "multi-part";
				} else {
					generateNameValuePairs = generateNameValuePairs(monitorRequest.getRequest().getFormURLEncoded(),
							globalVars);
					contentType = "form-url-encoded";
				}
				response = APIFactory.invokePut(path, headers, generateNameValuePairs, contentType, reqBody,
						monitorRequest.getName(), sslConnectionFactory, timeout);
			} else {
				response = APIFactory.invokePut(path, headers, null, contentType, reqBody, monitorRequest.getName(),
						sslConnectionFactory, timeout);
			}

		} else if (monitorRequest.getVerb().equalsIgnoreCase(API.DELETE.toString())) {
			response = APIFactory.invokeDelete(path, headers, reqBody, monitorRequest.getName(), sslConnectionFactory,
					timeout);
		} else if (monitorRequest.getVerb().equalsIgnoreCase(API.OPTIONS.toString())) {
			response = APIFactory.invokeOptions(path, headers, reqBody, monitorRequest.getName(), sslConnectionFactory,
					timeout);
		} else if (monitorRequest.getVerb().equalsIgnoreCase(API.PATCH.toString())) {
			response = APIFactory.invokePatch(path, headers, reqBody, monitorRequest.getName(), sslConnectionFactory,
					timeout);
		}

		ResponseManager responseManager = new ResponseManager();
		responseManager.gatherResponseData(response, monitorRequest.getResponse(), globalVars, encryptedVariables);

		return response;

	}

	private List<NameValuePair> generateNameValuePairs(List<FormParam> formParams, Map<String, String> globalVars) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (formParams != null) {
			for (FormParam formParam : formParams) {
				formParam.setValue(fillTemplate(formParam.getValue(), globalVars));
				params.add(new BasicNameValuePair(formParam.getName(), formParam.getValue()));
			}
		}
		return params;
	}


	public Map<String, String> computeHeaders(List<Header> headers, Map<String, String> globalVars) {
		Map<String, String> headerMap = new HashMap<String, String>();
		if (null == headers) {
			return headerMap;
		}
		for (Header header : headers) {
			header.setValue(fillTemplate(header.getValue(), globalVars));
			headerMap.put(header.getName(), header.getValue());
		}
		return headerMap;
	}

	private String fillTemplate(String input, Map<String, String> vars) {
		if (input == null) {
			return null;
		}
		if (vars == null) {
			return input;
		}
		Writer writer = new StringWriter();
		Mustache mustache = mf.compile(new StringReader(input), "headers");
		mustache.execute(writer, vars);
		return writer.toString();
	}

	private String computeQueryParams(String path, List<QueryParam> queryParams, Map<String, String> globalVars) {
		if (path != null) {
			if (!path.contains("?")) {
				path = path + "?";
			}
		} else {
			path = "?";
		}
		if (queryParams != null) {
			for (QueryParam param : queryParams) {
				param.setValue(fillTemplate(param.getValue(), globalVars));
				path = path + "&" + param.getName() + "=" + param.getValue();
			}
		} else {
			if (path.endsWith("?")) {
				path = path.substring(0, path.length() - 1);
			}
		}
		return path;
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

	private void invokeNotificationAgent(NotificationDetails notificationDetail, String status, String resource) {

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
					String mailBody = MessageFormat.format(emailBody, notificationDetail.getWorkspaceName(),
							notificationDetail.getCollectionname(), notificationDetail.getEnvironmentName(),
							notificationDetail.getDate(), status, resource, notificationDetail.getDailyUptime(),
							notificationDetail.getDailyLatency(), notificationDetail.getAvgUptime(),
							notificationDetail.getAvgLatency(), notificationDetail.getSchedulerId());

					EmailTemplate emailTemplate = new EmailTemplate();
					emailTemplate.setBody(mailBody);
					emailTemplate.setToMailId(notification.getEmails());
					String mailSubject = MessageFormat.format(subject, notificationDetail.getWorkspaceName(),
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