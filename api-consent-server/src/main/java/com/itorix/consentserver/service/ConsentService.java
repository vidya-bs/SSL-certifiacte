package com.itorix.consentserver.service;

import com.itorix.consentserver.model.Consent;
import com.itorix.consentserver.model.ItorixException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
public interface ConsentService {


    @PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
    @RequestMapping(method = RequestMethod.GET, value = "/v1/consents/scopes/category/names")
    public ResponseEntity<?> getScopeCategoryNames() throws ItorixException, ItorixException;

    @PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
    @RequestMapping(method = RequestMethod.GET, value = "/v1/consents/scopes")
    public ResponseEntity<?> getScopeCategoryByName(@RequestParam(value = "category", required = true) String categoryName) throws ItorixException;


    // User Consents APIs
    @RequestMapping(method = RequestMethod.POST, value = "/v1/consents")
    public ResponseEntity<?> createConsent(@RequestBody Consent consent) throws ItorixException;

    @PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
    @RequestMapping(method = RequestMethod.GET, value = "/v1/consents")
    public ResponseEntity<?> getConsents(@RequestParam Map<String,String> searchParams)throws ItorixException;


    @PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
    @RequestMapping(method = RequestMethod.GET, value = "/v1/consents/{consentId}")
    public ResponseEntity<?> getConsentById(@PathVariable(value = "consentId", required = true) String consentId) throws ItorixException;

    @RequestMapping(method = RequestMethod.PATCH, value = "/v1/consents/{consentId}/status/revoke")
    public ResponseEntity<?> revokeConsent(@PathVariable(value = "consentId", required = true) String consentId) throws ItorixException;

    @PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
    @RequestMapping(method = RequestMethod.GET, value = "/v1/consents/{consentId}/status")
    public ResponseEntity<?> getConsentStatus(@PathVariable(value = "consentId", required = true) String consentId) throws ItorixException;

    @RequestMapping(method = RequestMethod.PUT, value = "/v1/consents/{consentId}/scopes")
    public ResponseEntity<?> updateConsentScope(@PathVariable(value = "consentId", required = true) String consentId, @RequestBody List<String> scopes) throws ItorixException;

    @RequestMapping(method = RequestMethod.PATCH, value = "/v1/consents/expire")
    public ResponseEntity<?> expireConsents(@RequestHeader(value = "x-tenant", required = true) String tenantKey, @RequestHeader(value="x-consent-apikey", required = true) String apiKey) throws ItorixException;

}
