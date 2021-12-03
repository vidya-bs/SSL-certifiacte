package com.itorix.consentserver.service;

import com.itorix.consentserver.common.model.ItorixException;
import com.itorix.consentserver.common.model.Consent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public interface ConsentService {

    @RequestMapping(method = RequestMethod.GET, value = "/v1/consents/scopes/category")
    public ResponseEntity<?> getScopeCategories() throws ItorixException, ItorixException;

    @RequestMapping(method = RequestMethod.GET, value = "/v1/consents/scopes/category/names")
    public ResponseEntity<?> getScopeCategoryNames() throws ItorixException, ItorixException;

    @RequestMapping(method = RequestMethod.GET, value = "/v1/consents/scopes")
    public ResponseEntity<?> getScopeCategoryByName(@RequestParam(value = "category", required = true) String categoryName) throws ItorixException;


    @RequestMapping(method = RequestMethod.GET, value = "/v1/consents/data")
    public ResponseEntity<?> getScopeCategoryColumns() throws ItorixException;

    // User Consents APIs
    @RequestMapping(method = RequestMethod.POST, value = "/v1/consents")
    public ResponseEntity<?> createConsent(@RequestBody Consent consent) throws ItorixException;


    @RequestMapping(method = RequestMethod.GET, value = "/v1/consents/{userId}")
    public ResponseEntity<?> getConsentByPrimaryKey(@PathVariable(value = "userId", required = true) String userId) throws ItorixException;

    @RequestMapping(method = RequestMethod.PATCH, value = "/v1/consents/{userId}")
    public ResponseEntity<?> revokeConsent(@PathVariable(value = "userId", required = true) String userId) throws ItorixException;

    @RequestMapping(method = RequestMethod.GET, value = "/v1/consents/status/{userId}")
    public ResponseEntity<?> getConsentStatus(@PathVariable(value = "userId", required = true) String userId) throws ItorixException;

}
