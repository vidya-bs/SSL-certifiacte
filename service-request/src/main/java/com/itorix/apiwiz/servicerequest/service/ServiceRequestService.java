package com.itorix.apiwiz.servicerequest.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.servicerequest.model.ServiceRequest;
import com.itorix.apiwiz.servicerequest.model.ServiceRequestComments;

@CrossOrigin
@RestController
public interface ServiceRequestService {

	@PreAuthorize("hasAnyRole('ADMIN','DEVELOPER','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/config/servicerequests")
	public ResponseEntity<?> createServiceRequest(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody ServiceRequest config, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','DEVELOPER','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/config/servicerequests")
	public ResponseEntity<?> updateServiceRequest(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody ServiceRequest config, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','DEVELOPER','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/config/servicerequests/changestatus")
	public ResponseEntity<?> changeServiceRequestStatus(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "isSaaS", required = false) boolean isSaaS,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','DEVELOPER','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/config/servicerequests/loghistory")
	public ResponseEntity<?> logHistory(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "isSaaS", required = false) boolean isSaaS,
			@RequestBody ServiceRequestComments serviceRequestComments,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/servicerequests/loghistory")
	public ResponseEntity<Object> getLogHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "isSaaS", required = false) boolean isSaaS,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/servicerequests")
	public ResponseEntity<Object> getservicerequests(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize) throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/servicerequest")
	public ResponseEntity<Object> getservicerequestsByParameters(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "isSaaS", required = false) boolean isSaaS) throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/servicerequests/detail")
	public ResponseEntity<Object> getservicerequestsByParameters(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "isSaaS", required = false) boolean isSaaS) throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/service-requests/stats")
	public ResponseEntity<?> getServicerequestStats(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "timeunit", required = false) String timeunit,
			@RequestParam(value = "timerange", required = false) String timerange) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','DEVELOPER','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/config/servicerequests/{requestId}/revert")
	public ResponseEntity<Void> revertConfig(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable(value = "requestId") String requestId, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/servicerequests/{serviceRequestType}/history")
	public ResponseEntity<Object> getHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable(value = "serviceRequestType") String serviceRequestType,
			@RequestParam(value = "org") String org,
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "name") String name,
			@RequestParam(value = "isSaaS", required=false) boolean isSaaS,
			@RequestHeader(value="JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset) throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/servicerequest/search")
	public ResponseEntity<Object> configSearch(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "type") String type, @RequestParam(value = "name") String name,
			@RequestParam(value = "limit") int limit) throws Exception;

}
