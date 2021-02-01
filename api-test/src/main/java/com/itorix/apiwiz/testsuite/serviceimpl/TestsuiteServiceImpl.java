package com.itorix.apiwiz.testsuite.serviceimpl;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.json.JSONException;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.TenantContext;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.testsuite.dao.TestSuiteDAO;
import com.itorix.apiwiz.testsuite.model.Certificates;
import com.itorix.apiwiz.testsuite.model.CertificatesResponse;
import com.itorix.apiwiz.testsuite.model.Header;
import com.itorix.apiwiz.testsuite.model.MaskFields;
import com.itorix.apiwiz.testsuite.model.Scenario;
import com.itorix.apiwiz.testsuite.model.TestCase;
import com.itorix.apiwiz.testsuite.model.TestSuite;
import com.itorix.apiwiz.testsuite.model.TestSuiteResponse;
import com.itorix.apiwiz.testsuite.model.TestSuiteSchedule;
import com.itorix.apiwiz.testsuite.model.Variables;
import com.itorix.apiwiz.testsuite.service.TestSuiteService;

@CrossOrigin
@RestController
public class TestsuiteServiceImpl implements TestSuiteService {

	private static final String TEST_SUITE_EXECUTE = "/v1/execute";
	private static final String TEST_SUITE_CANCEL = "/v1/cancelExecution";

	private static final Logger logger = LoggerFactory.getLogger(TestsuiteServiceImpl.class);

	@Autowired
	TestSuiteDAO dao;

	@Autowired
	private IdentityManagementDao commonServices;


	@Value("${itorix.testsuit.agent:null}")
	private String testSuitAgentPath;

	@Value("${itorix.testsuit.contextPath:null}")
	private String testSuitContextPath;

	private static final String API_KEY_NAME = "x-apikey";
	private static final String TENANT_ID = "tenantId";
	@Autowired
	private ApplicationProperties applicationProperties;

	@Value("${server.ssl.key-alias:null}")
	private String keyAlias;

	@Value("${server.ssl.key-store-password:null}")
	private String keyStorepassword;

	@Value("${server.ssl.key-password:null}")
	private String keypassword;

	@Value("${server.ssl.key-store:null}")
	private String keyStoreFilePath;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	HttpServletRequest request;

