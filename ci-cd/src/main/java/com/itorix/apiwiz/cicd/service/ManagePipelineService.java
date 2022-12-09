package com.itorix.apiwiz.cicd.service;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.parser.ParseException;
import org.springframework.core.io.Resource;
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

import com.itorix.apiwiz.cicd.beans.BackUpRequest;
import com.itorix.apiwiz.cicd.beans.Pipeline;
import com.itorix.apiwiz.cicd.beans.PipelineGroups;
import com.itorix.apiwiz.cicd.beans.PipelineNameValidation;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Interface to provide CI/CD functionalities
 *
 * @author vphani
 */
@CrossOrigin
@RestController
@Api(value = "CI-CD", tags = "CI-CD")
public interface ManagePipelineService {

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/pipelines/validate", produces = {"application/json"})
	public ResponseEntity<?> validatePipelineName(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PipelineNameValidation pipelineName);

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/pipelines/groups/{groupName}/validate", produces = {
			"application/json"})
	public ResponseEntity<?> validateGroup(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("groupName") String groupName);

	@ApiOperation(value = "Create Pipeline", notes = "", code = 201)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Created", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/pipelines", consumes = {"application/json"}, produces = {
			"application/json"})
	public ResponseEntity<?> createPipeline(@RequestBody PipelineGroups pipelineGroups,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwType, HttpServletRequest request);

	@ApiOperation(value = "Update Pipeline", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/pipelines", consumes = {"application/json"}, produces = {
			"application/json"})
	public ResponseEntity<?> updatePipeline(@RequestBody PipelineGroups pipelineGroups,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-gwtype", required = false) String gwType, HttpServletRequest request);

	@ApiOperation(value = "Delete Pipeline Group", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/pipelines/{pipelineGroupName}", produces = {
			"application/json"})
	public ResponseEntity<?> deletePipelineGroup(@PathVariable("pipelineGroupName") String groupName,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request);

	@ApiOperation(value = "Delete Pipeline", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/pipelines/{pipelineGroupName}/{pipelineName}", produces = {
			"application/json"})
	public ResponseEntity<?> deletePipeline(@PathVariable("pipelineGroupName") String groupName,
			@PathVariable("pipelineName") String name, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request);

	@ApiOperation(value = "Get All Pipelines", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "ok", response = PipelineGroups.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/pipelines", produces = {"application/json"})
	public ResponseEntity<?> getAllPipelines(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request)
			throws ItorixException;

	@ApiOperation(value = "Get Pipelines From Group", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "ok", response = PipelineGroups.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/pipelines/{pipelineGroupName}", produces = {
			"application/json"})
	public ResponseEntity<?> getPipelinesFromGroup(@PathVariable("pipelineGroupName") String groupName,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request);

	@ApiOperation(value = "Get Pipeline", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "ok", response = Pipeline.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/pipelines/{pipelineGroupName}/{pipelineName}", produces = {
			"application/json"})
	public ResponseEntity<?> getPipeline(@PathVariable("pipelineGroupName") String groupName,
			@PathVariable("pipelineName") String name, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request);

	@ApiOperation(value = "Get Pipeline History", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "ok", response = String.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/pipelines/{pipelineGroupName}/{pipelineName}/history", produces = {
			"application/json"})
	public ResponseEntity<?> getPipelineHistory(@PathVariable("pipelineGroupName") String groupName,
			@PathVariable("pipelineName") String name,
			@RequestParam(value = "offset", required = false, defaultValue = "0") String offset,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request)
			throws ParseException;

	@ApiOperation(value = "Get Build And Test Artifacts", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "ok", response = String.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/pipelines/{pipelineGroupName}/{pipelineName}/{pipelineCounter}/{stageName}/{stageCounter}/{jobName}.json", produces = {
			"application/json"})
	public ResponseEntity<?> getBuildAndTestArtifacts(@PathVariable("pipelineGroupName") String groupName,
			@PathVariable("pipelineName") String pipelineName, @PathVariable("pipelineCounter") String pipelineCounter,
			@PathVariable("stageName") String stageName, @PathVariable("stageCounter") String stageCounter,
			@PathVariable("jobName") String jobName, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request)
			throws ParseException;

	@ApiOperation(value = "Get Artifacts", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "ok", response = String.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/pipelines/{pipelineName}/{pipelineCounter}/{stageName}/{stageCounter}/BuildAndDeploy/cruise-output/console.log")
	@UnSecure
	public ResponseEntity<String> getConsoleLogs(@PathVariable("pipelineName") String pipelineName,
			@PathVariable("pipelineCounter") String pipelineCounter, @PathVariable("stageName") String stageName,
			@PathVariable("stageCounter") String stageCounter,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request);

	@ApiOperation(value = "Get Console Log", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "ok", response = String.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/pipelines/{pipelineName}/{pipelineCounter}/{stageName}/{stageCounter}/BuildAndDeploy/{artifactName:.+}")
	@UnSecure(ignoreValidation = true)
	public ResponseEntity<Resource> getArtifacts(@PathVariable("pipelineName") String pipelineName,
			@PathVariable("pipelineCounter") String pipelineCounter, @PathVariable("stageName") String stageName,
			@PathVariable("stageCounter") String stageCounter, @PathVariable("artifactName") String artifactName,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request)
			throws ParseException;

	@ApiOperation(value = "Trigger Pipeline", notes = "", code = 202)
	@ApiResponses(value = {@ApiResponse(code = 202, message = "Accepted", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/pipelines/{pipelineGroupName}/{pipelineName}/run", produces = {
			"application/json"})
	public ResponseEntity<?> triggerPipeline(@PathVariable("pipelineGroupName") String groupName,
			@PathVariable("pipelineName") String pipelineName, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request);

	@ApiOperation(value = "Cancel Pipeline", notes = "", code = 202)
	@ApiResponses(value = {@ApiResponse(code = 202, message = "Accepted", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/pipelines/{pipelineGroupName}/{pipelineName}/{stageName}/cancel", produces = {
			"application/json"})
	public ResponseEntity<?> cancelPipeline(@PathVariable("pipelineGroupName") String groupName,
			@PathVariable("pipelineName") String pipelineName, @PathVariable("stageName") String stageName,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request);

	@ApiOperation(value = "Trigger Stage", notes = "", code = 202)
	@ApiResponses(value = {@ApiResponse(code = 202, message = "Accepted", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/pipelines/{pipelineGroupName}/{pipelineName}/{counter}/{stageName}/run", produces = {
			"application/json"})
	public ResponseEntity<?> triggerStage(@PathVariable("pipelineGroupName") String groupName,
			@PathVariable("pipelineName") String pipelineName, @PathVariable("counter") String counter,
			@PathVariable("stageName") String stageName, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request);

	@ApiOperation(value = "Send Notifications", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Accepted", response = String.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/pipelines/notifications")
	public ResponseEntity<?> sendNotifications(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody String emailBody, @RequestParam("pipelineName") String pipelineName,
			@RequestParam(value = "projectName", required = false) String projectName,
			@RequestParam("subject") String subject) throws Exception;

	@ApiOperation(value = "Get Logs", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Accepted", response = String.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@UnSecure
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/pipelines/logs", produces = {"text/plain"})
	public ResponseEntity<?> getLogs(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam("url") String url) throws Exception;

	@ApiOperation(value = "Get Metrics For Project", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Accepted", response = Object.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@UnSecure
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/pipelines/{pipelineGroupName}/{pipelineName}/metrics", produces = {
			"application/json"})
	public ResponseEntity<?> getMetricsForProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam("topk") String topk, @PathVariable("pipelineGroupName") String pipelineGroupName,
			@PathVariable("pipelineName") String pipelineName) throws Exception;

	@ApiOperation(value = "Get Pipeline DashBoard", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Accepted", response = Object.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@UnSecure
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/pipelines/dashboard", produces = {"application/json"})
	public ResponseEntity<?> getPipelineDashBoard(
			@RequestHeader(value = "interactionid", required = false) String interactionid) throws Exception;

	@ApiOperation(value = "Pause Pipeline", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/pipelines/{pipelineGroupName}/{pipelineName}/pause", produces = {
			"application/json"})
	public ResponseEntity<?> pausePipeline(@PathVariable("pipelineGroupName") String groupName,
			@PathVariable("pipelineName") String pipelineName, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request);

	@ApiOperation(value = "Un Pause Pipeline", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/pipelines/{pipelineGroupName}/{pipelineName}/unpause", produces = {
			"application/json"})
	public ResponseEntity<?> unpausePipeline(@PathVariable("pipelineGroupName") String groupName,
			@PathVariable("pipelineName") String pipelineName, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request);

	@ApiOperation(value = "Get Pipeline Status", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "No Content", response = String.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/pipelines/{pipelineGroupName}/{pipelineName}/status", produces = {
			"application/json"})
	public ResponseEntity<?> getPipelineStatus(@PathVariable("pipelineGroupName") String groupName,
			@PathVariable("pipelineName") String pipelineName, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request)
			throws ParseException;

	@ApiOperation(value = "Get Run Time Logs", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "No Content", response = String.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/pipelines/{pipelineGroupName}/{pipelineName}/{pipelineCounter}/{stageName}/{stageCounter}/{jobName}/logs", produces = {
			"text/plain"})
	public ResponseEntity<?> getRunTimeLogs(@PathVariable("pipelineGroupName") String groupName,
			@PathVariable("pipelineName") String pipelineName, @PathVariable("pipelineCounter") String pipelineCounter,
			@PathVariable("stageName") String stageName, @PathVariable("stageCounter") String stageCounter,
			@PathVariable("jobName") String jobName, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/cicd/stats")
	public ResponseEntity<?> getcicdStats(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "timeunit", required = false) String timeunit,
			@RequestParam(value = "timerange", required = false) String timerange) throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/cicd/backup")
	public ResponseEntity<?> createcicdBackUp(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody BackUpRequest backUpRequest)
			throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','SITE-ADMIN') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/cicd/backup")
	public ResponseEntity<?> updatecicdBackUp(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody BackUpRequest backUpRequest)
			throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/cicd/backup/config")
	public ResponseEntity<?> getcicdBackUp(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/cicd/backups/history")
	public ResponseEntity<?> getcicdBackUpHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/cicd/health")
	public ResponseEntity<?> getGoCdHealth(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/release/stats")
	public ResponseEntity<?> getrelaseStats(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "timeunit", required = false) String timeunit,
			@RequestParam(value = "timerange", required = false) String timerange) throws Exception;
}
