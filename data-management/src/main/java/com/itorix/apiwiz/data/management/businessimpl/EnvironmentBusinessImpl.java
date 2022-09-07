package com.itorix.apiwiz.data.management.businessimpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.zeroturnaround.zip.ZipUtil;

import com.amazonaws.regions.Regions;
import com.itorix.apiwiz.common.model.Constants;
import com.itorix.apiwiz.common.model.apigee.ApigeeServiceUser;
import com.itorix.apiwiz.common.model.apigee.CommonConfiguration;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.integrations.jfrog.JfrogIntegration;
import com.itorix.apiwiz.common.model.integrations.s3.S3Integration;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.service.GridFsRepository;
import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.common.util.apigeeX.ApigeeXUtill;
import com.itorix.apiwiz.common.util.artifatory.JfrogUtilImpl;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.common.util.json.JSONUtil;
import com.itorix.apiwiz.common.util.s3.S3Utils;
import com.itorix.apiwiz.data.management.business.EnvironmentBusiness;
import com.itorix.apiwiz.data.management.dao.IntegrationsDataDao;
import com.itorix.apiwiz.data.management.model.BackupInfo;
import com.itorix.apiwiz.data.management.model.EnvironmentBackUpInfo;
import com.itorix.apiwiz.data.management.model.ProxyBackUpInfo;
import com.itorix.apiwiz.data.management.model.ResourceBackUpInfo;
import com.itorix.apiwiz.data.management.model.RestoreProxyInfo;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.model.UserSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
@Slf4j
@Service
public class EnvironmentBusinessImpl implements EnvironmentBusiness {
	private static final Logger logger = LoggerFactory.getLogger(EnvironmentBusinessImpl.class);
	@Autowired
	BaseRepository baseRepository;
	@Autowired
	ApigeeUtil apigeeUtil;
	@Autowired
	private ApigeeXUtill apigeeXUtil;
	@Autowired
	JfrogUtilImpl jfrogUtil;
	@Autowired
	private IntegrationsDataDao integrationsDao;
	@Autowired
	private S3Utils s3Utils;
	@Autowired
	GridFsRepository gridFsRepository;

	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired
	OrganizationBusinessImpl organizationService;

	/**
	 * doEnvironmentBackUp
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public BackupInfo doEnvironmentBackUp(CommonConfiguration cfg) throws Exception {
		logger.debug("EnvironmentService.doEnvironmentBackUp : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization());
		long start = System.currentTimeMillis();
		BackupInfo backupInfo = null;
		logger.debug("Inside the EnvironmentService.doEnvironmentBackUp(" + cfg + ")");
		EnvironmentBackUpInfo environmentBackUpInfo = new EnvironmentBackUpInfo();
		if (cfg.getBackUpLocation() == null || cfg.getBackUpLocation() == "") {
			cfg.setBackUpLocation(applicationProperties.getBackupDir() + start);
			File backupDirectory = new File(applicationProperties.getBackupDir() + start);
			backupDirectory.mkdirs();
		}
		JSONObject apiProxyInfo = null;
		JSONObject productsInfo = null;
		JSONArray resourcesInfo = null;
		JSONArray appsInfo = null;
		String developersInfo = null;
		org.json.JSONObject obj = null;
		String downloadURI = "";
		if (null != cfg.getSelectedEnvironments()) {
			logger.debug("Performing environment backup");
			environmentBackUpInfo.setOrganization(cfg.getOrganization());
			environmentBackUpInfo.setStatus(Constants.STATUS_INPROGRESS);
			environmentBackUpInfo.setOperationId(cfg.getOperationId());
			environmentBackUpInfo = baseRepository.save(environmentBackUpInfo);
			apiProxyInfo = apigeeEnvironmentBackUP(cfg);
			resourcesInfo = backupResource(cfg);
			productsInfo = backupAPIProductsOnEnvironments(cfg);
			appsInfo = backupAppsOnProductBasis(cfg);
			developersInfo = backupAppDevelopers(cfg);
			ZipUtil.pack(new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization()),
					new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip"));

			try {
				JfrogIntegration jfrogIntegration = getJfrogIntegration();
				S3Integration s3Integration = getS3Integration();
				if (null != s3Integration) {
					downloadURI = s3Utils
							.uplaodFile(s3Integration.getKey(), s3Integration.getDecryptedSecret(),
									Regions.fromName(s3Integration.getRegion()), s3Integration.getBucketName(),
									"restore-backup/" + cfg.getOrganization() + "/" + start + "/"
											+ cfg.getOrganization() + ".zip",
									cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip");
				} else if (null != jfrogIntegration) {
					obj = jfrogUtil.uploadFiles(cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip",
							applicationProperties.getDataRestoreBackup(),
							jfrogIntegration.getHostURL() + "/artifactory/",
							"restore-backup/" + cfg.getOrganization() + "/" + start + "",
							jfrogIntegration.getUsername(), jfrogIntegration.getPassword());
					downloadURI = (String) obj.get("downloadURI");
				}
				// obj = jfrogUtil.uploadFiles(cfg.getBackUpLocation() + "/" +
				// cfg.getOrganization() + ".zip",
				// applicationProperties.getDataRestoreBackup(),
				// applicationProperties.getJfrogHost() + ":" +
				// applicationProperties.getJfrogPort()
				// + "/artifactory/",
				// "restore-backup/" + cfg.getOrganization() + "/" + start + "",
				// applicationProperties.getJfrogUserName(),
				// applicationProperties.getJfrogPassword());
			} catch (Exception e) {
				log.error("Exception occurred", e);
				throw e;
			}
		}
		FileUtils.cleanDirectory(new File(cfg.getBackUpLocation()));
		FileUtils.deleteDirectory(new File(cfg.getBackUpLocation()));
		if (StringUtils.isNotEmpty(downloadURI)) {
			long end = System.currentTimeMillis();
			long backupTimeTaken = (end - start) / 1000l;
			cfg.setProxyInfo(apiProxyInfo);
			cfg.setResourceInfo(resourcesInfo);
			cfg.setDevelopersInfo(developersInfo);
			cfg.setProductsInfo(productsInfo);
			cfg.setAppsInfo(appsInfo);
			environmentBackUpInfo.setJfrogUrl(downloadURI);
			environmentBackUpInfo.setTimeTaken(backupTimeTaken);
			environmentBackUpInfo.setEnvProxyInfo(apiProxyInfo);
			environmentBackUpInfo.setResourceInfo(resourcesInfo);
			environmentBackUpInfo.setProductsInfo(productsInfo);
			environmentBackUpInfo.setAppsInfo(appsInfo);
			environmentBackUpInfo.setDevelopersInfo(developersInfo);
			environmentBackUpInfo.setStatus("Completed");
			environmentBackUpInfo = baseRepository.save(environmentBackUpInfo);
			backupInfo = environmentBackUpInfo;
		}
		return backupInfo;
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

	/**
	 * getBaseBackupDirectory
	 *
	 * @param rollover
	 * @param cfg
	 * 
	 * @return
	 */
	private File getBaseBackupDirectory(boolean rollover, CommonConfiguration cfg) {
		File backupDirectory = new File(
				cfg.getBackUpLocation() + File.separator + cfg.getOrganization() + "/backups/" + cfg.getOrganization());

		if (rollover && backupDirectory.exists()) {
			backupDirectory.renameTo(new File(backupDirectory.getAbsolutePath() + "__" + System.currentTimeMillis()));
		}

		backupDirectory.mkdirs();
		return backupDirectory;
	}

