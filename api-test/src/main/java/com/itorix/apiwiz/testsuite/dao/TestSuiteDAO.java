package com.itorix.apiwiz.testsuite.dao;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.common.model.MetaData;
import com.itorix.apiwiz.common.model.SearchItem;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.Date.DateUtil;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.itorix.apiwiz.testsuite.business.gocd.beans.Attributes;
import com.itorix.apiwiz.testsuite.business.gocd.beans.Job;
import com.itorix.apiwiz.testsuite.business.gocd.beans.Material;
import com.itorix.apiwiz.testsuite.business.gocd.beans.Pipeline;
import com.itorix.apiwiz.testsuite.business.gocd.beans.PipelineGroup;
import com.itorix.apiwiz.testsuite.business.gocd.beans.Stage;
import com.itorix.apiwiz.testsuite.business.gocd.beans.Task;
import com.itorix.apiwiz.testsuite.model.Certificates;
import com.itorix.apiwiz.testsuite.model.Dashboard;
import com.itorix.apiwiz.testsuite.model.DashboardStats;
import com.itorix.apiwiz.testsuite.model.DashboardSummary;
import com.itorix.apiwiz.testsuite.model.Header;
import com.itorix.apiwiz.testsuite.model.MaskFields;
import com.itorix.apiwiz.testsuite.model.Pagination;
import com.itorix.apiwiz.testsuite.model.Response;
import com.itorix.apiwiz.testsuite.model.Scenario;
import com.itorix.apiwiz.testsuite.model.ScenarioStats;
import com.itorix.apiwiz.testsuite.model.Stats;
import com.itorix.apiwiz.testsuite.model.TestCase;
import com.itorix.apiwiz.testsuite.model.TestCaseStats;
import com.itorix.apiwiz.testsuite.model.TestSuite;
import com.itorix.apiwiz.testsuite.model.TestSuiteAnalysis;
import com.itorix.apiwiz.testsuite.model.TestSuiteHistoryResponse;
import com.itorix.apiwiz.testsuite.model.TestSuiteOverviewResponse;
import com.itorix.apiwiz.testsuite.model.TestSuiteResponse;
import com.itorix.apiwiz.testsuite.model.TestSuiteSchedule;
import com.itorix.apiwiz.testsuite.model.TestSuiteStats;
import com.itorix.apiwiz.testsuite.model.Variables;

@Component
@EnableScheduling
public class TestSuiteDAO {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Qualifier("masterMongoTemplate")
	@Autowired
	private MongoTemplate masterMongoTemplate;

	@Autowired
	private ApplicationProperties config;

	private static final Logger log = LoggerFactory.getLogger(TestSuiteDAO.class);

	@Autowired
	private IdentityManagementDao identityManagementDao;

	public void createMetaData(String metadataStr) {
		Query query = new Query().addCriteria(Criteria.where("key").is("testsuite"));
		MetaData metaData = masterMongoTemplate.findOne(query, MetaData.class);
		if(metaData != null)
		{
			Update update = new Update();
			update.set("metadata", metadataStr);
			masterMongoTemplate.updateFirst(query, update, MetaData.class);
		}else
			masterMongoTemplate.save(new MetaData("testsuite",metadataStr));
	}

	public Object getMetaData() {
		Query query = new Query().addCriteria(Criteria.where("key").is("testsuite"));
		MetaData metaData = masterMongoTemplate.findOne(query, MetaData.class);
		if (metaData != null)
			return metaData.getMetadata();
		return null;
	}

//	public void createMetaData(String metadata) {
//		masterMongoTemplate.dropCollection(MetaData.class);
//		masterMongoTemplate.save(new MetaData(metadata));
//	}
//
//	public Object getMetaData() {
//		List<MetaData> metaData = masterMongoTemplate.findAll(MetaData.class);
//		if (metaData.size() > 0)
//			return metaData.get(0).getMetadata();
//		return null;
//	}

	public void createVariables(Variables variables) throws ItorixException {
		if (findByConfigName(variables.getName()) == null) {
			String userId = null;
			String username = null;
			try {
				UserSession userSession  = UserSession.getCurrentSessionToken();
				userId = userSession.getUserId();
				username = userSession.getUsername();
			} catch (Exception e) {
			}
			String id = variables.getId();
			long timestamp = System.currentTimeMillis();
			variables.setMts(timestamp);
			variables.setModifiedBy(userId);
			variables.setModifiedUserName(username);
			if (id == null || id == "") {
				variables.setCts(timestamp);
				variables.setCreatedBy(userId);
				variables.setCreatedUserName(username);
			}
			mongoTemplate.save(variables);
		} else {
			throw new ItorixException("Variables exists with same name", "Config-1004");
		}
	}

	public Object updateVariables(Variables variables, String id) throws ItorixException {
		Query query = new Query(Criteria.where("_id").is(id));
		Variables dbVariables = mongoTemplate.findOne(query, Variables.class);
		//		DBObject dbDoc = new BasicDBObject();
		//		mongoTemplate.getConverter().write(variables, dbDoc);
		//		Update update = Update.fromDBObject(dbDoc, "_id");
		//		UpdateResult result = mongoTemplate.updateFirst(query, update, Variables.class);
		if (dbVariables != null) {
			variables.setId(id);
			String userId = null;
			String username = null;
			try {
				UserSession userSession  = UserSession.getCurrentSessionToken();
				userId = userSession.getUserId();
				username = userSession.getUsername();
			} catch (Exception e) {
			}
			long timestamp = System.currentTimeMillis();
			variables.setMts(timestamp);
			variables.setModifiedBy(userId);
			variables.setModifiedUserName(username);
			if (id == null || id == "") {
				variables.setCts(timestamp);
				variables.setCreatedBy(userId);
				variables.setCreatedUserName(username);
			}
			List<Header> headerVariables = variables.getVariables();
			for (Header header : headerVariables) {
				if (header.isEncryption()) {
					Header dbHeader = getVariable(dbVariables.getVariables(), header.getName());
					if((dbHeader == null) || !dbHeader.isEncryption() ||
							(dbHeader != null && !dbHeader.getValue().equals(header.getValue()))){
						try {
							header.setValue(new RSAEncryption().encryptText(header.getValue()));
						} catch (Exception e) {
							throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-9"), "Testsuite-9");
						}
					}
				}
			}
			mongoTemplate.save(variables);
			return true;
		} else {
			throw new ItorixException("No Record exists", "Config-1004");
		}
	}
	private Header getVariable(List<Header> headerVariables, String name){
		for (Header header : headerVariables)
			if (header.getName().equals(name))
				return header;
		return null;
	}

