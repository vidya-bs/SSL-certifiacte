package com.itorix.apiwiz.data.management.business;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.itorix.apiwiz.common.model.apigee.CommonConfiguration;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.data.management.model.AppBackUpInfo;
import com.itorix.apiwiz.data.management.model.BackupInfo;
import com.itorix.apiwiz.data.management.model.DeveloperBackUpInfo;
import com.itorix.apiwiz.data.management.model.OrgBackUpInfo;
import com.itorix.apiwiz.data.management.model.ProductsBackUpInfo;
import com.itorix.apiwiz.data.management.model.ProxyBackUpInfo;
import com.itorix.apiwiz.data.management.model.ResourceBackUpInfo;
import com.itorix.apiwiz.data.management.model.overview.ApigeeOrganizationalVO;

@Service
public interface OrganizationBusiness {
	/**
	 * This method is used to get the list of environments for an organization.
	 * 
	 * @param jsessionid
	 * @param organization
	 * @param interactionid
	 * @param type
	 * @return
	 * @throws ItorixException
	 */
	public List<String> getEnvironmentNames(String jsessionid, String organization, String interactionid, String type)
			throws ItorixException;

	/**
	 * Using this we will get the list of proxies for an organization.
	 * 
	 * @param jsessionid
	 * @param organization
	 * @param interactionid
	 * @param type
	 * @return
	 * @throws ItorixException
	 */
	public List<String> listAPIProxies(String jsessionid, String organization, String interactionid, String type)
			throws ItorixException;

	/**
	 * This will return the deployed proxies for specified org & env.
	 * 
	 * @param jsessionid
	 * @param organization
	 * @param environment
	 * @param interactionid
	 * @param type
	 * @return
	 * @throws ItorixException
	 */
	public String getAPIsDeployedToEnvironment(String jsessionid, String organization, String environment,
			String interactionid, String type) throws ItorixException;

	/**
	 * This method will do the backup of api's or proxies.
	 * 
	 * @param cfg
	 * @return
	 * @throws Exception
	 */
	public BackupInfo backupProxies(CommonConfiguration cfg) throws Exception;

	/**
	 * This method will do the backup of shared flows.
	 * 
	 * @param cfg
	 * @return
	 * @throws Exception
	 */
	public BackupInfo backupSharedflows(CommonConfiguration cfg) throws Exception;

	/**
	 * This method will do the backup of apps.
	 * 
	 * @param cfg
	 * @return
	 * @throws Exception
	 */
	public BackupInfo backUpApps(CommonConfiguration cfg) throws Exception;

	/**
	 * This method will do the backup of products.
	 * 
	 * @param cfg
	 * @return
	 * @throws Exception
	 */
	public BackupInfo backupProducts(CommonConfiguration cfg) throws Exception;

	/**
	 * This method will do the backup of developers.
	 * 
	 * @param cfg
	 * @return
	 * @throws Exception
	 */
	public BackupInfo backupDevelopers(CommonConfiguration cfg) throws Exception;

	/**
	 * This method will do the backup of resources.
	 * 
	 * @param cfg
	 * @return
	 * @throws Exception
	 */
	public BackupInfo backupResources(CommonConfiguration cfg) throws Exception;

	/**
	 * This method will do the backup of Organization.
	 * 
	 * @param cfg
	 * @return
	 * @throws Exception
	 */
	public BackupInfo backUpOrganization(CommonConfiguration cfg) throws Exception;

	/**
	 * This method will do the backup of Caches.
	 * 
	 * @param cfg
	 * @return
	 * @throws Exception
	 */
	public BackupInfo backupCaches(CommonConfiguration cfg) throws Exception;

	/**
	 * This method will do the backup of KVM's.
	 * 
	 * @param cfg
	 * @return
	 * @throws Exception
	 */
	public BackupInfo backupKVM(boolean delete, CommonConfiguration cfg) throws Exception;

