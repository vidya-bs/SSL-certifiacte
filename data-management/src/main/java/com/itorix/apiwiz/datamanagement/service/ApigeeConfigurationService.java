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

import com.itorix.apiwiz.common.model.apigee.ApigeeConfigurationVO;
import com.itorix.apiwiz.common.model.apigee.ApigeeIntegrationVO;
import com.itorix.apiwiz.common.model.apigee.ApigeeServiceUser;
import com.itorix.apiwiz.common.model.exception.ErrorObj;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
@Api(value = "DMB", tags = "DMB")
public interface ApigeeConfigurationService {


	/**
	 * <h1>http://hostname:port/v1/apigee/organizations/config</h1>
	 * <p>
	 * getConfiguration
	 * </p>
	 * 
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Get Configuration", notes = "", code=200)
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = ApigeeConfigurationVO.class,responseContainer = "List"),
        @ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apigee/organizations/config")
	public ResponseEntity<List<ApigeeConfigurationVO>> getConfiguration(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid)throws Exception ;
	/**
	 * <h1>http://hostname:port/v1/apigee/organizations</h1>
	 * <p>
	 * getListOfOrgAndEnv
	 * </p>
	 * 
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "GetList Of Org And Env", notes = "", code=200)
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = ApigeeConfigurationVO.class,responseContainer = "List"),
        @ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apigee/organizations")
	public  ResponseEntity<List<ApigeeConfigurationVO>> getListOfOrgAndEnv(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid", required = false) String jsessionid)throws Exception;

	/**
	 * <h1>http://hostname:port/v1/apigee/host</h1>
	 * <p>
	 * getApigeeHost
	 * </p>
	 * 
	 * @param interactionid
	 * @param jsessionid
	 * @param type
	 * @param org
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "Get Apigee Host", notes = "", code=200)
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = String.class),
        @ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apigee/host")
	public  ResponseEntity<Object> getApigeeHost(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@RequestParam("type") String type,
			@RequestParam("org") String org) throws Exception;
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apigee/authorization")
	public  ResponseEntity<Object> getApigeeAuthorization(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@RequestParam("type") String type,
			@RequestParam("org") String org) throws Exception;
	/**
	 * <h1>http://hostname:port/v1/apigee/organizations/import</h1>
	 * <p>
	 * createConfiguration
	 * </p>
	 * 
	 * @param interactionid
	 * @param jsessionid
	 * @param apigeeConfigurationVO
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Create Configuration", notes = "", code=201)
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created", response = Void.class),
        @ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.POST, value = "/v1/apigee/organizations/import")
	public  ResponseEntity<Void> createConfiguration(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid,
			@RequestBody List<ApigeeConfigurationVO> apigeeConfigurationVO)throws Exception;

	/**
	 * <h1>http://hostname:port/v1/apigee/organizations/import</h1>
	 * <p>
	 * updateConfiguration
	 * </p>
	 * 
	 * @param interactionid
	 * @param jsessionid
	 * @param apigeeConfigurationVO
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Update Configuration", notes = "", code=204)
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "No Content", response = Void.class),
        @ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)
       })
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/apigee/organizations/import")
	public  ResponseEntity<Void> updateConfiguration(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid,
			@RequestBody List<ApigeeConfigurationVO> apigeeConfigurationVO)throws Exception;

	/**
	 * <h1>http://hostname:port/v1/api/apigee/config</h1>
	 * <p>
	 * deleteConfiguration
	 * </p>
	 * 
	 * @param interactionid
	 * @param jsessionid
	 * @param apigeeConfigurationVO
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Delete Configuration", notes = "", code=200)
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = Void.class),
        @ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)
       })
	@RequestMapping(method = { RequestMethod.DELETE, RequestMethod.PATCH}, value = "/v1/api/apigee/config")
	public  ResponseEntity<Void> deleteConfiguration(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid,
			@RequestBody ApigeeConfigurationVO apigeeConfigurationVO)throws Exception;
	
	@RequestMapping(method = RequestMethod.PUT , value = "/v1/apigee/service-accounts")
	public  ResponseEntity<Void> updateServiceAccount(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid,
			@RequestBody List<ApigeeServiceUser> apigeeServiceUsers)throws Exception;
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apigee/service-accounts")
	public  ResponseEntity<Object> getServiceAccount(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;
	
	
	@RequestMapping(method = RequestMethod.POST , value = "/v1/connectors/apigee")
	public  ResponseEntity<Void> createApigeeIntegration(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid,
			@RequestBody ApigeeIntegrationVO apigeeIntegrationVO)throws Exception;
	
	@RequestMapping(method = RequestMethod.GET , value = "/v1/connectors/apigee")
	public  ResponseEntity<?> listApigeeIntegrations(@RequestHeader(
			value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid)throws Exception;
	
	@RequestMapping(method = RequestMethod.GET , value = "/v1/connectors/apigee/{orgId}")
	public  ResponseEntity<?> getApigeeIntegration(@RequestHeader(
			value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid,
			@PathVariable String orgId)throws Exception;
	
	@RequestMapping(method = RequestMethod.PUT , value = "/v1/connectors/apigee/{orgId}")
	public  ResponseEntity<Void> updateApigeeIntegration(@RequestHeader(
			value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid,
			@PathVariable String orgId,
			@RequestBody ApigeeIntegrationVO apigeeIntegrationVO)throws Exception;
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/connectors/apigee/{orgId}")
	public  ResponseEntity<?> deleteApigeeIntegration(@RequestHeader(
			value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid,
			@PathVariable String orgId)throws Exception;
}
