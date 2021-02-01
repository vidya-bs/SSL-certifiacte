package com.itorix.apiwiz.datamanagement.service;

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

import com.itorix.apiwiz.common.model.apigee.CommonConfiguration;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.data.management.model.BackupInfo;
import com.itorix.apiwiz.data.management.model.EnvironmentBackUpInfo;
import com.itorix.apiwiz.data.management.model.ProxyBackUpInfo;
import com.itorix.apiwiz.data.management.model.ResourceBackUpInfo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
@Api(value = "DMB", tags = "DMB")
public interface EnvironmentService {

	/**
	 * 
	 * @param interactionid
	 * @param jsessionid
	 * @param organization
	 * @param environment
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Get Environment Depolyed Proxies", notes = "", code=200)
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = String.class,responseContainer = "List"),
        @ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
        @ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
        @ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/organizations/{organization}/environments/{environment}/deployments")
	public ResponseEntity<List<String>> getEnvironmentDepolyedProxies(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@PathVariable("environment") String environment,
			@RequestParam(value = "type", required = false) String type)throws Exception;

	/**
	 * <h1>http://hostname:port//v1/api/apigee/organizations/{organization}/backupenvironment</h1>
	 * <p>
	 * This service Will took the back up of Environment's. It won't delete any
	 * information.
	 * </p>
	 * 
	 * @param interactionid
	 * @param cfg
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception 
	 */
	@ApiOperation(value = "Back Up Environment", notes = "", code=200)
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
        @ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
        @ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
        @ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/organizations/{organization}/backupenvironment")
	public ResponseEntity<BackupInfo> backUpEnvironment(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody CommonConfiguration cfg, @RequestHeader(value = "jsessionid") String jsessionid,
			@PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/cleanupenvironment</h1>
	 * <p>
	 * This service Will took the back up of Environment's. It will delete same
	 * information.
	 * </p>
	 * 
	 * @param interactionid
	 * @param cfg
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Clean Up Environment", notes = "", code=200)
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
        @ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
        @ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
        @ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/organizations/{organization}/cleanupenvironment")
	public ResponseEntity<BackupInfo> cleanUpEnvironment(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody CommonConfiguration cfg, @RequestHeader(value = "jsessionid") String jsessionid,
			@PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type)throws Exception;

	/**
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/restoreenvironment</h1>
	 * <p>
	 * This service Will took the restore of Environment's.
	 * </p>
	 * 
	 * @param interactionid
	 * @param cfg
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Restore Environment", notes = "", code=204)
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "No Content", response = Void.class),
        @ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
        @ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
        @ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/organizations/{organization}/restoreenvironment")
	public ResponseEntity<Void> restoreEnvironment(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody CommonConfiguration cfg, @RequestHeader(value = "jsessionid") String jsessionid,
			@PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type)throws Exception;

	/**
	 * <h1>http://hostname:port/v1/api/apigee/getenvironmentbackuphistory</h1>
	 * <p>
	 * This service Will get the backed up history..
	 * </p>
	 * 
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Get Environment Backup History", notes = "", code=200)
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = EnvironmentBackUpInfo.class,responseContainer="List"),
        @ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
        @ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
        @ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/getenvironmentbackuphistory")
	public ResponseEntity<List<EnvironmentBackUpInfo>> getEnvironmentBackupHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid)throws Exception;

	/**
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/environment/backupapiproxy</h1>
	 * <p>
	 * This service Will took the back up of Proxy's. It won't delete any
	 * information.
	 * </p>
	 * 
	 * @param interactionid
	 * @param cfg
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Back Up Environment Api Proxy", notes = "", code=200)
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
        @ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
        @ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
        @ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/organizations/{organization}/environment/backupapiproxy")
	public ResponseEntity<BackupInfo> backUpEnvironmentApiProxy(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody CommonConfiguration cfg, @RequestHeader(value = "jsessionid") String jsessionid,
			@PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type)throws Exception;

	/**
	 * <h1>http://hostname:port//v1/api/apigee/organizations/{organization}/environment/cleanupapiproxy</h1>
	 * <p>
	 * This service Will took the back up of Proxy's. It will delete same
	 * information.
	 * </p>
	 * 
	 * @param interactionid
	 * @param cfg
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Clean Up Environment Api Proxy", notes = "", code=200)
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
        @ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
        @ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
        @ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = { RequestMethod.DELETE, RequestMethod.PATCH}, value = "/v1/api/apigee/organizations/{organization}/environment/cleanupapiproxy")
	public ResponseEntity<BackupInfo> cleanUpEnvironmentApiProxy(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody CommonConfiguration cfg, @RequestHeader(value = "jsessionid") String jsessionid,
			@PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 * <h1>http://hostname:port//v1/api/apigee/organizations/{organization}/environment/restoreapiproxies</h1>
	 * <p>
	 * This service Will took the restore of Proxy's.
	 * </p>
	 * 
	 * @param interactionid
	 * @param cfg
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Restore Environment Api Proxy", notes = "", code=204)
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "No Content", response = Void.class),
        @ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
        @ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
        @ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/organizations/{organization}/environment/restoreapiproxies")
	public ResponseEntity<Void> restoreEnvironmentApiProxy(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody CommonConfiguration cfg, @RequestHeader(value = "jsessionid") String jsessionid,
			@PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 * <h1>http://hostname:port/v1/api/apigee/environment/getapiproxiesbackuphistory</h1>
	 * <p>
	 * This service Will took the history of backup, cleanup & restored Proxy's.
	 * </p>
	 * 
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Get Environment Api Proxies Backup History", notes = "", code=200)
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = ProxyBackUpInfo.class,responseContainer="List"),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/environment/getapiproxiesbackuphistory")
	public ResponseEntity<List<ProxyBackUpInfo> > getEnvironmentApiProxiesBackupHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid) throws Exception;

	/**
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/backupenvironmentcaches</h1>
	 * <p>
	 * This service Will took the back up of environment specific Cache's. It
	 * won't delete any information.
	 * </p>
	 * 
	 * @param cfg
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "back Up Caches", notes = "", code=200)
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
        @ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
        @ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
        @ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/organizations/{organization}/backupenvironmentcaches")
	public ResponseEntity<BackupInfo> backUpCaches(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody CommonConfiguration cfg, @RequestHeader(value = "jsessionid") String jsessionid,
			@PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/cleanupenvironmentcaches</h1>
	 * <p>
	 * This service Will took the back up of Environment specific cache's. It
	 * will delete same information.
	 * </p>
	 * 
	 * @param cfg
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Clean Up Caches", notes = "", code=200)
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
        @ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
        @ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
        @ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = { RequestMethod.DELETE, RequestMethod.PATCH}, value = "/v1/api/apigee/organizations/{organization}/cleanupenvironmentcaches")
	public ResponseEntity<BackupInfo> cleanUpCaches(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody CommonConfiguration cfg, @RequestHeader(value = "jsessionid") String jsessionid,
			@PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/restoreenvironmentcaches</h1>
	 * <p>
	 * This service Will took the restore of Environment specific cache's.
	 * </p>
	 * 
	 * @param jsessionid
	 * @param organization
	 * @param cfg
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Restore Caches", notes = "", code=204)
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "No Content", response = Void.class),
        @ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
        @ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
        @ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/organizations/{organization}/restoreenvironmentcaches")
	public ResponseEntity<Void> restoreCaches(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestBody CommonConfiguration cfg, @RequestParam(value = "type", required = false) String type)
			throws Exception;

	/**
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/getenvironmentcachesbackuphistory</h1>
	 * <p>
	 * This service Will took the backup , cleanup & restore of Environment
	 * specific cache's history.
	 * </p>
	 * 
	 * @param jsessionid
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Get Caches Backup History", notes = "", code=200)
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = ResourceBackUpInfo.class,responseContainer="List"),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/getenvironmentcachesbackuphistory")
	public ResponseEntity<List<ResourceBackUpInfo>> getCachesBackupHistory(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid) throws Exception;

	/**
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/backupenvironmenttaretservers</h1>
	 * <p>
	 * This service Will took the back up of Environment specific Target
	 * Servers's. It won't delete any information.
	 * </p>
	 * 
	 * @param cfg
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Back Up Target Servers", notes = "", code=200)
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
        @ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
        @ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
        @ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/organizations/{organization}/backupenvironmenttaretservers")
	public ResponseEntity<Object> backUpTargetServers(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody CommonConfiguration cfg, @RequestHeader(value = "jsessionid") String jsessionid,
			@PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/cleanupenvironmenttargetservers</h1>
	 * <p>
	 * This service Will took the back up of Environment specific Target
	 * Server's. It will delete same information.
	 * </p>
	 * 
	 * @param cfg
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Clean Up Target Servers", notes = "", code=200)
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
        @ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
        @ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
        @ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = { RequestMethod.DELETE, RequestMethod.PATCH}, value = "/v1/api/apigee/organizations/{organization}/cleanupenvironmenttargetservers")
	public ResponseEntity<BackupInfo> cleanUpTargetServers(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody CommonConfiguration cfg, @RequestHeader(value = "jsessionid") String jsessionid,
			@PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/restoreenvironmenttargetservers</h1>
	 * <p>
	 * This service Will took the restore of Environment specific Target
	 * Server's
	 * </p>
	 * 
	 * @param jsessionid
	 * @param organization
	 * @param cfg
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Restore Target Servers", notes = "", code=204)
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "No Content", response = Void.class),
        @ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
        @ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
        @ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/organizations/{organization}/restoreenvironmenttargetservers")
	public ResponseEntity<Void> restoreTargetServers(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestBody CommonConfiguration cfg, @RequestParam(value = "type", required = false) String type)
			throws Exception;
	/**
	 * <h1>http://hostname:port/v1/api/apigee/getenvironmenttargetserversbackuphistory</h1>
	 * <p>
	 * This service fetches all the list of backup's that were taken for
	 * environment specific targetServer's.
	 * </p>
	 * 
	 * @param cfg
	 * @param sys
	 * @param backuplevel
	 * @return CommonConfiguration
	 * @throws Exception
	 */
	@ApiOperation(value = "Get Target Servers Backup History", notes = "", code=200)
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = ResourceBackUpInfo.class,responseContainer="List"),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/getenvironmenttargetserversbackuphistory")
	public ResponseEntity<List<ResourceBackUpInfo> > getTargetServersBackupHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid)throws Exception;

	/**
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/backupenvironmentkvm</h1>
	 * <p>
	 * This service Will took the back up of Environment specific KVM's. It
	 * Won't delete any information.
	 * </p>
	 * 
	 * @param cfg
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Back Up KVM", notes = "", code=200)
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
        @ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
        @ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
        @ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/organizations/{organization}/backupenvironmentkvm")
	public ResponseEntity<BackupInfo> backUpKVM(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody CommonConfiguration cfg, @RequestHeader(value = "jsessionid") String jsessionid,
			@PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/cleanupenvironmentkvm</h1>
	 * <p>
	 * This service Will took the back up of Environment specific KVM's. It will
	 * delete same information.
	 * </p>
	 * 
	 * @param cfg
	 * @param jsessionid
	 * @param organization
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Clean Up KVM", notes = "", code=200)
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = BackupInfo.class),
        @ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
        @ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
        @ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = { RequestMethod.DELETE, RequestMethod.PATCH}, value = "/v1/api/apigee/organizations/{organization}/cleanupenvironmentkvm")
	public ResponseEntity<BackupInfo> cleanUpKVM(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody CommonConfiguration cfg, @RequestHeader(value = "jsessionid") String jsessionid,
			@PathVariable("organization") String organization,
			@RequestParam(value = "type", required = false) String type) throws Exception;

	/**
	 * <h1>http://hostname:port/v1/api/apigee/organizations/{organization}/restoreenvironmentkvm</h1>
	 * <p>
	 * This service Will took the restore of Environment specific KVM's.
	 * </p>
	 * 
	 * @param jsessionid
	 * @param organization
	 * @param cfg
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Restore KVM", notes = "", code=204)
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "No Content", response = Void.class),
        @ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
        @ApiResponse(code = 401, message = "Sorry! Apigee unauthorized user.", response = ErrorObj.class),
        @ApiResponse(code = 403, message = "Sorry! Insufficeint permissions to carry out this task", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Apigee connection timeout error.", response = ErrorObj.class),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/apigee/organizations/{organization}/restoreenvironmentkvm")
	public ResponseEntity<Void> restoreKVM(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable("organization") String organization,
			@RequestBody CommonConfiguration cfg, @RequestParam(value = "type", required = false) String type)
			throws Exception;

	/**
	 * <h1>http://hostname:port/v1/api/apigee/getenvironmentkvmbackuphistory</h1>
	 * <p>
	 * This service fetches all the list of backup's that were taken for
	 * Environment KVM's.
	 * </p>
	 * 
	 * @param cfg
	 * @param sys
	 * @param backuplevel
	 * @return CommonConfiguration
	 * @throws Exception
	 */
	@ApiOperation(value = "Get KVM Backup History", notes = "", code=200)
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = ResourceBackUpInfo.class,responseContainer="List"),
        @ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/apigee/getenvironmentkvmbackuphistory")
	public ResponseEntity<List<ResourceBackUpInfo> > getKVMBackupHistory(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid) throws Exception;
	
}