	/**
	 * backupProxies
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public BackupInfo backupProxies(CommonConfiguration cfg) throws Exception {
		logger.debug("EnvironmentService.backupProxies : interactionid=" + cfg.getInteractionid() + ": jsessionid="
				+ cfg.getJsessionId() + " : organization =" + cfg.getOrganization());
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		long start = System.currentTimeMillis();
		BackupInfo backupInfo = null;
		if (cfg.getBackUpLocation() == null || cfg.getBackUpLocation() == "") {
			cfg.setBackUpLocation(applicationProperties.getBackupDir() + start);
		}
		JSONObject apiProxyInfo = null;
		ProxyBackUpInfo proxyBackUpInfo = new ProxyBackUpInfo();
		proxyBackUpInfo.setOrganization(cfg.getOrganization());
		proxyBackUpInfo.setStatus(Constants.STATUS_INPROGRESS);
		proxyBackUpInfo.setBackUpLevel(Constants.APIGEE_ENVIRONMENT_BACKUP);
		proxyBackUpInfo.setOperationId(cfg.getOperationId());
		proxyBackUpInfo = baseRepository.save(proxyBackUpInfo);
		apiProxyInfo = apigeeEnvironmentBackUP(cfg);
		ZipUtil.pack(new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization()),
				new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip"));
		org.json.JSONObject obj = null;
		String downloadURI = "";
		try {
			JfrogIntegration jfrogIntegration = getJfrogIntegration();
			S3Integration s3Integration = getS3Integration();
			if (null != s3Integration) {
				downloadURI = s3Utils.uplaodFile(s3Integration.getKey(), s3Integration.getDecryptedSecret(),
						Regions.fromName(s3Integration.getRegion()), s3Integration.getBucketName(),
						"restore-backup/" + proxyBackUpInfo.getOrganization() + "/" + start + "/"
								+ cfg.getOrganization() + ".zip",
						cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip");
			} else if (null != jfrogIntegration) {
				obj = jfrogUtil.uploadFiles(cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip",
						applicationProperties.getDataRestoreBackup(), jfrogIntegration.getHostURL() + "/artifactory/",
						"restore-backup/" + proxyBackUpInfo.getOrganization() + "/" + start + "",
						jfrogIntegration.getUsername(), jfrogIntegration.getPassword());
				downloadURI = (String) obj.get("downloadURI");
			}
		} catch (Exception e) {
			log.error("Exception occurred", e);
			throw e;
		}
		FileUtils.cleanDirectory(new File(cfg.getBackUpLocation()));
		FileUtils.deleteDirectory(new File(cfg.getBackUpLocation()));
		if (StringUtils.isNotEmpty(downloadURI)) {
			long end = System.currentTimeMillis();
			long backupTimeTaken = (end - start) / 1000l;
			log.info("Total Time Taken: (sec): " + backupTimeTaken);
			proxyBackUpInfo.setJfrogUrl(downloadURI);
			proxyBackUpInfo.setTimeTaken(backupTimeTaken);
			proxyBackUpInfo.setProxyInfo(apiProxyInfo);
			proxyBackUpInfo.setStatus(Constants.STATUS_COMPLETED);
			proxyBackUpInfo = baseRepository.save(proxyBackUpInfo);
			backupInfo = proxyBackUpInfo;
		}
		return backupInfo;
	}

	/**
	 * getEnvironmentDepolyedProxies
	 *
	 * @param cfg
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<String> getEnvironmentDepolyedProxies(CommonConfiguration cfg) throws Exception {
		logger.debug("EnvironmentService.getEnvironmentDepolyedProxies : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		List<String> envApiList = new ArrayList<String>();
		if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
			String serviceResponse = apigeeXUtil.getProxies(cfg.getOrganization());
			JSONObject envJsonObject = (JSONObject) JSONSerializer.toJSON(serviceResponse);
			JSONArray envApis = (JSONArray) envJsonObject.get("proxies");
			for (int j = 0; j < envApis.size(); j++) {
				JSONObject apiProxyObject = (JSONObject) envApis.get(j);
				String apiName = (String) apiProxyObject.get("name");
				envApiList.add(apiName);
			}
		} else {
			if (cfg.getApigeeEmail() == null && cfg.getApigeePassword() == null) {
				ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(),
						cfg.getType());
				cfg.setApigeeEmail(apigeeServiceUser.getUserName());
				cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
			}
			cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
			String apisString = apigeeUtil.getAPIsDeployedToEnvironment(cfg);
			JSONObject envJsonObject = (JSONObject) JSONSerializer.toJSON(apisString);
			JSONArray envApis = (JSONArray) envJsonObject.get("aPIProxy");
			for (int i = 0; i < envApis.size(); i++) {
				JSONObject apiProxyObject = (JSONObject) envApis.get(i);
				String apiName = (String) apiProxyObject.get("name");
				envApiList.add(apiName);
			}
		}
		return envApiList;
	}

	/**
	 * apigeeEnvironmentBackUP
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws IOException
	 * @throws ItorixException
	 */
	private JSONObject apigeeEnvironmentBackUP(CommonConfiguration cfg) throws IOException, ItorixException {
		logger.debug(
				"EnvironmentService.apigeeEnvironmentBackUP : interactionid=" + cfg.getInteractionid() + ": jsessionid="
						+ cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg =" + cfg);
		List<String> envApiList = new ArrayList<String>();
		if (cfg.getApigeeEmail() == null && cfg.getApigeePassword() == null) {
			ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(),
					cfg.getType());
			cfg.setApigeeEmail(apigeeServiceUser.getUserName());
			cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		}
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		JSONObject proxiesData = new JSONObject();
		JSONArray skippedProxiesData = new JSONArray();
		JSONArray envProxies = new JSONArray();
		long start = System.currentTimeMillis();
		logger.debug("Inside the EnvironmentService.apigeeEnvironmentBackUP(" + cfg + ")");
		if (cfg.getBackUpLocation() == null || cfg.getBackUpLocation() == "") {
			cfg.setBackUpLocation(applicationProperties.getBackupDir() + start);
		}
		final File backupLocation = new File(getBaseBackupDirectory(false, cfg), "apiproxies");
		backupLocation.mkdirs();
		for (String environment : cfg.getSelectedEnvironments()) {
			cfg.setEnvironment(environment);
			String apisString = apigeeUtil.getAPIsDeployedToEnvironment(cfg);
			JSONObject envJsonObject = (JSONObject) JSONSerializer.toJSON(apisString);
			JSONArray envApis = (JSONArray) envJsonObject.get("aPIProxy");
			JSONObject apiContext = new JSONObject();
			for (int i = 0; i < envApis.size(); i++) {
				JSONObject apiProxyObject = (JSONObject) envApis.get(i);
				String apiName = (String) apiProxyObject.get("name");
				cfg.setApiName(apiName);
				envApiList.add(apiName);
				JSONArray apiRevisionArray = (JSONArray) apiProxyObject.get("revision");
				JSONObject envObj = new JSONObject();
				cfg.setEnvironment(environment);
				envObj.put("environment", environment);
				List<String> revisionList = apigeeUtil.getAnAPIProxyRevisionList(cfg);
				JSONArray deployProxies = new JSONArray();
				for (Object object : apiRevisionArray) {
					JSONObject revisionObj = (JSONObject) object;
					String revision = revisionObj.getString("name");
					cfg.setRevision(revision);
					final File versionFile = new File(backupLocation,
							File.separator + environment + File.separator + apiName + File.separator + revision);
					versionFile.getParentFile().mkdirs();
					byte[] revisionBundle = apigeeUtil.getAnAPIProxyRevision(cfg);
					FileUtils.writeByteArrayToFile(new File(versionFile.getAbsolutePath() + ".zip"), revisionBundle);
					envObj.put("revision", revision);
					deployProxies.add(envObj);
					apiContext.put("environments", deployProxies);
					apiContext.put("maxversion", Collections.max(revisionList));
					if (cfg.getIsCleanUpAreBackUp()) {
						apigeeUtil.forceUndeployAPIProxy(cfg);
						apigeeUtil.deleteAPIProxyRevision(cfg);
					}
				}

				JSONObject apiProxyContext = new JSONObject();
				apiProxyContext.put(apiName, apiContext);
				envProxies.add(apiProxyContext);
				// create context file for proxy
				createContextFile(apiContext, apiName, environment, backupLocation);
			}
		}
		cfg.setEnvApiList(envApiList);
		proxiesData.put("PROXIES", envProxies);
		proxiesData.put("SKIPPEDPROXIES", skippedProxiesData);
		return proxiesData;
	}

