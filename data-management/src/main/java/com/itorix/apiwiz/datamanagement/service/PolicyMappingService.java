package com.itorix.apiwiz.datamanagement.service;



import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.data.management.model.PolicyMapping;
import com.itorix.apiwiz.data.management.model.PolicyMappings;


@CrossOrigin
@RestController
public interface PolicyMappingService {

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/policy-mapping")
	public ResponseEntity<Void>  updateEnvironmentSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PolicyMappings policyMappings, 
			@RequestHeader(value = "jsessionid") String jsessionid)throws Exception;


	@RequestMapping(method = RequestMethod.GET, value = "/v1/policy-mapping")
	public ResponseEntity<PolicyMappings>  getEnvironmentSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid)throws Exception;

}
