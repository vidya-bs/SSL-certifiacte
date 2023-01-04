package com.itorix.apiwiz.servicerequest.serviceImpl;

import java.time.Instant;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContextHolder;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.itorix.apiwiz.servicerequest.dao.ServiceRequestDao;
import com.itorix.apiwiz.servicerequest.model.ServiceRequest;
import com.itorix.apiwiz.servicerequest.model.ServiceRequestComments;
import com.itorix.apiwiz.servicerequest.model.ServiceRequestTypes;
import com.itorix.apiwiz.servicerequest.service.ServiceRequestService;
@Slf4j
@CrossOrigin
@RestController
public class ServiceRequestServiceImpl implements ServiceRequestService {

	@Autowired
	ServiceRequestDao serviceRequestDao;

	@Autowired
	private IdentityManagementDao identityManagementDao;

	@Override
	public ResponseEntity<?> createServiceRequest(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype, @RequestBody ServiceRequest config,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		config.setCreatedUser(user.getFirstName() + " " + user.getLastName());
		config.setCreatedUserEmailId(user.getEmail());
		config.setCreatedDate(new Date(System.currentTimeMillis()));
		config.setModifiedUser(user.getFirstName() + " " + user.getLastName());
		config.setModifiedDate(new Date(System.currentTimeMillis()));
		config.setStatus("Review");
		config.setCreated(false);
		// config.setGwType(gwtype);
		config.setActiveFlag(Boolean.TRUE);
		config = serviceRequestDao.createServiceRequest(config);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Access-Control-Expose-Headers", "X-request-id");
		headers.add("X-request-id", config.get_id());
		return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> updateServiceRequest(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody ServiceRequest config, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception {
		if (!config.getType().equals("") && !config.getOrg().equals("")
				&& (config.getEnv() != null && !config.getEnv().equals("")
						|| "Product".equalsIgnoreCase(config.getType()))) {
			User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
			config.setModifiedUser(user.getFirstName() + " " + user.getLastName());
			config.setModifiedDate(new Date(System.currentTimeMillis()));
			config.setModifiedUserEmailId(user.getEmail());
			config.setStatus("Review");
			config.setActiveFlag(Boolean.TRUE);
			serviceRequestDao.updateServiceRequest(config);
			return new ResponseEntity<Void>(HttpStatus.CREATED);
		} else {
			throw new ItorixException("Insufficient Data in the Request", "Configuration-1005");
		}
	}

	@Override
	public ResponseEntity<?> changeServiceRequestStatus(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "x-gwtype", required = false) String gwtype,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "isSaaS", required = false) boolean isSaaS,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		if (!type.equals("") && !org.equals("")
				&& ((env != null && !env.equals("")) || "Product".equalsIgnoreCase(type))) {
			ServiceRequest config = new ServiceRequest();
			config.setType(type);
			config.setOrg(org);
			config.setEnv(env);
			config.setName(name);
			config.setStatus(status);
			config.setIsSaaS(isSaaS);
			config.setGwType(gwtype);
			User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
			UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
			String workspaceId = userSessionToken.getWorkspaceId();
			config.setModifiedUser(user.getFirstName() + " " + user.getLastName());
			config.setModifiedDate(new Date(System.currentTimeMillis()));
			config.setUserRole(user.getUserWorkspace(workspaceId).getRoles());
			serviceRequestDao.changeServiceRequestStatus(config, user);
			return new ResponseEntity<Void>(HttpStatus.CREATED);
		} else {
			throw new ItorixException("Insufficient Data in the Request", "Configuration-1005");
		}
	}

