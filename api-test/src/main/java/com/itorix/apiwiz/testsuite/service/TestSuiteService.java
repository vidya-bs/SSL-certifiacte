package com.itorix.apiwiz.testsuite.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.parser.ParseException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.testsuite.model.MaskFields;
import com.itorix.apiwiz.testsuite.model.Scenario;
import com.itorix.apiwiz.testsuite.model.TestCase;
import com.itorix.apiwiz.testsuite.model.TestSuite;
import com.itorix.apiwiz.testsuite.model.TestSuiteResponse;
import com.itorix.apiwiz.testsuite.model.TestSuiteSchedule;
import com.itorix.apiwiz.testsuite.model.Variables;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.sf.json.JSONException;

@CrossOrigin
@RestController
public interface TestSuiteService {

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/testsuites/metadata", consumes = {
	"application/json" }, produces = { "application/json" })
	public ResponseEntity<?> createMetaData(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody String metadata, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			HttpServletResponse response) throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/testsuites/metadata", produces = { "application/json" })
	public ResponseEntity<?> getMetaData(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/testsuites", consumes = {
	"application/json" }, produces = { "application/json" })
	public ResponseEntity<?> createTestSuite(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, @RequestBody TestSuite testSuite, HttpServletResponse response)
					throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/testsuites/{testsuiteid}/scenario", consumes = {
	"application/json" }, produces = { "application/json" })
	public ResponseEntity<?> createScenario(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody Scenario scenario, @PathVariable("testsuiteid") String testsuiteid, HttpServletRequest request,
			HttpServletResponse response) throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/testsuites/{testsuiteid}/scenarios/{scenarioid}/testcase", consumes = {
	"application/json" }, produces = { "application/json" })
	public ResponseEntity<?> createTestCase(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, @RequestBody TestCase testCase, @PathVariable("testsuiteid") String testsuiteid,
			@PathVariable("scenarioid") String scenarioid, HttpServletResponse response)
					throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/testsuites/{testsuiteid}/variables", produces = {
	"application/json" })
	public ResponseEntity<?> getTestSuiteVariables(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response, @PathVariable("testsuiteid") String testsuiteid)
					throws ItorixException;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/testsuites/{testsuiteid}", produces = {
	"application/json" })
	public ResponseEntity<?> getTestSuite(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response, @PathVariable("testsuiteid") String testsuiteid)
					throws ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/testsuites/{testsuiteid}", consumes = {
	"application/json" }, produces = { "application/json" })
	public ResponseEntity<?> updateTestSuite(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, @RequestBody TestSuite testSuite, HttpServletResponse response,
			@PathVariable("testsuiteid") String testsuiteid) throws ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/testsuites/{testsuiteid}/scenarios/{scenarioid}/testcase/{testcaseid}", consumes = {
	"application/json" }, produces = { "application/json" })
	public ResponseEntity<?> updateTestCase(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, @RequestBody TestCase testCase, HttpServletResponse response,
			@PathVariable("testsuiteid") String testsuiteid, @PathVariable("scenarioid") String scenarioid,
			@PathVariable("testcaseid") String testcaseid) throws ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/testsuites/{testsuiteid}", produces = {
	"application/json" })
	public ResponseEntity<?> deleteTestSuite(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response, @PathVariable("testsuiteid") String testsuiteid)
					throws ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/testsuites/{testsuiteid}/scenarios/{scenarioid}", produces = {
	"application/json" })
	public ResponseEntity<?> deleteScenario(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response, @PathVariable("testsuiteid") String testsuiteid,
			@PathVariable("scenarioid") String scenarioid) throws ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/testsuites/{testsuiteid}/scenarios/{scenarioid}/testcase/{testcaseid}", produces = {
	"application/json" })
	public ResponseEntity<?> deleteTestCase(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response, @PathVariable("testsuiteid") String testsuiteid,
			@PathVariable("scenarioid") String scenarioid, @PathVariable("testcaseid") String testcaseid)
					throws ItorixException;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/testsuites", produces = { "application/json" })
	public ResponseEntity<Object> getAllTestSuiteDetails(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestParam(value = "expand", required = false) String expand,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/testsuites/list", produces = { "application/json" })
	public ResponseEntity<Object> getAllTestSuiteList(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/testsuites/variables", consumes = {
	"application/json" }, produces = { "application/json" })
	public ResponseEntity<?> createVariables(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestBody Variables variables,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			HttpServletResponse response) throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/testsuites/variables/{id}", produces = {
	"application/json" })
	public ResponseEntity<?> getVariables(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response)
					throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/testsuites/variables/{id}", consumes = {
	"application/json" }, produces = { "application/json" })
	public ResponseEntity<?> updateVariables(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestBody Variables variables,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("id") String id,
			HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/testsuites/variables/{id}", produces = {
	"application/json" })
	public ResponseEntity<?> deleteVariables(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id)
					throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/testsuites/variables", produces = { "application/json" })
	public ResponseEntity<?> getAllVariables(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "expand", required = false, defaultValue = "false") String expand,
			HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/testsuites/{testsuiteid}/response", consumes = {
	"application/json" }, produces = { "application/json" })
	public ResponseEntity<?> saveTestSuiteResponse(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, @RequestBody TestSuiteResponse testSuiteResponse, HttpServletResponse response)
					throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/testsuites/{testsuiteid}/executions", produces = {
	"application/json" })
	public ResponseEntity<?> getTestSuiteResponseHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, @PathVariable("testsuiteid") String testsuiteid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			HttpServletResponse response)
					throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/testsuites/executions/{testsuiteresponseid}", produces = {
	"application/json" })
	public ResponseEntity<?> getTestSuiteResponseById(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, @PathVariable("testsuiteresponseid") String testsuiteresponseid,
			HttpServletResponse response) throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@ApiOperation(value = "Trigger TestSuite", notes = "", code = 202)
	@ApiResponses(value = { @ApiResponse(code = 202, message = "Accepted", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.POST, value = "/v1/testsuites/{testsuiteId}/{variableId}/run", produces = {
	"application/json" })
	public ResponseEntity<?> triggerTestSuite(@PathVariable("testsuiteId") String testSuiteId,
			@PathVariable("variableId") String variableId, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request)
					throws JsonProcessingException, JSONException, InterruptedException,ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@ApiOperation(value = "Cancel TestSuite", notes = "", code = 202)
	@ApiResponses(value = { @ApiResponse(code = 202, message = "Accepted", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.POST, value = "/v1/testsuites/{testsuiteId}/{variableId}/cancel", produces = {
	"application/json" })
	public ResponseEntity<?> cancelTestSuite(@PathVariable("testsuiteId") String testSuiteId,
			@PathVariable("variableId") String variableId, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@ApiOperation(value = "Get Testsuite Status", notes = "", code = 200)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "No Content", response = String.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/testsuites/executions/{executionid}/status")
	public ResponseEntity<?> getExecutionStatus(@PathVariable("executionid") String executionId,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request);

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@ApiOperation(value = "Get Testsuite Status", notes = "", code = 200)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "No Content", response = String.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/testsuites/{testsuiteid}/status")
	public ResponseEntity<?> getTestSuiteStatus(@PathVariable("testsuiteid") String testsuiteid,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request);

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@ApiOperation(value = "Get Testsuite History", notes = "", code = 200)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "ok", response = String.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/testsuites/{testsuiteId}/environments/{variableId}/executions", produces = {
	"application/json" })
	public ResponseEntity<?> getTestSuiteHistoryWithTestSuiteAndConfig(@PathVariable("testsuiteId") String testSuiteId,
			@PathVariable("variableId") String variableId,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "user", required = false) String user,
			@RequestParam(value = "range", required = false) String range,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request)
					throws ParseException, java.text.ParseException;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@ApiOperation(value = "Pause Testsuite", notes = "", code = 200)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "ok", response = String.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/testsuites/{testsuiteId}/pause", produces = {
	"application/json" })
	public ResponseEntity<?> pauseTestSuite(@PathVariable("testsuiteId") String testSuiteId,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request)
					throws ParseException;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@ApiOperation(value = "unPause Testsuite", notes = "", code = 200)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "ok", response = String.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/testsuites/{testsuiteId}/unpause", produces = {
	"application/json" })
	public ResponseEntity<?> unpauseTestSuite(@PathVariable("testsuiteId") String testSuiteId,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request)
					throws ParseException;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@ApiOperation(value = "Get Run Time Logs", notes = "", code = 200)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "No Content", response = String.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class) })
	@RequestMapping(method = RequestMethod.GET, value = "/v1/testsuites/{testsuiteExecutionId}/logs", produces = {
	"text/plain" })
	public ResponseEntity<?> getRunTimeLogs(@PathVariable("testsuiteExecutionId") String testsuiteExecutionId,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid) throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/testsuites/schedule", consumes = {
	"application/json" }, produces = { "application/json" })
	public ResponseEntity<?> createSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody TestSuiteSchedule schedule, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			HttpServletResponse response) throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/testsuites/schedule", consumes = {
	"application/json" }, produces = { "application/json" })
	public ResponseEntity<?> updateSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody TestSuiteSchedule schedule, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			HttpServletResponse response) throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/testsuites/schedule/{testSuiteId}/{configId}", produces = {
	"application/json" })
	public ResponseEntity<?> deleteSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable(value = "testSuiteId") String testSuiteId, @PathVariable(value = "configId") String configId,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/testsuites/schedule", produces = { "application/json" })
	public ResponseEntity<?> getSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/testsuites/{testSuiteId}/schedule", produces = {
	"application/json" })
	public ResponseEntity<?> getSchedule(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(value = "testSuiteId") String testSuiteId, HttpServletRequest request,
			HttpServletResponse response) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/testsuites/{testSuiteId}/configs/{configId}/analyze", produces = {
	"application/json" })
	public ResponseEntity<?> getAnalysis(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(value = "testSuiteId") String testSuiteId, @PathVariable(value = "configId") String configId,
			@RequestParam(value = "daterange", required = false) String daterange, HttpServletRequest request,
			HttpServletResponse response) throws ItorixException, java.text.ParseException;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/testsuites/dashboard", produces = { "application/json" })
	public ResponseEntity<?> getDashboardInfo(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "daterange", required = false) String daterange,
			@RequestParam(value = "timeunit", required = false) String timeunit, HttpServletRequest request,
			HttpServletResponse response) throws ItorixException, java.text.ParseException;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/testsuites/search")
	public ResponseEntity<Object> searchForTestSuite(
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "name") String name, @RequestParam(value = "limit") int limit) throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/testsuites/maskFields", consumes = {
	"application/json" }, produces = { "application/json" })
	public ResponseEntity<?> createOrUpdateMaskingFields(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody MaskFields requestBody, @RequestHeader(value = "JSESSIONID") String jsessionid)
					throws JsonProcessingException, ItorixException;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/testsuites/maskFields")
	public ResponseEntity<?> getMaskingFields(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid);

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/testsuites/certificates/{name}")
	public ResponseEntity<?> getCertificate(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,@PathVariable(name = "name") String name) throws ItorixException;



	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/testsuites/certificates", produces = { "application/json" })
	public ResponseEntity<?> getCertificates(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestParam(value = "names", required = false) String expand,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			HttpServletResponse response) throws ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/testsuites/certificates/{name}")
	public ResponseEntity<?> deleteCertificate(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,@PathVariable(value = "name") String name) throws ItorixException;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = {RequestMethod.POST}  , value = "/v1/testsuites/certificates", consumes = {	"multipart/form-data" })
	public ResponseEntity<Object> createOrUpdateCertificate(
			@RequestPart(value = "name", required = true) String name,
			@RequestPart(value = "jksFile", required = false) MultipartFile jksFile,
			@RequestPart(value = "description", required = false) String description,
			@RequestPart(value = "password", required = false) String password,
			@RequestPart(value = "alias", required = false) String alias,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'TEST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/testsuites/certificates/{name}/download")
	public ResponseEntity<Resource> downloadCertificate(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,@PathVariable(name = "name") String name) throws ItorixException;

}
