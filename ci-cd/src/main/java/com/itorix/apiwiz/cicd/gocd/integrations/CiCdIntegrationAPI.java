package com.itorix.apiwiz.cicd.gocd.integrations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.cicd.beans.Package;
import com.itorix.apiwiz.cicd.beans.*;
import com.itorix.apiwiz.cicd.dao.PipelineDao;
import com.itorix.apiwiz.cicd.dashboard.beans.CicdDashBoardResponse;
import com.itorix.apiwiz.cicd.dashboard.beans.PipeLineDashBoard;
import com.itorix.apiwiz.cicd.gocd.beans.Material;
import com.itorix.apiwiz.cicd.gocd.beans.Pipeline;
import com.itorix.apiwiz.cicd.gocd.beans.Stage;
import com.itorix.apiwiz.cicd.gocd.beans.*;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.integrations.Integration;
import com.itorix.apiwiz.common.model.integrations.gocd.GoCDIntegration;
import com.itorix.apiwiz.common.model.integrations.workspace.WorkspaceIntegration;
import com.itorix.apiwiz.common.model.projectmanagement.Project;
import com.itorix.apiwiz.common.model.slack.*;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.Date.DateUtil;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.common.util.mail.EmailTemplate;
import com.itorix.apiwiz.common.util.mail.MailUtil;
import com.itorix.apiwiz.common.util.scheduler.Schedule;
import com.itorix.apiwiz.common.util.slack.SlackUtil;
import com.itorix.apiwiz.devstudio.dao.IntegrationsDao;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContextHolder;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.itorix.apiwiz.projectmanagement.dao.ProjectManagementDao;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCursor;
import net.sf.saxon.trans.Err;
import org.springframework.util.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class created to handle GOCD pipeline APIs
 *
 * @author vphani
 */
@Component("cicdIntegrationApi")
public class CiCdIntegrationAPI {


	@Autowired
	SlackUtil slackUtil;
	private static final Logger logger = LoggerFactory.getLogger(CiCdIntegrationAPI.class);

	@Value("${itorix.core.gocd.build.apigee.base.directory}")
	private String BASE_DIR;

	private static final String PARTNER_NAME = "apiproxy";

	@Autowired
	private ApplicationProperties config;

	@Autowired
	private MailUtil mailUtil;

	@Autowired
	ProjectManagementDao projectPlanAndTrackService;

	@Autowired
	private PipelineDao pipelineDao;

	@Autowired
	public MongoTemplate mongoTemplate;

	@Autowired
	private IntegrationsDao integrationsDao;

	@Value("${itorix.core.scheduler.enable:null}")
	private String scheduleEnable;

	@Value("${itorix.core.scheduler.primary:null}")
	private String primary;

	@Value("${itorix.core.scheduler.primary.host:null}")
	private String primaryHost;

	@Autowired
	private IdentityManagementDao identityManagementDao;

