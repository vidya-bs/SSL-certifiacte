package com.itorix.apiwiz.devportal.service;

import org.springframework.http.ResponseEntity;
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

	@RequestMapping(method = RequestMethod.POST, value = "/v1/organizations/{org}/developers")
	public ResponseEntity<String> createDeveloper(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "type") String type,
			@PathVariable("org") String org, 
			@RequestBody String body) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/organizations/{org}/developers/{email:.+}/apps")
	public org.springframework.http.ResponseEntity<String> registerApp(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "type", required = false) String type,
			@PathVariable("org") String org,
			@PathVariable("email") String email,
			@RequestBody String body) throws Exception;
	
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/organizations/{org}/developers/{email:.+}/apps/{appName}")
	public org.springframework.http.ResponseEntity<String> updateApp(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "type", required = false) String type,
			@PathVariable("org") String org,
			@PathVariable("email") String email,
			@PathVariable("appName") String appName,
			@RequestBody String body) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/organizations/{org}/apiproducts")
	public org.springframework.http.ResponseEntity<String> getProducts(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "type") String type,
			@PathVariable("org") String org,
			@RequestParam(value="expand",required=false) String expand) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/organizations/{org}/developers/{email:.+}/apps")
	public org.springframework.http.ResponseEntity<String> getApps(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "type", required = false) String type,
			@PathVariable("org") String org,
			@PathVariable("email") String email,
			@RequestParam(value="expand",required=false) String expand) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/organizations/{org}/developers/{email:.+}/apps/{appName}")
	public org.springframework.http.ResponseEntity<String> getApp(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "type", required = false) String type,
			@PathVariable("org") String org,
			@PathVariable("email") String email,
			@PathVariable("appName") String appName) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/organizations/{org}/environments/{env}/stats/apps")
	public org.springframework.http.ResponseEntity<String> getPortalStats(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "type", required = false) String type,
			@PathVariable("org") String org,
			@PathVariable("env") String env,
			@RequestParam(value="select",required=false) String select,
			@RequestParam(value="timeRange",required=false) String timeRange,
			@RequestParam(value="timeUnit",required=false) String timeUnit,
			@RequestParam(value="filter",required=false) String filter) throws Exception;
	
}