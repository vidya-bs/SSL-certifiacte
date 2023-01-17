package com.itorix.apiwiz.data.management.businessimpl;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.zeroturnaround.zip.ZipUtil;

import com.amazonaws.regions.Regions;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.common.factory.IntegrationHelper;
import com.itorix.apiwiz.common.model.Constants;
import com.itorix.apiwiz.common.model.GridFsData;
import com.itorix.apiwiz.common.model.apigee.Environment;
import com.itorix.apiwiz.common.model.apigee.*;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.integrations.jfrog.JfrogIntegration;
import com.itorix.apiwiz.common.model.integrations.s3.S3Integration;
import com.itorix.apiwiz.common.model.proxystudio.ProxyArtifacts;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.service.GridFsRepository;
import com.itorix.apiwiz.common.util.StorageIntegration;
import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.common.util.apigeeX.ApigeeXUtill;
import com.itorix.apiwiz.common.util.artifatory.JfrogUtilImpl;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.common.util.s3.S3Utils;
import com.itorix.apiwiz.data.management.business.OrganizationBusiness;
import com.itorix.apiwiz.data.management.dao.IntegrationsDataDao;
import com.itorix.apiwiz.data.management.model.BackupInfo;
import com.itorix.apiwiz.data.management.model.*;
import com.itorix.apiwiz.data.management.model.mappers.APIProduct;
import com.itorix.apiwiz.data.management.model.overview.ApigeeOrganizationalVO;
import com.itorix.apiwiz.data.management.model.overview.Apps;
import com.itorix.apiwiz.data.management.model.overview.Products;
import com.itorix.apiwiz.data.management.model.overview.Proxies;
import com.itorix.apiwiz.data.management.model.overview.Sharedflow;
import com.itorix.apiwiz.data.management.model.overview.Targetserver;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import net.sf.json.util.JSONUtils;
@Slf4j
@Service
public class OrganizationBusinessImpl implements OrganizationBusiness {
	private static final Logger logger = LoggerFactory.getLogger(OrganizationBusinessImpl.class);
	@Autowired
	private BaseRepository baseRepository;
	@Autowired
	private JfrogUtilImpl jfrogUtil;
	@Autowired
	private ApigeeUtil apigeeUtil;

	@Autowired
	private ApigeeXUtill apigeeXUtil;
	@Autowired
	private IntegrationsDataDao integrationsDao;
	@Autowired
	private S3Utils s3Utils;
	@Autowired
	private GridFsRepository gridFsRepository;

	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired
	private IntegrationHelper integrationHelper;

	/**
	 * This method is used to get the list of environments for an organization.
	 *
	 * @param jsessionid
	 * @param organization
	 * @param interactionid
	 * @param type
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	public List<String> getEnvironmentNames(String jsessionid, String organization, String interactionid, String type)
			throws ItorixException {
		logger.debug("OrganizationDataMigrationService.getEnvironmentNames : interactionid=" + interactionid
				+ ": jsessionid=" + jsessionid + " : organization =" + organization);
		List<String> envList = null;
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setType(type);
		cfg.setOrganization(organization);
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(organization, type);
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		cfg.setInteractionid(interactionid);
		envList = apigeeUtil.getEnvironmentNames(cfg);
		return envList;
	}

	/**
	 * Using this we will get the list of proxies for an organization.
	 *
	 * @param jsessionid
	 * @param organization
	 * @param interactionid
	 * @param type
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	public List<String> listAPIProxies(String jsessionid, String organization, String interactionid, String type)
			throws ItorixException {
		logger.debug("OrganizationDataMigrationService.listAPIProxies : interactionid=" + interactionid
				+ ": jsessionid=" + jsessionid + " : organization =" + organization);
		List<String> apisList = null;
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setType(type);
		cfg.setOrganization(organization);
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(organization, type);
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		cfg.setInteractionid(interactionid);
		apisList = apigeeUtil.listAPIProxies(cfg);
		return apisList;
	}

	/**
	 * This will return the deployed proxies for specified org & env.
	 *
	 * @param jsessionid
	 * @param organization
	 * @param environment
	 * @param interactionid
	 * @param type
	 * 
	 * @return
	 * @throws ItorixException
	 */
	public String getAPIsDeployedToEnvironment(String jsessionid, String organization, String environment,
			String interactionid, String type) throws ItorixException {
		logger.debug("OrganizationDataMigrationService.getAPIsDeployedToEnvironment");
		if (type != null && type.equalsIgnoreCase("apigeex")) {
			try {
				return apigeeXUtil.getProxies(organization);
			} catch (Exception e) {
				log.error("Exception occurred", e);
			}
			return null;
		} else {
			CommonConfiguration cfg = new CommonConfiguration();
			cfg.setType(type);
			cfg.setOrganization(organization);
			cfg.setEnvironment(environment);
			ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(organization, type);
			cfg.setApigeeEmail(apigeeServiceUser.getUserName());
			cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
			cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
			return apigeeUtil.getAPIsDeployedToEnvironment(cfg);
		}
	}

	public ProxyBackUpInfo scheduleBackupProxies(CommonConfiguration cfg) {
		ProxyBackUpInfo proxyBackUpInfo = new ProxyBackUpInfo();
		proxyBackUpInfo.setOrganization(cfg.getOrganization());
		proxyBackUpInfo.setStatus(Constants.STATUS_SCHEDULED);
		proxyBackUpInfo.setOperationId(cfg.getOperationId());
		proxyBackUpInfo = baseRepository.save(proxyBackUpInfo);

		BackupEvent backupEvent = new BackupEvent();
		backupEvent.setCfg(cfg);
		backupEvent.setEvent("BackupProxies");
		backupEvent.setStatus(Constants.STATUS_SCHEDULED);
		backupEvent.setEventId(proxyBackUpInfo.getId());
		baseRepository.save(backupEvent);
		return proxyBackUpInfo;
	}

