package com.itorix.apiwiz.collaboration.service;

import java.util.List;
import java.util.Set;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itorix.apiwiz.collaboration.model.SwaggerTeam;
import com.itorix.apiwiz.collaboration.model.SwaggerTeamPermissionVO;
import com.itorix.apiwiz.collaboration.model.SwaggerVO;
import com.itorix.apiwiz.collaboration.model.TeamsHistoryResponse;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
public interface CollaborationService {

	/**
	 * Using this we can create the team.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggerTeam
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('ADMIN','PROJECT-ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Create Team", notes = "", code = 201)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Created", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/teams")
	public ResponseEntity<Void> createTeam(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody SwaggerTeam swaggerTeam)
			throws Exception;

	/**
	 * Using this existing team we can update.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param teamname
	 * @param swaggerTeam
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('ADMIN','PROJECT-ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Update Team", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected team name - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/teams/{teamname}")
	public ResponseEntity<Void> updateTeam(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("teamname") String teamname,
			@RequestBody SwaggerTeam swaggerTeam) throws Exception;

	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@ApiOperation(value = "get Team", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Team", response = SwaggerTeam.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected team name - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/teams/{teamname}")
	public ResponseEntity<SwaggerTeam> getTeam(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("teamname") String teamname)
			throws Exception;

	/**
	 * USing this we can delete the team.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param teamname
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyRole('ADMIN','PROJECT-ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Delete Team", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected team name - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/teams/{teamname}")
	public ResponseEntity<Void> deleteTeam(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("teamname") String teamname)
			throws Exception;

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
	@PreAuthorize("hasAnyRole('ADMIN','PROJECT-ADMIN','SITE-ADMIN','OPERATION') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Assoiate Team", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/{swaggername}/associate-team")
	public ResponseEntity<Void> assoiateTeam(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@RequestBody SwaggerVO swaggerVO) throws Exception;

	/**
	 * To get the assoiated teams.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get Assoiated Teams", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swaggername}/associated-teams")
	public ResponseEntity<?> getAssoiatedTeams(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername)
			throws Exception;

	/**
	 * To get the team permissions.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get Team Permissions", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = SwaggerTeamPermissionVO.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/teams/permission")
	public ResponseEntity<Object> getTeamPermissions(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	/**
	 * To get the all the teams.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get Teams", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = SwaggerTeam.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/teams")
	public ResponseEntity<TeamsHistoryResponse> getTeams(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "name", required = false) String name) throws Exception;

	/**
	 * To get the only team names.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get Team Names", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/team-names")
	public ResponseEntity<List<String>> getTeamNames(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	/**
	 * @param interactionid
	 * @param jsessionid
	 * @param name
	 * @param limit
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/teams/search", produces = {"application/json"})
	public ResponseEntity<Object> teamSearch(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam("name") String name,
			@RequestParam("limit") int limit) throws ItorixException, JsonProcessingException;
}
