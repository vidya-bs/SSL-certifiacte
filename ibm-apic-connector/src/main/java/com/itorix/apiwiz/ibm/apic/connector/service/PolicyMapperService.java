package com.itorix.apiwiz.ibm.apic.connector.service;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.ibm.apic.connector.model.APIDropdownListItem;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/v1/ibm-apic/policy-map")
public interface PolicyMapperService {

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/api-dropdown")
	public ResponseEntity<Object> getAPIDropdownList(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid
	) throws ItorixException, Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/")
	public ResponseEntity<Object> fetchPolicyMapForSelectedAPIs(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
			@RequestBody List<APIDropdownListItem> selectedAPIs,
			@RequestHeader(value = "pageSize", required = false,defaultValue = "10") int pageSize,
			@RequestHeader(value = "offset", required = false,defaultValue = "1") int offset
	) throws ItorixException, Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/")
	public ResponseEntity<Object> updatePolicyMap(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
			@RequestBody List<Map<String,String>> updatedPolicyMap
	) throws ItorixException, Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/apigee/policies")
	public ResponseEntity<Object> getApigeePolicies(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
			@RequestHeader(value = "searchKey", required = false) String searchKey
	) throws ItorixException, Exception;
}
