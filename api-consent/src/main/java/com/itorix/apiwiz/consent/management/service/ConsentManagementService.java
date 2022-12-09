package com.itorix.apiwiz.consent.management.service;

import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.consent.management.model.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin
@RestController
public interface ConsentManagementService {

	@ApiOperation(value = "Create Scope Category", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Accepted"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/consents/scopes/category")
	public ResponseEntity<?> createScopeCategory(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody ScopeCategory scopeCategory) throws ItorixException;

	@ApiOperation(value = "Update Scope Category", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Accepted"),
			@ApiResponse(code = 400, message = "Resource not found. No records found for selected Category name - %s.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/consents/scopes/category")
	public ResponseEntity<?> updateScopeCategory(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody ScopeCategory scopeCategory) throws ItorixException;

	@ApiOperation(value = "Delete Scope Category", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/consents/scopes/category/{name}")
	public ResponseEntity<?> deleteScopeCategory(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable String name) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get Scope Categories")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = ScopeCategoryResponse.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/consents/scopes/category")
	public ResponseEntity<?> getScopeCategories(@RequestParam Map<String, String> searchParams) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get Scope Categories")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = ScopeCategory.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/consents/scopes")
	public ResponseEntity<?> getScopeCategoryByName(
			@RequestParam(value = "category", required = true) String categoryName) throws ItorixException;

	@ApiOperation(value = "Create or Update Scope Category Columns")
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Accepted"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/consents/data")
	public ResponseEntity<?> createOrUpdateScopeCategoryColumns(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody ScopeCategoryColumns registryColumns) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get Scope Category Columns")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = ScopeCategoryColumns.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/consents/data")
	public ResponseEntity<?> getScopeCategoryColumns(@RequestHeader(value = "JSESSIONID") String jsessionid)
			throws ItorixException;

	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get Consents")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = ConsentResponse.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/consents")
	public ResponseEntity<?> getConsentsOverview(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam Map<String, String> searchParams) throws ItorixException;

	@ApiOperation(value = "Generate Consent API Key Pairs")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = String.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/consents/generate-token")
	public ResponseEntity<?> generateKeyPairs(@RequestHeader(value = "JSESSIONID") String jsessionid)
			throws ItorixException;

	@ApiOperation(value = "Update Consent Trigger")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/consents/update-consent-trigger")
	public ResponseEntity<?> updateTrigger(@RequestHeader(value = "JSESSIONID") String jsessionid)
			throws ItorixException;

	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get Consent API Token")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/consents/token")
	public ResponseEntity<?> getToken(@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException;

	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@ApiOperation(value = "Get Scope Category Names")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = String.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/consents/scopes/category/names")
	public ResponseEntity<?> getScopeCategoryNames() throws ItorixException, ItorixException;

	@ApiOperation(value = "Generate Excel Report of Consents")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = ConsentAuditExportResponse.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/consents/export", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> generateExcelReport(
			@RequestHeader(value = "interactionid", required = false) String interactionId,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam(value = "timeRange") String timeRange)
			throws Exception;

}