	/**
	 * createContextFile
	 *
	 * @param apiContext
	 * @param apiProxyName
	 * @param environment
	 * @param backupLocation
	 */
	private void createContextFile(final JSONObject apiContext, final String apiProxyName, final String environment,
			final File backupLocation) {
		try {
			FileWriter file = new FileWriter(new File(backupLocation,
					File.separator + environment + File.separator + apiProxyName + File.separator + "context.json"));
			file.write(apiContext.toString());
			file.flush();
			file.close();
		} catch (IOException e) {
			log.error("Exception occurred", e);
		}
	}

	/**
	 * backupAPIProductsOnEnvironments
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws IOException
	 * @throws ItorixException
	 */
	private JSONObject backupAPIProductsOnEnvironments(CommonConfiguration cfg) throws IOException, ItorixException {
		logger.debug("EnvironmentService.backupAPIProductsOnEnvironments : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		List<String> envApiList = cfg.getEnvApiList();
		List<String> productList = apigeeUtil.listAPIProducts(cfg);
		JSONObject productsList = new JSONObject();
		JSONArray productsData = new JSONArray();
		for (String productName : productList) {
			cfg.setApiProductName(productName);
			try {
				String apiProduct = apigeeUtil.getAPIProduct(cfg);
				JSONObject json = (JSONObject) JSONSerializer.toJSON(apiProduct);
				JSONArray prxiesList = json.getJSONArray("proxies");
				boolean proxyExists = false;
				for (Object proxyObj : prxiesList) {
					String proxy = (String) proxyObj;
					if (envApiList.contains(proxy)) {
						productsData.add(productName);
						proxyExists = true;
					}
					if (proxyExists)
						break;
				}

				if (proxyExists) { // proxy is deployed in input env passed and

					File backupLocation = new File(getBaseBackupDirectory(false, cfg), "apiproducts");
					backupLocation.mkdirs();
					FileWriter file = new FileWriter(new File(backupLocation, productName + ".json"));
					file.write(apiProduct.toString());
					file.flush();
					file.close();

					// if cleanup delete the file
					if (cfg.getIsCleanUpAreBackUp()) {
						apigeeUtil.deleteAPIProduct(cfg);
					}
				}

			} catch (IOException e) {
				log.error("Exception occurred", e);
			}
		}
		cfg.setJsonArray(productsData);
		productsList.put("PRODUCTS", productsData);
		return productsList;
	}

	/**
	 * restoreEnvironment
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public String restoreEnvironment(CommonConfiguration cfg) throws Exception {
		logger.debug("EnvironmentService.restoreEnvironment : interactionid=" + cfg.getInteractionid() + ": jsessionid="
				+ cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg =" + cfg);
		long start = System.currentTimeMillis();
		// BackupInfo backupInfo = null;
		EnvironmentBackUpInfo environmentBackUpInfo = new EnvironmentBackUpInfo();
		environmentBackUpInfo.setOrganization(cfg.getOrganization());
		environmentBackUpInfo.setStatus(Constants.STATUS_INPROGRESS);
		environmentBackUpInfo.setOperationId(cfg.getOperationId());
		environmentBackUpInfo = baseRepository.save(environmentBackUpInfo);
		restoreEnvironmentProxies(cfg);
		organizationService.restoreResource(cfg);
		organizationService.restoreAPIProducts(cfg);
		organizationService.restoreAPP(cfg);
		long end = System.currentTimeMillis();
		long backupTimeTaken = (end - start) / 1000l;
		environmentBackUpInfo.setTimeTaken(backupTimeTaken);
		environmentBackUpInfo.setStatus("Completed");
		environmentBackUpInfo = baseRepository.save(environmentBackUpInfo);
		// backupInfo = environmentBackUpInfo;
		return "SUCCESS";
	}

	/**
	 * restoreAPIProxies1
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ItorixException
	 */
	public String restoreAPIProxies1(CommonConfiguration cfg)
			throws IOException, InterruptedException, ItorixException {
		logger.debug("EnvironmentService.restoreAPIProxies1 : interactionid=" + cfg.getInteractionid() + ": jsessionid="
				+ cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg =" + cfg);
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		organizationService.downloadBackup1(cfg);
		File apiproxiesDir = new File(getBaseRestoreDirectory(cfg), "apiproxies");
		if (apiproxiesDir != null && apiproxiesDir.listFiles() != null && apiproxiesDir.listFiles().length > 0)
			for (File apiproxyDir : apiproxiesDir.listFiles()) {
				for (File envApiProxyDir : apiproxyDir.listFiles()) {
					apiproxyDir = envApiProxyDir;
					try {
						String apiProxyName = apiproxyDir.getName();
						cfg.setApiName(apiProxyName);
						if (apiProxyName.startsWith("."))
							continue;
						File deploymentsFile = new File(apiproxyDir, "context.json");
						String s = IOUtils.toString(new FileInputStream(deploymentsFile));
						ObjectMapper objMapper = new ObjectMapper();
						RestoreProxyInfo proxyInfo = new RestoreProxyInfo();
						proxyInfo = objMapper.readValue(s, RestoreProxyInfo.class);
						List<String> proxyVersions = proxyInfo.getVersions();
						for (String i : proxyVersions) {
							File revision = new File(apiproxyDir, i + ".zip");
							String deployedEnv = null;
							String deployedRev = null;
							try {
								cfg.setRevision(i);
								apigeeUtil.importApiProxy(cfg, revision);
								List<com.itorix.apiwiz.data.management.model.mappers.Environment> environments = proxyInfo
										.getEnvironments();
								for (com.itorix.apiwiz.data.management.model.mappers.Environment e : environments) {
									deployedEnv = e.getEnvironment();
									deployedRev = e.getRevision();
									if (null != cfg.getNewOrg() && cfg.getNewOrg().length() > 0)
										deployedEnv = cfg.getNewEnv();
									if (deployedRev.equals(i)) {
										// check if proxy is deployed in e if
										// yes
										// undeploy all
										cfg.setEnvironment(deployedEnv);
										organizationService.undeployProxyRevision(cfg, deployedEnv, apiProxyName, null);

										// get max revisions available
										List<Integer> revisionList = apigeeUtil.getRevisionsListForProxy(cfg);
										// Monetaization enabled proxies are not
										// being imported, hence cant be
										// deployed,
										// so adding below condition
										if (revisionList.size() > 0) {
											int maxRev = Collections.max(revisionList);
											cfg.setRevision(maxRev + "");
											cfg.setEnvironment(deployedEnv);
											// deploy revision maxRev, since
											// this is
											// latest uploaded
											apigeeUtil.deployAPIProxy(cfg);
										}
									}
								}

							} catch (IOException e) {
								log.error("Exception occurred", e);
							}
						}
					} catch (IOException e) {
						log.error("Exception occurred", e);
					}
				}
			}

		return "Success";
	}

	/**
	 * restoreEnvironmentProxies1
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ItorixException
	 */
	public BackupInfo restoreEnvironmentProxies1(CommonConfiguration cfg)
			throws IOException, InterruptedException, ItorixException {
		BackupInfo backupInfo = null;
		long start = System.currentTimeMillis();
		ProxyBackUpInfo proxyBackUpInfo = new ProxyBackUpInfo();
		proxyBackUpInfo.setOrganization(cfg.getOrganization());
		proxyBackUpInfo.setStatus(Constants.STATUS_INPROGRESS);
		proxyBackUpInfo.setBackUpLevel(Constants.APIGEE_ENVIRONMENT_BACKUP);
		proxyBackUpInfo.setOperationId(cfg.getOperationId());
		proxyBackUpInfo = baseRepository.save(proxyBackUpInfo);
		restoreEnvironmentProxies(cfg);
		long end = System.currentTimeMillis();
		long backupTimeTaken = (end - start) / 1000l;
		logger.debug("Total Time Taken: (sec): " + backupTimeTaken);
		proxyBackUpInfo.setTimeTaken(backupTimeTaken);
		proxyBackUpInfo.setStatus(Constants.STATUS_COMPLETED);
		proxyBackUpInfo = baseRepository.save(proxyBackUpInfo);
		backupInfo = proxyBackUpInfo;
		return backupInfo;
	}

	/**
	 * restoreEnvironmentProxies
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ItorixException
	 */
	public String restoreEnvironmentProxies(CommonConfiguration cfg)
			throws IOException, InterruptedException, ItorixException {
		logger.debug("EnvironmentService.restoreEnvironmentProxies : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		organizationService.downloadBackup1(cfg);
		File apiproxiesDir = new File(getBaseRestoreDirectory(cfg), "apiproxies");
		for (File apiproxyDir : apiproxiesDir.listFiles()) {
			for (File envApiProxyDir : apiproxyDir.listFiles()) {
				apiproxyDir = envApiProxyDir;
				try {
					String apiProxyName = apiproxyDir.getName();
					cfg.setApiName(apiProxyName);
					if (apiProxyName.startsWith("."))
						continue;
					File deploymentsFile = new File(apiproxyDir, "context.json");
					String s = IOUtils.toString(new FileInputStream(deploymentsFile));
					JSONObject apiContext = (JSONObject) JSONSerializer.toJSON(s);
					JSONArray environmentArray = (JSONArray) apiContext.get("environments");
					String revison = null;
					String environment = null;
					for (Object object : environmentArray) {
						JSONObject jsonObject = (JSONObject) object;
						revison = (String) jsonObject.get("revision");
						environment = (String) jsonObject.get("environment");
						cfg.setEnvironment(environment);
					}

					File fileRevision = new File(apiproxyDir, revison + ".zip");
					cfg.setRevision(revison);
					apigeeUtil.importApiProxy(cfg, fileRevision);
					// get proxy revisions list
					List<String> revisionList = apigeeUtil.getAnAPIProxyRevisionList(cfg);

					List<Integer> intList = revisionList.stream().map(Integer::valueOf).collect(Collectors.toList());
					int maxRevision = Collections.max(intList);
					// undeploy the revision
					undeployProxyRevision(cfg);
					cfg.setRevision(maxRevision + "");
					apigeeUtil.deployAPIProxy(cfg);

				} catch (IOException e) {
					log.error("Exception occurred", e);
				}
			}
		}

		return "SUCCESS";
	}

	/**
	 * backupAppsOnProductBasis
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	private JSONArray backupAppsOnProductBasis(CommonConfiguration cfg) throws ItorixException {
		logger.debug("EnvironmentService.backupAppsOnProductBasis : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		List<String> productList = JSONUtil.convertJSONObjectToList(cfg.getJsonArray());
		List<String> listOfAppIds = apigeeUtil.listAppIDsInAnOrganization(cfg);
		JSONArray appNames = new JSONArray();
		Set<String> developersToBackup = new HashSet<String>();
		List<String> appDevelopers = apigeeUtil.listDevelopers(cfg);
		Map<String, String> developersList = new HashMap<String, String>();
		for (String developerId : appDevelopers) {
			cfg.setDeveloperId(developerId);
			String developer = apigeeUtil.getDeveloper(cfg);
			JSONObject developerJson = (JSONObject) JSONSerializer.toJSON(developer);
			developersList.put(developerJson.getString("developerId"), developerJson.getString("email"));
		}
		for (String appID : listOfAppIds) {
			try {
				cfg.setAppID(appID);
				List<String> productsLinked = new ArrayList<String>();
				String appsString = apigeeUtil.getAppInAnOrganizationByAppID(cfg);
				JSONObject apps = (JSONObject) JSONSerializer.toJSON(appsString);
				JSONArray credentials = apps.getJSONArray("credentials");
				for (Object obj : credentials) {
					JSONObject credential = (JSONObject) obj;
					for (Object obj1 : credential.getJSONArray("apiProducts")) {
						JSONObject apiProduct = (JSONObject) obj1;
						productsLinked.add(apiProduct.getString("apiproduct"));
					}
				}
				boolean isProductExist = false;
				for (String tempProduct : productsLinked) {
					if (productList.contains(tempProduct)) {
						isProductExist = true;
					}
					if (isProductExist)
						break;
				}
				if (isProductExist) {

					developersToBackup.add(developersList.get(apps.getString("developerId")));
					String appName1 = apps.getString("name");
					appNames.add(appName1);
					cfg.setAppName(appName1);
					File backupLocation = new File(getBaseBackupDirectory(false, cfg), "apps");
					backupLocation.mkdirs();

					FileWriter file = new FileWriter(new File(backupLocation, appID + ".json"));
					file.write(appsString);
					file.flush();
					file.close();
					if (cfg.getIsCleanUpAreBackUp()) {
						apigeeUtil.deleteDeveloperApp(cfg, appName1, apps.getString("developerId"));
					}
				}
			} catch (IOException e) {
				log.error("Exception occurred", e);
			}
		}
		return appNames;
	}

	/**
	 * undeployProxyRevision
	 *
	 * @param cfg
	 * 
	 * @throws IOException
	 * @throws ItorixException
	 */
	private void undeployProxyRevision(CommonConfiguration cfg) throws IOException, ItorixException {
		try {
			if (null != cfg.getRevision()) {
				apigeeUtil.forceUndeployAPIProxy(cfg);
			} else {
				// get proxy deployments
				String proxyDeploymentURL = apigeeUtil.getAPIProxyDeploymentDetail1s(cfg);
				ObjectMapper o2 = new ObjectMapper();
				JsonNode targetNode = o2.readTree(proxyDeploymentURL);
				JSONObject targetdeployedEnv = (JSONObject) JSONSerializer.toJSON(targetNode.toString());
				if (targetdeployedEnv.containsKey("revision")) {
					// check rev deployments and undeploy them
					JSONArray revisionArray = targetdeployedEnv.getJSONArray("revision");

					for (Object tarRevObj : revisionArray) {
						JSONObject temp1 = (JSONObject) tarRevObj;
						String targetEnvNameDeployed1 = temp1.getString("name");
						cfg.setApiName(targetEnvNameDeployed1);
						apigeeUtil.forceUndeployAPIProxy(cfg);
					}
				}
			}

		} catch (IOException e) {
			log.error("Exception occurred", e);
		}
	}

	/**
	 * backupAppDevelopers
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws IOException
	 * @throws ItorixException
	 */
	private String backupAppDevelopers(CommonConfiguration cfg) throws IOException, ItorixException {
		logger.debug(
				"EnvironmentService.backupAppDevelopers : interactionid=" + cfg.getInteractionid() + ": jsessionid="
						+ cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg =" + cfg);
		List<String> developers = new ArrayList<String>();
		if (null == cfg.getDevelopersToBackup()) {
			developers = apigeeUtil.listDevelopers(cfg);
		} else {
			developers = new ArrayList<String>(cfg.getDevelopersToBackup());
		}
		for (String appDeveloper : developers) {
			try {
				cfg.setDeveloperId(appDeveloper);
				String appDeveloperString = apigeeUtil.getDeveloper(cfg).toString();
				File backupLocation = new File(getBaseBackupDirectory(false, cfg), "developers");
				backupLocation.mkdirs();
				FileWriter file = new FileWriter(new File(backupLocation, appDeveloper + ".json"));
				file.write(appDeveloperString);
				file.flush();
				file.close();
			} catch (IOException e) {
				log.error("Exception occurred", e);
			}

			if (cfg.getIsCleanUpAreBackUp()) {
				apigeeUtil.deleteDeveloper(cfg, appDeveloper);
			}
		}
		return developers.toString();
	}

	/**
	 * backupResources
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public BackupInfo backupResources(CommonConfiguration cfg) throws Exception {
		logger.debug("EnvironmentService.backupResources : interactionid=" + cfg.getInteractionid() + ": jsessionid="
				+ cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg =" + cfg);
		long start = System.currentTimeMillis();
		BackupInfo backupInfo = null;
		if (cfg.getBackUpLocation() == null || cfg.getBackUpLocation() == "") {
			cfg.setBackUpLocation(applicationProperties.getBackupDir() + start);
		}
		String oid = null;
		JSONArray resourcesInfo = null;

		ResourceBackUpInfo resourceBackupInfo = new ResourceBackUpInfo();
		resourceBackupInfo.setOrganization(cfg.getOrganization());
		resourceBackupInfo.setStatus(Constants.STATUS_INPROGRESS);
		resourceBackupInfo = baseRepository.save(resourceBackupInfo);
		resourcesInfo = backupResource(cfg);
		ZipUtil.pack(new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization()),
				new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip"));
		org.json.JSONObject obj = null;
		String downloadURI = "";
		try {
			JfrogIntegration jfrogIntegration = getJfrogIntegration();
			S3Integration s3Integration = getS3Integration();
			if (null != s3Integration) {
				downloadURI = s3Utils.uplaodFile(s3Integration.getKey(), s3Integration.getDecryptedSecret(),
						Regions.fromName(s3Integration.getRegion()), s3Integration.getBucketName(),
						"restore-backup/" + cfg.getOrganization() + "/" + start + "/" + cfg.getOrganization() + ".zip",
						cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip");
			} else if (null != jfrogIntegration) {
				obj = jfrogUtil.uploadFiles(cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip",
						applicationProperties.getDataRestoreBackup(), jfrogIntegration.getHostURL() + "/artifactory/",
						"restore-backup/" + cfg.getOrganization() + "/" + start + "", jfrogIntegration.getUsername(),
						jfrogIntegration.getPassword());
				downloadURI = (String) obj.get("downloadURI");
			}
			// obj = jfrogUtil.uploadFiles(cfg.getBackUpLocation() + "/" +
			// cfg.getOrganization() + ".zip",
			// applicationProperties.getDataRestoreBackup(),
			// applicationProperties.getJfrogHost() + ":" +
			// applicationProperties.getJfrogPort() + "/artifactory/",
			// "restore-backup/" + cfg.getOrganization() + "/" + start + "",
			// applicationProperties.getJfrogUserName(),
			// applicationProperties.getJfrogPassword());
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		FileUtils.cleanDirectory(new File(cfg.getBackUpLocation()));
		FileUtils.deleteDirectory(new File(cfg.getBackUpLocation()));
		if (StringUtils.isNotEmpty(downloadURI)) {
			long end = System.currentTimeMillis();
			long backupTimeTaken = (end - start) / 1000l;
			resourceBackupInfo.setJfrogUrl(downloadURI);
			resourceBackupInfo.setTimeTaken(backupTimeTaken);
			resourceBackupInfo.setResourceInfo(resourcesInfo);
			resourceBackupInfo.setStatus(Constants.STATUS_COMPLETED);
			resourceBackupInfo = baseRepository.save(resourceBackupInfo);
			backupInfo = resourceBackupInfo;
		}
		return backupInfo;
	}

	/**
	 * backupResource
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	public JSONArray backupResource(CommonConfiguration cfg) throws ItorixException {
		logger.debug("EnvironmentService.backupResource : interactionid=" + cfg.getInteractionid() + ": jsessionid="
				+ cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg =" + cfg);
		long start = System.currentTimeMillis();
		if (cfg.getBackUpLocation() == null || cfg.getBackUpLocation() == "") {
			cfg.setBackUpLocation(applicationProperties.getBackupDir() + start);
		}
		JSONArray resourcesList = new JSONArray();
		JSONArray envCaches = null;
		JSONArray envKVM = null;
		JSONArray envTargetServers = null;

		envCaches = backupCaches(cfg);
		resourcesList.addAll(envCaches);
		envKVM = backupKVM(cfg);
		resourcesList.addAll(envKVM);
		envTargetServers = backupTargetServers(cfg);
		resourcesList.addAll(envTargetServers);
		return resourcesList;
	}

	/**
	 * backupCaches
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	private JSONArray backupCaches(CommonConfiguration cfg) throws ItorixException {
		logger.debug("EnvironmentService.backupCaches : interactionid=" + cfg.getInteractionid() + ": jsessionid="
				+ cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg =" + cfg);
		long start = System.currentTimeMillis();
		if (cfg.getBackUpLocation() == null || cfg.getBackUpLocation() == "") {
			cfg.setBackUpLocation(applicationProperties.getBackupDir() + start);
		}
		List<String> environments = null;
		if (null == cfg.getSelectedEnvironments()) {
			environments = apigeeUtil.getEnvironmentNames(cfg);
			cfg.setSelectedEnvironments(environments);
		} else {
			environments = cfg.getSelectedEnvironments();
		}
		File environmentsDir = new File(getBaseBackupDirectory(false, cfg), "environments");
		environmentsDir.mkdirs();
		JSONArray resourcesList = new JSONArray();
		for (String environment : environments) {
			JSONObject resourceData = new JSONObject();
			JSONObject envList = new JSONObject();
			File environmentDir = new File(environmentsDir, environment);
			environmentDir.mkdirs();
			final File cachesDir = new File(environmentDir, "caches");
			cachesDir.mkdirs();
			cfg.setEnvironment(environment);
			List<String> caches = apigeeUtil.listCachesInAnEnvironment(cfg);
			resourceData.put("CACHES", caches);
			for (String cacheName : caches) {
				// If string contains spaces, it will not allow to process.
				try {
					cfg.setCacheName(cacheName);
					String cache = apigeeUtil.getInformationAboutACache(cfg);
					FileWriter file = new FileWriter(new File(cachesDir, cacheName + ".json"));
					file.write(cache);
					file.flush();
					file.close();

					if (cfg.getIsCleanUpAreBackUp()) {
						apigeeUtil.deleteCache(cfg);
					}

				} catch (IOException e) {
					log.error("Exception occurred", e);
				}
			}
			envList.put(environment, resourceData);
			resourcesList.add(envList);
		}

		return resourcesList;
	}

	/**
	 * backupKVM
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	private JSONArray backupKVM(CommonConfiguration cfg) throws ItorixException {
		logger.debug("EnvironmentService.backupKVM : interactionid=" + cfg.getInteractionid() + ": jsessionid="
				+ cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg =" + cfg);
		long start = System.currentTimeMillis();
		if (cfg.getBackUpLocation() == null || cfg.getBackUpLocation() == "") {
			cfg.setBackUpLocation(applicationProperties.getBackupDir() + start);
		}
		List<String> environments = null;
		if (null == cfg.getSelectedEnvironments()) {
			environments = apigeeUtil.getEnvironmentNames(cfg);
			cfg.setSelectedEnvironments(environments);
		} else {
			environments = cfg.getSelectedEnvironments();
		}
		File environmentsDir = new File(getBaseBackupDirectory(false, cfg), "environments");
		environmentsDir.mkdirs();
		JSONArray resourcesList = new JSONArray();
		for (String env : environments) {
			String environment = (String) env;
			JSONObject resourceData = new JSONObject();
			JSONObject envList = new JSONObject();
			File environmentDir = new File(environmentsDir, environment);
			environmentDir.mkdirs();
			final File keyValueMapsDir = new File(environmentDir, "keyvaluemaps");
			keyValueMapsDir.mkdirs();
			List<String> keyValueMaps = apigeeUtil.listKeyValueMapsInAnEnvironment(cfg);
			JSONArray keyValues = (JSONArray) JSONSerializer.toJSON(keyValueMaps);
			resourceData.put("KV", keyValues);
			for (String keyName : keyValueMaps) {
				// If string contains spaces, it will not allow to process..
				cfg.setKeyValueMapName(keyName);
				String keyValue = apigeeUtil.getKeyValueMapInAnEnvironment(cfg);
				FileWriter file;
				try {
					file = new FileWriter(new File(keyValueMapsDir, keyName.toString() + ".json"));
					file.write(keyValue);
					file.flush();
					file.close();
				} catch (IOException e) {
					log.error("Exception occurred", e);
				}

				if (cfg.getIsCleanUpAreBackUp()) {
					apigeeUtil.deleteKeyValueMapInAnEnvironment(cfg);
				}
			}
			envList.put(environment, resourceData);
			resourcesList.add(envList);
		}
		return resourcesList;
	}

	/**
	 * backupTargetServers
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	private JSONArray backupTargetServers(CommonConfiguration cfg) throws ItorixException {
		logger.debug(
				"EnvironmentService.backupTargetServers : interactionid=" + cfg.getInteractionid() + ": jsessionid="
						+ cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg =" + cfg);
		long start = System.currentTimeMillis();
		if (cfg.getBackUpLocation() == null || cfg.getBackUpLocation() == "") {
			cfg.setBackUpLocation(applicationProperties.getBackupDir() + start);
		}
		List<String> environments = null;
		if (null == cfg.getSelectedEnvironments()) {
			environments = apigeeUtil.getEnvironmentNames(cfg);
			cfg.setSelectedEnvironments(environments);
		} else {
			environments = cfg.getSelectedEnvironments();
		}
		File environmentsDir = new File(getBaseBackupDirectory(false, cfg), "environments");
		environmentsDir.mkdirs();

		JSONArray resourcesList = new JSONArray();

		for (String env : environments) {
			String environment = (String) env;
			JSONObject resourceData = new JSONObject();
			JSONObject envList = new JSONObject();

			File environmentDir = new File(environmentsDir, environment);
			environmentDir.mkdirs();

			final File targetServerDir = new File(environmentDir, "targetservers");
			targetServerDir.mkdirs();

			List<String> targetServers = apigeeUtil.listTargetServersInAnEnvironment(cfg);
			JSONArray skippedTargetServers = new JSONArray();
			resourceData.put("TARGETSERVERS", targetServers);
			for (String targetServer : targetServers) {
				cfg.setTargetServerName(targetServer);
				String serverDetails = apigeeUtil.getTargetServer(cfg);
				FileWriter file;
				try {
					file = new FileWriter(new File(targetServerDir, targetServer.toString() + ".json"));
					file.write(serverDetails);
					file.flush();
					file.close();
				} catch (IOException e) {
					log.error("Exception occurred", e);
				}

				if (cfg.getIsCleanUpAreBackUp()) {
					apigeeUtil.deleteTargetServer(cfg);
				}
			}
			resourceData.put("SKIPPEDTARGETSERVERS", skippedTargetServers);

			envList.put(environment, resourceData);
			resourcesList.add(envList);
		}
		return resourcesList;
	}

	/**
	 * getBaseRestoreDirectory
	 *
	 * @param cfg
	 * 
	 * @return
	 */
	private File getBaseRestoreDirectory(CommonConfiguration cfg) {
		File backupDirectory = new File(
				cfg.getBackUpLocation() + File.separator + cfg.getOrganization() + "/backups/" + cfg.getOrganization());
		return backupDirectory;
	}

	/**
	 * getEnvironmentBackupHistory
	 *
	 * @param interactionid
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public List<EnvironmentBackUpInfo> getEnvironmentBackupHistory(String interactionid) throws Exception {
		logger.debug("EnvironmentService.getOrganizationBackupHistory : CorelationId= " + interactionid);
		List<EnvironmentBackUpInfo> list = new LinkedList<>();
		list = baseRepository.findAll(EnvironmentBackUpInfo.LABEL_CREATED_TIME, "-", EnvironmentBackUpInfo.class);
		return list;
	}

	/**
	 * getApiproxiesBackupHistory
	 *
	 * @param backuplevel
	 * @param interactionid
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public <T> List<ProxyBackUpInfo> getApiproxiesBackupHistory(String backuplevel, String interactionid)
			throws Exception {
		logger.debug("EnvironmentService.getApiproxiesBackupHistory : CorelationId= " + interactionid
				+ " : backuplevel= " + backuplevel);
		List<ProxyBackUpInfo> list = new LinkedList<>();
		Query query = new Query(new Criteria()
				.andOperator(Criteria.where(ProxyBackUpInfo.LABEL_RESOURCE_BACKUP_LEVEL).is(backuplevel)));
		query.with(Sort.by(Sort.Direction.DESC, ProxyBackUpInfo.LABEL_CREATED_TIME));
		list = baseRepository.find(query, ProxyBackUpInfo.class);
		return list;
	}

	/**
	 * getCachesBackupHistory
	 *
	 * @param interactionid
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public List<ResourceBackUpInfo> getCachesBackupHistory(String interactionid) throws Exception {
		logger.debug("EnvironmentService.getCachesBackupHistory : CorelationId= " + interactionid);
		List<ResourceBackUpInfo> list = new LinkedList<>();
		Query query = new Query(
				new Criteria().andOperator(Criteria.where(ResourceBackUpInfo.LABEL_RESOURCE_TYPE).is("cache"),
						Criteria.where(ResourceBackUpInfo.LABEL_RESOURCE_BACKUP_LEVEL).is("environments")));
		query.with(Sort.by(Sort.Direction.DESC, ResourceBackUpInfo.LABEL_CREATED_TIME));
		list = baseRepository.find(query, ResourceBackUpInfo.class);
		return list;
	}

	/**
	 * getTargetServersBackupHistory
	 *
	 * @param interactionid
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public List<ResourceBackUpInfo> getTargetServersBackupHistory(String interactionid) throws Exception {
		logger.debug("EnvironmentService.getTargetServersBackupHistory : CorelationId= " + interactionid);
		List<ResourceBackUpInfo> list = new LinkedList<>();
		Query query = new Query(
				new Criteria().andOperator(Criteria.where(ResourceBackUpInfo.LABEL_RESOURCE_TYPE).is("targetserver"),
						Criteria.where(ResourceBackUpInfo.LABEL_RESOURCE_BACKUP_LEVEL).is("environments")));
		query.with(Sort.by(Sort.Direction.DESC, ResourceBackUpInfo.LABEL_CREATED_TIME));
		list = baseRepository.find(query, ResourceBackUpInfo.class);
		return list;
	}

	/**
	 * getKVMBackupHistory
	 *
	 * @param interactionid
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public List<ResourceBackUpInfo> getKVMBackupHistory(String interactionid) throws Exception {
		logger.debug("EnvironmentService.getKVMBackupHistory : CorelationId= " + interactionid);
		List<ResourceBackUpInfo> list = new LinkedList<>();
		Query query = new Query(
				new Criteria().andOperator(Criteria.where(ResourceBackUpInfo.LABEL_RESOURCE_TYPE).is("kvm"),
						Criteria.where(ResourceBackUpInfo.LABEL_RESOURCE_BACKUP_LEVEL).is("environments")));
		query.with(Sort.by(Sort.Direction.DESC, ResourceBackUpInfo.LABEL_CREATED_TIME));
		list = baseRepository.find(query, ResourceBackUpInfo.class);
		return list;
	}

	/*
	 * public Apigee getApigeeCredential(String jsessionid) { UserSession
	 * userSessionToken = baseRepository.findById(jsessionid,
	 * UserSession.class); User user =
	 * baseRepository.findById(userSessionToken.getUserId(), User.class); if
	 * (user != null) { Apigee apigee = user.getApigee(); return apigee; } else
	 * { return null; } }
	 */

}
