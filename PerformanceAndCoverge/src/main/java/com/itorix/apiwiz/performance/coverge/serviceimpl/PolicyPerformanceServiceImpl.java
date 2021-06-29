package com.itorix.apiwiz.performance.coverge.serviceimpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.itorix.apiwiz.common.model.apigee.ApigeeServiceUser;
import com.itorix.apiwiz.common.model.apigee.CommonConfiguration;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.service.GridFsRepository;
import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.performance.coverge.businessimpl.PolicyPerformanceBusinessImpl;
import com.itorix.apiwiz.performance.coverge.model.History;
import com.itorix.apiwiz.performance.coverge.model.PolicyPerformanceBackUpInfo;
import com.itorix.apiwiz.performance.coverge.service.PolicyPerformanceService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
public class PolicyPerformanceServiceImpl implements PolicyPerformanceService {
	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired(required = true)
	GridFsRepository gridFsRepository;

	@Autowired
	BaseRepository baseRepository;


	@Autowired
	PolicyPerformanceBusinessImpl policyPerformanceService;
	
	@Autowired
	private ApigeeUtil apigeeUtil;

	/**
	 * <h1>http://hostname:port/v1/buildconfig/policyperformance</h1>
	 * <p>
	 * preparePolicyPerformance
	 * </p>
	 * 
	 * @param interactionid
	 * @param postmanFile
	 * @param envFile
	 * @param org
	 * @param env
	 * @param proxy
	 * @param type
	 * @param headers
	 * @param jsessionid
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 */
	@ApiOperation(value = "Prepare PolicyPerformance", notes = "", code = 200)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Ok", response = PolicyPerformanceBackUpInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.POST, value = "/v1/buildconfig/policyperformance", produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Object> preparePolicyPerformance(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "postmanFile", required = false) MultipartFile postmanFile, 
			@RequestParam(value = "envFile" , required = false) MultipartFile envFile,
			@RequestParam("org") String org, 
			@RequestParam("env") String env,
			@RequestParam("proxy") String proxy,
			@RequestParam(value = "testsuiteId", required = false) String testsuiteId, 
			@RequestParam(value = "variableId", required = false) String variableId, 
			@RequestParam(value = "type", required = false) String type, 
			@RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		User user = policyPerformanceService.getUserDetailsFromSessionID(jsessionid);
		CommonConfiguration cfg = new CommonConfiguration();
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(org, type);
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(org, type));
		cfg.setOrganization(org);
		cfg.setEnvironment(env);
		cfg.setApiName(proxy);
		cfg.setInteractionid(interactionid);
		cfg.setJsessionId(jsessionid);
		cfg.setType(type);
		cfg.setPolicyPerformance(true);
		cfg.setTestsuiteId(testsuiteId);
		cfg.setVariableId(variableId);
		cfg.setPostmanFile(postmanFile);
		cfg.setEnvFile(envFile);
		cfg.setUserName(user.getFirstName() + " " + user.getLastName());
		Object obj = policyPerformanceService.executePolicyPerformance(cfg);
		return new ResponseEntity<Object>(obj, HttpStatus.OK);
	}

	/**
	 * <h1>http://hostname:port/v1/buildconfig/policyperformance</h1>
	 * <p>
	 * getPolicyPerformanceList
	 * </p>
	 * 
	 * @param interactionid
	 * @param headers
	 * @param jsessionid
	 * @param request
	 * @param response
	 * @return
	 */
	/*
	 * @ApiOperation(value = "Get Policy Performance List", notes = "", code =
	 * 200)
	 * 
	 * @ApiResponses(value = {
	 * 
	 * @ApiResponse(code = 200, message = "Ok", response = History.class,
	 * responseContainer = "List"),
	 * 
	 * @ApiResponse(code = 500, message =
	 * "Internal server error. Please contact support for further instructions.", response =
	 * ErrorObj.class) })
	 * 
	 * @RequestMapping(method = RequestMethod.GET, value =
	 * "/v1/buildconfig/policyperformance", produces = {
	 * MediaType.APPLICATION_JSON_VALUE }) public ResponseEntity<Object>
	 * getPolicyPerformanceList(
	 * 
	 * @RequestHeader(value = "interactionid", required = false) String
	 * interactionid,
	 * 
	 * @RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID")
	 * String jsessionid)throws Exception { List<History> history = new
	 * ArrayList<History>(); history =
	 * policyPerformanceService.getPolicyPerformanceList(interactionid); return
	 * new ResponseEntity<Object>(history, HttpStatus.OK); }
	 */

	/**
	 * <h1>http://hostname:port/v1/buildconfig/policyperformance/{id}</h1>
	 * <p>
	 * getPolicyPerformanceOnId
	 * </p>
	 * 
	 * @param interactionid
	 * @param id
	 * @param headers
	 * @param jsessionid
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Get Code Policy Performance ForId", notes = "", code = 200)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Ok", response = PolicyPerformanceBackUpInfo.class),
			@ApiResponse(code = 404, message = "Resource not found. Request validation failed. Please check the mandatory data fields and retry again.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/buildconfig/policyperformance/{id}", produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Object> getPolicyPerformanceOnId(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("id") String id, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		PolicyPerformanceBackUpInfo performanceBackUpInfo = null;
		if (id == null || id.length() <= 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("PolicyPerformance-1000"), "PolicyPerformance-1000");
		}
		performanceBackUpInfo = policyPerformanceService.getPolicyPerformanceOnId(id, interactionid);
		return new ResponseEntity<Object>(performanceBackUpInfo, HttpStatus.OK);

	}

	/**
	 * <h1>http://hostname:port/v1/buildconfig/policyperformance/{id}</h1>
	 * <p>
	 * deletePolicyPerformanceOnId
	 * </p>
	 * 
	 * @param interactionid
	 * @param id
	 * @param headers
	 * @param jsessionid
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Delete PolicyPerformance On Id", notes = "", code = 204)
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. Request validation failed. Please check the mandatory data fields and retry again.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/buildconfig/policyperformance/{id}")
	public ResponseEntity<Void> deletePolicyPerformanceOnId(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("id") String id, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		if (id == null || id.length() <= 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("PolicyPerformance-1000"), "PolicyPerformance-1000");
		}
		policyPerformanceService.deletePolicyPerformanceOnId(id, interactionid);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@ApiOperation(value = "Get Policy Performance List", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = History.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/buildconfig/policyperformance", produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<History>> getPolicyPerformanceList(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(name = "filter", required = false) boolean filter,
			@RequestParam(name = "proxy", required = false) String proxy,
			@RequestParam(name = "org", required = false) String org,
			@RequestParam(name = "env", required = false) String env,
			@RequestParam(name = "daterange", required = false) String daterange) throws Exception {
		List<History> history = new ArrayList<History>();
		history = policyPerformanceService.getPolicyPerformanceList(interactionid, filter, proxy, org, env, daterange);
		return new ResponseEntity<List<History>>(history, HttpStatus.OK);
	}
}