	public Variables findByVariableName(String name) {
		Query query = new Query(Criteria.where("name").is(name));
		Variables varibale = mongoTemplate.findOne(query, Variables.class);
		return varibale;
	}

	public TestSuiteOverviewResponse getVariables(int offset, int pageSize) {
		List<Variables> variables = mongoTemplate.findAll(Variables.class);
		if(variables.size()>0){
			for(Variables variable: variables )
				variable.setVariables(null);
			TestSuiteOverviewResponse response = new TestSuiteOverviewResponse();
			response.setData( variables);
			com.itorix.apiwiz.identitymanagement.model.Pagination pagination = new com.itorix.apiwiz.identitymanagement.model.Pagination();
			long total = mongoTemplate.count(new Query() ,Variables.class);
			pagination.setOffset(offset);
			pagination.setTotal(total);
			pagination.setPageSize(pageSize);
			response.setPagination(pagination);
			return response;
		}
		else
			return new TestSuiteOverviewResponse();

	}

	public List<Variables> getVariables() {
		return mongoTemplate.findAll(Variables.class);
	}

	public void deleteVariable(String id) {
		mongoTemplate.remove(new Query(Criteria.where("id").is(id)), Variables.class);
	}

	public String createTestSuite(TestSuite testSuite) throws ItorixException {
		if (findBytestSuiteName(testSuite.getName()) == null) {
			String userId = null;
			String username = null;
			try {
				UserSession userSession  = UserSession.getCurrentSessionToken();
				userId = userSession.getUserId();
				username = userSession.getUsername();
			} catch (Exception e) {
			}
			String id = testSuite.getId();
			long timestamp = System.currentTimeMillis();
			testSuite.setMts(timestamp);
			testSuite.setModifiedBy(userId);
			testSuite.setModifiedUserName(username);
			if (id == null || id == "") {
				testSuite.setCts(timestamp);
				testSuite.setCreatedBy(userId);
				testSuite.setCreatedUserName(username);
			}
			testSuite.setActive(true);
			mongoTemplate.save(testSuite);
			return findBytestSuiteName(testSuite.getName()).getId();
		} else {
			throw new ItorixException("Test suite exists with same name", "Config-1004");
		}
	}

	public String createTestCase(String testsuiteid, String scenarioid, TestCase testCase) throws ItorixException {
		Query query = new Query(Criteria.where("_id").is(testsuiteid));
		TestSuite testSuite = mongoTemplate.findOne(query, TestSuite.class);
		if (testSuite != null) {
			if (testSuite.getScenarios() != null) {
				for (Scenario scenario : testSuite.getScenarios()) {
					if (scenario.getId() != null && scenario.getId().equals(scenarioid)) {
						scenario.getTestCases().add(testCase);
						mongoTemplate.save(testSuite);
						for (TestCase testCaseUpdated : scenario.getTestCases()) {
							if (testCaseUpdated.getId() != null && testCaseUpdated.getName().equals(testCase.getName()))
								return testCaseUpdated.getId();
						}
					}
				}
			}
		}
		throw new ItorixException("TestSuite or scenario not available", "Config-1004");
	}

	public void createScenario(String testsuiteid, Scenario scenario) throws ItorixException {
		Query query = new Query(Criteria.where("_id").is(testsuiteid));
		TestSuite testSuite = mongoTemplate.findOne(query, TestSuite.class);
		if (testSuite != null) {
			if (testSuite.getScenarios() == null) {
				List<Scenario> scenarios = new ArrayList<>();
				scenarios.add(scenario);
				testSuite.setScenarios(scenarios);
			} else {
				testSuite.getScenarios().add(scenario);
			}
			mongoTemplate.save(testSuite);
		}
		throw new ItorixException("TestSuite or scenario not available", "Config-1004");
	}

	public String updateTestSuite(TestSuite testSuite, String testsuiteid) throws ItorixException {
		Query query = new Query(Criteria.where("_id").is(testsuiteid));
		TestSuite testSuit = mongoTemplate.findOne(query, TestSuite.class);

		//		DBObject dbDoc = new BasicDBObject();
		//		mongoTemplate.getConverter().write(testSuite, dbDoc);
		//		Update update = Update.fromDBObject(dbDoc, "_id");
		//		WriteResult result = mongoTemplate.updateFirst(query, update, TestSuite.class);
		if (testSuit != null) {
			testSuite.setId(testsuiteid);
			String userId = null;
			String username = null;
			try {
				UserSession userSession  = UserSession.getCurrentSessionToken();
				userId = userSession.getUserId();
				username = userSession.getUsername();
			} catch (Exception e) {
			}
			String id = testSuite.getId();
			long timestamp = System.currentTimeMillis();
			testSuite.setMts(timestamp);
			testSuite.setModifiedBy(userId);
			testSuite.setModifiedUserName(username);
			if (id == null || id == "") {
				testSuite.setCts(timestamp);
				testSuite.setCreatedBy(userId);
				testSuite.setCreatedUserName(username);
			}
			mongoTemplate.save(testSuite);
			return testsuiteid;
		} else {
			throw new ItorixException("No Record exists", "Config-1004");
		}
	}

	public void updateTestCase(TestCase testCase, String testsuiteId, String testcaseId) throws ItorixException {
		Query query = new Query(Criteria.where("_id").is(testsuiteId));
		TestSuite testSuite = mongoTemplate.findOne(query, TestSuite.class);
		if (testSuite != null) {
			if (testSuite.getScenarios() != null) {
				for (Scenario scenario : testSuite.getScenarios()) {
					if (scenario.getTestCases() != null) {
						for (int i = 0; i < scenario.getTestCases().size(); i++) {
							if (scenario.getTestCases().get(i).getId().equals(testcaseId)) {
								testCase.setId(testcaseId);
								scenario.getTestCases().set(i, testCase);
								mongoTemplate.save(testSuite);
								return;
							}
						}
					}
				}
			}
		}
		throw new ItorixException("No Record exists", "Config-1004");
	}

	public TestSuite getTestSuite(String testsuiteid) {
		return findBytestSuiteId(testsuiteid);
	}
	
