package com.itorix.apiwiz.portfolio.dao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.configmanagement.KVMEntry;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.integrations.Integration;
import com.itorix.apiwiz.common.model.integrations.git.GitIntegration;
import com.itorix.apiwiz.common.model.integrations.workspace.WorkspaceIntegration;
import com.itorix.apiwiz.common.model.integrations.workspace.WorkspaceIntegrationUtils;
import com.itorix.apiwiz.common.model.projectmanagement.Endpoints;
import com.itorix.apiwiz.common.model.projectmanagement.Organization;
import com.itorix.apiwiz.common.model.projectmanagement.Project;
import com.itorix.apiwiz.common.model.projectmanagement.ProjectMetaData;
import com.itorix.apiwiz.common.model.projectmanagement.ProjectProxyResponse;
import com.itorix.apiwiz.common.model.projectmanagement.ScmPromote;
import com.itorix.apiwiz.common.model.proxystudio.Category;
import com.itorix.apiwiz.common.model.proxystudio.CodeGenHistory;
import com.itorix.apiwiz.common.model.proxystudio.Policy;
import com.itorix.apiwiz.common.model.proxystudio.PortfolioProxy;
import com.itorix.apiwiz.common.model.proxystudio.Proxy;
import com.itorix.apiwiz.common.model.proxystudio.ProxyPortfolio;
import com.itorix.apiwiz.common.model.proxystudio.ProxyProject;
import com.itorix.apiwiz.common.model.proxystudio.ProxySCMDetails;
import com.itorix.apiwiz.common.model.proxystudio.Scm;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.common.util.scm.ScmUtilImpl;
import com.itorix.apiwiz.devstudio.businessImpl.CodeGenService;
import com.itorix.apiwiz.devstudio.dao.IntegrationsDao;
import com.itorix.apiwiz.devstudio.model.Operations;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.portfolio.model.db.Portfolio;
import com.itorix.apiwiz.portfolio.model.db.Projects;
import com.itorix.apiwiz.portfolio.model.db.proxy.Metadata;
import com.itorix.apiwiz.portfolio.model.db.proxy.Pipelines;
import com.itorix.apiwiz.portfolio.model.db.proxy.Policies;
import com.itorix.apiwiz.portfolio.model.db.proxy.PolicyCategory;
import com.itorix.apiwiz.portfolio.model.db.proxy.Proxies;
import com.itorix.apiwiz.portfolio.model.db.proxy.ScmConfig;
import com.itorix.apiwiz.portfolio.model.db.proxy.Stages;
import com.itorix.apiwiz.portfolio.model.db.proxy.WsdlFiles;
import com.itorix.apiwiz.projectmanagement.model.cicd.CodeCoverage;
import com.itorix.apiwiz.projectmanagement.model.cicd.Material;
import com.itorix.apiwiz.projectmanagement.model.cicd.PipelineGroups;
import com.itorix.apiwiz.projectmanagement.model.cicd.TestSuiteAndConfig;
import com.itorix.apiwiz.projectmanagement.model.cicd.UnitTests;
import com.itorix.apiwiz.serviceregistry.model.documents.ServiceRegistry;
import com.itorix.apiwiz.serviceregistry.model.documents.ServiceRegistryList;
import com.itorix.apiwiz.servicerequest.dao.ServiceRequestDao;
import com.itorix.apiwiz.servicerequest.model.ServiceRequest;

@Component
public class ProxyUtils {

	@Value("${server.port}")
	private String port;
	@Value("${server.contextPath}")
	private String context;

	@Autowired
	private CodeGenService codeGenService;
	@Autowired
	private ScmUtilImpl scmUtilImpl;
	@Autowired
	private ApplicationProperties applicationProperties;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private IdentityManagementDao commonServices;
	@Autowired
	private IdentityManagementDao identityManagementDao;
	@Autowired
	private ServiceRequestDao serviceRequestDao;
	@Autowired
	private WorkspaceIntegrationUtils workspaceIntegrationUtils;
	@Autowired
	private IntegrationsDao integrationsDao;