	/**
	 * This method will do the backup of api's or proxies.
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public BackupInfo backupProxies(CommonConfiguration cfg, String id) throws Exception {
		logger.debug("OrganizationDataMigrationService.backupProxies : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg = "
				+ cfg);
		BackupInfo backupInfo = null;
		ProxyBackUpInfo proxyBackUpInfo;
		if (id != null) {
			proxyBackUpInfo = baseRepository.findById(id, ProxyBackUpInfo.class);
		} else
			proxyBackUpInfo = new ProxyBackUpInfo();
		try {
			ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(),
					cfg.getType());
			cfg.setApigeeEmail(apigeeServiceUser.getUserName());
			cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
			cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
			long start = System.currentTimeMillis();

			if (cfg.getBackUpLocation() == null || cfg.getBackUpLocation() == "") {
				cfg.setBackUpLocation(applicationProperties.getBackupDir() + start);
			}
			JSONObject appsInfo = null;
			JSONObject developersInfo = null;
			JSONObject productsInfo = null;
			JSONObject apiProxyInfo = null;
			JSONObject sharedflowInfo = null;

			proxyBackUpInfo.setOrganization(cfg.getOrganization());
			proxyBackUpInfo.setStatus(Constants.STATUS_INPROGRESS);
			proxyBackUpInfo.setOperationId(cfg.getOperationId());
			proxyBackUpInfo = baseRepository.save(proxyBackUpInfo);
			appsInfo = backupApps(cfg);
			developersInfo = backupAppDevelopers(cfg);
			productsInfo = backupAPIProducts(cfg);
			apiProxyInfo = backupAPIProxies(cfg);
			sharedflowInfo = backupSharedflow(cfg);
			ZipUtil.pack(new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization()),
					new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip"));
			org.json.JSONObject obj = null;
			String downloadURI = "";
			try {

				StorageIntegration storageIntegration = integrationHelper.getIntegration();
				downloadURI = storageIntegration.uploadFile(
						"Backup/" + cfg.getOrganization() + "/" + start + "/" + cfg.getOrganization() + ".zip",
						cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip");

			} catch (Exception e) {
				log.error("Exception occurred", e);
				throw e;
			}
			FileUtils.cleanDirectory(new File(cfg.getBackUpLocation()));
			FileUtils.deleteDirectory(new File(cfg.getBackUpLocation()));
			if (StringUtils.isNotEmpty(downloadURI)) {
				long end = System.currentTimeMillis();
				long backupTimeTaken = (end - start) / 1000l;
				logger.debug("Total Time Taken: (sec): " + backupTimeTaken);
				proxyBackUpInfo.setJfrogUrl(downloadURI);
				proxyBackUpInfo.setTimeTaken(backupTimeTaken);
				proxyBackUpInfo.setAppsInfo(appsInfo);
				proxyBackUpInfo.setDevelopersInfo(developersInfo);
				proxyBackUpInfo.setProductsInfo(productsInfo);
				proxyBackUpInfo.setProxyInfo(apiProxyInfo);
				proxyBackUpInfo.setSharedflowInfo(sharedflowInfo);
				proxyBackUpInfo.setStatus(Constants.STATUS_COMPLETED);
				proxyBackUpInfo = baseRepository.save(proxyBackUpInfo);
				backupInfo = proxyBackUpInfo;
			}
		} catch (Exception e) {
			proxyBackUpInfo.setStatus(Constants.STATUS_FAILED);
			proxyBackUpInfo = baseRepository.save(proxyBackUpInfo);
			backupInfo = proxyBackUpInfo;
			logger.error("OrganizationDataMigrationService.backupProxies : interactionid=" + cfg.getInteractionid()
					+ ": jsessionid=" + cfg.getJsessionId() + " : ERROR = " + e.getMessage());
			throw e;
		}
		return backupInfo;
	}

	public SharedflowBackUpInfo scheduleBackupSharedflows(CommonConfiguration cfg) {
		SharedflowBackUpInfo sharedflowBackUpInfo = new SharedflowBackUpInfo();
		sharedflowBackUpInfo.setOrganization(cfg.getOrganization());
		sharedflowBackUpInfo.setStatus(Constants.STATUS_SCHEDULED);
		sharedflowBackUpInfo.setOperationId(cfg.getOperationId());
		sharedflowBackUpInfo = baseRepository.save(sharedflowBackUpInfo);

		BackupEvent backupEvent = new BackupEvent();
		backupEvent.setCfg(cfg);
		backupEvent.setEvent("BackupSharedflow");
		backupEvent.setStatus(Constants.STATUS_SCHEDULED);
		backupEvent.setEventId(sharedflowBackUpInfo.getId());
		baseRepository.save(backupEvent);
		return sharedflowBackUpInfo;
	}

	/**
	 * This method will do the backup of shared flows. apigeex api to invoke and
	 * retrive the c
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public BackupInfo backupSharedflows(CommonConfiguration cfg, String id) throws Exception {
		logger.debug("OrganizationDataMigrationService.backupSharedflows : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization());
		BackupInfo backupInfo = null;
		SharedflowBackUpInfo sharedflowBackUpInfo;
		if (id != null) {
			sharedflowBackUpInfo = baseRepository.findById(id, SharedflowBackUpInfo.class);
		} else
			sharedflowBackUpInfo = new SharedflowBackUpInfo();
		try {
			ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(),
					cfg.getType());
			cfg.setApigeeEmail(apigeeServiceUser.getUserName());
			cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
			cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
			long start = System.currentTimeMillis();

			if (cfg.getBackUpLocation() == null || cfg.getBackUpLocation() == "") {
				cfg.setBackUpLocation(applicationProperties.getBackupDir() + start);
			}

			JSONObject sharedflowInfo = null;

			sharedflowBackUpInfo.setOrganization(cfg.getOrganization());
			sharedflowBackUpInfo.setStatus(Constants.STATUS_INPROGRESS);
			sharedflowBackUpInfo.setOperationId(cfg.getOperationId());
			sharedflowBackUpInfo = baseRepository.save(sharedflowBackUpInfo);
			sharedflowInfo = backupSharedflow(cfg);
			ZipUtil.pack(new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization()),
					new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip"));
			org.json.JSONObject obj = null;
			String downloadURI = "";
			try {

				StorageIntegration storageIntegration = integrationHelper.getIntegration();
				downloadURI = storageIntegration.uploadFile(
						"Backup/" + cfg.getOrganization() + "/" + start + "/" + cfg.getOrganization() + ".zip",
						cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip");

			} catch (Exception e) {
				log.error("Exception occurred", e);
				throw e;
			}
			FileUtils.cleanDirectory(new File(cfg.getBackUpLocation()));
			FileUtils.deleteDirectory(new File(cfg.getBackUpLocation()));
			if (StringUtils.isNotEmpty(downloadURI)) {
				long end = System.currentTimeMillis();
				long backupTimeTaken = (end - start) / 1000l;
				logger.debug("Total Time Taken: (sec): " + backupTimeTaken);
				sharedflowBackUpInfo.setJfrogUrl(downloadURI);
				sharedflowBackUpInfo.setTimeTaken(backupTimeTaken);
				sharedflowBackUpInfo.setSharedflowInfo(sharedflowInfo);
				sharedflowBackUpInfo.setStatus(Constants.STATUS_COMPLETED);
				sharedflowBackUpInfo = baseRepository.save(sharedflowBackUpInfo);
				backupInfo = sharedflowBackUpInfo;
			}
		} catch (Exception e) {
			sharedflowBackUpInfo.setStatus(Constants.STATUS_FAILED);
			sharedflowBackUpInfo = baseRepository.save(sharedflowBackUpInfo);
			backupInfo = sharedflowBackUpInfo;
			logger.error("OrganizationDataMigrationService.backupSharedflows : interactionid=" + cfg.getInteractionid()
					+ ": jsessionid=" + cfg.getJsessionId() + " : ERROR = " + e.getMessage());
			throw e;
		}
		return backupInfo;
	}

	public AppBackUpInfo scheduleBackUpApps(CommonConfiguration cfg) {
		AppBackUpInfo backUpInfo = new AppBackUpInfo();
		backUpInfo.setOrganization(cfg.getOrganization());
		backUpInfo.setStatus(Constants.STATUS_SCHEDULED);
		backUpInfo.setOperationId(cfg.getOperationId());
		backUpInfo = baseRepository.save(backUpInfo);

		BackupEvent backupEvent = new BackupEvent();
		backupEvent.setCfg(cfg);
		backupEvent.setEvent("BackupApps");
		backupEvent.setStatus(Constants.STATUS_SCHEDULED);
		backupEvent.setEventId(backUpInfo.getId());
		baseRepository.save(backupEvent);
		return backUpInfo;
	}

	/**
	 * This method will do the backup of apps.
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public BackupInfo backUpApps(CommonConfiguration cfg, String id) throws Exception {
		logger.debug("OrganizationDataMigrationService.backUpApps : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		BackupInfo backupInfo = null;
		AppBackUpInfo appBackupInfo;
		if (id != null) {
			appBackupInfo = baseRepository.findById(id, AppBackUpInfo.class);
		} else
			appBackupInfo = new AppBackUpInfo();

		try {
			ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(),
					cfg.getType());
			cfg.setApigeeEmail(apigeeServiceUser.getUserName());
			cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
			cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
			long start = System.currentTimeMillis();
			if (cfg.getBackUpLocation() == null || cfg.getBackUpLocation() == "") {
				cfg.setBackUpLocation(applicationProperties.getBackupDir() + start);
			}
			JSONObject appsInfo = null;

			appBackupInfo.setOrganization(cfg.getOrganization());
			appBackupInfo.setOperationId(cfg.getOperationId());
			appBackupInfo.setStatus(Constants.STATUS_INPROGRESS);
			appBackupInfo = baseRepository.save(appBackupInfo);
			appsInfo = backupApps(cfg);
			cfg.setIsCleanUpAreBackUp(false);
			backupAppDevelopers(cfg);
			ZipUtil.pack(new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization()),
					new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip"));
			org.json.JSONObject obj = null;
			String downloadURI = "";
			try {

				StorageIntegration storageIntegration = integrationHelper.getIntegration();
				downloadURI = storageIntegration.uploadFile(
						"Backup/" + cfg.getOrganization() + "/" + start + "/" + cfg.getOrganization() + ".zip",
						cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip");

			} catch (Exception e) {
				logger.error(e.getMessage());
				log.error("Exception occurred", e);
				throw e;
			}
			FileUtils.cleanDirectory(new File(cfg.getBackUpLocation()));
			FileUtils.deleteDirectory(new File(cfg.getBackUpLocation()));
			if (StringUtils.isNotEmpty(downloadURI)) {
				long end = System.currentTimeMillis();
				long backupTimeTaken = (end - start) / 1000l;
				appBackupInfo.setJfrogUrl(downloadURI);
				appBackupInfo.setTimeTaken(backupTimeTaken);
				appBackupInfo.setAppInfo(appsInfo);
				appBackupInfo.setStatus(Constants.STATUS_COMPLETED);
				appBackupInfo = baseRepository.save(appBackupInfo);
				backupInfo = appBackupInfo;
			}
		} catch (Exception ex) {
			appBackupInfo.setStatus(Constants.STATUS_FAILED);
			appBackupInfo = baseRepository.save(appBackupInfo);
			backupInfo = appBackupInfo;
			logger.error("OrganizationDataMigrationService.backUpApps : interactionid=" + cfg.getInteractionid()
					+ ": jsessionid=" + cfg.getJsessionId() + " : ERROR = " + ex.getMessage());
			throw ex;
		}
		return backupInfo;
	}

	public ProductsBackUpInfo scheduleBackupProducts(CommonConfiguration cfg) {
		ProductsBackUpInfo backUpInfo = new ProductsBackUpInfo();
		backUpInfo.setOrganization(cfg.getOrganization());
		backUpInfo.setStatus(Constants.STATUS_SCHEDULED);
		backUpInfo.setOperationId(cfg.getOperationId());
		backUpInfo = baseRepository.save(backUpInfo);

		BackupEvent backupEvent = new BackupEvent();
		backupEvent.setCfg(cfg);
		backupEvent.setEvent("BackupProducts");
		backupEvent.setStatus(Constants.STATUS_SCHEDULED);
		backupEvent.setEventId(backUpInfo.getId());
		baseRepository.save(backupEvent);
		return backUpInfo;
	}

	/**
	 * This method will do the backup of products.
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public BackupInfo backupProducts(CommonConfiguration cfg, String id) throws Exception {
		logger.debug("OrganizationDataMigrationService.backupProducts : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		BackupInfo backupInfo = null;
		ProductsBackUpInfo productsBackupInfo;
		if (id != null) {
			productsBackupInfo = baseRepository.findById(id, ProductsBackUpInfo.class);
		} else
			productsBackupInfo = new ProductsBackUpInfo();
		try {
			ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(),
					cfg.getType());
			cfg.setApigeeEmail(apigeeServiceUser.getUserName());
			cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
			cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
			long start = System.currentTimeMillis();
			if (cfg.getBackUpLocation() == null || cfg.getBackUpLocation() == "") {
				cfg.setBackUpLocation(applicationProperties.getBackupDir() + start);
			}
			JSONObject appsInfo = null;
			JSONObject developersInfo = null;
			JSONObject productsInfo = null;

			productsBackupInfo.setOrganization(cfg.getOrganization());
			productsBackupInfo.setStatus(Constants.STATUS_INPROGRESS);
			productsBackupInfo.setOperationId(cfg.getOperationId());
			productsBackupInfo = baseRepository.save(productsBackupInfo);
			appsInfo = backupApps(cfg);
			developersInfo = backupAppDevelopers(cfg);
			productsInfo = backupAPIProducts(cfg);
			ZipUtil.pack(new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization()),
					new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip"));
			org.json.JSONObject obj = null;
			String downloadURI = "";
			try {

				StorageIntegration storageIntegration = integrationHelper.getIntegration();
				downloadURI = storageIntegration.uploadFile(
						"Backup/" + cfg.getOrganization() + "/" + start + "/" + cfg.getOrganization() + ".zip",
						cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip");

			} catch (Exception e) {
				logger.error(e.getMessage());
				log.error("Exception occurred", e);
				throw e;
			}
			FileUtils.cleanDirectory(new File(cfg.getBackUpLocation()));
			FileUtils.deleteDirectory(new File(cfg.getBackUpLocation()));
			if (StringUtils.isNotEmpty(downloadURI)) {
				long end = System.currentTimeMillis();
				long backupTimeTaken = (end - start) / 1000l;
				productsBackupInfo.setJfrogUrl(downloadURI);
				productsBackupInfo.setTimeTaken(backupTimeTaken);
				productsBackupInfo.setProductInfo(productsInfo);
				productsBackupInfo.setAppsInfo(appsInfo);
				productsBackupInfo.setDevelopersInfo(developersInfo);
				productsBackupInfo.setStatus(Constants.STATUS_COMPLETED);
				productsBackupInfo = baseRepository.save(productsBackupInfo);
				backupInfo = productsBackupInfo;
			}
		} catch (Exception ex) {
			productsBackupInfo.setStatus(Constants.STATUS_FAILED);
			productsBackupInfo = baseRepository.save(productsBackupInfo);
			backupInfo = productsBackupInfo;
			logger.error("OrganizationDataMigrationService.backupProducts : interactionid=" + cfg.getInteractionid()
					+ ": jsessionid=" + cfg.getJsessionId() + " : ERROR = " + ex.getMessage());
			throw ex;
		}

		return backupInfo;
	}

	public DeveloperBackUpInfo scheduleBackupDevelopers(CommonConfiguration cfg) {
		DeveloperBackUpInfo backUpInfo = new DeveloperBackUpInfo();
		backUpInfo.setOrganization(cfg.getOrganization());
		backUpInfo.setStatus(Constants.STATUS_SCHEDULED);
		backUpInfo.setOperationId(cfg.getOperationId());
		backUpInfo = baseRepository.save(backUpInfo);

		BackupEvent backupEvent = new BackupEvent();
		backupEvent.setCfg(cfg);
		backupEvent.setEvent("BackupDevelopers");
		backupEvent.setStatus(Constants.STATUS_SCHEDULED);
		backupEvent.setEventId(backUpInfo.getId());
		baseRepository.save(backupEvent);
		return backUpInfo;
	}

	/**
	 * This method will do the backup of developers.
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public BackupInfo backupDevelopers(CommonConfiguration cfg, String id) throws Exception {
		logger.debug("OrganizationDataMigrationService.backupDevelopers : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		BackupInfo backupInfo = null;
		DeveloperBackUpInfo developersBackupInfo;
		if (id != null) {
			developersBackupInfo = baseRepository.findById(id, DeveloperBackUpInfo.class);
		} else
			developersBackupInfo = new DeveloperBackUpInfo();
		try {
			ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(),
					cfg.getType());
			cfg.setApigeeEmail(apigeeServiceUser.getUserName());
			cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
			cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
			long start = System.currentTimeMillis();
			if (cfg.getBackUpLocation() == null || cfg.getBackUpLocation() == "") {
				cfg.setBackUpLocation(applicationProperties.getBackupDir() + start);
			}
			JSONObject appsInfo = null;
			JSONObject developersInfo = null;

			developersBackupInfo.setOrganization(cfg.getOrganization());
			developersBackupInfo.setStatus(Constants.STATUS_INPROGRESS);
			developersBackupInfo.setOperationId(cfg.getOperationId());
			developersBackupInfo = baseRepository.save(developersBackupInfo);
			appsInfo = backupApps(cfg);
			developersInfo = backupAppDevelopers(cfg);
			ZipUtil.pack(new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization()),
					new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip"));
			org.json.JSONObject obj = null;
			String downloadURI = "";
			try {

				StorageIntegration storageIntegration = integrationHelper.getIntegration();
				downloadURI = storageIntegration.uploadFile(
						"Backup/" + cfg.getOrganization() + "/" + start + "/" + cfg.getOrganization() + ".zip",
						cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip");

			} catch (Exception e) {
				log.error("Exception occurred", e);
				throw e;
			}
			FileUtils.cleanDirectory(new File(cfg.getBackUpLocation()));
			FileUtils.deleteDirectory(new File(cfg.getBackUpLocation()));
			if (StringUtils.isNotEmpty(downloadURI)) {
				long end = System.currentTimeMillis();
				long backupTimeTaken = (end - start) / 1000l;
				logger.debug("Total Time Taken: (sec): " + backupTimeTaken);
				developersBackupInfo.setJfrogUrl(downloadURI);
				developersBackupInfo.setTimeTaken(backupTimeTaken);
				developersBackupInfo.setDeveloperInfo(developersInfo);
				developersBackupInfo.setAppsInfo(appsInfo);
				developersBackupInfo.setStatus(Constants.STATUS_COMPLETED);
				developersBackupInfo = baseRepository.save(developersBackupInfo);
				backupInfo = developersBackupInfo;
			}
		} catch (Exception ex) {
			developersBackupInfo.setStatus(Constants.STATUS_FAILED);
			developersBackupInfo = baseRepository.save(developersBackupInfo);
			backupInfo = developersBackupInfo;
			logger.error("OrganizationDataMigrationService.backupDevelopers : interactionid=" + cfg.getInteractionid()
					+ ": jsessionid=" + cfg.getJsessionId() + " : ERROR = " + ex.getMessage());
			throw ex;
		}

		return backupInfo;
	}

	public ResourceBackUpInfo scheduleBackupResources(CommonConfiguration cfg) {
		ResourceBackUpInfo backUpInfo = new ResourceBackUpInfo();
		backUpInfo.setOrganization(cfg.getOrganization());
		backUpInfo.setStatus(Constants.STATUS_SCHEDULED);
		backUpInfo.setOperationId(cfg.getOperationId());
		backUpInfo = baseRepository.save(backUpInfo);

		BackupEvent backupEvent = new BackupEvent();
		backupEvent.setCfg(cfg);
		backupEvent.setEvent("BackupResources");
		backupEvent.setStatus(Constants.STATUS_SCHEDULED);
		backupEvent.setEventId(backUpInfo.getId());
		baseRepository.save(backupEvent);
		return backUpInfo;
	}

	/**
	 * This method will do the backup of resources.
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public BackupInfo backupResources(CommonConfiguration cfg, String id) throws Exception {
		logger.debug("OrganizationDataMigrationService.backupResources : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		BackupInfo backupInfo = null;
		ResourceBackUpInfo resourceBackupInfo;
		if (id != null) {
			resourceBackupInfo = baseRepository.findById(id, ResourceBackUpInfo.class);
		} else
			resourceBackupInfo = new ResourceBackUpInfo();
		try {
			ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(),
					cfg.getType());
			cfg.setApigeeEmail(apigeeServiceUser.getUserName());
			cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
			cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
			long start = System.currentTimeMillis();
			if (cfg.getBackUpLocation() == null || cfg.getBackUpLocation() == "") {
				cfg.setBackUpLocation(applicationProperties.getBackupDir() + start);
			}
			JSONArray resourcesInfo = null;

			resourceBackupInfo.setOrganization(cfg.getOrganization());
			resourceBackupInfo.setBackUpLevel(cfg.getBackUpLevel());
			resourceBackupInfo.setStatus(Constants.STATUS_INPROGRESS);
			resourceBackupInfo.setResourceType(Constants.APIGEE_BACKUP_ORG_RESOURCE);
			resourceBackupInfo.setOperationId(cfg.getOperationId());
			resourceBackupInfo = baseRepository.save(resourceBackupInfo);
			resourcesInfo = backupresources(cfg);
			ZipUtil.pack(new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization()),
					new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip"));
			org.json.JSONObject obj = null;
			String downloadURI = "";
			try {

				StorageIntegration storageIntegration = integrationHelper.getIntegration();
				downloadURI = storageIntegration.uploadFile(
						"Backup/" + cfg.getOrganization() + "/" + start + "/" + cfg.getOrganization() + ".zip",
						cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip");

			} catch (Exception e) {
				logger.error(e.getMessage());
				log.error("Exception occurred", e);
				throw e;
			}
			FileUtils.cleanDirectory(new File(cfg.getBackUpLocation()));
			FileUtils.deleteDirectory(new File(cfg.getBackUpLocation()));
			if (StringUtils.isNotEmpty(downloadURI)) {
				long end = System.currentTimeMillis();
				long backupTimeTaken = (end - start) / 1000l;
				logger.debug("Total Time Taken: (sec): " + backupTimeTaken);
				resourceBackupInfo.setJfrogUrl(downloadURI);
				resourceBackupInfo.setTimeTaken(backupTimeTaken);
				resourceBackupInfo.setResourceInfo(resourcesInfo);
				resourceBackupInfo.setStatus(Constants.STATUS_COMPLETED);
				resourceBackupInfo = baseRepository.save(resourceBackupInfo);
				backupInfo = resourceBackupInfo;
			}
		} catch (Exception ex) {
			resourceBackupInfo.setStatus(Constants.STATUS_FAILED);
			resourceBackupInfo = baseRepository.save(resourceBackupInfo);
			backupInfo = resourceBackupInfo;
			logger.error("OrganizationDataMigrationService.backupResources : interactionid=" + cfg.getInteractionid()
					+ ": jsessionid=" + cfg.getJsessionId() + " : ERROR = " + ex.getMessage());
			throw ex;
		}
		return backupInfo;
	}

	public OrgBackUpInfo scheduleBackupOrganization(CommonConfiguration cfg) {
		OrgBackUpInfo backUpInfo = new OrgBackUpInfo();
		backUpInfo.setOrganization(cfg.getOrganization());
		backUpInfo.setStatus(Constants.STATUS_SCHEDULED);
		backUpInfo.setOperationId(cfg.getOperationId());
		backUpInfo = baseRepository.save(backUpInfo);

		BackupEvent backupEvent = new BackupEvent();
		backupEvent.setCfg(cfg);
		backupEvent.setEvent("BackupOrganization");
		backupEvent.setStatus(Constants.STATUS_SCHEDULED);
		backupEvent.setEventId(backUpInfo.getId());
		baseRepository.save(backupEvent);
		return backUpInfo;
	}

	/**
	 * This method will do the backup of Organization.
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public BackupInfo backUpOrganization(CommonConfiguration cfg, String id) throws Exception {
		logger.debug("OrganizationDataMigrationService.backUpOrganization : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		BackupInfo backupInfo = null;
		OrgBackUpInfo consoleInfo;
		if (id != null) {
			consoleInfo = baseRepository.findById(id, OrgBackUpInfo.class);
		} else
			consoleInfo = new OrgBackUpInfo();

		try {

			long start = System.currentTimeMillis();
			if (cfg.getBackUpLocation() == null || cfg.getBackUpLocation() == "") {
				cfg.setBackUpLocation(applicationProperties.getBackupDir() + start);
			}
			JSONObject apiProxyInfo = null;
			JSONObject productsInfo = null;
			JSONArray resourcesInfo = null;
			JSONObject appsInfo = null;
			JSONObject developersInfo = null;
			JSONObject sharedflowInfo = null;

			consoleInfo.setOrganization(cfg.getOrganization());
			consoleInfo.setStatus(Constants.STATUS_INPROGRESS);
			consoleInfo.setOperationId(cfg.getOperationId());
			consoleInfo = baseRepository.save(consoleInfo);
			appsInfo = backupApps(cfg);
			developersInfo = backupAppDevelopers(cfg);
			productsInfo = backupAPIProducts(cfg);
			apiProxyInfo = backupAPIProxies(cfg);
			resourcesInfo = backupresources(cfg);
			sharedflowInfo = backupSharedflow(cfg);
			ZipUtil.pack(new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization()),
					new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip"));
			org.json.JSONObject obj = null;
			String downloadURI = "";
			try {

				StorageIntegration storageIntegration = integrationHelper.getIntegration();
				downloadURI = storageIntegration.uploadFile(
						"Backup/" + cfg.getOrganization() + "/" + start + "/" + cfg.getOrganization() + ".zip",
						cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip");

			} catch (Exception e) {
				log.error("Exception occurred", e);
				throw e;
			}

			FileUtils.cleanDirectory(new File(cfg.getBackUpLocation()));
			FileUtils.deleteDirectory(new File(cfg.getBackUpLocation()));

			if (StringUtils.isNotEmpty(downloadURI)) {
				long end = System.currentTimeMillis();
				long backupTimeTaken = (end - start) / 1000l;
				logger.debug("Total Time Taken: (sec): " + backupTimeTaken);
				consoleInfo.setJfrogUrl(downloadURI);
				consoleInfo.setTimeTaken(backupTimeTaken);
				consoleInfo.setProxyInfo(apiProxyInfo);
				consoleInfo.setResourceInfo(resourcesInfo);
				consoleInfo.setDevelopersInfo(developersInfo);
				consoleInfo.setProductsInfo(productsInfo);
				consoleInfo.setAppsInfo(appsInfo);
				consoleInfo.setSharedflowInfo(sharedflowInfo);
				consoleInfo.setStatus(Constants.STATUS_COMPLETED);
				consoleInfo = baseRepository.save(consoleInfo);
				backupInfo = consoleInfo;
			}
		} catch (Exception ex) {
			consoleInfo.setStatus(Constants.STATUS_FAILED);
			consoleInfo = baseRepository.save(consoleInfo);
			backupInfo = consoleInfo;
			logger.error("OrganizationDataMigrationService.backUpOrganization : interactionid=" + cfg.getInteractionid()
					+ ": jsessionid=" + cfg.getJsessionId() + " : ERROR = " + ex.getMessage());
			throw ex;
		}

		return backupInfo;
	}

	public ResourceBackUpInfo scheduleBackupCaches(CommonConfiguration cfg) {
		ResourceBackUpInfo backUpInfo = new ResourceBackUpInfo();
		backUpInfo.setOrganization(cfg.getOrganization());
		backUpInfo.setStatus(Constants.STATUS_SCHEDULED);
		backUpInfo.setOperationId(cfg.getOperationId());
		backUpInfo = baseRepository.save(backUpInfo);

		BackupEvent backupEvent = new BackupEvent();
		backupEvent.setCfg(cfg);
		backupEvent.setEvent("BackupCaches");
		backupEvent.setStatus(Constants.STATUS_SCHEDULED);
		backupEvent.setEventId(backUpInfo.getId());
		baseRepository.save(backupEvent);
		return backUpInfo;
	}

	/**
	 * This method will do the backup of Caches.
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public BackupInfo backupCaches(CommonConfiguration cfg, String id) throws Exception {
		logger.debug("OrganizationDataMigrationService.backupCaches : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		BackupInfo backupInfo = null;
		ResourceBackUpInfo resourceBackupInfo;
		if (id != null) {
			resourceBackupInfo = baseRepository.findById(id, ResourceBackUpInfo.class);
		} else
			resourceBackupInfo = new ResourceBackUpInfo();

		try {
			ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(),
					cfg.getType());
			cfg.setApigeeEmail(apigeeServiceUser.getUserName());
			cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
			cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
			long start = System.currentTimeMillis();
			if (cfg.getBackUpLocation() == null || cfg.getBackUpLocation() == "") {
				cfg.setBackUpLocation(applicationProperties.getBackupDir() + start);
			}
			JSONArray resourcesInfo = null;

			resourceBackupInfo.setOrganization(cfg.getOrganization());
			resourceBackupInfo.setBackUpLevel(cfg.getBackUpLevel());
			resourceBackupInfo.setStatus(Constants.STATUS_INPROGRESS);
			resourceBackupInfo.setResourceType(Constants.APIGEE_BACKUP_ORG_CACHES);
			resourceBackupInfo.setOperationId(cfg.getOperationId());
			resourceBackupInfo = baseRepository.save(resourceBackupInfo);
			resourcesInfo = backupCachesInOrg(cfg);
			ZipUtil.pack(new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization()),
					new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip"));
			org.json.JSONObject obj = null;
			String downloadURI = "";
			try {

				StorageIntegration storageIntegration = integrationHelper.getIntegration();
				downloadURI = storageIntegration.uploadFile(
						"Backup/" + cfg.getOrganization() + "/" + start + "/" + cfg.getOrganization() + ".zip",
						cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip");

			} catch (Exception e) {
				log.error("Exception occurred", e);
				throw e;
			}
			FileUtils.cleanDirectory(new File(cfg.getBackUpLocation()));
			FileUtils.deleteDirectory(new File(cfg.getBackUpLocation()));
			if (StringUtils.isNotEmpty(downloadURI)) {
				long end = System.currentTimeMillis();
				long backupTimeTaken = (end - start) / 1000l;
				logger.debug("Total Time Taken: (sec): " + backupTimeTaken);
				resourceBackupInfo.setJfrogUrl(downloadURI);
				resourceBackupInfo.setTimeTaken(backupTimeTaken);
				resourceBackupInfo.setResourceInfo(resourcesInfo);
				resourceBackupInfo.setStatus(Constants.STATUS_COMPLETED);
				resourceBackupInfo = baseRepository.save(resourceBackupInfo);
				backupInfo = resourceBackupInfo;
			}
		} catch (Exception ex) {
			resourceBackupInfo.setStatus(Constants.STATUS_FAILED);
			resourceBackupInfo = baseRepository.save(resourceBackupInfo);
			backupInfo = resourceBackupInfo;
			logger.error("OrganizationDataMigrationService.backupCaches : interactionid=" + cfg.getInteractionid()
					+ ": jsessionid=" + cfg.getJsessionId() + " : ERROR = " + ex.getMessage());
			throw ex;
		}
		return backupInfo;
	}

	public ResourceBackUpInfo scheduleBackupKVM(boolean delete, CommonConfiguration cfg) {
		ResourceBackUpInfo backUpInfo = new ResourceBackUpInfo();
		backUpInfo.setOrganization(cfg.getOrganization());
		backUpInfo.setStatus(Constants.STATUS_SCHEDULED);
		backUpInfo.setOperationId(cfg.getOperationId());
		backUpInfo = baseRepository.save(backUpInfo);

		BackupEvent backupEvent = new BackupEvent();
		backupEvent.setCfg(cfg);
		backupEvent.setEvent("BackupKVM");
		backupEvent.setStatus(Constants.STATUS_SCHEDULED);
		backupEvent.setDelete(delete);
		backupEvent.setEventId(backUpInfo.getId());
		baseRepository.save(backupEvent);
		return backUpInfo;
	}

	/**
	 * This method will do the backup of KVM's.
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public BackupInfo backupKVM(boolean delete, CommonConfiguration cfg, String id) throws Exception {
		logger.debug(
				"OrganizationDataMigrationService.backupKVM : interactionid=" + cfg.getInteractionid() + ": jsessionid="
						+ cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg =" + cfg);
		BackupInfo backupInfo = null;
		ResourceBackUpInfo resourceBackupInfo;
		if (id != null) {
			resourceBackupInfo = baseRepository.findById(id, ResourceBackUpInfo.class);
		} else
			resourceBackupInfo = new ResourceBackUpInfo();

		try {
			ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(),
					cfg.getType());
			cfg.setApigeeEmail(apigeeServiceUser.getUserName());
			cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
			cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
			long start = System.currentTimeMillis();
			if (cfg.getBackUpLocation() == null || cfg.getBackUpLocation() == "") {
				cfg.setBackUpLocation(applicationProperties.getBackupDir() + start);
			}
			JSONArray resourcesInfo = null;
			resourceBackupInfo.setOrganization(cfg.getOrganization());
			resourceBackupInfo.setBackUpLevel(cfg.getBackUpLevel());
			resourceBackupInfo.setResourceType(Constants.APIGEE_BACKUP_ORG_KVM);
			resourceBackupInfo.setStatus(Constants.STATUS_INPROGRESS);
			resourceBackupInfo.setOperationId(cfg.getOperationId());
			resourceBackupInfo = baseRepository.save(resourceBackupInfo);
			resourcesInfo = backupKVMInOrg(cfg, delete);
			ZipUtil.pack(new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization()),
					new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip"));
			org.json.JSONObject obj = null;
			String downloadURI = "";
			try {

				StorageIntegration storageIntegration = integrationHelper.getIntegration();
				downloadURI = storageIntegration.uploadFile(
						"Backup/" + cfg.getOrganization() + "/" + start + "/" + cfg.getOrganization() + ".zip",
						cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip");

			} catch (Exception e) {
				log.error("Exception occurred", e);
				throw e;
			}
			FileUtils.cleanDirectory(new File(cfg.getBackUpLocation()));
			FileUtils.deleteDirectory(new File(cfg.getBackUpLocation()));
			if (StringUtils.isNotEmpty(downloadURI)) {
				long end = System.currentTimeMillis();
				long backupTimeTaken = (end - start) / 1000l;
				logger.debug("Total Time Taken: (sec): " + backupTimeTaken);
				resourceBackupInfo.setJfrogUrl(downloadURI);
				resourceBackupInfo.setTimeTaken(backupTimeTaken);
				resourceBackupInfo.setResourceInfo(resourcesInfo);
				resourceBackupInfo.setStatus(Constants.STATUS_COMPLETED);
				resourceBackupInfo = baseRepository.save(resourceBackupInfo);
				backupInfo = resourceBackupInfo;
			}
		} catch (Exception ex) {
			resourceBackupInfo.setStatus(Constants.STATUS_FAILED);
			resourceBackupInfo = baseRepository.save(resourceBackupInfo);
			backupInfo = resourceBackupInfo;
			logger.error("OrganizationDataMigrationService.backupKVM : interactionid=" + cfg.getInteractionid()
					+ ": jsessionid=" + cfg.getJsessionId() + " : ERROR = " + ex.getMessage());
			throw ex;
		}
		return backupInfo;
	}

	public ResourceBackUpInfo scheduleBackupTargetServers(CommonConfiguration cfg) {
		ResourceBackUpInfo backUpInfo = new ResourceBackUpInfo();
		backUpInfo.setOrganization(cfg.getOrganization());
		backUpInfo.setStatus(Constants.STATUS_SCHEDULED);
		backUpInfo.setOperationId(cfg.getOperationId());
		backUpInfo = baseRepository.save(backUpInfo);

		BackupEvent backupEvent = new BackupEvent();
		backupEvent.setCfg(cfg);
		backupEvent.setEvent("BackupTargetServers");
		backupEvent.setStatus(Constants.STATUS_SCHEDULED);
		backupEvent.setEventId(backUpInfo.getId());
		baseRepository.save(backupEvent);
		return backUpInfo;
	}

	/**
	 * This method will do the backup of Target Servers.
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public BackupInfo backupTargetServers(CommonConfiguration cfg, String id) throws Exception {
		logger.debug("OrganizationDataMigrationService.backupTargetServers : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		BackupInfo backupInfo = null;
		ResourceBackUpInfo resourceBackupInfo;
		if (id != null) {
			resourceBackupInfo = baseRepository.findById(id, ResourceBackUpInfo.class);
		} else
			resourceBackupInfo = new ResourceBackUpInfo();
		try {
			ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(),
					cfg.getType());
			cfg.setApigeeEmail(apigeeServiceUser.getUserName());
			cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
			cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
			long start = System.currentTimeMillis();
			if (cfg.getBackUpLocation() == null || cfg.getBackUpLocation() == "") {
				cfg.setBackUpLocation(applicationProperties.getBackupDir() + start);
			}
			JSONArray resourcesInfo = null;

			resourceBackupInfo.setOrganization(cfg.getOrganization());
			resourceBackupInfo.setBackUpLevel(cfg.getBackUpLevel());
			resourceBackupInfo.setStatus(Constants.STATUS_INPROGRESS);
			resourceBackupInfo.setResourceType(Constants.APIGEE_BACKUP_ORG_TARGET_SERVER);
			resourceBackupInfo.setOperationId(cfg.getOperationId());
			resourceBackupInfo = baseRepository.save(resourceBackupInfo);
			resourcesInfo = backupTargetServersInOrg(cfg);
			ZipUtil.pack(new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization()),
					new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip"));
			org.json.JSONObject obj = null;
			String downloadURI = "";
			try {

				StorageIntegration storageIntegration = integrationHelper.getIntegration();
				downloadURI = storageIntegration.uploadFile(
						"Backup/" + cfg.getOrganization() + "/" + start + "/" + cfg.getOrganization() + ".zip",
						cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip");

			} catch (Exception e) {
				log.error("Exception occurred", e);
				throw e;
			}
			FileUtils.cleanDirectory(new File(cfg.getBackUpLocation()));
			FileUtils.deleteDirectory(new File(cfg.getBackUpLocation()));
			if (StringUtils.isNotEmpty(downloadURI)) {
				long end = System.currentTimeMillis();
				long backupTimeTaken = (end - start) / 1000l;
				logger.debug("Total Time Taken: (sec): " + backupTimeTaken);
				resourceBackupInfo.setJfrogUrl(downloadURI);
				resourceBackupInfo.setTimeTaken(backupTimeTaken);
				resourceBackupInfo.setResourceInfo(resourcesInfo);
				resourceBackupInfo.setStatus(Constants.STATUS_COMPLETED);
				resourceBackupInfo = baseRepository.save(resourceBackupInfo);
				backupInfo = resourceBackupInfo;
			}
		} catch (Exception ex) {
			resourceBackupInfo.setStatus(Constants.STATUS_FAILED);
			resourceBackupInfo = baseRepository.save(resourceBackupInfo);
			backupInfo = resourceBackupInfo;
			logger.error("OrganizationDataMigrationService.backupTargetServers : interactionid="
					+ cfg.getInteractionid() + ": jsessionid=" + cfg.getJsessionId() + " : ERROR = " + ex.getMessage());
			throw ex;
		}

		return backupInfo;
	}

	public DeveloperBackUpInfo scheduleRestoreDevelopers(CommonConfiguration cfg) {
		DeveloperBackUpInfo backUpInfo = new DeveloperBackUpInfo();
		backUpInfo.setOrganization(cfg.getOrganization());
		backUpInfo.setStatus(Constants.STATUS_SCHEDULED);
		backUpInfo.setOperationId(cfg.getOperationId());
		backUpInfo = baseRepository.save(backUpInfo);

		BackupEvent backupEvent = new BackupEvent();
		backupEvent.setCfg(cfg);
		backupEvent.setEvent("RestoreDevelopers");
		backupEvent.setStatus(Constants.STATUS_SCHEDULED);
		backupEvent.setEventId(backUpInfo.getId());
		baseRepository.save(backupEvent);
		return backUpInfo;
	}

	/**
	 * Using this method we can restore the developer's
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public BackupInfo restoreAppDevelopers1(CommonConfiguration cfg, String id) throws Exception {
		logger.debug("OrganizationDataMigrationService.restoreAppDevelopers1 : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		BackupInfo backupInfo = null;
		long start = System.currentTimeMillis();
		DeveloperBackUpInfo developersBackupInfo;
		if (id != null) {
			developersBackupInfo = baseRepository.findById(id, DeveloperBackUpInfo.class);
		} else
			developersBackupInfo = new DeveloperBackUpInfo();
		developersBackupInfo.setOrganization(cfg.getOrganization());
		developersBackupInfo.setStatus(Constants.STATUS_INPROGRESS);
		developersBackupInfo.setOperationId(cfg.getOperationId());
		developersBackupInfo = baseRepository.save(developersBackupInfo);
		restoreAppDevelopers(cfg);
		restoreAPP(cfg);
		long end = System.currentTimeMillis();
		long backupTimeTaken = (end - start) / 1000l;
		logger.debug("Total Time Taken: (sec): " + backupTimeTaken);
		developersBackupInfo.setTimeTaken(backupTimeTaken);
		developersBackupInfo.setStatus(Constants.STATUS_COMPLETED);
		developersBackupInfo = baseRepository.save(developersBackupInfo);
		backupInfo = developersBackupInfo;
		return backupInfo;
	}

	/**
	 * This method we can restore the developer's
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ItorixException
	 */
	public String restoreAppDevelopers(CommonConfiguration cfg)
			throws IOException, InterruptedException, ItorixException {
		logger.debug("OrganizationDataMigrationService.restoreAppDevelopers : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		downloadBackup1(cfg);
		File backupLocation = null;
		if (cfg.getOldOrg() != null) {
			backupLocation = new File(getBaseRestoreAndMigareDirectory(cfg), "developers");
		} else {
			backupLocation = new File(getBaseRestoreDirectory(cfg), "developers");
		}
		if (backupLocation != null && backupLocation.listFiles() != null && backupLocation.listFiles().length > 0)
			for (final File appDeveloper : backupLocation.listFiles()) {

				if (appDeveloper.getName().startsWith("."))
					continue;
				try {
					String s = IOUtils.toString(new FileInputStream(appDeveloper));
					cfg.setAppDeveloper(s);
					apigeeUtil.createDeveloper(cfg);
				} catch (Exception e) {
					log.error("Exception occurred", e);
				}
			}
		return "Success";
	}

	public ResourceBackUpInfo scheduleRestoreResources(CommonConfiguration cfg) {
		ResourceBackUpInfo backUpInfo = new ResourceBackUpInfo();
		backUpInfo.setOrganization(cfg.getOrganization());
		backUpInfo.setStatus(Constants.STATUS_SCHEDULED);
		backUpInfo.setOperationId(cfg.getOperationId());
		backUpInfo = baseRepository.save(backUpInfo);

		BackupEvent backupEvent = new BackupEvent();
		backupEvent.setCfg(cfg);
		backupEvent.setEvent("RestoreResources");
		backupEvent.setStatus(Constants.STATUS_SCHEDULED);
		backupEvent.setEventId(backUpInfo.getId());
		baseRepository.save(backupEvent);
		return backUpInfo;
	}

	/**
	 * This method will restore the resources.
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ItorixException
	 */
	public BackupInfo restoreResources(CommonConfiguration cfg, String id)
			throws IOException, InterruptedException, ItorixException {
		logger.debug("OrganizationDataMigrationService.restoreResources : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		BackupInfo backupInfo = null;
		ResourceBackUpInfo resourceBackupInfo;
		if (id != null) {
			resourceBackupInfo = baseRepository.findById(id, ResourceBackUpInfo.class);
		} else
			resourceBackupInfo = new ResourceBackUpInfo();
		resourceBackupInfo.setOrganization(cfg.getOrganization());
		resourceBackupInfo.setBackUpLevel(cfg.getBackUpLevel());
		resourceBackupInfo.setStatus(Constants.STATUS_INPROGRESS);
		resourceBackupInfo.setResourceType(Constants.APIGEE_BACKUP_ORG_RESOURCE);
		resourceBackupInfo.setOperationId(cfg.getOperationId());
		resourceBackupInfo = baseRepository.save(resourceBackupInfo);
		long start = System.currentTimeMillis();
		restoreResource(cfg);
		long end = System.currentTimeMillis();
		long backupTimeTaken = (end - start) / 1000l;
		logger.debug("Total Time Taken: (sec): " + backupTimeTaken);
		resourceBackupInfo.setTimeTaken(backupTimeTaken);
		resourceBackupInfo.setStatus(Constants.STATUS_COMPLETED);
		resourceBackupInfo = baseRepository.save(resourceBackupInfo);
		backupInfo = resourceBackupInfo;
		return backupInfo;
	}

	public String restoreKVM(CommonConfiguration cfg) throws IOException, InterruptedException, ItorixException {

		return "Success";
	}

	public String restoreResource(CommonConfiguration cfg) throws IOException, InterruptedException, ItorixException {
		logger.debug("OrganizationDataMigrationService.restoreResource : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(), cfg.getType());
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		downloadBackup1(cfg);
		File environmentsDir = null;
		if (cfg.getOldOrg() != null) {
			environmentsDir = new File(getBaseRestoreAndMigareDirectory(cfg), "environments");
		} else {
			environmentsDir = new File(getBaseRestoreDirectory(cfg), "environments");
		}

		for (File environmentDir : environmentsDir.listFiles()) {
			String environment = environmentDir.getName();
			cfg.setEnvironment(environment);
			if (environment.startsWith("."))
				continue;

			for (File resourceType : environmentDir.listFiles()) {

				for (final File resource : resourceType.listFiles()) {

					if (null != cfg.getNewOrg() && cfg.getNewOrg().length() > 0) {
						environment = cfg.getNewEnv();
						cfg.setEnvironment(environment);
					}
					String s = IOUtils.toString(new FileInputStream(resource));
					cfg.setResource(s);
					try {
						if ("keyvaluemaps".equals(resourceType.getName())
								&& !cfg.getType().equalsIgnoreCase("apigeex")) {
							String orgInfo = apigeeUtil.getOrganization(cfg);
							JSONObject orgJson = (JSONObject) JSONSerializer.toJSON(orgInfo);
							if (orgJson != null) {
								JSONObject propertysJson = orgJson.getJSONObject("properties");
								if (propertysJson != null) {
									JSONArray propertyArray = propertysJson.getJSONArray("property");
									if (propertyArray != null && propertyArray.size() > 0) {
										for (int i = 0; i < propertyArray.size(); i++) {
											JSONObject propertyJson = propertyArray.getJSONObject(i);
											if (propertyJson != null
													&& "features.isCpsEnabled".equals(propertyJson.getString("name"))
													&& "true".equals(propertyJson.getString("value"))) {
												// String
												// map_name=resource.getName().substring(0,
												// resource.getName().lastIndexOf("."));
												JSONObject kvmJson = (JSONObject) JSONSerializer.toJSON(s);
												String map_name = kvmJson.getString("name");

												boolean KeyValueMaps = apigeeUtil.isKeyValueMap(cfg, map_name);

												JSONArray entryArray = kvmJson.getJSONArray("entry");
												if (KeyValueMaps) {
													for (int j = 0; j < entryArray.size(); j++) {
														List<String> keys = apigeeUtil
																.listKeysInAnEnvironmentKeyValueMap(cfg, map_name);
														JSONObject entryJson = entryArray.getJSONObject(j);
														String entry_name = entryJson.getString("name");
														cfg.setResource(entryJson.toString());
														if (keys != null && keys.contains(entry_name)) {
															apigeeUtil.updateAKeyValueMapEntryInAnEnvironment(cfg,
																	map_name, entry_name);
														} else {
															apigeeUtil.createAnEntryInAnEnvironmentKeyValueMap(cfg,
																	map_name);
														}
													}
												} else {
													JSONObject mapObj = new JSONObject();
													mapObj.put("name", map_name);
													cfg.setResource(mapObj.toString());
													apigeeUtil.createKeyValueMapInAnEnvironment(cfg);
													for (int j = 0; j < entryArray.size(); j++) {
														JSONObject entryJson = entryArray.getJSONObject(j);
														cfg.setResource(entryJson.toString());
														apigeeUtil.createAnEntryInAnEnvironmentKeyValueMap(cfg,
																map_name);
													}
												}

											} else {
												apigeeUtil.createResourceInEnvironment(cfg, resourceType.getName());
											}
										}
									} else {
										apigeeUtil.createResourceInEnvironment(cfg, resourceType.getName());
									}
								}
							}

						} else {
							apigeeUtil.createResourceInEnvironment(cfg, resourceType.getName());
						}
					} catch (Exception e) {
						logger.error(e.getMessage());
						log.error("Exception occurred", e);
						throw e;
					}
				}
			}
		}
		// FileUtils.cleanDirectory(new File(cfg.getBackUpLocation()));
		// FileUtils.deleteDirectory(new File(cfg.getBackUpLocation()));
		return "Success";
	}

	public String restoreResource1(CommonConfiguration cfg) throws IOException, InterruptedException, ItorixException {
		logger.debug("OrganizationDataMigrationService.restoreResource : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		downloadBackup1(cfg);
		File environmentsDir = null;
		if (cfg.getOldOrg() != null) {
			environmentsDir = new File(getBaseRestoreAndMigareDirectory(cfg), "environments");
		} else {
			environmentsDir = new File(getBaseRestoreDirectory(cfg), "environments");
		}

		for (File environmentDir : environmentsDir.listFiles()) {
			String environment = environmentDir.getName();
			cfg.setEnvironment(environment);
			if (environment.startsWith("."))
				continue;

			for (File resourceType : environmentDir.listFiles()) {

				for (final File resource : resourceType.listFiles()) {

					if (null != cfg.getNewOrg() && cfg.getNewOrg().length() > 0) {
						environment = cfg.getNewEnv();
						cfg.setEnvironment(environment);
					}
					String s = IOUtils.toString(new FileInputStream(resource));
					cfg.setResource(s);
					try {
						if ("keyvaluemaps".equals(resourceType.getName())
								&& !cfg.getType().equalsIgnoreCase("apigeex")) {
							String orgInfo = apigeeUtil.getOrganization(cfg);
							JSONObject orgJson = (JSONObject) JSONSerializer.toJSON(orgInfo);
							if (orgJson != null) {
								JSONObject propertysJson = orgJson.getJSONObject("properties");
								if (propertysJson != null) {
									JSONArray propertyArray = propertysJson.getJSONArray("property");
									if (propertyArray != null && propertyArray.size() > 0) {
										for (int i = 0; i < propertyArray.size(); i++) {
											JSONObject propertyJson = propertyArray.getJSONObject(i);
											if (propertyJson != null
													&& "features.isCpsEnabled".equals(propertyJson.getString("name"))
													&& "true".equals(propertyJson.getString("value"))) {
												JSONObject kvmJson = (JSONObject) JSONSerializer.toJSON(s);
												String map_name = kvmJson.getString("name");

												boolean KeyValueMaps = apigeeUtil.isKeyValueMap(cfg, map_name);

												JSONArray entryArray = kvmJson.getJSONArray("entry");
												if (KeyValueMaps) {
													for (int j = 0; j < entryArray.size(); j++) {
														List<String> keys = apigeeUtil
																.listKeysInAnEnvironmentKeyValueMap(cfg, map_name);
														JSONObject entryJson = entryArray.getJSONObject(j);
														String entry_name = entryJson.getString("name");
														cfg.setResource(entryJson.toString());
														if (keys != null && keys.contains(entry_name)) {
															apigeeUtil.updateAKeyValueMapEntryInAnEnvironment(cfg,
																	map_name, entry_name);
														} else {
															apigeeUtil.createAnEntryInAnEnvironmentKeyValueMap(cfg,
																	map_name);
														}
													}
												} else {
													JSONObject mapObj = new JSONObject();
													mapObj.put("name", map_name);
													cfg.setResource(mapObj.toString());
													apigeeUtil.createKeyValueMapInAnEnvironment(cfg);
													for (int j = 0; j < entryArray.size(); j++) {
														JSONObject entryJson = entryArray.getJSONObject(j);
														cfg.setResource(entryJson.toString());
														apigeeUtil.createAnEntryInAnEnvironmentKeyValueMap(cfg,
																map_name);
													}
												}

											} else {
												apigeeUtil.createResourceInEnvironment(cfg, resourceType.getName());
											}
										}
									} else {
										apigeeUtil.createResourceInEnvironment(cfg, resourceType.getName());
									}
								}
							}

						} else {
							if (!resourceType.getName().equals("caches"))
								apigeeUtil.createResourceInEnvironment(cfg, resourceType.getName());
						}
					} catch (Exception e) {
						logger.error(e.getMessage());
						e.printStackTrace();
						throw e;
					}
				}
			}
		}
		// FileUtils.cleanDirectory(new File(cfg.getBackUpLocation()));
		// FileUtils.deleteDirectory(new File(cfg.getBackUpLocation()));
		return "Success";
	}

	private void deployProxies(CommonConfiguration cfg) throws ItorixException {
		Mappings mapping = cfg.getMappings();
		if (mapping!=null && mapping.getProxies() != null) {
			String env = cfg.getNewEnv();
			cfg.setEnvironment(env);
			for (com.itorix.apiwiz.common.model.apigee.Proxy proxy : mapping.getProxies()) {
				String proxyName = proxy.getName();
				cfg.setApiName(proxyName);
				List<Integer> revsList = apigeeUtil.getRevisionsListForProxy(cfg);
				Integer rev = Collections.max(revsList);
				cfg.setRevision(Integer.toString(rev));
				apigeeUtil.deployAPIProxy1(cfg);
			}
		}
	}

	private void deploySharedflows(CommonConfiguration cfg) throws ItorixException {
		Mappings mapping = cfg.getMappings();
		if (mapping!=null && mapping.getSharedflows() != null) {
			for (com.itorix.apiwiz.common.model.apigee.Sharedflow sharedflow : mapping.getSharedflows()) {
				String sharedflowName = sharedflow.getName();
				cfg.setSharedflowName(sharedflowName);
				List<Integer> revsList = apigeeUtil.getRevisionsListForSharedflow(cfg);
				Integer rev = Collections.max(revsList);
				cfg.setRevision(Integer.toString(rev));
				apigeeUtil.deploySharedflow(cfg);
			}
		}
	}

	/*
	 * The below section contains all the list of private methods
	 */

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

	private JSONObject backupAPIProxies(CommonConfiguration cfg) throws IOException, ItorixException {
		logger.debug("OrganizationDataMigrationService.backupAPIProxies : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		List<String> selectedProxies = null;
		JSONObject proxiesData = new JSONObject();
		JSONArray skippedProxiesData = new JSONArray();
		JSONArray proxiesInfo = new JSONArray();
		final File backupLocation = new File(getBaseBackupDirectory(false, cfg), "apiproxies");
		backupLocation.mkdirs();
		if (null == cfg.getSelectedProxies()) {
			selectedProxies = new ArrayList<String>();
			List<String> proxiesList = apigeeUtil.listAPIProxies(cfg);
			if (proxiesList != null) {
				for (int i = 0; i < proxiesList.size(); i++) {
					String proxy = proxiesList.get(i);
					selectedProxies.add(proxy);
				}
			}
		}
		for (String apiObj : selectedProxies) {
			JSONObject apiContext = new JSONObject();
			final String api = apiObj;
			cfg.setApiName(api);
			APIProxyDeploymentDetailsResponse apiProxyDeploymentResponse = apigeeUtil.getAPIProxyDeploymentDetails(cfg);
			ArrayList<Integer> versionList = new ArrayList<Integer>();
			String[] versions = null;
			if (cfg.getIsDepoyedOnly()) {
				Set<String> set = new HashSet<String>();
				for (Environment environment : apiProxyDeploymentResponse.getEnvironment()) {
					for (Revision revision : environment.getRevision()) {
						cfg.setRevision(revision.getName());
						set.add(revision.getName());
						versionList.add(Integer.valueOf(revision.getName()));
						final File versionFile = new File(backupLocation,
								File.separator + api + File.separator + revision.getName());
						versionFile.getParentFile().mkdirs();
						byte[] revisionBundle = apigeeUtil.getAnAPIProxyRevision(cfg);
						FileUtils.writeByteArrayToFile(new File(versionFile.getAbsolutePath() + ".zip"),
								revisionBundle);
					}
				}
				versions = new String[set.size()];
				versions = set.toArray(versions);
				if (apiProxyDeploymentResponse.getEnvironment().length > 0) {
					JSONArray deploymentConfgs = new JSONArray();
					for (Environment envo : apiProxyDeploymentResponse.getEnvironment()) {
						if (envo.getRevision().length > 0) {
							Revision revision = envo.getRevision()[0];
							JSONObject envObj = new JSONObject();
							envObj.put("environment", envo.getName());
							envObj.put("revision", revision.getName());
							deploymentConfgs.add(envObj);
							if (cfg.getIsCleanUpAreBackUp()) {
								cfg.setEnvironment(envo.getName());
								cfg.setRevision(revision.getName());
								apigeeUtil.forceUndeployAPIProxy(cfg);
							}
						}
						apiContext.put("environments", deploymentConfgs);
					}
				}
				try {

					if (versions.length > 0 && versionList.size() > 0) {
						apiContext.put("versions", versions);
						apiContext.put("maxversion", Collections.max(versionList));
						FileWriter file = new FileWriter(
								new File(backupLocation, File.separator + api + File.separator + "context.json"));
						file.write(apiContext.toString());
						file.flush();
						file.close();
						JSONObject apiContext1 = new JSONObject();
						apiContext1.put(api, apiContext);
						proxiesInfo.add(apiContext1);
					}

				} catch (IOException e) {
					logger.error(e.getMessage());
					throw e;
				}
				if (cfg.getIsCleanUpAreBackUp()) {
					apigeeUtil.deleteAPIProxy(cfg);
				}
			} else {
				APIProxyResponse apiProxyResponse = apigeeUtil.getAPIProxy(cfg);
				versions = apiProxyResponse.getRevision();
				for (String verObj : apiProxyResponse.getRevision()) {
					String version = (String) verObj;
					cfg.setRevision(version);
					versionList.add(Integer.valueOf(version));
					final File versionFile = new File(backupLocation, File.separator + api + File.separator + version);
					versionFile.getParentFile().mkdirs();
					byte[] revisionBundle = apigeeUtil.getAnAPIProxyRevision(cfg);
					FileUtils.writeByteArrayToFile(new File(versionFile.getAbsolutePath() + ".zip"), revisionBundle);
				}
				if (apiProxyDeploymentResponse.getEnvironment().length > 0) {
					JSONArray deploymentConfgs = new JSONArray();
					for (Environment envo : apiProxyDeploymentResponse.getEnvironment()) {
						if (envo.getRevision().length > 0) {
							Revision revision = envo.getRevision()[0];
							JSONObject envObj = new JSONObject();
							envObj.put("environment", envo.getName());
							envObj.put("revision", revision.getName());
							deploymentConfgs.add(envObj);
							if (cfg.getIsCleanUpAreBackUp()) {
								cfg.setEnvironment(envo.getName());
								cfg.setRevision(revision.getName());
								apigeeUtil.forceUndeployAPIProxy(cfg);
							}
						}
						apiContext.put("environments", deploymentConfgs);
					}
				}
				try {

					if (versions.length > 0 && versionList.size() > 0) {
						apiContext.put("versions", versions);
						apiContext.put("maxversion", Collections.max(versionList));
						FileWriter file = new FileWriter(
								new File(backupLocation, File.separator + api + File.separator + "context.json"));
						file.write(apiContext.toString());
						file.flush();
						file.close();
						JSONObject apiContext1 = new JSONObject();
						apiContext1.put(api, apiContext);
						proxiesInfo.add(apiContext1);
					}

				} catch (IOException e) {
					logger.error(e.getMessage());
					throw e;
				}
				if (cfg.getIsCleanUpAreBackUp()) {
					apigeeUtil.deleteAPIProxy(cfg);
				}
			}
		}

		proxiesData.put("PROXIES", proxiesInfo);
		proxiesData.put("SKIPPEDPROXIES", skippedProxiesData);
		return proxiesData;
	}

	/**
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws IOException
	 * @throws ItorixException
	 */
	private JSONObject backupSharedflow(CommonConfiguration cfg) throws IOException, ItorixException {
		logger.debug("OrganizationDataMigrationService.backupSharedflow : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		List<String> selectedSharedflows = null;
		JSONObject sharedflowsData = new JSONObject();
		JSONArray skippedSharedflowsData = new JSONArray();
		JSONArray SharedflowsInfo = new JSONArray();
		final File backupLocation = new File(getBaseBackupDirectory(false, cfg), "sharedflows");
		backupLocation.mkdirs();
		if (null == cfg.getSelectedSharedflows()) {
			selectedSharedflows = new ArrayList<String>();
			List<String> sharedflowsList = apigeeUtil.listSharedflows(cfg);
			if (sharedflowsList != null) {
				for (int i = 0; i < sharedflowsList.size(); i++) {
					String sharedflow = sharedflowsList.get(i);
					selectedSharedflows.add(sharedflow);
				}
			}
		}
		for (String SharedflowObj : selectedSharedflows) {
			JSONObject apiContext = new JSONObject();
			final String sharedflowName = SharedflowObj;
			cfg.setSharedflowName(sharedflowName);
			String sharedflowsDeploymentDetailsResponseString = apigeeUtil.getSharedflowsDeploymentDetails(cfg);
			JSONObject sharedflowsDeploymentDetailsResponse = (JSONObject) JSONSerializer
					.toJSON(sharedflowsDeploymentDetailsResponseString);
			JSONArray environments = (JSONArray) sharedflowsDeploymentDetailsResponse.get("environment");
			ArrayList<Integer> versionList = new ArrayList<Integer>();
			Set<String> versions = new HashSet<String>();
			if (cfg.getIsDepoyedOnly()) {
				Set<String> set = new HashSet<String>();

				for (int i = 0; i < environments.size(); i++) {
					JSONObject environment = (JSONObject) environments.get(i);
					JSONArray revisions = (JSONArray) environment.get("revision");
					for (int j = 0; j < revisions.size(); j++) {
						JSONObject revision = (JSONObject) revisions.get(j);
						String name = (String) revision.get("name");
						cfg.setRevision(name);
						set.add(name);
						versionList.add(Integer.valueOf(name));
						final File versionFile = new File(backupLocation,
								File.separator + sharedflowName + File.separator + name);
						versionFile.getParentFile().mkdirs();
						byte[] revisionBundle = apigeeUtil.getAnSharedflowRevision(cfg);
						FileUtils.writeByteArrayToFile(new File(versionFile.getAbsolutePath() + ".zip"),
								revisionBundle);
					}
				}
				/*
				 * versions = new String[set.size()];
				 * versions=set.toArray(versions);
				 */
				versions.addAll(set);
				if (environments.size() > 0) {
					JSONArray deploymentConfgs = new JSONArray();
					for (int i = 0; i < environments.size(); i++) {
						JSONObject environment = (JSONObject) environments.get(i);
						JSONArray revisions = (JSONArray) environment.get("revision");
						for (int j = 0; j < revisions.size(); j++) {
							JSONObject revision = (JSONObject) revisions.get(j);
							String name = (String) revision.get("name");
							JSONObject envObj = new JSONObject();
							envObj.put("environment", (String) environment.get("name"));
							envObj.put("revision", name);
							deploymentConfgs.add(envObj);
							if (cfg.getIsCleanUpAreBackUp()) {
								cfg.setEnvironment((String) environment.get("name"));
								cfg.setRevision(name);
								apigeeUtil.forceUndeploySharedflow(cfg);
							}
						}
						apiContext.put("environments", deploymentConfgs);
					}
				}
				try {

					if (versions.size() > 0 && versionList.size() > 0) {
						apiContext.put("versions", versions);
						apiContext.put("maxversion", Collections.max(versionList));
						FileWriter file = new FileWriter(new File(backupLocation,
								File.separator + sharedflowName + File.separator + "context.json"));
						file.write(apiContext.toString());
						file.flush();
						file.close();
						JSONObject apiContext1 = new JSONObject();
						apiContext1.put(sharedflowName, apiContext);
						SharedflowsInfo.add(apiContext1);
					}

				} catch (IOException e) {
					logger.error(e.getMessage());
					throw e;
				}
				if (cfg.getIsCleanUpAreBackUp()) {
					apigeeUtil.deleteSharedflow(cfg);
				}
			} else {
				String sharedflowResponse = apigeeUtil.getSharedflow(cfg);
				JSONObject sharedflowResponseObj = (JSONObject) JSONSerializer.toJSON(sharedflowResponse);
				if (sharedflowResponseObj != null) {
					JSONArray revisions = (JSONArray) sharedflowResponseObj.get("revision");
					if (revisions != null) {
						for (int i = 0; i < revisions.size(); i++) {
							versions.add(revisions.getString(i));
						}
					}
				}
				for (String verObj : versions) {
					String version = (String) verObj;
					cfg.setRevision(version);
					versionList.add(Integer.valueOf(version));
					final File versionFile = new File(backupLocation,
							File.separator + sharedflowName + File.separator + version);
					versionFile.getParentFile().mkdirs();
					byte[] revisionBundle = apigeeUtil.getAnSharedflowRevision(cfg);
					FileUtils.writeByteArrayToFile(new File(versionFile.getAbsolutePath() + ".zip"), revisionBundle);
				}
				if (environments.size() > 0) {
					JSONArray deploymentConfgs = new JSONArray();
					for (int i = 0; i < environments.size(); i++) {
						JSONObject environment = (JSONObject) environments.get(i);
						JSONArray revisions = (JSONArray) environment.get("revision");
						for (int j = 0; j < revisions.size(); j++) {
							JSONObject revision = (JSONObject) revisions.get(j);
							String name = (String) revision.get("name");
							JSONObject envObj = new JSONObject();
							envObj.put("environment", (String) environment.get("name"));
							envObj.put("revision", name);
							deploymentConfgs.add(envObj);
							if (cfg.getIsCleanUpAreBackUp()) {
								cfg.setEnvironment((String) environment.get("name"));
								cfg.setRevision(name);
								apigeeUtil.forceUndeploySharedflow(cfg);
							}
						}
						apiContext.put("environments", deploymentConfgs);
					}
				}
				try {

					if (versions.size() > 0 && versionList.size() > 0) {
						apiContext.put("versions", versions);
						apiContext.put("maxversion", Collections.max(versionList));
						FileWriter file = new FileWriter(new File(backupLocation,
								File.separator + sharedflowName + File.separator + "context.json"));
						file.write(apiContext.toString());
						file.flush();
						file.close();
						JSONObject apiContext1 = new JSONObject();
						apiContext1.put(sharedflowName, apiContext);
						SharedflowsInfo.add(apiContext1);
					}

				} catch (IOException e) {
					logger.error(e.getMessage());
					throw e;
				}
				if (cfg.getIsCleanUpAreBackUp()) {
					apigeeUtil.deleteSharedflow(cfg);
				}
			}
		}

		sharedflowsData.put("PROXIES", SharedflowsInfo);
		sharedflowsData.put("SKIPPEDPROXIES", skippedSharedflowsData);
		return sharedflowsData;
	}

	private JSONObject backupApps(CommonConfiguration cfg) throws IOException, ItorixException {
		logger.debug("OrganizationDataMigrationService.backupApps : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		List<String> appsString = apigeeUtil.listAppIDsInAnOrganization(cfg);

		JSONObject appsList = new JSONObject();
		JSONArray skippedAppsData = new JSONArray();
		JSONArray appNames = new JSONArray();
		for (String app : appsString) {
			cfg.setAppID(app);
			String developerAppString = apigeeUtil.getAppInAnOrganizationByAppID(cfg);
			JSONObject json1 = (JSONObject) JSONSerializer.toJSON(developerAppString);
			String appName1 = json1.getString("name");
			appNames.add(appName1);
			File backupLocation = new File(getBaseBackupDirectory(false, cfg), "apps");
			backupLocation.mkdirs();
			try {
				FileWriter file = new FileWriter(new File(backupLocation, app + ".json"));
				file.write(developerAppString.toString());
				file.flush();
				file.close();
			} catch (IOException e) {
				throw e;
			}
			if (cfg.getIsCleanUpAreBackUp()) {
				JSONObject json = (JSONObject) JSONSerializer.toJSON(developerAppString);
				String developeremail = json.getString("developerId");
				cfg.setDeveloperId(developeremail);
				String appName = json.getString("name");
				cfg.setAppName(appName);
				if (appName.contains(" "))
					appName = appName.replace(" ", "%20");
				JSONArray jsonArray = json.getJSONArray("credentials");
				for (int i = 0; i < jsonArray.size(); i++) {
					Object elem = jsonArray.get(i);
					if (elem instanceof JSONObject) {
						JSONObject obj = (JSONObject) elem;
						String consumerKey = obj.getString("consumerKey");
						cfg.setConsumerKey(consumerKey);
						String developerAppinfo = apigeeUtil.getDeveloper(cfg);
						JSONObject developerJson = (JSONObject) JSONSerializer.toJSON(developerAppinfo);
						String email = developerJson.getString("email");
						cfg.setDeveloperEmail(email);
						apigeeUtil.deleteKeyForADeveloperApp(cfg);
					}
				}

				apigeeUtil.deleteDeveloperApp(cfg, developeremail, appName);
			}
		}
		appsList.put("APPS", appNames);
		appsList.put("SKIPPEDAPPS", skippedAppsData);
		return appsList;
	}

	private JSONObject backupAPIProducts(CommonConfiguration cfg) throws IOException, ItorixException {
		logger.debug("OrganizationDataMigrationService.backupAPIProducts : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		List<String> apiProducts = apigeeUtil.listAPIProducts(cfg);
		JSONObject productsList = new JSONObject();
		JSONArray productsData = new JSONArray();
		JSONArray skippedProductsData = new JSONArray();
		for (String apiProduct : apiProducts) {
			// If string contains spaces, it will not allow to process.
			cfg.setApiProductName(apiProduct);
			productsData.add(apiProduct);
			try {
				String apiProductString = apigeeUtil.getAPIProduct(cfg).toString();
				File backupLocation = new File(getBaseBackupDirectory(false, cfg), "apiproducts");
				backupLocation.mkdirs();
				FileWriter file = new FileWriter(new File(backupLocation, apiProduct + ".json"));
				file.write(apiProductString);
				file.flush();
				file.close();

			} catch (IOException e) {
				logger.error(e.getMessage());
				throw e;
			}

			if (cfg.getIsCleanUpAreBackUp()) {
				cfg.setApiProductName(apiProduct);
				apigeeUtil.deleteAPIProduct(cfg);
			}
		}
		productsList.put("PRODUCTS", productsData);
		productsList.put("SKIPPEDPRODUCTS", skippedProductsData);
		return productsList;
	}

	private JSONObject backupAppDevelopers(CommonConfiguration cfg) throws IOException, ItorixException {
		logger.debug("OrganizationDataMigrationService.backupAppDevelopers : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		JSONObject developersList = new JSONObject();
		JSONArray skippedDevelopersData = new JSONArray();
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
		developersList.put("DEVELOPERS", developers.toString());
		developersList.put("SKIPPEDDEVELOPERS", skippedDevelopersData);
		return developersList;
	}

	private JSONArray backupresources(CommonConfiguration cfg) throws IOException, ItorixException {
		logger.debug("OrganizationDataMigrationService.backupResources : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		List<String> environments = null;
		if (null == cfg.getSelectedEnvironments()) {
			environments = apigeeUtil.getEnvironmentNames(cfg);
		} else {
			environments = cfg.getSelectedEnvironments();
		}

		JSONArray resourcesList = new JSONArray();
		JSONArray envCaches = null;
		JSONArray envKVM = null;
		JSONArray envTargetServers = null;

		if (cfg.getType() != null && !cfg.getType().equalsIgnoreCase("apigeex")) {
			envCaches = backupCaches(cfg, environments);
		}
		resourcesList.addAll(envCaches);
		if (cfg.getType() != null && !cfg.getType().equalsIgnoreCase("apigeex")) {
			envKVM = backupKVM(cfg, environments);
		}
		resourcesList.addAll(envKVM);
		envTargetServers = backupTargetServers(cfg, environments);
		resourcesList.addAll(envTargetServers);

		return resourcesList;
	}

	private JSONArray backupCachesInOrg(CommonConfiguration cfg) throws ItorixException {
		logger.debug("OrganizationDataMigrationService.backupCaches : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		List<String> environments = null;
		if (null == cfg.getSelectedEnvironments()) {
			environments = apigeeUtil.getEnvironmentNames(cfg);
		} else {
			environments = cfg.getSelectedEnvironments();
		}

		JSONArray resourcesList = new JSONArray();
		JSONArray envCaches = null;

		envCaches = backupCaches(cfg, environments);
		resourcesList.addAll(envCaches);

		return resourcesList;
	}

	private JSONArray backupKVMInOrg(CommonConfiguration cfg, final boolean delete)
			throws IOException, ItorixException {
		logger.debug("OrganizationDataMigrationService.backupKVMInOrg : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		List<String> environments = null;
		if (null == cfg.getSelectedEnvironments()) {
			environments = apigeeUtil.getEnvironmentNames(cfg);
		} else {
			environments = cfg.getSelectedEnvironments();
		}

		JSONArray resourcesList = new JSONArray();
		JSONArray envKVM = null;

		envKVM = backupKVM(cfg, environments);
		resourcesList.addAll(envKVM);

		return resourcesList;
	}

	private JSONArray backupTargetServersInOrg(CommonConfiguration cfg) throws IOException, ItorixException {
		List<String> environments = null;
		if (null == cfg.getSelectedEnvironments()) {
			environments = apigeeUtil.getEnvironmentNames(cfg);
		} else {
			environments = cfg.getSelectedEnvironments();
		}

		JSONArray resourcesList = new JSONArray();
		JSONArray envTargetServers = null;

		envTargetServers = backupTargetServers(cfg, environments);
		resourcesList.addAll(envTargetServers);

		return resourcesList;
	}

	private JSONArray backupCaches(CommonConfiguration cfg, List<String> environments) throws ItorixException {
		logger.debug("OrganizationDataMigrationService.backupResources : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
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
			JSONArray skippedCaches = new JSONArray();
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
			resourceData.put("SKIPPEDCACHES", skippedCaches);
			envList.put(environment, resourceData);
			resourcesList.add(envList);
		}

		return resourcesList;
	}

	private JSONArray backupKVM(CommonConfiguration cfg, List<String> environments)
			throws IOException, ItorixException {
		logger.debug("OrganizationDataMigrationService.backupResources : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		File environmentsDir = new File(getBaseBackupDirectory(false, cfg), "environments");
		environmentsDir.mkdirs();

		JSONArray resourcesList = new JSONArray();

		for (String env : environments) {
			String environment = (String) env;
			JSONObject resourceData = new JSONObject();
			JSONObject envList = new JSONObject();

			File environmentDir = new File(environmentsDir, environment);
			environmentDir.mkdirs();
			cfg.setEnvironment(environment);
			final File keyValueMapsDir = new File(environmentDir, "keyvaluemaps");
			keyValueMapsDir.mkdirs();
			List<String> keyValueMaps = apigeeUtil.listKeyValueMapsInAnEnvironment(cfg);
			JSONArray keyValues = (JSONArray) JSONSerializer.toJSON(keyValueMaps);
			resourceData.put("KV", keyValues);
			JSONArray skippedKVs = new JSONArray();
			for (String keyName : keyValueMaps) {
				// If string contains spaces, it will not allow to process..
				cfg.setKeyValueMapName(keyName);
				String keyValue = apigeeUtil.getKeyValueMapInAnEnvironment(cfg);
				FileWriter file = new FileWriter(new File(keyValueMapsDir, keyName.toString() + ".json"));
				file.write(keyValue);
				file.flush();
				file.close();

				logger.debug(keyValue);

				if (cfg.getIsCleanUpAreBackUp()) {
					apigeeUtil.deleteKeyValueMapInAnEnvironment(cfg);
				}
			}
			resourceData.put("SKIPPEDKVS", skippedKVs);
			envList.put(environment, resourceData);
			resourcesList.add(envList);
		}
		return resourcesList;
	}

	private JSONArray backupTargetServers(CommonConfiguration cfg, List<String> environments)
			throws IOException, ItorixException {
		logger.debug("OrganizationDataMigrationService.backupResources : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
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
			cfg.setEnvironment(environment);
			List<String> targetServers = apigeeUtil.listTargetServersInAnEnvironment(cfg);
			JSONArray skippedTargetServers = new JSONArray();
			resourceData.put("TARGETSERVERS", targetServers);
			for (String targetServer : targetServers) {
				cfg.setTargetServerName(targetServer);
				String serverDetails = apigeeUtil.getTargetServer(cfg);
				FileWriter file = new FileWriter(new File(targetServerDir, targetServer.toString() + ".json"));
				file.write(serverDetails);
				file.flush();
				file.close();
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

	public String restoreAPIProxies(String oid, CommonConfiguration cfg)
			throws IOException, InterruptedException, RestClientException, ItorixException {
		logger.debug("############# Restoring API Proxies ##############");
		downloadBackup(oid, cfg);
		File apiproxiesDir = new File(getBaseRestoreDirectory(cfg), "apiproxies");
		if (apiproxiesDir != null && apiproxiesDir.listFiles() != null && apiproxiesDir.listFiles().length > 0)
			for (File apiproxyDir : apiproxiesDir.listFiles()) {
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
									// check if proxy is deployed in e if yes
									// undeploy all
									cfg.setEnvironment(deployedEnv);
									undeployProxyRevision(cfg, deployedEnv, apiProxyName, null);

									// get max revisions available
									List<Integer> revisionList = apigeeUtil.getRevisionsListForProxy(cfg);
									// Monetaization enabled proxies are not
									// being imported, hence cant be deployed,
									// so adding below condition
									if (revisionList.size() > 0) {

										int maxRev = Collections.max(revisionList);
										cfg.setRevision(maxRev + "");
										cfg.setEnvironment(deployedEnv);

										// deploy revision maxRev, since this is
										// latest uploaded
										apigeeUtil.deployAPIProxy(cfg);
									}
								}
							}

						} catch (IOException e) {
							log.error("Exception occurred", e);

						} catch (Exception e1) {
							log.error("Exception occurred", e1);

						}
					}
				} catch (IOException e) {
					log.error("Exception occurred", e);
				}
			}

		return "Success";
	}

	public String restoreAPIProxies1(CommonConfiguration cfg)
			throws IOException, InterruptedException, ItorixException {
		logger.debug("OrganizationDataMigrationService.restoreAPIProxies1 : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		// ApigeeServiceUser apigeeServiceUser =
		// apigeeUtil.getApigeeServiceAccount(cfg.getOrganization(),
		// cfg.getType());
		// cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		// cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		// cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(),
		// cfg.getType()));
		downloadBackup1(cfg);
		File apiproxiesDir = null;
		if (cfg.getOldOrg() != null) {
			apiproxiesDir = new File(getBaseRestoreAndMigareDirectory(cfg), "apiproxies");
		} else {
			apiproxiesDir = new File(getBaseRestoreDirectory(cfg), "apiproxies");
		}
		if (apiproxiesDir != null && apiproxiesDir.listFiles() != null && apiproxiesDir.listFiles().length > 0)
			for (File apiproxyDir : apiproxiesDir.listFiles()) {
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
							String response = apigeeUtil.importApiProxy(cfg, revision);
							List<com.itorix.apiwiz.data.management.model.mappers.Environment> environments = proxyInfo
									.getEnvironments();
							for (com.itorix.apiwiz.data.management.model.mappers.Environment e : environments) {
								deployedEnv = e.getEnvironment();
								deployedRev = e.getRevision();
								if (null != cfg.getNewOrg() && cfg.getNewOrg().length() > 0)
									deployedEnv = cfg.getNewEnv();
								cfg.setEnvironment(deployedEnv);
								if (e.getEnvironment().equalsIgnoreCase(cfg.getOldEnv())) {
									if (deployedRev.equals(i)) {
										// check if proxy is deployed in e if
										// yes
										// undeploy all

										undeployProxyRevision(cfg, deployedEnv, apiProxyName, null);
										apigeeUtil.deployAPIProxy1(cfg);
									}
								} else {
									if (deployedRev.equals(i)) {
										// check if proxy is deployed in e if
										// yes
										// undeploy all

										undeployProxyRevision(cfg, deployedEnv, apiProxyName, null);
										apigeeUtil.deployAPIProxy1(cfg);
									}
								}
							}

						} catch (IOException e) {
							log.error("Exception occurred", e);
						} catch (Exception e) {
							log.error("Exception occurred", e);
						}
					}
					APIProxyDeploymentDetailsResponse deploymentsRes = apigeeUtil.getAPIProxyDeploymentDetails(cfg);
					if (deploymentsRes.getEnvironment().length <= 0) {
						// get max revisions available
						List<Integer> revisionList = apigeeUtil.getRevisionsListForProxy(cfg);
						// Monetaization enabled proxies are not
						// being imported, hence cant be deployed,
						// so adding below condition
						if (revisionList.size() > 0) {

							int maxRev = Collections.max(revisionList);
							cfg.setRevision(maxRev + "");
							// deploy revision maxRev, since this is
							// latest uploaded
							apigeeUtil.deployAPIProxy1(cfg);
						}
					}

				} catch (IOException e) {
					log.error("Exception occurred", e);
				} catch (Exception e) {
					log.error("Exception occurred", e);
				}
			}

		return "Success";
	}

	public String restoreApigeexProxies(CommonConfiguration cfg)
			throws IOException, InterruptedException, ItorixException {
		logger.debug("OrganizationDataMigrationService.restoreAPIProxies1 : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg =" + cfg);
		downloadBackup1(cfg);
		File apiproxiesDir = null;
		if (cfg.getOldOrg() != null) {
			apiproxiesDir = new File(getBaseRestoreAndMigareDirectory(cfg), "apiproxies");
		} else {
			apiproxiesDir = new File(getBaseRestoreDirectory(cfg), "apiproxies");
		}
		if (apiproxiesDir != null && apiproxiesDir.listFiles() != null && apiproxiesDir.listFiles().length > 0) {
			for (File apiproxyDir : apiproxiesDir.listFiles()) {
				try {
					String apiProxyName = apiproxyDir.getName();
					Mappings mapping = cfg.getMappings();
					if (mapping!=null && mapping.getProxies() != null) {
						String env = cfg.getNewEnv();
						cfg.setEnvironment(env);
						for (com.itorix.apiwiz.common.model.apigee.Proxy proxy : mapping.getProxies()) {
							if (StringUtils.equals(proxy.getName(), apiProxyName)) {
								logger.debug("ProxyName from File:{}, ProxyName from List:{} ", apiProxyName,
										proxy.getName());
								cfg.setApiName(apiProxyName);
								File revision = new File(apiproxyDir, proxy.getRevision() + ".zip");
								try {
									logger.debug("ProxyName:{}", proxy.getName());
									cfg.setRevision(proxy.getRevision());
									String response = apigeeUtil.importApiProxy(cfg, revision);
								} catch (Exception e) {
									log.error("Exception occurred", e);
								}
							}
						}
					}
				} catch (Exception e) {
					log.error("Exception occurred", e);
				}
			}
		}

		return "Success";
	}

	public String restoreApigeexSharedflows(CommonConfiguration cfg)
			throws IOException, InterruptedException, ItorixException {
		logger.debug(
				"OrganizationDataMigrationService.restoreSharedflows1 : interactionid={}: jsessionid={} : organization ={}  : cfg ={}"
				, cfg.getInteractionid(), cfg.getJsessionId(), cfg.getOrganization(), cfg);
		downloadBackup1(cfg);
		File sharedflowsDir = null;
		if (cfg.getOldOrg() != null) {
			sharedflowsDir = new File(getBaseRestoreAndMigareDirectory(cfg), "sharedflows");
			logger.debug("SharedFlowDir:{}", sharedflowsDir);
		} else {
			sharedflowsDir = new File(getBaseRestoreDirectory(cfg), "sharedflows");
			logger.debug("SharedFlowDir:{}", sharedflowsDir);
		}
		if (sharedflowsDir != null && sharedflowsDir.listFiles() != null
				&& sharedflowsDir.listFiles().length > 0) {
			for (File sharedflowDir : sharedflowsDir.listFiles()) {
				try {
					String sharedflowName = sharedflowDir.getName();
					logger.debug("sharedFlow:{}", sharedflowName);
					if (StringUtils.equalsIgnoreCase(cfg.getType(), "apigeex")) {
						Mappings mapping = cfg.getMappings();
						if (mapping != null && mapping.getSharedflows() != null) {
							for (com.itorix.apiwiz.common.model.apigee.Sharedflow sharedflow : mapping.getSharedflows()) {
								if (StringUtils.equals(sharedflowName, sharedflow.getName())) {
									logger.debug("SharedFlow from File:{}, SharedFlow from List:{} ", sharedflowName,
											sharedflow.getName());
									cfg.setSharedflowName(sharedflowName);
									File revision = new File(sharedflowDir, sharedflow.getRevision() + ".zip");
									try {
										cfg.setRevision(sharedflow.getRevision());
										apigeeUtil.importSharedflows(cfg, revision);
									} catch (IOException e) {
										log.error("Exception occurred", e);
									}
								}
							}
						}
						return "Success";
					}
					else{
						cfg.setSharedflowName(sharedflowName);
						File revision = new File(sharedflowDir, ".zip");
						try {
//							cfg.setRevision(sharedflow.getRevision());
							apigeeUtil.importSharedflows(cfg, revision);
						} catch (IOException e) {
							log.error("Exception occurred", e);
						}

					}
				} catch (IOException e) {
					log.error("Exception occurred", e);
				} catch (Exception e) {
					log.error("Exception occurred", e);
				}
			}
		}
		return "Success";
	}

	public String restoreSharedflows1(CommonConfiguration cfg)
			throws IOException, InterruptedException, ItorixException {
		logger.debug("OrganizationDataMigrationService.restoreSharedflows1 : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		downloadBackup1(cfg);
		File sharedflowsDir = null;
		if (cfg.getOldOrg() != null) {
			sharedflowsDir = new File(getBaseRestoreAndMigareDirectory(cfg), "sharedflows");
		} else {
			sharedflowsDir = new File(getBaseRestoreDirectory(cfg), "sharedflows");
		}
		if (sharedflowsDir != null && sharedflowsDir.listFiles() != null && sharedflowsDir.listFiles().length > 0)
			for (File sharedflowDir : sharedflowsDir.listFiles()) {
				try {
					String sharedflowName = sharedflowDir.getName();
					cfg.setSharedflowName(sharedflowName);
					if (sharedflowName.startsWith("."))
						continue;
					File deploymentsFile = new File(sharedflowDir, "context.json");
					String s = IOUtils.toString(new FileInputStream(deploymentsFile));
					ObjectMapper objMapper = new ObjectMapper();
					RestoreProxyInfo proxyInfo = new RestoreProxyInfo();
					proxyInfo = objMapper.readValue(s, RestoreProxyInfo.class);
					List<String> proxyVersions = proxyInfo.getVersions();
					for (String i : proxyVersions) {
						File revision = new File(sharedflowDir, i + ".zip");
						String deployedEnv = null;
						String deployedRev = null;
						try {
							cfg.setRevision(i);
							System.out.println(i);
							apigeeUtil.importSharedflows(cfg, revision);
							List<com.itorix.apiwiz.data.management.model.mappers.Environment> environments = proxyInfo
									.getEnvironments();
							for (com.itorix.apiwiz.data.management.model.mappers.Environment e : environments) {
								deployedEnv = e.getEnvironment();
								deployedRev = e.getRevision();
								if (null != cfg.getNewOrg() && cfg.getNewOrg().length() > 0)
									deployedEnv = cfg.getNewEnv();
								cfg.setEnvironment(deployedEnv);
								if (e.getEnvironment().equalsIgnoreCase(cfg.getOldEnv())) {
									if (deployedRev.equals(i)) {
										undeploySharedflowRevison(cfg, deployedEnv, sharedflowName, null);
										apigeeUtil.deploySharedflow(cfg);
									}
								} else {
									if (deployedRev.equals(i)) {
										undeploySharedflowRevison(cfg, deployedEnv, sharedflowName, null);
										apigeeUtil.deploySharedflow(cfg);
									}
								}
							}
							String deploymentsRes = apigeeUtil.getSharedflowsDeploymentDetails(cfg);
							JSONObject deployments = (JSONObject) JSONSerializer.toJSON(deploymentsRes);
							JSONArray envs = (JSONArray) deployments.get("environment");
							if (envs == null || envs.size() <= 0) {
								List<Integer> revisionList = apigeeUtil.getRevisionsListForSharedflow(cfg);
								if (revisionList.size() > 0) {
									int maxRev = Collections.max(revisionList);
									cfg.setRevision(maxRev + "");
									apigeeUtil.deploySharedflow(cfg);
								}
							}
						} catch (IOException e) {
							log.error("Exception occurred", e);
						}
					}
				} catch (IOException e) {
					log.error("Exception occurred", e);
				} catch (Exception e) {
					log.error("Exception occurred", e);
				}
			}
		return "Success";
	}

	public AppBackUpInfo scheduleRestoreApps(CommonConfiguration cfg) {
		AppBackUpInfo backUpInfo = new AppBackUpInfo();
		backUpInfo.setOrganization(cfg.getOrganization());
		backUpInfo.setStatus(Constants.STATUS_SCHEDULED);
		backUpInfo.setOperationId(cfg.getOperationId());
		backUpInfo = baseRepository.save(backUpInfo);

		BackupEvent backupEvent = new BackupEvent();
		backupEvent.setCfg(cfg);
		backupEvent.setEvent("RestoreApps");
		backupEvent.setStatus(Constants.STATUS_SCHEDULED);
		backupEvent.setEventId(backUpInfo.getId());
		baseRepository.save(backupEvent);
		return backUpInfo;
	}

	public BackupInfo restoreAPPs(CommonConfiguration cfg, String id)
			throws IOException, InterruptedException, ItorixException {
		logger.debug("OrganizationDataMigrationService.restoreAPPs : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		BackupInfo backupInfo = null;
		long start = System.currentTimeMillis();
		AppBackUpInfo appBackupInfo = new AppBackUpInfo();
		if (id != null) {
			appBackupInfo = baseRepository.findById(id, AppBackUpInfo.class);
		} else
			appBackupInfo = new AppBackUpInfo();
		appBackupInfo.setOrganization(cfg.getOrganization());
		appBackupInfo.setOperationId(cfg.getOperationId());
		appBackupInfo.setStatus(Constants.STATUS_INPROGRESS);
		appBackupInfo = baseRepository.save(appBackupInfo);
		restoreAPP(cfg);
		long end = System.currentTimeMillis();
		long backupTimeTaken = (end - start) / 1000l;
		logger.debug("Total Time Taken: (sec): " + backupTimeTaken);
		appBackupInfo.setTimeTaken(backupTimeTaken);
		appBackupInfo.setStatus(Constants.STATUS_COMPLETED);
		appBackupInfo = baseRepository.save(appBackupInfo);
		backupInfo = appBackupInfo;
		return backupInfo;
	}

	public String restoreAPP(CommonConfiguration cfg) throws IOException, InterruptedException, ItorixException {
		logger.debug("OrganizationDataMigrationService.restoreAPP : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		downloadBackup1(cfg);
		File backupLocation = null;
		if (cfg.getOldOrg() != null) {
			backupLocation = new File(getBaseRestoreAndMigareDirectory(cfg), "apps");
		} else {
			backupLocation = new File(getBaseRestoreDirectory(cfg), "apps");
		}

		HashMap<String, String> developersMap = readDevelopers(cfg);

		if (backupLocation != null && backupLocation.listFiles() != null && backupLocation.listFiles().length > 0)
			for (File app : backupLocation.listFiles()) {

				try {

					if (app.getName().startsWith("."))
						continue;

					String s = IOUtils.toString(new FileInputStream(app));

					final JSONObject appBackup = (JSONObject) JSONSerializer.toJSON(s);

					// if(appBackup.containsKey(DEVELOPER_ID)){
					String developerEmail = "";
					if (appBackup.containsKey("developerId")) {
						final String developerId = appBackup.getString("developerId");
						developerEmail = developersMap.get(developerId);
					} else {
						developerEmail = developersMap.get("default");
					}

					final JSONObject request = new JSONObject();
					request.put("name", appBackup.get("name"));
					if (ObjectUtils.isNotEmpty(appBackup.get("apiProducts")))
						request.put("apiProducts", appBackup.get("apiProducts"));
					if (ObjectUtils.isNotEmpty(appBackup.get("scopes")))
						request.put("scopes", appBackup.get("scopes"));
					if (ObjectUtils.isNotEmpty(appBackup.get("accessType")))
						request.put("accessType", appBackup.get("accessType"));
					if (ObjectUtils.isNotEmpty(appBackup.get("appFamily")))
						request.put("appFamily", appBackup.get("appFamily"));
					if (ObjectUtils.isNotEmpty(appBackup.get("attributes")))
						request.put("attributes", appBackup.get("attributes"));

					final String finalDeveloperEmail = developerEmail;
					cfg.setDeveloperEmail(finalDeveloperEmail);

					String name = appBackup.getString("name").replace(" ", "%20"); // (URLEncoder.encode(appBackup.getString("name"),"ISO-8859-1"").replace("+",
					cfg.setAppName(name);

					String appCreatedString = apigeeUtil.postDeveloperApps(cfg, request.toString());
					logger.debug("Sleeping for delay due to management Server-- Do Delete new App." + request.toString()
							+ "AppName" + name);
					Thread.sleep(3000);

					JSONObject appCreated = (JSONObject) JSONSerializer.toJSON(appCreatedString);
					if (appCreated.containsKey("credentials")) {
						JSONArray credentials = appCreated.getJSONArray("credentials");
						for (Object credentialO : credentials) {
							JSONObject credential = (JSONObject) credentialO;
							String consumerKey = credential.getString("consumerKey");
							cfg.setConsumerKey(consumerKey);
							apigeeUtil.deleteKeyForADeveloperApp(cfg);
							logger.debug(
									"Sleeping for delay due to management Server-- Do Delete new App." + consumerKey);
							Thread.sleep(3000);
						}

						JSONArray backupCredentials = appBackup.getJSONArray("credentials");
						for (Object backupCredentialO : backupCredentials) {
							JSONObject backupCredential = (JSONObject) backupCredentialO;

							String consumerKey = backupCredential.getString("consumerKey");
							String consumerSecret = backupCredential.getString("consumerSecret");
							JSONObject consumerKeyRequest = new JSONObject();
							consumerKeyRequest.put("consumerKey", consumerKey);
							consumerKeyRequest.put("consumerSecret", consumerSecret);

							apigeeUtil.createdeveloperKeysForDeveloperApp(cfg, consumerKeyRequest.toString());

							JSONArray backupProducts = backupCredential.getJSONArray("apiProducts");
							JSONObject productsRequest = new JSONObject();
							JSONArray products = new JSONArray();
							for (Object backupProductO : backupProducts) {
								JSONObject backupProduct = (JSONObject) backupProductO;
								String productName = backupProduct.getString("apiproduct");
								products.add(productName);
								String productStatus = backupProduct.getString("status");
								if ("approved".equalsIgnoreCase(productStatus)) {
									// todo check if the product has manual
									// approval. If yes, then you have to
									// manually
									// approve the product
								}
							}

							productsRequest.put("apiProducts", products);
							cfg.setConsumerKey(consumerKey);
							apigeeUtil.addAPIProductToKey(cfg, productsRequest.toString());
						}
					}

				} catch (IOException e) {
					log.error("Exception occurred", e);
				}
			}
		FileUtils.cleanDirectory(new File(cfg.getBackUpLocation()));
		FileUtils.deleteDirectory(new File(cfg.getBackUpLocation()));
		return "Success";
	}

	public ProductsBackUpInfo scheduleRestoreProducts(CommonConfiguration cfg) {
		ProductsBackUpInfo backUpInfo = new ProductsBackUpInfo();
		backUpInfo.setOrganization(cfg.getOrganization());
		backUpInfo.setStatus(Constants.STATUS_SCHEDULED);
		backUpInfo.setOperationId(cfg.getOperationId());
		backUpInfo = baseRepository.save(backUpInfo);

		BackupEvent backupEvent = new BackupEvent();
		backupEvent.setCfg(cfg);
		backupEvent.setEvent("RestoreProducts");
		backupEvent.setStatus(Constants.STATUS_SCHEDULED);
		backupEvent.setEventId(backUpInfo.getId());
		baseRepository.save(backupEvent);
		return backUpInfo;
	}

	public BackupInfo restoreAPIProducts1(CommonConfiguration cfg, String id) throws Exception {
		logger.debug("OrganizationDataMigrationService.restoreAPIProducts1 : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		BackupInfo backupInfo = null;
		long start = System.currentTimeMillis();
		ProductsBackUpInfo productsBackupInfo;
		if (id != null) {
			productsBackupInfo = baseRepository.findById(id, ProductsBackUpInfo.class);
		} else
			productsBackupInfo = new ProductsBackUpInfo();

		productsBackupInfo.setOrganization(cfg.getOrganization());
		productsBackupInfo.setStatus(Constants.STATUS_INPROGRESS);
		productsBackupInfo.setOperationId(cfg.getOperationId());
		restoreAPIProducts(cfg);
		restoreAppDevelopers(cfg);
		restoreAPP(cfg);
		long end = System.currentTimeMillis();
		long backupTimeTaken = (end - start) / 1000l;
		logger.debug("Total Time Taken: (sec): " + backupTimeTaken);
		productsBackupInfo.setTimeTaken(backupTimeTaken);
		productsBackupInfo.setStatus(Constants.STATUS_COMPLETED);
		productsBackupInfo = baseRepository.save(productsBackupInfo);
		backupInfo = productsBackupInfo;
		return backupInfo;
	}

	public String restoreAPIProducts(CommonConfiguration cfg) throws Exception {
		logger.debug("OrganizationDataMigrationService.restoreAPIProducts : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(cfg.getOrganization(), cfg.getType()));
		downloadBackup1(cfg);
		File backupLocation = null;
		if (cfg.getOldOrg() != null) {
			backupLocation = new File(getBaseRestoreAndMigareDirectory(cfg), "apiproducts");
		} else {
			backupLocation = new File(getBaseRestoreDirectory(cfg), "apiproducts");
		}

		if (backupLocation != null && backupLocation.listFiles() != null && backupLocation.listFiles().length > 0)
			for (final File apiProduct : backupLocation.listFiles()) {
				if (backupLocation.getName().startsWith("."))
					continue;
				String js = "";
				String str = "";
				BufferedReader br = null;
				String sCurrentLine;
				br = new BufferedReader(new FileReader(apiProduct));
				while ((sCurrentLine = br.readLine()) != null) {
					str += sCurrentLine;
				}
				br.close();
				if (null != cfg.getNewOrg() && cfg.getNewOrg().length() > 0) {
					String environment = cfg.getNewEnv();
					try {
						ObjectMapper objectMapper = new ObjectMapper();
						APIProduct productObj = new APIProduct();
						productObj = objectMapper.readValue(str, APIProduct.class);
						productObj.getEnvironments().clear();
						productObj.getEnvironments().add(environment);
						productObj.setCreatedBy(null);
						productObj.setLastModifiedBy(null);
						if(CollectionUtils.isEmpty(productObj.getScopes()))
							productObj.setScopes(null);


						ObjectMapper mapper1 = new ObjectMapper();

						mapper1.setSerializationInclusion(JsonInclude.Include.NON_NULL);
						mapper1.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

						js = mapper1.writeValueAsString(productObj);
						cfg.setApiProduct(js);
						apigeeUtil.createApiProduct(cfg);

					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						throw e;
					}
				} else {
					cfg.setApiProduct(str);
					apigeeUtil.createApiProduct(cfg);
					logger.debug("Adding a delay in POST API Calls.--");
					// Thread.sleep(150000);
				}
			}
		return "Success";
	}

	public ProxyBackUpInfo scheduleRestoreApiProxies(CommonConfiguration cfg) {
		ProxyBackUpInfo backUpInfo = new ProxyBackUpInfo();
		backUpInfo.setOrganization(cfg.getOrganization());
		backUpInfo.setStatus(Constants.STATUS_SCHEDULED);
		backUpInfo.setOperationId(cfg.getOperationId());
		backUpInfo = baseRepository.save(backUpInfo);

		BackupEvent backupEvent = new BackupEvent();
		backupEvent.setCfg(cfg);
		backupEvent.setEvent("RestoreApiProxies");
		backupEvent.setStatus(Constants.STATUS_SCHEDULED);
		backupEvent.setEventId(backUpInfo.getId());
		baseRepository.save(backupEvent);
		return backUpInfo;
	}

	public BackupInfo restoreApiProxies(CommonConfiguration cfg, String id) throws Exception {
		logger.debug("OrganizationDataMigrationService.restoreApiProxies : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		BackupInfo backupInfo = null;
		long start = System.currentTimeMillis();
		ProxyBackUpInfo proxyBackUpInfo;
		if (id != null) {
			proxyBackUpInfo = baseRepository.findById(id, ProxyBackUpInfo.class);
		} else
			proxyBackUpInfo = new ProxyBackUpInfo();

		proxyBackUpInfo.setOrganization(cfg.getOrganization());
		proxyBackUpInfo.setStatus(Constants.STATUS_INPROGRESS);
		proxyBackUpInfo.setOperationId(cfg.getOperationId());
		proxyBackUpInfo = baseRepository.save(proxyBackUpInfo);
		restoreSharedflows1(cfg);
		restoreAPIProxies1(cfg);
		restoreAPIProducts(cfg);
		restoreAppDevelopers(cfg);
		restoreAPP(cfg);
		long end = System.currentTimeMillis();
		long backupTimeTaken = (end - start) / 1000l;
		logger.debug("Total Time Taken: (sec): " + backupTimeTaken);
		proxyBackUpInfo.setTimeTaken(backupTimeTaken);
		proxyBackUpInfo.setStatus(Constants.STATUS_COMPLETED);
		proxyBackUpInfo = baseRepository.save(proxyBackUpInfo);
		backupInfo = proxyBackUpInfo;
		return backupInfo;
	}

	public SharedflowBackUpInfo scheduleRestoreSharedflows(CommonConfiguration cfg) {
		SharedflowBackUpInfo backUpInfo = new SharedflowBackUpInfo();
		backUpInfo.setOrganization(cfg.getOrganization());
		backUpInfo.setStatus(Constants.STATUS_SCHEDULED);
		backUpInfo.setOperationId(cfg.getOperationId());
		backUpInfo = baseRepository.save(backUpInfo);

		BackupEvent backupEvent = new BackupEvent();
		backupEvent.setCfg(cfg);
		backupEvent.setEvent("RestoreSharedFlows");
		backupEvent.setStatus(Constants.STATUS_SCHEDULED);
		backupEvent.setEventId(backUpInfo.getId());
		baseRepository.save(backupEvent);
		return backUpInfo;
	}

	public BackupInfo restoreSharedflows(CommonConfiguration cfg, String id) throws Exception {
		logger.debug("OrganizationDataMigrationService.restoreSharedflows : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		long start = System.currentTimeMillis();
		BackupInfo backupInfo = null;
		SharedflowBackUpInfo sharedflowBackUpInfo;

		if (id != null) {
			sharedflowBackUpInfo = baseRepository.findById(id, SharedflowBackUpInfo.class);
		} else
			sharedflowBackUpInfo = new SharedflowBackUpInfo();
		sharedflowBackUpInfo.setOrganization(cfg.getOrganization());
		sharedflowBackUpInfo.setStatus(Constants.STATUS_INPROGRESS);
		sharedflowBackUpInfo.setOperationId(cfg.getOperationId());
		sharedflowBackUpInfo = baseRepository.save(sharedflowBackUpInfo);
		restoreSharedflows1(cfg);
		long end = System.currentTimeMillis();
		long backupTimeTaken = (end - start) / 1000l;
		logger.debug("Total Time Taken: (sec): " + backupTimeTaken);
		sharedflowBackUpInfo.setTimeTaken(backupTimeTaken);
		sharedflowBackUpInfo.setStatus(Constants.STATUS_COMPLETED);
		sharedflowBackUpInfo = baseRepository.save(sharedflowBackUpInfo);
		backupInfo = sharedflowBackUpInfo;
		return backupInfo;
	}

	public OrgBackUpInfo scheduleRestoreOrganization(CommonConfiguration cfg) {
		OrgBackUpInfo backUpInfo = new OrgBackUpInfo();
		backUpInfo.setOrganization(cfg.getOrganization());
		backUpInfo.setStatus(Constants.STATUS_SCHEDULED);
		backUpInfo.setOperationId(cfg.getOperationId());
		backUpInfo = baseRepository.save(backUpInfo);

		BackupEvent backupEvent = new BackupEvent();
		backupEvent.setCfg(cfg);
		backupEvent.setEvent("RestoreOrganization");
		backupEvent.setStatus(Constants.STATUS_SCHEDULED);
		backupEvent.setEventId(backUpInfo.getId());
		baseRepository.save(backupEvent);
		return backUpInfo;
	}

	public OrgBackUpInfo scheduleMigrateOrganization(CommonConfiguration cfg) {
		OrgBackUpInfo backUpInfo = new OrgBackUpInfo();
		backUpInfo.setOrganization(cfg.getOrganization());
		backUpInfo.setStatus(Constants.STATUS_SCHEDULED);
		backUpInfo.setOperationId(cfg.getOperationId());
		backUpInfo = baseRepository.save(backUpInfo);

		BackupEvent backupEvent = new BackupEvent();
		backupEvent.setCfg(cfg);
		backupEvent.setEvent("MIGRATEORGANIZATION");
		backupEvent.setStatus(Constants.STATUS_SCHEDULED);
		backupEvent.setEventId(backUpInfo.getId());
		baseRepository.save(backupEvent);
		return backUpInfo;
	}

	public BackupInfo restoreOrganization(CommonConfiguration cfg, String id) throws Exception {
		logger.debug("OrganizationDataMigrationService.restoreOrganization : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		BackupInfo backupInfo = null;
		long start = System.currentTimeMillis();
		OrgBackUpInfo consoleInfo;
		if (id != null) {
			consoleInfo = baseRepository.findById(id, OrgBackUpInfo.class);
		} else
			consoleInfo = new OrgBackUpInfo();

		consoleInfo.setOrganization(cfg.getOrganization());
		consoleInfo.setStatus(Constants.STATUS_INPROGRESS);
		consoleInfo.setOperationId(cfg.getOperationId());
		consoleInfo = baseRepository.save(consoleInfo);
		restoreResource(cfg);
		restoreSharedflows1(cfg);
		restoreAPIProxies1(cfg);
		restoreAPIProducts(cfg);
		restoreAppDevelopers(cfg);
		restoreAPP(cfg);
		long end = System.currentTimeMillis();
		long backupTimeTaken = (end - start) / 1000l;
		logger.debug("Total Time Taken: (sec): " + backupTimeTaken);
		consoleInfo.setTimeTaken(backupTimeTaken);
		consoleInfo.setStatus(Constants.STATUS_COMPLETED);
		consoleInfo = baseRepository.save(consoleInfo);
		backupInfo = consoleInfo;
		return backupInfo;
	}

	public BackupInfo migrateOrganization(CommonConfiguration cfg, String id) throws Exception {
		logger.debug("OrganizationDataMigrationService.restoreOrganization : interactionid=" + cfg.getInteractionid()
				+ ": jsessionid=" + cfg.getJsessionId() + " : organization =" + cfg.getOrganization() + " : cfg ="
				+ cfg);
		BackupInfo backupInfo = null;
		long start = System.currentTimeMillis();
		OrgBackUpInfo consoleInfo;
		if (id != null) {
			consoleInfo = baseRepository.findById(id, OrgBackUpInfo.class);
		} else
			consoleInfo = new OrgBackUpInfo();

		consoleInfo.setOrganization(cfg.getOrganization());
		consoleInfo.setStatus(Constants.STATUS_INPROGRESS);
		consoleInfo.setOperationId(cfg.getOperationId());
		consoleInfo = baseRepository.save(consoleInfo);
		try {
			restoreResource1(cfg);
			if (StringUtils.equalsIgnoreCase(cfg.getType(), "apigeex")) {
				log.info("Restore resource Completed : {}", cfg.getType());
				restoreApigeexSharedflows(cfg);
				log.info("Restore ApigeeX SharedFlows Completed : {}", cfg.getGwtype());
				deploySharedflows(cfg);
				log.info("Deploy ApigeeX SharedFlows Completed : {}", cfg.getGwtype());
				restoreApigeexProxies(cfg);
				log.info("Restore ApigeeX Proxies Completed : {}", cfg.getGwtype());
				deployProxies(cfg);
				log.info("Deploy ApigeeX Proxies Completed : {}", cfg.getGwtype());
				restoreAPIProducts(cfg);
				log.info("Restore ApigeeX Products Completed : {}", cfg.getGwtype());
				restoreAppDevelopers(cfg);
				log.info("Restore ApigeeX Developers Completed : {}", cfg.getGwtype());
				restoreAPP(cfg);
				log.info("Restore ApigeeX App Completed : {}", cfg.getGwtype());
			}else{
				restoreSharedflows1(cfg);
				restoreAPIProxies1(cfg);
				restoreAPIProducts(cfg);
				restoreAppDevelopers(cfg);
				restoreAPP(cfg);

			}
			long end = System.currentTimeMillis();
			long backupTimeTaken = (end - start) / 1000l;
			logger.debug("Total Time Taken: (sec): " + backupTimeTaken);
			consoleInfo.setTimeTaken(backupTimeTaken);
			consoleInfo.setStatus(Constants.STATUS_COMPLETED);
			consoleInfo = baseRepository.save(consoleInfo);
			backupInfo = consoleInfo;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			consoleInfo.setStatus(Constants.STATUS_FAILED);
			consoleInfo = baseRepository.save(consoleInfo);
		}
		return backupInfo;
	}

	public void undeployProxyRevision(CommonConfiguration cfg, String environment, String apiName, String revision)
			throws IOException, ItorixException {

		if (null != revision) {
			cfg.setApiName(apiName);
			cfg.setEnvironment(environment);
			cfg.setRevision(revision);
			apigeeUtil.unDeployAPIProxy(cfg);

		} else {
			String depResponse = apigeeUtil.getAPIProxyDeploymentDetail1s(cfg);

			ObjectMapper o2 = new ObjectMapper();
			JsonNode targetNode = o2.readTree(depResponse);
			JSONObject targetdeployedEnv = (JSONObject) JSONSerializer.toJSON(targetNode.toString());

			if (targetdeployedEnv.containsKey("revision")) {
				// check rev deployments and undeploy them
				JSONArray revisionArray = targetdeployedEnv.getJSONArray("revision");

				for (Object tarRevObj : revisionArray) {
					JSONObject temp1 = (JSONObject) tarRevObj;
					String rev = temp1.getString("name");
					cfg.setRevision(rev);
					apigeeUtil.forceUndeployAPIProxy(cfg);
				}
				// all revisions are undeployed
			}
		}
	}

	public void undeploySharedflowRevison(CommonConfiguration cfg, String environment, String sharedflowName,
			String revision) throws IOException, ItorixException {

		if (null != revision) {
			cfg.setSharedflowName(sharedflowName);
			cfg.setEnvironment(environment);
			cfg.setRevision(revision);
			apigeeUtil.unDeploySharedflow(cfg);

		} else {
			String depResponse = apigeeUtil.getSharedflowDeploymentDetails(cfg);

			ObjectMapper o2 = new ObjectMapper();
			JsonNode targetNode = o2.readTree(depResponse);
			JSONObject targetdeployedEnv = (JSONObject) JSONSerializer.toJSON(targetNode.toString());

			if (targetdeployedEnv.containsKey("revision")) {
				// check rev deployments and undeploy them
				JSONArray revisionArray = targetdeployedEnv.getJSONArray("revision");

				for (Object tarRevObj : revisionArray) {
					JSONObject temp1 = (JSONObject) tarRevObj;
					String rev = temp1.getString("name");
					cfg.setRevision(rev);
					apigeeUtil.forceUndeploySharedflow(cfg);
				}
				// all revisions are undeployed
			}
		}
	}

	private HashMap<String, String> readDevelopers(CommonConfiguration cfg) throws IOException {
		HashMap<String, String> developers = new HashMap<String, String>();
		File backupLocation = null;
		if (cfg.getOldOrg() != null) {
			backupLocation = new File(getBaseRestoreAndMigareDirectory(cfg), "developers");
		} else {
			backupLocation = new File(getBaseRestoreDirectory(cfg), "developers");
		}
		JSONObject appDeveloper = new JSONObject();
		if (backupLocation != null && backupLocation.listFiles() != null && backupLocation.listFiles().length > 0) {
			for (File appDeveloperFile : backupLocation.listFiles()) {
				if (appDeveloperFile.getName().startsWith("."))
					continue;

				String s = IOUtils.toString(new FileInputStream(appDeveloperFile));
				appDeveloper = (JSONObject) JSONSerializer.toJSON(s);

				developers.put(appDeveloper.getString("developerId"), appDeveloper.getString("email"));
			}
			developers.put("default", appDeveloper.getString("email"));
		}
		return developers;
	}

	public CommonConfiguration downloadBackup(String oid, CommonConfiguration cfg) {
		long start = System.currentTimeMillis();
		/*
		 * String backUpLocation = applicationProperties.getRestoreDir() +
		 * timeStamp; cfg.setBackUpLocation(backUpLocation);
		 */
		if (cfg.getBackUpLocation() == null || cfg.getBackUpLocation() == "") {
			cfg.setBackUpLocation(applicationProperties.getBackupDir() + start);
		}

		// Download zip
		String folderToDownload = cfg.getBackUpLocation();
		File folder = new File(folderToDownload);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		try {
			gridFsRepository.findById(
					new GridFsData(oid, cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip", null));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("Exception occurred", e);
		}
		if (null != cfg.getNewOrg() && cfg.getNewOrg().length() > 0) {
			cfg.setOrganization(cfg.getNewOrg());
		}
		ZipUtil.unpack(new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip"),
				new File(cfg.getBackUpLocation()));
		return cfg;
	}

	public CommonConfiguration downloadBackup1(CommonConfiguration cfg) throws IOException {
		long start = System.currentTimeMillis();
		/*
		 * String restoreLocation = applicationProperties.getRestoreDir() +
		 * timeStamp; cfg.setBackUpLocation(restoreLocation);
		 */
		if (cfg.getBackUpLocation() == null || cfg.getBackUpLocation() == "") {
			cfg.setBackUpLocation(applicationProperties.getBackupDir() + start);
		}
		// Download zip
		String folderToDownload = cfg.getBackUpLocation();
		File folder = new File(folderToDownload);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.add("JSESSIONID", cfg.getJsessionId());

		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

		ResponseEntity<byte[]> response = restTemplate.exchange(cfg.getArtifatURL(), HttpMethod.GET, requestEntity,
				byte[].class);

		// byte[] imageBytes = restTemplate.getForObject(cfg.getJfrogUrl(),
		// byte[].class);
		byte[] imageBytes = response.getBody();
		Files.write(Paths.get(cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip"), imageBytes);
		if (null != cfg.getNewOrg() && cfg.getNewOrg().length() > 0) {
			cfg.setOrganization(cfg.getNewOrg());
		}
		ZipUtil.unpack(new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization() + ".zip"),
				new File(cfg.getBackUpLocation() + "/" + cfg.getOrganization()));
		return cfg;
	}

	private File getBaseBackupDirectory(boolean rollover, CommonConfiguration cfg) {
		File backupDirectory = new File(
				cfg.getBackUpLocation() + File.separator + cfg.getOrganization() + "/backups/" + cfg.getOrganization());

		if (rollover && backupDirectory.exists()) {
			backupDirectory.renameTo(new File(backupDirectory.getAbsolutePath() + "__" + System.currentTimeMillis()));
		}

		backupDirectory.mkdirs();
		return backupDirectory;
	}

	private File getBaseRestoreDirectory(CommonConfiguration cfg) {
		File backupDirectory = new File(
				cfg.getBackUpLocation() + File.separator + cfg.getOrganization() + "/backups/" + cfg.getOrganization());
		return backupDirectory;
	}

	private File getBaseRestoreAndMigareDirectory(CommonConfiguration cfg) {
		File backupDirectory = new File(
				cfg.getBackUpLocation() + File.separator + cfg.getOrganization() + "/backups/" + cfg.getOldOrg());
		return backupDirectory;
	}

	public List<ProxyBackUpInfo> getApiProxiesBackupHistory(String interactionid) throws Exception {
		logger.debug("OrganizationController.getApiProxiesBackupHistory : CorelationId= " + interactionid);
		List<ProxyBackUpInfo> list = new LinkedList<>();
		list = baseRepository.findAll(ProxyBackUpInfo.LABEL_CREATED_TIME, "-", ProxyBackUpInfo.class);
		return list;
	}

	public List<AppBackUpInfo> getAppsBackupHistory(String interactionid) throws Exception {
		logger.debug("OrganizationController.getApiProxiesBackupHistory : CorelationId= " + interactionid);
		List<AppBackUpInfo> list = new LinkedList<>();
		list = baseRepository.findAll(AppBackUpInfo.LABEL_CREATED_TIME, "-", AppBackUpInfo.class);
		return list;
	}

	public List<ProductsBackUpInfo> getproductsBackupHistory(String interactionid) throws Exception {
		logger.debug("OrganizationController.getproductsBackupHistory : CorelationId= " + interactionid);
		List<ProductsBackUpInfo> list = new LinkedList<>();
		list = baseRepository.findAll(ProductsBackUpInfo.LABEL_CREATED_TIME, "-", ProductsBackUpInfo.class);
		return list;
	}

	public List<DeveloperBackUpInfo> getDevelopersBackupHistory(String interactionid) throws Exception {
		logger.debug("OrganizationController.getDevelopersBackupHistory : CorelationId= " + interactionid);
		List<DeveloperBackUpInfo> list = new LinkedList<>();
		list = baseRepository.findAll(DeveloperBackUpInfo.LABEL_CREATED_TIME, "-", DeveloperBackUpInfo.class);
		return list;
	}

	public List<OrgBackUpInfo> getOrganizationBackupHistory(String interactionid) throws Exception {
		logger.debug("OrganizationController.getOrganizationBackupHistory : CorelationId= " + interactionid);
		List<OrgBackUpInfo> list = new LinkedList<>();
		list = baseRepository.findAll(OrgBackUpInfo.LABEL_CREATED_TIME, "-", OrgBackUpInfo.class);
		return list;
	}

	public List<ResourceBackUpInfo> getCachesBackupHistory(String interactionid) throws Exception {
		logger.debug("OrganizationController.getCachesBackupHistory : CorelationId= " + interactionid);
		List<ResourceBackUpInfo> list = new LinkedList<>();
		Query query = new Query(
				new Criteria().andOperator(Criteria.where(ResourceBackUpInfo.LABEL_RESOURCE_TYPE).is("cache"),
						Criteria.where(ResourceBackUpInfo.LABEL_RESOURCE_BACKUP_LEVEL).is("org")));
		query.with(Sort.by(Sort.Direction.DESC, ResourceBackUpInfo.LABEL_CREATED_TIME));
		list = baseRepository.find(query, ResourceBackUpInfo.class);
		return list;
	}

	public List<ResourceBackUpInfo> getKVMBackupHistory(String interactionid) throws Exception {
		logger.debug("OrganizationController.getKVMBackupHistory : CorelationId= " + interactionid);
		List<ResourceBackUpInfo> list = new LinkedList<>();
		Query query = new Query(
				new Criteria().andOperator(Criteria.where(ResourceBackUpInfo.LABEL_RESOURCE_TYPE).is("kvm"),
						Criteria.where(ResourceBackUpInfo.LABEL_RESOURCE_BACKUP_LEVEL).is("org")));
		query.with(Sort.by(Sort.Direction.DESC, ResourceBackUpInfo.LABEL_CREATED_TIME));
		list = baseRepository.find(query, ResourceBackUpInfo.class);
		return list;
	}

	public List<ResourceBackUpInfo> getTargetServersBackupHistory(String interactionid) throws Exception {
		logger.debug("OrganizationController.getTargetServersBackupHistory : CorelationId= " + interactionid);
		List<ResourceBackUpInfo> list = new LinkedList<>();
		Query query = new Query(
				new Criteria().andOperator(Criteria.where(ResourceBackUpInfo.LABEL_RESOURCE_TYPE).is("targetserver"),
						Criteria.where(ResourceBackUpInfo.LABEL_RESOURCE_BACKUP_LEVEL).is("org")));
		query.with(Sort.by(Sort.Direction.DESC, ResourceBackUpInfo.LABEL_CREATED_TIME));
		list = baseRepository.find(query, ResourceBackUpInfo.class);
		return list;
	}

	public List<ResourceBackUpInfo> getResourcesBackupHistory(String interactionid) throws Exception {
		logger.debug("OrganizationController.getResourcesBackupHistory : CorelationId= " + interactionid);
		List<ResourceBackUpInfo> list = new LinkedList<>();
		Query query = new Query(
				new Criteria().andOperator(Criteria.where(ResourceBackUpInfo.LABEL_RESOURCE_TYPE).is("resources"),
						Criteria.where(ResourceBackUpInfo.LABEL_RESOURCE_BACKUP_LEVEL).is("org")));
		query.with(Sort.by(Sort.Direction.DESC, ResourceBackUpInfo.LABEL_CREATED_TIME));
		list = baseRepository.find(query, ResourceBackUpInfo.class);
		return list;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getOrgBackUpHistory(String sys, String backuplevel, String interactionid) throws Exception {
		logger.debug("OrganizationController.getOrgBackUpHistory : CorelationId= " + interactionid);
		List<T> list = new LinkedList<>();
		if (sys.equals(Constants.APIGEE_BACKUP_ORG)) {
			list = (List<T>) baseRepository.findAll(OrgBackUpInfo.LABEL_CREATED_TIME, "-", OrgBackUpInfo.class);
		}
		if (sys.equals(Constants.APIGEE_BACKUP_ORG_APIPROXY)) {
			list = (List<T>) baseRepository.findAll(ProxyBackUpInfo.LABEL_CREATED_TIME, "-", ProxyBackUpInfo.class);
		}
		if (sys.equals(Constants.APIGEE_BACKUP_ORG_APP)) {
			list = (List<T>) baseRepository.findAll(AppBackUpInfo.LABEL_CREATED_TIME, "-", AppBackUpInfo.class);
		}

		if (sys.equals(Constants.APIGEE_BACKUP_ORG_RESOURCE) || sys.equals(Constants.APIGEE_BACKUP_ORG_CACHES)
				|| sys.equals(Constants.APIGEE_BACKUP_ORG_KVM) || sys.equals(Constants.APIGEE_BACKUP_ORG_TARGET_SERVER)
				|| sys.equals(Constants.APIGEE_BACKUP_ORG_VIRTUAL_HOST)) {

			Query query = new Query(
					new Criteria().andOperator(Criteria.where(ResourceBackUpInfo.LABEL_RESOURCE_TYPE).is(sys),
							Criteria.where(ResourceBackUpInfo.LABEL_RESOURCE_BACKUP_LEVEL).is(backuplevel)));
			query.with(Sort.by(Sort.Direction.DESC, ResourceBackUpInfo.LABEL_CREATED_TIME));
			list = (List<T>) baseRepository.find(query, ResourceBackUpInfo.class);
		}

		if (sys.equals(Constants.APIGEE_BACKUP_ORG_PRODUCT)) {
			list = (List<T>) baseRepository.findAll(ProductsBackUpInfo.LABEL_CREATED_TIME, "-",
					ProductsBackUpInfo.class);
		}

		if (sys.equals(Constants.APIGEE_BACKUP_ORG_DEVELOPER)) {
			list = (List<T>) baseRepository.findAll(DeveloperBackUpInfo.LABEL_CREATED_TIME, "-",
					DeveloperBackUpInfo.class);
		}

		if (sys.equals(Constants.APIGEE_ENVIRONMENT_BACKUP)) {
			list = (List<T>) baseRepository.findAll(EnvironmentBackUpInfo.LABEL_CREATED_TIME, "-",
					EnvironmentBackUpInfo.class);
		}

		return list;
	}

	public CommonConfiguration deleteBackUp(CommonConfiguration cfg, String oid, String sys) throws IOException {
		switch (sys) {
			case Constants.APIGEE_BACKUP_ORG :
				logger.debug("Deleting OrgBackUpInfo of {}", oid);
				baseRepository.delete(OrgBackUpInfo.LABEL_FILE_OID, oid, OrgBackUpInfo.class);
				break;
			case Constants.APIGEE_BACKUP_ORG_APIPROXY :
				logger.debug("Deleting ProxyBackUpInfo of {}", oid);
				baseRepository.delete(ProxyBackUpInfo.LABEL_FILE_OID, oid, ProxyBackUpInfo.class);
				break;
			/*
			 * case Constants.APIGEE_BACKUP_ORG_PROXY_REVISION:
			 * baseRepository.delete(ProxyRevisionBackUpInfo.LABEL_FILE_OID,
			 * oid, ProxyRevisionBackUpInfo.class); break;
			 */
			case Constants.APIGEE_BACKUP_ORG_PRODUCT :
				logger.debug("Deleting ProductsBackUpInfo of {}", oid);
				baseRepository.delete(ProductsBackUpInfo.LABEL_FILE_OID, oid, ProductsBackUpInfo.class);
				break;
			case Constants.APIGEE_BACKUP_ORG_RESOURCE :
			case Constants.APIGEE_BACKUP_ORG_CACHES :
			case Constants.APIGEE_BACKUP_ORG_KVM :
			case Constants.APIGEE_BACKUP_ORG_TARGET_SERVER :
			case Constants.APIGEE_BACKUP_ORG_VIRTUAL_HOST :
				logger.debug("Deleting ResourceBackUpInfo");
				baseRepository.delete(ResourceBackUpInfo.LABEL_FILE_OID, oid, ResourceBackUpInfo.class);
				break;
			case Constants.APIGEE_ENVIRONMENT_BACKUP :
				logger.debug("Deleting EnvironmentBackUpInfo of {}", oid);
				baseRepository.delete(EnvironmentBackUpInfo.LABEL_FILE_OID, oid, EnvironmentBackUpInfo.class);
				break;
		}
		gridFsRepository.deleteById(oid);
		return cfg;
	}

	public ApigeeOrganizationalVO apigeeOrganizationalView(CommonConfiguration cfg, boolean refresh)
			throws ItorixException, JsonProcessingException, IOException {
		ApigeeOrganizationalVO vo = new ApigeeOrganizationalVO();
		if (refresh) {
			vo.setType(cfg.getType());
			ObjectMapper mapper = new ObjectMapper();
			long start = System.currentTimeMillis();
			if (cfg.getBackUpLocation() == null || cfg.getBackUpLocation() == "") {
				cfg.setBackUpLocation(applicationProperties.getBackupDir() + start);
			}
			final File backupLocation = new File(getBaseBackupDirectory(false, cfg), "apiproxies");
			backupLocation.mkdirs();
			cfg.setExpand(true);
			vo.setName(cfg.getOrganization());
			List<com.itorix.apiwiz.data.management.model.overview.Environment> environmentList = new ArrayList<>();
			try {
				List<String> environments = apigeeUtil.getEnvironmentNames(cfg);
				String apiProductsList = apigeeUtil.listAPIProductsByQuery(cfg);
				String appsList = apigeeUtil.listAppIDsInAnOrganizationByQuery(cfg);
				String developersList = apigeeUtil.listDevelopersByQuery(cfg);
				if (environments != null) {
					for (String environment : environments) {
						com.itorix.apiwiz.data.management.model.overview.Environment env = new com.itorix.apiwiz.data.management.model.overview.Environment();
						env.setName(environment);
						cfg.setEnvironment(environment);
						String deployedProxies = getAPIsDeployedToEnvironment(cfg.getJsessionId(),
								cfg.getOrganization(), cfg.getEnvironment(), cfg.getInteractionid(), cfg.getType());
						// System.out.println(deployedProxies);
						List<String> proxyList = apigeeUtil.listAPIProxies(cfg);
						JsonNode proxyResponseNode = mapper.readTree(deployedProxies).get("aPIProxy");
						List<Proxies> proxiesList = new ArrayList<>();
						for (String proxyName : proxyList) {
							String revison = "";
							cfg.setApiName(proxyName);
							if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
								String deployments = apigeeUtil.getApigeexAPIDeployment(cfg);
								JSONObject deployedNodes = (JSONObject) JSONSerializer.toJSON(deployments);
								if (ObjectUtils.isNotEmpty(deployedNodes.get("deployments"))) {
									JSONArray envApis = (JSONArray) deployedNodes.get("deployments");
									for (int j = 0; j < envApis.size(); j++) {
										JSONObject apiProxyObject = (JSONObject) envApis.get(j);
										String envStr = (String) apiProxyObject.get("environment");
										if (envStr.equals(environment))
											revison = (String) apiProxyObject.get("revision");
									}
								}
							} else {
								// List<Products> productsList = new
								// ArrayList<>();
								JsonNode revisonsNode = proxyResponseNode.get("revision");
								if (revisonsNode != null && revisonsNode.isArray()) {
									for (final JsonNode revisonNode : revisonsNode) {
										revison = revisonNode.get("name").asText();
									}
								}
								APIProxyDeploymentDetailsResponse aPIProxyDeploymentDetailsResponse = apigeeUtil
										.getAPIProxyDeploymentDetails(cfg);
								Environment[] environmentArray = aPIProxyDeploymentDetailsResponse.getEnvironment();
								if (environmentArray != null && environmentArray.length > 0) {
									for (Environment en : environmentArray) {
										if (environment.equals(en.getName())) {
											Revision[] revisionArray = en.getRevision();
											if (revisionArray != null && revisionArray.length > 0) {
												for (Revision re : revisionArray) {
													revison = re.getName();
												}
											}
										}
									}
								}
							}
							if (revison.length() > 0) {
								Proxies pro = new Proxies();
								pro.setName(proxyName);

								JsonNode apiProductsResponseNode = mapper.readTree(apiProductsList);
								JsonNode appsResponseNode = mapper.readTree(appsList);
								JsonNode developersResponseNode = mapper.readTree(developersList);

								List<Products> products = new ArrayList<>();
								List<String> productList = filterMappedProductsForEachProxy(apiProductsResponseNode,
										proxyName);
								for (String product : productList) {
									Products p = new Products();
									p.setName(product);
									List<String> appList = filterMappedAppsForEachProduct(appsResponseNode, product);
									List<Apps> apps = new ArrayList<>();
									for (String appName : appList) {
										Apps app = new Apps();
										app.setName(appName);
										List<String> developerList = filterMappedDevelopersForEachApp(
												developersResponseNode, appName);
										app.setDevelopers(developerList);
										apps.add(app);
									}
									if (!CollectionUtils.isEmpty(apps))
										p.setApps(apps);
									products.add(p);
								}
								pro.setProducts(products);
								pro.setRevision(revison);
								cfg.setRevision(revison);
								try {
									final File versionFile = new File(backupLocation, File.separator + proxyName);
									versionFile.getParentFile().mkdirs();
									byte[] revisionBundle = apigeeUtil.getAnAPIProxyRevision(cfg);
									FileUtils.writeByteArrayToFile(new File(versionFile.getAbsolutePath() + ".zip"),
											revisionBundle);
									ZipUtil.unpack(new File(versionFile.getAbsolutePath() + ".zip"),
											new File(versionFile.getAbsolutePath()));
									Proxies proxyArtifacts = ProcessProxyArtifacts.processArtifacts(
											versionFile.getAbsolutePath() + File.separator + "apiproxy");
									FileUtils.cleanDirectory(new File(cfg.getBackUpLocation()));
									FileUtils.deleteDirectory(new File(cfg.getBackUpLocation()));
									if (proxyArtifacts != null) {
										pro.setCache(proxyArtifacts.getCache());
										pro.setKvm(proxyArtifacts.getKvm());
										pro.setPaths(proxyArtifacts.getPaths());
										pro.setTargetservers(getTargetURL(proxyArtifacts.getTargetservers(), cfg));
										pro.setProxyPolicies(proxyArtifacts.getProxyPolicies());
										pro.setTargetPolicies(proxyArtifacts.getTargetPolicies());
										pro.setBasePath(proxyArtifacts.getBasePath());
									}
								} catch (Exception e) {
									logger.error(e.getMessage(), e);
								}
								proxiesList.add(pro);
							}
						}
						List<Sharedflow> sharedFlows = getSharedflowDetails(cfg, environment);
						env.setProxies(proxiesList);
						env.setSharedFlows(sharedFlows);
						environmentList.add(env);
					}
				}
				vo.setEnvironment(environmentList);
				baseRepository.delete("name", cfg.getOrganization(), "type", cfg.getType(),
						ApigeeOrganizationalVO.class);
				baseRepository.save(vo);
			} catch (Exception e) {
				throw e;
			}
		} else {
			List<ApigeeOrganizationalVO> list = baseRepository.find("name", cfg.getOrganization(), "type",
					cfg.getType(), ApigeeOrganizationalVO.class);
			if (list != null && list.size() > 0) {
				vo = list.get(0);
			}
		}
		return vo;
	}

	private List<Targetserver> getTargetURL(List<Targetserver> targets, CommonConfiguration cfg) throws ItorixException{
		for(Targetserver target : targets) {
			if(target.getName() != null) {
				cfg.setTargetServerName(target.getName());
				String targetStr = apigeeUtil.getTargetServer(cfg);
				JSONObject targetServer = (JSONObject) JSONSerializer.toJSON(targetStr);
				if (ObjectUtils.isNotEmpty(targetServer.get("host"))) {
					String host = (String) targetServer.get("host");
					Integer port = (Integer) targetServer.get("port");
					String protocol = "http";
					if (ObjectUtils.isNotEmpty(targetServer.get("sSLInfo"))) 
						protocol = "https";
					String url = protocol + "://" + host +":" + port;
					target.setUrl(url);
					
				}
			}
		}
		return targets;
	}

	private List<Sharedflow> getSharedflowDetails(CommonConfiguration cfg, String environment) throws ItorixException {

		List<String> sharedFlows = apigeeUtil.listSharedflows(cfg);
		List<Sharedflow> sharedFlowList = new ArrayList<>();
		for (String sharedflowStr : sharedFlows) {
			String revison = "";
			Sharedflow sharedflow = new Sharedflow();
			cfg.setApiName(sharedflowStr);
			sharedflow.setName(sharedflowStr);
			if (cfg.getType() != null && cfg.getType().equalsIgnoreCase("apigeex")) {
				String deployments = apigeeUtil.getApigeexSharedflowDeployment(cfg);
				JSONObject deployedNodes = (JSONObject) JSONSerializer.toJSON(deployments);
				if (ObjectUtils.isNotEmpty(deployedNodes.get("deployments"))) {
					JSONArray envApis = (JSONArray) deployedNodes.get("deployments");
					for (int j = 0; j < envApis.size(); j++) {
						JSONObject apiProxyObject = (JSONObject) envApis.get(j);
						String envStr = (String) apiProxyObject.get("environment");
						if (envStr.equals(environment))
							revison = (String) apiProxyObject.get("revision");
					}
				}
			} else {
				String deployments = apigeeUtil.getSharedflowDeploymentDetails(cfg);
				JSONObject deployedNodes = (JSONObject) JSONSerializer.toJSON(deployments);
				if (ObjectUtils.isNotEmpty(deployedNodes.get("environment"))) {
					JSONArray envApis = (JSONArray) deployedNodes.get("environment");
					for (Object apiObj : envApis) {
						JSONObject envObj = (JSONObject) apiObj;
						String envName = (String) envObj.get("name");
						if (envName.equals(environment)) {
							JSONArray revisions = (JSONArray) envObj.get("revision");
							for (Object rev : revisions) {
								JSONObject revisionObj = (JSONObject) rev;
								revison = (String) revisionObj.get("name");
								break;
							}
						}
					}
				}
			}
			if (revison.length() > 0) {
				sharedflow.setRevision(revison);

			}
			sharedFlowList.add(sharedflow);

		}
		return sharedFlowList;
	}

	private List<String> filterMappedProductsForEachProxy(JsonNode apiProductsResponseNode, String proxyName) {
		List<String> list = new ArrayList<>();
		if (apiProductsResponseNode != null) {
			JsonNode apiProductArrayNode = apiProductsResponseNode.get("apiProduct");
			if (apiProductArrayNode != null) {
				if (apiProductArrayNode.isArray()) {
					for (final JsonNode productNode : apiProductArrayNode) {
						JsonNode proxyNode = productNode.get("proxies");
						if (proxyNode.isArray()) {
							for (final JsonNode proxy : proxyNode) {
								if (proxy.asText().equals(proxyName)) {
									list.add(productNode.get("name").asText());
								}
							}
						}
					}
				}
			}
		}
		return list;
	}

	private List<String> filterMappedAppsForEachProduct(JsonNode appsResponseNode, String productName) {
		List<String> list = new ArrayList<>();
		if (appsResponseNode != null) {
			JsonNode appsArrayNode = appsResponseNode.get("app");
			if (appsArrayNode != null) {
				if (appsArrayNode.isArray()) {
					for (final JsonNode appNode : appsArrayNode) {
						JsonNode credentialsNode = appNode.get("credentials");
						if (credentialsNode != null) {
							for (final JsonNode credentialNode : credentialsNode) {
								JsonNode apiProductsNode = credentialNode.get("apiProducts");
								if (apiProductsNode.isArray()) {
									for (JsonNode apiProductNode : apiProductsNode) {
										if (apiProductNode.get("apiproduct").asText().equals(productName)
												&& apiProductNode.get("status").asText().equals("approved")) {
											list.add(appNode.get("name").asText());
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return list;
	}

	private List<String> filterMappedDevelopersForEachApp(JsonNode developersResponseNode, String appName) {
		List<String> list = new ArrayList<>();
		if (developersResponseNode != null) {
			JsonNode developersArrayNode = developersResponseNode.get("developer");
			if (developersArrayNode != null) {
				if (developersArrayNode.isArray()) {
					for (final JsonNode developerNode : developersArrayNode) {
						JsonNode appsNode = developerNode.get("apps");
						if (appsNode != null && appsNode.isArray()) {
							for (JsonNode appNode : appsNode) {
								if (appNode.asText().equals(appName)) {
									list.add(developerNode.get("email").asText());
								}
							}
						}
					}
				}
			}
		}
		return list;
	}

	public JsonNode populateVoToJson(CommonConfiguration cfg)
			throws JsonGenerationException, JsonMappingException, IOException, ItorixException {
		ObjectMapper mapper = new ObjectMapper();
		ApigeeOrganizationalVO vo = null;
		List<ApigeeOrganizationalVO> list = baseRepository.find("name", cfg.getOrganization(), "type", cfg.getType(),
				ApigeeOrganizationalVO.class);
		if (list != null && list.size() > 0) {
			vo = list.get(0);
		}
		JsonNode rootNode = mapper.createObjectNode();
		if (vo != null) {
			ArrayNode environmentArray = mapper.createArrayNode();
			for (com.itorix.apiwiz.data.management.model.overview.Environment en : vo.getEnvironment()) {
				JsonNode environmentObject = mapper.createObjectNode();
				((ObjectNode) environmentObject).put("name", en.getName());
				ArrayNode proxyArray = mapper.createArrayNode();
				for (Proxies pro : en.getProxies()) {
					JsonNode proxyObject = mapper.createObjectNode();
					((ObjectNode) proxyObject).put("name", pro.getName());

					ArrayNode proxySiblingArray = mapper.createArrayNode();
					// Target
					JsonNode targetObject = mapper.createObjectNode();
					((ObjectNode) targetObject).put("name", "TargetServers");
					ArrayNode targetArray = mapper.createArrayNode();
					for (Targetserver s : pro.getTargetservers()) {
						JsonNode eachTargetObject = mapper.createObjectNode();
						((ObjectNode) eachTargetObject).put("name", s.getName());
						targetArray.add(eachTargetObject);
					}
					((ObjectNode) targetObject).putArray("children").addAll(targetArray);
					proxySiblingArray.add(targetObject);

					// Caches
					JsonNode cacheObject = mapper.createObjectNode();
					((ObjectNode) cacheObject).put("name", "Caches");
					ArrayNode cacheArray = mapper.createArrayNode();
					for (String s : pro.getCache()) {
						JsonNode eachCacheObject = mapper.createObjectNode();
						((ObjectNode) eachCacheObject).put("name", s);
						cacheArray.add(eachCacheObject);
					}
					((ObjectNode) cacheObject).putArray("children").addAll(cacheArray);
					proxySiblingArray.add(cacheObject);

					JsonNode productObject = mapper.createObjectNode();
					((ObjectNode) productObject).put("name", "products");
					ArrayNode productArray = mapper.createArrayNode();
					try {
						for (Products s : pro.getProducts()) {
							JsonNode eachproductObject = mapper.createObjectNode();
							((ObjectNode) eachproductObject).put("name", s.getName());
							JsonNode appObject = mapper.createObjectNode();
							((ObjectNode) appObject).put("name", "apps");
							ArrayNode appArray = mapper.createArrayNode();
							ArrayNode appSiblingArray = mapper.createArrayNode();
							for (Apps a : s.getApps()) {
								JsonNode eachappObject = mapper.createObjectNode();
								((ObjectNode) eachappObject).put("name", a.getName());
								appSiblingArray.add(eachappObject);
								JsonNode developerObject = mapper.createObjectNode();
								((ObjectNode) developerObject).put("name", "developers");
								ArrayNode developerArray = mapper.createArrayNode();
								ArrayNode developerSiblingArray = mapper.createArrayNode();
								for (String d : a.getDevelopers()) {
									JsonNode eachdeveloperObject = mapper.createObjectNode();
									((ObjectNode) eachdeveloperObject).put("name", d);
									developerSiblingArray.add(eachdeveloperObject);
								}
								((ObjectNode) developerObject).putArray("_children").addAll(developerSiblingArray);
								developerArray.add(developerObject);
								((ObjectNode) eachappObject).putArray("_children").addAll(developerArray);
							}

							((ObjectNode) appObject).putArray("_children").addAll(appSiblingArray);
							appArray.add(appObject);
							((ObjectNode) eachproductObject).putArray("_children").addAll(appArray);
							productArray.add(eachproductObject);
						}
					} catch (Exception e) {
					}
					((ObjectNode) productObject).putArray("_children").addAll(productArray);
					proxySiblingArray.add(productObject);

					((ObjectNode) proxyObject).putArray("_children").addAll(proxySiblingArray);
					proxyArray.add(proxyObject);
				}
				((ObjectNode) environmentObject).putArray("_children").addAll(proxyArray);
				environmentArray.add(environmentObject);
			}
			((ObjectNode) rootNode).put("name", vo.getName());
			((ObjectNode) rootNode).putArray("_children").addAll(environmentArray);
		}
		return rootNode;
	}

	/*
	 * private ProxyData populateProxyData(CommonConfiguration cfg){ ProxyData
	 * proxyData=new ProxyData(); Env env=new Env();
	 * env.setName(cfg.getEnvironment()); List<Env> envList=new ArrayList<>();
	 * envList.add(env); OrgEnv orgEnv=new OrgEnv();
	 * orgEnv.setName(cfg.getOrganization()); orgEnv.setType(cfg.getType());
	 * orgEnv.setEnvs(envList); List<OrgEnv> orgList=new ArrayList<>();
	 * orgList.add(orgEnv); OrgEnvs orgEnvs=new OrgEnvs();
	 * orgEnvs.setOrgEnvs(orgList); proxyData.setOrgEnvs(orgEnvs);
	 * proxyData.setProxyName(cfg.getApiName()); return proxyData; }
	 */

}