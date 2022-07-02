package com.itorix.apiwiz.performance.coverge.serviceimpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.apigee.ApigeeServiceUser;
import com.itorix.apiwiz.common.model.apigee.CommonConfiguration;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.postman.SoapUiEnvFileInfo;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.service.GridFsRepository;
import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.performance.coverge.businessimpl.CodeCoverageBusinessImpl;
import com.itorix.apiwiz.performance.coverge.businessimpl.PostManService;
import com.itorix.apiwiz.performance.coverge.model.CodeCoverageBackUpInfo;
import com.itorix.apiwiz.performance.coverge.model.CodeCoverageVO;
import com.itorix.apiwiz.performance.coverge.model.History;
import com.itorix.apiwiz.performance.coverge.model.MonitorResponse;
import com.itorix.apiwiz.performance.coverge.model.PostManEnvFileInfo;
import com.itorix.apiwiz.performance.coverge.service.CodeCoverageService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
public class CodeCoverageServiceImpl implements CodeCoverageService {
	private static final Logger logger = LoggerFactory.getLogger(CodeCoverageServiceImpl.class);
	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired(required = true)
	GridFsRepository gridFsRepository;

	@Autowired
	BaseRepository baseRepository;

	@Autowired
	CodeCoverageBusinessImpl codeCoverageService;

	@Autowired
	PostManService postManService;

	@Autowired
	private ApigeeUtil apigeeUtil;

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/buildconfig/codecoverage</h1>
	 *
	 * Using this we can test the the code coverage how much we done
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
	 * 
	 * @return
	 * 
	 * @throws IOException
	 * @throws JAXBException
	 */
	@ApiOperation(value = "Prepare CodeCoverage", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = CodeCoverageBackUpInfo.class),
			@ApiResponse(code = 400, message = "Sorry! There is no apigee credentails defined for the logged in user.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/buildconfig/codecoverage", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> prepareCodeCoverage(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwtype,
			@RequestParam(value = "postmanFile", required = false) MultipartFile postmanFile,
			@RequestParam(value = "envFile", required = false) MultipartFile envFile, @RequestParam("org") String org,
			@RequestParam("env") String env, @RequestParam("proxy") String proxy,
			@RequestParam(value = "testsuiteId", required = false) String testsuiteId,
			@RequestParam(value = "variableId", required = false) String variableId,
			@RequestParam(value = "type", required = false) String type, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		CodeCoverageBackUpInfo codeCoverageBackUpInfo = null;
		User user = codeCoverageService.getUserDetailsFromSessionID(jsessionid);
		CommonConfiguration cfg = new CommonConfiguration();
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(org, type);
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(org, type));
		cfg.setOrganization(org);
		cfg.setEnvironment(env);
		cfg.setApiName(proxy);
		cfg.setPostmanFile(postmanFile);
		cfg.setEnvFile(envFile);
		cfg.setInteractionid(interactionid);
		cfg.setJsessionId(jsessionid);
		cfg.setType(type);
		cfg.setCodeCoverage(true);
		cfg.setTestsuiteId(testsuiteId);
		cfg.setVariableId(variableId);
		cfg.setUserName(user.getFirstName() + " " + user.getLastName());
		cfg.setGwtype(type);
		codeCoverageBackUpInfo = codeCoverageService.executeCodeCoverage(cfg);
		return new ResponseEntity<Object>(codeCoverageBackUpInfo, HttpStatus.OK);
	}

