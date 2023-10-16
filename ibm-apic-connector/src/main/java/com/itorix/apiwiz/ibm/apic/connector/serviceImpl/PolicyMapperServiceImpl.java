package com.itorix.apiwiz.ibm.apic.connector.serviceImpl;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.ibm.apic.connector.dao.PolicyMapDAO;
import com.itorix.apiwiz.ibm.apic.connector.model.APIDropdownListItem;
import com.itorix.apiwiz.ibm.apic.connector.service.PolicyMapperService;
import com.itorix.apiwiz.ibm.apic.connector.util.IBMAPICSpecUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.itorix.apiwiz.ibm.apic.connector.util.HttpUtil.response;

@Service
@CrossOrigin
@RestController
public class PolicyMapperServiceImpl implements PolicyMapperService {

	private static final Logger logger = LoggerFactory.getLogger(PolicyMapperServiceImpl.class);

	@Autowired
	private PolicyMapDAO policyMapDAO;

	@Autowired
	private IBMAPICSpecUtil ibmapicSpecUtil;

	@Override
	public ResponseEntity<Object> getAPIDropdownList(String interactionid, String jsessionid)
			throws ItorixException, Exception {
		try{
			return new ResponseEntity<>(ibmapicSpecUtil.getAPIDropdownList(),HttpStatus.OK);
		}catch (Exception ex){
			logger.error("Could Not Query OAS With IBM Configs:" + ex.getMessage());
			return response("Could Not Query OAS With IBM Configs",HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<Object> fetchPolicyMapForSelectedAPIs(String interactionid, String jsessionid,
			List<APIDropdownListItem> selectedAPIs, int pageSize, int offset)
			throws ItorixException, Exception {
		try{
			return new ResponseEntity<>(ibmapicSpecUtil.fetchPolicyMapForSelectedAPIs(selectedAPIs,pageSize,offset),HttpStatus.OK);
		}catch (Exception ex){
			logger.error("Could Not Fetch Policies For Selected APIs:" + ex.getMessage());
			return response("Could Not Fetch Policies For Selected APIs",HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<Object> updatePolicyMap(String interactionid, String jsessionid,
			List<Map<String,String>> updatedPolicyMap) throws ItorixException, Exception {
		try{
			policyMapDAO.updatePolicyMap(updatedPolicyMap);
			return new ResponseEntity<>(updatedPolicyMap,HttpStatus.OK);
		}catch (Exception ex){
			logger.error(String.format("%s :: %s", ErrorCodes.errorMessage.get("IBM-APIC-Connector-3"),ex.getMessage()));
			return response(ErrorCodes.errorMessage.get("IBM-APIC-Connector-3"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<Object> getApigeePolicies(String interactionid, String jsessionid, String searchKey)
			throws ItorixException, Exception {
		try{
			return new ResponseEntity<>(policyMapDAO.getApigeePolicies(searchKey),HttpStatus.OK);
		}catch (Exception ex){
			logger.error(String.format("%s :: %s", ErrorCodes.errorMessage.get("IBM-APIC-Connector-3"),ex.getMessage()));
			return response(ErrorCodes.errorMessage.get("IBM-APIC-Connector-3"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