	public TestSuite getTestSuiteVaraibles(String testsuiteid) {
		TestSuite dbTestsuite = findBytestSuiteId(testsuiteid);
		TestSuite testsuite = new TestSuite();
		if(dbTestsuite != null){
			testsuite.setId(dbTestsuite.getId());
			List<Scenario> scenarios = new ArrayList<>();
			testsuite.setScenarios(scenarios);
			for(Scenario dbScenario: dbTestsuite.getScenarios()){
				Scenario scenario = new Scenario();
				scenarios.add(scenario);
				scenario.setName(dbScenario.getName());
				scenario.setId(dbScenario.getId());
				for(TestCase dbTestCase: dbScenario.getTestCases()){
					TestCase testCase = new TestCase();
					Response response = new Response();
					testCase.setId(dbTestCase.getId());
					testCase.setName(dbTestCase.getName());
					if(dbTestCase.getResponse() != null && dbTestCase.getResponse().getVariables() != null){
						response.setVariables(dbTestCase.getResponse().getVariables());
					}
					testCase.setResponse(response);
					scenario.getTestCases().add(testCase);
				}
			}
		}
		testsuite.setExecutionStatus(null);
		return testsuite;
	}

	public void deleteTestSuite(String testsuiteid) {
		mongoTemplate.remove(new Query(Criteria.where("id").is(testsuiteid)), TestSuite.class);
	}

	public void deleteScenario(String testSuiteId, String scenarioid) {
		Query query = new Query(Criteria.where("_id").is(testSuiteId));
		TestSuite testSuite = mongoTemplate.findOne(query, TestSuite.class);
		if (testSuite != null) {
			if (testSuite.getScenarios() != null) {
				for (Scenario scenario : testSuite.getScenarios()) {
					if (scenario.getId().equals(scenarioid)) {
						testSuite.getScenarios().remove(scenario);
						mongoTemplate.save(testSuite);
						return;
					}
				}
			}
		}
	}

	public void deleteTestCase(String testSuiteId, String testcaseid) {
		Query query = new Query(Criteria.where("_id").is(testSuiteId));
		TestSuite testSuite = mongoTemplate.findOne(query, TestSuite.class);
		if (testSuite != null) {
			if (testSuite.getScenarios() != null) {
				for (Scenario scenario : testSuite.getScenarios()) {
					if (scenario.getTestCases() != null) {
						for (TestCase testcase : scenario.getTestCases()) {
							if (testcase.getId().equals(testcaseid)) {
								scenario.getTestCases().remove(testcase);
								mongoTemplate.save(testSuite);
								return;
							}
						}
					}
				}
			}
		}
	}

	public TestSuite findBytestSuiteId(String testsuiteid) {
		Query query = new Query(Criteria.where("_id").is(testsuiteid));
		TestSuite testSuite = mongoTemplate.findOne(query, TestSuite.class);
		return testSuite;
	}

	public TestSuite findBytestSuiteName(String testSuiteName) {
		Query query = new Query(Criteria.where("name").is(testSuiteName));
		TestSuite testSuite = mongoTemplate.findOne(query, TestSuite.class);
		return testSuite;
	}

	public Variables findByConfigName(String configName) {
		Query query = new Query(Criteria.where("name").is(configName));
		return mongoTemplate.findOne(query, Variables.class);
	}

	public List<TestSuite> getAllTestSuite(String expand) {
		List<TestSuite> testSuites = mongoTemplate.findAll(TestSuite.class);
		for (TestSuite testSuite : testSuites) {
			if (expand != null && expand.equalsIgnoreCase("true") && testSuite.getScenarios() != null) {
				for (Scenario scenario : testSuite.getScenarios()) {
					if (scenario.getTestCases() != null)
						for (TestCase testCase : scenario.getTestCases()) {
							testCase.setRequest(null);
							testCase.setResponse(null);
							testCase.setHost(null);
							testCase.setPort(null);
							testCase.setMessage(null);
							testCase.setPath(null);
							testCase.setSchemes(null);
						}
				}
			} else {
				testSuite.setScenarios(null);
			}
		}
		return testSuites;
	}


	public TestSuiteOverviewResponse getAllTestSuite(String expand, int offset, int pageSize) {
		Query query = new Query().with(Sort.by(Direction.DESC, "_id"))
				.skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
		TestSuiteOverviewResponse response = new TestSuiteOverviewResponse();
		List<TestSuite> testSuites = mongoTemplate.find(query, TestSuite.class);
		for (TestSuite testSuite : testSuites) {
			if (expand != null && expand.equalsIgnoreCase("true") && testSuite.getScenarios() != null) {
				for (Scenario scenario : testSuite.getScenarios()) {
					if (scenario.getTestCases() != null)
						for (TestCase testCase : scenario.getTestCases()) {
							testCase.setRequest(null);
							testCase.setResponse(null);
							testCase.setHost(null);
							testCase.setPort(null);
							testCase.setMessage(null);
							testCase.setPath(null);
							testCase.setSchemes(null);
						}
				}
			} else {
				testSuite.setScenarios(null);
			}
		}
		if (testSuites != null) {
			Long counter = mongoTemplate.count(new Query(), TestSuite.class);
			com.itorix.apiwiz.identitymanagement.model.Pagination pagination = new com.itorix.apiwiz.identitymanagement.model.Pagination();
			pagination.setOffset(offset);
			pagination.setTotal(counter);
			pagination.setPageSize(pageSize);
			response.setPagination(pagination);
			response.setData(testSuites);
		}
		return response;
	}

	public List<TestSuite> getAllTestSuites() {
		List<TestSuite> testSuites = mongoTemplate.findAll(TestSuite.class);
		for (TestSuite testSuite : testSuites) {
			testSuite.setScenarios(null);
		}
		return testSuites;
	}



	public String executeTestSuite(String testSuiteId, String variableId, String userName, boolean isCron)
			throws JsonProcessingException, JSONException, InterruptedException, ItorixException {
		TestSuite testSuite = null;
		Variables variables = null;
		testSuite = getTestSuite(testSuiteId);
		if(testSuite == null){
			throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-6"), "Testsuite-6");
		}

		if (!testSuite.getActive()) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-2"), "Testsuite-2");
		}
		variables = getVariablesById(variableId);

		if(variables == null){
			throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-7"), "Testsuite-7");
		}

