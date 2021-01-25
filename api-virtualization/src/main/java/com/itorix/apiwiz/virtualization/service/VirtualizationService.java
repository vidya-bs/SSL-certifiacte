package com.itorix.apiwiz.virtualization.service;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.virtualization.model.GroupVO;
import com.itorix.apiwiz.virtualization.model.expectation.Expectation;
import com.itorix.apiwiz.virtualization.model.logging.MockLog;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
public interface VirtualizationService {


	@ApiOperation(value = "Get grooups", notes = "", code=200, response=GroupVO.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "list of available groups", response = GroupVO.class),
			@ApiResponse(code = 404, message = "Requestd group does not exist.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
	})
	@RequestMapping(method = RequestMethod.GET, value = {"/v1/mock/scenarios-groups", "/v1/mock/scenarios-groups/{groupId}"})
	public org.springframework.http.ResponseEntity<Object> getGroups(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "names", required = false) String names,
			@RequestParam(value="filter", required=false) String filter,
			@PathVariable(value = "groupId", required = false) String groupId) throws Exception;

	@ApiOperation(value = "Create group", notes = "", code=201, response=Void.class)
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Group Created sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Bad request.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
	})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/mock/scenarios-groups")
	public org.springframework.http.ResponseEntity<?> createGroup(
			@RequestBody GroupVO group,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid);

	@ApiOperation(value = "Update group", notes = "", code=204, response=Void.class)
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Group updated sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Bad request.", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "requested group does not exist.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
	})
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/mock/scenarios-groups/{groupId}")
	public org.springframework.http.ResponseEntity<?> updateGroup(
			@RequestBody GroupVO group,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@PathVariable(value="groupId",required=true) String groupId,
			@RequestHeader(value = "interactionid", required = false) String interactionid);

	@ApiOperation(value = "delete group", notes = "", code=204, response=Void.class)
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Group deleted sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Bad request.", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "requested group does not exist.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
	})
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/mock/scenarios-groups/{groupId}")
	public org.springframework.http.ResponseEntity<?> deleteGroup(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable(value="groupId",required=true) String groupId);


	@ApiOperation(value = "get logs", notes = "", code=200, response=MockLog.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "get list of logentries", response = MockLog.class),
			@ApiResponse(code = 404, message = "no logentry found.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
	})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/mock/logs", produces = "application/json")
	public org.springframework.http.ResponseEntity<?> getLogEntries(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "path", required = false) String path,
			@RequestParam(value = "match", required = false) String match,
			@RequestParam(value="logId",required=false) String logId);


	@RequestMapping(method = RequestMethod.GET, value = "/v1/mock/logs/{logId}", produces = "application/json")
	public org.springframework.http.ResponseEntity<?> getLogEntrie(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable(value="logId",required=false) String logId);


	@ApiOperation(value = "get logs for a expectation", notes = "", code=200, response=MockLog.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "get list of logentries", response = MockLog.class),
			@ApiResponse(code = 404, message = "no logentry found.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class)
	})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/mock/{expectationId}/logs", produces = "application/json")
	public org.springframework.http.ResponseEntity<?> getExpectationLogEntries(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("expectationId") String expectationId);

	@RequestMapping(method = RequestMethod.GET, value = "/v1/mock/logs/expectation/names", produces = "application/json")
	public org.springframework.http.ResponseEntity<?> getExpectationLogNames(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid);

/////////////////////////////////////


	@RequestMapping(method = RequestMethod.POST, value = "/v1/mock/scenarios" , produces = "application/json")
	public org.springframework.http.ResponseEntity<?> createScenario(
			@RequestBody Expectation  expectation,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid) throws Exception;


	@RequestMapping(method = RequestMethod.PUT, value = "/v1/mock/scenarios/{scenarioId}")
	public org.springframework.http.ResponseEntity<?> updateScenario(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@PathVariable(value="scenarioId",required=true) String scenarioId,
			@RequestBody Expectation  expectation,
			@RequestHeader(value = "interactionid", required = false) String interactionid) throws Exception;


	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/mock/scenarios/{scenarioId}")
	public org.springframework.http.ResponseEntity<?> deleteScenario(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@PathVariable(value="scenarioId",required=true) String scenarioId,
			@RequestHeader(value = "interactionid", required = false) String interactionid) throws Exception;


	@RequestMapping(method = RequestMethod.GET, value = "/v1/mock/scenarios/{scenarioId}", produces = "application/json")
	public org.springframework.http.ResponseEntity<?> getScenario(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@PathVariable(value="scenarioId",required=false) String scenarioId,
			@RequestHeader(value = "interactionid", required = false) String interactionid) throws Exception;


	@RequestMapping(method = RequestMethod.GET, value = "/v1/mock/scenarios", produces = "application/json")
	public org.springframework.http.ResponseEntity<?> getScenarios(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value="groupId",required=false) String groupId,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", defaultValue = "10") int pageSize) throws Exception;


}
