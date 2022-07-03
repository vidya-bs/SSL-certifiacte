package com.itorix.apiwiz.datamanagement.serviceimpl;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.itorix.apiwiz.common.model.apigeeX.ApigeeXConfigurationVO;
import com.itorix.apiwiz.common.model.apigeeX.ApigeeXEnvironment;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.data.management.dao.ApigeeXIntegrationDAO;
import com.itorix.apiwiz.datamanagement.service.ApigeeXConfigurationService;

@CrossOrigin
@RestController
public class ApigeeXConfigurationServiceImpl implements ApigeeXConfigurationService {

	@Autowired
	private ApigeeXIntegrationDAO apigeeXIntegrationDAO;
	
	@Override
	public ResponseEntity<Void> createConfiguration(
			String interactionid, String jsessionid, 
			MultipartFile envFile,
			String org) throws Exception {
		ApigeeXConfigurationVO apigeeXConfigurationVo = apigeeXIntegrationDAO.getConfiguration(org);
		if(apigeeXConfigurationVo != null){
			throw new ItorixException();
		}
		byte[] bytes = envFile.getBytes();
		String jsonStr = new String(bytes, StandardCharsets.UTF_8);
		ApigeeXConfigurationVO apigeeXConfigurationVO = new ApigeeXConfigurationVO();
		apigeeXConfigurationVO.setOrgName(org);
		apigeeXConfigurationVO.setJsonKey(jsonStr);
		apigeeXConfigurationVO = apigeeXIntegrationDAO.poplulateEnvironments(apigeeXConfigurationVO);
		apigeeXIntegrationDAO.saveJSONKey(apigeeXConfigurationVO);
		return new ResponseEntity<>(HttpStatus.CREATED);
		
	}

	@Override
	public ResponseEntity<?> getConfigurations(String interactionid, 
			String jsessionid) throws Exception {
		return new ResponseEntity<>(apigeeXIntegrationDAO.getConfigurations(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getConfiguration(String interactionid, 
			String jsessionid, String org) throws Exception {
		return new ResponseEntity<>(apigeeXIntegrationDAO.getConfiguration(org), HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<?> createEnvironmentSchedule(
			String interactionid,
			String jsessionid,
			String org,
			ApigeeXEnvironment environment)
			throws Exception{
		return new ResponseEntity<>(apigeeXIntegrationDAO.updateKVM(org, environment), HttpStatus.OK);
	}
	
	public ResponseEntity<?> deleteConfiguration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid,
			@PathVariable("orgId") String orgId) throws Exception{
		apigeeXIntegrationDAO.deleteConfiguration(orgId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
}
