package com.itorix.apiwiz.consent.management.service;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.consent.management.model.Consent;
import com.itorix.apiwiz.consent.management.model.ScopeCategory;
import com.itorix.apiwiz.consent.management.model.ScopeCategoryColumns;
import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @RequestMapping(method = RequestMethod.GET, value = "/v1/consents/scopes/category")
    public ResponseEntity<?> getScopeCategory(@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException;

    @UnSecure
    @RequestMapping(method = RequestMethod.GET, value = "/v1/consents/scopes/category/{name}")
    public ResponseEntity<?> getScopeCategoryNames(@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable(value = "name") String name) throws ItorixException;

    @UnSecure
    @RequestMapping(method = RequestMethod.GET, value = "/v1/consents/scopes/category")
    public ResponseEntity<?> getScopeCategoryByName(@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam(value = "name", required = true) String name) throws ItorixException;


    @PreAuthorize("hasAnyRole('ADMIN') and hasAnyAuthority('ENTERPRISE')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/v1/consents/scopes/category/{name}")
    public ResponseEntity<?> deleteScopeCategory(@RequestHeader(value = "JSESSIONID") String jsessionid,
                                                 @PathVariable String name) throws ItorixException;


    @PreAuthorize("hasAnyRole('ADMIN') and hasAnyAuthority('ENTERPRISE')")
    @RequestMapping(method = RequestMethod.PUT, value = "/v1/consents/data")
    public ResponseEntity<?> createOrUpdateScopeCategoryColumns(@RequestHeader(value = "JSESSIONID") String jsessionid,
                                                          @RequestBody ScopeCategoryColumns registryColumns) throws ItorixException;

    @UnSecure
    @RequestMapping(method = RequestMethod.GET, value = "/v1/consents/data")
    public ResponseEntity<?> getScopeCategoryColumns(@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException;

    // User Consents APIs
    @UnSecure
    @RequestMapping(method = RequestMethod.POST, value = "/v1/consents")
    public ResponseEntity<?> createConsent(@RequestBody Consent consent) throws ItorixException;

    @PreAuthorize("hasAnyRole('ADMIN') and hasAnyAuthority('ENTERPRISE')")
    @RequestMapping(method = RequestMethod.GET, value = "/v1/consents")
    public ResponseEntity<?> getConsentsOverview(@RequestHeader(value = "JSESSIONID") String jsessionid,
                                                 @RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
                                                 @RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
                                                 @RequestParam(value = "status", required = false) String consentStatus,
                                                 @RequestParam(value = "category", required = false) String category) throws ItorixException;

    @UnSecure
    @RequestMapping(method = RequestMethod.GET, value = "/v1/consents/{userId}")
    public ResponseEntity<?> getConsentByPrimaryKey(@PathVariable(value = "userId", required = true) String userId) throws ItorixException;

    @UnSecure
    @RequestMapping(method = RequestMethod.PATCH, value = "/v1/consents/{userId}")
    public ResponseEntity<?> revokeConsent(@PathVariable(value = "userId", required = true) String userId) throws ItorixException;

    @UnSecure
    @RequestMapping(method = RequestMethod.GET, value = "/v1/consents/status/{userId}")
    public ResponseEntity<?> getConsentStatus(@PathVariable(value = "userId", required = true) String userId) throws ItorixException;

}