	/**
	 * @param pipelineGroups
	 * @param isNew
	 * 
	 * @throws java.lang.Exception
	 */
	public void createOrEditPipeline(PipelineGroups pipelineGroups, boolean isNew) throws Exception {
		GoCDIntegration goCDIntegration = getGocdIntegration();
		if (pipelineGroups == null) {
			return;
		}
		if (!(pipelineGroups.getPipelines().size() > 0)) {
			return;
		}

		// PipelineGroup pipelineGroup = new
		// PipelineGroup(pipelineGroups.getProjectName());
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		PipelineGroup pipelineGroup = new PipelineGroup(userSessionToken.getWorkspaceId());

		Pipeline pipeline = new Pipeline(getPipelineName(pipelineGroups), getPipelineName(pipelineGroups) + "-${count}",
				false);
		String projectName = pipelineGroups.getPipelines().get(0).getDefineName().replaceAll(" ", "%20");

		// Creating and adding Materials for pipeline
		Material material = new Material();
		Attributes attributes = new Attributes();
		String scmURL = pipelineGroups.getPipelines().get(0).getMaterials().get(0).getScmURL();
		String scmType = pipelineGroups.getPipelines().get(0).getMaterials().get(0).getScmType();

		Map<String, String> scmDetails = getSCMDetails(scmType.toUpperCase(), "cicd-proxy");
		if(scmDetails!=null){
			String scmUserName = scmDetails.get("username");
			String scmPassword = scmDetails.get("password");
			String scmToken = scmDetails.get("token");
			String scmUsertype = scmDetails.get("userType");

			if (scmType != null && scmType != "" && scmType.equals("svn")) {
				logger.debug("Setting details to attributes");
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
				String auth = scmUserName + ":" + scmPassword;
				if (scmUsertype != null && scmUsertype.equalsIgnoreCase("TOKEN")) {
					auth = scmToken;
				}
				if (scmURL.startsWith("http://")) {
					scmURL = "http://" + auth + "@" + scmURL.replaceAll("http://", "");
				} else {
					scmURL = "https://" + auth + "@" + scmURL.replaceAll("https://", "");
				}
				attributes.setUrl(scmURL);
				attributes.setBranch(pipelineGroups.getPipelines().get(0).getMaterials().get(0).getScmBranch());
				attributes.setDestination("PipelineProxy");
				attributes.setInvertFilter(false);
				attributes.setName("ProxyPolling");
				attributes.setAutoUpdate(true);
				attributes.setShallowClone(true);
			}
			material.setAttributes(attributes);
		}
		else{
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("CICD-1003"),
					"Scm details with given data not found"),"CICD-1003");
		}
		// Adding build Materials
		Material buildMaterial = new Material();
		Attributes buildAttributes = new Attributes();

		String buildScmType = getBuildScmProp("itorix.core.gocd.build.scm.type");
		String buildScmURL = getBuildScmProp("itorix.core.gocd.build.scm.url");
		String buildScmBranch = getBuildScmProp("itorix.core.gocd.build.scm.branch");

		if(buildScmType!=null && buildScmURL!=null && buildScmBranch!=null){
			Map<String, String> buildScmDetails = getSCMDetails(buildScmType.toUpperCase(), "cicd-build");
			if(buildScmDetails!=null){
				String buildScmUserName = buildScmDetails.get("username");
				String buildScmPassword = buildScmDetails.get("password");
				String buildScmToken = buildScmDetails.get("token");
				String buildScmUsertype = buildScmDetails.get("userType");

				if (buildScmType != null && buildScmType != "" && buildScmType.equals("svn")) {
					logger.debug("Setting details to buildAttributes");
					buildMaterial.setType("svn");
					buildAttributes.setUrl(buildScmURL);
					buildAttributes.setDestination("PipelineBuild");
					buildAttributes.setInvertFilter(false);
					buildAttributes.setName("BuildPolling");
					buildAttributes.setAutoUpdate(false);
					buildAttributes.setUsername(buildScmUserName);
					buildAttributes.setPassword(buildScmPassword);
				} else {
					buildMaterial.setType("git");
					String auth = buildScmUserName + ":" + buildScmPassword;
					if (buildScmUsertype != null && buildScmUsertype.equalsIgnoreCase("TOKEN")) {
						auth = buildScmToken;
					}
					if (buildScmURL.startsWith("http://")) {
						buildScmURL = "http://" + auth + "@" + buildScmURL.replaceAll("http://", "");
					} else {
						buildScmURL = "https://" + auth + "@" + buildScmURL.replaceAll("https://", "");
					}
					buildAttributes.setUrl(buildScmURL);
					buildAttributes.setBranch(buildScmBranch);
					buildAttributes.setDestination("PipelineBuild");
					buildAttributes.setInvertFilter(false);
					buildAttributes.setName("BuildPolling");
					buildAttributes.setAutoUpdate(false);
					buildAttributes.setShallowClone(true);
					buildMaterial.setAttributes(buildAttributes);
				}
			}
			else {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("CICD-1003"),
						"Scm details with given data not found"),"CICD-1003");
			}
		}
		else {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("CICD-1003"),
					"Scm details with given data not found"),"CICD-1003");
		}


		List<Material> materials = new ArrayList<Material>();
		materials.add(material);
		materials.add(buildMaterial);
		pipeline.setMaterials(materials);

		// Set Pipeline Stages
		List<Stage> stages = new ArrayList<>();
		com.itorix.apiwiz.cicd.beans.Stage stage = pipelineGroups.getPipelines().get(0).getStages().get(0);
		String jobType = "success";
		if (stage != null && stage.getType() != null && stage.getType().equalsIgnoreCase("Manual")) {
			jobType = "manual";
		}
		Stage initialStage = new Stage(stage.getName(), true, false, false, jobType);
		List<Job> devJobs = new ArrayList<>();
		Job devJob = new Job("BuildAndDeploy");

		List<Task> tasks = new ArrayList<>();
		String proxyName = pipelineGroups.getPipelines().get(0).getProxyName();
		String version = pipelineGroups.getPipelines().get(0).getVersion();
		String org = pipelineGroups.getPipelines().get(0).getStages().get(0).getOrgName();
		String env = pipelineGroups.getPipelines().get(0).getStages().get(0).getEnvName();
		String gwType = pipelineGroups.getPipelines().get(0).getStages().get(0).getGwType();

		// Get Installation Type
		Boolean isSaaS = pipelineGroups.getPipelines().get(0).getStages().get(0).getIsSaas();
		if (isSaaS == null) {
			isSaaS = true;
		}

		// Retrieve Artifacts
		List<Artifact> artifacts = new ArrayList<>();
		tasks.add(new Task("pluggable_task", "passed", "copyPipelineDir", "-q -b PipelineBuild/copy.gradle",
				goCDIntegration.getGradleHome()));

		String proxyType = null;
		if (pipelineGroups.getPipelines().get(0).getType() == null
				|| pipelineGroups.getPipelines().get(0).getType().equalsIgnoreCase("proxies")) {
			proxyType = "proxies";
			artifacts.add(
					new Artifact(BASE_DIR + pipelineGroups.getPipelines().get(0).getProxyName() + ".zip", "", "build"));

			tasks.add(new Task("pluggable_task", "passed", "buildProxy",
					prepareGradleParams(BASE_DIR, PARTNER_NAME, proxyName, version, org, env,
							config.getApigeeUserName(), config.getApigeePassword(), 0, 0, false, null, isSaaS,
							projectName, gwType),
					goCDIntegration.getGradleHome()));
			tasks.add(new Task("pluggable_task", "passed", "deployProxy",
					prepareGradleParams(BASE_DIR, PARTNER_NAME, proxyName, version, org, env,
							config.getApigeeUserName(), config.getApigeePassword(), 0, 0, true, null, isSaaS,
							projectName, gwType),
					goCDIntegration.getGradleHome()));

			if (pipelineGroups.getPipelines() != null && pipelineGroups.getPipelines().get(0) != null && pipelineGroups
					.getPipelines().get(0).getStages().get(0).getUnitTests().getEnabled().equals("true")) {
				int unitTestsAcceptancePer = pipelineGroups.getPipelines().get(0).getStages().get(0).getUnitTests()
						.getAcceptance();
				if (pipelineGroups.getPipelines().get(0).getStages().get(0).getUnitTests().getArtifactType() != null
						&& pipelineGroups.getPipelines().get(0).getStages().get(0).getUnitTests()
								.getArtifactType() == ArtifactType.Testsuite) {
					List<TestSuiteAndConfig> testsuites = pipelineGroups.getPipelines().get(0).getStages().get(0)
							.getUnitTests().getTestSuites();
					for (TestSuiteAndConfig testsuite : testsuites) {
						tasks.add(new Task("pluggable_task", "passed", "executeUnitTestCases",
								prepareGradleParams(BASE_DIR, PARTNER_NAME, proxyName, version, org, env,
										config.getApigeeUserName(), config.getApigeePassword(),
										testsuite.getTestSuiteId(), testsuite.getEnvironmentId(),
										unitTestsAcceptancePer, 0, true, ArtifactType.Testsuite, isSaaS, projectName),
								goCDIntegration.getGradleHome()));
					}
				} else {
					tasks.add(new Task("pluggable_task", "passed", "executeUnitTestCases",
							prepareGradleParams(BASE_DIR, PARTNER_NAME, proxyName, version, org, env,
									config.getApigeeUserName(), config.getApigeePassword(), unitTestsAcceptancePer, 0,
									true,
									pipelineGroups.getPipelines().get(0).getStages().get(0).getUnitTests()
											.getArtifactType(),
									isSaaS, projectName, gwType),
							goCDIntegration.getGradleHome()));
				}
				artifacts.add(new Artifact(BASE_DIR + "unitTest.html", "", "build"));
			}

			if (pipelineGroups.getPipelines() != null && pipelineGroups.getPipelines().get(0) != null && pipelineGroups
					.getPipelines().get(0).getStages().get(0).getCodeCoverage().getEnabled().equals("true")) {
				int codeCoverageAcceptancePer = pipelineGroups.getPipelines().get(0).getStages().get(0)
						.getCodeCoverage().getAcceptance();
				if (pipelineGroups.getPipelines().get(0).getStages().get(0).getCodeCoverage().getArtifactType() != null
						&& pipelineGroups.getPipelines().get(0).getStages().get(0).getCodeCoverage()
								.getArtifactType() == ArtifactType.Testsuite) {
					List<TestSuiteAndConfig> testsuites = pipelineGroups.getPipelines().get(0).getStages().get(0)
							.getCodeCoverage().getTestSuites();
					for (TestSuiteAndConfig testsuite : testsuites) {
						tasks.add(new Task("pluggable_task", "passed", "executeCodeCoverage",
								prepareGradleParams(BASE_DIR, PARTNER_NAME, proxyName, version, org, env,
										config.getApigeeUserName(), config.getApigeePassword(),
										testsuite.getTestSuiteId(), testsuite.getEnvironmentId(), 0,
										codeCoverageAcceptancePer, true, ArtifactType.Testsuite, isSaaS, projectName),
								goCDIntegration.getGradleHome()));
					}
				} else {
					tasks.add(new Task("pluggable_task", "passed", "executeCodeCoverage",
							prepareGradleParams(BASE_DIR, PARTNER_NAME, proxyName, version, org, env,
									config.getApigeeUserName(), config.getApigeePassword(), 0,
									codeCoverageAcceptancePer, true,
									pipelineGroups.getPipelines().get(0).getStages().get(0).getCodeCoverage()
											.getArtifactType(),
									isSaaS, projectName, gwType),
							goCDIntegration.getGradleHome()));
				}
				artifacts.add(new Artifact(BASE_DIR + "codeCoverage.html", "", "build"));
			}

			String name = pipelineGroups.getPipelines().get(0).getProjectName() != null
					? pipelineGroups.getPipelines().get(0).getProjectName()
					: "";
			tasks.add(new Task("exec", "passed", "./email",
					config.getAppUrl() + " " + "Pass" + " " + name.replaceAll(" ", "%20"), null, true));
			tasks.add(new Task("exec", "failed", "./email",
					config.getAppUrl() + " " + "Failure" + " " + name.replaceAll(" ", "%20"), null, true));
			if (pipelineGroups.getPipelines() != null && pipelineGroups.getPipelines().get(0) != null
					&& (pipelineGroups.getPipelines().get(0).getStages().get(0).getUnitTests().getEnabled()
							.equals("true")
							|| pipelineGroups.getPipelines().get(0).getStages().get(0).getCodeCoverage().getEnabled()
									.equals("true"))) {
				tasks.add(new Task("pluggable_task", "failed", "publishOldVersion",
						prepareGradleParams(BASE_DIR, PARTNER_NAME, proxyName, version, org, env,
								config.getApigeeUserName(), config.getApigeePassword(), 0, 0, true, null, isSaaS,
								projectName, gwType),
						goCDIntegration.getGradleHome()));
			}
		} else if (pipelineGroups.getPipelines().get(0).getType() != null
				&& pipelineGroups.getPipelines().get(0).getType().equalsIgnoreCase("sharedflow")) {
			proxyType = "sharedflow";
			artifacts.add(
					new Artifact(BASE_DIR + pipelineGroups.getPipelines().get(0).getProxyName() + ".zip", "", "build"));
			tasks.add(new Task("pluggable_task", "passed", "deploySharedFlow",
					prepareGradleParamsSharedFlow(".", "SharedFlow", proxyName, version, org, env,
							config.getApigeeUserName(), config.getApigeePassword(), 0, 0, false, isSaaS),
					goCDIntegration.getGradleHome()));
			tasks.add(new Task("exec", "passed", "./email", config.getAppUrl() + " " + "Pass", null, true));
			tasks.add(new Task("exec", "failed", "./email", config.getAppUrl() + " " + "Failure", null, true));
		}

		devJob.setTasks(tasks);
		devJobs.add(devJob);
		initialStage.setJobs(devJobs);
		devJob.setArtifacts(artifacts);
		stages.add(initialStage);

		List<com.itorix.apiwiz.cicd.beans.Stage> inputStages = pipelineGroups.getPipelines().get(0).getStages();
		for (int i = 1; i < inputStages.size(); i++) {
			if (proxyType.equalsIgnoreCase("sharedflow")) {
				stages.add(createSharedFlowStage(inputStages.get(i), pipelineGroups, stage.getName()));
			} else {
				stages.add(createStage(inputStages.get(i), pipelineGroups, stage.getName(), projectName));
			}
		}

		// Set Pipeline Stages
		pipeline.setStages(stages);
		pipelineGroup.setPipeline(pipeline);

		// Invoke API and throw exception in case of issues/errors for API
		// creation
		// GoCDIntegration integrations = getGocdIntegration();
		String goHost = config.getPipelineBaseUrl();
		String userName = config.getCicdAuthUserName();
		String password = config.getCicdAuthPassword();
		String gocdVersion = config.getGocdVersion();
		if (goCDIntegration != null) {
			goHost = goCDIntegration.getHostURL();
			userName = goCDIntegration.getUsername();
			password = goCDIntegration.getPassword();
			gocdVersion = goCDIntegration.getVersion();
		}

		RestTemplate restTemplate = new RestTemplate();
		ObjectMapper mapper = new ObjectMapper();
		restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor(userName, password));
		String response = null;

		if (isNew) {
			logger.debug(mapper.writeValueAsString(pipelineGroup));
			HttpEntity<PipelineGroup> requestEntity = new HttpEntity<>(pipelineGroup,
					getCommonHttpHeaders(CiCdIntegrationHelper.CREATE_EDIT, gocdVersion));
			String endpoint = goCDIntegration.getHostURL();
			logger.debug("Making a call to {}", goHost + config.getPipelineAdminEndPoint());
			try{
				response = restTemplate.postForObject(goHost + config.getPipelineAdminEndPoint(), requestEntity,
						String.class);
			} catch (Exception e) {
				logger.error("Error : {} ,occurred while making call to url : {}",e.getMessage(), goHost + config.getPipelineAdminEndPoint());
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("CICD-1003"),
						"Exception occurred while calling gocd with url {}", goHost + config.getPipelineAdminEndPoint()),"CICD-1003");
			}

		} else {
			logger.debug(mapper.writeValueAsString(pipelineGroup));
			HttpHeaders headers = getCommonHttpHeaders(CiCdIntegrationHelper.CREATE_EDIT, gocdVersion);
			HttpEntity<String> getRequestEntity = new HttpEntity<>("", headers);
			try{
				ResponseEntity<String> responseEntity = restTemplate.exchange(
						goHost + config.getPipelineAdminEndPoint() + File.separator + getPipelineName(pipelineGroups),
						HttpMethod.GET, getRequestEntity, String.class);
				if (responseEntity != null && responseEntity.getHeaders() != null
						&& responseEntity.getHeaders().get("ETag") != null) {
					String eTag = responseEntity.getHeaders().get("ETag").get(0).replaceAll("\"", "");
					headers.set("If-Match", eTag);
				} else {
					throw new ItorixException(String.format(ErrorCodes.errorMessage.get("CICD-1003"),
							"Pipeline Not Found"),"CICD-1003");
				}
			} catch (Exception e) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("CICD-1003"),
						e.getMessage()),"CICD-1003");
			}

			logger.debug(mapper.writeValueAsString(pipeline));
			HttpEntity<Pipeline> requestEntity = new HttpEntity<>(pipeline, headers);
			try {
				String url = goHost + config.getPipelineAdminEndPoint() + File.separator
						+ getPipelineName(pipelineGroups);
				logger.debug("Making a call to {}", url);
				ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);
				response = responseEntity.getBody();
			} catch (Exception ex) {
				logger.error("Exception occurred", ex);
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("CICD-1003"),ex.getMessage()),"CICD-1003");
			}
		}
		// Un pausing pipeline
		try {
			// Un pausing pipeline
			unPausePipeline(pipelineGroup.getPipeline().getName());
		} catch (Exception ex) {
			logger.error("Exception occurred", ex);
			logger.error("Error while unpausing or pipeline might be unpaused already", ex);
		}
		logger.debug(response);
	}

	private Map<String, String> getSCMDetails(String scmType, String userType) {
		Query query = new Query();
		query.addCriteria(Criteria.where("type").is(scmType));
		List<Integration> dbIntegrations = mongoTemplate.find(query, Integration.class);
		if (dbIntegrations != null) {
			try {
				for (Integration integration : dbIntegrations) {
					if (integration.getGitIntegration().getUserType().equals(userType)) {
						Map<String, String> values = new HashMap<>();
						values.put("token", integration.getGitIntegration().getDecryptedToken());
						values.put("username", integration.getGitIntegration().getUsername());
						values.put("password", integration.getGitIntegration().getDecryptedPassword());
						values.put("userType", integration.getGitIntegration().getAuthType());
						return values;
					}
				}
			} catch (Exception e) {
				logger.error("Exception occurred", e);
			}
		}
		return null;
	}

	private String getBuildScmProp(String key) throws Exception {
		Query query = new Query();
		query.addCriteria(Criteria.where("propertyKey").is(key)
				.orOperator(Criteria.where("_id").is(key)));
		WorkspaceIntegration integration = mongoTemplate.findOne(query, WorkspaceIntegration.class);
		if (integration != null) {
			RSAEncryption rSAEncryption;
			rSAEncryption = new RSAEncryption();
			return integration.getEncrypt()
					? rSAEncryption.decryptText(integration.getPropertyValue())
					: integration.getPropertyValue();
		} else
			return null;
	}

	private String getPipelineName(PipelineGroups pipelineGroups) {
		String name = null;
		if (!pipelineGroups.getPipelines().isEmpty() && pipelineGroups.getProjectName() != null
				&& pipelineGroups.getPipelines().get(0).getProxyName() != null
				&& pipelineGroups.getPipelines().get(0).getVersion() != null
				&& !pipelineGroups.getPipelines().get(0).getMaterials().isEmpty()
				&& pipelineGroups.getPipelines().get(0).getMaterials().get(0).getScmBranch() != null) {
			name = pipelineGroups.getProjectName().replaceAll(" ", "-") + "_"
					+ pipelineGroups.getPipelines().get(0).getProxyName().replaceAll(" ", "-") + "_"
					+ pipelineGroups.getPipelines().get(0).getVersion() + "_"
					+ pipelineGroups.getPipelines().get(0).getMaterials().get(0).getScmBranch();
		}
		return name;
	}

	/**
	 * @param stage
	 * @param pipelineGroups
	 * 
	 * @return
	 */
	private Stage createStage(com.itorix.apiwiz.cicd.beans.Stage stage, PipelineGroups pipelineGroups,
			String artifactStageName, String projectName) {
		GoCDIntegration goCDIntegration = getGocdIntegration();
		List<Artifact> artifacts = new ArrayList<>();
		String jobType = "success";
		if (stage != null && stage.getType() != null && stage.getType().equalsIgnoreCase("Manual")) {
			jobType = "manual";
		}
		Stage pipelineStage = new Stage(stage.getName(), true, false, false, jobType);
		// Set Tasks
		Job job = new Job("PromoteBuild");
		List<Task> tasks = new ArrayList<>();

		// Get Installation Type
		Boolean isSaaS = pipelineGroups.getPipelines().get(0).getStages().get(0).getIsSaas();
		if (isSaaS == null) {
			isSaaS = true;
		}

		// fetch Artifact
		if (goCDIntegration.getVersion().trim().equalsIgnoreCase("18.10.0"))
			tasks.add(new Task("fetch", "passed", getPipelineName(pipelineGroups), artifactStageName, "BuildAndDeploy",
					pipelineGroups.getPipelines().get(0).getProxyName() + ".zip", "PromoteBuild", "gocd"));
		else
			tasks.add(new Task("fetch", "passed", getPipelineName(pipelineGroups), artifactStageName, "BuildAndDeploy",
					pipelineGroups.getPipelines().get(0).getProxyName() + ".zip", "PromoteBuild"));

		// Additional Tasks to support Build
		tasks.add(new Task("pluggable_task", "passed", "copyPipelineDir", "-q -b PipelineBuild/copy.gradle",
				goCDIntegration.getGradleHome()));

		// Promote Deployment
		tasks.add(new Task("pluggable_task", "passed", "promoteDeployment",
				prepareGradleParams(BASE_DIR, PARTNER_NAME, pipelineGroups.getPipelines().get(0).getProxyName(),
						pipelineGroups.getPipelines().get(0).getVersion(), stage.getOrgName(), stage.getEnvName(),
						config.getApigeeUserName(), config.getApigeePassword(), 0, 0, true, null, isSaaS, projectName,
						stage.getGwType()),
				goCDIntegration.getGradleHome()));

		String proxyName = pipelineGroups.getPipelines().get(0).getProxyName();
		String version = pipelineGroups.getPipelines().get(0).getVersion();
		String org = stage.getOrgName();
		String env = stage.getEnvName();
		boolean publishOldVersion = false;

		if (stage.getUnitTests() != null && stage.getUnitTests().getEnabled() != null
				&& stage.getUnitTests().getEnabled().equals("true")) {
			int unitTestsAcceptancePer = stage.getUnitTests().getAcceptance();
			if (stage.getUnitTests().getArtifactType() != null) {
				if (stage.getUnitTests().getArtifactType() == ArtifactType.Testsuite) {
					List<TestSuiteAndConfig> testSuites = stage.getUnitTests().getTestSuites();
					for (TestSuiteAndConfig testSuite : testSuites) {
						tasks.add(new Task("pluggable_task", "passed", "executeUnitTestCases",
								prepareGradleParams(BASE_DIR, PARTNER_NAME, proxyName, version, org, env,
										config.getApigeeUserName(), config.getApigeePassword(),
										testSuite.getTestSuiteId(), testSuite.getEnvironmentId(),
										unitTestsAcceptancePer, 0, true, stage.getUnitTests().getArtifactType(), isSaaS,
										projectName),
								goCDIntegration.getGradleHome()));
					}
				} else {
					tasks.add(new Task("pluggable_task", "passed", "executeUnitTestCases",
							prepareGradleParams(BASE_DIR, PARTNER_NAME, proxyName, version, org, env,
									config.getApigeeUserName(), config.getApigeePassword(), unitTestsAcceptancePer, 0,
									true, stage.getUnitTests().getArtifactType(), isSaaS, projectName,
									stage.getGwType()),
							goCDIntegration.getGradleHome()));
				}
			} else {
				tasks.add(new Task("pluggable_task", "passed", "executeUnitTestCases",
						prepareGradleParams(BASE_DIR, PARTNER_NAME, proxyName, version, org, env,
								config.getApigeeUserName(), config.getApigeePassword(), unitTestsAcceptancePer, 0, true,
								null, isSaaS, projectName, stage.getGwType()),
						goCDIntegration.getGradleHome()));
			}
			artifacts.add(new Artifact(BASE_DIR + "unitTest.html", "", "build"));
			publishOldVersion = true;
		}

		if (stage.getCodeCoverage() != null && stage.getCodeCoverage().getEnabled() != null
				&& stage.getCodeCoverage().getEnabled().equals("true")) {
			int codeCoverageAcceptancePer = stage.getCodeCoverage().getAcceptance();

			if (stage.getCodeCoverage().getArtifactType() != null) {
				if (stage.getCodeCoverage().getArtifactType() == ArtifactType.Testsuite) {
					List<TestSuiteAndConfig> testSuites = stage.getCodeCoverage().getTestSuites();
					for (TestSuiteAndConfig testSuite : testSuites) {
						tasks.add(new Task("pluggable_task", "passed", "executeCodeCoverage",
								prepareGradleParams(BASE_DIR, PARTNER_NAME, proxyName, version, org, env,
										config.getApigeeUserName(), config.getApigeePassword(),
										testSuite.getTestSuiteId(), testSuite.getEnvironmentId(), 0,
										codeCoverageAcceptancePer, true, stage.getCodeCoverage().getArtifactType(),
										isSaaS, projectName),
								goCDIntegration.getGradleHome()));
					}
				} else {
					tasks.add(new Task("pluggable_task", "passed", "executeCodeCoverage",
							prepareGradleParams(BASE_DIR, PARTNER_NAME, proxyName, version, org, env,
									config.getApigeeUserName(), config.getApigeePassword(), 0,
									codeCoverageAcceptancePer, true, stage.getCodeCoverage().getArtifactType(), isSaaS,
									projectName, stage.getGwType()),
							goCDIntegration.getGradleHome()));
				}

			} else {
				tasks.add(new Task("pluggable_task", "passed", "executeCodeCoverage",
						prepareGradleParams(BASE_DIR, PARTNER_NAME, proxyName, version, org, env,
								config.getApigeeUserName(), config.getApigeePassword(), 0, codeCoverageAcceptancePer,
								true, null, isSaaS, projectName, stage.getGwType()),
						goCDIntegration.getGradleHome()));
			}
			artifacts.add(new Artifact(BASE_DIR + "codecoverage.html", "", "build"));
			publishOldVersion = true;
		}
		String name = pipelineGroups.getPipelines().get(0).getProjectName() != null
				? pipelineGroups.getPipelines().get(0).getProjectName()
				: "";
		tasks.add(new Task("exec", "passed", "./email",
				config.getAppUrl() + " " + "Pass" + " " + name.replaceAll(" ", "%20"), null, true));
		tasks.add(new Task("exec", "failed", "./email",
				config.getAppUrl() + " " + "Failure" + " " + name.replaceAll(" ", "%20"), null, true));

		if (publishOldVersion) {
			tasks.add(new Task("pluggable_task", "failed", "publishOldVersion",
					prepareGradleParams(BASE_DIR, PARTNER_NAME, proxyName, version, org, env,
							config.getApigeeUserName(), config.getApigeePassword(), 0, 0, true, null, isSaaS,
							projectName, stage.getGwType()),
					goCDIntegration.getGradleHome()));
		}

		job.setTasks(tasks);
		List<Job> jobs = new ArrayList<>();
		job.setArtifacts(artifacts);
		jobs.add(job);

		pipelineStage.setJobs(jobs);
		return pipelineStage;
	}

	/**
	 * @param stage
	 * @param pipelineGroups
	 * 
	 * @return
	 */
	private Stage createSharedFlowStage(com.itorix.apiwiz.cicd.beans.Stage stage, PipelineGroups pipelineGroups,
			String artifactStageName) {
		GoCDIntegration goCDIntegration = getGocdIntegration();
		String jobType = "success";
		if (stage != null && stage.getType() != null && stage.getType().equalsIgnoreCase("Manual")) {
			jobType = "manual";
		}
		Stage pipelineStage = new Stage(stage.getName(), true, false, false, jobType);
		// Set Tasks
		Job job = new Job("PromoteBuild");
		List<Task> tasks = new ArrayList<>();
		// Get Installation Type
		Boolean isSaaS = pipelineGroups.getPipelines().get(0).getStages().get(0).getIsSaas();
		if (isSaaS == null) {
			isSaaS = true;
		}

		// fetch Artifact
		if (goCDIntegration.getVersion().trim().equalsIgnoreCase("18.10.0"))
			tasks.add(new Task("fetch", "passed", getPipelineName(pipelineGroups), artifactStageName, "BuildAndDeploy",
					pipelineGroups.getPipelines().get(0).getProxyName() + ".zip", "PromoteBuild", "gocd"));
		else
			tasks.add(new Task("fetch", "passed", getPipelineName(pipelineGroups), artifactStageName, "BuildAndDeploy",
					pipelineGroups.getPipelines().get(0).getProxyName() + ".zip", "PromoteBuild"));

		// Additional Tasks to support Build
		tasks.add(new Task("pluggable_task", "passed", "copyPipelineDir", "-q -b PipelineBuild/copy.gradle",
				goCDIntegration.getGradleHome()));

		// Promote Deployment
		tasks.add(new Task("pluggable_task", "passed", "promoteSharedFlow",
				prepareGradleParamsSharedFlow(BASE_DIR, PARTNER_NAME,
						pipelineGroups.getPipelines().get(0).getProxyName(),
						pipelineGroups.getPipelines().get(0).getVersion(), stage.getOrgName(), stage.getEnvName(),
						config.getApigeeUserName(), config.getApigeePassword(), 0, 0, true, isSaaS),
				goCDIntegration.getGradleHome()));

		tasks.add(new Task("exec", "passed", "./email", config.getAppUrl() + " " + "Pass", null, true));
		tasks.add(new Task("exec", "failed", "./email", config.getAppUrl() + " " + "Failure", null, true));

		job.setTasks(tasks);
		List<Job> jobs = new ArrayList<>();
		jobs.add(job);

		pipelineStage.setJobs(jobs);
		return pipelineStage;
	}

	/**
	 * @param baseDir
	 * @param partnerName
	 * @param proxyName
	 * @param version
	 * @param org
	 * @param env
	 * @param apigeeUserName
	 * @param apigeePassword
	 * @param unitTestHoldThresholdpercent
	 * @param codeCoverageThresholdPercent
	 * @param skipClean
	 * @param revertVersion
	 * 
	 * @return
	 */
	public String prepareGradleParams(String baseDir, String partnerName, String proxyName, String version, String org,
			String env, String apigeeUserName, String apigeePassword, int unitTestHoldThresholdpercent,
			int codeCoverageThresholdPercent, boolean skipClean, ArtifactType artifactType, boolean isSaaS,
			String projectName, String gwType) {
		String params = "-PbaseDir=" + baseDir + " -PpartnerName=" + partnerName + " -PName=" + proxyName
				+ " -PversionName=" + version + " -Porg=" + org + " -Penv=" + env + " -Pusername=" + apigeeUserName
				+ " -Ppassword=" + apigeePassword + " -PisSaaS=" + isSaaS;
		if (unitTestHoldThresholdpercent != 0) {
			params += " -PunitTestThresholdPercentage=" + unitTestHoldThresholdpercent;
		}
		if (codeCoverageThresholdPercent != 0) {
			params += " -PcodeCoverageThresholdPercentage=" + codeCoverageThresholdPercent;
		}
		if (skipClean) {
			params += " -PskipClean=true";
		}
		if (artifactType != null) {
			params += " -PartifactType=" + artifactType;
		}
		if (projectName != null) {
			params += " -PprojectName=" + projectName;
		}
		if (projectName != null) {
			params += " -PgwType=" + gwType;
		}
		return params;
	}

	/**
	 * @param baseDir
	 * @param partnerName
	 * @param proxyName
	 * @param version
	 * @param org
	 * @param env
	 * @param apigeeUserName
	 * @param apigeePassword
	 * @param unitTestHoldThresholdpercent
	 * @param codeCoverageThresholdPercent
	 * @param skipClean
	 * @param revertVersion
	 * 
	 * @return
	 */
	public String prepareGradleParams(String baseDir, String partnerName, String proxyName, String version, String org,
			String env, String apigeeUserName, String apigeePassword, String testSuiteId, String configId,
			int unitTestHoldThresholdpercent, int codeCoverageThresholdPercent, boolean skipClean,
			ArtifactType artifactType, boolean isSaaS, String projectName) {
		String params = "-PbaseDir=" + baseDir + " -PpartnerName=" + partnerName + " -PName=" + proxyName
				+ " -PversionName=" + version + " -Porg=" + org + " -Penv=" + env + " -Pusername=" + apigeeUserName
				+ " -Ppassword=" + apigeePassword + " -PisSaaS=" + isSaaS + " -PtestSuiteId=" + testSuiteId
				+ " -PconfigId=" + configId;
		if (unitTestHoldThresholdpercent != 0) {
			params += " -PunitTestThresholdPercentage=" + unitTestHoldThresholdpercent;
		}
		if (codeCoverageThresholdPercent != 0) {
			params += " -PcodeCoverageThresholdPercentage=" + codeCoverageThresholdPercent;
		}
		if (skipClean) {
			params += " -PskipClean=true";
		}
		if (artifactType != null) {
			params += " -PartifactType=" + artifactType;
		}
		if (projectName != null) {
			params += " -PprojectName=" + projectName;
		}
		return params;
	}

	/**
	 * @param baseDir
	 * @param partnerName
	 * @param proxyName
	 * @param version
	 * @param org
	 * @param env
	 * @param apigeeUserName
	 * @param apigeePassword
	 * @param unitTestHoldThresholdpercent
	 * @param codeCoverageThresholdPercent
	 * @param skipClean
	 * @param revertVersion
	 * 
	 * @return
	 */
	public String prepareGradleParamsSharedFlow(String baseDir, String partnerName, String proxyName, String version,
			String org, String env, String apigeeUserName, String apigeePassword, int unitTestHoldThresholdpercent,
			int codeCoverageThresholdPercent, boolean skipClean, boolean isSaaS) {
		String params = "-q -b sharedflow.gradle -PbaseDir=" + baseDir + " -PpartnerName=" + partnerName + " -PName="
				+ proxyName + " -PversionName=" + version + " -Porg=" + org + " -Penv=" + env + " -Pusername="
				+ apigeeUserName + " -Ppassword=" + apigeePassword + " -PisSaaS=" + isSaaS;
		if (unitTestHoldThresholdpercent != 0) {
			params += " -PunitTestThresholdPercentage=" + unitTestHoldThresholdpercent;
		}
		if (codeCoverageThresholdPercent != 0) {
			params += " -PcodeCoverageThresholdPercentage=" + codeCoverageThresholdPercent;
		}
		if (skipClean) {
			params += " -PskipClean=true";
		}
		return params;
	}

	/**
	 * Delete Pipeline
	 *
	 * @param name
	 */
	public void deletePipeline(String name) {
		// Invoke API and throw exception in case of issues/errors for API
		// creation
		GoCDIntegration goCDIntegration = getGocdIntegration();
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors()
				.add(new BasicAuthorizationInterceptor(goCDIntegration.getUsername(), goCDIntegration.getPassword()));
		HttpEntity<String> requestEntity = new HttpEntity<>("",
				getCommonHttpHeaders(CiCdIntegrationHelper.DELETE, goCDIntegration.getVersion()));
		logger.debug("Making a call to {}",
				goCDIntegration.getHostURL() + config.getPipelineAdminEndPoint() + File.separator + name);
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				goCDIntegration.getHostURL() + config.getPipelineAdminEndPoint() + File.separator + name,
				HttpMethod.DELETE, requestEntity, String.class);
		logger.debug(responseEntity.getBody());
	}

	public void pausePipeline(String name) {
		managePipeline(name, "pause");
	}

	public void unPausePipeline(String name) {
		managePipeline(name, "unpause");
	}

	private void managePipeline(String name, String action) {
		GoCDIntegration goCDIntegration = getGocdIntegration();
		HttpHeaders headers = new HttpHeaders();
		if (CiCdIntegrationHelper.getConfirmHeader(goCDIntegration.getVersion()) != null) {
			headers.set(CiCdIntegrationHelper.getConfirmHeader(goCDIntegration.getVersion()), "true");
			headers.set("Accept", "application/vnd.go.cd.v1+json");
			headers.setBasicAuth(goCDIntegration.getUsername(), goCDIntegration.getPassword());
		} else
			headers.set("Confirm", "true");
		RestTemplate restTemplate = new RestTemplate();
		// restTemplate.getInterceptors()
		// .add(new BasicAuthorizationInterceptor(config.getCicdAuthUserName(),
		// config.getCicdAuthPassword()));
		HttpEntity<String> requestEntity = new HttpEntity<>(headers);
		logger.debug("Making a call to {}", goCDIntegration.getHostURL() + config.getPipelineEndPoint() + File.separator
				+ name + File.separator + action);
		ResponseEntity<String> responseEntity = restTemplate.exchange(goCDIntegration.getHostURL()
				+ config.getPipelineEndPoint() + File.separator + name + File.separator + action, HttpMethod.POST,
				requestEntity, String.class);
		logger.debug(responseEntity.getBody());
	}

	public String getPipelineStatus(String name) throws ItorixException {
		GoCDIntegration goCDIntegration = getGocdIntegration();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Confirm", "true");
		RestTemplate restTemplate = new RestTemplate();
		try{
			restTemplate.getInterceptors()
					.add(new BasicAuthorizationInterceptor(goCDIntegration.getUsername(), goCDIntegration.getPassword()));
			HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
			logger.debug("Making a call to {}", goCDIntegration.getHostURL() + goCDIntegration.getHostURL()
					+ config.getPipelineEndPoint() + File.separator + name + File.separator + "status");
			ResponseEntity<String> responseEntity = restTemplate.exchange(goCDIntegration.getHostURL()
							+ config.getPipelineEndPoint() + File.separator + name + File.separator + "status", HttpMethod.GET,
					requestEntity, String.class);
			logger.debug(responseEntity.getBody());
			return responseEntity.getBody();
		} catch (RestClientException e) {
			throw new ItorixException("Error while retrieving pipeline status. Pipeline might not be available.Please check the pipeline in input url ","CICD-1003");
		}

	}

	public String getPipelineHistory(String groupName, String name, String offset)
			throws ItorixException {
		GoCDIntegration goCDIntegration = getGocdIntegration();
		RestTemplate restTemplate = new RestTemplate();
		try{
			restTemplate.getInterceptors()
					.add(new BasicAuthorizationInterceptor(goCDIntegration.getUsername(), goCDIntegration.getPassword()));
			return restTemplate.getForObject(goCDIntegration.getHostURL()
							+ config.getPipelinesHistoryEndPoint().replaceAll(":PipelineName", name).replaceAll(":offset", offset),
					String.class);
		} catch (RestClientException e) {
			throw new ItorixException("Error while retrieving pipeline history from GOCD for  "+name,"CI-CD-GBTA500");
		}

	}

	public String getArtifactDetails(String groupName, String pipelineName, String pipelineCounter, String stageName,
			String stageCounter, String jobName) {
		GoCDIntegration goCDIntegration = getGocdIntegration();
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors()
				.add(new BasicAuthorizationInterceptor(goCDIntegration.getUsername(), goCDIntegration.getPassword()));
		return restTemplate
				.getForObject(
						goCDIntegration.getHostURL() + config.getPipelinesArtifactoryEndPoint()
								.replaceAll(":pipelineGroupName", groupName).replaceAll(":pipelineName", pipelineName)
								.replaceAll(":pipelineCounter", pipelineCounter).replaceAll(":stageName", stageName)
								.replaceAll(":stageCounter", stageCounter).replaceAll(":jobName", jobName),
						String.class)
				.replaceAll(goCDIntegration.getHostURL() + "/go/files",
						config.getAppUrl() + config.getAppDomain() + "/v1/pipelines");
	}

	public String getLogs(String url) {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors()
				.add(new BasicAuthorizationInterceptor(config.getCicdAuthUserName(), config.getCicdAuthPassword()));
		return restTemplate.getForObject(url, String.class);
	}

	public HttpHeaders getCommonHttpHeaders(String method, String version) {
		String accept = CiCdIntegrationHelper.getHeader(method, version);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Accept", accept);
		return headers;
	}

	public void triggerPipeline(String groupName, String pipelineName) {
		GoCDIntegration goCDIntegration = getGocdIntegration();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Confirm", "true");
		headers.set("Accept", "application/vnd.go.cd.v1+json");
		if (CiCdIntegrationHelper.getConfirmHeader(goCDIntegration.getVersion()) != null)
			headers.set(CiCdIntegrationHelper.getConfirmHeader(goCDIntegration.getVersion()), "true");
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors()
				.add(new BasicAuthorizationInterceptor(goCDIntegration.getUsername(), goCDIntegration.getPassword()));
		HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
		try {
			String uri = goCDIntegration.getHostURL() + config.getPipelineEndPoint() + File.separator + pipelineName
					+ File.separator + "schedule";
			logger.debug("Making a call to {}", uri);
			ResponseEntity<String> responseEntity = restTemplate.exchange(goCDIntegration.getHostURL()
					+ config.getPipelineEndPoint() + File.separator + pipelineName + File.separator + "schedule",
					HttpMethod.POST, requestEntity, String.class);
			logger.debug(responseEntity.getBody());
		} catch (Exception ex) {
			logger.error("Exception occurred", ex);
		}
	}

	public void cancelPipeline(String groupName, String pipelineName, String stageName) {
		GoCDIntegration goCDIntegration = getGocdIntegration();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Confirm", "true");
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors()
				.add(new BasicAuthorizationInterceptor(goCDIntegration.getUsername(), goCDIntegration.getPassword()));
		HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
		logger.debug("Making a call to {}", goCDIntegration.getHostURL() + config.getCancelPipelineEndPoint()
				.replaceAll(":pipelineName", pipelineName).replaceAll(":stageName", stageName));
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				goCDIntegration.getHostURL() + config.getCancelPipelineEndPoint()
						.replaceAll(":pipelineName", pipelineName).replaceAll(":stageName", stageName),
				HttpMethod.POST, requestEntity, String.class);
		logger.debug(responseEntity.getBody());
	}

	public void triggerStage(String groupName, String pipelineName, String stageName, String counter) {
		GoCDIntegration goCDIntegration = getGocdIntegration();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Confirm", "true");
		String accept = CiCdIntegrationHelper.getHeader(CiCdIntegrationHelper.TRIGGER_STAGE,
				goCDIntegration.getVersion());
		headers.set("Accept", accept);
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors()
				.add(new BasicAuthorizationInterceptor(goCDIntegration.getUsername(), goCDIntegration.getPassword()));
		HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
		logger.debug("Making a call to {}",
				goCDIntegration.getHostURL()
						+ config.getPipelineStageTriggerEndPoint().replaceAll(":PipelineName", pipelineName)
								.replaceAll(":stageCounter", counter).replaceAll(":stageName", stageName));
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				goCDIntegration.getHostURL()
						+ config.getPipelineStageTriggerEndPoint().replaceAll(":PipelineName", pipelineName)
								.replaceAll(":stageCounter", counter).replaceAll(":stageName", stageName),
				HttpMethod.POST, requestEntity, String.class);
		logger.debug(responseEntity.getBody());
	}

	public Object sendNotification(String emailBody, String pipelineName, String projectName, String subject,
			String interactionid) throws IOException, ItorixException, MessagingException {
		List<String> toMailId = new ArrayList<String>();
		toMailId = identityManagementDao.getAllUsersWithRoleDevOPS();
		EmailTemplate emailTemplate = new EmailTemplate();
		emailTemplate.setSubject(subject);
		if (pipelineName.indexOf("_") == -1) {
			throw new ItorixException("Pipeline Name Must Contain Hypen", "CICD-1000");
		}
		// String[] splitByHypen = pipelineName.split("_");
		com.itorix.apiwiz.cicd.beans.Pipeline pipeline = pipelineDao.getPipeline(pipelineName);
		String portfolioId = null;
		if (!ObjectUtils.isEmpty(pipeline))
			portfolioId = pipeline.getPortfolioId();
		if (StringUtils.isNotEmpty(portfolioId)) {
		}
		// String exactProjectName = (projectName != null && projectName != "")
		// ? projectName : splitByHypen[0];
		emailTemplate.setToMailId(toMailId);
		emailTemplate.setBody(emailBody);
		mailUtil.sendEmail(emailTemplate);


		logger.info("Sending slack message");
		List<SlackWorkspace> slackWorkspaces = mongoTemplate.findAll(SlackWorkspace.class);
		if (slackWorkspaces.isEmpty()) return null;
		SlackWorkspace slackWorkspace = slackWorkspaces.get(0);
		String token = slackWorkspace.getToken();
		List<SlackChannel> channels = slackWorkspace.getChannelList();
		for (SlackChannel i : channels) {
			if (i.getScopeSet().contains(NotificationScope.Scopes.Build)) {
				PostMessage postMessage = new PostMessage();
				ArrayList<Attachment> attachmentsToSend = new ArrayList<>();
				Attachment attachment = new Attachment();
				attachment.setMrkdwn_in("text");
				attachment.setTitle_link("https://www.apiwiz.io/");
				attachment.setColor("#820309");
				attachment.setPretext("INFO");
				attachment.setTitle("NOTIFICATION");
//					String s=notificationData.get(STATUS);
				attachment.setText(emailBody);

				attachmentsToSend.add(attachment);
				postMessage.setAttachments(attachmentsToSend);
				slackUtil.sendMessage(postMessage, i.getChannelName(), token);
			}
		}

		return null;
	}

	public Object getMetricsForProject(String interactionid, String topk, String project_name, String pipelineName) {
		List<Object> result = new ArrayList<>();
		List<Object> pipelineList = new ArrayList<>();
		Map<String, Object> finalresultmap = new HashMap<>();
		Map<String, Object> pipelineresultmap = new HashMap<>();
		Map<String, Object> projectsresultmap = new HashMap<>();
		try {
			String reponsehistory = getPipelineHistory(project_name, pipelineName, topk);
			logger.info("reponsehistory::" + reponsehistory);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonHistory = mapper.readTree(reponsehistory);
			JsonNode pipelines = jsonHistory.path("pipelines");
			JsonNode pagination = jsonHistory.get("pagination");
			int total = pagination.get("total").asInt();
			// JsonNode stages = pipelines.path("stages");
			Iterator<JsonNode> elements = pipelines.elements();
			while (elements.hasNext()) {
				Map<String, Object> resultmap = new HashMap<>();
				List<Object> stagesresult = new ArrayList<>();

				JsonNode top10Nodes = elements.next();
				resultmap.put("buildNumber", top10Nodes.get("natural_order").asInt());

				JsonNode stages = top10Nodes.path("stages");
				Iterator<JsonNode> stagesList = stages.elements();
				while (stagesList.hasNext()) {
					Map<String, Object> stagesresultMap = new HashMap<>();
					JsonNode stageNodes = stagesList.next();
					JsonNode job = stageNodes.path("jobs");
					Iterator<JsonNode> jobiterator = job.elements();
					stagesresultMap.put("name", stageNodes.get("name"));
					stagesresultMap.put("result", stageNodes.get("result"));
					while (jobiterator.hasNext()) {
						JsonNode jobNode = jobiterator.next();
						stagesresultMap.put("scheduled_date", jobNode.get("scheduled_date"));
					}
					if (stagesresultMap.get("scheduled_date") == null) {
						stagesresultMap.put("scheduled_date", null);
					}
					stagesresult.add(stagesresultMap);
				}
				resultmap.put("stages", stagesresult);
				result.add(resultmap);
			}

			finalresultmap.put("pipelineName", pipelineName);
			finalresultmap.put("proxy_name", pipelineName.split("_")[1]);
			finalresultmap.put("total", total);
			finalresultmap.put("metrics", result);
			pipelineList.add(finalresultmap);
			pipelineresultmap.put("pipelines", pipelineList);
			pipelineresultmap.put("name", project_name);
			projectsresultmap.put("projects", pipelineresultmap);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Exception occurred", e);
		} catch (ItorixException e) {
			logger.error("Exception occurred", e);
		}
		return projectsresultmap;
	}

	private String getCruiseJobDuration(String pipelineName, int pipelineCounter, String stageName, String jobName) {
		GoCDIntegration goCDIntegration = getGocdIntegration();
		String counter = new Integer(pipelineCounter).toString();
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors()
				.add(new BasicAuthorizationInterceptor(goCDIntegration.getUsername(), goCDIntegration.getPassword()));
		HttpEntity<String> requestEntity = new HttpEntity<>("text/csv",
				getCommonHttpHeaders(CiCdIntegrationHelper.CRUISE_JOB_DURATION, goCDIntegration.getVersion()));
		logger.debug("Making a call to {}",
				goCDIntegration.getHostURL() + config.getJobDuration().replaceAll(":pipelineName", pipelineName)
						.replaceAll(":stageName", stageName).replaceAll(":jobName", jobName)
						.replaceAll(":pipelineCounter", counter));

		ResponseEntity<String> responseEntity = restTemplate.exchange(goCDIntegration.getHostURL()
				+ config.getJobDuration().replaceAll(":pipelineName", pipelineName).replaceAll(":stageName", stageName)
						.replaceAll(":jobName", jobName).replaceAll(":pipelineCounter", counter),
				HttpMethod.GET, requestEntity, String.class);
		logger.debug(responseEntity.getBody());
		return responseEntity.getBody();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public Object getMetricsForProject(String interactionid, String topk, String project_name,
			List<com.itorix.apiwiz.cicd.beans.Pipeline> pipelines, String groupName) throws IOException {

		List<Object> pipelineList = new ArrayList<>();
		Map<String, Object> pipelineresultmap = new HashMap<>();
		List<SucessRatio> pipeLineSucessRatiolist = new ArrayList<>();
		int totalPipelineCount = 0;
		int totalpieplineSucessCount = 0;
		try {

			for (com.itorix.apiwiz.cicd.beans.Pipeline pipeline : pipelines) {
				Map<String, Object> pipelinereult = new HashMap<>();
				List<Object> result = new ArrayList<>();
				String pipelineName = pipeline.getName();
				int totalcount = 0;
				int totalSucesscount = 0;
				int total = 0;
				List<SucessRatio> stagesSucessRatio = new ArrayList<>();

				String offsets = config.getCicddashBoardOffSet();
				String[] offset = offsets.split(",");

				/*
				 * ArrayList<String> offset = new ArrayList<>();
				 *
				 * offset.add("0"); offset.add("10");
				 */
				SucessRatio pipelineSucessRatio = new SucessRatio();
				Map<String, Object> stageWiseCount = new HashMap<>();
				for (String offsetValue : offset) {
					try {
						String reponsehistory = getPipelineHistory(project_name, pipelineName, offsetValue);
						logger.debug("reponsehistory::" + reponsehistory);
						ObjectMapper mapper = new ObjectMapper();
						JsonNode jsonHistory = mapper.readTree(reponsehistory);
						JsonNode pipeLinesNode = jsonHistory.path("pipelines");
						JsonNode pagination = jsonHistory.get("pagination");
						total = pagination.get("total").asInt();
						// JsonNode stages = pipelines.path("stages");
						Iterator<JsonNode> elements = pipeLinesNode.elements();

						while (elements.hasNext()) {
							Map<String, Object> resultmap = new HashMap<>();
							List<Object> stagesresult = new ArrayList<>();

							JsonNode top10Nodes = elements.next();
							resultmap.put("buildNumber", top10Nodes.get("natural_order").asInt());
							int pipelineCounter = top10Nodes.get("natural_order").asInt();
							JsonNode stages = top10Nodes.path("stages");
							Iterator<JsonNode> stagesList = stages.elements();
							while (stagesList.hasNext()) {
								String jobName = null;
								Map<String, Object> stagesresultMap = new HashMap<>();
								JsonNode stageNodes = stagesList.next();
								JsonNode job = stageNodes.path("jobs");
								Iterator<JsonNode> jobiterator = job.elements();
								stagesresultMap.put("name", stageNodes.get("name"));
								stagesresultMap.put("result", stageNodes.get("result"));
								if (stageWiseCount.containsKey(stageNodes.get("name").asText())) {

									HashMap resultCountMap = (HashMap) stageWiseCount
											.get(stageNodes.get("name").asText());
									if (stageNodes.get("result") != null) {
										if (resultCountMap.containsKey(stageNodes.get("result").asText())) {
											int val = (int) resultCountMap.get((stageNodes.get("result").asText()));
											resultCountMap.put((stageNodes.get("result").asText()), val + 1);
											stageWiseCount.put(stageNodes.get("name").asText(), resultCountMap);
										} else {

											resultCountMap.put(stageNodes.get("result").asText(), 1);
											stageWiseCount.put(stageNodes.get("name").asText(), resultCountMap);
										}
									}

								} else {
									Map<String, Object> resultCountMap = new HashMap<>();
									if (stageNodes.get("result") != null) {
										resultCountMap.put(stageNodes.get("result").asText(), 1);
										stageWiseCount.put(stageNodes.get("name").asText(), resultCountMap);
									}
								}
								while (jobiterator.hasNext()) {
									JsonNode jobNode = jobiterator.next();
									jobName = jobNode.get("name").asText();
									stagesresultMap.put("scheduled_date", jobNode.get("scheduled_date"));
									String CruiseJobDuration = getCruiseJobDuration(pipelineName, pipelineCounter,
											stageNodes.get("name").asText(), jobName);
									String[] duration = CruiseJobDuration.split("\n");
									stagesresultMap.put("duration", duration[1]);
								}
								if (stagesresultMap.get("scheduled_date") == null) {
									stagesresultMap.put("scheduled_date", null);
								}
								if (stagesresultMap.get("duration") == null) {
									stagesresultMap.put("duration", 0);
								}
								stagesresult.add(stagesresultMap);
							}
							resultmap.put("stages", stagesresult);
							result.add(resultmap);
						}
					} catch (Exception e) {
						logger.error("Exception occurred", e);
					}
				}
				for (Map.Entry<String, Object> stagewise : stageWiseCount.entrySet()) {
					int totalval = 0;
					SucessRatio sucessRatio = new SucessRatio();
					Map<String, Object> value = (Map<String, Object>) stagewise.getValue();
					String stageName = stagewise.getKey();

					sucessRatio.setName(stageName);
					Set<String> keySet = value.keySet();
					for (String key : keySet) {
						totalval += (int) value.get(key);
						if (key.equalsIgnoreCase("passed")) {
							sucessRatio.setSuccess((int) value.get(key));
						}
					}
					sucessRatio.setTotal(totalval);
					sucessRatio.setSucessRatio(((sucessRatio.getSuccess() * 100) / totalval));
					stagesSucessRatio.add(sucessRatio);
				}

				for (SucessRatio sucessRatio : stagesSucessRatio) {
					totalcount += sucessRatio.getTotal();
					totalSucesscount += sucessRatio.getSuccess();
				}

				pipelineSucessRatio.setName(pipelineName);
				pipelineSucessRatio.setSuccess(totalSucesscount);
				pipelineSucessRatio.setTotal(totalcount);
				pipelineSucessRatio.setSucessRatio(totalcount > 0 ? (totalSucesscount * 100) / totalcount : 0);
				pipeLineSucessRatiolist.add(pipelineSucessRatio);
				pipelinereult.put("pipelineName", pipelineName);
				pipelinereult.put("proxy_name", pipelineName.split("_")[1]);
				pipelinereult.put("total", total);
				pipelinereult.put("metrics", result);
				pipelinereult.put("stageSuccessRatios", stagesSucessRatio);
				pipelinereult.put("pipelineSuccessRatio", pipelineSucessRatio);
				pipelineList.add(pipelinereult);
			}

			for (SucessRatio sucessRatio : pipeLineSucessRatiolist) {
				totalPipelineCount += sucessRatio.getTotal();
				totalpieplineSucessCount += sucessRatio.getSuccess();
			}
			SucessRatio projectSucessRatio = new SucessRatio();
			projectSucessRatio.setName(project_name);
			projectSucessRatio.setSuccess(totalpieplineSucessCount);
			projectSucessRatio.setTotal(totalPipelineCount);
			projectSucessRatio.setSucessRatio((totalpieplineSucessCount * 100) / totalPipelineCount);
			pipelineresultmap.put("pipelines", pipelineList);
			pipelineresultmap.put("name", groupName);
			pipelineresultmap.put("projectSuccessRatio", projectSucessRatio);

		} catch (Exception e) {
			logger.error("Exception occurred", e);
		}

		return pipelineresultmap;
	}

	public Object getPipelineDashBoard() {
		List<Object> dashBoardResult = new ArrayList<>();
		Map<String, Object> projectsresultmap = new HashMap<>();

		try {

			PipeLineDashBoard pipeLineResponse = findpipelineResponse();
			if (pipeLineResponse != null) {
				return pipeLineResponse.getCicdDashBoardResponse();
			} else {
				List<PipelineGroups> availablePipelines = pipelineDao.getAvailablePipelines();
				if (availablePipelines.size() > 0) {
					for (PipelineGroups pipelineGroup : availablePipelines) {
						try {
							String projectname = pipelineGroup.getPipelines().get(0).getProjectName() != null
									? pipelineGroup.getPipelines().get(0).getProjectName()
									: pipelineGroup.getProjectName();
							logger.debug("pipeline project name : " + projectname);
							Object metricsForProject = getMetricsForProject(null, "0", projectname,
									pipelineGroup.getPipelines(), pipelineGroup.getProjectName());
							dashBoardResult.add(metricsForProject);
						} catch (Exception e) {
							logger.error("Exception occurred", e);
						}
					}
				}
				if (dashBoardResult.size() > 0) {
					projectsresultmap.put("projects", dashBoardResult);
				}
				return projectsresultmap;
			}

		} catch (Exception e) {
			logger.error("Exception occurred", e);
		}
		return "";
	}

	// @Scheduled(fixedDelayString =
	// "${itorix.core.gocd.dashboard.fixedDelay.in.milliseconds}")
	public void getScheduledPipelineDashBoard() throws IOException {
		if (Schedule.isSchedulable(scheduleEnable, primary, primaryHost)) {
			logger.debug("CiCdIntegrationAPI:getScheduledPipelineDashBoard  : Scheduler Started for CICD DashBoard");
			List<Object> dashBoardResult = new ArrayList<Object>();
			try {
				List<PipelineGroups> availablePipelines = mongoTemplate.findAll(PipelineGroups.class);
				// List<PipelineGroups> availablePipelines =
				// pipelineDao.getAvailablePipelines();
				if (availablePipelines.size() > 0) {
					for (PipelineGroups pipelineGroup : availablePipelines) {
						try {
							String projectname = pipelineGroup.getPipelines().get(0).getProjectName() != null
									? pipelineGroup.getPipelines().get(0).getProjectName()
									: pipelineGroup.getProjectName();
							Object metricsForProject = getMetricsForProject(null, "0", projectname,
									pipelineGroup.getPipelines(), pipelineGroup.getProjectName());
							dashBoardResult.add(metricsForProject);
						} catch (Exception e) {
							logger.error("Exception occurred", e);
						}
					}
				}
			} catch (Exception e) {
				logger.error("Exception occurred", e);
			}
			if (dashBoardResult.size() > 0) {
				Map<String, Object> projectsresultmap = new HashMap<>();
				projectsresultmap.put("projects", dashBoardResult);
				ObjectMapper mapper = new ObjectMapper();
				String writeValueAsString = mapper.writeValueAsString(projectsresultmap);
				CicdDashBoardResponse readValue = mapper.readValue(writeValueAsString, CicdDashBoardResponse.class);
				PipeLineDashBoard pipeLineDashBoard = new PipeLineDashBoard();
				pipeLineDashBoard.setDashBoradFunctionName(PipeLineDashBoard.FUNCTION_NAME);
				pipeLineDashBoard.setCicdDashBoardResponse(readValue);
				mongoTemplate.dropCollection(PipeLineDashBoard.class);
				mongoTemplate.save(pipeLineDashBoard);
			}
			logger.debug("CiCdIntegrationAPI:getScheduledPipelineDashBoard  : Scheduler Completed for CICD DashBoard");
		}
	}

	public PipeLineDashBoard findpipelineResponse() {

		Query query = new Query(
				Criteria.where(PipeLineDashBoard.LABEL_DASH_BOARD_FUNCTIONNAME).is(PipeLineDashBoard.FUNCTION_NAME));
		PipeLineDashBoard cicdDashBoardResponse = mongoTemplate.findOne(query, PipeLineDashBoard.class);
		return cicdDashBoardResponse;
	}

	// Get runtime build logs for a stage and task name.
	public String getRuntimeLogs(String groupName, String pipelineName, String pipelineCounter, String stageName,
			String stageCounter, String jobName) throws ItorixException {
		GoCDIntegration goCDIntegration = getGocdIntegration();
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors()
				.add(new BasicAuthorizationInterceptor(goCDIntegration.getUsername(), goCDIntegration.getPassword()));
		try {
			return restTemplate
					.getForObject(
							goCDIntegration.getHostURL() + config.getPipelinesRunTimeLogsEndPoint()
									.replaceAll(":pipelineGroupName", groupName).replaceAll(":pipelineName", pipelineName)
									.replaceAll(":pipelineCounter", pipelineCounter).replaceAll(":stageName", stageName)
									.replaceAll(":stageCounter", stageCounter).replaceAll(":jobName", jobName),
							String.class)
					.replaceAll("http://localhost:8153", goCDIntegration.getHostURL());
		} catch (RestClientException e) {
			logger.error(Arrays.toString(e.getStackTrace()));
			throw new ItorixException("Error while getting Runtime logs from GOCD for "+pipelineName,"CI-CD-GBTA500");
		}
	}

	@SuppressWarnings({"deprecation", "unchecked"})
	public ObjectNode getcicdtats(String timeunit, String timerange) throws ParseException {
		// logger.info("getcicdtats", timeunit, timerange);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		String[] dates = timerange.split("~");

		Date startDate = null;
		Date endDate = null;

		if (dates != null && dates.length > 0) {
			startDate = dateFormat.parse(dates[0]);
			endDate = dateFormat.parse(dates[1]);
		}

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		ObjectNode metricsNode = mapper.createObjectNode();
		metricsNode.put("name", timeunit);
		ArrayNode valuesNode = mapper.createArrayNode();
		ArrayNode projectsNode = mapper.createArrayNode();

		while (startDate.compareTo(endDate) <= 0) {
			Query query = new Query();
			query.addCriteria(Criteria.where(PipelineGroups.LABEL_CREATED_TIME)
					.gte(new Long(DateUtil.getStartOfDay(startDate).getTime() + ""))
					.lt(new Long(DateUtil.getEndOfDay(startDate).getTime() + "")));
			List<PipelineGroups> list = mongoTemplate.find(query, PipelineGroups.class);
			// if(list!=null && list.size()>0){
			ObjectNode valueNode = mapper.createObjectNode();
			valueNode.put("timestamp", DateUtil.getStartOfDay(startDate).getTime() + "");
			valueNode.put("value", list.size());
			valuesNode.add(valueNode);
			// }
			startDate = DateUtil.addDays(startDate, 1);
		}
		metricsNode.set("values", valuesNode);

		ObjectNode statsNode = mapper.createObjectNode();
		List<PipelineGroups> list = mongoTemplate.findAll(PipelineGroups.class);

		for (PipelineGroups pipelineGroups : list) {
			try {
				ObjectNode projectNode = mapper.createObjectNode();
				ArrayNode dimesionList = mapper.createArrayNode();
				// Project project =
				// projectPlanAndTrackService.findByProjectName(pipelineGroups.getProjectName());
				// projectNode.put("name", project.getName());
				// projectNode.put("status", project.getStatus());
				// projectNode.put("inProduction", project.getInProd());
				// projectNode.put("pipelineCount",
				// pipelineGroups.getPipelines().size());
				String projectName = null;
				for (com.itorix.apiwiz.cicd.beans.Pipeline pipeline : pipelineGroups.getPipelines()) {
					ObjectNode dimesionNode = mapper.createObjectNode();
					dimesionNode.put("pipelineName", pipeline.getName());
					dimesionNode.put("status", pipeline.getStatus());
					dimesionList.add(dimesionNode);
					if (pipeline.getProjectName() != null)
						projectName = pipeline.getDefineName() != null && !pipeline.getDefineName().equals("")
								? pipeline.getDefineName()
								: pipeline.getProjectName();
				}
				if (projectName == null)
					projectName = pipelineGroups.getDefineName() != null && !pipelineGroups.getDefineName().equals("")
							? pipelineGroups.getDefineName()
							: pipelineGroups.getProjectName();
				Project project = projectPlanAndTrackService.findByProjectName(projectName);
				projectNode.put("name", pipelineGroups.getProjectName());
				projectNode.put("status", project.getStatus());
				projectNode.put("inProduction", project.getInProd());
				projectNode.put("pipelineCount", pipelineGroups.getPipelines().size());
				projectNode.put("dimensions", dimesionList);
				projectsNode.add(projectNode);
			} catch (Exception ex) {
				logger.error("Exception occurred", ex);
			}
		}

		List<String> distinctList = getList(
				mongoTemplate.getCollection(mongoTemplate.getCollectionName(PipelineGroups.class))
						.distinct("pipelines.status", String.class));

		if (distinctList != null && distinctList.size() > 0) {
			for (String status : distinctList) {
				int count = 0;
				Query query = new Query();
				query.addCriteria(Criteria.where("pipelines.status").is(status));
				List<PipelineGroups> pipelineByStatus = mongoTemplate.find(query, PipelineGroups.class);
				for (PipelineGroups pipelineGroup : pipelineByStatus) {
					for (com.itorix.apiwiz.cicd.beans.Pipeline pipeline : pipelineGroup.getPipelines()) {
						if (status.equalsIgnoreCase(pipeline.getStatus())) {
							count += 1;
						}
					}
				}
				statsNode.put(status.toLowerCase(), count);
			}
		}
		statsNode.put("projects", projectsNode);
		rootNode.set("metrics", metricsNode);
		rootNode.set("stats", statsNode);

		return rootNode;
	}

	public void createcicdBackUp(BackUpRequest backUpRequest) throws ItorixException {
		List<BackUpRequest> findAll = mongoTemplate.findAll(BackUpRequest.class);
		if (findAll.size() > 0) {
			throw new ItorixException(new Throwable().getMessage(), "CICD-1001", new Throwable());
		} else {
			mongoTemplate.save(backUpRequest);
		}
	}

	public Object updatecicdBackUp(BackUpRequest backUpRequest) throws ItorixException {

		List<BackUpRequest> findAll = mongoTemplate.findAll(BackUpRequest.class);
		if (findAll.size() > 0) {
			mongoTemplate.dropCollection(BackUpRequest.class);
			mongoTemplate.save(backUpRequest);
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("CICD-1002"), "CICD-1002");
		}
		return "";
	}

	public Object getcicdBackUpDetails() throws ItorixException {
		List<BackUpRequest> findAll = mongoTemplate.findAll(BackUpRequest.class);
		if (findAll.size() > 0) {
			return findAll.get(0);
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("CICD-1002"), "CICD-1002");
		}
	}

	public Object getcicdBackUpHistory() throws ItorixException {
		List<BackUpHistory> backUpHistory = mongoTemplate.findAll(BackUpHistory.class);
		return backUpHistory;
	}

	// @Scheduled(cron = "* 0 0 * * ?")
	// @Scheduled(cron = "0/30 * * * * ?")
	public void SchedulecicdBackUpDaily() throws JsonProcessingException, IOException {
		GoCDIntegration goCDIntegration = getGocdIntegration();
		Query query = new Query();
		query.addCriteria(Criteria.where("interval").is("daily"));
		BackUpRequest backUpRequest = mongoTemplate.findOne(query, BackUpRequest.class);
		BackUpHistory backUpHistory = new BackUpHistory();
		if (backUpRequest != null) {

			HttpHeaders headers = new HttpHeaders();
			headers.set("Confirm", "true");
			headers.set("accept", "application/vnd.go.cd.v1+json");
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getInterceptors().add(
					new BasicAuthorizationInterceptor(goCDIntegration.getUsername(), goCDIntegration.getPassword()));
			HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
			logger.debug("Making a call to {}", goCDIntegration.getHostURL() + config.getCicdbackUp());
			ResponseEntity<String> responseEntity = restTemplate.exchange(
					goCDIntegration.getHostURL() + config.getCicdbackUp(), HttpMethod.POST, requestEntity,
					String.class);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonHistory = mapper.readTree(responseEntity.getBody());
			String path = jsonHistory.get("path").asText();

			backUpHistory.setPath(path);
			backUpHistory.setTime(jsonHistory.get("time").asText());
			/*
			 * backUpRequest.setPath(path);
			 * backUpRequest.setTime(jsonHistory.get("time").asText()); DBObject
			 * dbDoc = new BasicDBObject();
			 * mongoTemplate.getConverter().write(backUpRequest, dbDoc); Update
			 * update = Update.fromDBObject(dbDoc, "_id"); WriteResult result =
			 * mongoTemplate.updateFirst(query, update, BackUpRequest.class);
			 * log.info(result.isUpdateOfExisting());
			 */
			mongoTemplate.save(backUpHistory);
		}
	}

	// @Scheduled(cron = "* 0 0 1 * ?")
	public void SchedulecicdBackUpMonthly() throws JsonProcessingException, IOException {
		GoCDIntegration goCDIntegration = getGocdIntegration();
		Query query = new Query();
		query.addCriteria(Criteria.where("interval").is("monthly"));
		BackUpRequest backUpRequest = mongoTemplate.findOne(query, BackUpRequest.class);
		BackUpHistory backUpHistory = new BackUpHistory();
		if (backUpRequest != null) {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Confirm", "true");
			headers.set("accept", "application/vnd.go.cd.v1+json");
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getInterceptors().add(
					new BasicAuthorizationInterceptor(goCDIntegration.getUsername(), goCDIntegration.getPassword()));
			HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
			logger.debug("Making a call to {}", goCDIntegration.getHostURL() + config.getCicdbackUp());
			ResponseEntity<String> responseEntity = restTemplate.exchange(
					goCDIntegration.getHostURL() + config.getCicdbackUp(), HttpMethod.POST, requestEntity,
					String.class);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonHistory = mapper.readTree(responseEntity.getBody());
			String path = jsonHistory.get("path").asText();
			backUpHistory.setPath(path);
			backUpHistory.setTime(jsonHistory.get("time").asText());
			/*
			 * backUpRequest.setPath(path);
			 * backUpRequest.setTime(jsonHistory.get("time").asText()); DBObject
			 * dbDoc = new BasicDBObject();
			 * mongoTemplate.getConverter().write(backUpRequest, dbDoc); Update
			 * update = Update.fromDBObject(dbDoc, "_id"); WriteResult result =
			 * mongoTemplate.updateFirst(query, update, BackUpRequest.class);
			 * log.info(result.isUpdateOfExisting());
			 */
			mongoTemplate.save(backUpHistory);
		}
	}

	// @Scheduled(cron = "* 30 0 * * MON")
	public void SchedulecicdBackUpWeekly() throws JsonProcessingException, IOException {
		GoCDIntegration goCDIntegration = getGocdIntegration();
		Query query = new Query();
		query.addCriteria(Criteria.where("interval").is("weekly"));
		BackUpRequest backUpRequest = mongoTemplate.findOne(query, BackUpRequest.class);
		BackUpHistory backUpHistory = new BackUpHistory();
		if (backUpRequest != null) {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Confirm", "true");
			headers.set("accept", "application/vnd.go.cd.v1+json");
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getInterceptors().add(
					new BasicAuthorizationInterceptor(goCDIntegration.getUsername(), goCDIntegration.getPassword()));
			HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
			logger.debug("Making a call to {}", goCDIntegration.getHostURL() + config.getCicdbackUp());
			ResponseEntity<String> responseEntity = restTemplate.exchange(
					goCDIntegration.getHostURL() + config.getCicdbackUp(), HttpMethod.POST, requestEntity,
					String.class);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonHistory = mapper.readTree(responseEntity.getBody());
			String path = jsonHistory.get("path").asText();

			backUpHistory.setPath(path);
			backUpHistory.setTime(jsonHistory.get("time").asText());
			/*
			 * backUpRequest.setPath(path);
			 * backUpRequest.setTime(jsonHistory.get("time").asText()); DBObject
			 * dbDoc = new BasicDBObject();
			 * mongoTemplate.getConverter().write(backUpRequest, dbDoc); Update
			 * update = Update.fromDBObject(dbDoc, "_id"); WriteResult result =
			 * mongoTemplate.updateFirst(query, update, BackUpRequest.class);
			 * log.info(result.isUpdateOfExisting());
			 */
			mongoTemplate.save(backUpHistory);
		}
	}

	@SuppressWarnings("deprecation")
	public Object getGoCdHealth() {

		try {
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode rootNode = mapper.createObjectNode();
			ObjectNode serverNode = mapper.createObjectNode();
			ArrayNode agentNodes = mapper.createArrayNode();
			GoCDIntegration goCDIntegration = getGocdIntegration();
			HttpHeaders headers = new HttpHeaders();
			String accept = CiCdIntegrationHelper.getHeader(CiCdIntegrationHelper.HEALTH, goCDIntegration.getVersion());
			headers.set("accept", accept);
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getInterceptors().add(
					new BasicAuthorizationInterceptor(goCDIntegration.getUsername(), goCDIntegration.getPassword()));
			HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
			logger.debug("Making a call to {}", goCDIntegration.getHostURL() + config.getCicdServerHealth());
			ResponseEntity<String> responseEntity = restTemplate.exchange(
					goCDIntegration.getHostURL() + config.getCicdServerHealth(), HttpMethod.GET, requestEntity,
					String.class);
			logger.debug(responseEntity.toString());

			JsonNode readTree = mapper.readTree(responseEntity.getBody());
			String serverhealth = readTree.get("health").asText();
			serverNode.put("health", serverhealth);
			// serverNode.put("server",subServerNode);

			requestEntity = new HttpEntity<>("", headers);
			restTemplate.getInterceptors().add(
					new BasicAuthorizationInterceptor(goCDIntegration.getUsername(), goCDIntegration.getPassword()));
			logger.debug("Making a call to {}", goCDIntegration.getHostURL() + config.getCicdAgentHealth());
			responseEntity = restTemplate.exchange(goCDIntegration.getHostURL() + config.getCicdAgentHealth(),
					HttpMethod.GET, requestEntity, String.class);

			JsonNode readclientTree = mapper.readTree(responseEntity.getBody());
			JsonNode embedded = readclientTree.path("_embedded");
			JsonNode agents = embedded.path("agents");
			Iterator<JsonNode> agentelements = agents.elements();
			while (agentelements.hasNext()) {
				ObjectNode agentRootNode = mapper.createObjectNode();
				JsonNode agentNode = agentelements.next();
				agentRootNode.put("uuid", agentNode.get("uuid").asText());
				agentRootNode.put("hostname", agentNode.get("hostname").asText());
				agentRootNode.put("sandbox", agentNode.get("sandbox").asText());
				agentRootNode.put("ip_address", agentNode.get("ip_address").asText());
				agentRootNode.put("operating_system", agentNode.get("operating_system").asText());
				agentRootNode.put("free_space", agentNode.get("free_space").asText());
				agentRootNode.put("agent_state", agentNode.get("agent_state").asText());
				agentRootNode.put("build_state", agentNode.get("build_state").asText());
				agentNodes.add(agentRootNode);
			}
			rootNode.put("server", serverNode);
			rootNode.put("agents", agentNodes);
			return rootNode;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return "";
	}

	@SuppressWarnings({"unchecked", "deprecation"})
	public Object getreleaseStats(String timeunit, String timerange) throws ParseException {
		// logger.info("getreleaseStats", timeunit, timerange);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		String[] dates = timerange.split("~");

		Date startDate = null;
		Date endDate = null;

		if (dates != null && dates.length > 0) {
			startDate = dateFormat.parse(dates[0]);
			endDate = dateFormat.parse(dates[1]);
		}

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		ObjectNode metricsNode = mapper.createObjectNode();
		metricsNode.put("name", timeunit);
		ArrayNode valuesNode = mapper.createArrayNode();

		while (startDate.compareTo(endDate) <= 0) {
			Query query = new Query();
			query.addCriteria(Criteria.where(Package.LABEL_CREATED_TIME)
					.gte(new Long(DateUtil.getStartOfDay(startDate).getTime() + ""))
					.lt(new Long(DateUtil.getEndOfDay(startDate).getTime() + "")));
			List<Package> list = mongoTemplate.find(query, Package.class);
			// if(list!=null && list.size()>0){
			ObjectNode valueNode = mapper.createObjectNode();
			valueNode.put("timestamp", DateUtil.getStartOfDay(startDate).getTime() + "");
			valueNode.put("value", list.size());
			valuesNode.add(valueNode);
			// }
			startDate = DateUtil.addDays(startDate, 1);
		}
		metricsNode.set("values", valuesNode);

		ObjectNode statsNode = mapper.createObjectNode();
		List<Package> list = mongoTemplate.findAll(Package.class);
		ArrayNode packageList = mapper.createArrayNode();
		for (Package packagedetails : list) {
			ObjectNode projectNode = mapper.createObjectNode();
			projectNode.put("name", packagedetails.getDescription());
			projectNode.put("status", packagedetails.getState());
			packageList.add(projectNode);
		}

		List<String> distinctList = getList(mongoTemplate.getCollection(mongoTemplate.getCollectionName(Package.class))
				.distinct("state", String.class));

		if (distinctList != null && distinctList.size() > 0) {
			for (String status : distinctList) {
				Query query = new Query();
				query.addCriteria(Criteria.where("state").is(status));
				List<Package> packageStatus = mongoTemplate.find(query, Package.class);
				statsNode.put(status.toLowerCase(), packageStatus.size());
			}
		}
		statsNode.put("packages", packageList);
		rootNode.set("metrics", metricsNode);
		rootNode.set("stats", statsNode);

		return rootNode;
	}

	public Resource getArtifacts(String pipelineName, String pipelineCounter, String stageName, String stageCounter,
			String artifactName) {
		GoCDIntegration goCDIntegration = getGocdIntegration();
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors()
				.add(new BasicAuthorizationInterceptor(goCDIntegration.getUsername(), goCDIntegration.getPassword()));
		return restTemplate.getForObject(
				goCDIntegration.getHostURL() + config.getCicdArtifactUrl().replaceAll(":pipelineGroupName", "")
						.replaceAll(":pipelineName", pipelineName).replaceAll(":pipelineCounter", pipelineCounter)
						.replaceAll(":stageName", stageName).replaceAll(":stageCounter", stageCounter)
						.replaceAll(":jobName", "BuildAndDeploy").replaceAll(":artifactName", artifactName),
				Resource.class);
	}

	private List<String> getList(DistinctIterable<String> iterable) {
		MongoCursor<String> cursor = iterable.iterator();
		List<String> list = new ArrayList<>();
		while (cursor.hasNext()) {
			list.add(cursor.next());
		}
		return list;
	}

	private GoCDIntegration getGocdIntegration() {
		GoCDIntegration goCDIntegration = new GoCDIntegration();
		String hostURL = config.getPipelineBaseUrl();
		String username = config.getCicdAuthUserName();
		String password = config.getCicdAuthPassword();
		String version = config.getGocdVersion();
		goCDIntegration.setHostURL(hostURL);
		goCDIntegration.setPassword(password);
		goCDIntegration.setUsername(username);
		goCDIntegration.setVersion(version);
		List<Integration> integrations = integrationsDao.getIntegration("GOCD");
		if (integrations != null) {
			try {
				GoCDIntegration dbGoCDIntegration = integrations.get(0).getGoCDIntegration();
				RSAEncryption rSAEncryption = new RSAEncryption();
				password = rSAEncryption.decryptText(dbGoCDIntegration.getPassword());
				dbGoCDIntegration.setPassword(password);
				return dbGoCDIntegration;
			} catch (Exception e) {
			}
		}
		return goCDIntegration;
	}
}
