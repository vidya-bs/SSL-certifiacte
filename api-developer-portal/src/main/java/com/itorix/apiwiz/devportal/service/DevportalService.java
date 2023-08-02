package com.itorix.apiwiz.devportal.service;

import com.itorix.apiwiz.devportal.model.DeveloperApp;
import com.itorix.apiwiz.devportal.model.monetization.PurchaseRecord;
import java.util.List;
import java.util.Map;
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

@CrossOrigin
@RestController
public interface DevportalService {

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/organizations/{org}/developers")
	public ResponseEntity<String> createDeveloper(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type") String type, @PathVariable("org") String org, @RequestBody String body)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/organizations/{org}/developers/{email:.+}/apps")
	public org.springframework.http.ResponseEntity<String> registerApp(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("email") String email, @RequestBody Map<String,Object> body) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/app-mappings")
	public List<DeveloperApp> getRegisteredApps(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "org",required = false)String org,
			@RequestParam(value = "email",required = false)String email,
			@RequestParam(value = "appId",required = false)String appId,
			@RequestParam(value = "appName",required = false)String appName) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/organizations/{org}/developers/{email:.+}/apps/{appName}")
	public org.springframework.http.ResponseEntity<String> updateApp(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("email") String email, @PathVariable("appName") String appName,
			@RequestParam(value = "status", required = false) String status,
			@RequestBody String body)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/organizations/{org}/developers/{email:.+}/apps/{appName}/keys/{appKey}")
	public org.springframework.http.ResponseEntity<String> updateAppProduct(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("email") String email, @PathVariable("appName") String appName,
			@PathVariable("appKey") String appKey, @RequestBody String body) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/organizations/{org}/developers/{email:.+}/apps/{appName}/keys/{appKey}/apiproducts/{product}")
	public org.springframework.http.ResponseEntity<String> deleteAppProduct(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("email") String email, @PathVariable("appName") String appName,
			@PathVariable("appKey") String appKey, @PathVariable("product") String product) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/organizations/{org}/developers/{email:.+}/apps/{appName}")
	public org.springframework.http.ResponseEntity<String> deleteApp(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("email") String email, @PathVariable("appName") String appName) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/organizations/{org}/apiproducts",produces = {"application/json"})
	public org.springframework.http.ResponseEntity<String> getProducts(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type") String type, @PathVariable("org") String org,
			@RequestParam(value = "expand", required = false) String expand) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/organizations/{org}/developers/{email:.+}/apps")
	public org.springframework.http.ResponseEntity<String> getApps(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("email") String email, @RequestParam(value = "expand", required = false) String expand)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/organizations/{org}/developers/apps")
	public org.springframework.http.ResponseEntity<Object> getAppsByOrganisation(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type,
			@PathVariable("org") String org)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/organizations/{org}/developers/{email:.+}/apps/{appName}")
	public org.springframework.http.ResponseEntity<String> getApp(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type, @PathVariable("org") String org,
			@PathVariable("email") String email, @PathVariable("appName") String appName) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
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

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/organizations/{org}/apiproducts/filter")
	public ResponseEntity<String> getProductsForPartner(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "partners", required = false) String partner,
			@RequestHeader(value = "type") String type, @PathVariable("org") String org) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/product-bundle")
	public ResponseEntity<?> getProductBundleCards(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "parterType",required = false) String partnerType,
			@RequestParam(value = "org" , required = false)String org,
			@RequestParam(value = "offset" ,defaultValue = "1")int offset,
			@RequestParam(value = "pagesize", defaultValue = "10")int pagesize,
			@RequestParam(value = "paginated" ,required = false , defaultValue = "true") boolean paginated) throws Exception;


	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/apps/purchase")
	public ResponseEntity<?> purchaseRatePlan(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PurchaseRecord purchaseRecord) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apps/{app-id}/purchase")
	public ResponseEntity<?> getPurchaseHistoryByAppId(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("app-id") String appId) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apps/purchase")
	public ResponseEntity<?> getPurchaseHistory(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "app-id", required = false) String appId,
			@RequestParam("email") String developerEmailId,
			@RequestParam(value = "org", required = false) String organization) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/apps/{app-id}/purchase/{purchase-id}")
	public ResponseEntity<?> deletePurchaseById(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("app-id") String appId,
			@PathVariable("purchase-id") String purchaseId) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/wallet/apps")
	public ResponseEntity<?> getWalletBalanceByFilter(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "app-id", required = false) String appId,
			@RequestParam(value = "email", required = false) String email) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/wallet/apps/{app-id}")
	public ResponseEntity<?> getWalletBalanceByAppId(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("app-id") String appId) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/wallet/apps/{app-id}")
	public ResponseEntity<?> addWalletBalanceForAppId(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "topup") double topUp,
			@PathVariable("app-id") String appId) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apps/{app-id}/billing")
	public ResponseEntity<?> computeBillForAppId(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "transactions") double transactions,
			@RequestHeader(value = "startDate") String startDate,
			@RequestHeader(value = "endDate") String endDate,
			@PathVariable("app-id") String appId) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/organizations/{org}/developers/{developerEmailId}/apps/{appName}/keys/{consumerKey}/apiproducts/{productName}")
	public ResponseEntity<?> updateProductStatus(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestHeader(value = "type", required = false) String type,
			@PathVariable("org") String org,
			@PathVariable("developerEmailId") String developerEmailId,
			@PathVariable("appName") String appName,
			@PathVariable("consumerKey") String consumerKey,
			@PathVariable("productName") String productName,
			@RequestParam(value = "action") String action)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/gateways")
	public ResponseEntity<?> getAllGateways(@RequestHeader(value = "JSESSIONID") String jsessionId,
											@RequestHeader(value = "interactionid", required = false) String interactionid)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/gateways/{gateway}/environments")
	public ResponseEntity<?> getGatewayEnvs(@RequestHeader(value = "JSESSIONID") String jsessionId,
											@RequestHeader(value = "interactionid", required = false) String interactionid,
											@RequestHeader(value = "x-gwtype", required = false) String gwtype,
											@RequestHeader(value = "type", required = false) String type,
											@PathVariable("gateway") String gateway,
											@RequestParam(value = "resourceGroup",required = false) String resourceGroup) throws Exception;


	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/gateways/{gateway}/environments/{env}/apps")
	public ResponseEntity<?> getGatewayApps(@RequestHeader(value = "JSESSIONID") String jsessionId,
											@RequestHeader(value = "interactionid", required = false) String interactionid,
											@RequestHeader(value = "type", required = false) String type,
											@PathVariable("gateway") String gateway,
											@PathVariable("env") String env,
											@RequestParam(value = "workspace",required = false) String workspace,
											@RequestParam(value = "resourceGroup",required = false) String resourceGroup) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/gateways/kong/{runTime}/workspaces")
	public ResponseEntity<?> getKongWorkspaces(@RequestHeader(value = "JSESSIONID") String jsessionId,
											   @RequestHeader(value = "interactionid", required = false) String interactionid,
											   @PathVariable("runTime") String runTime) throws Exception;
	@PreAuthorize("hasAnyRole('ADMIN','PORTAL') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/gateways/azure/resource-groups")
	public ResponseEntity<?> getAzureResourceGroups(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid
	) throws Exception;
}
