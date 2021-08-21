package com.itorix.apiwiz.datamanagement.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.data.management.dao.PolicyMappingDAO;
import com.itorix.apiwiz.data.management.model.PolicyMapping;
import com.itorix.apiwiz.data.management.model.PolicyMappings;
import com.itorix.apiwiz.datamanagement.service.PolicyMappingService;

@CrossOrigin
@RestController
public class PolicyMappingServiceImpl implements PolicyMappingService {
	@Autowired
	private PolicyMappingDAO policyMappingDAO;

	public ResponseEntity<Void> updateEnvironmentSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PolicyMappings policyMappings, @RequestHeader(value = "jsessionid") String jsessionid)
			throws Exception {
		policyMappingDAO.savePolicyMapping(policyMappings);
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	public ResponseEntity<PolicyMappings> getEnvironmentSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid) throws Exception {
		return new ResponseEntity<PolicyMappings>(policyMappingDAO.getPolicyMappings(), HttpStatus.OK);
	}
}
