package com.itorix.apiwiz.data.management.business;

import java.util.List;

import org.springframework.stereotype.Service;

import com.itorix.apiwiz.common.model.apigee.ApigeeConfigurationVO;
import com.itorix.apiwiz.common.model.apigee.ApigeeIntegrationVO;
import com.itorix.apiwiz.common.model.apigee.ApigeeServiceUser;
import com.itorix.apiwiz.common.model.exception.ItorixException;

@Service
public interface ApigeeConfigurationBusiness {

	/**
	 * getConfiguration
	 *
	 * @param interactionid
	 * @param jsessionid
	 * 
	 * @return
	 */
	public List<ApigeeConfigurationVO> getConfiguration(String interactionid, String jsessionid) throws ItorixException;

	/**
	 * createConfiguration
	 *
	 * @param list
	 * @param interactionid
	 * @param jsessionid
	 * 
	 * @throws ItorixException
	 */
	public void createConfiguration(List<ApigeeConfigurationVO> list, String interactionid, String jsessionid)
			throws ItorixException;

	/**
	 * updateConfiguration
	 *
	 * @param list
	 * @param interactionid
	 * @param jsessionid
	 * 
	 * @throws ItorixException
	 */
	public void updateConfiguration(List<ApigeeConfigurationVO> list, String interactionid, String jsessionid)
			throws ItorixException;

	/**
	 * deleteConfiguration
	 *
	 * @param apigeeConfigurationVO
	 * @param interactionid
	 * 
	 * @throws ItorixException
	 */
	public void deleteConfiguration(ApigeeConfigurationVO apigeeConfigurationVO, String interactionid)
			throws ItorixException;

	/**
	 * getApigeeHost
	 *
	 * @param type
	 * @param org
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	public Object getApigeeHost(String type, String org) throws ItorixException;

	public Object getApigeexHost(String type, String org) throws ItorixException;
	
	public Object getApigeeAuthorization(String type, String org) throws ItorixException;
	
	public Object getApigeexAuthorization(String type, String org) throws Exception;

	public void updateServiceAccount(List<ApigeeServiceUser> apigeeServiceUsers) throws ItorixException;

	public List<ApigeeServiceUser> getServiceAccounts() throws ItorixException;

	public void createApigeeIntegration(ApigeeIntegrationVO apigeeIntegrationVO) throws ItorixException;

	public void updateApigeeIntegration(ApigeeIntegrationVO apigeeIntegrationVO) throws ItorixException;

	public List<ApigeeIntegrationVO> listApigeeIntegrations() throws ItorixException;

	public ApigeeIntegrationVO getApigeeIntegration(String id) throws ItorixException;

	public void deleteApigeeIntegration(String id) throws ItorixException;
}
