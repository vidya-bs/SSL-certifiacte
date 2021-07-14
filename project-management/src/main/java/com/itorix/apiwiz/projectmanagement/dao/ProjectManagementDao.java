package com.itorix.apiwiz.projectmanagement.dao;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.common.model.SearchItem;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.projectmanagement.ArtifactType;
import com.itorix.apiwiz.common.model.projectmanagement.Category;
import com.itorix.apiwiz.common.model.projectmanagement.Contacts;
import com.itorix.apiwiz.common.model.projectmanagement.Organization;
import com.itorix.apiwiz.common.model.projectmanagement.Pipeline;
import com.itorix.apiwiz.common.model.projectmanagement.Pipelines;
import com.itorix.apiwiz.common.model.projectmanagement.Project;
import com.itorix.apiwiz.common.model.projectmanagement.ProjectFile;
import com.itorix.apiwiz.common.model.projectmanagement.ProjectProxyResponse;
import com.itorix.apiwiz.common.model.projectmanagement.Proxies;
import com.itorix.apiwiz.common.model.projectmanagement.ScmPromote;
import com.itorix.apiwiz.common.model.projectmanagement.ServiceRegistry;
import com.itorix.apiwiz.common.model.projectmanagement.Stage;
import com.itorix.apiwiz.common.model.proxystudio.CodeGenHistory;
import com.itorix.apiwiz.common.model.proxystudio.OrgEnv;
import com.itorix.apiwiz.common.model.proxystudio.Proxy;
import com.itorix.apiwiz.common.model.proxystudio.ProxySCMDetails;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.Date.DateUtil;
import com.itorix.apiwiz.common.util.scm.ScmUtilImpl;
import com.itorix.apiwiz.common.util.zip.ZIPUtil;
import com.itorix.apiwiz.data.management.dao.PolicyMappingDAO;
import com.itorix.apiwiz.data.management.model.PolicyMapping;
import com.itorix.apiwiz.devstudio.businessImpl.CodeGenService;
import com.itorix.apiwiz.devstudio.model.Operations;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.Pagination;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.itorix.apiwiz.projectmanagement.businessImpl.ProjectBusinessImpl;
import com.itorix.apiwiz.projectmanagement.model.ProjectHistoryResponse;
import com.itorix.apiwiz.projectmanagement.model.cicd.CodeCoverage;
import com.itorix.apiwiz.projectmanagement.model.cicd.Material;
import com.itorix.apiwiz.projectmanagement.model.cicd.PipelineGroups;
import com.itorix.apiwiz.projectmanagement.model.cicd.TestSuiteAndConfig;
import com.itorix.apiwiz.projectmanagement.model.cicd.UnitTests;
import com.mongodb.DB;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

import net.sf.json.JSONObject;

@Component
public class ProjectManagementDao {

	private Logger logger = LoggerFactory.getLogger(ProjectManagementDao.class);

//	@Autowired
//	protected UserSessionRepository userSessionRepository;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	MongoOperations mongoOperations;
	@Autowired
	protected BaseRepository baseRepository;
	@Autowired
	private ApplicationProperties applicationProperties;
	@Autowired
	private ScmUtilImpl scmUtilImpl;
	@Autowired
	private ProjectBusinessImpl projectBusinessImpl;

	@Autowired
	private CodeGenService codeGenService;
	@Autowired
	private IdentityManagementDao commonServices;

	@Autowired
	private PolicyMappingDAO policyMappingDAO;

	@Value("${server.port}")
	private String port;
	@Value("${server.contextPath}")
	private String context;

	private static final String GIT_HOST_URL = "https://github.com/itorix-api/";