	/**
	 * This method will do the backup of Target Servers.
	 * 
	 * @param cfg
	 * @return
	 * @throws Exception
	 */
	public BackupInfo backupTargetServers(CommonConfiguration cfg) throws Exception;

	/**
	 * Using this method we can restore the developer's
	 * 
	 * @param cfg
	 * @return
	 * @throws Exception
	 */
	public BackupInfo restoreAppDevelopers1(CommonConfiguration cfg) throws Exception;

	/**
	 * This method we can restore the developer's
	 * 
	 * @param cfg
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ItorixException
	 */
	public String restoreAppDevelopers(CommonConfiguration cfg)
			throws IOException, InterruptedException, ItorixException;

	/**
	 * This method will restore the resources.
	 * 
	 * @param cfg
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ItorixException
	 */
	public BackupInfo restoreResources(CommonConfiguration cfg)
			throws IOException, InterruptedException, ItorixException;

	public String restoreKVM(CommonConfiguration cfg) throws IOException, InterruptedException, ItorixException;

	public String restoreResource(CommonConfiguration cfg) throws IOException, InterruptedException, ItorixException;

	public String restoreAPIProxies(String oid, CommonConfiguration cfg)
			throws IOException, InterruptedException, RestClientException, ItorixException;

	public String restoreAPIProxies1(CommonConfiguration cfg) throws IOException, InterruptedException, ItorixException;

	public String restoreSharedflows1(CommonConfiguration cfg)
			throws IOException, InterruptedException, ItorixException;

	public BackupInfo restoreAPPs(CommonConfiguration cfg) throws IOException, InterruptedException, ItorixException;

	public String restoreAPP(CommonConfiguration cfg) throws IOException, InterruptedException, ItorixException;

	public BackupInfo restoreAPIProducts1(CommonConfiguration cfg) throws Exception;

	public String restoreAPIProducts(CommonConfiguration cfg) throws Exception;

	public BackupInfo restoreApiProxies(CommonConfiguration cfg) throws Exception;

	public BackupInfo restoreSharedflows(CommonConfiguration cfg) throws Exception;

	public BackupInfo restoreOrganization(CommonConfiguration cfg) throws Exception;

	public void undeployProxyRevision(CommonConfiguration cfg, String environment, String apiName, String revision)
			throws IOException, ItorixException;

	public void undeploySharedflowRevison(CommonConfiguration cfg, String environment, String sharedflowName,
			String revision) throws IOException, ItorixException;

	public List<ProxyBackUpInfo> getApiProxiesBackupHistory(String interactionid) throws Exception;

	public List<AppBackUpInfo> getAppsBackupHistory(String interactionid) throws Exception;

	public List<ProductsBackUpInfo> getproductsBackupHistory(String interactionid) throws Exception;

	public List<DeveloperBackUpInfo> getDevelopersBackupHistory(String interactionid) throws Exception;

	public List<OrgBackUpInfo> getOrganizationBackupHistory(String interactionid) throws Exception;

	public List<ResourceBackUpInfo> getCachesBackupHistory(String interactionid) throws Exception;

	public List<ResourceBackUpInfo> getKVMBackupHistory(String interactionid) throws Exception;

	public List<ResourceBackUpInfo> getTargetServersBackupHistory(String interactionid) throws Exception;

	public List<ResourceBackUpInfo> getResourcesBackupHistory(String interactionid) throws Exception;

	@SuppressWarnings("unchecked")
	public <T> List<T> getOrgBackUpHistory(String sys, String backuplevel, String interactionid) throws Exception;

	public CommonConfiguration deleteBackUp(CommonConfiguration cfg, String oid, String sys) throws IOException;
	public JsonNode populateVoToJson(CommonConfiguration cfg) throws JsonGenerationException, JsonMappingException, IOException, ItorixException;
	public ApigeeOrganizationalVO apigeeOrganizationalView(CommonConfiguration cfg,boolean refresh) throws ItorixException, JsonProcessingException, IOException;

}