		return triggerPipeline(testSuiteId, variableId, testSuite, variables, userName, false, isCron);
		//			createPipeline(testSuiteId, variableId, testSuite.getName(), variables.getName());
		//			triggerPipeline(testSuiteId, variableId, testSuite, variables, userName, false);
	}

	public String triggerPipeline(String testSuiteId, String variableId, TestSuite testSuite, Variables variables,
			String userName, boolean historyCallRequired, boolean isCron) throws JSONException, InterruptedException,ItorixException {
		if (!isCron) {
			Query query = new Query(Criteria.where("testSuiteId").is(testSuiteId).and("configId").is(variableId)
					.and("status").is(TestSuiteResponse.STATUSES.SCHEDULED.getValue()));
			List<TestSuiteResponse> response = mongoTemplate.find(query, TestSuiteResponse.class);
			if (response != null && response.size() > 0) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-1"), "Testsuite-1");
			}
		}
		TestSuiteResponse response = new TestSuiteResponse(testSuiteId, variableId, testSuite, TestSuiteResponse.STATUSES.SCHEDULED.getValue());
		response.setCreatedBy(userName);
		updateCount(response);
		saveTestSuiteResponse(response);
		return response.getId();
	}

	//	private String getBuildNumber(String historyResponse) throws JSONException {
	//		return JsonPath.parse(historyResponse).read("pipelines[0].counter").toString();
	//	}

	private synchronized void updateCount(TestSuiteResponse response) {
		Query query = new Query(
				Criteria.where("testSuiteId").is(response.getTestSuiteId()).and("configId").is(response.getConfigId()));
		response.setCounter("" + ( mongoTemplate.count(query, TestSuiteResponse.class) + 1 ));
	}

	//	public void updateUser(User user, List<String> projectRoles) {
	//		Query query = new Query(Criteria.where("id").is(user.getId()));
	//		DBObject dbDoc = new BasicDBList();
	//		mongoTemplate.getConverter().write(projectRoles, dbDoc);
	//		Update update = Update.fromDBObject(dbDoc);
	//		mongoTemplate.upsert(query, update, "workspaces.0.roles");
	//
	//	}

	public void createPipeline(String testSuiteId, String variableId, String pipelineName, String configName)
			throws JsonProcessingException {
		// If there is an exception in triggering, create the job and trigger it
		PipelineGroup pipelineGroup = new PipelineGroup(pipelineName);
		Pipeline pipeline = new Pipeline(pipelineName + "_" + configName, pipelineName + "_" + configName + "-${count}",
				false);
		// Creating and adding Materials for pipeline
		Material material = new Material();
		Attributes attributes = new Attributes();
		String scmUserName = config.getProxyScmUserName();
		String scmPassword = config.getBuildScmPassword();

		String scmURL = "URL";
		String scmType = "git";

		if (scmType != null && scmType != "" && scmType.equals("svn")) {
			material.setType("svn");
			attributes.setUrl(scmURL);
			attributes.setDestination("PipelineProxy");
			attributes.setInvertFilter(false);
			attributes.setName("ProxyPolling");
			attributes.setAutoUpdate(true);
			attributes.setUsername(scmUserName);
			attributes.setPassword(scmPassword);
		} else {
			material.setType("git");
			scmURL = "https://" + scmUserName + ":" + scmPassword + "@github.com/itorix/build.git";
			attributes.setUrl(scmURL);
			attributes.setBranch("master");
			attributes.setDestination(testSuiteId + "_" + variableId);
			attributes.setInvertFilter(false);
			attributes.setName("TestSuite");
			attributes.setAutoUpdate(true);
			attributes.setShallowClone(true);
		}
		material.setAttributes(attributes);

		List<Material> materials = new ArrayList<Material>();
		materials.add(material);
		pipeline.setMaterials(materials);

		// Set Pipeline Stages
		List<Stage> stages = new ArrayList<>();
		Stage initialStage = new Stage("Trigger", true, false, false, "manual");

		// Set Tasks
		List<Job> jobs = new ArrayList<>();
		Job triggerJob = new Job("TestSuite");

		List<Task> tasks = new ArrayList<>();
		tasks.add(new Task("exec", "passed", config.getTestSuiteTriggerScriptLocation(),
				"-DtestSuiteId=" + testSuiteId + " -DconfigId=" + variableId + " -DappUrl=" + config.getAppUrl()
				+ " -Dusername=" + config.getServiceUserName() + " -Dpassword=" + config.getServicePassword(),
				null, true));

		triggerJob.setTasks(tasks);
		jobs.add(triggerJob);
		initialStage.setJobs(jobs);
		stages.add(initialStage);

		// Set Pipeline Stages
		pipeline.setStages(stages);
		pipelineGroup.setPipeline(pipeline);

		// Invoke API and throw exception in case of issues/errors for API
		// creation
		RestTemplate restTemplate = new RestTemplate();
		ObjectMapper mapper = new ObjectMapper();
		restTemplate.getInterceptors()
		.add(new BasicAuthorizationInterceptor(config.getCicdAuthUserName(), config.getCicdAuthPassword()));
		String response = null;
		log.debug(mapper.writeValueAsString(pipelineGroup));
		HttpEntity<PipelineGroup> requestEntity = new HttpEntity<>(pipelineGroup, getCommonHttpHeaders());
		try {
			response = restTemplate.postForObject(config.getPipelineBaseUrl() + config.getPipelineAdminEndPoint(),
					requestEntity, String.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Un pausing pipeline
		try {
			unPausePipeline(pipelineGroup.getPipeline().getName());
		} catch (Exception ex) {
			log.error("Error while unpausing or pipeline might be unpaused already", ex);
		}
		log.info(response);
	}

	public void pausePipeline(String name) {
		managePipeline(name, "pause");
	}

	public void unPausePipeline(String name) {
		managePipeline(name, "unpause");
	}

	private void managePipeline(String name, String action) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Confirm", "true");
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors()
		.add(new BasicAuthorizationInterceptor(config.getCicdAuthUserName(), config.getCicdAuthPassword()));
		HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
		ResponseEntity<String> responseEntity = restTemplate.exchange(config.getPipelineBaseUrl()
				+ config.getPipelineEndPoint() + File.separator + name + File.separator + action, HttpMethod.POST,
				requestEntity, String.class);
		log.debug(responseEntity.getBody());
	}

	public List<TestSuiteResponse> getTestSuiteEligibleForCancel(String testSuiteId, String variableId) {

		Query query = new Query(Criteria.where("testSuiteId").is(testSuiteId).and("configId").is(variableId).and("status").in(TestSuiteResponse.STATUSES.IN_PROGRESS.getValue(),TestSuiteResponse.STATUSES.SCHEDULED.getValue()));

		return mongoTemplate.find(query, TestSuiteResponse.class);
		//		for(TestSuiteResponse testSuite : find ){
		//			testSuite.getTestSuiteAgent()
		//		}


		//		TestSuite testSuite = getTestSuite(testSuiteId);
		//		Variables variables = getVariablesById(variableId);
		//
		//		HttpHeaders headers = new HttpHeaders();
		//		headers.set("Confirm", "true");
		//		RestTemplate restTemplate = new RestTemplate();
		//		restTemplate.getInterceptors()
		//		.add(new BasicAuthorizationInterceptor(config.getCicdAuthUserName(), config.getCicdAuthPassword()));
		//		HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
		//		ResponseEntity<String> responseEntity = restTemplate
		//				.exchange(config.getPipelineBaseUrl() + config.getCancelPipelineEndPoint()
		//				.replaceAll(":pipelineName", testSuite.getName() + "_" + variables.getName())
		//				.replaceAll(":stageName", "Trigger"), HttpMethod.POST, requestEntity, String.class);
		//		log.info(responseEntity.getBody());
		//		saveTestSuiteResponse(new TestSuiteResponse(testSuiteId, variableId, testSuite, "Cancelled"));
		//return null;
	}

	public HttpHeaders getCommonHttpHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Accept", "application/vnd.go.cd.v4+json");
		return headers;
	}

	public Variables getVariablesById(String id) {
		Query query = new Query(Criteria.where("id").is(id));
		Variables var = mongoTemplate.findOne(query, Variables.class);
		return var;
	}

	public TestSuiteResponse getTestSuiteResponseById(String testsuiteResponseid) {
		Query query = new Query(Criteria.where("_id").is(testsuiteResponseid));
		TestSuiteResponse response = mongoTemplate.findOne(query, TestSuiteResponse.class);

		if (response != null && response.getTestSuite() != null && response.getTestSuite().getScenarios() != null) {
			response = populateDuration(response);
		}
		response.getTestSuite().setExecutionStatus(null);
		return response;
	}

	private TestSuiteResponse populateDuration(TestSuiteResponse response) {
		if (response != null && response.getTestSuite() != null && response.getTestSuite().getScenarios() != null) {
			Long testSuiteDurationTotal = new Long(0);
			for (Scenario scenario : response.getTestSuite().getScenarios()) {
				Long total = new Long(0);
				for (TestCase testCase : scenario.getTestCases()) {
					if (testCase.getDuration() != null) {
						total += testCase.getDuration();
					}
				}
				testSuiteDurationTotal += total;
				scenario.setDuration(total);
			}
			response.getTestSuite().setDuration(testSuiteDurationTotal);
		}
		return response;
	}

	public TestSuiteHistoryResponse getTestSuiteResponse(String testsuiteid, int offset) {
		TestSuiteHistoryResponse historyResponse = new TestSuiteHistoryResponse();
		Query query = new Query(Criteria.where("testSuiteId").is(testsuiteid))
				.with(Sort.by(Direction.DESC, "_id")).skip(offset > 0 ? ((offset - 1) * 10) : 0).limit(10);
		List<TestSuiteResponse> responses = mongoTemplate.find(query, TestSuiteResponse.class);
		if (responses != null) {
			for (TestSuiteResponse response : responses) {
				response.setTestSuiteName(response.getTestSuite().getName());
				response.setConfigName(getVariablesById(response.getConfigId()).getName());
				response.setSuccessRate(response.getTestSuite().getSuccessRate());
				response.setTestStatus(response.getTestSuite().getStatus());
				response.setDuration(populateDuration(response).getTestSuite().getDuration());
				response.setTestSuite(null);
			}
			query = new Query(Criteria.where("testSuiteId").is(testsuiteid));
			Long counter = mongoTemplate.count(query, TestSuiteResponse.class);
			Pagination pagination = new Pagination();
			pagination.setOffset(offset);
			pagination.setTotal(counter);
			pagination.setPageSize(10);
			historyResponse.setPagination(pagination);
			historyResponse.setResponses(responses);
		}
		return historyResponse;
	}

	public void saveTestSuiteResponse(TestSuiteResponse testSuiteResponse) {
		String testSuiteExecutionStatus = testSuiteResponse.getStatus();
		if (testSuiteExecutionStatus != null && (!testSuiteResponse.isManual())
				&& (testSuiteExecutionStatus.equalsIgnoreCase(TestSuiteResponse.STATUSES.COMPLETED.getValue()))
				|| testSuiteExecutionStatus.equalsIgnoreCase(TestSuiteResponse.STATUSES.CANCELLED.getValue())
				|| (testSuiteExecutionStatus.equalsIgnoreCase(TestSuiteResponse.STATUSES.IN_PROGRESS.getValue())
						&& testSuiteResponse.getCounter() != null)) {
			Query query = new Query(Criteria.where("testSuiteId").is(testSuiteResponse.getTestSuiteId()).and("configId")
					.is(testSuiteResponse.getConfigId()).and("status").is(TestSuiteResponse.STATUSES.IN_PROGRESS.getValue()));
			List<TestSuiteResponse> responses = mongoTemplate.find(query, TestSuiteResponse.class);
			for (TestSuiteResponse response : responses) {
				response.setMts(System.currentTimeMillis());
				response.setStatus(testSuiteResponse.getStatus());
				response.setCounter(testSuiteResponse.getCounter());
				response.setTestSuite(testSuiteResponse.getTestSuite());
				mongoTemplate.save(response);
				if (testSuiteExecutionStatus.equalsIgnoreCase(TestSuiteResponse.STATUSES.COMPLETED.getValue())
						|| testSuiteExecutionStatus.equalsIgnoreCase(TestSuiteResponse.STATUSES.CANCELLED.getValue())) {
					saveDashboardStats(response);
				}
			}
		} else {
			mongoTemplate.save(testSuiteResponse);
		}
	}

	private void saveDashboardStats(TestSuiteResponse testSuiteResponse) {
		DashboardStats stats = new DashboardStats(testSuiteResponse.getTestSuiteId(), testSuiteResponse.getConfigId(),
				testSuiteResponse.getCts(), testSuiteResponse.getMts(), testSuiteResponse.getCreatedUserName(),
				testSuiteResponse.getModifiedUserName(), testSuiteResponse.getCreatedBy(),
				testSuiteResponse.getModifiedBy(), testSuiteResponse.getSuccessRate(),
				testSuiteResponse.getTestSuite().getStatus(), testSuiteResponse.getTestSuiteName());
		mongoTemplate.save(stats);

		DashboardSummary summary = mongoTemplate.findOne(
				new Query(Criteria.where("testSuiteName").is(testSuiteResponse.getTestSuite().getName())),
				DashboardSummary.class);

		if (testSuiteResponse.getStatus() != null && testSuiteResponse.getTestSuite() != null) {
			if (summary == null) {
				if (testSuiteResponse.getTestSuite().getStatus().equalsIgnoreCase("PASS")) {
					mongoTemplate.save(new DashboardSummary(testSuiteResponse.getTestSuiteId(),
							testSuiteResponse.getTestSuite().getName(), 1, 0, 0,
							testSuiteResponse.getTestSuite().getSuccessRate()));
				} else if (testSuiteResponse.getTestSuite().getStatus().equalsIgnoreCase("FAIL")) {
					mongoTemplate.save(new DashboardSummary(testSuiteResponse.getTestSuiteId(),
							testSuiteResponse.getTestSuite().getName(), 0, 1, 0,
							testSuiteResponse.getTestSuite().getSuccessRate()));
				} else {
					mongoTemplate.save(new DashboardSummary(testSuiteResponse.getTestSuiteId(),
							testSuiteResponse.getTestSuite().getName(), 0, 0, 1,
							testSuiteResponse.getTestSuite().getSuccessRate()));
				}
			} else {
				if (testSuiteResponse.getTestSuite().getStatus().equalsIgnoreCase("PASS")) {
					summary.setSuccessCount(summary.getSuccessCount() + 1);
					summary.setSuccessRatio(
							(summary.getSuccessRatio() + testSuiteResponse.getTestSuite().getSuccessRate()) / 2);
				} else if (testSuiteResponse.getTestSuite().getStatus().equalsIgnoreCase("FAIL")) {
					summary.setFailureCount(summary.getFailureCount() + 1);
					summary.setSuccessRatio(
							(summary.getSuccessRatio() + testSuiteResponse.getTestSuite().getSuccessRate()) / 2);
				} else {
					summary.setCancelledCount(summary.getCancelledCount() + 1);
				}
				mongoTemplate.save(summary);
			}
		}
	}

	public String getTestSuiteExecutionStatus(String testsuiteResponseid) {
		Query query = new Query(Criteria.where("_id").is(testsuiteResponseid));
		TestSuiteResponse response = mongoTemplate.findOne(query, TestSuiteResponse.class);
		return response.getStatus();
	}

	public TestSuiteHistoryResponse getTestSuiteHistory(String testSuiteId, String configId, int offset, String user,
			String range) throws ParseException {
		Query query = null, countQuery = null;
		TestSuiteHistoryResponse historyResponse = new TestSuiteHistoryResponse();
		if (user == null && range == null) {
			query = new Query(Criteria.where("testSuiteId").is(testSuiteId).and("configId").is(configId))
					.with(Sort.by(Direction.DESC, "_id")).skip(offset > 0 ? ((offset - 1) * 10) : 0)
					.limit(10);
			countQuery = new Query(Criteria.where("testSuiteId").is(testSuiteId));
		} else if (range != null) {
			String[] dates = range.split("~");
			Date startDate = null;
			Date endDate = null;
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

			if (dates != null && dates.length > 0) {
				startDate = dateFormat.parse(dates[0]);
				endDate = dateFormat.parse(dates[1]);
			}
			if (startDate.compareTo(endDate) <= 0) {
				if (user == null) {
					query = new Query(Criteria.where("testSuiteId").is(testSuiteId).and("configId").is(configId)
							.and("cts").gte(new Long(DateUtil.getStartOfDay(startDate).getTime() + ""))
							.lte(new Long(DateUtil.getEndOfDay(endDate).getTime() + "")))
							.with(Sort.by(Direction.DESC, "_id"))
							.skip(offset > 0 ? ((offset - 1) * 10) : 0).limit(10);
					countQuery = new Query(Criteria.where("testSuiteId").is(testSuiteId).and("configId").is(configId)
							.and("cts").gte(new Long(DateUtil.getStartOfDay(startDate).getTime() + ""))
							.lte(new Long(DateUtil.getEndOfDay(endDate).getTime() + "")));
				} else {
					query = new Query(
							Criteria.where("testSuiteId").is(testSuiteId).and("configId").is(configId).and("createdBy")
							.is(user).and("cts").gte(new Long(DateUtil.getStartOfDay(startDate).getTime() + ""))
							.lte(new Long(DateUtil.getEndOfDay(endDate).getTime() + "")))
							.with(Sort.by(Direction.DESC, "_id"))
							.skip(offset > 0 ? ((offset - 1) * 10) : 0).limit(10);
					countQuery = new Query(
							Criteria.where("testSuiteId").is(testSuiteId).and("configId").is(configId).and("createdBy")
							.is(user).and("cts").gte(new Long(DateUtil.getStartOfDay(startDate).getTime() + ""))
							.lte(new Long(DateUtil.getEndOfDay(endDate).getTime() + "")));
				}
			}
		} else if (user != null) {
			query = new Query(Criteria.where("testSuiteId").is(testSuiteId).and("configId").is(configId)
					.and("createdBy").is(user));
		}
		List<TestSuiteResponse> responses = mongoTemplate.find(query, TestSuiteResponse.class);
		for (TestSuiteResponse response : responses) {
			response.setTestSuiteName(response.getTestSuite().getName());
			response.setConfigName(getVariablesById(response.getConfigId()).getName());
			response.setSuccessRate(response.getTestSuite().getSuccessRate());
			response.setTestStatus(response.getTestSuite().getStatus());
			response.setDuration(response.getTestSuite().getDuration());
			response.setTestSuite(null);
		}

		if (responses != null) {
			Long counter = mongoTemplate.count(countQuery, TestSuiteResponse.class);
			Pagination pagination = new Pagination();
			pagination.setOffset(offset);
			pagination.setTotal(counter);
			pagination.setPageSize(10);
			historyResponse.setPagination(pagination);
			historyResponse.setResponses(responses);
		}
		return historyResponse;
	}

	public TestSuiteHistoryResponse getTestSuiteHistory(String testSuiteId, String configId, int offset)
			throws ParseException {
		Query query = null, countQuery = null;
		TestSuiteHistoryResponse historyResponse = new TestSuiteHistoryResponse();
		query = new Query(Criteria.where("testSuiteId").is(testSuiteId).and("configId").is(configId))
				.with(Sort.by(Direction.DESC, "_id")).skip(offset > 0 ? ((offset - 1) * 10) : 0).limit(10);
		countQuery = new Query(Criteria.where("testSuiteId").is(testSuiteId).and("configId").is(configId));

		List<TestSuiteResponse> responses = mongoTemplate.find(query, TestSuiteResponse.class);
		for (TestSuiteResponse response : responses) {
			response.setTestSuiteName(response.getTestSuite().getName());
			response.setConfigName(getVariablesById(response.getConfigId()).getName());
			response.setSuccessRate(response.getTestSuite().getSuccessRate());
			response.setTestStatus(response.getTestSuite().getStatus());
			response.setDuration(response.getTestSuite().getDuration());
			response.setTestSuite(null);
		}

		if (responses != null) {
			Long counter = mongoTemplate.count(countQuery, TestSuiteResponse.class);
			Pagination pagination = new Pagination();
			pagination.setOffset(offset);
			pagination.setTotal(counter);
			pagination.setPageSize(10);
			historyResponse.setPagination(pagination);
			historyResponse.setResponses(responses);
		}
		return historyResponse;
	}

	public void pauseTestSuite(String testSuiteId) {
		TestSuite testSuite = getTestSuite(testSuiteId);
		// pausePipeline(testSuite.getName() + "_" + variables.getName());
		testSuite.setActive(false);
		mongoTemplate.save(testSuite);
	}

	public void unpauseTestSuite(String testSuiteId) {
		TestSuite testSuite = getTestSuite(testSuiteId);
		// unPausePipeline(testSuite.getName() + "_" + variables.getName());
		testSuite.setActive(true);
		mongoTemplate.save(testSuite);
	}

	public String getRuntimeLogs(String testsuiteExecutionId) {
		RestTemplate restTemplate = new RestTemplate();
		TestSuiteResponse testSuiteResponse = getTestSuiteResponseById(testsuiteExecutionId);
		TestSuite testSuite = getTestSuite(testSuiteResponse.getTestSuiteId());
		Variables variables = getVariablesById(testSuiteResponse.getConfigId());
		restTemplate.getInterceptors()
		.add(new BasicAuthorizationInterceptor(config.getCicdAuthUserName(), config.getCicdAuthPassword()));
		return restTemplate.getForObject(config.getPipelineBaseUrl() + config.getTestSuitePipelineLogUrl()
		.replaceAll(":pipelineName", testSuite.getName() + "_" + variables.getName())
		.replaceAll(":buildNumber", testSuiteResponse.getCounter()), String.class);
	}

	public void createSchedule(TestSuiteSchedule schedule) {
		updateSchedule(schedule);
	}

	public void updateSchedule(TestSuiteSchedule schedule) {
		Query query = new Query(
				Criteria.where("testSuiteId").is(schedule.getTestSuiteId()).and("configId").is(schedule.getConfigId()));
		List<TestSuiteSchedule> schedules = mongoTemplate.find(query, TestSuiteSchedule.class);
		for (TestSuiteSchedule testSuiteSchedule : schedules) {
			deleteSchedule(testSuiteSchedule);
		}
		mongoTemplate.save(schedule);
	}

	public void deleteSchedule(TestSuiteSchedule schedule) {
		deleteSchedule(schedule.getTestSuiteId(), schedule.getConfigId());
	}

	public void deleteSchedule(String testSuiteId, String configId) {
		mongoTemplate.remove(new Query(Criteria.where("testSuiteId").is(testSuiteId).and("configId").is(configId)),
				TestSuiteSchedule.class);
	}


	public List<TestSuiteSchedule> getTestSuiteSchedule(String testSuiteId) {
		List<TestSuiteSchedule> schedules = null;
		if (testSuiteId == null) {
			schedules = mongoTemplate.findAll(TestSuiteSchedule.class);
		} else {
			Query query = new Query(Criteria.where("testSuiteId").is(testSuiteId));
			schedules = mongoTemplate.find(query, TestSuiteSchedule.class);
		}
		if (schedules != null) {
			for (TestSuiteSchedule schedule : schedules) {
				Query query = new Query(Criteria.where("testSuiteId").is(testSuiteId));
				TestSuite testSuite = mongoTemplate.findOne(query, TestSuite.class);
				if (testSuite != null) {
					schedule.setTestSuiteName(testSuite.getName());
				}
				Variables vars = getVariablesById(schedule.getConfigId());
				if (vars != null) {
					schedule.setEnvName(vars.getName());
				}
			}
		}
		return schedules;
	}

	public TestSuiteAnalysis getAnalysis(String testSuiteId, String configId, String dateStr)
			throws ParseException, ItorixException {
		List<TestSuiteResponse> responses = null;
		Query query = null;
		if (dateStr != null && dateStr.length() > 0) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			String dates[] = dateStr.split("~");
			Date startDate = null;
			Date endDate = null;

			if (dates != null && dates.length > 0) {
				startDate = dateFormat.parse(dates[0]);
				endDate = dateFormat.parse(dates[1]);
			}

			query = new Query(Criteria.where("testSuiteId").is(testSuiteId).and("configId").is(configId).and("cts")
					.gte(new Long(DateUtil.getStartOfDay(startDate).getTime() + ""))
					.lte(new Long(DateUtil.getEndOfDay(endDate).getTime() + "")));
		} else {
			query = new Query(Criteria.where("testSuiteId").is(testSuiteId).and("configId").is(configId));
		}
		responses = mongoTemplate.find(query, TestSuiteResponse.class);
		return analyze(responses);
	}

	public Dashboard getDashboardDetails(String daterange, String timeunit) throws ParseException, ItorixException {
		if (timeunit != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			String[] dates = daterange.split("~");
			Date startDate = null;
			Date endDate = null;

			if (dates != null && dates.length > 0) {
				startDate = dateFormat.parse(dates[0]);
				endDate = dateFormat.parse(dates[1]);
			}
			// if (startDate.compareTo(endDate) <= 0) {
			// Query query = new Query(
			// Criteria.where("cts").gte(new
			// Long(DateUtil.getStartOfDay(startDate).getTime() + ""))
			// .lte(new Long(DateUtil.getEndOfDay(endDate).getTime() + "")));
			// List<TestSuiteResponse> responses = mongoTemplate.find(query,
			// TestSuiteResponse.class);
			// dashboard = buildDashboard(responses);
			// } else {
			// throw new ItorixException("Invalid Date Range", "Config-1004");
			// }
		} else {
			List<DashboardSummary> summaries = mongoTemplate.findAll(DashboardSummary.class);
			if (summaries != null) {
				int successCount = 0, failureCount = 0, cancelledCount = 0;
				for (DashboardSummary summary : summaries) {
					successCount += summary.getSuccessCount();
					failureCount += summary.getFailureCount();
					cancelledCount += summary.getCancelledCount();
				}

				return new Dashboard(successCount + failureCount + cancelledCount, successCount, failureCount,
						cancelledCount,
						new Stats(getTestSuites(summaries, "topSuccess"), getTestSuites(summaries, "topFailures")));

			}
		}

		return new Dashboard();
	}

	private TestSuiteAnalysis analyze(List<TestSuiteResponse> responses) {
		TestSuiteAnalysis analysis = null;
		if (responses != null && responses.size() > 0) {
			List<TestSuiteStats> stats = new ArrayList<>();
			for (TestSuiteResponse response : responses) {
				if (response != null && response.getStatus() != null
						&& response.getStatus().equalsIgnoreCase(TestSuiteResponse.STATUSES.COMPLETED.getValue()) && response.getTestSuite() != null) {
					List<ScenarioStats> scenarioStats = new ArrayList<>();
					for (Scenario scenario : response.getTestSuite().getScenarios()) {
						List<TestCaseStats> testCasestats = new ArrayList<>();
						for (TestCase testCase : scenario.getTestCases()) {
							if (testCase.getStatus() != null) {
								if (testCase.getStatus().equalsIgnoreCase("PASS")) {
									testCasestats.add(new TestCaseStats(testCase.getName(), new Double(100.00),
											testCase.getDuration().doubleValue()));
								} else if (testCase.getStatus().equalsIgnoreCase("FAIL")) {
									testCasestats.add(new TestCaseStats(testCase.getName(), new Double(0.00),
											testCase.getDuration().doubleValue()));
								}
							}
						}
						if (scenario.getStatus() != null) {
							Double duration = 0.0;
							if (scenario.getDuration() != null) {
								duration = scenario.getDuration().doubleValue();
							}
							scenarioStats.add(new ScenarioStats(scenario.getName(), scenario.getSuccessRate(), duration,
									testCasestats));
						}
					}
					stats.add(new TestSuiteStats(response.getMts(), response.getTestSuite().getId(),
							response.getTestSuite().getName(), response.getTestSuite().getSuccessRate(),
							response.getTestSuite().getDuration().doubleValue(), scenarioStats));
				}
				analysis = new TestSuiteAnalysis(stats);
			}
		}

		return analysis;
	}

	private List<DashboardSummary> getTestSuites(List<DashboardSummary> responses, String type) {
		if (type.equalsIgnoreCase("topSuccess")) {
			responses.sort(Comparator.comparing(DashboardSummary::getSuccessRatio,
					Comparator.nullsLast(Comparator.reverseOrder())));
			return responses.stream().limit(10).collect(Collectors.toList());
		} else if (type.equalsIgnoreCase("topFailures")) {
			List<DashboardSummary> filteredSummary = responses.stream().filter(t -> t.getSuccessRatio() < 100.0)
					.collect(Collectors.toList());
			filteredSummary.sort(Comparator.comparing(DashboardSummary::getSuccessRatio,
					Comparator.nullsLast(Comparator.naturalOrder())));
			return filteredSummary.stream().limit(10).collect(Collectors.toList());
		}
		return responses;
	}

	public Boolean getTestSuiteStatus(String testSuiteId) {
		TestSuite testSuite = getTestSuite(testSuiteId);
		if (testSuite == null) {
			return false;
		}
		return testSuite.getActive();
	}

	public Object searchForTestSuite(String name, int limit) throws ItorixException {
		BasicQuery query = new BasicQuery("{\"name\": {$regex : '" + name + "', $options: 'i'}}");
		query.limit(limit > 0 ? limit : 10);
		List<TestSuite> allTestSuite = mongoTemplate.find(query, TestSuite.class);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode response = mapper.createObjectNode();
		ArrayNode responseFields = mapper.createArrayNode();
		for (TestSuite vo : allTestSuite) {
			SearchItem searchItem = new SearchItem();
			searchItem.setId(vo.getId());
			searchItem.setName(vo.getName());
			responseFields.addPOJO(searchItem);
		}
		response.set("TestSuite", responseFields);
		return response;
	}

	public void deleteTestSuiteResponse(String id) {
		mongoTemplate.remove(new Query(Criteria.where("id").is(id)), TestSuiteResponse.class);
	}

	public void createOrUpdateMaskingFields(MaskFields maskingFilelds) {
		Query query = new Query();
		query.addCriteria(Criteria.where("fields").exists(true));
		Update update = new Update();
		update.set("fields", maskingFilelds.getFields());
		mongoTemplate.upsert(query, update, MaskFields.class);
	}

	public MaskFields getMaskingFields() {
		List<MaskFields> maskingFields = mongoTemplate.findAll(MaskFields.class);
		return maskingFields.isEmpty() ? null : maskingFields.get(0);
	}

	public List<Certificates> getCertificates(boolean names) {
		if (names) {
			Query searchQuery = new Query();
			searchQuery.fields().include("name");
			return mongoTemplate.find(searchQuery, Certificates.class);
		}
		Query searchQuery = new Query();
		searchQuery.fields().exclude("content").exclude("password");
		return mongoTemplate.find(searchQuery, Certificates.class);
	}

	public void deleteCertificate(String name) throws ItorixException {
		if(mongoTemplate.remove(new Query(Criteria.where("name").is(name)),
				Certificates.class).getDeletedCount() == 0){
			throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-15"), "Testsuite-15");
		}
	}

	public List<TestSuite> getCertificateReference(String name){
		Query searchQuery = new Query(Criteria.where("certificateName").is(name));
		searchQuery.fields().include("name");
		return mongoTemplate.find(searchQuery, TestSuite.class);
	}

	public Certificates getCertificate(String name){
		Query searchQuery = new Query(Criteria.where("name").is(name));
		searchQuery.fields().exclude("content").exclude("password");
		return mongoTemplate.findOne(searchQuery, Certificates.class);
	}

	public void createOrUpdateCertificate(String name, byte[] jKSFile, String description, String password,
			String alias, String jsessionid) throws ItorixException {
		if (StringUtils.hasText(password) && (jKSFile != null && jKSFile.length > 0)) {
			try {
				KeyStore ks = KeyStore.getInstance("jks");
				ks.load(new ByteArrayInputStream(jKSFile), password.toCharArray());
			} catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
				log.error("Issue in uploaded certificate", e);
				throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-12"), "Testsuite-12");
			}
		}

		Update update = new Update();
		update.set("name", name);
		update.set("content", jKSFile);
		update.set("description", description);
		try {
			if(StringUtils.hasText(password)){
				update.set("password", new RSAEncryption().encryptText(password));
			} else {
				update.set("password", password);
			}
		} catch (Exception e) {
			log.error("exception during pwd encryption" , e);
		}
		update.set("alias", alias);

		Query query = new Query(Criteria.where("name").is(name));

		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		if(CollectionUtils.isEmpty(mongoTemplate.find(query, Certificates.class))){
			update.set("cts", System.currentTimeMillis());
			update.set("createdBy", user.getFirstName() + " " + user.getLastName());
		} else {
			update.set("mts", System.currentTimeMillis());
			update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());
		}

		mongoTemplate.upsert(query, update, Certificates.class);
	}

	public byte[] downloadCertificate(String name) {
		Query searchQuery = new Query(Criteria.where("name").is(name));
		searchQuery.fields().include("content");
		Certificates certificate = mongoTemplate.findOne(searchQuery, Certificates.class);
		if(certificate != null){
			return certificate.getContent();
		}
		return null;
	}
}