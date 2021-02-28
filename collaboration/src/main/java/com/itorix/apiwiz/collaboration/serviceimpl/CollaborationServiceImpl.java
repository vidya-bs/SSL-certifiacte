package com.itorix.apiwiz.collaboration.serviceimpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itorix.apiwiz.collaboration.business.CollaborationBusiness;
import com.itorix.apiwiz.collaboration.model.Swagger3VO;
import com.itorix.apiwiz.collaboration.model.SwaggerMetadata;
import com.itorix.apiwiz.collaboration.model.SwaggerTeam;
import com.itorix.apiwiz.collaboration.model.SwaggerTeamPermissionVO;
import com.itorix.apiwiz.collaboration.model.SwaggerVO;
import com.itorix.apiwiz.collaboration.model.TeamsHistoryResponse;
import com.itorix.apiwiz.collaboration.service.CollaborationService;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
@CrossOrigin
@RestController
public class CollaborationServiceImpl implements CollaborationService {

	@Autowired
	CollaborationBusiness collaborationBusiness;
	/**
	 * Using this we can create the team.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggerTeam
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Create Team", notes = "", code = 201)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Created", response = Void.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.POST, value = "/v1/teams")
	public ResponseEntity<Void> createTeam(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody SwaggerTeam swaggerTeam)
					throws Exception {
		swaggerTeam.setInteractionid(interactionid);
		SwaggerTeam team = collaborationBusiness.findSwaggerTeam(swaggerTeam);
		if (team != null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Teams-1000"), "Teams-1000");
		}
		collaborationBusiness.createTeam(swaggerTeam);
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	/**
	 * Using this existing team we can update.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param teamname
	 * @param swaggerTeam
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Update Team", notes = "", code = 204)
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 404, message = "No records found for selected swagger team name - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/teams/{teamname}")
	public ResponseEntity<Void> updateTeam(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("teamname") String teamname,
			@RequestBody SwaggerTeam swaggerTeam) throws Exception {
		swaggerTeam.setInteractionid(interactionid);
		swaggerTeam.setJsessionid(jsessionid);
		collaborationBusiness.updateTeam(swaggerTeam, teamname);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	/**
	 * USing this we can delete the team.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param teamname
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "get Team", notes = "", code = 204)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Team", response = SwaggerTeam.class),
			@ApiResponse(code = 404, message = "No records found for selected swagger team name - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/teams/{teamname}")
	public ResponseEntity<SwaggerTeam> getTeam(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("teamname") String teamname)
					throws Exception {
		;
		return new ResponseEntity<SwaggerTeam>(collaborationBusiness.getTeam(teamname, interactionid),HttpStatus.OK);
	}

	/**
	 * USing this we can delete the team.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param teamname
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Delete Team", notes = "", code = 204)
	@ApiResponses(value = { @ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 404, message = "No records found for selected swagger team name - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/teams/{teamname}")
	public ResponseEntity<Void> deleteTeam(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("teamname") String teamname)
					throws Exception {
		collaborationBusiness.deleteTeam(teamname, interactionid, jsessionid);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Using this we can associate the swagger with team.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param swaggerVO
	 * @param request
	 * @param response
	 */
	@ApiOperation(value = "Assoiate Team", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "No records found for selected swagger name - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/{swaggername}/associate-team")
	public ResponseEntity<Void> assoiateTeam(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas,
			@PathVariable("swaggername") String swaggername,
			@RequestBody SwaggerVO swaggerVO) throws Exception {
		Set<String> teams = swaggerVO.getTeams();
		if(oas == null || oas.trim().equals(""))
			oas = "2.0";
		collaborationBusiness.associateTeam(swaggername, teams, interactionid, oas);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	/**
	 * To get the assoiated teams.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Get Assoiated Teams", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "No records found for selected swagger name - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swaggername}/associated-teams")
	public ResponseEntity<?> getAssoiatedTeams(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@RequestHeader(value = "oas", required = false) String oas,
			@PathVariable("swaggername") String swaggername)
					throws Exception {
		if(oas == null || oas.trim().equals(""))
			oas = "2.0";
		List<SwaggerTeam> teams = new ArrayList<SwaggerTeam>();
		Set<String> responseSet = new HashSet<>();
		if( oas.equals("2.0") ){
			SwaggerVO vo = collaborationBusiness.findSwagger(swaggername, interactionid);
			if (vo != null) {
				SwaggerMetadata metadata = collaborationBusiness.getSwaggerMetadata(vo.getName(), oas);
				if(metadata!= null)
					responseSet = metadata.getTeams();
			}
		}
		else if( oas.equals("3.0") ){
			Swagger3VO vo = collaborationBusiness.findSwagger3(swaggername, interactionid);
			if (vo != null) {
				SwaggerMetadata metadata = collaborationBusiness.getSwaggerMetadata(vo.getName(), oas);
				if(metadata!= null)
					responseSet = metadata.getTeams();
			}
		}
		if(responseSet.size() > 0)
		for(String teamName : responseSet)
			teams.add(collaborationBusiness.getTeam(teamName, interactionid));
		return new ResponseEntity<Object>(teams, HttpStatus.OK);
	}

	/**
	 * To get the team permissions.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Get Team Permissions", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = SwaggerTeamPermissionVO.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/teams/permission")
	public ResponseEntity<Object> getTeamPermissions(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		String responseString = collaborationBusiness.getTeamPermissions(interactionid, jsessionid);
		return new ResponseEntity<Object>(responseString, HttpStatus.OK);
	}

	/**
	 * To get the all the teams.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Get Teams", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = TeamsHistoryResponse.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/teams")
	public ResponseEntity<TeamsHistoryResponse> getTeams(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize) throws Exception {
		TeamsHistoryResponse list = collaborationBusiness.findSwaggerTeames(jsessionid, interactionid, offset, pageSize);
		return new ResponseEntity<TeamsHistoryResponse>(list, HttpStatus.OK);
	}

	/**
	 * To get the only team names.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Get Team Names", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "No records found for selected swagger name - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Sorry! Internal server error. Please try again later.", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/team-names")
	public ResponseEntity<List<String>> getTeamNames(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		List<String> list = collaborationBusiness.findSwaggerTeameNames(jsessionid, interactionid);
		return new ResponseEntity<List<String>>(list, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/v1/teams/search", produces = { "application/json" })
	public ResponseEntity<Object> teamSearch(String interactionid, String jsessionid, String name, int limit)
			throws ItorixException, JsonProcessingException {
		return new ResponseEntity<Object>(collaborationBusiness.teamSearch(interactionid,name, limit), HttpStatus.OK);
	}


}
