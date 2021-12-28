package com.itorix.apiwiz.consent.management.service;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.consent.management.model.ScopeCategory;
import com.itorix.apiwiz.consent.management.model.ScopeCategoryColumns;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin
@RestController
public interface ConsentManagementService {

	@PreAuthorize("hasAnyRole('ADMIN') and hasAnyAuthority('ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/consents/scopes/category")
	public ResponseEntity<?> createScopeCategory(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody ScopeCategory scopeCategory) throws ItorixException;

	@PreAuthorize("hasAnyRole('ADMIN') and hasAnyAuthority('ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/consents/scopes/category")
	public ResponseEntity<?> updateScopeCategory(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody ScopeCategory scopeCategory) throws ItorixException;

	@PreAuthorize("hasAnyRole('ADMIN') and hasAnyAuthority('ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/consents/scopes/category/{name}")
	public ResponseEntity<?> deleteScopeCategory(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable String name) throws ItorixException;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/consents/scopes/category")
	public ResponseEntity<?> getScopeCategories(@RequestParam Map<String,String> searchParams) throws ItorixException, ItorixException;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/consents/scopes")
	public ResponseEntity<?> getScopeCategoryByName(@RequestParam(value = "category", required = true) String categoryName) throws ItorixException;

	@PreAuthorize("hasAnyRole('ADMIN') and hasAnyAuthority('ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/consents/data")
	public ResponseEntity<?> createOrUpdateScopeCategoryColumns(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody ScopeCategoryColumns registryColumns) throws ItorixException;


	@RequestMapping(method = RequestMethod.GET, value = "/v1/consents/data")
	public ResponseEntity<?> getScopeCategoryColumns(@RequestHeader(value = "JSESSIONID") String jsessionid)
			throws ItorixException;


	@RequestMapping(method = RequestMethod.GET, value = "/v1/consents")
	public ResponseEntity<?> getConsentsOverview(@RequestHeader(value = "JSESSIONID") String jsessionid,
												 @RequestParam Map<String,String> searchParams) throws ItorixException;


	@PreAuthorize("hasAnyRole('ADMIN') and hasAnyAuthority('ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/consents/generate-token")
	public ResponseEntity<?> generateKeyPairs(@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException;

	@PreAuthorize("hasAnyRole('ADMIN') and hasAnyAuthority('ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PATCH, value = "/v1/consents/update-consent-trigger")
	public ResponseEntity<?> updateTrigger(@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/consents/token")
	public ResponseEntity<?> getToken(@RequestHeader(value = "JSESSIONID") String jsessionid)
			throws ItorixException;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/consents/scopes/category/names")
	public ResponseEntity<?> getScopeCategoryNames() throws ItorixException, ItorixException;


	@PreAuthorize("hasAnyRole('ADMIN') and hasAnyAuthority('ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/consents/export", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> generateExcelReport(
			@RequestHeader(value = "interactionid", required = false) String interactionId,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam(value = "timeRange") String timeRange) throws Exception;

}