	public ResponseEntity<?> createMetaData(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody String metadata, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			HttpServletResponse response) throws JsonProcessingException, ItorixException {
		dao.createMetaData(metadata);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<?> getMetaData(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException, ItorixException {
		return new ResponseEntity<>(dao.getMetaData(), HttpStatus.OK);
	}

	public ResponseEntity<?> createTestSuite(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, @RequestBody TestSuite testSuite, HttpServletResponse response)
			throws JsonProcessingException, ItorixException {
		User user = commonServices.getUserDetailsFromSessionID(jsessionid);
		testSuite.setModifiedBy(user.getFirstName() + " " + user.getLastName());
		testSuite.setCreatedBy(user.getFirstName() + " " + user.getLastName());
		String testsuitId = dao.createTestSuite(testSuite);
		return new ResponseEntity<>("{\"id\": \"" + testsuitId + "\"}", HttpStatus.CREATED);
	}

	public ResponseEntity<?> createScenario(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody Scenario scenario, @PathVariable("testsuiteid") String testsuiteid, HttpServletRequest request,
			HttpServletResponse response) throws JsonProcessingException, ItorixException {
		User user = commonServices.getUserDetailsFromSessionID(jsessionid);
		dao.createScenario(testsuiteid, scenario);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	public ResponseEntity<?> createTestCase(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, @RequestBody TestCase testCase, @PathVariable("testsuiteid") String testsuiteid,
			@PathVariable("scenarioid") String scenarioid, HttpServletResponse response)
			throws JsonProcessingException, ItorixException {
		String testId = dao.createTestCase(testsuiteid, scenarioid, testCase);
		return new ResponseEntity<>("{\"id\": \"" + testId + "\"}", HttpStatus.CREATED);
	}
	
	public ResponseEntity<?> getTestSuiteVariables(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response, @PathVariable("testsuiteid") String testsuiteid)
					throws ItorixException{
		return new ResponseEntity<>(dao.getTestSuiteVaraibles(testsuiteid), HttpStatus.OK);
	}

	public ResponseEntity<?> getTestSuite(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response, @PathVariable("testsuiteid") String testsuiteid)
			throws ItorixException {
		return new ResponseEntity<>(dao.getTestSuite(testsuiteid), HttpStatus.OK);
	}

	public ResponseEntity<?> updateTestSuite(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, @RequestBody TestSuite testSuite, HttpServletResponse response,
			@PathVariable("testsuiteid") String testsuiteid) throws ItorixException {
		User user = commonServices.getUserDetailsFromSessionID(jsessionid);
		testSuite.setModifiedBy(user.getFirstName() + " " + user.getLastName());
		return new ResponseEntity<>(dao.updateTestSuite(testSuite, testsuiteid),
				HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<?> updateTestCase(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, @RequestBody TestCase testCase, HttpServletResponse response,
			@PathVariable("testsuiteid") String testsuiteid, @PathVariable("scenarioid") String scenarioid,
			@PathVariable("testcaseid") String testcaseid) throws ItorixException {
		dao.updateTestCase(testCase, testsuiteid, testcaseid);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<?> deleteTestSuite(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response, @PathVariable("testsuiteid") String testsuiteid)
			throws ItorixException {
		dao.deleteTestSuite(testsuiteid);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<?> deleteScenario(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response, @PathVariable("testsuiteid") String testsuiteid,
			@PathVariable("scenarioid") String scenarioid) throws ItorixException {
		dao.deleteScenario(testsuiteid, scenarioid);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<?> deleteTestCase(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response, @PathVariable("testsuiteid") String testsuiteid,
			@PathVariable("scenarioid") String scenarioid, @PathVariable("testcaseid") String testcaseid)
			throws ItorixException {
		dao.deleteTestCase(testsuiteid, testcaseid);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<Object> getAllTestSuiteDetails(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers,
			@RequestParam(value = "expand", required = false) String expand,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize) throws ItorixException{
		expand = "false";
		return new ResponseEntity<>(dao.getAllTestSuite(expand, offset, pageSize), HttpStatus.OK);
	}

	public ResponseEntity<Object> getAllTestSuiteList(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException{
		return new ResponseEntity<Object>(dao.getAllTestSuites(), HttpStatus.OK);
	}

	public ResponseEntity<?> createVariables(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestBody Variables variables,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			HttpServletResponse response) throws JsonProcessingException, ItorixException {
		User user = commonServices.getUserDetailsFromSessionID(jsessionid);
		variables.setModifiedBy(user.getFirstName() + " " + user.getLastName());
		variables.setCreatedBy(user.getFirstName() + " " + user.getLastName());
		List<Header> headerVariables = variables.getVariables();

		for (Header header : headerVariables) {
			if (header.isEncryption()) {
				try {
					header.setValue(new RSAEncryption().encryptText(header.getValue()));
				} catch (Exception e) {
					throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-9"), "Testsuite-9");
				}
			}
		}

		dao.createVariables(variables);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	public ResponseEntity<?> getVariables(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response)
			throws JsonProcessingException, ItorixException {
		Variables variables = dao.getVariablesById(id);
		if (variables == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(dao.getVariablesById(id), HttpStatus.OK);
	}

	public ResponseEntity<?> updateVariables(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestBody Variables variables,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException, ItorixException {
		User user = commonServices.getUserDetailsFromSessionID(jsessionid);
		variables.setModifiedBy(user.getFirstName() + " " + user.getLastName());
		return new ResponseEntity<>(dao.updateVariables(variables, id), HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<?> deleteVariables(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id)
			throws JsonProcessingException, ItorixException {
		dao.deleteVariable(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<?> getAllVariables(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "expand", required = false, defaultValue = "false") String expand,
			HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException, ItorixException {
		if(expand !=null && expand.equalsIgnoreCase("true"))
			return new ResponseEntity<>(dao.getVariables(), HttpStatus.OK);
		else
			return new ResponseEntity<>(dao.getVariables(offset, pageSize), HttpStatus.OK);
	}

	public ResponseEntity<?> saveTestSuiteResponse(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, @RequestBody TestSuiteResponse testSuiteResponse, HttpServletResponse response)
			throws JsonProcessingException, ItorixException {
		dao.saveTestSuiteResponse(testSuiteResponse);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	public ResponseEntity<?> getTestSuiteResponseHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, @PathVariable("testsuiteid") String testsuiteid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			HttpServletResponse response)
			throws JsonProcessingException, ItorixException {
		return new ResponseEntity<>(
				dao.getTestSuiteResponse(testsuiteid, offset), HttpStatus.OK);
	}

	public ResponseEntity<?> getTestSuiteResponseById(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, @PathVariable("testsuiteresponseid") String testsuiteresponseid,
			HttpServletResponse response) throws JsonProcessingException, ItorixException {
		return new ResponseEntity<>(dao.getTestSuiteResponseById(testsuiteresponseid),
				HttpStatus.OK);
	}

	public RestTemplate getRestTemplate() {

		try {
			TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
			SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
					.loadTrustMaterial(null, acceptingTrustStrategy).build();
			HostnameVerifier allowAll = new HostnameVerifier() {
				@Override
				public boolean verify(String hostName, SSLSession session) {
					return true;
				}
			};
			SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, allowAll);

			CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
					.setSSLSocketFactory(csf).build();

			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

			requestFactory.setHttpClient(httpClient);

			return new RestTemplate(requestFactory);
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			logger.error("exception when creating rest template", e);
		}
		return null;

	}

	public ResponseEntity<?> triggerTestSuite(@PathVariable("testsuiteId") String testSuiteId,
			@PathVariable("variableId") String variableId, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request)
			throws JsonProcessingException, JSONException, InterruptedException, ItorixException {
		return triggerTestSuite(testSuiteId, variableId, jsessionId, interactionid, request, false);
	}

	public ResponseEntity<?> triggerTestSuite(String testSuiteId,
			 String variableId, @RequestHeader(value = "JSESSIONID") String jsessionId,
			String interactionid, HttpServletRequest request, boolean isSchedulerCall )
			throws JsonProcessingException, JSONException, InterruptedException, ItorixException {

		String testSuitRespId = null;

		try {
			if (!StringUtils.hasText(testSuitAgentPath)) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-4"), "Testsuite-4");
			}
			RestTemplate restTemplate = getRestTemplate();

			HttpHeaders headers = new HttpHeaders();
			if (isSchedulerCall) {
				testSuitRespId = dao.executeTestSuite(testSuiteId, variableId, "Cron Job", true);

				headers.set(TENANT_ID, TenantContext.getCurrentTenant());
				headers.set(API_KEY_NAME, new RSAEncryption().decryptText(applicationProperties.getApiKey()));
			} else {

				User user = commonServices.getUserDetailsFromSessionID(jsessionId);
				testSuitRespId = dao.executeTestSuite(testSuiteId, variableId,
						user.getFirstName() + " " + user.getLastName(), false);
				headers.set("JSESSIONID", jsessionId);
			}

			headers.setContentType(MediaType.APPLICATION_JSON);

			Map<String, String> body = new HashMap<>();
			body.put("testSuiteExecutionId", testSuitRespId);
			HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(body, headers);

			ResponseEntity<String> result = restTemplate.postForEntity(
					testSuitAgentPath + testSuitContextPath + TEST_SUITE_EXECUTE, httpEntity, String.class);
			if (!result.getStatusCode().is2xxSuccessful()) {
				dao.deleteTestSuiteResponse(testSuitRespId);
				logger.error("error returned from test suit agent", result.getBody());
				throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-5"), "Testsuite-5");
			}
		} catch (HttpServerErrorException e) {
			dao.deleteTestSuiteResponse(testSuitRespId);
			logger.error("error executing test suit agent " +  e.getResponseBodyAsString());
			throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-5"), "Testsuite-5");
		} catch (Exception e) {
			logger.error("error executing test suit agent ",  e);
			dao.deleteTestSuiteResponse(testSuitRespId);
			if(e instanceof ItorixException){
				throw (ItorixException)e;
			}
			throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-5"), "Testsuite-5");
		}
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	public ResponseEntity<?> cancelTestSuite(@PathVariable("testsuiteId") String testSuiteId,
			@PathVariable("variableId") String variableId, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request) throws ItorixException {
		List<TestSuiteResponse> cancelTestSuite = dao.getTestSuiteEligibleForCancel(testSuiteId,
				variableId);
		if (cancelTestSuite.isEmpty()) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-8"), "Testsuite-8");
		}
		RestTemplate restTemplate = getRestTemplate();
		for (TestSuiteResponse testSuite : cancelTestSuite) {

			HttpHeaders headers = new HttpHeaders();
			headers.set("JSESSIONID", jsessionId);
			headers.setContentType(MediaType.APPLICATION_JSON);

			Map<String, String> body = new HashMap<>();
			body.put("testSuiteExecutionId", testSuite.getId());
			HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(body, headers);

			String url = testSuitAgentPath.substring(0,testSuitAgentPath.indexOf(":")) + "://" + testSuite.getTestSuiteAgent() + "/" + testSuitContextPath + TEST_SUITE_CANCEL;
			ResponseEntity<String> result;
			try {
				result = restTemplate.postForEntity(url, httpEntity, String.class);
				if (!result.getStatusCode().is2xxSuccessful()) {
					logger.error("error returned from test suit agent url {} ,  {} ", url, result.getBody());
					throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-5"), "Testsuite-5");
				}
			} catch (Exception e) {
				logger.error("error executing cancel test suit agent {} , {} ", url, e);
				throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-5"), "Testsuite-5");
			}
		}

		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	public ResponseEntity<?> getExecutionStatus(@PathVariable("executionid") String executionId,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request) {
		String response = null;

		if (StringUtils.isEmpty(executionId)) {
			return new ResponseEntity<>(new ErrorObj("Invalid pipeline Group or Pipeline Name", "CI-CD-D500"),
					HttpStatus.BAD_REQUEST);
		}
		try {
			response = dao.getTestSuiteExecutionStatus(executionId);
		} catch (Exception ex) {
			logger.error("Error while retrieving testsuite status", ex.getCause());
			return new ResponseEntity<>(new ErrorObj("Error while retrieving testsuite status", ""),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>("{\"status\":\"" + response + "\"}", HttpStatus.OK);
	}

	public ResponseEntity<?> getTestSuiteStatus(@PathVariable("testsuiteid") String testsuiteid,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request) {
		Boolean isActive = false;
		if (StringUtils.isEmpty(testsuiteid)) {
			return new ResponseEntity<>(new ErrorObj("Invalid pipeline Group or Pipeline Name", "CI-CD-D500"),
					HttpStatus.BAD_REQUEST);
		}
		try {
			isActive = dao.getTestSuiteStatus(testsuiteid);
		} catch (Exception ex) {
			logger.error("Error while retrieving testsuite status", ex.getCause());
			return new ResponseEntity<>(new ErrorObj("Error while retrieving testsuite status", ""),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>("{\"isActive\":" + isActive + "}", HttpStatus.OK);
	}

	public ResponseEntity<?> getTestSuiteHistoryWithTestSuiteAndConfig(@PathVariable("testsuiteId") String testSuiteId,
			@PathVariable("variableId") String variableId,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "user", required = false) String user,
			@RequestParam(value = "range", required = false) String range,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request)
			throws ParseException, java.text.ParseException {
		if (range == null && user == null)
			return new ResponseEntity<>(
					dao.getTestSuiteHistory(testSuiteId, variableId, offset),
					HttpStatus.OK);
		else
			return new ResponseEntity<>(dao.getTestSuiteHistory(testSuiteId, variableId,
					offset, user, range), HttpStatus.OK);
	}

	public ResponseEntity<?> pauseTestSuite(@PathVariable("testsuiteId") String testSuiteId,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request)
			throws ParseException {
		dao.pauseTestSuite(testSuiteId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<?> unpauseTestSuite(@PathVariable("testsuiteId") String testSuiteId,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request)
			throws ParseException {
		dao.unpauseTestSuite(testSuiteId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<?> getRunTimeLogs(@PathVariable("testsuiteExecutionId") String testsuiteExecutionId,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid) throws Exception {
		if (StringUtils.isEmpty(testsuiteExecutionId)) {
			return new ResponseEntity<>("Invalid Request", HttpStatus.BAD_REQUEST);
		}
		String responseLogs = null;
		try {
			responseLogs = dao.getRuntimeLogs(testsuiteExecutionId);
		} catch (Exception ex) {
			logger.error("Error while retrieving pipeline information", ex.getCause());
			return new ResponseEntity<>(new ErrorObj("Error while retrieving logs from runtime", "CI-CD-GBTA500"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(responseLogs, HttpStatus.OK);
	}

	public ResponseEntity<?> createSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody TestSuiteSchedule schedule, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			HttpServletResponse response) throws JsonProcessingException, ItorixException {
		dao.createSchedule(schedule);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	public ResponseEntity<?> updateSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody TestSuiteSchedule schedule, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			HttpServletResponse response) throws JsonProcessingException, ItorixException {
		dao.updateSchedule(schedule);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<?> deleteSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable(value = "testSuiteId") String testSuiteId, @PathVariable(value = "configId") String configId,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException, ItorixException {
		dao.deleteSchedule(testSuiteId, configId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<?> getSchedule(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response) throws ItorixException {

		return new ResponseEntity<>(dao.getTestSuiteSchedule(null), HttpStatus.OK);
	}

	public ResponseEntity<?> getSchedule(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(value = "testSuiteId") String testSuiteId, HttpServletRequest request,
			HttpServletResponse response) throws ItorixException {

		return new ResponseEntity<>(dao.getTestSuiteSchedule(testSuiteId), HttpStatus.OK);
	}

	public ResponseEntity<?> getAnalysis(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(value = "testSuiteId") String testSuiteId, @PathVariable(value = "configId") String configId,
			@RequestParam(value = "daterange", required = false) String daterange, HttpServletRequest request,
			HttpServletResponse response) throws ItorixException, java.text.ParseException {
		return new ResponseEntity<>(dao.getAnalysis(testSuiteId, configId, daterange),
				HttpStatus.OK);
	}

	public ResponseEntity<?> getDashboardInfo(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "daterange", required = false) String daterange,
			@RequestParam(value = "timeunit", required = false) String timeunit, HttpServletRequest request,
			HttpServletResponse response) throws ItorixException, java.text.ParseException {
		return new ResponseEntity<>(dao.getDashboardDetails(daterange, timeunit), HttpStatus.OK);
	}

	public ResponseEntity<Object> searchForTestSuite(
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "name") String name, @RequestParam(value = "limit") int limit) throws Exception{
		return new ResponseEntity<Object>(dao.searchForTestSuite(name, limit), HttpStatus.OK);

	}

	@Override
	public ResponseEntity<?> createOrUpdateMaskingFields(String interactionid, @RequestBody MaskFields requestBody,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws JsonProcessingException, ItorixException {
		dao.createOrUpdateMaskingFields(requestBody);
		return null;
	}

	@Override
	public ResponseEntity<?> getMaskingFields(String interactionid,  @RequestHeader(value = "JSESSIONID") String jsessionid) {
		MaskFields maskingFields = dao.getMaskingFields();
		return new ResponseEntity<>(maskingFields, HttpStatus.OK);
	}



	@Override
	public ResponseEntity<?> deleteCertificate(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,@PathVariable(value = "name") String name) throws ItorixException{
		List<TestSuite> certificateReferences = dao.getCertificateReference(name);
		if(!CollectionUtils.isEmpty(certificateReferences)){
			String testSuites = certificateReferences.stream().map(s->s.getName()).collect(Collectors.joining(","));
			throw new ItorixException((String.format(ErrorCodes.errorMessage.get("Testsuite-16"),testSuites)), "Testsuite-16");
		}

		dao.deleteCertificate(name);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getCertificate(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,@PathVariable(value = "name") String name) throws ItorixException {

		Certificates certificate = dao.getCertificate(name);
		if (certificate == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-15"), "Testsuite-15");
		}

		String url = request.getRequestURL().toString();
		StringBuilder downloadLocation =  new StringBuilder(url.substring(0,url.indexOf(request.getContextPath())+request.getContextPath().length()+1));
		downloadLocation.append("/v1/testsuites/certificates/").append(name).append("/download");
		CertificatesResponse  certificatesResponse = new CertificatesResponse();
		BeanUtils.copyProperties(certificate, certificatesResponse);
		certificatesResponse.setDownloadLocation(downloadLocation.toString());
		return new ResponseEntity<>(certificatesResponse,HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createOrUpdateCertificate(
			@RequestPart(value = "name", required = true) String name,
			@RequestPart(value = "jksFile", required = false) MultipartFile jksFile,
			@RequestPart(value = "description", required = false) String description,
			@RequestPart(value = "password", required = false) String password,
			@RequestPart(value = "alias", required = false) String alias,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		if (jksFile == null) {
			throw new ItorixException((String.format(ErrorCodes.errorMessage.get("Testsuite-10"), "JKSFile")),
					"Testsuite-10");
		}
		byte[] bytes = jksFile.getBytes();
		if (bytes == null || bytes.length == 0) {
			throw new ItorixException((String.format(ErrorCodes.errorMessage.get("Testsuite-10"), "JKSFile")),
					"Testsuite-10");
		}
		dao.createOrUpdateCertificate(name, bytes, description, password, alias, jsessionid);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getCertificates(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestParam(value = "names", required = false) String names,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			HttpServletResponse response) throws ItorixException {
		List<Certificates> certificates = dao.getCertificates(Boolean.parseBoolean(names));
		if (Boolean.parseBoolean(names)) {
			return new ResponseEntity<>(certificates.stream().map(s -> s.getName()).collect(Collectors.toList()),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>(certificates, HttpStatus.OK);
		}
	}

	public ResponseEntity<Resource> downloadCertificate(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,@PathVariable(name = "name") String name) throws ItorixException{

		byte[] content = dao.downloadCertificate(name);
		if(content == null || content.length == 0){
			throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-15"), "Testsuite-15");
		}

		ByteArrayResource resource = new ByteArrayResource(content);

	    return ResponseEntity.ok()
	            .contentType(MediaType.APPLICATION_OCTET_STREAM)
	            .body(resource);

	}

}