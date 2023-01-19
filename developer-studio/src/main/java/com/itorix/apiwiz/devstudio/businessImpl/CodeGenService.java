package com.itorix.apiwiz.devstudio.businessImpl;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.opensaml.xml.encryption.Public;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.zeroturnaround.zip.ZipUtil;
import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.common.factory.IntegrationHelper;
import com.itorix.apiwiz.common.model.SearchItem;
import com.itorix.apiwiz.common.model.apigee.VirtualHost;
import com.itorix.apiwiz.common.model.configmanagement.KVMEntry;
import com.itorix.apiwiz.common.model.configmanagement.ServiceRequest;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.integrations.Integration;
import com.itorix.apiwiz.common.model.integrations.git.GitIntegration;
import com.itorix.apiwiz.common.model.integrations.jfrog.JfrogIntegration;
import com.itorix.apiwiz.common.model.integrations.s3.S3Integration;
import com.itorix.apiwiz.common.model.projectmanagement.Organization;
import com.itorix.apiwiz.common.model.projectmanagement.Project;
import com.itorix.apiwiz.common.model.projectmanagement.ProxyConnection;
import com.itorix.apiwiz.common.model.proxystudio.Scm;
import com.itorix.apiwiz.common.model.proxystudio.*;
import com.itorix.apiwiz.common.model.proxystudio.apigeeassociations.Deployments;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.StorageIntegration;
import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.common.util.artifatory.JfrogUtilImpl;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.common.util.s3.S3Utils;
import com.itorix.apiwiz.common.util.scm.ScmUtilImpl;
import com.itorix.apiwiz.common.util.zip.ZIPUtil;
import com.itorix.apiwiz.devstudio.business.LoadSwagger;
import com.itorix.apiwiz.devstudio.business.LoadWADL;
import com.itorix.apiwiz.devstudio.business.LoadWSDL;
import com.itorix.apiwiz.devstudio.dao.IntegrationsDao;
import com.itorix.apiwiz.devstudio.dao.MongoConnection;
import com.itorix.apiwiz.devstudio.model.*;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContextHolder;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.itorix.apiwiz.performance.coverge.businessimpl.CodeCoverageBusinessImpl;
import com.itorix.apiwiz.performance.coverge.businessimpl.PolicyPerformanceBusinessImpl;
import com.itorix.apiwiz.performance.coverge.model.History;
import com.itorix.apiwiz.serviceregistry.model.documents.ServiceRegistryColumnEntry;
import com.itorix.apiwiz.serviceregistry.model.documents.ServiceRegistryColumns;
import com.itorix.apiwiz.servicerequest.dao.ServiceRequestDao;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import freemarker.template.TemplateException;
import io.swagger.models.Swagger;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.time.Instant;
import java.util.*;
@Slf4j
@Component("codeGenService")
public class CodeGenService {
	@Value("${itorix.core.apigee.proxy.templates.base}")
	private String proxyGeneration;
	@Value("${server.port}")
	private String port;
	@Value("${server.contextPath}")
	private String context;

	@Autowired
	ApigeeProxyGeneration apigeeProxyGen;
	@Autowired
	private ApigeeTargetGeneration apigeeTargetGen;
	@Autowired
	private ApplicationProperties applicationProperties;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private BaseRepository baseRepository;
	@Autowired
	private MongoConnection mongoConnection;
	@Autowired
	private ProxyGen proxyGen;
	@Autowired
	private TargetGen targetGen;
	@Autowired
	ProxyGenerator proxyGenerator;
	@Autowired
	private JfrogUtilImpl ufile;
	@Autowired
	private S3Utils s3Utils;
	@Autowired
	private ScmUtilImpl scmUtil;
	@Autowired
	private CodeCoverageBusinessImpl codeCoverageService;
	@Autowired
	private PolicyPerformanceBusinessImpl policyPerformanceService;
	@Autowired
	private IntegrationsDao integrationsDao;
	@Autowired
	private ApigeeUtil apigeeUtil;
	@Autowired
	private IdentityManagementDao identityManagementDao;
	@Autowired
	private ServiceRequestDao serviceRequestDao;
	@Autowired
	private  LoadSwaggerImpl swagger ;
	

	@Autowired
	private IntegrationHelper integrationHelper;