	@Override
	public ResponseEntity<?> logHistory(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "isSaaS", required = false) boolean isSaaS,
			@RequestBody ServiceRequestComments serviceRequestComments,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		if (!type.equals("") && !org.equals("")
				&& ((env != null && !env.equals("")) || "Product".equalsIgnoreCase(type))) {
			serviceRequestComments.setType(type);
			serviceRequestComments.setOrg(org);
			serviceRequestComments.setEnv(env);
			serviceRequestComments.setName(name);
			serviceRequestComments.setIsSaaS(isSaaS);
			User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
			serviceRequestComments.setCreatedUser(user.getFirstName() + " " + user.getLastName());
			serviceRequestComments.setCreatedDate(Instant.now().toString());
			serviceRequestDao.createLogHistory(serviceRequestComments);
			return new ResponseEntity<Void>(HttpStatus.CREATED);
		} else {
			throw new ItorixException("Insufficient Data in the Request", "Configuration-1005");
		}
	}

	@Override
	public ResponseEntity<Object> getLogHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "isSaaS", required = false) boolean isSaaS,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		if (!type.equals("") && !org.equals("")
				&& ((env != null && !env.equals("")) || "Product".equalsIgnoreCase(type))) {
			ServiceRequestComments serviceRequestComments = new ServiceRequestComments();
			serviceRequestComments.setType(type);
			serviceRequestComments.setOrg(org);
			serviceRequestComments.setEnv(env);
			serviceRequestComments.setName(name);
			serviceRequestComments.setIsSaaS(isSaaS);
			return new ResponseEntity<Object>(serviceRequestDao.getLogHistory(serviceRequestComments), HttpStatus.OK);
		} else {
			throw new ItorixException("Insufficient Data in the Request", "Configuration-1005");
		}
	}

	@Override
	public ResponseEntity<Object> getservicerequests(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value="timerange",required =false)String timerange,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize) throws Exception {
		ServiceRequest serviceRequest = new ServiceRequest();
		serviceRequest.setType(type);
		serviceRequest.setStatus(status);
		serviceRequest.setName(name);
		return new ResponseEntity<Object>(serviceRequestDao.getServiceRequests(serviceRequest, offset, pageSize,timerange),
				HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getservicerequestsByParameters(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "isSaaS", required = false) boolean isSaaS) throws Exception {
		ServiceRequest serviceRequest = new ServiceRequest();
		serviceRequest.setStatus(status);
		serviceRequest.setType(type);
		serviceRequest.setIsSaaS(isSaaS);
		return new ResponseEntity<Object>(serviceRequestDao.getservicerequests(serviceRequest), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getservicerequestsByParameters(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "isSaaS", required = false) boolean isSaaS) throws Exception {
		ServiceRequest serviceRequest = new ServiceRequest();
		serviceRequest.setName(name);
		serviceRequest.setOrg(org);
		serviceRequest.setEnv(env);
		serviceRequest.setType(type);
		serviceRequest.setIsSaaS(isSaaS);
		return new ResponseEntity<Object>(serviceRequestDao.getservicerequestsFullDetails(serviceRequest),
				HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getServicerequestStats(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "timeunit", required = false) String timeunit,
			@RequestParam(value = "timerange", required = false) String timerange) throws Exception {
		return new ResponseEntity<Object>(serviceRequestDao.getServiceRequestStats(timeunit, timerange), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> revertConfig(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable(value = "requestId") String requestId, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception {
		String req = null;
		log.info("no");
		serviceRequestDao.revertServiceRequest(requestId);
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Object> getHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable(value = "serviceRequestType") String serviceRequestType,
			@RequestParam(value = "org") String org, @RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "name") String name, @RequestParam(value = "isSaaS", required = false) boolean isSaaS,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset) throws Exception {
		ServiceRequest serviceRequest = new ServiceRequest();
		serviceRequest.setName(name);
		serviceRequest.setOrg(org);
		serviceRequest.setEnv(env);
		serviceRequest.setType(ServiceRequestTypes.valueOf(serviceRequestType.toUpperCase()).getResponse());
		serviceRequest.setIsSaaS(isSaaS);
		return new ResponseEntity<Object>(serviceRequestDao.getservicerequest(serviceRequest), HttpStatus.OK);
	}

	public ResponseEntity<Object> configSearch(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "type") String type, @RequestParam(value = "name") String name,
			@RequestParam(value = "limit") int limit) throws Exception {
		return new ResponseEntity<Object>(serviceRequestDao.configSearchOnServiceRequest(type, name, limit),
				HttpStatus.OK);
	}
}
