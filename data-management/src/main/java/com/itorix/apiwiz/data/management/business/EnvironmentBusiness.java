package com.itorix.apiwiz.data.management.business;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.itorix.apiwiz.common.model.apigee.CommonConfiguration;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.data.management.model.BackupInfo;
import com.itorix.apiwiz.data.management.model.EnvironmentBackUpInfo;
import com.itorix.apiwiz.data.management.model.ProxyBackUpInfo;
import com.itorix.apiwiz.data.management.model.ResourceBackUpInfo;

import net.sf.json.JSONArray;

@Service
public interface EnvironmentBusiness {
	/**
	 * doEnvironmentBackUp
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public BackupInfo doEnvironmentBackUp(CommonConfiguration cfg) throws Exception;

	/**
	 * backupProxies
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public BackupInfo backupProxies(CommonConfiguration cfg) throws Exception;

	/**
	 * getEnvironmentDepolyedProxies
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	public List<String> getEnvironmentDepolyedProxies(CommonConfiguration cfg) throws Exception;

	/**
	 * restoreEnvironment
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public String restoreEnvironment(CommonConfiguration cfg) throws Exception;

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
	public String restoreAPIProxies1(CommonConfiguration cfg) throws IOException, InterruptedException, ItorixException;

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
			throws IOException, InterruptedException, ItorixException;

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
			throws IOException, InterruptedException, ItorixException;

	/**
	 * backupResources
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public BackupInfo backupResources(CommonConfiguration cfg) throws Exception;

	/**
	 * backupResource
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	public JSONArray backupResource(CommonConfiguration cfg) throws ItorixException;

	/**
	 * getEnvironmentBackupHistory
	 *
	 * @param interactionid
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public List<EnvironmentBackUpInfo> getEnvironmentBackupHistory(String interactionid) throws Exception;

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
			throws Exception;

	/**
	 * getCachesBackupHistory
	 *
	 * @param interactionid
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public List<ResourceBackUpInfo> getCachesBackupHistory(String interactionid) throws Exception;

	/**
	 * getTargetServersBackupHistory
	 *
	 * @param interactionid
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public List<ResourceBackUpInfo> getTargetServersBackupHistory(String interactionid) throws Exception;

	/**
	 * getKVMBackupHistory
	 *
	 * @param interactionid
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public List<ResourceBackUpInfo> getKVMBackupHistory(String interactionid) throws Exception;
}
