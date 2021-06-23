package com.itorix.apiwiz.performance.coverge.service;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;
import org.springframework.http.HttpHeaders;
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

import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.performance.coverge.model.History;
import com.itorix.apiwiz.performance.coverge.model.PolicyPerformanceBackUpInfo;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
public interface  PolicyPerformanceService {
	

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
			@RequestParam(value = "type", required = false) String type, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception ;

	
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
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception ;

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
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;
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
			@RequestParam(name = "daterange", required = false) String daterange) throws Exception;
}
