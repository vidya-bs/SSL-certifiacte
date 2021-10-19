package com.itorix.apiwiz.datamanagement.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.itorix.apiwiz.common.model.apigee.ApigeeConfigurationVO;
import com.itorix.apiwiz.common.model.apigee.ApigeeIntegrationVO;
import com.itorix.apiwiz.common.model.apigee.ApigeeServiceUser;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.data.management.business.ApigeeConfigurationBusiness;
import com.itorix.apiwiz.datamanagement.service.ApigeeConfigurationService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
@Api(value = "DMB", tags = "DMB")
public class ApigeeConfigurationServiceImpl implements ApigeeConfigurationService {

	@Autowired
	private ApigeeConfigurationBusiness apigeeConfigurationBusiness;
	private static final Logger logger = LoggerFactory.getLogger(ApigeeConfigurationServiceImpl.class);

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/apigee/organizations/config</h1>
	 *
	 * <p>
	 * getConfiguration
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Get Configuration", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = ApigeeConfigurationVO.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apigee/organizations/config")
	public ResponseEntity<List<ApigeeConfigurationVO>> getConfiguration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid) throws Exception {
		List<ApigeeConfigurationVO> list = apigeeConfigurationBusiness.getConfiguration(interactionid, jsessionid);
		return new ResponseEntity<List<ApigeeConfigurationVO>>(list, HttpStatus.OK);
	}

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/apigee/organizations</h1>
	 *
	 * <p>
	 * getListOfOrgAndEnv
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "GetList Of Org And Env", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = ApigeeConfigurationVO.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apigee/organizations")
	public ResponseEntity<List<ApigeeConfigurationVO>> getListOfOrgAndEnv(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid) throws Exception {
		List<ApigeeConfigurationVO> responseList = new ArrayList<>();
		List<ApigeeConfigurationVO> list = apigeeConfigurationBusiness.getConfiguration(interactionid, jsessionid);
		if (list != null) {
			for (ApigeeConfigurationVO vo : list) {
				ApigeeConfigurationVO acvo = new ApigeeConfigurationVO();
				acvo.setId(vo.getId());
				acvo.setOrgname(vo.getOrgname());
				acvo.setType(vo.getType());
				acvo.setEnvironments(vo.getEnvironments());
				responseList.add(acvo);
			}
		}
		return new ResponseEntity<List<ApigeeConfigurationVO>>(responseList, HttpStatus.OK);
	}

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/apigee/host</h1>
	 *
	 * <p>
	 * getApigeeHost
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param type
	 * @param org
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	@ApiOperation(value = "Get Apigee Host", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = String.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/apigee/host")
	public ResponseEntity<Object> getApigeeHost(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @RequestParam("type") String type,
			@RequestParam("org") String org) throws Exception {
		Object o = apigeeConfigurationBusiness.getApigeeHost(type, org);
		return new ResponseEntity<Object>(o, HttpStatus.OK);
	}

	public ResponseEntity<Object> getApigeeAuthorization(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @RequestParam("type") String type,
			@RequestParam("org") String org) throws Exception {
		Object o = apigeeConfigurationBusiness.getApigeeAuthorization(type, org);
		return new ResponseEntity<Object>(o, HttpStatus.OK);
	}

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/apigee/organizations/import</h1>
	 *
	 * <p>
	 * createConfiguration
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param apigeeConfigurationVO
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Create Configuration", notes = "", code = 201)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Created", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/apigee/organizations/import")
	public ResponseEntity<Void> createConfiguration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid,
			@RequestBody List<ApigeeConfigurationVO> apigeeConfigurationVO) throws Exception {
		apigeeConfigurationBusiness.createConfiguration(apigeeConfigurationVO, interactionid, jsessionid);
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/apigee/organizations/import</h1>
	 *
	 * <p>
	 * updateConfiguration
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param apigeeConfigurationVO
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Update Configuration", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/apigee/organizations/import")
	public ResponseEntity<Void> updateConfiguration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid,
			@RequestBody List<ApigeeConfigurationVO> apigeeConfigurationVO) throws Exception {
		apigeeConfigurationBusiness.updateConfiguration(apigeeConfigurationVO, interactionid, jsessionid);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/apigee/config</h1>
	 *
	 * <p>
	 * deleteConfiguration
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param apigeeConfigurationVO
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Delete Configuration", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = {RequestMethod.DELETE, RequestMethod.PATCH}, value = "/v1/api/apigee/config")
	public ResponseEntity<Void> deleteConfiguration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid,
			@RequestBody ApigeeConfigurationVO apigeeConfigurationVO) throws Exception {
		apigeeConfigurationBusiness.deleteConfiguration(apigeeConfigurationVO, interactionid);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	public ResponseEntity<Void> updateServiceAccount(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid,
			@RequestBody List<ApigeeServiceUser> apigeeServiceUsers) throws Exception {
		apigeeConfigurationBusiness.updateServiceAccount(apigeeServiceUsers);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<Object> getServiceAccount(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		return new ResponseEntity<Object>(apigeeConfigurationBusiness.getServiceAccounts(), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1/connectors/apigee")
	public ResponseEntity<Void> createApigeeIntegration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid,
			@RequestBody ApigeeIntegrationVO apigeeIntegrationVO) throws Exception {

		apigeeConfigurationBusiness.createApigeeIntegration(apigeeIntegrationVO);
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/v1/connectors/apigee")
	public ResponseEntity<?> listApigeeIntegrations(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid) throws Exception {
		return new ResponseEntity<>(apigeeConfigurationBusiness.listApigeeIntegrations(), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/v1/connectors/apigee/{orgId}")
	public ResponseEntity<?> getApigeeIntegration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable String orgId) throws Exception {
		return new ResponseEntity<>(apigeeConfigurationBusiness.getApigeeIntegration(orgId), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/connectors/apigee/{orgId}")
	public ResponseEntity<Void> updateApigeeIntegration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable String orgId,
			@RequestBody ApigeeIntegrationVO apigeeIntegrationVO) throws Exception {
		apigeeIntegrationVO.setId(orgId);
		apigeeConfigurationBusiness.updateApigeeIntegration(apigeeIntegrationVO);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/connectors/apigee/{orgId}")
	public ResponseEntity<Void> deleteApigeeIntegration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "jsessionid") String jsessionid, @PathVariable String orgId) throws Exception {
		apigeeConfigurationBusiness.deleteApigeeIntegration(orgId);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}
}
