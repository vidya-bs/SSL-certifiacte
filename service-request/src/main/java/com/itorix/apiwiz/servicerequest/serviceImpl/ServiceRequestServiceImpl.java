package com.itorix.apiwiz.servicerequest.serviceImpl;

import com.itorix.apiwiz.common.model.apigee.StaticFields;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.monetization.Webhook;
import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.servicerequest.model.MonetizationConfigComments;
import java.time.Instant;
import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itorix.apiwiz.common.model.monetization.Company;
import com.itorix.apiwiz.common.model.monetization.DeveloperCategory;
import com.itorix.apiwiz.common.model.monetization.ProductBundle;
import com.itorix.apiwiz.common.model.monetization.RatePlan;
import javax.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
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
			@RequestParam(value = "productId", required = false) String productId,
			@RequestBody ServiceRequest config, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception {
//		ResponseEntity responseEntity;
		if (productId != null) {
			User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
			serviceRequestDao.updateServiceRequestInApigee(productId,config,user);
			return new ResponseEntity<Void>(HttpStatus.ACCEPTED);
		}else {
			if (!config.getType().equals("") && !config.getOrg().equals("")
					&& (config.getEnv() != null && !config.getEnv().equals("")
					|| "Product".equalsIgnoreCase(config.getType()))) {
				User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
				config.setModifiedUser(user.getFirstName() + " " + user.getLastName());
				config.setModifiedDate(new Date(System.currentTimeMillis()));
				config.setModifiedUserEmailId(user.getEmail());
				config.setStatus(StaticFields.STATUS_REVIEW);
				config.setActiveFlag(Boolean.TRUE);
				serviceRequestDao.updateServiceRequest(config);
				return new ResponseEntity<Void>(HttpStatus.CREATED);
			} else {
				throw new ItorixException("Insufficient Data in the Request", "Configuration-1005");
			}
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
	public ResponseEntity<Object> getAllServiceRequests(String interactionid, String jsessionid, String type, String status, String orgName, String name) throws Exception {
		return new ResponseEntity<Object>(serviceRequestDao.getAllServiceRequests(type,status,orgName,name), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getAllMonetizationConfigs(String interactionid, String jsessionid, String type, String status, String orgName) throws Exception {
		return new ResponseEntity<Object>(serviceRequestDao.getAllMonetizationConfigs(type,status,orgName), HttpStatus.OK);
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
		if (!serviceRequestDao.revertServiceRequest(requestId)) {
			return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
		}
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

	public ResponseEntity<?> createProductBundle(String interactionid, String jsessionid,
			ProductBundle productBundle) throws ItorixException {

		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		productBundle.setCreatedUser(user.getFirstName() + " " + user.getLastName());
		productBundle.setCreatedUserEmailId(user.getEmail());
		productBundle.setCreatedDate(new Date(System.currentTimeMillis()));
		productBundle.setModifiedUser(user.getFirstName() + " " + user.getLastName());
		productBundle.setModifiedDate(new Date(System.currentTimeMillis()));
		productBundle.setStatus("Review");
		productBundle.setActiveFlag(Boolean.TRUE);
		return new ResponseEntity<>(serviceRequestDao.createProductBundle(productBundle).getId(),HttpStatus.CREATED);
	}

	public ResponseEntity<?> updateProductBundle(String interactionid, String jsessionid,
			ProductBundle productBundle)throws ItorixException {

		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		productBundle.setModifiedUser(user.getFirstName() + " " + user.getLastName());
		productBundle.setModifiedDate(new Date(System.currentTimeMillis()));
		productBundle.setModifiedUserEmailId(user.getEmail());
		productBundle.setStatus("Review");
		productBundle.setActiveFlag(Boolean.TRUE);
		return new ResponseEntity<>(serviceRequestDao.updateProductBundle(productBundle),HttpStatus.ACCEPTED);
	}

	public ResponseEntity<?> deleteProductBundle(String interactionid, String jsessionid, String bundleId)throws ItorixException {
		serviceRequestDao.deleteProductBundle(bundleId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<?> getProductBundles(String interactionid, String jsessionid,String orgName, String timerange,String status,int offset,int pagesize,String id, String name) throws ItorixException {
		return new ResponseEntity<>(serviceRequestDao.getProductBundles(timerange,status,offset,pagesize,orgName,id,name),
				HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> changeProductBundleStatus(String interactionid, String org, String bundleName, String status,
			String jsessionid)
			throws ItorixException, ParseException, MessagingException, JsonProcessingException {
		if (org != null && bundleName != null) {
			User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
			ProductBundle productBundle = new ProductBundle();
			productBundle.setOrganization(org);
			productBundle.setName(bundleName);
			productBundle.setStatus(status);
			serviceRequestDao.changeProductBundleStatus(productBundle,user);
			return new ResponseEntity<Void>(HttpStatus.ACCEPTED);
		} else {
			throw new ItorixException("Insufficient Data in the Request", "Configuration-1005");
		}
	}


	@Override
	public ResponseEntity<?> createDeveloperCategory(String interactionid, String jsessionid,
			DeveloperCategory developerCategory)
			throws ItorixException, ParseException {
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		developerCategory.setCreatedUser(user.getFirstName() + " " + user.getLastName());
		developerCategory.setCreatedUserEmailId(user.getEmail());
		developerCategory.setCreatedDate(new Date(System.currentTimeMillis()));
		developerCategory.setModifiedUser(user.getFirstName() + " " + user.getLastName());
		developerCategory.setModifiedDate(new Date(System.currentTimeMillis()));
		developerCategory.setStatus("Review");
		developerCategory.setActiveFlag(Boolean.TRUE);
		return new ResponseEntity<>(serviceRequestDao.createDeveloperCategory(developerCategory).getId(),HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> updateDeveloperCategory(String interactionid, String jsessionid,
			DeveloperCategory developerCategory)
			throws ItorixException {
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		developerCategory.setModifiedUser(user.getFirstName() + " " + user.getLastName());
		developerCategory.setModifiedDate(new Date(System.currentTimeMillis()));
		developerCategory.setModifiedUserEmailId(user.getEmail());
		developerCategory.setStatus("Review");
		developerCategory.setActiveFlag(Boolean.TRUE);
		return new ResponseEntity<>(serviceRequestDao.updateDeveloperCategory(developerCategory),HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<?> deleteDeveloperCategory(String interactionid, String jsessionid,
			String categoryId) throws ItorixException {
		serviceRequestDao.deleteDeveloperCategory(categoryId);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getDeveloperCategories(String interactionid, String jsessionid,
			String orgName, String timerange,String status ,int offset, int pagesize,String name) throws ItorixException {
		return new ResponseEntity<>(serviceRequestDao.getDeveloperCategories(orgName,timerange,status,offset,pagesize,name),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> changeDeveloperCategoryStatus(String interactionid, String org, String categoryName, String status,
			String jsessionid) throws ItorixException, ParseException {
		if (org != null && categoryName != null) {
			DeveloperCategory developerCategory = new DeveloperCategory();
			developerCategory.setOrganization(org);
			developerCategory.setName(categoryName);
			developerCategory.setStatus(status);
			serviceRequestDao.changeDeveloperCategoryStatus(developerCategory);
			return new ResponseEntity<Void>(HttpStatus.ACCEPTED);
		} else {
			throw new ItorixException("Insufficient Data in the Request", "Configuration-1005");
		}
	}

	@Override
	public ResponseEntity<?> getCompanies(String interactionid, String jsessionid, String orgName,
			String timerange,String status, int offset, int pagesize,String name) throws ItorixException {
		return new ResponseEntity<>(serviceRequestDao.getCompanies(orgName,timerange,status,offset,pagesize,name),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createCompany(String interactionid, String jsessionid, Company company)
			throws ItorixException, JsonProcessingException, ParseException {
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		company.setCreatedUser(user.getFirstName() + " " + user.getLastName());
		company.setCreatedUserEmailId(user.getEmail());
		company.setCreatedDate(new Date(System.currentTimeMillis()));
		company.setModifiedUser(user.getFirstName() + " " + user.getLastName());
		company.setModifiedDate(new Date(System.currentTimeMillis()));
		company.setStatus("Review");
		company.setActiveFlag(Boolean.TRUE);
		return new ResponseEntity<>(serviceRequestDao.createCompany(company).getId(),HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> updateCompany(String interactionid, String jsessionid, Company company)
			throws ItorixException {
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		company.setModifiedUser(user.getFirstName() + " " + user.getLastName());
		company.setModifiedDate(new Date(System.currentTimeMillis()));
		company.setModifiedUserEmailId(user.getEmail());
		company.setStatus("Review");
		company.setActiveFlag(Boolean.TRUE);
		return new ResponseEntity<>(serviceRequestDao.updateCompany(company),HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<?> deleteCompany(String interactionid, String jsessionid, String companyId)
			throws ItorixException {
		serviceRequestDao.deleteCompany(companyId);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> changeCompanyStatus(String interactionid, String org,
			String companyName, String status, String jsessionid)
			throws ItorixException, ParseException, JsonProcessingException {
		if (org != null && companyName != null) {
			Company company = new Company();
			company.setOrganization(org);
			company.setName(companyName);
			company.setStatus(status);
			serviceRequestDao.changeCompanyStatus(company);
			return new ResponseEntity<Void>(HttpStatus.ACCEPTED);
		} else {
			throw new ItorixException("Insufficient Data in the Request", "Configuration-1005");
		}
	}

	@Override
	public ResponseEntity<?> getDevelopersInApigee(String interactionid, String jsessionid,
			String org)
			throws ItorixException, ParseException {
		if (org != null) {
			return new ResponseEntity<>(serviceRequestDao.getApigeeDevelopers(org),HttpStatus.OK);
		} else {
			throw new ItorixException("Invalid request data  ", "Configuration-1005");
		}
	}

	@UnSecure(useUpdateKey = true)
	@Override
	public ResponseEntity<Object> createCountryMetadata(
			String interactionid, String jsessionid, String apikey,
			String countryMetadata) throws ItorixException {
		serviceRequestDao.saveCountryMetadata(countryMetadata);
		return new ResponseEntity<Object>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getCountryMetaData(
			String interactionid, String jsessionid) throws ItorixException, JsonProcessingException {
		return new ResponseEntity<Object>(serviceRequestDao.getCountryMetaData(),HttpStatus.OK);
	}

	public ResponseEntity<Object> monetizationConfigSearch(String interactionid, String type, String name,
			int limit) throws Exception {
		return new ResponseEntity<Object>(serviceRequestDao.monetizationConfigSearch(type, name, limit),
				HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getMonetizationConfigHistory(String interactionid, String monetizationConfigtype, String org,
			String name, String jsessionid) throws Exception {
		return new ResponseEntity<Object>(serviceRequestDao.getMonetizationConfigHistory(monetizationConfigtype,org,name), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> monetizationLogHistory(String interactionid, String type, String org,
			String name, MonetizationConfigComments monetizationConfigComments, String jsessionid)
			throws Exception {
		if (!type.equals("") && !org.equals("")) {
			monetizationConfigComments.setType(type);
			monetizationConfigComments.setOrg(org);
			monetizationConfigComments.setName(name);
			User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
			monetizationConfigComments.setCreatedUser(user.getFirstName() + " " + user.getLastName());
			monetizationConfigComments.setCreatedDate(Instant.now().toString());
			serviceRequestDao.createMonetizationLogHistory(monetizationConfigComments);
			return new ResponseEntity<Void>(HttpStatus.CREATED);
		} else {
			throw new ItorixException("Insufficient Data in the Request", "Configuration-1005");
		}
	}

	@Override
	public ResponseEntity<Object> getMonetizationLogHistory(String interactionid, String type,
			String org, String name, String jsessionid) throws Exception {
		if (!type.equals("") && !org.equals("")){
			MonetizationConfigComments monetizationConfigComments = new MonetizationConfigComments();
			monetizationConfigComments.setType(type);
			monetizationConfigComments.setOrg(org);
			monetizationConfigComments.setName(name);
			return new ResponseEntity<Object>(serviceRequestDao.getMonetizationLogHistory(monetizationConfigComments), HttpStatus.OK);
		} else {
			throw new ItorixException("Insufficient Data in the Request", "Configuration-1005");
		}
	}

	@Override
	public ResponseEntity<?> createWebhook(String interactionid, String jsessionid, Webhook webhook)
			throws ItorixException, JsonProcessingException, ParseException {
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		webhook.setCreatedUser(user.getFirstName() + " " + user.getLastName());
		webhook.setCreatedUserEmailId(user.getEmail());
		webhook.setCreatedDate(new Date(System.currentTimeMillis()));
		webhook.setModifiedUser(user.getFirstName() + " " + user.getLastName());
		webhook.setModifiedDate(new Date(System.currentTimeMillis()));
		webhook.setStatus("Review");
		webhook.setActiveFlag(Boolean.TRUE);
		return new ResponseEntity<>(serviceRequestDao.createWebhook(webhook).getId(),HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> updateWebhook(String interactionid, String jsessionid, Webhook webhook)
			throws ItorixException {
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		webhook.setModifiedUser(user.getFirstName() + " " + user.getLastName());
		webhook.setModifiedDate(new Date(System.currentTimeMillis()));
		webhook.setModifiedUserEmailId(user.getEmail());
		webhook.setStatus("Review");
		webhook.setActiveFlag(Boolean.TRUE);
		return new ResponseEntity<>(serviceRequestDao.updateWebhook(webhook),HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<?> deleteWebhook(String interactionid, String jsessionid, String webhookId)
			throws ItorixException {
		serviceRequestDao.deleteWebhook(webhookId);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getWebhooks(String interactionid, String jsessionid, String orgName,
			String timerange, String status, int offset, int pagesize,String name) throws ItorixException {
		return new ResponseEntity<>(serviceRequestDao.getWebhooks(orgName,timerange,status,offset,pagesize,name),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> changeWebhookStatus(String interactionid, String org,
			String webhookName, String status, String jsessionid)
			throws ItorixException, ParseException {
		if (org != null && webhookName != null) {
			Webhook webhook = new Webhook();
			webhook.setOrganization(org);
			webhook.setName(webhookName);
			webhook.setStatus(status);
			serviceRequestDao.changeWebhookStatus(webhook);
			return new ResponseEntity<Void>(HttpStatus.ACCEPTED);
		} else {
			throw new ItorixException("Insufficient Data in the Request", "Configuration-1005");
		}
	}

	@Override
	public ResponseEntity<?> getRatePlans(String interactionid, String jsessionid, String orgName,
			String name,String status, String timerange, int offset, int pagesize) throws ItorixException {
		return new ResponseEntity<>(serviceRequestDao.getRatePlans(orgName, name,status, timerange, offset, pagesize),
				HttpStatus.OK);
	}
	@Override
	public ResponseEntity<?> createRatePlan(String interactionid, String jsessionid, RatePlan ratePlan)
			throws ItorixException {

		ratePlan = serviceRequestDao.createRatePlan(jsessionid,ratePlan);
		if(ratePlan != null){
			log.info("Successfully Created RatePlan: " + ratePlan);
			return new ResponseEntity<>(ratePlan,HttpStatus.CREATED);
		}
		throw new ItorixException(ErrorCodes.errorMessage.get("Monetization-1030"),"Monetization-1030");
	}
	@Override
	public ResponseEntity<?> updateRatePlan(String interactionid, String jsessionid, RatePlan ratePlan)
			throws ItorixException {


		ratePlan = serviceRequestDao.updateRatePlan(jsessionid,ratePlan);
		if(ratePlan != null){
			log.info("Successfully Updated RatePlan: " + ratePlan);
			return new ResponseEntity<>(ratePlan,HttpStatus.OK);
		}
		throw new ItorixException(ErrorCodes.errorMessage.get("Monetization-1040"),"Monetization-1040");

	}
	@Override
	public ResponseEntity<?> deleteRatePlanById(String interactionid, String jsessionid, String ratePlanId)
			throws ItorixException {
		serviceRequestDao.deleteRatePlan(ratePlanId);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}
	@Override
	public ResponseEntity<?> changeRatePlanStatus(String interactionid, String jsessionid, String orgName,
			String name, String status) throws ItorixException, ParseException {
		if (orgName != null && name != null && status != null) {
			RatePlan ratePlan = new RatePlan();
			ratePlan.setName(name);
			ratePlan.setOrganization(orgName);
			ratePlan.setConfigStatus(status);
			serviceRequestDao.changeRatePlanStatus(jsessionid,ratePlan);
			return new ResponseEntity<Void>(HttpStatus.ACCEPTED);
		} else {
			throw new ItorixException("Insufficient Data in the Request", "Configuration-1005");
		}
	}
	@Override
	public ResponseEntity<?> getRatePlanHistory(String interactionid, String jsessionid, String orgName,
			String name) throws ItorixException {
		if (orgName != null && name != null) {
			return new ResponseEntity<>(serviceRequestDao.getRatePlanHistory(orgName,name),HttpStatus.OK);
		} else {
			throw new ItorixException("Insufficient Data in the Request", "Configuration-1005");
		}
	}

	@Override
	public ResponseEntity<?> getSupportedCurrencies(String interactionid, String jsessionid, String orgName)throws ItorixException, ParseException {
		if (orgName != null) {
			return new ResponseEntity<>(serviceRequestDao.getSupportedCurrencies(orgName),HttpStatus.OK);
		} else {
			throw new ItorixException("Insufficient Data in the Request", "Configuration-1005");
		}
	}
}
