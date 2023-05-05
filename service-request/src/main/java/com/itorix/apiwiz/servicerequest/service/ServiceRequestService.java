package com.itorix.apiwiz.servicerequest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.monetization.Company;
import com.itorix.apiwiz.common.model.monetization.DeveloperCategory;
import com.itorix.apiwiz.common.model.monetization.ProductBundle;
import com.itorix.apiwiz.common.model.monetization.Webhook;
import com.itorix.apiwiz.common.model.monetization.RatePlan;
import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.servicerequest.model.MonetizationConfigComments;
import javax.mail.MessagingException;
import org.json.simple.parser.ParseException;
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

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/config/servicerequests")
	public ResponseEntity<?> createServiceRequest(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype, @RequestBody ServiceRequest config,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/config/servicerequests")
	public ResponseEntity<?> updateServiceRequest(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "productId", required = false) String productId,
			@RequestBody ServiceRequest config, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'OPERATIONS') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/config/servicerequests/changestatus")
	public ResponseEntity<?> changeServiceRequestStatus(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "isSaaS", required = false) boolean isSaaS,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/config/servicerequests/loghistory")
	public ResponseEntity<?> logHistory(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "isSaaS", required = false) boolean isSaaS,
			@RequestBody ServiceRequestComments serviceRequestComments,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/servicerequests/loghistory")
	public ResponseEntity<Object> getLogHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "isSaaS", required = false) boolean isSaaS,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/servicerequests")
	public ResponseEntity<Object> getservicerequests(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value="timerange",required =false)String timerange,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize) throws Exception;


	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/servicerequests/{servicerequestType}")
	public ResponseEntity<Object> getAllServiceRequests(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(value = "servicerequestType", required = false) String type,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "org",required = false)String orgName,
			@RequestParam(value = "name",required = false) String name) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/monetization/{monetizationConfigType}")
	public ResponseEntity<Object> getAllMonetizationConfigs(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(value = "monetizationConfigType") String type,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "org",required = false)String orgName) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/servicerequest")
	public ResponseEntity<Object> getservicerequestsByParameters(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "isSaaS", required = false) boolean isSaaS) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/servicerequests/detail")
	public ResponseEntity<Object> getservicerequestsByParameters(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "isSaaS", required = false) boolean isSaaS) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/service-requests/stats")
	public ResponseEntity<?> getServicerequestStats(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "timeunit", required = false) String timeunit,
			@RequestParam(value = "timerange", required = false) String timerange) throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/config/servicerequests/{requestId}/revert")
	public ResponseEntity<Void> revertConfig(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable(value = "requestId") String requestId, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/servicerequests/{serviceRequestType}/history")
	public ResponseEntity<Object> getHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable(value = "serviceRequestType") String serviceRequestType,
			@RequestParam(value = "org") String org, @RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "name") String name, @RequestParam(value = "isSaaS", required = false) boolean isSaaS,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/servicerequest/search")
	public ResponseEntity<Object> configSearch(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "type") String type, @RequestParam(value = "name") String name,
			@RequestParam(value = "limit") int limit) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/apigee/product-bundle")
	public ResponseEntity<?> createProductBundle(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody ProductBundle productBundle) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/apigee/product-bundle")
	public ResponseEntity<?> updateProductBundle(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody ProductBundle productBundle)throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/apigee/product-bundle/{product-bundle-Id}")
	public ResponseEntity<?> deleteProductBundle(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(value = "product-bundle-Id")String bundleId)throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apigee/product-bundle")
	public ResponseEntity<?> getProductBundles(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "org" , required = false) String orgName,
			@RequestParam(value="timerange",required =false)String timerange,
			@RequestParam(value = "status",required =false)String status,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pagesize,
			@RequestParam(value = "id",required = false) String id,
			@RequestParam(value = "name",required =false)String name) throws ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/apigee/product-bundle/changestatus")
	public ResponseEntity<?> changeProductBundleStatus(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "bundle-name", required = false) String bundleName,
			@RequestParam(value = "status", required = false) String status,
			@RequestHeader(value = "JSESSIONID") String jsessionid)
			throws ItorixException, ParseException, MessagingException, JsonProcessingException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/apigee/developer-category")
	public ResponseEntity<?> createDeveloperCategory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody DeveloperCategory developerCategory)
			throws ItorixException, JsonProcessingException, ParseException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/apigee/developer-category")
	public ResponseEntity<?> updateDeveloperCategory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody DeveloperCategory developerCategory) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/apigee/developer-category/{category-id}")
	public ResponseEntity<?> deleteDeveloperCategory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(value = "category-id") String categoryId) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apigee/developer-category")
	public ResponseEntity<?> getDeveloperCategories(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "org" , required = false)String orgName,
			@RequestParam(value="timerange",required =false)String timerange,
			@RequestParam(value = "status" , required = false)String status,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pagesize,
			@RequestParam(value = "name" , required = false)String name) throws ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/apigee/developer-category/changestatus")
	public ResponseEntity<?> changeDeveloperCategoryStatus(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "developer-category", required = false) String categoryName,
			@RequestParam(value = "status", required = false) String status,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException, ParseException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apigee/company")
	public ResponseEntity<?> getCompanies(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "org" , required = false)String orgName,
			@RequestParam(value="timerange",required =false)String timerange,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pagesize,
			@RequestParam(value = "name", required = false) String name) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/apigee/company")
	public ResponseEntity<?> createCompany(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody Company company)
			throws ItorixException, JsonProcessingException, ParseException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/apigee/company")
	public ResponseEntity<?> updateCompany(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody Company company) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/apigee/company/{company-id}")
	public ResponseEntity<?> deleteCompany(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(value = "company-id") String companyId) throws ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/apigee/company/changestatus")
	public ResponseEntity<?> changeCompanyStatus(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "company", required = false) String companyName,
			@RequestParam(value = "status", required = false) String status,
			@RequestHeader(value = "JSESSIONID") String jsessionid)
			throws ItorixException, ParseException, JsonProcessingException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apigee/developers")
	public ResponseEntity<?> getDevelopersInApigee(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "org" , required = false)String orgName)
			throws ItorixException, ParseException;

	@UnSecure(useUpdateKey = true)
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/apigee/country-code", produces = {"application/json"})
	public ResponseEntity<Object> createCountryMetadata(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID" ,required = false) String jsessionid,
			@RequestHeader(value = "x-apikey") String apikey,
			@RequestBody String countryMetadata) throws ItorixException;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/apigee/country-code", produces = {"application/json"})
	public ResponseEntity<Object> getCountryMetaData(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid)
			throws ItorixException, JsonProcessingException;


	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/monetization/search")
	public ResponseEntity<Object> monetizationConfigSearch(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "type") String type, @RequestParam(value = "name") String name,
			@RequestParam(value = "limit") int limit) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/monetization/{monetizationConfigType}/history")
	public ResponseEntity<Object> getMonetizationConfigHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable(value = "monetizationConfigType") String monetizationConfigType,
			@RequestParam(value = "org") String org,
			@RequestParam(value = "name") String name,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/config/monetization/loghistory")
	public ResponseEntity<?> monetizationLogHistory(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "name", required = false) String name,
			@RequestBody MonetizationConfigComments monetizationConfigComments,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/monetization/loghistory")
	public ResponseEntity<Object> getMonetizationLogHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "name", required = false) String name,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/apigee/webhook")
	public ResponseEntity<?> createWebhook(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody Webhook webhook)
			throws ItorixException, JsonProcessingException, ParseException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/apigee/webhook")
	public ResponseEntity<?> updateWebhook(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody Webhook webhook) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/apigee/webhook/{webhook-id}")
	public ResponseEntity<?> deleteWebhook(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(value = "webhook-id") String webhookId) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apigee/webhook")
	public ResponseEntity<?> getWebhooks(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "org" , required = false)String orgName,
			@RequestParam(value="timerange",required =false)String timerange,
			@RequestParam(value = "status" , required = false)String status,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pagesize,
			@RequestParam(value = "name" , required = false)String name) throws ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/apigee/webhook/changestatus")
	public ResponseEntity<?> changeWebhookStatus(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "webhook-name", required = false) String webhookName,
			@RequestParam(value = "status", required = false) String status,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException, ParseException;


	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apigee/rate-plan")
	public ResponseEntity<?> getRatePlans(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "org" , required = false)String orgName,
			@RequestParam(value = "name" , required = false) String name,
			@RequestParam(value = "status" , required = false) String status,
			@RequestParam(value="timerange",required =false) String timerange,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize) throws ItorixException;


	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/apigee/rate-plan")
	public ResponseEntity<?> createRatePlan(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody RatePlan ratePlan) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/apigee/rate-plan")
	public ResponseEntity<?> updateRatePlan(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody RatePlan ratePlan) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/apigee/rate-plan/{rateplan-id}")
	public ResponseEntity<?> deleteRatePlanById(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(value = "rateplan-id") String ratePlanId) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/apigee/rate-plan/changestatus")
	public ResponseEntity<?> changeRatePlanStatus(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "org")String orgName,
			@RequestParam(value = "name") String name,
			@RequestParam(value = "status") String status) throws ItorixException, ParseException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apigee/rate-plan/history")
	public ResponseEntity<?> getRatePlanHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "org")String orgName,
			@RequestParam(value = "name") String name) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apigee/supported-currencies")
	public ResponseEntity<?> getSupportedCurrencies(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "org")String orgName)throws ItorixException, ParseException;
}
