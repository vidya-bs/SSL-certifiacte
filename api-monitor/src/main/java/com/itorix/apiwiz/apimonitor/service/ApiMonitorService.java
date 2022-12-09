package com.itorix.apiwiz.apimonitor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itorix.apiwiz.apimonitor.model.Variables;
import com.itorix.apiwiz.apimonitor.model.collection.MonitorCollections;
import com.itorix.apiwiz.apimonitor.model.request.MonitorRequest;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@CrossOrigin
@RestController
public interface ApiMonitorService {

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION' ) and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = {RequestMethod.POST}, value = "/v1/monitor/collections", consumes = {"application/json"})
	public ResponseEntity<Object> createCollection(@RequestBody MonitorCollections monitorCollections,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION' ) and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = {RequestMethod.PUT}, value = "/v1/monitor/collections/{id}", consumes = {
			"application/json"})
	public ResponseEntity<Object> updateCollection(@RequestBody MonitorCollections monitorCollections,
			@PathVariable(value = "id") String id, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = {RequestMethod.GET}, value = "/v1/monitor/collections", produces = {"application/json"})
	public ResponseEntity<Object> getCollections(@RequestParam(value = "offset", defaultValue = "1") int offset,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "pagesize", defaultValue = "10") int pageSize) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = {RequestMethod.GET}, value = "/v1/monitor/collections/{id}", produces = {
			"application/json"})
	public ResponseEntity<Object> getCollection(@PathVariable(value = "id") String id,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION' ) and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = {RequestMethod.DELETE}, value = "/v1/monitor/collections/{id}", produces = {
			"application/json"})
	public ResponseEntity<Object> deleteCollection(@PathVariable(value = "id") String id,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION' ) and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = {RequestMethod.POST}, value = "/v1/monitor/collections/{id}/requests", consumes = {
			"application/json"}, produces = {"application/json"})
	public ResponseEntity<Object> createRequest(@RequestBody MonitorRequest monitorRequest,
			@PathVariable(value = "id") String id, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION' ) and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = {
			RequestMethod.PUT}, value = "/v1/monitor/collections/{id}/requests/{requestId}", consumes = {
					"application/json"})
	public ResponseEntity<Object> updateRequest(@RequestBody MonitorRequest monitorRequest,
			@PathVariable(value = "id") String id, @PathVariable(value = "requestId") String requestId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = {RequestMethod.GET}, value = "/v1/monitor/collections/{id}/requests", produces = {
			"application/json"})
	public ResponseEntity<Object> getRequests(@PathVariable(value = "id") String id,
			@RequestParam(value = "offset", defaultValue = "1") int offset,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "pagesize", defaultValue = "10") int pageSize) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = {
			RequestMethod.GET}, value = "/v1/monitor/collections/{id}/requests/{requestId}", produces = {
					"application/json"})
	public ResponseEntity<Object> getRequest(@PathVariable(value = "id") String id,
			@PathVariable(value = "requestId") String requestId, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION' ) and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = {
			RequestMethod.DELETE}, value = "/v1/monitor/collections/{id}/requests/{requestId}", produces = {
					"application/json"})
	public ResponseEntity<Object> deleteRequest(@PathVariable(value = "id") String id,
			@PathVariable(value = "requestId") String requestId, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = {
			RequestMethod.GET}, value = "/v1/monitor/collections/{collectionId}/requests/{requestId}/schedulers/{schedulerId}", produces = {
					"application/json"})
	public ResponseEntity<Object> getRequestStats(@PathVariable(value = "collectionId") String collectionId,
			@PathVariable(value = "requestId") String requestId,
			@PathVariable(value = "schedulerId") String schedulerId,
			@RequestParam("date") @DateTimeFormat(pattern = "MM-dd-yyyy") Date date,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = {
			RequestMethod.GET}, value = "/v1/monitor/collections/{collectionId}/requests/{requestId}/events/{eventId}", produces = {
					"application/json"})
	public ResponseEntity<Object> getRequestStatLogs(@PathVariable(value = "collectionId") String collectionId,
			@PathVariable(value = "requestId") String requestId, @PathVariable(value = "eventId") String eventId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION' ) and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/monitor/variables", consumes = {
			"application/json"}, produces = {"application/json"})
	public ResponseEntity<?> createVariables(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestBody Variables variables,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			HttpServletResponse response) throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/monitor/variables/{id}", produces = {"application/json"})
	public ResponseEntity<?> getVariables(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response)
			throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION' ) and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/monitor/variables/{id}", consumes = {
			"application/json"}, produces = {"application/json"})
	public ResponseEntity<?> updateVariables(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestBody Variables variables,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION' ) and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/monitor/variables/{id}", produces = {
			"application/json"})
	public ResponseEntity<?> deleteVariables(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id)
			throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/monitor/variables", produces = {"application/json"})
	public ResponseEntity<?> getAllVariables(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/monitor/variables/overview", produces = {
			"application/json"})
	public ResponseEntity<?> getVariablesOverview(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "expand", required = false) String expand,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize);

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/monitor/certificates/{name}")
	public ResponseEntity<?> getCertificate(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable(name = "name") String name)
			throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/monitor/certificates", produces = {"application/json"})
	public ResponseEntity<?> getCertificates(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestParam(value = "names", required = false) String expand,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			HttpServletResponse response) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/monitor/certificates/overview", produces = {
			"application/json"})
	public ResponseEntity<?> getCertificatesOverView(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "expand", required = false) String expand,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize);

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION' ) and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/monitor/certificates/{name}")
	public ResponseEntity<?> deleteCertificate(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable(value = "name") String name)
			throws ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION' ) and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = {RequestMethod.POST}, value = "/v1/monitor/certificates", consumes = {
			"multipart/form-data"})
	public ResponseEntity<Object> createOrUpdateCertificate(@RequestPart(value = "name", required = true) String name,
			@RequestPart(value = "jksFile", required = false) MultipartFile jksFile,
			@RequestPart(value = "description", required = false) String description,
			@RequestPart(value = "password", required = false) String password,
			@RequestPart(value = "alias", required = false) String alias,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/monitor/certificates/{name}/download", produces = {
			"application/json"})
	public ResponseEntity<Resource> downloadCertificate(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable(name = "name") String name)
			throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/monitor/collections/{collectionId}/sequence", produces = {
			"application/json"})
	public ResponseEntity<Object> getRequestSequence(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(name = "collectionId") String collectionId) throws ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION' ) and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/monitor/collections/{collectionId}/sequence")
	public ResponseEntity<Object> updateRequestSequence(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(name = "collectionId") String collectionId,
			@RequestBody MonitorCollections monitorCollections) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/monitor/collections/{collectionId}/schedulers", produces = {
			"application/json"})
	public ResponseEntity<Object> getSchedulers(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(name = "collectionId") String collectionId) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/monitor/search")
	public ResponseEntity<Object> search(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "name") String name, @RequestParam(value = "limit") int limit) throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION' ) and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/monitor/metadata", consumes = {"application/json"})
	@UnSecure(ignoreValidation = true)
	public ResponseEntity<?> createMetaData(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid, @RequestBody String metadata)
			throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/monitor/metadata", produces = {"application/json"})
	public ResponseEntity<?> getMetaData(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/monitor/collections/{collectionId}/variables", produces = {
			"application/json"})
	public ResponseEntity<?> getCollectionsVariable(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(name = "collectionId") String collectionId) throws JsonProcessingException, ItorixException;
}
