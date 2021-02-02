package com.itorix.apiwiz.serviceregistry.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.serviceregistry.model.ServiceRegistryEntriesResponse;
import com.itorix.apiwiz.serviceregistry.model.documents.ServiceRegistry;
import com.itorix.apiwiz.serviceregistry.model.documents.ServiceRegistryColumns;
import com.itorix.apiwiz.serviceregistry.model.documents.ServiceRegistryList;
import com.itorix.apiwiz.serviceregistry.model.documents.ServiceRegistryResponse;

@CrossOrigin
@RestController
public interface ServiceRegistryService {


	@RequestMapping(method = RequestMethod.GET, value = "/v1/service-registry/columns")
	public ResponseEntity<ServiceRegistryColumns> getServiceRegistryColumns(@RequestHeader(value = "JSESSIONID") String jsessionid);

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/service-registry/columns")
	public ResponseEntity<?> createOrUpdateServRegColumns(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody ServiceRegistryColumns registryColumns);

	//// service registry ///////
	// https://itorix.atlassian.net/browse/IE-166
	@RequestMapping(method = RequestMethod.POST, value = "/v1/service-registry")
	public ResponseEntity<?> createServiceRegistry(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody ServiceRegistryList serviceRegistry) throws ItorixException;

	// https://itorix.atlassian.net/browse/IE-167
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/service-registry/{service-registry-id}")
	public ResponseEntity<?> updateServiceRegistry(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("service-registry-id") String serviceRegistryId,
			@RequestBody ServiceRegistryList serviceRegistry) throws ItorixException;

	// https://itorix.atlassian.net/browse/IE-171
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/service-registry/{service-registry-id}")
	public ResponseEntity<?> deleteServiceRegistry(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("service-registry-id") String serviceRegistryId) throws ItorixException;


	//https://itorix.atlassian.net/browse/IE-164
	@RequestMapping(method = RequestMethod.GET, value = "/v1/service-registry")
	public ResponseEntity<ServiceRegistryResponse> getServiceRegistry(
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize);

	//https://itorix.atlassian.net/browse/IE-168
//	@RequestMapping(method = RequestMethod.GET, value = "/v1/service-registry")
//	public ResponseEntity<ServiceRegistryResponseDto> getServiceRegistries(
//			@RequestHeader(value = "JSESSIONID") String jsessionid);
	
	////////////////////////////////////////////////////////// ServiceRegistryEntriesResponsesDto
	////////////////////////////////////////////////////////// ServiceRegistriesResponseDto

	//https://itorix.atlassian.net/browse/IE-161
	@RequestMapping(method = RequestMethod.POST, value = "/v1/service-registry/{service-registry-id}/rows")
	public ResponseEntity<?> createServiceRegistryEntry(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("service-registry-id") String serviceRegistryId,
			@RequestBody ServiceRegistry serviceRegistry) throws ItorixException;

	//https://itorix.atlassian.net/browse/IE-162
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/service-registry/{service-registry-id}/rows/{row-id}")
	public ResponseEntity<?> updateServiceRegistryEntry(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody ServiceRegistry serviceRegistry, 
			@PathVariable("service-registry-id") String serviceRegistryId,
			@PathVariable("row-id") String rowId) throws ItorixException;

	//https://itorix.atlassian.net/browse/IE-163
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/service-registry/{service-registry-id}/rows/{row-id}")
	public ResponseEntity<?> deleteServiceRegistryEntry(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("service-registry-id") String serviceRegistryId,
			@PathVariable("row-id") String rowId) throws ItorixException;

	//https://itorix.atlassian.net/browse/IE-165
	@RequestMapping(method = RequestMethod.GET, value = "/v1/service-registry/{service-registry-id}")
	public ResponseEntity<ServiceRegistryEntriesResponse> getServiceRegistryEntries(
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("service-registry-id") String serviceRegistryId) throws ItorixException;
	
	@RequestMapping(method = RequestMethod.GET, value = "v1/service-registry/search", produces = { "application/json" })
	public ResponseEntity<Object> swaggerSearch(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@RequestParam("name") String name, @RequestParam("limit") int limit)
			throws ItorixException;
}