	private static final String GIT_HOST_URL = "https://github.com/asu2150/";

	public ProjectProxyResponse generateProxy(Portfolio portfolio, String projectId, String proxyId,
			String jsessionId) {
		ProjectProxyResponse response = new ProjectProxyResponse();
		try {
			Projects portfolioProject = portfolio.getProjects().stream().filter(p -> p.getId().equals(projectId))
					.findFirst().get();
			Proxies projectProxy = portfolioProject.getProxies().stream().filter(p -> p.getId().equals(proxyId))
					.findFirst().get();
			String projectName = portfolioProject.getName();
			Project project = populateProject(projectProxy, projectName);
			ObjectMapper mapper = new ObjectMapper();
			CodeGenHistory proxyGen = populateProxyGenerationObj(portfolio, projectId, proxyId);
			response.setGitRepoName(proxyGen.getProxySCMDetails().getReponame());
			response.setGitBranch(proxyGen.getProxySCMDetails().getBranch());
			String folderPath = applicationProperties.getTempDir() + "proxyGeneration";
			org.apache.commons.io.FileUtils.forceMkdir(new File(folderPath));
			Operations operations = new Operations();
			operations.setDir(folderPath);
			operations.setjSessionid(jsessionId);
			User user = commonServices.getUserDetailsFromSessionID(jsessionId);
			operations.setUser(user);
			codeGenService.processCodeGen(proxyGen, operations, project);
			org.apache.commons.io.FileUtils.deleteDirectory(new File(folderPath));
			response.setGitPush("true");
			if (null != projectProxy.getApigeeConfig().getScmConfig())
				projectProxy.getApigeeConfig().getScmConfig().setRepoName(proxyGen.getProxySCMDetails().getReponame());
			else {
				ScmConfig scmConfig = new ScmConfig();
				scmConfig.setRepoName(proxyGen.getProxySCMDetails().getReponame());
				projectProxy.getApigeeConfig().setScmConfig(scmConfig);
			}
			String pipelineName = createPipeline(projectProxy, projectName, proxyGen, jsessionId);
			response.setPipelineName(pipelineName);
			createServiceConfigs(projectProxy.getApigeeConfig().getPipelines(), projectProxy.getName(),
					getBranchType(proxyGen.getProxySCMDetails().getBranch()), jsessionId);
			mongoTemplate.save(portfolio);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	public void promoteProxy(ProxyPortfolio proxyPortfolio, Scm scm, Portfolio portfolio, String jsessionid) {
		String projectId = proxyPortfolio.getProjects().get(0).getId();
		String proxyId = proxyPortfolio.getProjects().get(0).getProxies().get(0).getId();
		String projectName = proxyPortfolio.getProjects().get(0).getName();
		Projects portfolioProject = portfolio.getProjects().stream().filter(p -> p.getId().equals(projectId))
				.findFirst().get();
		Proxies projectProxy = portfolioProject.getProxies().stream().filter(p -> p.getId().equals(proxyId)).findFirst()
				.get();
		ScmPromote scmPromote = new ScmPromote();
		scmPromote.setBaseBranch(scm.getBaseBranch());
		scmPromote.setTargetBranch(scm.getDestinationBranch());
		scmPromote.setRepoName(scm.getReponame());
		String proxyName = projectProxy.getName();
		try {
			promoteToMaster(projectProxy, scmPromote, projectName, proxyName, jsessionid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createRelease(Portfolio portfolio, String projectId, String proxyId, String releaseTag,
			String jsessionid) {
		Projects portfolioProject = portfolio.getProjects().stream().filter(p -> p.getId().equals(projectId))
				.findFirst().get();
		Proxies projectProxy = portfolioProject.getProxies().stream().filter(p -> p.getId().equals(proxyId)).findFirst()
				.get();
		String projectName = portfolioProject.getName();
		ScmPromote scmPromote = new ScmPromote();
		scmPromote.setBaseBranch("master");
		scmPromote.setTargetBranch("release-" + releaseTag.trim());
		scmPromote.setRepoName(projectProxy.getApigeeConfig().getScmConfig().getRepoName());
		String proxyName = projectProxy.getName();
		try {
			promoteToMaster(projectProxy, scmPromote, projectName, proxyName, jsessionid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String createPipeline(Proxies projectProxy, String projectName, CodeGenHistory proxyGen, String jsessionId)
			throws ItorixException {
		PipelineGroups pipelineGroups = new PipelineGroups();
		pipelineGroups.setProjectName(projectName.replaceAll("\\.", ""));
		com.itorix.apiwiz.projectmanagement.model.cicd.Pipeline pipeline = new com.itorix.apiwiz.projectmanagement.model.cicd.Pipeline();
		pipeline.setDisplayName(proxyGen.getProxy().getName().replaceAll("\\.", "").trim() + "_"
				+ proxyGen.getProxySCMDetails().getBranch());
		pipeline.setProxyName(proxyGen.getProxy().getName().replaceAll("\\.", "").trim());
		pipeline.setType("proxies");
		pipeline.setProjectName(projectName);
		pipeline.setMaterials(populateMaterials(proxyGen.getProxySCMDetails()));
		pipeline.setStages(populateStages(projectProxy.getApigeeConfig().getPipelines(),
				getBranchType(proxyGen.getProxySCMDetails().getBranch())));
		pipeline.setVersion(proxyGen.getProxy().getVersion().replaceAll("\\.", "").trim());
		List<com.itorix.apiwiz.projectmanagement.model.cicd.Pipeline> pipelines = new ArrayList<com.itorix.apiwiz.projectmanagement.model.cicd.Pipeline>();
		pipelines.add(pipeline);
		pipelineGroups.setPipelines(pipelines);
		String branch = pipeline.getMaterials().get(0).getScmBranch();
		if (!isPipelineExists(projectName.replaceAll(" ", "-").replaceAll("\\.", ""), branch, jsessionId))
			createPipeline(pipelineGroups, jsessionId);
		return projectName.replaceAll(" ", "-").replaceAll("\\.", "") + "_"
				+ proxyGen.getProxy().getName().replaceAll("\\.", "").trim() + "_"
				+ proxyGen.getProxy().getVersion().replaceAll("\\.", "").trim() + "_"
				+ proxyGen.getProxySCMDetails().getBranch();
	}

	private boolean isPipelineExists(String projectName, String branchName, String jsessionid) throws ItorixException {
		PipelineGroups pipelineGroup = getPipelines(projectName, jsessionid);
		if (pipelineGroup != null)
			for (com.itorix.apiwiz.projectmanagement.model.cicd.Pipeline pipeline : pipelineGroup.getPipelines()) {
				if (pipeline.getName().contains(branchName))
					return true;
			}
		return false;
	}

	private void createPipeline(PipelineGroups pipelineGroups, String jsessionId) throws ItorixException {
		try {
			String hostUrl = "http://localhost:#port#/#context#/v1/pipelines";
			hostUrl = hostUrl.replaceAll("#port#", port);
			hostUrl = hostUrl.replaceAll("#context#", context.replaceAll("/", ""));
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("JSESSIONID", jsessionId);
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<PipelineGroups> requestEntity = new HttpEntity<>(pipelineGroups, headers);
			// ResponseEntity<String> response =
			restTemplate.exchange(hostUrl, HttpMethod.POST, requestEntity, String.class);
		} catch (Exception e) {
			throw new ItorixException("error creating pipeline", "", e);
		}
	}

	private List<Material> populateMaterials(ProxySCMDetails proxySCMDetails) {
		List<Material> materials = new ArrayList<Material>();
		Material material = new Material();
		material.setScmBranch(proxySCMDetails.getBranch());
		material.setScmRepo(proxySCMDetails.getReponame());
		material.setScmType(proxySCMDetails.getScmSource());
		material.setScmURL(proxySCMDetails.getHostUrl());
		materials.add(material);
		return materials;
	}

	private List<com.itorix.apiwiz.projectmanagement.model.cicd.Stage> populateStages(List<Pipelines> pipelines,
			String branchtype) {
		List<com.itorix.apiwiz.projectmanagement.model.cicd.Stage> stages = new ArrayList<com.itorix.apiwiz.projectmanagement.model.cicd.Stage>();
		for (Pipelines pipeline : pipelines)
			if (pipeline.getBranchType().equals(branchtype))
				for (Stages stage : pipeline.getStages()) {
					com.itorix.apiwiz.projectmanagement.model.cicd.Stage pipelienStage = new com.itorix.apiwiz.projectmanagement.model.cicd.Stage();
					pipelienStage.setName(stage.getName().trim());
					pipelienStage.setType(stage.getType().trim());
					pipelienStage.setOrgName(stage.getOrgName().trim());
					pipelienStage.setEnvName(stage.getEnvName().trim());
					pipelienStage.setIsSaas(stage.getType().equalsIgnoreCase("onprem") ? false : true);
					pipelienStage.setSequenceID(stage.getSequenceID());
					UnitTests unitTests = new UnitTests();
					unitTests.setEnabled("false");
					if (null != stage.getUnitTests()) {
						unitTests.setEnabled(stage.getUnitTests().getEnabled());
						unitTests.setAcceptance(stage.getUnitTests().getAcceptance());
						unitTests.setArtifactType(stage.getUnitTests().getArtifactType());
						if (stage.getUnitTests().getTestsuites() != null) {
							List<TestSuiteAndConfig> testSuites = new ArrayList<TestSuiteAndConfig>();
							TestSuiteAndConfig testSuiteAndConfig = new TestSuiteAndConfig();
							testSuites.add(testSuiteAndConfig);
							testSuiteAndConfig
									.setTestSuiteId(stage.getUnitTests().getTestsuites().get(0).getTestSuiteId());
							testSuiteAndConfig
									.setEnvironmentId(stage.getUnitTests().getTestsuites().get(0).getEnvironmentId());
							unitTests.setTestSuites(testSuites);
						}
					}
					pipelienStage.setUnitTests(unitTests);
					CodeCoverage codecoverage = new CodeCoverage();
					codecoverage.setEnabled("false");
					if (null != stage.getCodeCoverage()) {
						codecoverage.setEnabled(stage.getCodeCoverage().getEnabled());
						codecoverage.setAcceptance(stage.getCodeCoverage().getAcceptance());
						codecoverage.setArtifactType(stage.getCodeCoverage().getArtifactType());
						if (stage.getCodeCoverage().getTestsuites() != null) {
							List<TestSuiteAndConfig> testSuites = new ArrayList<TestSuiteAndConfig>();
							TestSuiteAndConfig testSuiteAndConfig = new TestSuiteAndConfig();
							testSuites.add(testSuiteAndConfig);
							testSuiteAndConfig
									.setTestSuiteId(stage.getCodeCoverage().getTestsuites().get(0).getTestSuiteId());
							testSuiteAndConfig.setEnvironmentId(
									stage.getCodeCoverage().getTestsuites().get(0).getEnvironmentId());
							codecoverage.setTestSuites(testSuites);
						}
					}
					pipelienStage.setCodeCoverage(codecoverage);
					stages.add(pipelienStage);
				}
		return stages;
	}

	private PipelineGroups getPipelines(String projectName, String jsessionId) throws ItorixException {
		try {
			String hostUrl = "http://localhost:#port#/#context#/v1/pipelines/" + projectName;
			hostUrl = hostUrl.replaceAll("#port#", port);
			hostUrl = hostUrl.replaceAll("#context#", context.replaceAll("/", ""));
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("JSESSIONID", jsessionId);
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
			ResponseEntity<PipelineGroups> response = restTemplate.exchange(hostUrl, HttpMethod.GET, requestEntity,
					PipelineGroups.class);
			return response.getBody();
		} catch (Exception e) {
			return null;
		}
	}

	private Project populateProject(Proxies projectProxy, String projectName) {
		Project project = new Project();
		project.setName(projectName);
		List<com.itorix.apiwiz.common.model.projectmanagement.Proxies> proxies = new ArrayList<>();
		com.itorix.apiwiz.common.model.projectmanagement.Proxies proxy = new com.itorix.apiwiz.common.model.projectmanagement.Proxies();
		proxies.add(proxy);
		project.setProxies(proxies);
		proxy.setName(projectProxy.getName());
		proxy.setApigeeVirtualHosts(populateVirtualHosts(projectProxy.getApigeeConfig().getApigeeVirtualHosts()));
		proxy.setProjectMetaData(populateMetadata(projectProxy.getApigeeConfig().getMetadata()));
		return project;
	}

	private List<ProjectMetaData> populateMetadata(List<Metadata> metadataList) {
		List<ProjectMetaData> projectMetaDataList = new ArrayList<>();
		for (Metadata metadata : metadataList) {
			ProjectMetaData projectMetaData = new ProjectMetaData();
			projectMetaData.setName(metadata.getName());
			projectMetaData.setValue(metadata.getValue());
			projectMetaDataList.add(projectMetaData);
		}
		return projectMetaDataList;
	}

	private Set<String> populateVirtualHosts(List<String> apigeeVirtualHosts) {
		Set<String> virtualHosts = new HashSet<>();
		for (String host : apigeeVirtualHosts)
			virtualHosts.add(host);
		return virtualHosts;
	}

	private CodeGenHistory populateProxyGenerationObj(Portfolio portfolio, String projectId, String proxyId)
			throws ItorixException {
		try {
			Projects project = portfolio.getProjects().stream().filter(p -> p.getId().equals(projectId)).findFirst()
					.get();
			Proxies projectProxy = project.getProxies().stream().filter(p -> p.getId().equals(proxyId)).findFirst()
					.get();
			ProxyPortfolio proxyPortfolio = new ProxyPortfolio();
			proxyPortfolio.setId(portfolio.getId());
			proxyPortfolio.setName(portfolio.getName());
			List<ProxyProject> projects = new ArrayList<>();
			proxyPortfolio.setProjects(projects);
			ProxyProject proxyProject = new ProxyProject();
			projects.add(proxyProject);
			proxyProject.setId(project.getId());
			proxyProject.setName(project.getName());
			List<PortfolioProxy> proxies = new ArrayList<>();
			proxyProject.setProxies(proxies);
			PortfolioProxy portfolioProxy = new PortfolioProxy();
			proxies.add(portfolioProxy);
			portfolioProxy.setId(projectProxy.getId());
			portfolioProxy.setName(projectProxy.getName());
			CodeGenHistory codeGenHistory = new CodeGenHistory();
			List<Category> policyTemplates = populatePolicyTemplates(projectProxy);
			codeGenHistory.setPolicyTemplates(policyTemplates);
			codeGenHistory.setProxy(populateProxy(project.getProxies().get(0), portfolio.getName()));
			codeGenHistory.setProxySCMDetails(populateProxySCMDetails(projectProxy.getName()));
			codeGenHistory.setPortfolio(proxyPortfolio);
			return codeGenHistory;
		} catch (ItorixException e) {
			e.printStackTrace();
			throw new ItorixException("unable to create repo ", "", e);
		}
	}

	private Proxy populateProxy(Proxies projectProxy, String name) {
		ObjectMapper mapper = new ObjectMapper();
		String proxyBuildArtifact = "";
		if (projectProxy.getApigeeConfig().getDesignArtifacts() != null
				&& projectProxy.getApigeeConfig().getDesignArtifacts().getWsdlFiles() != null)
			for (WsdlFiles file : projectProxy.getApigeeConfig().getDesignArtifacts().getWsdlFiles())
				proxyBuildArtifact = proxyBuildArtifact + file.getWsdlName() + "  ";
		Proxy proxy = new Proxy();
		String path = "";
		try {
			System.out.println(mapper.writeValueAsString(projectProxy.getBasePaths()));
			path = mapper.writeValueAsString(projectProxy.getBasePaths()).replaceAll("\"", "").replaceAll("\\[", "")
					.replaceAll("\\]", "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		proxy.setBasePath(path);
		proxy.setName(projectProxy.getName());
		proxy.setDescription(projectProxy.getName());
		proxy.setVersion(projectProxy.getProxyVersion());
		proxy.setBuildProxyArtifactType("Portfolio");
		proxy.setBuildProxyArtifact(name);
		proxy.setBranchType("feature");
		return proxy;
	}

	private ProxySCMDetails populateProxySCMDetails(String proxyName) throws ItorixException {
		String repoName = createSCMRepo(proxyName);
		String branch = "feature-" + System.currentTimeMillis();
		ProxySCMDetails proxySCMDetails = new ProxySCMDetails();
		proxySCMDetails.setReponame(repoName);
		proxySCMDetails.setBranch(branch);
		proxySCMDetails.setScmSource("GIT");
		proxySCMDetails.setHostUrl(GIT_HOST_URL + repoName);
		proxySCMDetails.setUsername(getBuildScmProp("itorix.core.scm.username" ));
		proxySCMDetails.setPassword(getBuildScmProp("itorix.core.scm.token"));
		createSCMBranch(proxySCMDetails);
		return proxySCMDetails;
	}
	
	private String getBuildScmProp(String key) {
		try{
		Query query = new Query();
		query.addCriteria(Criteria.where("propertyKey").is(key));
		WorkspaceIntegration integration = mongoTemplate.findOne(query, WorkspaceIntegration.class);
		if (integration != null) {
			RSAEncryption rSAEncryption;
			rSAEncryption = new RSAEncryption();
			return integration.getEncrypt()
					? rSAEncryption.decryptText(integration.getPropertyValue())
					: integration.getPropertyValue();
		} 
		}catch(Exception e){
			
		}
		return null;
	}

	private String createSCMRepo(String proxyName) throws ItorixException {
		String repoName = "apigee-" + proxyName;
		try {
			scmUtilImpl.createRepository(repoName, "Created from Itorix platform", "https://api.github.com/user/repos",
					applicationProperties.getProxyScmUserName(), applicationProperties.getProxyScmPassword());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return repoName;
	}

	private void createSCMBranch(ProxySCMDetails proxySCMDetails) throws ItorixException {
		scmUtilImpl.createBranch(proxySCMDetails.getBranch(), "", proxySCMDetails.getHostUrl(), proxySCMDetails.getPassword());
				//proxySCMDetails.getUsername(), proxySCMDetails.getPassword());
	}

	private String getBranchType(String branchName) {
		if (branchName.contains("feature"))
			return "feature";
		if (branchName.contains("master"))
			return "master";
		if (branchName.contains("release"))
			return "release";
		return null;
	}

	private List<Category> populatePolicyTemplates(Proxies projectProxy) {
		List<PolicyCategory> policyCategory = projectProxy.getApigeeConfig().getPolicyCategory();
		List<Category> policyTemplates = mongoTemplate.findAll(Category.class);
		for (Category category : policyTemplates) {
			for (Policy policy : category.getPolicies()) {
				try {
					PolicyCategory categoryElem = policyCategory.stream()
							.filter(p -> p.getName().equals(category.getName())).findFirst().get();
					if (categoryElem != null) {
						Policies policyElem = categoryElem.getPolicies().stream()
								.filter(p -> p.getName().equals(policy.getName())).findFirst().get();
						policy.setEnabled(policyElem.isEnabled());
					}
				} catch (Exception e) {
				}
			}
		}
		return policyTemplates;
	}

	private void promoteToMaster(Proxies projectProxy, ScmPromote scmPromote, String projectName, String proxyName,
			String jsessionid) throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		String repoName = scmPromote.getRepoName();
		String gitURL = workspaceIntegrationUtils.getBuildScmProp("GitHost");
		String gitType = workspaceIntegrationUtils.getBuildScmProp("GitHost.type");
		Integration integration = integrationsDao.getGitIntegration(gitType.toUpperCase(), "proxy");
		GitIntegration gitIntegration = null;
		if (integration != null)
			gitIntegration = integration.getGitIntegration();
		String hostUrl = (null != gitURL ? gitURL : GIT_HOST_URL) + repoName;
		String sourceBranch = scmPromote.getBaseBranch();
		String targetBranch = scmPromote.getTargetBranch();
		try {
			if (null != gitIntegration) {
				RSAEncryption rSAEncryption = new RSAEncryption();
				if (gitIntegration.getAuthType().equalsIgnoreCase("token")) {
					String token = rSAEncryption.decryptText(gitIntegration.getToken());
					scmUtilImpl.promoteToGitToken(sourceBranch, targetBranch, hostUrl, gitType, token, null);
				} else {
					String username = gitIntegration.getUsername();
					String password = rSAEncryption.decryptText(gitIntegration.getPassword());
					scmUtilImpl.promoteToGit(sourceBranch, targetBranch, hostUrl, username, password, null);
				}
			}
			createPromotePipeline(projectProxy, scmPromote, projectName, proxyName, jsessionid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createRelease(Proxies projectProxy, ScmPromote scmPromote, String projectName, String proxyName,
			String jsessionid) throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		String repoName = scmPromote.getRepoName();
		String hostUrl = GIT_HOST_URL + repoName;
		String sourceBranch = scmPromote.getBaseBranch();
		String targetBranch = scmPromote.getTargetBranch();
		scmUtilImpl.promoteToGit(sourceBranch, targetBranch, hostUrl, applicationProperties.getProxyScmUserName(),
				applicationProperties.getProxyScmPassword(), null);
		try {
			createPromotePipeline(projectProxy, scmPromote, projectName, proxyName, jsessionid);
		} catch (ItorixException e) {
			e.printStackTrace();
		}
	}

	private void createPromotePipeline(Proxies projectProxy, ScmPromote scmPromote, String projectName,
			String proxyName, String jsessionid) throws ItorixException {
		PipelineGroups pipelineGroups = new PipelineGroups();
		pipelineGroups.setProjectName(projectName.replaceAll("\\.", ""));
		com.itorix.apiwiz.projectmanagement.model.cicd.Pipeline pipeline = new com.itorix.apiwiz.projectmanagement.model.cicd.Pipeline();
		pipeline.setDisplayName(proxyName.replaceAll("\\.", "").trim() + "_" + scmPromote.getTargetBranch());
		pipeline.setProxyName(proxyName.replaceAll("\\.", "").trim());
		pipeline.setType("proxies");
		pipeline.setProjectName(projectName);
		ProxySCMDetails proxySCMDetails = new ProxySCMDetails();
		proxySCMDetails.setBranch(scmPromote.getTargetBranch());
		proxySCMDetails.setHostUrl(GIT_HOST_URL + scmPromote.getRepoName());
		proxySCMDetails.setReponame(scmPromote.getRepoName());
		proxySCMDetails.setScmSource("GIT");

		pipeline.setMaterials(populateMaterials(proxySCMDetails));
		pipeline.setStages(populateStages(projectProxy.getApigeeConfig().getPipelines(),
				getBranchType(scmPromote.getTargetBranch())));
		pipeline.setVersion(projectProxy.getProxyVersion().replaceAll("\\.", "").trim());
		List<com.itorix.apiwiz.projectmanagement.model.cicd.Pipeline> pipelines = new ArrayList<com.itorix.apiwiz.projectmanagement.model.cicd.Pipeline>();
		pipelines.add(pipeline);
		pipelineGroups.setPipelines(pipelines);
		String branch = scmPromote.getTargetBranch();
		createPipeline(pipelineGroups, jsessionid);
		createServiceConfigs(projectProxy.getApigeeConfig().getPipelines(), projectProxy.getName(),
				getBranchType(branch), jsessionid);
	}

	private void createServiceConfigs(List<Pipelines> pipelines, String proxyName, String branchType, String jsessionid)
			throws ItorixException {
		for (Pipelines pipeline : pipelines) {
			if (pipeline.getBranchType().equals(branchType)) {
				for (Stages stage : pipeline.getStages()) {
					Organization org = new Organization();
					org.setName(stage.getOrgName());
					org.setEnv(stage.getEnvName());
					org.setType(stage.getType());
					String registryId = getRegestryId(proxyName + "_" + stage.getEnvName().toUpperCase());
					createServiceConfig(org, proxyName, registryId, jsessionid);
				}
			}
		}
	}

	public void createServiceConfig(Organization organization, String proxyName, String registryId, String jsessionId)
			throws ItorixException {
		try {
			List<Map<String, String>> serviceRegistry = getServiceRegistryEntries(registryId);
			if (serviceRegistry == null)
				throw new ItorixException("no service registry for proxy " + proxyName + " registry Id " + registryId,
						"");
			ServiceRequest config = new ServiceRequest();
			config.setType("KVM");
			config.setName(proxyName);
			config.setOrg(organization.getName());
			config.setEnv(organization.getEnv());
			config.setEncrypted("false");
			config.setIsSaaS(organization.getType().equalsIgnoreCase("saas") ? true : false);
			KVMEntry entry = new KVMEntry();
			entry.setName("endpoints");
			entry.setValue(getEndpoints(serviceRegistry));
			List<KVMEntry> entries = new ArrayList<KVMEntry>();
			entries.add(entry);
			config.setEntry(entries);
			User user = identityManagementDao.getUserDetailsFromSessionID(jsessionId);
			config.setCreatedUser(user.getFirstName() + " " + user.getLastName());
			config.setCreatedUserEmailId(user.getEmail());
			config.setCreatedDate(new Date(System.currentTimeMillis()));
			config.setModifiedUser(user.getFirstName() + " " + user.getLastName());
			config.setModifiedDate(new Date(System.currentTimeMillis()));
			config.setStatus("Review");
			config.setCreated(false);
			config.setActiveFlag(Boolean.TRUE);
			serviceRequestDao.createServiceRequest(config);
			config.setStatus("Approved");
			List<String> roles = identityManagementDao.getUserRoles(jsessionId); // user.getRoles();
			if (!roles.contains("Admin"))
				roles.add("Admin");
			config.setUserRole(roles);
			serviceRequestDao.changeServiceRequestStatus(config, user);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	private String getEndpoints(List<Map<String, String>> registryEndpoints) {
		Endpoints endpoints = new Endpoints();
		endpoints.setEndpoints(registryEndpoints);
		try {
			String value = new ObjectMapper().writeValueAsString(endpoints);
			value = value.replaceAll("\"endpoints\"", "\"Endpoints\"").replaceAll("\"endpoint\"", "\"Endpoint\"");
			return value;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "";
		}
	}

	private String getRegestryId(String name) {
		Query query = new Query(Criteria.where("name").is(name));
		ServiceRegistryList serviceRegistry = mongoTemplate.findOne(query, ServiceRegistryList.class);
		return serviceRegistry.getId();
	}

	private List<Map<String, String>> getServiceRegistryEntries(String serviceRegistryId) {
		List<ServiceRegistry> rowList = mongoTemplate
				.find(new Query(Criteria.where("serviceRegistryId").is(serviceRegistryId)), ServiceRegistry.class);
		if (rowList != null) {
			List<Map<String, String>> data = new ArrayList<>();
			for (ServiceRegistry row : rowList) {
				data.add(row.getData());
			}
			return data;
		}
		return null;

	}

}