	public String uploadTemplates(MultipartFile file) {
		String zipLocation = applicationProperties.getTempDir() + "unzip";
		String zipFile = applicationProperties.getTempDir() + file.getOriginalFilename();
		ZIPUtil unZip = new ZIPUtil();
		try {
			File targetFile = new File(zipFile);
			file.transferTo(targetFile);
			unZip.unzip(zipFile, zipLocation);
			ObjectMapper mapper = new ObjectMapper();
			mongoConnection.updateDocument(mongoConnection.getFolder(),
					mapper.writeValueAsString(getFolder(zipLocation + "/API")));
			FileUtils.cleanDirectory(new File(zipLocation));
			FileUtils.deleteDirectory(new File(zipLocation));
			File zipfile = new File(zipFile);
			zipfile.delete();
			return mongoConnection.getFolder();
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return null;
	}

	private Folder getFolder(String dirName) {
		log.debug("Getting folder for {}", dirName);
		File dir = new File(dirName);
		Folder folder;
		if (dir.isDirectory()) {
			folder = new Folder();
			folder.setName(dir.getName());
			folder.setFolder(true);
			File[] filesList = dir.listFiles();
			for (File file : filesList)
				folder.addFile(getFolder(file.getAbsolutePath()));
		} else {
			folder = new Folder();
			folder.setName(dir.getName());
			folder.setFolder(false);
			try {
				FileInputStream fileInputStream = new FileInputStream(dir.getAbsolutePath());
				mongoConnection.insertFile(fileInputStream, dir.getName());
				fileInputStream.close();
			} catch (FileNotFoundException e) {
				log.error("FileNotFoundException occurred", e);
			} catch (IOException e) {
				log.error("Exception occurred", e);
			}
		}
		return folder;
	}

	public ProxyGenResponse processCodeGen(CodeGenHistory codeGen, Operations operations, Project project)
			throws JsonParseException, JsonMappingException, IOException, TemplateException, ItorixException {
		Folder api = loadStructure();
		Folder commonFolder = api.getFile("Common");
		Folder proxyFolder = api.getFile("Proxy");
		Folder targetFolder = api.getFile("Target");
		String time = Long.toString(System.currentTimeMillis());
		String dir = operations.getDir() + time + File.separatorChar;
		ProxyArtifacts proxyArtifacts = null;
		// if (true) {
		if (proxyGeneration != null && proxyGeneration.equalsIgnoreCase("true")) {
			String proxyDir = dir + "src/gateway/" + codeGen.getProxy().getName() + (
					StringUtils.isEmpty(codeGen.getProxy().getVersion()) ? StringUtils.EMPTY
							: "_" + codeGen.getProxy().getVersion())
					+ "/apiproxy";
			if (codeGen.getProxy() != null)
				apigeeProxyGen.generateProxyCode(proxyFolder, commonFolder, codeGen, proxyDir);
			if (codeGen.getTarget() != null)
				apigeeTargetGen.generateTargetCode(targetFolder, codeGen, proxyDir);
			if (codeGen.getProxy() != null && codeGen.getTarget() != null) {
			}
		} else {
			if (codeGen.getProxy() != null & codeGen.getTarget() != null)
				proxyGen.generateCommonCode(commonFolder, codeGen, dir);
			if (codeGen.getProxy() != null)
				proxyGen.generateProxyCode(proxyFolder, codeGen, dir);
			if (codeGen.getTarget() != null)
				targetGen.generateTargetCode(targetFolder, codeGen, dir);
		}
		String proxyDir = dir + "src/gateway/" + codeGen.getProxy().getName() + (
				StringUtils.isEmpty(codeGen.getProxy().getVersion()) ? StringUtils.EMPTY
						: "_" + codeGen.getProxy().getVersion()) + "/apiproxy";
		CleanUnused.clean(proxyDir + File.separatorChar);
		proxyArtifacts = CleanUnused.processArtifacts(proxyDir);
		ZipUtil.pack(new File(dir), new File(operations.getDir() + time + ".zip"));
		// } else {
		// String proxyDir = dir + "src/gateway/" + codeGen.getProxy().getName()
		// + "/apiproxy";
		// codeGen.setProjectName(project.getName());
		// proxyGenerator.generateProxyCode(null, codeGen, proxyDir, project);
		// CleanUnused.clean(proxyDir + File.separatorChar);
		// proxyArtifacts = CleanUnused.processArtifacts(proxyDir);
		// ZipUtil.pack(new File(dir), new File(operations.getDir() + time +
		// ".zip"));
		// }

		try {
			ProxyData data = new ProxyData();
			org.json.JSONObject obj = null;
			String downloadURI = "";
			if (isValidScmDetails(codeGen)) {
				GitIntegration scmIntegration = getScmIntegration(
						codeGen.getProxySCMDetails().getScmSource().toUpperCase());
				RSAEncryption rSAEncryption = new RSAEncryption();

				if (scmIntegration.getAuthType().equals("TOKEN")) {
					String token = rSAEncryption.decryptText(scmIntegration.getToken());
					scmUtil.pushFilesToSCMBase64(new File(dir), codeGen.getProxySCMDetails().getReponame(),
							scmIntegration.getAuthType(), token, codeGen.getProxySCMDetails().getHostUrl(),
							codeGen.getProxySCMDetails().getScmSource(), codeGen.getProxySCMDetails().getBranch(),
							codeGen.getProxySCMDetails().getCommitMessage());
					codeGen.setScmURL(codeGen.getProxySCMDetails().getHostUrl());
					codeGen.setScmBranch(codeGen.getProxySCMDetails().getBranch());
				} else {
					String scmPassword = rSAEncryption.decryptText(scmIntegration.getPassword());
					scmUtil.pushFilesToSCM(new File(dir), codeGen.getProxySCMDetails().getReponame(),
							scmIntegration.getUsername(), scmPassword, codeGen.getProxySCMDetails().getHostUrl(),
							codeGen.getProxySCMDetails().getScmSource(), codeGen.getProxySCMDetails().getBranch(),
							codeGen.getProxySCMDetails().getCommitMessage());
					codeGen.setScmURL(codeGen.getProxySCMDetails().getHostUrl());
					codeGen.setScmBranch(codeGen.getProxySCMDetails().getBranch());
				}
			}
			try {
				StorageIntegration storageIntegration = integrationHelper.getIntegration();
				downloadURI = storageIntegration.uploadFile("proxy-generation/API/" + time + ".zip",
						operations.getDir() + time + ".zip");
				if (project != null)
					data.setProjectName(project.getName());
				if (null != codeGen.getPortfolio())
					data.setPortfolio(codeGen.getPortfolio());
				if (proxyArtifacts != null)
					data.setProxyArtifacts(proxyArtifacts);

			} catch (Exception e) {
				log.error("Exception occurred", e);
			}
			codeGen.setDownloadURL(downloadURI);
			codeGen.setUserCreated(operations.getUser().getFirstName() + " " + operations.getUser().getLastName());
			data.setDownloadURI(downloadURI);
			data.setLastModifiedUser(operations.getUser().getId());
			data.setLastModifiedUserName(
					operations.getUser().getFirstName() + " " + operations.getUser().getLastName());
			if (codeGen.getProxy() != null)
				if (codeGen.getProxy().getVersion() != null && project == null)
					data.setProxyName(codeGen.getProxy().getName() +(
							StringUtils.isEmpty(codeGen.getProxy().getVersion()) ? StringUtils.EMPTY
									: "_" + codeGen.getProxy().getVersion()));
				else
					data.setProxyName(codeGen.getProxy().getName());
			else
				data.setProxyName(codeGen.getTarget().get(0).getName());
			data.addCodeGenHistory(codeGen);
			String modified = Instant.now().toString();
			data.setDateModified(modified);
			codeGen.setDateCreated(modified);
			saveHistory(data);
			File tempFile = new File(dir);
			tempFile.delete();
			String swaggerStr = getSwagger(codeGen.getProxy().getBuildProxyArtifact(), codeGen.getProxy().getRevision(),
					codeGen.getProxy().getOas());
			if (null != swaggerStr)
				createAPICTarget(swaggerStr, proxyArtifacts.getTargetServers(), proxyArtifacts,
						codeGen.getProxy().getName());

			ProxyGenResponse response = populateProxyArtifacts(proxyArtifacts);
			response.setProxyName(data.getProxyName());
			response.setVersion(codeGen.getProxy().getVersion());
			response.setProxySCMDetails(codeGen.getProxySCMDetails());
			response.setDownloadURI(downloadURI);
			return response;
		} catch (Exception ex) {
			log.error("Exception occurred", ex);
			throw new ItorixException(ex.getMessage(), "ProxyGen-1000", ex);
		}
	}

	private ProxyGenResponse populateProxyArtifacts(ProxyArtifacts proxyArtifacts) throws ItorixException {
		ProxyGenResponse response = new ProxyGenResponse();
		List<String> kvms = proxyArtifacts.getKvms();
		if (kvms != null) {
			List<Artifact> configKVMS = new ArrayList<>();
			response.setKvms(configKVMS);
			for (String name : kvms) {
				Artifact kvmArtifact = new Artifact();
				List<OrgEnv> orgs = mongoConnection.getApigeeOrgs();
				kvmArtifact.setOrg(orgs);
				configKVMS.add(kvmArtifact);
				kvmArtifact.setName(name);
				List<ServiceRequest> requests = mongoConnection.getKVMRequests(name);
				if (requests != null) {
					for (ServiceRequest request : requests) {
						String type = request.getIsSaaS() == true ? "saas" : "onprem";
						kvmArtifact.setArtifactStatus(request.getOrg(), type, request.getEnv(), request.getStatus(),
								request.get_id());
					}
				}
				// List<KVMConfig> kvmList = mongoConnection.getKVM(name);
				// if(kvmList != null){
				// for(KVMConfig orgConfig : kvmList){
				// kvmArtifact.setArtifactStatus(orgConfig.getOrg(),
				// orgConfig.getType(),
				// orgConfig.getEnv(), "created");
				// }
				// }
			}
		}

		List<String> caches = proxyArtifacts.getCaches();
		if (caches != null) {
			List<Artifact> configCaches = new ArrayList<>();
			response.setCaches(configCaches);
			for (String name : caches) {
				Artifact cacheArtifact = new Artifact();
				List<OrgEnv> orgs = mongoConnection.getApigeeOrgs();
				configCaches.add(cacheArtifact);
				cacheArtifact.setName(name);
				cacheArtifact.setOrg(orgs);
				List<ServiceRequest> requests = mongoConnection.getCacheRequests(name);
				if (requests != null) {
					for (ServiceRequest request : requests) {
						String type = request.getIsSaaS() == true ? "saas" : "onprem";
						cacheArtifact.setArtifactStatus(request.getOrg(), type, request.getEnv(), request.getStatus(),
								request.get_id());
					}
				}
				// List<CacheConfig> cacheList = mongoConnection.getCache(name);
				// if(cacheList != null){
				// for(CacheConfig orgConfig : cacheList){
				// cacheArtifact.setArtifactStatus(orgConfig.getOrg(),
				// orgConfig.getType(),
				// orgConfig.getEnv(), "created");
				// }
				// }
			}
		}

		List<String> targets = proxyArtifacts.getTargetServers();
		if (targets != null) {
			List<Artifact> configTargets = new ArrayList<>();
			response.setTargetServers(configTargets);
			for (String name : targets) {
				Artifact targetArtifact = new Artifact();
				List<OrgEnv> orgs = mongoConnection.getApigeeOrgs();
				configTargets.add(targetArtifact);
				targetArtifact.setName(name);
				targetArtifact.setOrg(orgs);
				List<ServiceRequest> requests = mongoConnection.getTargetRequests(name);
				if (requests != null) {
					for (ServiceRequest request : requests) {
						String type = request.getIsSaaS() == true ? "saas" : "onprem";
						targetArtifact.setArtifactStatus(request.getOrg(), type, request.getEnv(), request.getStatus(),
								request.get_id());
					}
				}
			}
		}
		return response;
	}

	private String getSwagger(String swaggerName, String revision, String oas) {
		log.debug(" Getting Swagger from {}", swaggerName);
		if ("2.0".equals(oas)) {
			log.debug("Getting Swagger for oas {}", oas);

			Query query = new Query();
			query.addCriteria(Criteria.where("name").is(swaggerName));
			query.addCriteria(Criteria.where("revision").is(Integer.parseInt(revision)));
			SwaggerVO swaggerVO = mongoTemplate.findOne(query, SwaggerVO.class);
			if (null != swaggerVO) {
				String swaggerStr = swaggerVO.getSwagger();
				Swagger swagger;
				try {
					swagger = convertToSwagger(swaggerStr);
					if (swagger.getVendorExtensions() != null
							&& swagger.getVendorExtensions().get("x-EndpointExtension") != null) {
						return swaggerStr;
					}
				} catch (IOException e) {
					log.error("Exception occurred", e);
				}
			}
		}
		return null;
	}

	private Swagger convertToSwagger(String data) throws IOException {
		ObjectMapper mapper;
		if (data.trim().startsWith("{")) {
			mapper = Json.mapper();
		} else {
			mapper = Yaml.mapper();
		}
		JsonNode rootNode = mapper.readTree(data);
		JsonNode swaggerNode = rootNode.get("swagger");
		if (swaggerNode == null) {
			throw new IllegalArgumentException("Swagger String has an invalid format.");
		} else {
			return mapper.convertValue(rootNode, Swagger.class);
		}
	}

	private void createAPICTarget(String swaggerString, List<String> targetServers, ProxyArtifacts proxyArtifacts,
			String proxyName) {
		try {
			List<OrgEnv> org = mongoConnection.getApigeeOrgs();
			ObjectMapper mapper = new ObjectMapper();
			JsonNode data = mapper.readTree(swaggerString);
			Map<String, String> mapping = getApicMapping();
			String targeType = mapping.get("x-ibm-target-type");
			String targetPath = mapping.get("x-ibm-target-path");
			if (null != targetPath) {
				log.debug("Creating APIC target for target type {}", targeType);
				for (OrgEnv orgEnv : org) {
					for (Env env : orgEnv.getEnvs()) {
						String environment = getEnvironmet(mapping, orgEnv.getName(), env.getName(), orgEnv.getType());
						Organization organization = new Organization();
						organization.setName(orgEnv.getName());
						organization.setEnv(env.getName());
						organization.setType(orgEnv.getType());
						if (targeType.equalsIgnoreCase("targetserver")) {
							String target = getAPICTarget(data, targetPath, environment);
							createTargetServiceConfig(organization, targetServers.get(0), target);
						} else if (targeType.equalsIgnoreCase("kvm")) {
							createAPICKvm(organization, data, mapping, proxyArtifacts, proxyName);
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
	}

	private void createAPICKvm(Organization orgEnv, JsonNode data, Map<String, String> mapping,
			ProxyArtifacts proxyArtifacts, String name) {
		try {
			List<String> registryColumns = getRegistryColumns();
			Map<String, String> columnMap = getColumnMap(mapping, registryColumns);
			String rootNodePath = mapping.get("x-ibm-kvm-root");
			String environment = getEnvironmet(mapping, orgEnv.getName(), orgEnv.getEnv(), orgEnv.getType());
			rootNodePath = rootNodePath.replaceAll("\\{environment\\}", environment).replaceAll("#", "\\.");
			JsonNode rootNode = new ParseNode().parse(data, rootNodePath);
			if (null != rootNode) {
				Map<String, JsonNode> endpoints = getMapValuesNode(rootNode, columnMap);
				ObjectMapper mapper = new ObjectMapper();
				String endpointsStr = mapper.writeValueAsString(endpoints);
				createKvmServiceConfig(orgEnv, name, endpointsStr);
			}
		} catch (Exception e) {
			log.error("Exception occurred", e);

		}
	}

	private Map<String, JsonNode> getMapValuesNode(JsonNode node, Map<String, String> columnMap) {
		Map<String, JsonNode> dataMap = new HashMap<>();
		for (String name : columnMap.keySet()) {
			JsonNode nodeData = new ParseNode().parse(node, columnMap.get(name));
			dataMap.put(name, nodeData);
		}
		return dataMap;
	}

	private Map<String, String> getMap(List<String> registryColumns) {
		Map<String, String> dataMap = new HashMap<>();
		for (String name : registryColumns) {
			dataMap.put(name, null);
		}
		return dataMap;
	}

	private List<String> getRegistryColumns() {
		List<ServiceRegistryColumns> registryColumns = mongoTemplate.findAll(ServiceRegistryColumns.class);
		ServiceRegistryColumns registryColumnNames = registryColumns.isEmpty() ? null : registryColumns.get(0);
		List<String> names = new ArrayList<>();
		for (ServiceRegistryColumnEntry entry : registryColumnNames.getColumns()) {
			names.add(entry.getColumnId());
		}
		if (CollectionUtils.isEmpty(names))
			return null;
		return names;
	}

	private Map<String, String> getColumnMap(Map<String, String> mapping, List<String> registryColumns) {
		Map<String, String> columnMapping = new HashMap<>();
		for (String name : registryColumns)
			columnMapping.put(name, null);
		for (String name : mapping.keySet()) {
			if (name.contains("x-ibm-kvm-column-")) {
				log.debug("Column mapping for {}", name);
				String key = name.replaceAll("x-ibm-kvm-column-", "");
				String value = mapping.get(name);
				if (registryColumns.contains(key.trim())) {
					columnMapping.put(key, value);
				}
			}
		}
		return columnMapping;
	}

	private String getAPICTarget(JsonNode data, String targetPath, String environmet) {
		targetPath = targetPath.replaceAll("\\{environment\\}", environmet);
		JsonNode node = parseNode(data, targetPath);
		if (null != node) {
			String target = node.asText();
			return target;
		}
		return null;
	}

	private String getEnvironmet(Map<String, String> mapping, String org, String env, String type) {
		String path = org + ":" + env + ":" + type;
		String environmet = mapping.get(path);
		if (null != environmet) {
			environmet = environmet.replaceAll("x-ibm-environment-", "");
			return environmet;
		}
		return null;
	}

	private Map<String, String> getApicMapping() {
		Map<String, String> mappings = null;
		Query query = new Query();
		query.addCriteria(Criteria.where("type").is("APIC"));
		List<Integration> dbIntegrations = mongoTemplate.find(query, Integration.class);
		if (!CollectionUtils.isEmpty(dbIntegrations)) {
			mappings = dbIntegrations.get(0).getApicIntegration().getMappings();
		}
		return mappings;
	}

	private JsonNode parseNode(JsonNode node, String path) {
		log.debug("Parsing node {}", node);
		path = path.replace("x-ibm-policy/", "");
		String[] paths = path.split("/");
		try {
			JsonNode jsonNode = node;
			for (int i = 0; i < paths.length; i++) {
				String pathToken = paths[i];
				if (pathToken.contains("[")) {
					String nodeName = pathToken.replaceAll("\\[.*?\\]", "");
					String elementName = pathToken.substring(pathToken.indexOf("[") + 1, pathToken.indexOf("]"));
					ArrayNode arrayNode = (ArrayNode) jsonNode.get(nodeName);
					for (JsonNode elementNode : arrayNode) {
						if (null != elementNode.get(getFieldName(elementName))) {
							jsonNode = elementNode.get(getFieldName(elementName));
						}
					}
				} else {
					jsonNode = jsonNode.get(pathToken);
				}
			}
			return jsonNode;
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return null;
	}

	private String getFieldName(String name) {
		log.debug("Getting field name {}", name);
		String[] tokens = name.split("=");
		return tokens[1].replaceAll("'", "").replaceAll("#", ".");
	}

	private void createTargetServiceConfig(Organization organization, String name, String targrtURL)
			throws ItorixException {
		log.debug("Creating target service configuration for organization {}", organization);
		try {
			com.itorix.apiwiz.servicerequest.model.ServiceRequest config = new com.itorix.apiwiz.servicerequest.model.ServiceRequest();
			config.setType("TargetServer");
			config.setName(name);
			String host = null;
			String scheme = "http";
			String port = null;
			try {
				String[] tokens = targrtURL.split("://");
				if (tokens.length > 1) {
					host = tokens[1];
					scheme = tokens[0];
				} else
					host = tokens[0];
				tokens = host.split(":");
				if (tokens.length > 1) {
					host = tokens[0];
					port = tokens[1];
				}

			} catch (Exception ex) {
				log.error("Exception occurred", ex);
			}
			config.setOrg(organization.getName());
			config.setHost(host);
			if (null != port)
				config.setPort(Integer.parseInt(port));
			if ("https".equals(scheme)) {
				config.setSslEnabled(true);
			}
			config.setEnv(organization.getEnv());
			config.setIsSaaS(organization.getType().equalsIgnoreCase("saas") ? true : false);
			UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
			User user = identityManagementDao.getUserById(userSessionToken.getUserId());
			config.setCreatedUser(user.getFirstName() + " " + user.getLastName());
			config.setCreatedUserEmailId(user.getEmail());
			config.setCreatedDate(new Date(System.currentTimeMillis()));
			config.setModifiedUser(user.getFirstName() + " " + user.getLastName());
			config.setModifiedDate(new Date(System.currentTimeMillis()));
			config.setStatus("Review");
			config.setCreated(false);
			config.setActiveFlag(Boolean.TRUE);
			serviceRequestDao.createServiceRequest(config);

		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
	}

	private void createKvmServiceConfig(Organization organization, String proxyName, String endpoints)
			throws ItorixException {
		log.debug("Creating kvm service config for organization {}", organization);
		try {
			com.itorix.apiwiz.servicerequest.model.ServiceRequest config = new com.itorix.apiwiz.servicerequest.model.ServiceRequest();
			config.setType("KVM");
			config.setName(proxyName);
			config.setOrg(organization.getName());
			config.setEnv(organization.getEnv());
			config.setEncrypted("false");
			config.setIsSaaS(organization.getType().equalsIgnoreCase("saas") ? true : false);
			KVMEntry entry = new KVMEntry();
			entry.setName("endpoints");
			entry.setValue(endpoints);
			List<KVMEntry> entries = new ArrayList<KVMEntry>();
			entries.add(entry);
			config.setEntry(entries);
			UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
			User user = identityManagementDao.getUserById(userSessionToken.getUserId());
			config.setCreatedUser(user.getFirstName() + " " + user.getLastName());
			config.setCreatedUserEmailId(user.getEmail());
			config.setCreatedDate(new Date(System.currentTimeMillis()));
			config.setModifiedUser(user.getFirstName() + " " + user.getLastName());
			config.setModifiedDate(new Date(System.currentTimeMillis()));
			config.setStatus("Review");
			config.setCreated(false);
			config.setActiveFlag(Boolean.TRUE);
			serviceRequestDao.createServiceRequest(config);
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
	}

	private JfrogIntegration getJfrogIntegration() {
		JfrogIntegration jfrogIntegration = integrationsDao.getJfrogIntegration().getJfrogIntegration();
		if (jfrogIntegration != null) {
			String decryptedPassword = "";
			try {
				RSAEncryption rSAEncryption = new RSAEncryption();
				decryptedPassword = rSAEncryption.decryptText(jfrogIntegration.getPassword());
			} catch (Exception e) {
				log.error("Exception occurred", e);
			}
			jfrogIntegration.setPassword(decryptedPassword);
		} else {
			String hostURL = applicationProperties.getJfrogHost() + ":" + applicationProperties.getJfrogPort();
			String userName = applicationProperties.getJfrogUserName();
			String password = applicationProperties.getJfrogPassword();
			jfrogIntegration = new JfrogIntegration();
			jfrogIntegration.setHostURL(hostURL);
			jfrogIntegration.setUsername(userName);
			jfrogIntegration.setPassword(password);
		}
		return jfrogIntegration;
	}

	private S3Integration getS3Integration() {
		S3Integration s3Integration = integrationsDao.getS3Integration().getS3Integration();
		if (s3Integration != null) {
			String decryptedPassword = "";
			try {
				decryptedPassword = s3Integration.getDecryptedSecret();
			} catch (Exception e) {
				log.error("Exception occurred", e);
			}
		} else {
			String key = applicationProperties.getS3key();
			String secret = applicationProperties.getS3secret();
			String bucketName = applicationProperties.getS3bucketName();
			String region = applicationProperties.getS3region();
			if (null != key && null != secret && null != bucketName && null != region) {
				s3Integration = new S3Integration();
				s3Integration.setKey(key);
				s3Integration.setSecret(secret);
				s3Integration.setRegion(region);
				s3Integration.setBucketName(bucketName);
			}
		}
		return s3Integration;
	}

	public GitIntegration getScmIntegration(String type) {
		GitIntegration gitIntegration = null;
		Integration integration = integrationsDao.getGitIntegration(type, "proxy");

		if (integration != null)
			gitIntegration = integration.getGitIntegration();
		return gitIntegration;
	}

	public boolean isValidScmDetails(CodeGenHistory codeGen) {
		boolean isValid = false;
		if (codeGen.getProxySCMDetails() != null && codeGen.getProxySCMDetails().getHostUrl() != null
				&& codeGen.getProxySCMDetails().getReponame() != null
				&& codeGen.getProxySCMDetails().getScmSource() != null) {
			isValid = true;
		}
		return isValid;
	}

	private void saveHistory(ProxyData data) {
		log.debug("Saving history of data {}", data);
		ProxyData proxyData = mongoConnection.getProxyHistory(data.getProxyName());
		if (proxyData != null) {
			try {
				proxyData.setProjectName(data.getCodeGenHistory().get(0).getProjectName());
				proxyData.addCodeGenHistory(data.getCodeGenHistory().get(0));
				proxyData.setDownloadURI(data.getDownloadURI());
				proxyData.setDateModified(data.getDateModified());
				proxyData.setLastModifiedUserName(data.getLastModifiedUserName());
				proxyData.setLastModifiedUser(data.getLastModifiedUser());
				proxyData.setProxyArtifacts(data.getProxyArtifacts());
				proxyData.setProjectName(data.getProjectName());
			} catch (Exception e) {
				log.error("Exception occurred", e);
			}
		} else {
			proxyData = data;
		}
		try {
			mongoConnection.saveProxyHistory(proxyData);
		} catch (JsonProcessingException e) {
			log.error("Exception occurred", e);
		}
	}

	@SuppressWarnings("unchecked")
	public ProxyHistoryResponse getHistory(int offset, int pageSize, String proxy) throws Exception {

		ProxyHistoryResponse response = mongoConnection.getProxyHistory(offset, pageSize, proxy);
		List<ProxyData> stringData = (List<ProxyData>) response.getData();
		List<Map<String, String>> listData = new ArrayList<Map<String, String>>();
		if (stringData == null) {
			new ProxyHistoryResponse();
		}
		for (ProxyData proxyData : stringData) {
			Map<String, String> mapData = new HashMap<String, String>();
			mapData.put("id", proxyData.getId());
			mapData.put("proxyName", proxyData.getProxyName());
			mapData.put("userName", proxyData.getLastModifiedUserName());
			mapData.put("dateModified", proxyData.getDateModified());
			mapData.put("downloadURI", proxyData.getDownloadURI());
			List<History> history = null;
			try {
				history = codeCoverageService.getCodeCoverageList(null, true, proxyData.getProxyName(), null, null,
						null, true, null, null);
				if (history != null && history.size() > 0) {
					int index = history.size() - 1;
					mapData.put("codeCoverageId", history.get(index).getId());
					mapData.put("percentage", history.get(index).getPercentage());
				} else
					mapData.put("codeCoverageId", null);
			} catch (Exception e) {
				mapData.put("codeCoverageId", null);
			}

			history = null;
			try {
				history = policyPerformanceService.getPolicyPerformanceList(null, true, proxyData.getProxyName(), null,
						null, null, true, null, null);
				if (history != null && history.size() > 0) {
					int index = history.size() - 1;
					mapData.put("policyCoverageId", history.get(index).getId());
				} else
					mapData.put("policyCoverageId", null);
			} catch (Exception e) {
				mapData.put("policyCoverageId", null);
			}

			OrgEnvs orgEnvs = proxyData.getOrgEnvs();
			mapData.put("status", getDeploymentStatus(orgEnvs));
			listData.add(mapData);
		}
		response.setData(listData);
		return response;
	}

	public List<String> getProxies() {
		return mongoConnection.getProxyNames();
	}

	public List<String> getProxies(String proxy, String revision) {
		List<String> stringData = mongoConnection.getProxyHistory();
		Set<String> listData = new HashSet<String>();
		if (stringData == null) {
			return new ArrayList<String>(listData);
		}

		for (String dataElement : stringData) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				ProxyData proxyData = mapper.readValue(dataElement, ProxyData.class);
				for (CodeGenHistory history : proxyData.getCodeGenHistory())
					if (history.getProxy().getBuildProxyArtifactType() != null
							&& history.getProxy().getBuildProxyArtifactType().equals("swagger"))
						if (history.getProxy().getBuildProxyArtifact() != null
								&& history.getProxy().getBuildProxyArtifact().equals(proxy))
							if (history.getProxy().getRevision() != null
									&& history.getProxy().getRevision().equals(revision))
								listData.add(proxyData.getProxyName());
			} catch (IOException e) {
				log.error("Exception occurred", e);
			}
		}
		return new ArrayList<String>(listData);
	}

	public boolean saveProxyArtifacts(String proxy, ProxyArtifacts proxyArtifacts) throws ItorixException {
		return mongoConnection.updateArtifacts(proxy, proxyArtifacts);
	}

	public boolean saveProxyData(ProxyData data) throws ItorixException, JsonProcessingException {
		return mongoConnection.saveProxyHistory(data);
	}

	public ProxyArtifacts getProxyArtifacts(String proxy) throws ItorixException {
		log.debug("Getting proxy artifacts from {}", proxy);
		try {
			ProxyData data = mongoConnection.getProxyHistory(proxy);
			if (data != null) {
				return data.getProxyArtifacts();
			} else
				throw new ItorixException("No records exist for Proxy - " + proxy, "ProxyGen-1001");
		} catch (Exception e) {
			throw new ItorixException(e.getMessage(), "ProxyGen-1000", e);
		}
	}

	public List<ProxyConnection> getProxyConnections(String proxy) throws ItorixException {
		log.debug("Fetching proxy connections from {}", proxy);

		try {
			ProxyData data = mongoConnection.getProxyHistory(proxy);
			if (data != null) {
				return data.getProxyConnections();
			} else
				throw new ItorixException("No records exist for Proxy - " + proxy, "ProxyGen-1001");
		} catch (Exception e) {
			throw new ItorixException(e.getMessage(), "ProxyGen-1000", e);
		}
	}

	public String getProjectName(String proxy) throws ItorixException {
		log.debug("Fetching project name from {}", proxy);

		try {
			ProxyData data = mongoConnection.getProxyHistory(proxy);
			if (data != null) {
				return data.getProjectName();
			} else
				throw new ItorixException("No records exist for Proxy - " + proxy, "ProxyGen-1001");
		} catch (Exception e) {
			throw new ItorixException(e.getMessage(), "ProxyGen-1000", e);
		}
	}

	public ProxyData getProxyData(String proxy) throws ItorixException {
		try {
			ProxyData data = mongoConnection.getProxyHistory(proxy);
			if (data != null) {
				return data;
			} else
				throw new ItorixException("No records exist for Proxy - " + proxy, "ProxyGen-1001");
		} catch (Exception e) {
			throw new ItorixException(e.getMessage(), "ProxyGen-1000", e);
		}
	}

	public boolean saveAssociatedOrgs(String proxy, OrgEnvs orgenvs) throws ItorixException {
		log.debug("Saving associated organisations from {}", proxy);
		if (mongoConnection.updateAssociatedOrgs(proxy, orgenvs)) {
			log.debug("Saving associated orgs for proxy {}", proxy);
			try {
				List<OrgEnv> orgEnvList = orgenvs.getOrgEnvs();
				for (OrgEnv orgEnv : orgEnvList) {
					String environment = orgEnv.getName();
					List<Env> orgList = orgEnv.getEnvs();
					for (Env organization : orgList) {
						ProxyData proxyData = mongoConnection.saveProxyDetails(proxy);
						List<Deployments> deployments = proxyData.getProxyApigeeDetails().getDeployments();
						String revision = null;
						try {
							for (Deployments deployment : deployments) {
								if (deployment.getOrg().equals(environment)
										&& deployment.getEnv().equals(organization.getName())) {
									List<com.itorix.apiwiz.common.model.proxystudio.apigeeassociations.Proxy> proxiesList = deployment
											.getProxies();
									com.itorix.apiwiz.common.model.proxystudio.apigeeassociations.Proxy deployedProxy = proxiesList
											.get(0);
									revision = deployedProxy.getRevision();
								}
							}
						} catch (Exception e) {
							log.error("Exception occurred", e);
						}
						if (revision != null && revision != "") {
							organization.setStatus("deployed");
						} else {
							organization.setStatus("created");
						}
					}
				}
				orgenvs.setOrgEnvs(orgEnvList);
				return mongoConnection.updateAssociatedOrgs(proxy, orgenvs);
			} catch (Exception e) {
				log.error("Exception occurred", e);
			}
		}
		return false;
	}

	public void saveAssociatedOrgforProxy(String proxy, OrgEnv orgenv) {
		log.debug("Saving associated organisations for {}", proxy);
		try {
			mongoConnection.saveProxyDetailsByOrgEnv(proxy, orgenv);
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
	}

	public OrgEnvs getAssociatedOrgs(String proxy) throws ItorixException {
		return mongoConnection.getAssociatedOrgs(proxy);
	}

	public ProxyData getApigeeDetails(String proxy) throws ItorixException {
		return getApigeeDetails(proxy, false, null);
	}

	public void removeProxy(String proxy) throws ItorixException {
		mongoConnection.removeProxy(proxy);
	}

	public void promoteProxy(PromoteSCM proxySCM) throws Exception {
		String proxyId = proxySCM.getScm().getProxyId();
		ProxyData proxyData = mongoConnection.getProxyDetailsById(proxyId);
		if (proxyData != null && proxyData.getPortfolio() != null) {
			promoteProxy(proxyData.getPortfolio(), proxySCM.getScm());
		} else {
			String type = proxySCM.getScm().getScmType().toUpperCase();
			GitIntegration gitIntegration = getScmIntegration(type);
			if (gitIntegration != null) {
				if (type.equalsIgnoreCase("GITLAB")) {
					RSAEncryption rSAEncryption = new RSAEncryption();
					if (gitIntegration.getAuthType().equalsIgnoreCase("TOKEN")) {
						String scmPassword = rSAEncryption.decryptText(gitIntegration.getToken());
						scmUtil.promoteToGitToken(proxySCM.getScm().getBaseBranch(),
								proxySCM.getScm().getDestinationBranch(), proxySCM.getScm().getGitURL(),
								proxySCM.getScm().getScmType(), scmPassword, proxySCM.getScm().getCommitMessage());
					} else {
						String scmPassword = rSAEncryption.decryptText(gitIntegration.getPassword());
						scmUtil.promoteToGit(proxySCM.getScm().getBaseBranch(),
								proxySCM.getScm().getDestinationBranch(), proxySCM.getScm().getGitURL(),
								gitIntegration.getUsername(), scmPassword, proxySCM.getScm().getCommitMessage());
					}
				} else if (type.equalsIgnoreCase("GIT")) {
					RSAEncryption rSAEncryption = new RSAEncryption();

					if (gitIntegration.getAuthType().equalsIgnoreCase("TOKEN")) {
						String scmPassword = rSAEncryption.decryptText(gitIntegration.getToken());
						scmUtil.promoteToGitToken(proxySCM.getScm().getBaseBranch(),
								proxySCM.getScm().getDestinationBranch(), proxySCM.getScm().getGitURL(),
								proxySCM.getScm().getScmType(), scmPassword, proxySCM.getScm().getCommitMessage());
					} else {
						String scmPassword = rSAEncryption.decryptText(gitIntegration.getPassword());
						scmUtil.promoteToGit(proxySCM.getScm().getBaseBranch(),
								proxySCM.getScm().getDestinationBranch(), proxySCM.getScm().getGitURL(),
								gitIntegration.getUsername(), scmPassword, proxySCM.getScm().getCommitMessage());
					}

				}
			}
		}
	}

	private void promoteProxy(ProxyPortfolio portfolio, Scm scm) throws ItorixException {
		try {
			PromoteProxyRequest promoteProxyRequest = new PromoteProxyRequest();
			promoteProxyRequest.setPortfolio(portfolio);
			promoteProxyRequest.setScm(scm);
			UserSession userSession = UserSession.getCurrentSessionToken();
			String hostUrl = "http://localhost:#port#/#context#/v1/portfolios/proxies/promote";
			hostUrl = hostUrl.replaceAll("#port#", port);
			hostUrl = hostUrl.replaceAll("#context#", context.replaceAll("/", ""));
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("JSESSIONID", userSession.getId());
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<PromoteProxyRequest> requestEntity = new HttpEntity<>(promoteProxyRequest, headers);
			// ResponseEntity<String> response =
			log.debug("Making a call to {}", hostUrl);
			restTemplate.exchange(hostUrl, HttpMethod.POST, requestEntity, String.class);
		} catch (Exception e) {
			throw new ItorixException("error creating pipeline", "", e);
		}
	}

	public ProxyData getApigeeDetails(String proxy, boolean refresh, String type) throws ItorixException {
		ProxyData proxyDetails = new ProxyData();
		try {
			proxyDetails = mongoConnection.getProxyDetails(proxy);
			if (!refresh) {
				if (proxyDetails == null)
					throw new ItorixException("Data not available", "ProxyGen-1002");
				// if(proxyDetails!=null &&
				// proxyDetails.getProxyApigeeDetails()!=null){
				if (proxyDetails.getProxyApigeeDetails() != null
						&& proxyDetails.getProxyApigeeDetails().getDeployments() == null) {
					proxyDetails.setProxyApigeeDetails(null);
				} else if (proxyDetails.getProxyApigeeDetails() != null
						&& proxyDetails.getProxyApigeeDetails().getDeployments() != null
						&& proxyDetails.getProxyApigeeDetails().getDeployments() != null
						&& proxyDetails.getProxyApigeeDetails().getDeployments().size() < 1)
					proxyDetails.setProxyApigeeDetails(null);
				else if (proxyDetails.getProxyApigeeDetails() != null
						&& proxyDetails.getProxyApigeeDetails().getDeployments() != null
						&& proxyDetails.getProxyApigeeDetails().getDeployments().size() > 0
						&& proxyDetails.getProxyApigeeDetails().getDeployments().get(0) == null)
					proxyDetails.setProxyApigeeDetails(null);
				if (type != null) {
					proxyDetails = getProxyDetails(proxyDetails, type);
				}
				return proxyDetails;
			} else {
				return mongoConnection.saveProxyDetails(proxy);
			}
		} catch (Exception e) {
			throw new ItorixException(e.getMessage(), "ProxyGen-1000", e);
		}
	}

	private ProxyData getProxyDetails(ProxyData data, String type) {

		if (type.equalsIgnoreCase("artifacts")) {
			data.setOrgEnvs(null);
			data.setCodeGenHistory(null);
			data.setProxyApigeeDetails(null);
			data.setProxyConnections(null);
		}
		if (type.equalsIgnoreCase("details")) {
			data.setOrgEnvs(null);
			data.setCodeGenHistory(null);
			data.setProxyApigeeDetails(null);
			data.setProxyArtifacts(null);
			data.setProxyConnections(null);
		}
		if (type.equalsIgnoreCase("deployment")) {
			data.setOrgEnvs(null);
			data.setCodeGenHistory(null);
			data.setProxyArtifacts(null);
			data.setProxyConnections(null);
		}
		if (type.equalsIgnoreCase("history")) {
			data.setOrgEnvs(null);
			data.setProxyApigeeDetails(null);
			data.setProxyArtifacts(null);
			data.setProxyConnections(null);
		}
		if (type.equalsIgnoreCase("endpoints")) {
			data.setOrgEnvs(null);
			data.setCodeGenHistory(null);
			data.setProxyApigeeDetails(null);
			data.setProxyArtifacts(null);
		}
		return data;
	}

	private String getVersion(String content) throws JsonMappingException, JsonProcessingException {
		log.debug("Fetching version from {}", content);
		if (content == null) {
			return null;
		}
		ObjectMapper JsonMapper = Json.mapper();
		JsonNode node = JsonMapper.readTree(content);
		JsonNode version = node.get("openapi");
		if (version != null) {
			return "3.0";
		} else {
			return "2.0";
		}
	}

	public Proxy proxyOperations(Operations operations) throws ItorixException {
		try {
			String content;
			String oas = "2.0";
			ObjectMapper mapper = new ObjectMapper();
			if (!operations.isSwaggerInDB()) {
				InputStream inStream = operations.getFile().getInputStream();
				content = IOUtils.toString(inStream, "UTF-8");
				if (operations.getType().equalsIgnoreCase("swagger"))
					oas = getVersion(content);

			} else {
				if (operations.getOas() != null && operations.getOas().equals("3.0")) {
					Swagger3VO swaggerVO = baseRepository.findOne("name", operations.getFileName(), "revision",
							operations.getVersion(), Swagger3VO.class);
					content = swaggerVO.getSwagger();
					oas = operations.getOas();
				} else {
					SwaggerVO swaggerVO = baseRepository.findOne("name", operations.getFileName(), "revision",
							operations.getVersion(), SwaggerVO.class);
					content = swaggerVO.getSwagger();
				}
			}
			String proxyString = null;
			if (operations.getType().equalsIgnoreCase("swagger")) {
				try {
					//LoadSwagger swagger = new LoadSwaggerImpl();
					proxyString = swagger.loadProxySwaggerDetails(content, oas);
				} catch (Exception e) {
					log.error("Exception occurred", e);
					throw e;
				}
			} else if (operations.getType().equalsIgnoreCase("WADL")) {
				LoadWADL wadl = new LoadWADLImpl();
				proxyString = wadl.getWADLProxyOperations(content);
			} else if (operations.getType().equalsIgnoreCase("WSDL")) {
				LoadWSDL wsdl = new LoadWSDLImpl();
				proxyString = wsdl.loadWSDLProxyOperations(content);
			}
			Proxy proxy = mapper.readValue(proxyString, Proxy.class);
			if (null == proxy.getName()) {
				String[] name = operations.getFileName().toString().split("\\.");
				if (name.length > 0)
					proxy.setName(name[0]);
				else
					proxy.setName(operations.getFileName().replaceAll(" ", ""));
				proxy.setDescription(proxy.getName());
				proxy.setVersion("v1");
			}
			proxy.setBuildProxyArtifact(operations.getFileName());
			proxy.setBuildProxyArtifactType(operations.getType());
			if (operations.getVersion() > 0)
				proxy.setRevision(Integer.toString(operations.getVersion()));
			proxy.setOas(operations.getOas());
			return proxy;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "ProxyGen-1000", ex);
		}
	}

	public Target targetOperations(Operations operations) throws ItorixException {
		try {

			String content;
			String oas = "2.0";
			ObjectMapper mapper = new ObjectMapper();
			if (!operations.isSwaggerInDB()) {
				InputStream inStream = operations.getFile().getInputStream();
				content = IOUtils.toString(inStream, "UTF-8");
				if (operations.getType().equalsIgnoreCase("swagger"))
					oas = getVersion(content);
			} else {
				if (operations.getOas() != null && operations.getOas().equals("3.0")) {
					Swagger3VO swaggerVO = baseRepository.findOne("name", operations.getFileName(), "revision",
							operations.getVersion(), Swagger3VO.class);
					content = swaggerVO.getSwagger();
					oas = operations.getOas();
				} else {
					SwaggerVO swaggerVO = baseRepository.findOne("name", operations.getFileName(), "revision",
							operations.getVersion(), SwaggerVO.class);
					content = swaggerVO.getSwagger();
				}
			}
			String proxyString = "";
			if (operations.getType().equalsIgnoreCase("swagger")) {
				//LoadSwagger swagger = new LoadSwaggerImpl();
				proxyString = swagger.loadTargetSwaggerDetails(content, oas);
			} else if (operations.getType().equalsIgnoreCase("WADL")) {
				LoadWADL wadl = new LoadWADLImpl();
				proxyString = wadl.getWADLTargetOperations(content);
			} else if (operations.getType().equalsIgnoreCase("WSDL")) {
				LoadWSDL wsdl = new LoadWSDLImpl();
				proxyString = wsdl.loadWSDLOperations(content);
			}
			Target target = mapper.readValue(proxyString, Target.class);
			if (null == target.getName()) {
				String[] name = operations.getFileName().toString().split("\\.");
				if (name.length > 0)
					target.setName(name[0].replaceAll(" ", ""));
				else
					target.setName(operations.getFileName());
				target.setDescription(target.getName().replaceAll(" ", ""));
			}

			for (int i = 0; i < target.getFlows().getFlow().length; i++) {
				String condition = target.getFlows().getFlow()[i].getName();
				target.getFlows().getFlow()[i].setCondition("(target.route.operation = \"" + condition + "\")");
			}
			target.setBuildTargetArtifact(operations.getFileName());
			target.setBuildTargetArtifactType(operations.getType());
			target.setOas(operations.getOas());
			if (operations.getVersion() > 0)
				target.setRevision(Integer.toString(operations.getVersion()));
			target.setOas(operations.getOas());
			return target;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "ProxyGen-1000", ex);
		}
	}

	public boolean insertFile(MultipartFile file) throws IOException {
		return mongoConnection.insertFile(file);
	}

	public boolean removeFile(String file) throws IOException {
		return mongoConnection.removeFile(file);
	}

	public boolean addFolderResource(String path, String resource, boolean isFolder)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		String dbFolder = mongoConnection.getFolder();

		Folder folder = mapper.readValue(dbFolder, Folder.class);
		String[] pathToken = path.split("/");
		Folder pathFolder = folder;
		for (int i = 0; i < pathToken.length - 1; i++) {
			pathFolder = pathFolder.getFile(pathToken[i + 1]);
		}
		if (pathFolder.getFile(resource) == null) {
			Folder file = new Folder();
			file.setName(resource);
			file.setFolder(isFolder);
			pathFolder.addFile(file);
		} else {
			return false;
		}

		String updateFolder = mapper.writeValueAsString(folder);
		mongoConnection.updateDocument(dbFolder, updateFolder);
		return true;
	}

	public List<String> getFolders(String path) throws JsonParseException, JsonMappingException, IOException {
		log.debug(" Getting folders from {}", path);
	  log.info("path received : {}", path);
		ObjectMapper mapper = new ObjectMapper();
		String dbFolder = mongoConnection.getFolder();
		Folder folder = mapper.readValue(dbFolder, Folder.class);
		String[] pathToken = path.split("/");
		Folder pathFolder = folder;
		for (int i = 0; i < pathToken.length - 1; i++)
			pathFolder = pathFolder.getFile(pathToken[i + 1]);

		List<Folder> files = pathFolder.getFiles();
		List<String> fileList = null;
		if (files != null) {
			fileList = new ArrayList<String>();
			for (Folder file : files)
				fileList.add(file.getName());
		}

		return fileList;
	}

	public boolean removeFile(String path, String file) throws JsonParseException, JsonMappingException, IOException {
		log.debug("Remove file {}", file);
		ObjectMapper mapper = new ObjectMapper();
		String dbFolder = mongoConnection.getFolder();
		Folder folder = mapper.readValue(dbFolder, Folder.class);
		String[] pathToken = path.split("/");
		Folder pathFolder = folder;
		for (int i = 0; i < pathToken.length - 1; i++) {
			pathFolder = pathFolder.getFile(pathToken[i + 1]);
		}
		Folder tmp = pathFolder.getFile(file);
		if (tmp == null) {
			return false;
		} else {
			pathFolder.removeFile(file);
		}

		String updateFolder = mapper.writeValueAsString(folder);
		mongoConnection.updateDocument(dbFolder, updateFolder);
		return true;
	}

	private String getDeploymentStatus(OrgEnvs orgEnvs) {
		log.debug(" Getting deployment status for orgEnvs {}", orgEnvs);
		List<String> status = new ArrayList<>();

		if (orgEnvs != null) {
			log.debug("Getting deployment status");
			try {
				for (OrgEnv orgEnv : orgEnvs.getOrgEnvs()) {
					if (orgEnv.getEnvs() != null)
						for (Env env : orgEnv.getEnvs()) {
							if (env.getStatus() != null)
								status.add(env.getStatus());
						}
				}
			} catch (Exception ex) {
				log.error("Exception occurred", ex);
			}
		}
		if (status.contains("depricated")) {
			return "depricated";
		} else if (status.contains("deployed"))
			return "deployed";
		else
			return "created";
	}

	public String getFile(String file) {
		return mongoConnection.getFile(file);
	}

	public Object getCategories() throws ItorixException {
		try {
			return getCategories(null);
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "ProxyGen-1000", ex);
		}
	}

	public Object getCategories(String swaggerId, int revision, String oas) throws ItorixException {
		try {
			if (null != swaggerId && "2.0".equals(oas.trim())) {
				Swagger swagger = getSwaggerbyId(swaggerId, revision, oas);
				if (null != swagger) {
					Object catagories = swagger.getVendorExtensions().get("x-policies");
					if (null != catagories)
						return catagories;
					else
						return getCategories(false);
				}
			}
			if(null != swaggerId){
				return getCategories(false);
			}
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "ProxyGen-1000", ex);
		}
		return getCategories(null);
	}

	private Swagger getSwaggerbyId(String swaggerId, int revision, String oas) {
		log.debug("Get Swagger by Id: {}", swaggerId);
		if ("2.0".equals(oas)) {
			log.debug("Getting Swagger by Id: {}", swaggerId);
			Query query = new Query();
			query.addCriteria(Criteria.where("swaggerId").is(swaggerId));
			query.addCriteria(Criteria.where("revision").is(revision));
			SwaggerVO swaggerVO = mongoTemplate.findOne(query, SwaggerVO.class);
			if (null != swaggerVO) {
				String swaggerStr = swaggerVO.getSwagger();
				Swagger swagger = null;
				try {
					swagger = convertToSwagger(swaggerStr);
					if (swagger.getVendorExtensions() != null
							&& swagger.getVendorExtensions().get("x-EndpointExtension") != null) {
						return swagger;
					}
				} catch (IOException e) {
					log.error("Exception occurred", e);
				}
			}
		}
		return null;
	}

	public Object getCategories(String name) throws ItorixException {
		log.debug("Getting categories for {}", name);
		try {
			if ((name != null) && (!name.equals(""))) {
				Query query = new Query(Criteria.where("name").is(name));
				return mongoTemplate.find(query, Category.class);
			} else {
				return mongoTemplate.findAll(Category.class);
			}
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "ProxyGen-1000", ex);
		}
	}
	
	public List<Category> getCategories(boolean applyToFlow) throws ItorixException {
		try {
			List<Category> categories = null;
			MongoCollection<Document> collection = mongoTemplate.getCollection("Connectors.Apigee.Policy.Templates");
			List<Document> aggregateQuery = new ArrayList<>();
			List<Object> conditions = new ArrayList<>();
			conditions.add("$$policies.applyToFlow");
			conditions.add(applyToFlow);
			Document matchDoc = new Document("$match", new Document("policies.applyToFlow", applyToFlow));
			Document projectDoc = new Document("$project",new Document("_id", 0)
					.append("name", "$name")
					.append("type", "$type")
					.append("description", "$description")
					.append("policies",new Document("$filter",new Document("input","$policies").append("as", "policies").append("cond", new Document("$eq",conditions))))
					);
			aggregateQuery.add(matchDoc);
			aggregateQuery.add(projectDoc);

			AggregateIterable<Document> doc = collection.aggregate(aggregateQuery);
			if(doc != null && doc.cursor().hasNext()) {
				ObjectMapper mapper = new ObjectMapper();
				for (Document doc1 : doc) {
					if(categories == null)
						categories = new ArrayList<>();
					Category category = mapper.readValue(doc1.toJson(), Category.class);
					categories.add(category);
				}
			} else {
				categories = mongoTemplate.findAll(Category.class);
			}
			return categories;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ItorixException(ex.getMessage(), "ProxyGen-1000", ex);
		}
	}
	

	public boolean saveCategory(List<Category> categories) throws ItorixException {
		try {
			mongoTemplate.remove(new Query(), Category.class);
			for (Category category : categories) {
				Query query = new Query(Criteria.where("type").is(category.getType()));
				Update update = new Update();
				update.set("name", category.getName());
				update.set("description", category.getDescription());
				update.set("policies", category.getPolicies());
				mongoTemplate.upsert(query, update, Category.class);
			}
			return true;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "ProxyGen-1000", ex);
		}
	}

	public void publishProxyConnections(String proxyName, OrgEnv orgEnv) {
		try {
			saveAssociatedOrgforProxy(proxyName, orgEnv);

			// ProxyArtifacts proxyArtifacts = getProxyArtifacts(proxyName);
			// List<ProxyConnection> connections = getProxyConnections(orgEnv,
			// proxyArtifacts);
			// List<ProxyConnection> uniqueconnections;
			// if(project.getProxyByName(proxyName).getProxyConnections()!=
			// null)
			// {
			// uniqueconnections =
			// getUniqueValues(project.getProxyByName(proxyName).getProxyConnections(),
			// orgEnv.getName(),
			// orgEnv.getEnvs().get(0).getName());
			// uniqueconnections.addAll(connections);
			// }
			// else
			// uniqueconnections = connections;
			// project.getProxyByName(proxyName).setProxyConnections(uniqueconnections);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("Exception occurred", e);
		}
	}

	public List<ProxyConnection> getProxyConnections(OrgEnv orgEnv, ProxyArtifacts proxyArtifacts) {
		log.debug(" Getting proxy connections");

		List<ProxyEndpoint> proxyEndpoints = proxyArtifacts.getProxyEndpoints();
		List<ProxyConnection> proxyConnections = new ArrayList<ProxyConnection>();
		for (Env env : orgEnv.getEnvs()) {
			for (ProxyEndpoint proxyEndpoint : proxyEndpoints)
				for (String virtualHost : proxyEndpoint.getVirtualHosts()) {
					List<String> hosts = getProxyConnectionURL(orgEnv.getName(), env.getName(),
							orgEnv.getType().equalsIgnoreCase("saas") ? "true" : "false", virtualHost);
					if (hosts != null)
						for (String host : hosts) {
							String url;
							if (host == null)
								url = "N/A";
							else
								url = host + proxyEndpoint.getBasePath();
							ProxyConnection connection = new ProxyConnection();
							connection.setEnvName(env.getName());
							connection.setIsSaaS(orgEnv.getType().equalsIgnoreCase("saas") ? "true" : "false");
							connection.setOrgName(orgEnv.getName());
							connection.setProxyEndpoint(proxyEndpoint.getName());
							connection.setProxyURL(url);
							proxyConnections.add(connection);
						}
				}
		}
		return proxyConnections;
	}

	public List<String> getProxyConnectionURL(String org, String env, String isSaaS, String vHostName) {
		log.debug("Getting proxy connection URL for org: {}", org);
		try {
			String apigeeURL = apigeeUtil.getApigeeHost(isSaaS.equalsIgnoreCase("true") ? "saas" : "onprem", org)
					+ "v1/organizations/" + org + "/environments/" + env + "/virtualhosts/" + vHostName;
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			headers.set("Authorization",
					apigeeUtil.getApigeeAuth(org, isSaaS.equalsIgnoreCase("true") ? "saas" : "onprem"));
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
			ResponseEntity<VirtualHost> response = restTemplate.exchange(apigeeURL, HttpMethod.GET, requestEntity,
					VirtualHost.class);
			VirtualHost virtualHost = response.getBody();
			if (virtualHost != null) {
				List<String> hosts = new ArrayList<String>();
				for (String hAlias : virtualHost.getHostAliases()) {
					String host = ((virtualHost.getsSLInfo() != null
							&& virtualHost.getsSLInfo().getEnabled().equalsIgnoreCase("true")) ? "https" : "http")
							+ "://" + hAlias + ":" + virtualHost.getPort();
					hosts.add(host);
					log.info(host);
				}
				return hosts;
			}
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return null;
	}

	//
	// public boolean updateCategory(<Category> category) throws ItorixException
	// {
	// try{
	// Query query = new Query(Criteria.where("name").is(category.getName()));
	// DBObject dbDoc = new BasicDBObject();
	// mongoTemplate.getConverter().write(category, dbDoc);
	// Update update = Update.fromDBObject(dbDoc, "_id");
	// WriteResult result = mongoTemplate.updateFirst(query, update,
	// Category.class);
	// if( result.isUpdateOfExisting())
	// return result.isUpdateOfExisting();
	// else
	// throw new ItorixException("No Record exists","ProxyGen-1002" );
	// }
	// catch(ItorixException ex){
	// throw ex;
	// }
	// catch(Exception ex){
	// throw new ItorixException(ex.getMessage(),"ProxyGen-1000", ex );
	// }
	// }
	//
	public boolean deleteCategory(Category category) throws ItorixException {
		try {
			Query query = new Query(Criteria.where("name").is(category.getName()));
			DeleteResult result = mongoTemplate.remove(query, Category.class);
			if (result.getDeletedCount() > 0)
				return result.wasAcknowledged();
			else
				throw new ItorixException("No Record exists", "ProxyGen-1002");
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "ProxyGen-1000", ex);
		}
	}

	private Folder loadStructure() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		String dbFolder = mongoConnection.getFolder();
		Folder api = mapper.readValue(dbFolder, Folder.class);
		return api;
	}

	/**
	 * proxySearch
	 *
	 * @param interactionid
	 * 
	 * @return
	 * 
	 * @throws JsonProcessingException
	 */
	public Object proxySearch(String interactionid, String name, int limit)
			throws ItorixException, JsonProcessingException {
		BasicQuery query = new BasicQuery("{\"proxyName\": {$regex : '" + name + "', $options: 'i'}}");
		query.limit(limit > 0 ? limit : 10);
		List<ProxyData> allProxys = mongoTemplate.find(query, ProxyData.class);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode response = mapper.createObjectNode();
		ArrayNode responseFields = mapper.createArrayNode();
		for (ProxyData vo : allProxys) {
			SearchItem item = new SearchItem();
			item.setId(vo.getId());
			item.setName(vo.getProxyName());
			responseFields.addPOJO(item);
		}
		response.set("proxies", responseFields);
		return response;
	}
}
