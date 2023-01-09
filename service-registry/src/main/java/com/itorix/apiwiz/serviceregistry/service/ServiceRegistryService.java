package com.itorix.apiwiz.serviceregistry.service;

import java.util.List;

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

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.serviceregistry.model.ServiceRegistryEntriesResponse;
import com.itorix.apiwiz.serviceregistry.model.documents.ServiceRegistry;
import com.itorix.apiwiz.serviceregistry.model.documents.ServiceRegistryColumns;
import com.itorix.apiwiz.serviceregistry.model.documents.ServiceRegistryList;
import com.itorix.apiwiz.serviceregistry.model.documents.ServiceRegistryResponse;

@CrossOrigin
@RestController
public interface ServiceRegistryService {

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/service-registry/columns")
	public ResponseEntity<ServiceRegistryColumns> getServiceRegistryColumns(
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION' ) and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/service-registry/columns")
	public ResponseEntity<?> createOrUpdateServRegColumns(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody ServiceRegistryColumns registryColumns) throws ItorixException;

	//// service registry ///////
	// https://itorix.atlassian.net/browse/IE-166
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION' ) and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/service-registry")
	public ResponseEntity<?> createServiceRegistry(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody ServiceRegistryList serviceRegistry) throws ItorixException;

	// https://itorix.atlassian.net/browse/IE-167
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION' ) and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/service-registry/{service-registry-id}")
	public ResponseEntity<?> updateServiceRegistry(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("service-registry-id") String serviceRegistryId,
			@RequestBody ServiceRegistryList serviceRegistry) throws ItorixException;

	// https://itorix.atlassian.net/browse/IE-171
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION' ) and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/service-registry/{service-registry-id}")
	public ResponseEntity<?> deleteServiceRegistry(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("service-registry-id") String serviceRegistryId) throws ItorixException;

	// https://itorix.atlassian.net/browse/IE-164
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/service-registry")
	public ResponseEntity<ServiceRegistryResponse> getServiceRegistry(
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize)
			throws ItorixException;

	// https://itorix.atlassian.net/browse/IE-161
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION' ) and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/service-registry/{service-registry-id}/rows")
	public ResponseEntity<?> createServiceRegistryEntry(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("service-registry-id") String serviceRegistryId, @RequestBody ServiceRegistry serviceRegistry)
			throws ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION' ) and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/service-registry/{service-registry-id}/addrows")
	public ResponseEntity<?> createServiceRegistryEntries(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("service-registry-id") String serviceRegistryId,
			@RequestBody List<ServiceRegistry> serviceRegistryList) throws ItorixException;

	// https://itorix.atlassian.net/browse/IE-162
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION' ) and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/service-registry/{service-registry-id}/rows/{row-id}")
	public ResponseEntity<?> updateServiceRegistryEntry(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody ServiceRegistry serviceRegistry, @PathVariable("service-registry-id") String serviceRegistryId,
			@PathVariable("row-id") String rowId) throws ItorixException;

	// https://itorix.atlassian.net/browse/IE-163
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION' ) and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/service-registry/{service-registry-id}/rows/{row-id}")
	public ResponseEntity<?> deleteServiceRegistryEntry(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("service-registry-id") String serviceRegistryId, @PathVariable("row-id") String rowId)
			throws ItorixException;

	// https://itorix.atlassian.net/browse/IE-165
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/service-registry/{service-registry-id}")
	public ResponseEntity<ServiceRegistryEntriesResponse> getServiceRegistryEntries(
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("service-registry-id") String serviceRegistryId) throws ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION' ) and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/service-registry/{service-registry-id}/publish")
	public ResponseEntity<?> publishServiceRegistry(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("service-registry-id") String serviceRegistryId) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "v1/service-registry/search", produces = {"application/json"})
	public ResponseEntity<Object> swaggerSearch(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam("name") String name,
			@RequestParam("limit") int limit) throws ItorixException;

}
