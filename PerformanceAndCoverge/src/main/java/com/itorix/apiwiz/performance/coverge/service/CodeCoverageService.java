package com.itorix.apiwiz.performance.coverge.service;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.performance.coverge.model.CodeCoverageBackUpInfo;
import com.itorix.apiwiz.performance.coverge.model.CodeCoverageVO;
import com.itorix.apiwiz.performance.coverge.model.History;
import com.itorix.apiwiz.performance.coverge.model.MonitorResponse;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
public interface CodeCoverageService {

	/**
	 * <h1>http://hostname:port/v1/buildconfig/codecoverage</h1>
	 * <p>
	 * Using this we can test the the code coverage how much we done
	 *
	 * @param request
	 * @param response
	 * @param interactionid
	 * @param postmanFile
	 * @param envFile
	 * @param org
	 * @param env
	 * @param proxy
	 * @param type
	 * @param headers
	 * @param jsessionid
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 */
	@ApiOperation(value = "Prepare CodeCoverage", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = CodeCoverageBackUpInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('ADMIN','DEVELOPER') and hasAnyAuthority('TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/buildconfig/codecoverage", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> prepareCodeCoverage(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "postmanFile", required = false) MultipartFile postmanFile,
			@RequestParam(value = "envFile", required = false) MultipartFile envFile, @RequestParam("org") String org,
			@RequestParam("env") String env, @RequestParam("proxy") String proxy,
			@RequestParam(value = "testsuiteId", required = false) String testsuiteId,
			@RequestParam(value = "variableId", required = false) String variableId,
			@RequestParam(value = "type", required = false) String type, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/buildconfig/codecoverage/{id}</h1>
	 *
	 * <p>
	 * This service fetches code coverage based on id that were ran
	 *
	 * @param interactionid
	 * @param id
	 * @param headers
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Get Code Coverage Overview ForId", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = CodeCoverageBackUpInfo.class),
			@ApiResponse(code = 404, message = "Resource not found. Request validation failed. Please check the mandatory data fields and retry again.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/buildconfig/codecoverage/{id}", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> getCodeCoverageOverviewForId(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("id") String id, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/buildconfig/codecoverage/{id}</h1>
	 *
	 * @param interactionid
	 * @param id
	 * @param headers
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Delete Code Coverage Overview ForId", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. Request validation failed. Please check the mandatory data fields and retry again.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('ADMIN','DEVELOPER') and hasAnyAuthority('TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/buildconfig/codecoverage/{id}")
	public ResponseEntity<Void> deleteCodeCoverageOverviewForId(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("id") String id, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@ApiOperation(value = "Get Code Coverages", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = MonitorResponse.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/buildconfig/codecoverage")
	public ResponseEntity<List<History>> getMonitoringStats(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(name = "filter", required = false) boolean filter,
			@RequestParam(name = "proxy", required = false) String proxy,
			@RequestParam(name = "org", required = false) String org,
			@RequestParam(name = "env", required = false) String env,
			@RequestParam(name = "daterange", required = false) String daterange) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/codecoverage/unittest</h1>
	 *
	 * @param interactionid
	 * @param headers
	 * @param org
	 * @param env
	 * @param proxy
	 * @param isSaaS
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/cicd/unittest")
	public Object getUnitTests(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam("org") String org, @RequestParam("env") String env, @RequestParam("proxy") String proxy,
			@RequestParam(value = "testsuitId", required = false) String testsuitId,
			@RequestParam(value = "artifactType", required = false) String artifactType,
			@RequestParam(value = "variableId", required = false) String variableId,
			@RequestParam("isSaaS") boolean isSaaS) throws Exception;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/api/codecoverage/execute</h1>
	 *
	 * @param interactionid
	 * @param headers
	 * @param codeCoverageVO
	 * @param type
	 * @param isSaaS
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	@PreAuthorize("hasAnyRole('ADMIN','DEVELOPER') and hasAnyAuthority('TEAM','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/cicd/codecoverage")
	public Object executeCodeCoverage(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody CodeCoverageVO codeCoverageVO, @RequestParam(value = "type", required = false) String type,
			@RequestParam("isSaaS") boolean isSaaS) throws Exception;
}
