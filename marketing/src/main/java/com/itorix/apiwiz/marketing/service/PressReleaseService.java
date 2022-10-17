package com.itorix.apiwiz.marketing.service;

import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.marketing.pressrelease.model.PressRelease;
import com.itorix.apiwiz.marketing.pressrelease.model.PressReleaseStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/v1/marketing")
public interface PressReleaseService {

    @PostMapping(value = "/pressrelease")
    public ResponseEntity<?> createPressRelease(
            @RequestHeader(value = "interactionid") String interactionid,
            @RequestHeader(value = "JSESSIONID") String jsessionid,
            @RequestBody PressRelease pressRelease
    ) throws Exception;

    @PatchMapping(value = "/pressrelease/{id}")
    public ResponseEntity<?> editPressRelease(@RequestHeader(value = "interactionid") String interactionid,
                                      @RequestHeader(value = "JSESSIONID") String jsessionid,
                                      @PathVariable("id") String releaseId,
                                      @RequestBody PressRelease pressRelease
    ) throws Exception;

    @PatchMapping(value = "/pressrelease/{id}/status/{status}")
    public ResponseEntity<?> changeStatus(
            @RequestHeader(value = "interactionid") String interactionid,
            @RequestHeader(value = "JSESSIONID") String jsessionid,
            @PathVariable("id") String releaseId,
            @PathVariable("status") PressReleaseStatus status
    ) throws Exception;


    @UnSecure(ignoreValidation = true)
    @RequestMapping(method = RequestMethod.GET, value = "/pressrelease")
    public ResponseEntity<?> getPressReleaseData(
            @RequestHeader(value = "interactionid", required = false) String interactionid,
            @RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
            @RequestHeader(value = "x-apikey",required = false) String apikey,
            @RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
            @RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize
    ) throws Exception;

    @UnSecure(ignoreValidation = true)
    @RequestMapping(method = RequestMethod.GET, value = "/pressrelease/{filterValue}")
    public ResponseEntity<?> getDataByFilter(
            @RequestHeader(value = "interactionid", required = false) String interactionid,
            @RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
            @RequestHeader(value = "x-apikey",required = false) String apikey,
            @RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
            @RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "filter") String filter,
            @PathVariable(value = "filterValue") String filterValue
    ) throws Exception;

    @DeleteMapping(value = "/pressrelease/{id}")
    public ResponseEntity<?> deletePressRelease(
            @RequestHeader(value = "interactionid") String interactionid,
            @RequestHeader(value = "JSESSIONID") String jsessionid,
            @PathVariable("id") String releaseId
    ) throws Exception;

}