	/**
	 *
	 *
	 * <h1>http://hostname:port/v1/buildconfig/codecoverage</h1>
	 *
	 * <p>
	 * This service fetches all the codecoverages that were ran
	 *
	 * @param interactionid
	 * @param headers
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	/*
	 * @ApiOperation(value = "Get Code Coverages", notes = "", code=200)
	 * 
	 * @ApiResponses(value = {
	 * 
	 * @ApiResponse(code = 200, message = "Ok", response =
	 * History.class,responseContainer = "List"),
	 * 
	 * @ApiResponse(code = 500, message =
	 * "Internal server error. Please contact support for further instructions."
	 * , response = ErrorObj.class) })
	 * 
	 * @RequestMapping(method = RequestMethod.GET, value =
	 * "/v1/buildconfig/codecoverage", produces = {
	 * MediaType.APPLICATION_JSON_VALUE }) public ResponseEntity<Object>
	 * getCodeCoverages(@RequestHeader(value = "interactionid", required =
	 * false) String interactionid,
	 * 
	 * @RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID")
	 * String jsessionid)throws Exception { List<History> history = new
	 * ArrayList<History>(); history =
	 * codeCoverageService.getCodeCoverageList(interactionid); return new
	 * ResponseEntity<Object>(history, HttpStatus.OK); }
	 */

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
	@RequestMapping(method = RequestMethod.GET, value = "/v1/buildconfig/codecoverage/{id}", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> getCodeCoverageOverviewForId(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("id") String id, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		CodeCoverageBackUpInfo codeCoverageBackUpInfo = null;
		if (id == null || id.length() <= 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("CodeCoverage-1000"), "CodeCoverage-1000");
		}
		codeCoverageBackUpInfo = codeCoverageService.getCodeCoverageOnId(id, interactionid);
		if (codeCoverageBackUpInfo == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("CodeCoverage-1000"), "CodeCoverage-1000");
		}
		return new ResponseEntity<Object>(codeCoverageBackUpInfo, HttpStatus.OK);
	}

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
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/buildconfig/codecoverage/{id}")
	public ResponseEntity<Void> deleteCodeCoverageOverviewForId(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("id") String id, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		if (id == null || id.length() <= 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("CodeCoverage-1000"), "CodeCoverage-1000");
		}
		codeCoverageService.deleteCodeCoverageOnId(id, interactionid);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@ApiOperation(value = "Get Code Coverages", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = MonitorResponse.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/buildconfig/codecoverage")
	public ResponseEntity<List<History>> getMonitoringStats(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(name = "filter", required = false) boolean filter,
			@RequestParam(name = "proxy", required = false) String proxy,
			@RequestParam(name = "org", required = false) String org,
			@RequestParam(name = "env", required = false) String env,
			@RequestParam(name = "daterange", required = false) String daterange) throws Exception {
		List<History> history = new ArrayList<History>();
		history = codeCoverageService.getCodeCoverageList(interactionid, filter, proxy, org, env, daterange);
		return new ResponseEntity<List<History>>(history, HttpStatus.OK);
	}

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
	public Object getUnitTests(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam("org") String org, @RequestParam("env") String env, @RequestParam("proxy") String proxy,
			@RequestParam(value = "testsuitId", required = false) String testsuitId,
			@RequestParam(value = "artifactType", required = false) String artifactType,
			@RequestParam(value = "variableId", required = false) String variableId,
			@RequestParam("isSaaS") boolean isSaaS) throws Exception {
		Object res = null;
		try {
			if (artifactType == null || artifactType.equalsIgnoreCase("Postman")) {
				String postman = (String) postManService.getPostMan(org, env, proxy, interactionid,
						PostManEnvFileInfo.UNIT_TEST, isSaaS);
				String environment = (String) postManService.getEnvFile(org, env, proxy, interactionid,
						PostManEnvFileInfo.UNIT_TEST, isSaaS);
				res = codeCoverageService.executeUnitTests(postman, environment);
			} else if (artifactType != null && artifactType.equalsIgnoreCase("SoapUi")) {
				String soapUiFile = (String) postManService.getSoapUiInfoFile(org, env, proxy, interactionid,
						SoapUiEnvFileInfo.UNIT_TEST, isSaaS);
				res = codeCoverageService.executeSoapUi(soapUiFile, null);
			} else if (artifactType != null && artifactType.equalsIgnoreCase("testsuite")) {
				res = codeCoverageService.executeTestsuiteUnittests(testsuitId, variableId);
			}
			return res;
		} catch (ItorixException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

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
	@SuppressWarnings({"unchecked", "rawtypes"})
	public Object executeCodeCoverage(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody CodeCoverageVO codeCoverageVO, @RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "isSaaS", required = false) boolean isSaaS) throws Exception {
		CodeCoverageBackUpInfo res = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			String env = codeCoverageVO.getEnv();
			String org = codeCoverageVO.getOrg();
			String proxy = codeCoverageVO.getProxy();
			String postmanFile = null;
			try {
				postmanFile = (String) postManService.getPostMan(org, env, proxy, null,
						PostManEnvFileInfo.CODE_COVERAGE, isSaaS);
				logger.debug(mapper.writeValueAsString(codeCoverageVO));
			} catch (Exception e) {

			}
			codeCoverageVO.setPostmanFile(postmanFile);
			codeCoverageVO.setType(type);
			String envFile = null;
			try {
				envFile = (String) postManService.getEnvFile(codeCoverageVO.getOrg(), codeCoverageVO.getEnv(),
						codeCoverageVO.getProxy(), interactionid, PostManEnvFileInfo.CODE_COVERAGE, isSaaS);
			} catch (Exception e) {

			}
			codeCoverageVO.setEnvFile(envFile);
			res = codeCoverageService.codeCoverageTest(codeCoverageVO);
			Map returnData = new HashMap();
			returnData.put("codeCoverage", res.getProxyStat().getCoverage());
			returnData.put("downloadURL", res.getUrl());
			return returnData;
		} catch (ItorixException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}
}