	public Object createNewProject(Project project, String jsessionid) throws ItorixException {

		Project projectexists = findByProjectName(project.getName());
		if (projectexists == null) {
			/*	UserSession userSessionToken =userSessionRepository.findOne(jsessionid);
			User dbUser = mongoOperations.findById(userSessionToken.getUserId(),User.class);*/
			UserSession userSession  = UserSession.getCurrentSessionToken();
			User dbUser = userSession.getUser(); //identityManagementDao.getUserDetailsFromSessionID(jsessionid);
			project.setCts(System.currentTimeMillis());
			project.setMts(System.currentTimeMillis());
			project.setCreatedUserName(dbUser.getFirstName() + " " + dbUser.getLastName());
			project.setModifiedUserName(dbUser.getFirstName() + " " + dbUser.getLastName());
			if(project.getProxies()!=null && project.getProxies().size()>0) {
				for(Proxies proxy : project.getProxies()) {
					if(proxy.getPipelines()!=null) {
						proxy.setPipelines(setArtifactType(proxy.getPipelines()));
					}
					if(proxy.getXsdFiles()!=null) {
						proxy.setXsdFiles(updateProxyFiles(project.getName(), proxy.getName(), proxy.getXsdFiles(), "XSD"));
					}
					if(proxy.getWsdlFiles() != null) {
						proxy.setWsdlFiles(updateProxyFiles(project.getName(), proxy.getName(), proxy.getWsdlFiles(), "WSDL"));
					}
					if(proxy.getAttachments() != null) {
						proxy.setAttachments(updateProxyFiles(project.getName(), proxy.getName(), proxy.getAttachments(), "ATTACHMENT"));
					}
				}
			}
			mongoTemplate.save(project);
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("PROJECT_PLAN_TRACK_001"),"PROJECT_PLAN_TRACK_001");
		}
		return "";
	}

	private List<Pipeline> setArtifactType(List<Pipeline> pipelines){
		for(Pipeline pipeline: pipelines) {
			for (Stage stage: pipeline.getStages()) {
				String artifactType =null;
				if(stage.getUnitTests().getArtifactType()!=null) {
					try {
						artifactType = stage.getUnitTests().getArtifactType();
						String artifact = ArtifactType.valueOf(artifactType).getResponse();
						stage.getUnitTests().setArtifactType(artifact);
					}
					catch(Exception e) {
						stage.getUnitTests().setArtifactType("null");
					}
				}
				if(stage.getCodeCoverage().getArtifactType()!=null) {
					artifactType =null;
					try {
						artifactType = stage.getCodeCoverage().getArtifactType();
						String artifact = ArtifactType.valueOf(artifactType).getResponse();
						stage.getCodeCoverage().setArtifactType(artifact);
					}
					catch(Exception e) {
						stage.getCodeCoverage().setArtifactType("null");
					}
				}
			}
		}
		return pipelines;
	}


	public Project findByProjectName(String name) {
		Query query = new Query(Criteria.where(Project.LABEL_NAME).in(name));
		Project project = mongoTemplate.findOne(query, Project.class);
		return project;
	}
	public Object getAllProjectNames() throws ItorixException {

		List<Project> allprojects = mongoTemplate.findAll(Project.class);
		JSONObject projectsList = new JSONObject();
		List<String> projectNames = new ArrayList<String>();
		for (Project project : allprojects) {
			projectNames.add(project.getName());
		}
		projectsList.put("Projects", projectNames);
		return projectsList;
	}

	public Object findProjectByName(String projectName) throws ItorixException {

		String json = null;
		Project particularProject = findByProjectName(projectName);
		if(particularProject!=null){
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			try {
				json = mapper.writeValueAsString(particularProject);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			return json;
		}else{

			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("ProjectPlan-1001"),projectName ),"ProjectPlan-1001");
		}
	}
	public Object findOrganistionsOfProject(String projectName) throws ItorixException {

		Set<Organization> organizationListPerProxy = new HashSet<Organization>();
		JSONObject organizationList = new JSONObject();
		Project particularProject = findByProjectName(projectName);
		if (particularProject != null) {
			for (Proxies proxie : particularProject.getProxies()) {
				for (Organization organization : proxie.getOrganization()) {
					organizationListPerProxy.add(organization);
				}
			}
		} else {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("ProjectPlan-1001"),projectName ),"ProjectPlan-1001");

		}
		organizationList.put("Organisations", organizationListPerProxy);
		return organizationList;
	}

	public Object findOrganistionsOfProxyOfProject(String projectName, String proxiesName) throws ItorixException {

		Set<Organization> organizationListPerProxy = new HashSet<Organization>();
		JSONObject organizationList = new JSONObject();
		Project particularProject = findByProjectName(projectName);
		if (particularProject != null) {
			for (Proxies proxie : particularProject.getProxies()) {
				if (proxiesName.equalsIgnoreCase(proxie.getName())) {
					for (Organization organization : proxie.getOrganization()) {
						organizationListPerProxy.add(organization);
					}
				}else{
					throw new ItorixException(ErrorCodes.errorMessage.get("ProjectPlan-1006"),"ProjectPlan-1006");
				}
			}
		} else {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("ProjectPlan-1001"),projectName ),"ProjectPlan-1001");
		}
		organizationList.put("Organisations", organizationListPerProxy);
		return organizationList;
	}
	public Object findProxiesForProject(String projectName) throws ItorixException {

		Set<String> proxies = new HashSet<String>();
		JSONObject proxiesList = new JSONObject();
		Project particularProject = findByProjectName(projectName);
		if (particularProject != null) {
			for (Proxies proxie : particularProject.getProxies()) {
				proxies.add(proxie.getName());
			}
		} else {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("ProjectPlan-1001"),projectName ),"ProjectPlan-1001");
		}
		proxiesList.put("Proxies", proxies);
		return proxiesList;
	}

	public Object findProductsForProject(String projectName) throws ItorixException {

		Set<String> productsListPerProxy = new HashSet<String>();
		JSONObject productList = new JSONObject();
		Project particularProject = findByProjectName(projectName);
		if (particularProject != null) {
			for (Proxies proxie : particularProject.getProxies()) {
				for (String product : proxie.getProducts()) {
					productsListPerProxy.add(product);
				}
			}
		} else {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("ProjectPlan-1001"),projectName ),"ProjectPlan-1001");
		}
		productList.put("Products", productsListPerProxy);
		return productList;
	}

	public Object findProductsForProxyForProject(String projectName, String proxiesName) throws ItorixException {

		Set<String> productsListPerProxy = new HashSet<String>();
		JSONObject productList = new JSONObject();
		Project particularProject = findByProjectName(projectName);
		if (particularProject != null) {
			for (Proxies proxie : particularProject.getProxies()) {
				if (proxiesName.equalsIgnoreCase(proxie.getName())) {
					for (String product : proxie.getProducts()) {
						productsListPerProxy.add(product);
					}
				}else{
					throw new ItorixException(ErrorCodes.errorMessage.get("ProjectPlan-1006"),"ProjectPlan-1006");
				}
			}
		} else {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("ProjectPlan-1001"),projectName ),"ProjectPlan-1001");
		}
		productList.put("Products", productsListPerProxy);
		return productList;
	}
	public Object findAppsForProject(String projectName) throws ItorixException {

		Set<String> appsListPerProject = new HashSet<String>();
		JSONObject applicationsList = new JSONObject();
		Project particularProject = findByProjectName(projectName);
		if (particularProject != null) {
			for (Proxies proxie : particularProject.getProxies()) {
				for (String application : proxie.getApplications()) {
					appsListPerProject.add(application);
				}
			}
		} else {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("ProjectPlan-1001"),projectName ),"ProjectPlan-1001");
		}
		applicationsList.put("Applications", appsListPerProject);
		return applicationsList;
	}
	public Object findStatusOfProject(String projectName) throws ItorixException {

		JSONObject projectStatus = new JSONObject();
		Project particularProject = findByProjectName(projectName);
		if (particularProject != null) {
			projectStatus.put("Status", particularProject.getStatus());
			return projectStatus;
		} else {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("ProjectPlan-1001"),projectName ),"ProjectPlan-1001");
		}

	}

	public List<Contacts> findContactsOfProject(String projectName) throws ItorixException {
		Project particularProject = findByProjectName(projectName);
		if (particularProject != null) {
			List<Contacts> contacts = particularProject.getContacts();
			return contacts;
		} else {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("ProjectPlan-1001"),projectName ),"ProjectPlan-1001");
		}
	}
	
	
	public Object getOverview(int offset) throws ItorixException {
		Query query = new Query()
				.with(Sort.by(Direction.DESC, "mts")).skip(offset > 0 ? ((offset - 1) * 10) : 0)
				.limit(10);
		ProjectHistoryResponse historyResponse = new ProjectHistoryResponse();
		List<Project> allprojects =  mongoTemplate.find(query, Project.class);
		List<Project> projectDetails = new ArrayList<Project>();
		for (Project project : allprojects) {
			Project requiredProjectDetails = new Project();
			requiredProjectDetails.setName(project.getName());
			requiredProjectDetails.setStatus(project.getStatus());
			requiredProjectDetails.setInProd(project.getInProd());
			requiredProjectDetails.setCts(project.getCts());
			requiredProjectDetails.setMts(project.getMts());
			requiredProjectDetails.setCreatedUserName(project.getCreatedUserName());
			requiredProjectDetails.setModifiedUserName(project.getModifiedUserName());
			requiredProjectDetails.setContacts(project.getContacts());
			projectDetails.add(requiredProjectDetails);
		}
		if (allprojects != null) {
			Long counter = mongoTemplate.count(new Query(),Project.class);
			Pagination pagination = new Pagination();
			pagination.setOffset(offset);
			pagination.setTotal(counter);
			pagination.setPageSize(10);
			historyResponse.setPagination(pagination);
			historyResponse.setData(projectDetails);
		}
		return historyResponse;
	}
	public Object findAppsForProxyForProject(String projectName, String proxiesName) throws ItorixException {
		Set<String> appsListPerProxy = new HashSet<String>();
		JSONObject applicationsList = new JSONObject();
		Project particularProject = findByProjectName(projectName);
		if (particularProject != null) {
			for (Proxies proxie : particularProject.getProxies()) {
				if (proxiesName.equalsIgnoreCase(proxie.getName())) {
					for (String application : proxie.getApplications()) {
						appsListPerProxy.add(application);
					}
				}else{
					throw new ItorixException(ErrorCodes.errorMessage.get("ProjectPlan-1006"),"ProjectPlan-1006");
				}
			}
		} else {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("ProjectPlan-1001"),projectName ),"ProjectPlan-1001");
		}
		applicationsList.put("Applications", appsListPerProxy);
		return applicationsList;
	}
	public Object updateProject(Project project, String jsessionId) throws ItorixException {
		Project projectexists = findByProjectName(project.getName());
		if (projectexists != null) {
			UserSession userSession  = UserSession.getCurrentSessionToken();
			User dbUser = userSession.getUser();
			projectexists.setContacts(project.getContacts());
			projectexists.setProxies(project.getProxies());
			projectexists.setSharedflow(project.getSharedflow());
			projectexists.setSwaggers(project.getSwaggers());
			projectexists.setTeams(project.getTeams());
			projectexists.setDescription(project.getDescription());
			projectexists.setOrganization(project.getOrganization());
			projectexists.setTeamOwner(project.getTeamOwner());
			projectexists.setOwnerEmail(project.getOwnerEmail());
			projectexists.setInProd(project.getInProd());
			projectexists.setStatus(project.getStatus());
			projectexists.setMts(System.currentTimeMillis());
			if(dbUser != null)
				projectexists.setModifiedUserName(dbUser.getFirstName() + " " + dbUser.getLastName());
			if(project.getProxies()!=null && project.getProxies().size()>0) {
				for(Proxies proxy : project.getProxies()) {
					if(proxy.getXsdFiles()!=null) {
						proxy.setXsdFiles(updateProxyFiles(project.getName(), proxy.getName(), proxy.getXsdFiles(), "XSD"));
					}
					if(proxy.getWsdlFiles() != null) {
						proxy.setWsdlFiles(updateProxyFiles(project.getName(), proxy.getName(), proxy.getWsdlFiles(), "WSDL"));
					}
					if(proxy.getAttachments() != null) {
						proxy.setAttachments(updateProxyFiles(project.getName(), proxy.getName(), proxy.getAttachments(), "ATTACHMENT"));
					}
				}
			}
			mongoTemplate.save(projectexists);
		} else {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("ProjectPlan-1001"),project.getName() ),"ProjectPlan-1001");
		}
		return "";
	}

	public Object modifyProject(Project project) throws ItorixException {
		Project projectexists = findByProjectName(project.getName());
		if (projectexists != null) {
			UserSession userSession  = UserSession.getCurrentSessionToken();
			User dbUser = userSession.getUser();
			if(project.getContacts()!=null)
				projectexists.setContacts(project.getContacts());
			if(project.getProxies()!=null)
				projectexists.setProxies(updateProxies(project.getProxies(), projectexists.getProxies()));
			if(project.getSharedflow()!=null)
				projectexists.setSharedflow(project.getSharedflow());
			if(project.getSwaggers()!=null)
				projectexists.setSwaggers(project.getSwaggers());
			if(project.getSwaggers()!=null)
				projectexists.setTeams(project.getTeams());
			if(project.getDescription()!=null)
				projectexists.setDescription(project.getDescription());
			if(project.getOrganization()!=null)
				projectexists.setOrganization(project.getOrganization());
			if(project.getDescription()!=null)
				projectexists.setTeamOwner(project.getTeamOwner());
			if(project.getTeamOwner()!=null)
				projectexists.setOwnerEmail(project.getOwnerEmail());
			if(project.getInProd()!=null)
				projectexists.setInProd(project.getInProd());
			if(project.getStatus()!=null)
				projectexists.setStatus(project.getStatus());
			projectexists.setMts(System.currentTimeMillis());
			if(dbUser != null)
				projectexists.setModifiedUserName(dbUser.getFirstName() + " " + dbUser.getLastName());
			mongoTemplate.save(projectexists);
		} else {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("ProjectPlan-1001"),project.getName() ),"ProjectPlan-1001");
		}
		return "";
	}

	private List<Proxies> updateProxies(List<Proxies> proxies, List<Proxies> projectProxies) {
		Proxies newProxy = proxies.get(0);
		for(Proxies proxy : projectProxies)
		{
			if(newProxy.getBasePath()!=null)
				proxy.setBasePath(newProxy.getBasePath());
			if(newProxy.getInterfaceType()!=null)
				proxy.setInterfaceType(newProxy.getInterfaceType());
			if(newProxy.getRepoName()!=null)
				proxy.setRepoName(newProxy.getRepoName());
			if(newProxy.getTwoWaySSL()!=null)
				proxy.setTwoWaySSL(newProxy.getTwoWaySSL());
			if(newProxy.getApigeeVirtualHosts()!=null)
				proxy.setApigeeVirtualHosts(newProxy.getApigeeVirtualHosts());
			if(newProxy.getPolicyTemplates()!=null)
				proxy.setPolicyTemplates(newProxy.getPolicyTemplates());
			if(newProxy.getProjectMetaData()!=null)
				proxy.setProjectMetaData(newProxy.getProjectMetaData());
			if(newProxy.getPipelines()!=null)
				proxy.setPipelines(newProxy.getPipelines());
		}
		return projectProxies;
	}

	//	private List<Proxies> updateProxies(List<Proxies> proxies){
	//		return proxies;
	//	}

	public Object deleteProject(String projectName, String jsessionid) throws ItorixException {

		Project projectexists = findByProjectName(projectName);
		if (projectexists != null) {
			mongoTemplate.remove(new Query(Criteria.where(Project.LABEL_NAME).in(projectName)), Project.class);
		} else {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("ProjectPlan-1001"),projectName ),"ProjectPlan-1001");
		}
		return "";
	}
	public ObjectNode getProjectStats(String timeunit,String timerange) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		String[] dates=timerange.split("~");

		Date startDate=null;
		Date endDate=null;
		Date orgStartDateinit=null;
		Date origEnddate=null;
		if(dates!=null && dates.length>0){
			startDate= dateFormat.parse(dates[0]);
			endDate= dateFormat.parse(dates[1]);
		}
		orgStartDateinit=startDate;
		origEnddate=endDate;
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		ObjectNode metricsNode = mapper.createObjectNode();
		metricsNode.put("name", timeunit);
		ArrayNode valuesNode = mapper.createArrayNode();
		ArrayNode projectsNode = mapper.createArrayNode();

		while(startDate.compareTo(endDate)<=0){
			Query query = new Query();
			query.addCriteria(Criteria.where(Project.LABEL_CREATED_TIME).
					gte(new Long(DateUtil.getStartOfDay(startDate).getTime() + "")).lt(new Long(DateUtil.getEndOfDay(startDate).getTime() + "")));
			List<Project> list = baseRepository.find(query, Project.class);
			//if(list!=null && list.size()>0){
			ObjectNode valueNode = mapper.createObjectNode();
			valueNode.put("timestamp", DateUtil.getStartOfDay(startDate).getTime() + "");
			valueNode.put("value", list.size());
			valuesNode.add(valueNode);
			//}
			startDate = DateUtil.addDays(startDate, 1);
		}
		metricsNode.set("values", valuesNode);
		ObjectNode statsNode =mapper.createObjectNode();
		List<Project> list = baseRepository.findAll(Project.class);

		for (Project project : list) {
			ObjectNode projectNode = mapper.createObjectNode();
			ObjectNode dimesionNode = mapper.createObjectNode();
			projectNode.put("name",project.getName());
			projectNode.put("status",project.getStatus());
			projectNode.put("inProduction",project.getInProd());

			int proxiesOrSharedFlow=0;

			if(project.getProxies()!=null && project.getProxies().size()>0){
				proxiesOrSharedFlow +=project.getProxies().size();
			}
			if(project.getSharedflow()!=null && project.getSharedflow().size()>0 ){
				proxiesOrSharedFlow +=project.getSharedflow().size();
			}
			dimesionNode.put("proxies", proxiesOrSharedFlow);
			ArrayNode proxiesNames = mapper.createArrayNode();
			ArrayNode applicationNames = mapper.createArrayNode();
			ArrayNode productNames = mapper.createArrayNode();
			ArrayNode developerNames = mapper.createArrayNode();

			int applications=0;
			int products=0;
			int developers=0;
			if(project.getProxies()!=null){
				for (Proxies proxie : project.getProxies()) {
					applications += proxie.getApplications() != null ? proxie.getApplications().size() : 0;
					products += proxie.getProducts() != null ? proxie.getProducts().size() : 0;
					developers+=proxie.getDevelopers() != null ? proxie.getDevelopers().size() : 0;

					proxiesNames.add(proxie.getName());
					if(proxie.getApplications() !=null)
						for (String application: proxie.getApplications()) {
							applicationNames.add(application);
						}
					if(proxie.getProducts() != null)
						for (String product: proxie.getProducts()) {
							productNames.add(product);
						}
					if(proxie.getDevelopers() != null )
						for (String developer: proxie.getDevelopers()) {
							developerNames.add(developer);
						}
				} 
			}
			dimesionNode.put("proxiesNames",proxiesNames);
			dimesionNode.put("applications", applications);
			dimesionNode.put("applicationNames",applicationNames);
			dimesionNode.put("products", products);
			dimesionNode.put("productNames",productNames);
			dimesionNode.put("developers", developers);
			dimesionNode.put("developerNames",developerNames);
			projectNode.put("dimensions", dimesionNode);
			projectsNode.add(projectNode);
		}
		List<String> distinctList=baseRepository.findDistinctValuesByColumnName(Project.class,"status");
		if(distinctList!=null && distinctList.size()>0){
			for(String status:distinctList){
				Query query = new Query();
				query.addCriteria(Criteria.where("status").is(status));
				List<Project> projectListByStatus = baseRepository.find(query, Project.class);
				ObjectNode statNode = mapper.createObjectNode();
				statsNode.put(status.toLowerCase(), projectListByStatus.size());
			}
		}
		statsNode.put("projects",projectsNode);
		rootNode.set("metrics", metricsNode);
		rootNode.set("stats", statsNode);
		return rootNode;
	}

	public boolean uploadProjectFile(MultipartFile file, ProjectFile projectFile) {
		String fileName= UUID.randomUUID().toString();
		try {
			InputStream inStream = file.getInputStream();
			insertFile(inStream,fileName);
			projectFile.setLocation(fileName);
			projectFile.setInUse(false);
			updateProjectFile(projectFile);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}


	public ProjectFile findProjectFile(ProjectFile projectFile) {
		Query query = new Query(Criteria.where("projectName").is(projectFile.getProjectName())
				.and("proxyName").is(projectFile.getProxyName())
				.and("type").is(projectFile.getType())
				.and("name").is(projectFile.getFileName()));
		ProjectFile file = mongoTemplate.findOne(query, ProjectFile.class);
		return file;
	}

	public void updateProjectFileInUse(ProjectFile projectFile) {
		Query query = new Query(Criteria.where("projectName").is(projectFile.getProjectName())
				.and("proxyName").is(projectFile.getProxyName())
				.and("type").is(projectFile.getType())
				.and("fileName").is(projectFile.getFileName()));
		mongoTemplate.findOne(query, ProjectFile.class);
		ProjectFile file = mongoTemplate.findOne(query, ProjectFile.class);
		if(file != null) {
			file.setInUse(true);
			mongoTemplate.save(file);
		}
	}

	public List<ProjectFile> findProjectFiles(String projectName, String proxyName, String type) {
		Query query = new Query(Criteria.where("projectName").is(projectName)
				.and("proxyName").is(proxyName)
				.and("type").is(type));
		List<ProjectFile> files = mongoTemplate.find(query, ProjectFile.class);
		return files;
	}

	public List<ProjectFile> updateProxyFiles(String projectName, String proxyName, List<ProjectFile> files, String type) {
		List<ProjectFile> dbFiles = this.findProjectFiles(projectName, proxyName, type);
		for(ProjectFile dbFile: dbFiles) {
			boolean found =false;
			for(ProjectFile file : files) {
				String fileLocation = applicationProperties.getAppUrl()+ "/" + applicationProperties.getAppDomain() + "/" + "v1/projects/"
						+ projectName + "/proxy/" + proxyName + "/" + type.toLowerCase() + "/" + file.getFileName();
				file.setFileLocation(fileLocation);
				if(file.getFileName().equals(file.getFileName())) 
					found=true;	
			}
			if(!found)
				deleteProjectFile(dbFile);
		}
		return files;
	}

	public boolean updateProjectFile(ProjectFile projectFile) {
		Query query = new Query(Criteria.where("projectName").is(projectFile.getProjectName())
				.and("proxyName").is(projectFile.getProxyName())
				.and("type").is(projectFile.getType())
				.and("fileName").is(projectFile.getFileName()));
		ProjectFile file = mongoTemplate.findOne(query, ProjectFile.class);
		if(file != null) {
			deleteFile(file.getLocation());
			file.setFileLocation(projectFile.getFileLocation());
			file.setProjectName(projectFile.getProjectName());
			file.setProxyName(projectFile.getProxyName());
			file.setFileName(projectFile.getFileName());
			file.setType(projectFile.getType());
			file.setLocation(projectFile.getLocation());
			file.setInUse(projectFile.isInUse());
			mongoTemplate.save(file);
		}
		else {
			mongoTemplate.save(projectFile);
		}
		return true;
	}

	public void deleteProjectFile(ProjectFile projectFile) {
		Query query = new Query(Criteria.where("projectName").is(projectFile.getProjectName())
				.and("proxyName").is(projectFile.getProxyName())
				.and("type").is(projectFile.getType())
				.and("fileName").is(projectFile.getFileName()));
		ProjectFile file = mongoTemplate.findOne(query, ProjectFile.class);
		if(file != null) {
			deleteFile(file.getLocation());
			mongoTemplate.remove(file);
		}
	}

	public String readProjectFile(ProjectFile projectFile) {
		Query query = new Query(Criteria.where("projectName").is(projectFile.getProjectName())
				.and("proxyName").is(projectFile.getProxyName())
				.and("type").is(projectFile.getType())
				.and("fileName").is(projectFile.getFileName()));
		mongoTemplate.findOne(query, ProjectFile.class);
		ProjectFile file = mongoTemplate.findOne(query, ProjectFile.class);
		if(file != null) {
			return getFile(file.getLocation());
		}
		return null;
	}

	private String getFile(String fileName){
		String reader =null;
		//DB db = mongoTemplate.getDb();
		DB db = mongoTemplate.getMongoDbFactory().getLegacyDb();
		try {
			GridFS gfs = new GridFS(db, "Project.Files");
			GridFSDBFile file = gfs.findOne(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
			StringBuilder stringBuilder = new StringBuilder();
			String ls = System.getProperty("line.separator");
			String line= null;
			while( ( line = br.readLine() ) != null ) {
				stringBuilder.append( line );
				stringBuilder.append( ls );
			}
			br.close();
			reader = stringBuilder.toString();
		} catch (MongoException e) {
			e.printStackTrace();
			logger.error("getFile "+ fileName+" : " +e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("getFile "+ fileName +" : "+ e.getMessage());
		}
		return reader;
	}

	private void insertFile(InputStream inStream, String fileName) throws IOException{
		try {
			DB db = mongoTemplate.getMongoDbFactory().getLegacyDb();
			GridFS gfs = new GridFS(db, "Project.Files");
			GridFSDBFile dbFile = gfs.findOne(fileName);
			if(dbFile!=null)
				gfs.remove(fileName);
			GridFSInputFile gfsFile = gfs.createFile(inStream);
			gfsFile.setFilename(fileName);
			gfsFile.save();
		} catch (MongoException e) {
			e.printStackTrace();
			logger.error("insertFile "+e.getMessage());
			throw e;
		}
	}

	private boolean deleteFile(String fileName){
		try {
			GridFS gfs = new GridFS( mongoTemplate.getMongoDbFactory().getLegacyDb(), "Project.Files");
			GridFSDBFile dbFile = gfs.findOne(fileName);
			if(dbFile!=null)
				gfs.remove(fileName);
			return true;
		} catch (MongoException e) {
			e.printStackTrace();
			logger.error("insertFile "+e.getMessage());
			throw e;
		}
	}

	public ServiceRegistry getServiceRegistry(String projectName, String proxyName, String registryName) {
		Project project = findByProjectName(projectName);
		if(project.getProxies()!=null)
			for(Proxies proxy : project.getProxies())
				if(proxy.getName().equals(proxyName))
					if(proxy.getServiceRegistries()!=null)
						for(ServiceRegistry registry : proxy.getServiceRegistries())
							if(registry.getName().equals(registryName))
								return registry;

		return null;
	}

	public List<ServiceRegistry> getServiceRegistry(String projectName, String proxyName) {
		List<ServiceRegistry> serviceRegisties = new ArrayList<ServiceRegistry>();
		Project project = findByProjectName(projectName);
		if(project.getProxies()!=null)
			for(Proxies proxy : project.getProxies())
				if(proxy.getName().equals(proxyName))
					if(proxy.getServiceRegistries()!=null)
						serviceRegisties =  proxy.getServiceRegistries();

		return serviceRegisties;
	}

	public void createUpdateServiceRegistry(String projectName, String proxyName, ServiceRegistry serviceRegistry) {
		if(getServiceRegistry(projectName,proxyName, serviceRegistry.getName())!=null) {
			Project project = findByProjectName(projectName);
			if(project.getProxies()!=null)
				for(Proxies proxy : project.getProxies())
					if(proxy.getName().equals(proxyName))
						if(proxy.getServiceRegistries()!=null)
							for(ServiceRegistry registry : proxy.getServiceRegistries())
								if(registry.getName().equals(serviceRegistry.getName()))
									registry.setEndpoints(serviceRegistry.getEndpoints());
			mongoTemplate.save(project);
		}
		else {
			Project project = findByProjectName(projectName);
			if(project.getProxies()!=null)
				for(Proxies proxy : project.getProxies())
					if(proxy.getName().equals(proxyName)){
						List<ServiceRegistry> serviceRegistries = proxy.getServiceRegistries() == null ? new ArrayList<ServiceRegistry>(): proxy.getServiceRegistries();
						serviceRegistries.add(serviceRegistry);
						proxy.setServiceRegistries(serviceRegistries);
					}
			mongoTemplate.save(project);
		}
	}

	public void deleteServiceRegistry(String projectName, String proxyName, String registryName) {
		ServiceRegistry deleteregistry = getServiceRegistry(projectName,proxyName, registryName);
		if(deleteregistry != null) {
			Project project = findByProjectName(projectName);
			if(project.getProxies()!=null)
				for(Proxies proxy : project.getProxies())
					if(proxy.getName().equals(proxyName))
						if(proxy.getServiceRegistries()!=null) {
							for(ServiceRegistry registry : proxy.getServiceRegistries())
								if(registry.getName().equals(registryName))
									deleteregistry =registry;
							List <ServiceRegistry> registryList = proxy.getServiceRegistries();
							registryList.remove(deleteregistry);
							proxy.setServiceRegistries(registryList);
						}
			mongoTemplate.save(project);						
		}
	}

	public List<Category> getCategories() throws ItorixException{
		try{
			List<Category> listCategory = new ArrayList<Category>();
			List<com.itorix.apiwiz.common.model.proxystudio.Category> categories =  mongoTemplate.findAll(com.itorix.apiwiz.common.model.proxystudio.Category.class); 
			ObjectMapper mapper = new ObjectMapper();
			for(com.itorix.apiwiz.common.model.proxystudio.Category devCategory : categories) {
				String stringCategory = mapper.writeValueAsString(devCategory);
				Category category= mapper.readValue(stringCategory, Category.class);
				listCategory.add(category);
			}
			return listCategory;
		}
		catch(Exception ex){
			throw new ItorixException(ex.getMessage(),"ProjectPlan-1001", ex );
		}
	}

	public Map<String, String> loadExcelData(MultipartFile file, String jsessionId) throws IOException, ItorixException, InvalidFormatException {
		String excelFile = applicationProperties.getTempDir() + file.getOriginalFilename();
		File targetFile = new File(excelFile);
		file.transferTo(targetFile);
		ExcelReader excelReader = new ExcelReader();
		Map<String, String> loadedProjects = new HashMap<String, String>();
		for(Map<String,String> map : excelReader.readExcelData(excelFile)) {
			try {
				Project project = excelReader.populateProject( map );
				if(project !=null) {
					project.getProxies().get(0).setPolicyTemplates(excelReader.populatePolicyTemplates(map,getCategories()));
					if(findByProjectName(project.getName()) == null ) {
						createNewProject(project,jsessionId);
						loadedProjects.put(project.getName(), "new project created");
					}
					else {
						this.modifyProject(project);
						loadedProjects.put(project.getName(), "project updated");
					}
				}
			}catch(ItorixException ex) {
				loadedProjects.put(map.get("Service_Name"), "error creating project");
			}
		}
		FileUtils.delete(targetFile);
		return loadedProjects;
	}


	public void loadProxyData(MultipartFile file, String projectName , String jsessionId) {
		String zipFileName= FilenameUtils.getBaseName(file.getOriginalFilename());
		String zipLocation = applicationProperties.getTempDir()+ UUID.randomUUID().toString() + File.separatorChar +"unzip" + File.separatorChar;
		String zipFile = applicationProperties.getTempDir() + file.getOriginalFilename();
		ZIPUtil unZip = new ZIPUtil();
		try {
			Project project = findByProjectName(projectName);
			if(project != null) {
				File targetFile = new File(zipFile);
				file.transferTo(targetFile);
				unZip.unzip(zipFile, zipLocation);
				FileUtils.delete(targetFile);
				ExcelReader excelReader = new ExcelReader();
				project = excelReader.readProxyData(project, zipLocation + zipFileName + File.separatorChar );
				project = updatePipelines(project, zipLocation + zipFileName + File.separatorChar + "pipeline.json" );
				File[] attachments = new File(zipLocation + zipFileName + File.separatorChar + "attachments" +  File.separatorChar).listFiles();
				for (File attachment : attachments){
					String ext = FilenameUtils.getExtension(attachment.getName()).toUpperCase();
					ProjectFile projectFile = new  ProjectFile();
					projectFile.setFileName(attachment.getName());
					projectFile.setProjectName(projectName);
					projectFile.setProxyName(project.getProxies().get(0).getName());
					InputStream targetStream = new DataInputStream(new FileInputStream(attachment));
					String fileName= UUID.randomUUID().toString();
					insertFile(targetStream,fileName);
					projectFile.setLocation(fileName);
					projectFile.setInUse(true);
					switch (ext) {
					case "WSDL":
						projectFile.setType("WSDL");
						break;
					case "XSD":
						projectFile.setType("XSD");
						break;
					default : 
						projectFile.setType("ATTACHMENT");
					}
					updateProjectFile(projectFile);
				}
				updateProject(project, jsessionId);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		try {
			org.apache.commons.io.FileUtils.deleteDirectory(new File(zipLocation));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Project updatePipelines(Project project, String file) {
		ObjectMapper mapper = new ObjectMapper();
		String pipelineStr;
		try {
			pipelineStr = org.apache.commons.io.FileUtils.readFileToString(new File(file));
			Pipelines pipelines = mapper.readValue(pipelineStr, Pipelines.class);
			project.getProxies().get(0).setPipelines(pipelines.getPipelines());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return project;
	}

	public ProjectProxyResponse generateProxy(String projectName, String proxyName, String jsessionId) {
		ProjectProxyResponse response = new ProjectProxyResponse();
		try {

			Project project = findByProjectName(projectName);
			CodeGenHistory proxyGen = populateProxyGenerationObj(project);
			response.setGitRepoName(proxyGen.getProxySCMDetails().getReponame());
			response.setGitBranch(proxyGen.getProxySCMDetails().getBranch());
			project.getProxies().get(0).setRepoName(response.getGitRepoName());
			updateProject(project, jsessionId);
			String folderPath = applicationProperties.getTempDir() + "proxyGeneration";
			org.apache.commons.io.FileUtils.forceMkdir(new File(folderPath));
			Operations operations = new Operations();
			operations.setDir(folderPath);
			operations.setjSessionid(jsessionId);
			User user = commonServices.getUserDetailsFromSessionID(jsessionId);
			operations.setUser(user);
			if(project.getProxyByName(proxyName).getDefaultVirtualHosts()){
				PolicyMapping mapping = policyMappingDAO.getPolicyMapping("virtualHost.default");
				if(mapping != null) 
					if(project.getProxyByName(proxyName).getApigeeVirtualHosts() != null)
						project.getProxyByName(proxyName).getApigeeVirtualHosts().addAll(Arrays.asList(mapping.getValue().split(",")));
					else {
						Set<String> Vhosts=  new HashSet<String>(Arrays.asList(mapping.getValue().split(",")));
						project.getProxyByName(proxyName).setApigeeVirtualHosts(Vhosts);
					}
			}
			codeGenService.processCodeGen(proxyGen, operations, project);
			org.apache.commons.io.FileUtils.deleteDirectory(new File(folderPath));
			response.setGitPush("true");
			String pipelineName = createPipeline(project, proxyGen, jsessionId);
			response.setPipelineName(pipelineName);
			projectBusinessImpl.createServiceConfigs(projectName, proxyName, getBranchType(proxyGen.getProxySCMDetails().getBranch()), jsessionId);
			response.setConfigKVM("true");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}


	private String createPipeline(Project project, CodeGenHistory proxyGen, String jsessionId) throws ItorixException {
		PipelineGroups pipelineGroups = new PipelineGroups();
		pipelineGroups.setProjectName(project.getName().replaceAll("\\.", ""));
		if(project.getContacts() !=null) {
			List<String> notifications = new ArrayList<String>();
			for(Contacts contact : project.getContacts())
				notifications.add(contact.getEmail());
			pipelineGroups.setNotifications(notifications);
		}
		com.itorix.apiwiz.projectmanagement.model.cicd.Pipeline pipeline = new com.itorix.apiwiz.projectmanagement.model.cicd.Pipeline();
		pipeline.setDisplayName(proxyGen.getProxy().getName().replaceAll("\\.", "").trim()+"_"
				+ proxyGen.getProxySCMDetails().getBranch());
		pipeline.setProxyName(proxyGen.getProxy().getName().replaceAll("\\.", "").trim());
		pipeline.setType("proxies");
		pipeline.setProjectName(project.getName());
		pipeline.setMaterials(populateMaterials(proxyGen.getProxySCMDetails()));
		pipeline.setStages(populateStages(project, getBranchType(proxyGen.getProxySCMDetails().getBranch())));
		pipeline.setVersion(proxyGen.getProxy().getVersion().replaceAll("\\.", "").trim());
		List<com.itorix.apiwiz.projectmanagement.model.cicd.Pipeline> pipelines = new ArrayList<com.itorix.apiwiz.projectmanagement.model.cicd.Pipeline>();
		pipelines.add(pipeline);
		pipelineGroups.setPipelines(pipelines);
		String branch = pipeline.getMaterials().get(0).getScmBranch();
		if(!isPipelineExists(project.getName().replaceAll(" " , "-").replaceAll("\\." , ""), branch, jsessionId))
			createPipeline(pipelineGroups, jsessionId);
		return project.getName().replaceAll(" " , "-").replaceAll("\\." , "")+ "_" + 
		proxyGen.getProxy().getName().replaceAll("\\.", "").trim() + "_" +
		proxyGen.getProxy().getVersion().replaceAll("\\.", "").trim() + "_" +
		proxyGen.getProxySCMDetails().getBranch();
	}

	private String getBranchType(String branchName) {
		if(branchName.contains("feature"))
			return "feature";
		if(branchName.contains("master"))
			return "master";
		if(branchName.contains("release"))
			return "release";
		return null;
	}

	private void createPipeline(PipelineGroups pipelineGroups, String jsessionId) throws ItorixException{
		try {
			String hostUrl = "http://localhost:#port#/#context#/v1/pipelines";
			hostUrl = hostUrl.replaceAll("#port#", port);
			hostUrl = hostUrl.replaceAll("#context#", context.replaceAll("/", ""));
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("JSESSIONID", jsessionId);
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<PipelineGroups> requestEntity = new HttpEntity<>(pipelineGroups, headers);
			//			ResponseEntity<String> response  = 
			restTemplate.exchange(hostUrl, HttpMethod.POST, requestEntity, String.class);
		}catch(Exception e){
			throw new ItorixException("error creating pipeline","",e);
		}
	}

	private boolean isPipelineExists(String projectName, String branchName, String jsessionid) throws ItorixException {
		PipelineGroups pipelineGroup = getPipelines(projectName, jsessionid);
		if(pipelineGroup !=null)
			for(com.itorix.apiwiz.projectmanagement.model.cicd.Pipeline pipeline: pipelineGroup.getPipelines()) {
				if(pipeline.getName().contains(branchName))
					return true;
			}
		return false;
	}

	private PipelineGroups getPipelines(String projectName, String jsessionId) throws ItorixException{
		try {
			String hostUrl = "http://localhost:#port#/#context#/v1/pipelines/" + projectName;
			hostUrl = hostUrl.replaceAll("#port#", port);
			hostUrl = hostUrl.replaceAll("#context#", context.replaceAll("/", ""));
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("JSESSIONID", jsessionId);
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
			ResponseEntity<PipelineGroups> response  = 
					restTemplate.exchange(hostUrl, HttpMethod.GET, requestEntity, PipelineGroups.class);
			return response.getBody();
		}catch(Exception e){
			return null;
		}
	}

	private List<com.itorix.apiwiz.projectmanagement.model.cicd.Stage> populateStages(Project project , String branchtype) {
		List<com.itorix.apiwiz.projectmanagement.model.cicd.Stage> stages = new ArrayList<com.itorix.apiwiz.projectmanagement.model.cicd.Stage>();
		for(Pipeline pipeline : project.getProxies().get(0).getPipelines())
			if(pipeline.getBranchType().equals(branchtype))
				for(Stage stage : pipeline.getStages()) {
					com.itorix.apiwiz.projectmanagement.model.cicd.Stage pipelienStage = new com.itorix.apiwiz.projectmanagement.model.cicd.Stage();
					pipelienStage.setName(stage.getName().trim());
					pipelienStage.setType(stage.getType().trim());
					pipelienStage.setOrgName(stage.getOrgName().trim());
					pipelienStage.setEnvName(stage.getEnvName().trim());
					pipelienStage.setIsSaas(stage.getIsSaaS().equalsIgnoreCase("true")?true:false);
					pipelienStage.setSequenceID(stage.getSequenceID());
					UnitTests unitTests = new UnitTests();
					unitTests.setEnabled(stage.getUnitTests().getEnabled());
					unitTests.setAcceptance(stage.getUnitTests().getAcceptance());
					unitTests.setArtifactType(stage.getUnitTests().getArtifactType());
					if( stage.getUnitTests().getTestsuites() != null ) {
						List<TestSuiteAndConfig> testSuites = new ArrayList<TestSuiteAndConfig>();
						TestSuiteAndConfig testSuiteAndConfig = new TestSuiteAndConfig();
						testSuites.add(testSuiteAndConfig);
						testSuiteAndConfig.setTestSuiteId(stage.getUnitTests().getTestsuites().get(0).getTestSuiteId());
						testSuiteAndConfig.setEnvironmentId(stage.getUnitTests().getTestsuites().get(0).getEnvironmentId());
						unitTests.setTestSuites(testSuites);
					}
					pipelienStage.setUnitTests(unitTests);
					CodeCoverage codecoverage = new CodeCoverage();
					codecoverage.setEnabled(stage.getCodeCoverage().getEnabled());
					codecoverage.setAcceptance(stage.getCodeCoverage().getAcceptance());
					codecoverage.setArtifactType(stage.getCodeCoverage().getArtifactType());
					if( stage.getCodeCoverage().getTestsuites() != null ) {
						List<TestSuiteAndConfig> testSuites = new ArrayList<TestSuiteAndConfig>();
						TestSuiteAndConfig testSuiteAndConfig = new TestSuiteAndConfig();
						testSuites.add(testSuiteAndConfig);
						testSuiteAndConfig.setTestSuiteId(stage.getCodeCoverage().getTestsuites().get(0).getTestSuiteId());
						testSuiteAndConfig.setEnvironmentId(stage.getCodeCoverage().getTestsuites().get(0).getEnvironmentId());
						codecoverage.setTestSuites(testSuites);
					}
					pipelienStage.setCodeCoverage(codecoverage);
					stages.add(pipelienStage);
				}
		return stages;
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

	private CodeGenHistory populateProxyGenerationObj(Project project ) throws ItorixException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			Proxies projectProxy = project.getProxies().get(0);
			List<com.itorix.apiwiz.common.model.proxystudio.Category> listCategory = new ArrayList<com.itorix.apiwiz.common.model.proxystudio.Category>();
			List<Category> categories =  projectProxy.getPolicyTemplates(); 
			for(Category devCategory : categories) {
				String stringCategory;
				stringCategory = mapper.writeValueAsString(devCategory);
				com.itorix.apiwiz.common.model.proxystudio.Category category= mapper.readValue(stringCategory, com.itorix.apiwiz.common.model.proxystudio.Category.class);
				listCategory.add(category);
			}
			CodeGenHistory codeGenHistory = new CodeGenHistory();
			//codeGenHistory.setPolicyTemplates(getTemplates());//remove this line
			List<Category> policyTemplates = project.getProxies().get(0).getPolicyTemplates();
			codeGenHistory.setPolicyTemplates(getTemplates(policyTemplates));
			codeGenHistory.setProxy(populateProxy(project.getProxies().get(0)));
			codeGenHistory.setProxySCMDetails(populateProxySCMDetails(project));
			return codeGenHistory;
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ItorixException("unable to create repo ","",e);
		} catch (ItorixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ItorixException("unable to create repo ","",e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private String createSCMRepo(Project project) throws ItorixException {
		String repoName = "apigee-"+project.getProxies().get(0).getName();
		try {
			scmUtilImpl.createRepository(repoName, 
					"Created from Itorix platform", 
					"https://api.github.com/user/repos", 
					applicationProperties.getProxyScmUserName(),
					applicationProperties.getProxyScmPassword());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return repoName;
	}

	private void createSCMBranch(ProxySCMDetails proxySCMDetails) throws ItorixException {
		scmUtilImpl.createBranch(proxySCMDetails.getBranch(), "", proxySCMDetails.getHostUrl(), proxySCMDetails.getUsername(), applicationProperties.getProxyScmPassword());
	}

	private ProxySCMDetails populateProxySCMDetails(Project project) throws ItorixException {
		String repoName = createSCMRepo(project);
		String branch = "feature-" + System.currentTimeMillis();
		ProxySCMDetails proxySCMDetails = new ProxySCMDetails();
		proxySCMDetails.setReponame(repoName);
		proxySCMDetails.setUsername(applicationProperties.getProxyScmUserName());
		proxySCMDetails.setPassword(applicationProperties.getProxyScmEncryptedPassword());
		proxySCMDetails.setBranch(branch);
		proxySCMDetails.setScmSource("GIT");
		proxySCMDetails.setHostUrl(GIT_HOST_URL + repoName);
		createSCMBranch(proxySCMDetails);
		return proxySCMDetails;
	}

	private Proxy populateProxy(Proxies projectProxy) {
		String proxyBuildArtifact ="";
		if(projectProxy.getWsdlFiles() != null && projectProxy.getWsdlFiles().size() > 0)
			for (ProjectFile file : projectProxy.getWsdlFiles())
				proxyBuildArtifact = proxyBuildArtifact + file.getFileName() + "  ";
		Proxy proxy = new Proxy();
		String path = String.join(",", projectProxy.getBasePath());
		proxy.setBasePath(path);
		proxy.setName(projectProxy.getName());
		proxy.setDescription(projectProxy.getName());
		proxy.setVersion(projectProxy.getVersion());
		proxy.setBuildProxyArtifactType("WSDL");
		proxy.setBuildProxyArtifact(proxyBuildArtifact);
		proxy.setBranchType("feature");

		return proxy;
	}

	private List<com.itorix.apiwiz.common.model.proxystudio.Category> getTemplates(List<Category> policyTemplates) {
		ObjectMapper mapper = new ObjectMapper();
		//String templateStr = "{\"policyTemplates\":[{\"type\":\"trafficmanagement\",\"name\":\"TrafficManagement\",\"description\":\"Traffic management policies let you configure cache, control traffic quotas and spikes, set concurrent rate limits, and so on.\",\"policies\":[{\"name\":\"spikearrest\",\"displayName\":\"SpikeArrest\",\"description\":\"Spike Arrest policy protects against traffic spikes.\",\"enabled\":true},{\"name\":\"quotaatproductlevel\",\"displayName\":\"Quota at Productlevel\",\"description\":\"Quota policy to configure the number of request messages that an API proxy allows over a period of time.\",\"enabled\":false},{\"name\":\"responsecache\",\"displayName\":\"Response Cache\",\"description\":\"Caches data from a backend resource, reducing the number of requests to the resource.\",\"enabled\":false}]},{\"type\":\"security\",\"name\":\"Security\",\"description\":\"Security policies let you control access to your APIs with OAuth, API key validation, and other threat protection features.\",\"policies\":[{\"name\":\"oauth\",\"displayName\":\"OAuth\",\"description\":\"Secure APIs with OAuth\",\"enabled\":false},{\"name\":\"cors\",\"displayName\":\"CORS\",\"description\":\"Enable API's to support CORS requests\",\"enabled\":false},{\"name\":\"accesscontrol\",\"displayName\":\"Access Control\",\"description\":\"Lets you allow or deny access to your APIs by specific IP addresses. \",\"enabled\":false},{\"name\":\"verifyapikey\",\"displayName\":\"Verify API Key\",\"description\":\"Lets you enforce verification of API keys at runtime.\",\"enabled\":false},{\"name\":\"jwt\",\"displayName\":\"JWT\",\"description\":\"Lets you enforce JWT verification for your proxy.\",\"enabled\":false}]},{\"type\":\"logging\",\"name\":\"Logging\",\"description\":\"Logging policies allow you to log the request data and response data.\",\"policies\":[{\"name\":\"log\",\"displayName\":\"Logger\",\"description\":\"Lets you to log the proxy data during runtime.\",\"enabled\":false}]},{\"type\":\"mediation\",\"name\":\"Mediation\",\"description\":\"Mediation policies let you perform message transformation, parsing, and validation, as well as raise faults and alerts.\",\"policies\":[{\"name\":\"jsontoxml\",\"displayName\":\"JSON To XML\",\"description\":\"Converts messages from the JavaScript Object Notation (JSON) format to extensible markup language (XML) \",\"enabled\":false},{\"name\":\"xmltojson\",\"displayName\":\"XML To JSON\",\"description\":\"Converts messages from the extensible markup language (XML) fromat to JavaScript Object Notation (JSON).\",\"enabled\":false}]},{\"type\":\"threatprotection\",\"name\":\"ThreatManagement\",\"description\":\"ThreatManagement allow you to scan the incoming payloads for any miscellaneous content in the request.\",\"policies\":[{\"name\":\"jsoninjection\",\"displayName\":\"JSON Injection\",\"description\":\"Enable payload scan for JSON content\",\"enabled\":false},{\"name\":\"xssinjection\",\"displayName\":\"XSS Injection\",\"description\":\"Scans XSS threats in payloads\",\"enabled\":false},{\"name\":\"xmlinjection\",\"displayName\":\"XML Injection\",\"description\":\"Enable payload scan for XML content\",\"enabled\":false}]}]}";
		List<com.itorix.apiwiz.common.model.proxystudio.Category> listCategory = new ArrayList<com.itorix.apiwiz.common.model.proxystudio.Category>();
		try {
			for(Category devCategory : policyTemplates) {
				String stringCategory;
				stringCategory = mapper.writeValueAsString(devCategory);
				com.itorix.apiwiz.common.model.proxystudio.Category category= mapper.readValue(stringCategory, com.itorix.apiwiz.common.model.proxystudio.Category.class);
				listCategory.add(category);
			}
			return listCategory;
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void createPromotePipeline(ScmPromote scmPromote, String projectName, String proxyName, String jsessionid) throws ItorixException {
		Project project = findByProjectName(projectName);
		PipelineGroups pipelineGroups = new PipelineGroups();
		pipelineGroups.setProjectName(project.getName().replaceAll("\\.", ""));
		if(project.getContacts() !=null) {
			List<String> notifications = new ArrayList<String>();
			for(Contacts contact : project.getContacts())
				notifications.add(contact.getEmail());
			pipelineGroups.setNotifications(notifications);
		}
		com.itorix.apiwiz.projectmanagement.model.cicd.Pipeline pipeline = new com.itorix.apiwiz.projectmanagement.model.cicd.Pipeline();
		pipeline.setDisplayName(proxyName.replaceAll("\\.", "").trim()+ "_" + scmPromote.getTargetBranch());
		pipeline.setProxyName(proxyName.replaceAll("\\.", "").trim());
		pipeline.setType("proxies");
		pipeline.setProjectName(project.getName());
		ProxySCMDetails proxySCMDetails = new ProxySCMDetails();
		proxySCMDetails.setBranch(scmPromote.getTargetBranch());
		proxySCMDetails.setHostUrl(GIT_HOST_URL + scmPromote.getRepoName());
		proxySCMDetails.setReponame(scmPromote.getRepoName());
		proxySCMDetails.setScmSource("GIT");

		pipeline.setMaterials(populateMaterials(proxySCMDetails));
		pipeline.setStages(populateStages(project , getBranchType(scmPromote.getTargetBranch())));
		pipeline.setVersion(project.getProxies().get(0).getVersion().replaceAll("\\.", "").trim());
		List<com.itorix.apiwiz.projectmanagement.model.cicd.Pipeline> pipelines = new ArrayList<com.itorix.apiwiz.projectmanagement.model.cicd.Pipeline>();
		pipelines.add(pipeline);
		pipelineGroups.setPipelines(pipelines);
		String branch = pipeline.getMaterials().get(0).getScmBranch();
		//if(!isPipelineExists(project.getName().replaceAll(" " , "-").replaceAll("\\." , ""), branch, jsessionid))
		createPipeline(pipelineGroups, jsessionid);
		projectBusinessImpl.createServiceConfigs(projectName, proxyName, getBranchType(scmPromote.getTargetBranch()), jsessionid);
	}

	public void promoteToMaster(ScmPromote scmPromote, String projectName, String proxyName, String jsessionid) throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		String repoName = scmPromote.getRepoName();
		String hostUrl = GIT_HOST_URL + repoName;
		String sourceBranch = scmPromote.getBaseBranch();
		String targetBranch = scmPromote.getTargetBranch();
		scmUtilImpl.promoteToGit(sourceBranch, targetBranch, hostUrl, 
				applicationProperties.getProxyScmUserName(), applicationProperties.getProxyScmPassword(), null);
		try {
			createPromotePipeline(scmPromote, projectName, proxyName, jsessionid);
		} catch (ItorixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public MultiValueMap<String, String> createProxyConnections(String proxyName, OrgEnv orgEnv) {
		projectBusinessImpl.publishProxyConnections(proxyName, orgEnv);
		return null;
	}

	public void createProxyConnections(String projectName, String proxyName, OrgEnv orgEnv) {
		projectBusinessImpl.publishProxyConnections(projectName, proxyName, orgEnv);
	}
	
	public Object searchProject(String name,int limit) throws ItorixException {
		BasicQuery query = 
				new BasicQuery("{\"name\": {$regex : '" + name + "', $options: 'i'}}");
			query.limit(limit > 0 ? limit : 10);
		List<Project> allprojects = mongoTemplate.find(query,Project.class);
		JSONObject projectsList = new JSONObject();
		List<SearchItem> projectNames = new ArrayList<SearchItem>();
		for (Project project : allprojects) {
			SearchItem item = new SearchItem();
			item.setId(project.getId());
			item .setName(project.getName());
			projectNames.add(item);
		}
		projectsList.put("Projects", projectNames);
		return projectsList;
	}

}
