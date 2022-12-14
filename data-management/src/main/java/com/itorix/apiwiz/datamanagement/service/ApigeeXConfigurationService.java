package com.itorix.apiwiz.datamanagement.service;

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
import org.springframework.web.multipart.MultipartFile;

import com.itorix.apiwiz.common.model.apigeeX.ApigeeXEnvironment;

@CrossOrigin
@RestController
public interface ApigeeXConfigurationService {

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/apigeex/configurations", consumes = {
			"multipart/form-data"})
	public ResponseEntity<Void> createConfiguration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid,
			@RequestParam(value = "envFile") MultipartFile envFile, @RequestParam("org") String org) throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/apigeex/configurations", consumes = {
			"multipart/form-data"})
	public ResponseEntity<Void> updateConfiguration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid,
			@RequestParam(value = "envFile") MultipartFile envFile, @RequestParam("org") String org) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apigeex/configurations")
	public ResponseEntity<?> getConfigurations(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apigeex/configurations/{org}")
	public ResponseEntity<?> getConfiguration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("org") String org,
			@RequestParam(value = "refresh", required = false) String refresh) throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/apigeex/configurations/{org}")
	public ResponseEntity<?> createEnvironmentSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("org") String org,
			@RequestBody ApigeeXEnvironment environment) throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/apigeex/configurations/{orgId}")
	public ResponseEntity<?> deleteConfiguration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("orgId") String orgId)
			throws Exception;

}
