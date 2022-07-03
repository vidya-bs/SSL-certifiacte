package com.itorix.apiwiz.devportal.service;

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

@CrossOrigin(origins = "*")
@RestController
public interface DevportalService {

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('PRO','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/organizations/{org}/developers")
	public ResponseEntity<String> createDeveloper(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type") String type, @PathVariable("org") String org, @RequestBody String body)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/organizations/{org}/developers/{email:.+}/apps")
	public org.springframework.http.ResponseEntity<String> registerApp(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("email") String email, @RequestBody String body) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/organizations/{org}/developers/{email:.+}/apps/{appName}")
	public org.springframework.http.ResponseEntity<String> updateApp(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("email") String email, @PathVariable("appName") String appName, @RequestBody String body)
			throws Exception;
	
	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/organizations/{org}/developers/{email:.+}/apps/{appName}/keys/{appKey}")
	public org.springframework.http.ResponseEntity<String> updateAppProduct(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("email") String email, @PathVariable("appName") String appName,
			@PathVariable("appKey") String appKey, @RequestBody String body)
			throws Exception;
	
	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/organizations/{org}/developers/{email:.+}/apps/{appName}/keys/{appKey}/apiproducts/{product}")
	public org.springframework.http.ResponseEntity<String> deleteAppProduct(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("email") String email, @PathVariable("appName") String appName,
			@PathVariable("appKey") String appKey, @PathVariable("product") String product)
			throws Exception;
	
	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/organizations/{org}/developers/{email:.+}/apps/{appName}")
	public org.springframework.http.ResponseEntity<String> deleteApp(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("email") String email, @PathVariable("appName") String appName)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/organizations/{org}/apiproducts")
	public org.springframework.http.ResponseEntity<String> getProducts(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type") String type, @PathVariable("org") String org,
			@RequestParam(value = "expand", required = false) String expand) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/organizations/{org}/developers/{email:.+}/apps")
	public org.springframework.http.ResponseEntity<String> getApps(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("email") String email, @RequestParam(value = "expand", required = false) String expand)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/organizations/{org}/developers/{email:.+}/apps/{appName}")
	public org.springframework.http.ResponseEntity<String> getApp(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("email") String email, @PathVariable("appName") String appName) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/organizations/{org}/environments/{env}/stats/apps")
	public org.springframework.http.ResponseEntity<String> getPortalStats(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("env") String env, @RequestParam(value = "select", required = false) String select,
			@RequestParam(value = "timeRange", required = false) String timeRange,
			@RequestParam(value = "timeUnit", required = false) String timeUnit,
			@RequestParam(value = "filter", required = false) String filter) throws Exception;
}
