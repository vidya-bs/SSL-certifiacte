package com.itorix.apiwiz.apimonitor.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itorix.apiwiz.apimonitor.dao.ApiMonitorDAO;
import com.itorix.apiwiz.apimonitor.model.*;
import com.itorix.apiwiz.apimonitor.model.collection.APIMonitorResponse;
import com.itorix.apiwiz.apimonitor.model.collection.MonitorCollections;
import com.itorix.apiwiz.apimonitor.model.collection.Schedulers;
import com.itorix.apiwiz.apimonitor.model.request.MonitorRequest;
import com.itorix.apiwiz.apimonitor.model.stats.RequestStats;
import com.itorix.apiwiz.apimonitor.model.stats.logs.MonitorRequestLog;
import com.itorix.apiwiz.apimonitor.service.ApiMonitorService;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@EnableScheduling
public class ApiMonitorServiceImpl implements ApiMonitorService {

	@Autowired
	ApiMonitorDAO apiMonitorDAO;

	@Autowired
	private IdentityManagementDao commonServices;

	@Autowired
	HttpServletRequest request;

	@Override
	public ResponseEntity<Object> createCollection(@RequestBody MonitorCollections monitorCollections,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		String id = apiMonitorDAO.createCollection(monitorCollections, jsessionid);
		return new ResponseEntity<>("{\"id\": \"" + id + "\"}", HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Object> updateCollection(@RequestBody MonitorCollections monitorCollections,
			@PathVariable(value = "id") String id, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception {
		apiMonitorDAO.updateCollection(monitorCollections, id, jsessionid);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> getCollections(@RequestParam(value = "offset", defaultValue = "1") int offset,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "pagesize", defaultValue = "10") int pageSize) throws Exception {
		APIMonitorResponse monitorCollections = apiMonitorDAO.getCollections(offset, pageSize);
		return new ResponseEntity<>(monitorCollections, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getCollection(@PathVariable(value = "id") String id,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		MonitorCollections monitorCollection = apiMonitorDAO.getCollection(id);
		return new ResponseEntity<>(monitorCollection, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> deleteCollection(@PathVariable(value = "id") String id,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		apiMonitorDAO.deleteCollection(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<Object> createRequest(@RequestBody MonitorRequest monitorRequest,
			@PathVariable(value = "id") String id, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception {
		String requestId = apiMonitorDAO.createRequest(id, monitorRequest, jsessionid);
		return new ResponseEntity<>("{\"id\": \"" + requestId + "\"}", HttpStatus.CREATED);
	}

	public ResponseEntity<Object> updateRequest(@RequestBody MonitorRequest monitorRequest,
			@PathVariable(value = "id") String id, @PathVariable(value = "requestId") String requestId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		apiMonitorDAO.updateRequest(id, requestId, monitorRequest, jsessionid);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<Object> getRequests(@PathVariable(value = "id") String id,
			@RequestParam(value = "offset", defaultValue = "1") int offset,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "pagesize", defaultValue = "10") int pageSize) throws Exception {
		MonitorCollections monitorRequests = apiMonitorDAO.getRequests(id, offset, pageSize);
		return new ResponseEntity<>(monitorRequests, HttpStatus.OK);
	}

	public ResponseEntity<Object> getRequest(@PathVariable(value = "id") String id,
			@PathVariable(value = "requestId") String requestId, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception {
		MonitorRequest monitorRequests = apiMonitorDAO.getRequest(id, requestId);
		return new ResponseEntity<>(monitorRequests, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> deleteRequest(@PathVariable(value = "id") String id,
			@PathVariable(value = "requestId") String requestId, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception {
		apiMonitorDAO.deleteRequest(id, requestId, jsessionid);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> getRequestStats(@PathVariable(value = "collectionId") String collectionId,
			@PathVariable(value = "requestId") String requestId,
			@PathVariable(value = "schedulerId") String schedulerId,
			@RequestParam("date") @DateTimeFormat(pattern = "MM-dd-yyyy") Date date,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {

		RequestStats requestStats = apiMonitorDAO.getRequestStats(collectionId, requestId, schedulerId, date);
		return new ResponseEntity<>(requestStats, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getRequestStatLogs(@PathVariable(value = "collectionId") String collectionId,
			@PathVariable(value = "requestId") String requestId, @PathVariable(value = "eventId") String eventId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {

		MonitorRequestLog monitorLogs = apiMonitorDAO.getRequestStatLogs(collectionId, requestId, eventId);
		return new ResponseEntity<>(monitorLogs, HttpStatus.OK);
	}

	public ResponseEntity<?> createVariables(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestBody Variables variables,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			HttpServletResponse response) throws JsonProcessingException, ItorixException {
		User user = commonServices.getUserDetailsFromSessionID(jsessionid);

		variables.setCreatedBy(user.getFirstName() + " " + user.getLastName());
		variables.setCts(System.currentTimeMillis());
		List<Header> headerVariables = variables.getVariables();

		for (Header header : headerVariables) {
			if (header.isEncryption()) {
				try {
					header.setValue(new RSAEncryption().encryptText(header.getValue()));
				} catch (Exception e) {
					throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-1001"), "Monitor-1001");
				}
			}
		}

		apiMonitorDAO.createVariables(variables);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	public ResponseEntity<?> getVariables(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response)
			throws JsonProcessingException, ItorixException {
		Variables variables = apiMonitorDAO.getVariablesById(id);
		if (variables == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(apiMonitorDAO.getVariablesById(id), HttpStatus.OK);
	}

	public ResponseEntity<?> updateVariables(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestBody Variables variables,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException, ItorixException {
		User user = commonServices.getUserDetailsFromSessionID(jsessionid);
		variables.setModifiedBy(user.getFirstName() + " " + user.getLastName());
		variables.setMts(System.currentTimeMillis());
		// variables.setCreatedBy(user.getFirstName()+" "+user.getLastName());
		return new ResponseEntity<>(apiMonitorDAO.updateVariables(variables, id, jsessionid), HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<?> deleteVariables(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id)
			throws JsonProcessingException, ItorixException {
		apiMonitorDAO.deleteVariable(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<?> getAllVariables(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException, ItorixException {
		List<Variables> variables = apiMonitorDAO.getVariables();
		if (variables == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(variables, HttpStatus.OK);
	}
	@Override
	public ResponseEntity<?> getVariablesOverview(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "expand", required = false) String expand,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize) {
		VariablesOverviewResponse response = apiMonitorDAO.getAllVariables(offset, pageSize);
		if (Boolean.parseBoolean(expand)) {
			return new ResponseEntity<>(
					response.getVariables().stream().map(v -> v.getName()).collect(Collectors.toList()), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<?> deleteCertificate(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable(value = "name") String name)
			throws ItorixException {

		apiMonitorDAO.deleteCertificate(name);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getCertificate(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable(value = "name") String name)
			throws ItorixException {

		Certificates certificate = apiMonitorDAO.getCertificate(name);
		if (certificate == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-1002"), "Monitor-1002");
		}

		String url = request.getRequestURL().toString();
		StringBuilder downloadLocation = new StringBuilder(
				url.substring(0, url.indexOf(request.getContextPath()) + request.getContextPath().length() + 1));
		downloadLocation.append("/v1/monitor/certificates/").append(name).append("/download");
		CertificatesResponse certificatesResponse = new CertificatesResponse();
		BeanUtils.copyProperties(certificate, certificatesResponse);
		certificatesResponse.setDownloadLocation(downloadLocation.toString());
		return new ResponseEntity<>(certificatesResponse, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createOrUpdateCertificate(@RequestPart(value = "name", required = true) String name,
			@RequestPart(value = "jksFile", required = false) MultipartFile jksFile,
			@RequestPart(value = "description", required = false) String description,
			@RequestPart(value = "password", required = false) String password,
			@RequestPart(value = "alias", required = false) String alias,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {

		if (jksFile == null) {
			throw new ItorixException((String.format(ErrorCodes.errorMessage.get("Monitor-1003"), "JKSFile")),
					"Monitor-1003");
		}
		byte[] bytes = jksFile.getBytes();
		if (bytes == null || bytes.length == 0) {
			throw new ItorixException((String.format(ErrorCodes.errorMessage.get("Monitor-1003"), "JKSFile")),
					"Monitor-1003");
		}

		apiMonitorDAO.createOrUpdateCertificate(name, bytes, description, password, alias, jsessionid);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getCertificates(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestParam(value = "names", required = false) String names,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			HttpServletResponse response) throws ItorixException {
		List<Certificates> certificates = apiMonitorDAO.getCertificates(Boolean.parseBoolean(names));
		if (Boolean.parseBoolean(names)) {
			return new ResponseEntity<>(certificates.stream().map(s -> s.getName()).collect(Collectors.toList()),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>(certificates, HttpStatus.OK);
		}
	}
	@Override
	public ResponseEntity<?> getCertificatesOverView(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "expand", required = false) String expand,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize) {
		CertificatesOverviewResponse response = apiMonitorDAO.getAllCertificates(offset, pageSize);
		if (Boolean.parseBoolean(expand)) {
			return new ResponseEntity<>(
					response.getCertificates().stream().map(c -> c.getName()).collect(Collectors.toList()),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
	}

	public ResponseEntity<Resource> downloadCertificate(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable(name = "name") String name)
			throws ItorixException {

		byte[] content = apiMonitorDAO.downloadCertificate(name);
		if (content == null || content.length == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-1002"), "Monitor-1002");
		}

		ByteArrayResource resource = new ByteArrayResource(content);

		return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
	}

	@Override
	public ResponseEntity<Object> getRequestSequence(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(name = "collectionId") String collectionId) throws ItorixException {
		MonitorCollections collections = apiMonitorDAO.getRequestSequence(collectionId);
		return new ResponseEntity<>(collections, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateRequestSequence(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(name = "collectionId") String collectionId,
			@RequestBody MonitorCollections monitorCollections) throws ItorixException {
		apiMonitorDAO.updateRequestSequence(collectionId, monitorCollections.getSequence());
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> getSchedulers(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(name = "collectionId") String collectionId) throws ItorixException {
		MonitorCollections monitors = apiMonitorDAO.getSchedulers(collectionId);
		List<Requests> requests = new ArrayList<>();
		for (Schedulers scheduler : monitors.getSchedulers()) {
			for (MonitorRequest monitorRequest : monitors.getMonitorRequest()) {
				Requests request = new Requests();
				request.setCollectionID(collectionId);
				request.setSchedulerId(scheduler.getId());
				request.setEnvironmentName(scheduler.getEnvironmentName());
				request.setId(monitorRequest.getId());
				request.setName(monitorRequest.getName());
				requests.add(request);
			}
		}
		SchedulerResponse response = new SchedulerResponse();
		response.setRequests(requests);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> search(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "name") String name, @RequestParam(value = "limit", defaultValue = "10") int limit)
			throws Exception {
		return new ResponseEntity<Object>(apiMonitorDAO.search(name, limit), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createMetaData(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody String metadata)
			throws JsonProcessingException, ItorixException {
		apiMonitorDAO.createMetaData(metadata);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getMetaData(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws JsonProcessingException, ItorixException {
		return new ResponseEntity<>(apiMonitorDAO.getMetaData(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getCollectionsVariable(String interactionid, String jsessionid, String collectionId)
			throws JsonProcessingException, ItorixException {
		return new ResponseEntity<>(apiMonitorDAO.getCollectionsVariable(collectionId), HttpStatus.OK);
	}
}
