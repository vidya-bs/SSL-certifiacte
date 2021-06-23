package com.itorix.apiwiz.virtualization.serviceImpl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.virtualization.dao.GroupServiceDAO;
import com.itorix.apiwiz.virtualization.dao.ScenarioServiceDAO;
import com.itorix.apiwiz.virtualization.model.ExpectationResponse;
import com.itorix.apiwiz.virtualization.model.GroupVO;
import com.itorix.apiwiz.virtualization.model.expectation.Expectation;
import com.itorix.apiwiz.virtualization.model.logging.MockLog;
import com.itorix.apiwiz.virtualization.service.VirtualizationService;
import com.itorix.hyggee.mockserver.client.serialization.ObjectMapperFactory;


@CrossOrigin
@RestController
public class VirtualizationServiceImpl implements VirtualizationService {

	private static final Logger logger = LoggerFactory.getLogger(VirtualizationServiceImpl.class);
	@Autowired
	GroupServiceDAO groupService;
	@Autowired
	ScenarioServiceDAO scenarioService;
	@Autowired
	private IdentityManagementDao commonServices;
	ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();

	public org.springframework.http.ResponseEntity<Object> getGroups(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "names", required = false) String names,
			@RequestParam(value="filter",required=false) String filter,
			@PathVariable(value = "groupId", required = false) String groupId) throws Exception{
		if(groupId !=null)
			return  new ResponseEntity<Object>(groupService.getGroup(groupId), HttpStatus.OK);
		else
			if(names != null)
				return  new ResponseEntity<Object>(groupService.getGroupNames(), HttpStatus.OK);
			if(filter != null)
				return  new ResponseEntity<Object>(groupService.getGroups(filter), HttpStatus.OK);
			else
				return  new ResponseEntity<Object>(groupService.getGroups(offset, pageSize), HttpStatus.OK);
	}


	public org.springframework.http.ResponseEntity<?> createGroup(
			@RequestBody GroupVO group,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid){
		logger.debug("inside createGroup method ");
		group = groupService.saveGroup( group, commonServices.getUserDetailsFromSessionID(jsessionId));
		if(group != null) {
			HttpHeaders headers = new HttpHeaders();
			headers.add("Access-Control-Expose-Headers", "X-group-id");
			headers.add("X-group-id", group.getId());
			ResponseEntity<Object> response = new org.springframework.http.ResponseEntity<Object>(headers,HttpStatus.CREATED);
			return response;
		}else {
			return new ResponseEntity<>(new ErrorObj("Group Id is empty", "MOCK-GR400"),HttpStatus.BAD_REQUEST);
		}
	}


	public org.springframework.http.ResponseEntity<?> updateGroup(
			@RequestBody GroupVO group,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@PathVariable(value="groupId",required=true) String groupId,
			@RequestHeader(value = "interactionid", required = false) String interactionid){
		logger.debug("inside updateGroup method ");
		group.setId(groupId);
		if(groupService.updateGroup(group ,  commonServices.getUserDetailsFromSessionID(jsessionId))) {
			ResponseEntity<Object> response = new org.springframework.http.ResponseEntity<Object>(HttpStatus.NO_CONTENT);
			return response;
		}else {
			return new ResponseEntity<>(new ErrorObj("Group Id is empty", "MOCK-GR404"),HttpStatus.BAD_REQUEST);
		}
	}


	public org.springframework.http.ResponseEntity<?> deleteGroup(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable(value="groupId",required=true) String groupId){
		logger.debug("inside deleteGroup method ");
		if(groupId != null && !groupId.equals("")) {
			GroupVO group = new GroupVO();
			group.setId(groupId);
			if(groupService.deleteGroup(group)) {
				ResponseEntity<?> response = new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
				return response;
			}else {
				return new ResponseEntity<>(new ErrorObj("invalid Group Id", "MOCK-GR404"),HttpStatus.NOT_FOUND);
			}
		}else {
			return new ResponseEntity<>(new ErrorObj("Group Id is empty", "MOCK-GR400"),HttpStatus.BAD_REQUEST);
		}
	}


	public org.springframework.http.ResponseEntity<?> getLogEntries(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "path", required = false) String path,
			@RequestParam(value = "match", required = false) String match,
			@RequestParam(value="logId",required=false) String logId){
		if(logId != null && logId !=""){
			MockLog logEntry = scenarioService.getLogEntrie(logId);
			if(logEntry != null)
				return new org.springframework.http.ResponseEntity<Object>(logEntry,HttpStatus.OK);
			else
				return new org.springframework.http.ResponseEntity<Object>(new ErrorObj("no logentry found.","MOCK-LOG404"),HttpStatus.NOT_FOUND);
		}
		else
			return new org.springframework.http.ResponseEntity<Object>(scenarioService.getLogEntries(name, path, match, offset, pageSize),HttpStatus.OK);
	}


	public org.springframework.http.ResponseEntity<?> getLogEntrie(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable(value="logId",required=false) String logId){
		
			MockLog logEntry = scenarioService.getLogEntrie(logId);
//			if(logEntry != null)
				return new org.springframework.http.ResponseEntity<MockLog>(logEntry,HttpStatus.OK);
//			else
//				return new org.springframework.http.ResponseEntity<Object>(new ErrorObj("no logentry found.","MOCK-LOG404"),HttpStatus.NOT_FOUND);
		
	}


	public org.springframework.http.ResponseEntity<?> getExpectationLogEntries(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("expectationId") String expectationId){
		if(expectationId != null && expectationId !=""){
			List <MockLog> logEntries = scenarioService.getLogEntries(expectationId);
			if(logEntries != null)
				return new org.springframework.http.ResponseEntity<Object>(logEntries,HttpStatus.OK);
			else
				return new org.springframework.http.ResponseEntity<Object>(new ErrorObj("no logentry found.","MOCK-LOG404"),HttpStatus.NOT_FOUND);
		}
		else
			return new org.springframework.http.ResponseEntity<Object>(new ErrorObj("no expectation ID provided.","MOCK-LOG400"),HttpStatus.BAD_REQUEST);
	}

	public org.springframework.http.ResponseEntity<?> getExpectationLogNames(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid){
 		return new org.springframework.http.ResponseEntity<Object>(scenarioService.getLogExpectationNames(),HttpStatus.OK);
	}


	public org.springframework.http.ResponseEntity<?> createScenario(
			@RequestBody Expectation  expectationRequest,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid) throws Exception{
		logger.debug("inside create scenario method ");
		if (groupService.isValidGroup(expectationRequest.getGroupId())) {
			String id = scenarioService.createScenario(expectationRequest, jsessionId);
			ResponseEntity<Object> response = new org.springframework.http.ResponseEntity<Object>("{\"id\": \"" + id + "\"}",
					HttpStatus.CREATED);
			return response;
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("MockServer-1002"), "MockServer-1002");
		}
	}


	public org.springframework.http.ResponseEntity<?> updateScenario(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@PathVariable(value="scenarioId",required=true) String scenarioId,
			@RequestBody Expectation  expectationRequest,
			@RequestHeader(value = "interactionid", required = false) String interactionid) throws ItorixException {
		scenarioService.updateScenario(expectationRequest, scenarioId,jsessionId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}


	public org.springframework.http.ResponseEntity<?> deleteScenario(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@PathVariable(value="scenarioId",required=true) String scenarioId,
			@RequestHeader(value = "interactionid", required = false) String interactionid) throws Exception {
		scenarioService.deleteScenario(scenarioId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}


	public org.springframework.http.ResponseEntity<?> getScenario(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@PathVariable(value="scenarioId",required=false) String scenarioId,
			@RequestHeader(value = "interactionid", required = false) String interactionid) throws ItorixException{
		Expectation expectation = scenarioService.getScenario(scenarioId);
		return new ResponseEntity<>(expectation , HttpStatus.OK);
	}


	public org.springframework.http.ResponseEntity<?> getScenarios(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value="groupId",required=false) String groupId,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", defaultValue = "10") int pageSize) {
		ExpectationResponse expectations = scenarioService.getScenarios(groupId, offset, pageSize);
		return new ResponseEntity<>(expectations, HttpStatus.OK);
	}
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1/mock/search", produces = "application/json")
	public org.springframework.http.ResponseEntity<?> searchGroup(
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "name") String name, @RequestParam(value = "limit") int limit) throws Exception{
		return new ResponseEntity<>(groupService.search(name, limit), HttpStatus.OK);
	}
}
