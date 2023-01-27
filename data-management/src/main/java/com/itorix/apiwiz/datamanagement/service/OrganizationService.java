package com.itorix.apiwiz.datamanagement.service;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.itorix.apiwiz.common.model.apigee.CommonConfiguration;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.data.management.model.ApigeeOrganizationalVO;
import com.itorix.apiwiz.data.management.model.AppBackUpInfo;
import com.itorix.apiwiz.data.management.model.BackupInfo;
import com.itorix.apiwiz.data.management.model.DeveloperBackUpInfo;
import com.itorix.apiwiz.data.management.model.OrgBackUpInfo;
import com.itorix.apiwiz.data.management.model.ProductsBackUpInfo;
import com.itorix.apiwiz.data.management.model.ProxyBackUpInfo;
import com.itorix.apiwiz.data.management.model.ResourceBackUpInfo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
@Api(value = "DMB", tags = "DMB")
public interface OrganizationService {

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/environments</h1>
	 *
	 * <p>
	 * This service is used to obtain all the environments available for that
	 * organizations passed in the input
	 *
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get Env List For Organizations", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/organizations/{organization}/environments", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<List<String>> getEnvListForOrganizations(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/apis</h1>
	 *
	 * <p>
	 * This service is used to obtain all the api proxys available for the
	 * organizations passed in the input
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "List API Proxies", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/organizations/{organization}/apis")
	public ResponseEntity<List<String>> listAPIProxies(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/proxies</h1>
	 *
	 * <p>
	 * This service is used to obtain all the deployed api proxys available for
	 * the organizations passed in the input
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param json
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws JSONException
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "List Of Deployed API Proxies", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/apigee/organizations/{organization}/proxies")
	public ResponseEntity<Map<String, List<String>>> listOfDeployedAPIProxies(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestBody String json, @RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/backupapiproxy</h1>
	 *
	 * <p>
	 * This service takes back up API proxy's for that organization.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param isDepoyedOnly
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Back Up Proxies", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/organizations/{organization}/backupapiproxy")
	public ResponseEntity<BackupInfo> backUpApiProxies(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestParam(value = "isDepoyedOnly") boolean isDepoyedOnly,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/cleanupapiproxy</h1>
	 *
	 * <p>
	 * This service deletes complete takes backup of organization data. Backup
	 * can also be taken individually for
	 * apiproxies,resources,caches,kvm's,virtualhost,targetservers,apiproducts,
	 * apps,developers
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param isDepoyedOnly
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Clean Up Proxies", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = {RequestMethod.DELETE,
			RequestMethod.PATCH}, value = "/v1/api/apigee/organizations/{organization}/cleanupapiproxy")
	public ResponseEntity<BackupInfo> cleanUpApiProxies(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestParam(value = "isDepoyedOnly") boolean isDepoyedOnly,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/restoreapiproxies</h1>
	 *
	 * <p>
	 * This service restores organization data, resources, apiproducts,
	 * appdevelopers, apps
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param cfg
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Restore Proxies", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/organizations/{organization}/restoreapiproxies")
	public ResponseEntity<Void> restoreApiProxies(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestBody CommonConfiguration cfg, @RequestParam(value = "type", required = false) String type)
			throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/backupSharedflows</h1>
	 *
	 * <p>
	 * This service takes back up API proxy's for that organization.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param isDepoyedOnly
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Back Up Shareflow", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/organizations/{organization}/backupsharedflow")
	public ResponseEntity<BackupInfo> backupSharedflows(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestParam(value = "isDepoyedOnly") boolean isDepoyedOnly,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/cleanupsharedflow</h1>
	 *
	 * <p>
	 * This service deletes complete takes backup of organization data. Backup
	 * can also be taken individually for
	 * apiproxies,resources,caches,kvm's,virtualhost,targetservers,apiproducts,
	 * apps,developers
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param isDepoyedOnly
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Clean Up Shareflow", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = {RequestMethod.DELETE,
			RequestMethod.PATCH}, value = "/v1/api/apigee/organizations/{organization}/cleanupsharedflow")
	public ResponseEntity<BackupInfo> cleanUpSharedflows(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestParam(value = "isDepoyedOnly") boolean isDepoyedOnly,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/restoresharedflows</h1>
	 *
	 * <p>
	 * This service restores organization data, resources, apiproducts,
	 * appdevelopers, apps, & sharedflows
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param cfg
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Restore Shareflow", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/organizations/{organization}/restoresharedflows")
	public ResponseEntity<Void> restoreShareflow(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestBody CommonConfiguration cfg, @RequestParam(value = "type", required = false) String type)
			throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/getapiproxiesbackuphistory</h1>
	 *
	 * <p>
	 * This service fetches all the list of backup's that were taken for
	 * organization,resources,appdevelopers,apiproducts,apps
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get Api Proxies Backup History", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = ProxyBackUpInfo.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/getapiproxiesbackuphistory")
	public ResponseEntity<List<ProxyBackUpInfo>> getApiProxiesBackupHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/backupapps</h1>
	 *
	 * <p>
	 * This service Will took the back up of app's and won't delete any
	 * information.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Back Up Apps", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/organizations/{organization}/backupapps")
	public ResponseEntity<BackupInfo> backUpApps(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/cleanupapps</h1>
	 *
	 * <p>
	 * This service will do the cleaning up of app's and delete as well.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Clean Up Apps", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = {RequestMethod.DELETE,
			RequestMethod.PATCH}, value = "/v1/api/apigee/organizations/{organization}/cleanupapps")
	public ResponseEntity<BackupInfo> cleanUpApps(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/restoreapps</h1>
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param cfg
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Restore Apps", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/organizations/{organization}/restoreapps")
	public ResponseEntity<Void> restoreApps(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestBody CommonConfiguration cfg, @RequestParam(value = "type", required = false) String type)
			throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/getappsbackuphistory</h1>
	 *
	 * <p>
	 * This service fetches all the list of backup's that were taken for
	 * organization,resources,appdevelopers,apiproducts,apps
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get Apps Backup History", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = AppBackUpInfo.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/getappsbackuphistory")
	public ResponseEntity<List<AppBackUpInfo>> getAppsBackupHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/backupproducts</h1>
	 *
	 * <p>
	 * This service Will took the back up of App's , Developer's and Prodect's.
	 * It won't delete any information.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Back Up Products", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/organizations/{organization}/backupproducts")
	public ResponseEntity<BackupInfo> backUpProducts(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/cleanupproducts</h1>
	 *
	 * <p>
	 * This service Will took the back up of App's , Developer's and Prodect's.
	 * It Will delete same information.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Clean Up Products", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = {RequestMethod.DELETE,
			RequestMethod.PATCH}, value = "/v1/api/apigee/organizations/{organization}/cleanupproducts")
	public ResponseEntity<BackupInfo> cleanUpProducts(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/restoreappproducts</h1>
	 *
	 * <p>
	 * This service Will took the restore of App's , Developer's and Prodect's.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param cfg
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Restore Products", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/organizations/{organization}/restoreappproducts")
	public ResponseEntity<Void> restoreAppProducts(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestBody CommonConfiguration cfg, @RequestParam(value = "type", required = false) String type)
			throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/getproductsbackuphistory</h1>
	 *
	 * <p>
	 * This service fetches all the list of backup's that were taken for
	 * organization apiproducts, apps, appdevelopers.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get Products Backup History", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = ProductsBackUpInfo.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/getproductsbackuphistory")
	public ResponseEntity<List<ProductsBackUpInfo>> getproductsBackupHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/backupdevelopers</h1>
	 *
	 * <p>
	 * This service Will took the back up of App's and Developer's. It won't
	 * delete any information.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Back Up Developers", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/organizations/{organization}/backupdevelopers")
	public ResponseEntity<BackupInfo> backUpDevelopers(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/cleanupdevelopers</h1>
	 *
	 * <p>
	 * This service Will took the back up of App's and Developer's. It will
	 * delete same information.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Clean Up Developers", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = {RequestMethod.DELETE,
			RequestMethod.PATCH}, value = "/v1/api/apigee/organizations/{organization}/cleanupdevelopers")
	public ResponseEntity<BackupInfo> cleanUpDevelopers(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/restoreappdevelopers</h1>
	 *
	 * <p>
	 * This service Will took the restore of App's and Developer's.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param cfg
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Restore App Developers", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/organizations/{organization}/restoreappdevelopers")
	public ResponseEntity<Void> restoreAppDevelopers(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestBody CommonConfiguration cfg, @RequestParam(value = "type", required = false) String type)
			throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port//v1/api/apigee/getdevelopersbackuphistory</h1>
	 *
	 * <p>
	 * This service fetches all the list of backup's that were taken for
	 * organization appdevelopers.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get Developers Backup History", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = DeveloperBackUpInfo.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/getdevelopersbackuphistory")
	public ResponseEntity<List<DeveloperBackUpInfo>> getDevelopersBackupHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/backupresoruces</h1>
	 *
	 * <p>
	 * This service Will took the back up of Cache's, KVM's and TargetServer's.
	 * It will delete same information.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Back Up Resources", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/organizations/{organization}/backupresoruces")
	public ResponseEntity<BackupInfo> backUpResources(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/cleanupresoruces</h1>
	 *
	 * <p>
	 * This service Will took the back up of Cache's, KVM's and TargetServer's.
	 * It will delete same information.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Clean Up Resources", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = {RequestMethod.DELETE,
			RequestMethod.PATCH}, value = "/v1/api/apigee/organizations/{organization}/cleanupresoruces")
	public ResponseEntity<BackupInfo> cleanUpResources(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/restoreresources</h1>
	 *
	 * <p>
	 * This service Will took the restore of Cache's, KVM's and TargetServer's.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param cfg
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Restore Resources", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/organizations/{organization}/restoreresources")
	public ResponseEntity<Void> restoreResources(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestBody CommonConfiguration cfg, @RequestParam(value = "type", required = false) String type)
			throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port//v1/api/apigee/getresourcesbackuphistory</h1>
	 *
	 * <p>
	 * This service fetches all the list of backup's that were taken for
	 * organization resources.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get Resources Backup History", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = ResourceBackUpInfo.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/getresourcesbackuphistory")
	public ResponseEntity<List<ResourceBackUpInfo>> getResourcesBackupHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/backuporganization</h1>
	 *
	 * <p>
	 * This service Will took the back up of Entire Organization. It won't
	 * delete any information.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param isDepoyedOnly
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Back Up Organization", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/organizations/{organization}/backuporganization")
	public ResponseEntity<BackupInfo> backUpOrganization(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestParam(value = "isDepoyedOnly") boolean isDepoyedOnly,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/cleanuporganization</h1>
	 *
	 * <p>
	 * This service Will took the back up of Entire Organization. It will delete
	 * same information.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Clean Up Organization", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = {RequestMethod.DELETE,
			RequestMethod.PATCH}, value = "/v1/api/apigee/organizations/{organization}/cleanuporganization")
	public ResponseEntity<BackupInfo> cleanUpOrganization(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/restoreorganization</h1>
	 *
	 * <p>
	 * This service restores organization data, resources, apiproducts,
	 * appdevelopers, apps
	 *
	 * @param jsessionid
	 * @param organization
	 * @param cfg
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Restore Organization", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/organizations/{organization}/restoreorganization")
	public ResponseEntity<Void> restoreOrganization(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestBody CommonConfiguration cfg, @RequestParam(value = "type", required = false) String type)
			throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port//v1/api/apigee/organizations/migrate</h1>
	 *
	 * <p>
	 * This service restores organization data, resources, apiproducts,
	 * appdevelopers, apps
	 *
	 * @param jsessionid
	 * @param cfg
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Restore Organization", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/organizations/migrate")
	public ResponseEntity<?> restoreOrganization(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @RequestBody CommonConfiguration cfg,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/getorganizationbackuphistory</h1>
	 *
	 * <p>
	 * This service fetches all the list of backup's that were taken for
	 * organization,resources,appdevelopers,apiproducts,apps
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get Organization Backup History", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = ResourceBackUpInfo.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/getorganizationbackuphistory")
	public ResponseEntity<List<OrgBackUpInfo>> getOrganizationBackupHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/backupcaches</h1>
	 *
	 * <p>
	 * This service Will took the back up of Cache's. It won't delete any
	 * information.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Back Up Caches", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/organizations/{organization}/backupcaches")
	public ResponseEntity<BackupInfo> backUpCaches(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/cleanupcaches</h1>
	 *
	 * <p>
	 * This service Will took the back up of Cache's. It will delete same
	 * information.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Clean Up Caches", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = {RequestMethod.DELETE,
			RequestMethod.PATCH}, value = "/v1/api/apigee/organizations/{organization}/cleanupcaches")
	public ResponseEntity<BackupInfo> cleanUpCaches(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/restorecaches</h1>
	 *
	 * <p>
	 * This service Will took the restore of Cache's.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param cfg
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Restore Caches", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/organizations/{organization}/restorecaches")
	public ResponseEntity<Void> restoreCaches(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestBody CommonConfiguration cfg, @RequestParam(value = "type", required = false) String type)
			throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/apigee/getcachesbackuphistory</h1>
	 *
	 * <p>
	 * This service fetches all the list of backup's that were taken for
	 * organization cache's.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get Caches Backup History", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = ResourceBackUpInfo.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/getcachesbackuphistory")
	public ResponseEntity<List<ResourceBackUpInfo>> getCachesBackupHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/backupkvm</h1>
	 *
	 * <p>
	 * This service Will took the back up of KVM's. It won't delete any
	 * information.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Back Up KVM", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/organizations/{organization}/backupkvm")
	public ResponseEntity<BackupInfo> backUpKVM(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/cleanupkvm</h1>
	 *
	 * <p>
	 * This service Will took the back up of KVM's. It will delete same
	 * information.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Clean Up KVM", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = {RequestMethod.DELETE,
			RequestMethod.PATCH}, value = "/v1/api/apigee/organizations/{organization}/cleanupkvm")
	public ResponseEntity<BackupInfo> cleanUpKVM(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/restorekvm</h1>
	 *
	 * <p>
	 * This service Will took the restore of KVM's. information.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param cfg
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Restore KVM", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/organizations/{organization}/restorekvm")
	public ResponseEntity<Void> restoreKVM(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestBody CommonConfiguration cfg, @RequestParam(value = "type", required = false) String type)
			throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/getkvmbackuphistory</h1>
	 *
	 * <p>
	 * This service fetches all the list of backup's that were taken for
	 * organization KVM's.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get KVM Backup History", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = ResourceBackUpInfo.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/getkvmbackuphistory")
	public ResponseEntity<List<ResourceBackUpInfo>> getKVMBackupHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/backuptaretservers</h1>
	 *
	 * <p>
	 * This service Will took the back up of TargetServer's. It won't delete any
	 * information.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Back Up Target Servers", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/organizations/{organization}/backuptaretservers")
	public ResponseEntity<BackupInfo> backUpTargetServers(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/cleanuptargetservers</h1>
	 *
	 * <p>
	 * This service Will took the back up of TargetServer's. It will delete same
	 * information. </p
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Clean Up Target Servers", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = {RequestMethod.DELETE,
			RequestMethod.PATCH}, value = "/v1/api/apigee/organizations/{organization}/cleanuptargetservers")
	public ResponseEntity<BackupInfo> cleanUpTargetServers(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/restoretargetservers</h1>
	 *
	 * <p>
	 * This service Will took the restore of TargetServer's.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param cfg
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Restore Target Servers", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
			@ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/organizations/{organization}/restoretargetservers")
	public ResponseEntity<Void> restoreTargetServers(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestBody CommonConfiguration cfg, @RequestParam(value = "type", required = false) String type)
			throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/gettargetserversbackuphistory</h1>
	 *
	 * <p>
	 * This service fetches all the list of backup's that were taken for
	 * organization Target Server's
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get Target Servers Backup History", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = ResourceBackUpInfo.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/gettargetserversbackuphistory")
	public ResponseEntity<List<ResourceBackUpInfo>> getTargetServersBackupHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/apigee/getorgbackuphistory1</h1>
	 *
	 * <p>
	 * This service fetches all the list of backup's that were taken for
	 * organization,resources,appdevelopers,apiproducts,apps
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param sys
	 * @param backuplevel
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get Swagger Revison's", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = Object.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/getorgbackuphistory")
	public ResponseEntity<List<Object>> getOrgBackUpHistory1(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @RequestParam(value = "sys") String sys,
			@RequestParam(value = "backuplevel") String backuplevel) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get apigee Organizational View", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = ApigeeOrganizationalVO.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apigee/organizations/{org_name}/summary")
	public ResponseEntity<JsonNode> apigeeOrganizationalView(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("org_name") String org_name,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get apigee Organizational View", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = ApigeeOrganizationalVO.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apigee/organizations/{org_name}/overview")
	public ResponseEntity<?> apigeeOrganizationalOverView(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("org_name") String org_name,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "refresh", required = false) boolean refresh) throws Exception;


	@ApiOperation(value = "Scehedule apigee Organizational View", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = ApigeeOrganizationalVO.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PostMapping(
	value = "/v1/apigee/organizations/{org_name}/overview-schedule")
	public ResponseEntity<?> scheduleOrganizationalOverView(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid,
			@PathVariable("org_name") String org_name,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	@ApiOperation(value = "Get apigee Organizational Schedule View", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = ApigeeOrganizationalVO.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@GetMapping( value = "/v1/apigee/organizations/{org_name}/overview-schedule")
	public ResponseEntity<?> retrieveScheduleOrganizationalOverView(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid,
			@PathVariable("org_name") String org_name,
			@RequestParam(value = "type", required = false) String type) throws Exception;
}
