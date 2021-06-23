package com.itorix.apiwiz.cicd.service;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
public interface SoapArtifactService {


	@ApiOperation(value = "getPostManDetails", notes = "", code = 200, response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "getPostManDetails", response = String.class),
			@ApiResponse(code = 400, message = "Resource not found. Please check the request and retry again.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class) })
	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/soap-ui/file")
	public ResponseEntity<Object> getPostman(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value="orgId", required = false) String orgId,
			@RequestParam("org") String org, @RequestParam("env") String env, @RequestParam("proxy") String proxy,
			@RequestParam("type") String type, @RequestParam("isSaaS") boolean isSaaS,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response) throws Throwable, IOException;


	@ApiOperation(value = "getPostmanFilesList", notes = "", code = 200, response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "getPostmanFilesList", response = String.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class) })
	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/soap-ui/overview")
	public ResponseEntity<Object> getPostmanFilesList(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value="orgId", required = false) String orgId,
			@RequestParam(value="org", required = false) String org,
			@RequestParam(value="env", required = false) String env,
			@RequestParam(value="proxy", required = false) String proxy,
			@RequestParam(value="type", required = false) String type,
			@RequestParam(value="isSaaS", required = false) boolean isSaaS,
			HttpServletRequest request, HttpServletResponse response);


	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/soap-ui/environment/overview")
	public ResponseEntity<Object> getEnvFilesList(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value="orgId", required = false) String orgId,
			@RequestParam(value="org", required = false) String org,
			@RequestParam(value="env", required = false) String env,
			@RequestParam(value="proxy", required = false) String proxy,
			@RequestParam(value="type", required = false) String type,
			@RequestParam(value="isSaaS", required = false) boolean isSaaS,
			HttpServletRequest request, HttpServletResponse response);



	@ApiOperation(value = "updatePostman", notes = "", code = 200, response = Void.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "updatePostman", response = Void.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class) })
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION','DEVELOPER','PROJECT-ADMIN') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/soap-ui/file")
	public ResponseEntity<Object> updatePostman(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value="orgId", required = false) String orgId,
			@RequestParam("file") MultipartFile file, @RequestParam("org") String org,
			@RequestParam("env") String env, @RequestParam("proxy") String proxy, @RequestParam("type") String type,
			@RequestHeader HttpHeaders headers, @RequestParam("isSaaS") boolean isSaaS,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			HttpServletResponse response) throws ItorixException;

	@ApiOperation(value = "getenv", notes = "", code = 200, response = Void.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "getenv", response = Void.class),
			@ApiResponse(code = 400, message = "Resource not found. Please check the request and retry again.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class) })

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/soap-ui/environment/file")
	public ResponseEntity<Object> getenv(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value="orgId", required = false) String orgId,
			@RequestParam("org") String org, @RequestParam("env") String env, @RequestParam("proxy") String proxy,
			@RequestParam("type") String type, @RequestParam("isSaaS") boolean isSaaS,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response) throws IOException, ItorixException ;


	@ApiOperation(value = "updateEnvironementFile", notes = "", code = 204, response = Void.class)
	@ApiResponses(value = { @ApiResponse(code = 204, message = "updateEnvironementFile", response = Void.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class) })
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION','DEVELOPER','PROJECT-ADMIN') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/soap-ui/environment/file")
	public ResponseEntity<Object> updateEnvironementFile(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value="orgId", required = false) String orgId,
			@RequestParam("file") MultipartFile file, @RequestParam("org") String org,
			@RequestParam("env") String env, @RequestParam("proxy") String proxy, @RequestParam("type") String type,
			@RequestParam("isSaaS") boolean isSaaS, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			HttpServletResponse response) throws ItorixException ;

	@ApiOperation(value = "deletePostManFile", notes = "", code = 204, response = Void.class)
	@ApiResponses(value = { @ApiResponse(code = 204, message = "deletePostManFile", response = Void.class),
			@ApiResponse(code = 400, message = "PostMan or Resource not found. Please check the request and retry again.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class) })
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION','DEVELOPER','PROJECT-ADMIN') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/soap-ui/file")
	public ResponseEntity<Object> deletePostManEnvFile(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value="orgId", required = false) String orgId,
			@RequestParam("org") String org, @RequestParam("env") String env, @RequestParam("proxy") String proxy,
			@RequestParam("type") String type, @RequestParam("isSaaS") boolean isSaaS,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response) throws ItorixException;

	@ApiOperation(value = "deleteEnvFile", notes = "", code = 204, response = Void.class)
	@ApiResponses(value = { @ApiResponse(code = 204, message = "deleteEnvFile", response = Void.class),
			@ApiResponse(code = 400, message = "PostMan or Resource not found. Please check the request and retry again.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class) })
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION','DEVELOPER','PROJECT-ADMIN') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/soap-ui/environment/file")
	public ResponseEntity<Object> deleteEnvFile(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value="orgId", required = false) String orgId,
			@RequestParam("org") String org, @RequestParam("env") String env, @RequestParam("proxy") String proxy,
			@RequestParam("type") String type, @RequestParam("isSaaS") boolean isSaaS,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response) throws ItorixException;


}
