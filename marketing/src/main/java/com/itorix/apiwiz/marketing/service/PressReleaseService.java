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

    @UnSecure(ignoreValidation = true)
    @PostMapping(value = "/pressrelease")
    public ResponseEntity<?> createPressRelease(@RequestHeader(value = "x-apikey") String apikey,
            @RequestBody PressRelease pressRelease
    ) throws Exception;

    @UnSecure(ignoreValidation = true)
    @PatchMapping(value = "/pressrelease/{id}")
    public ResponseEntity<?> editPressRelease(@RequestHeader(value = "x-apikey") String apikey,
                                      @PathVariable("id") String releaseId,
                                      @RequestBody PressRelease pressRelease
    ) throws Exception;

    @UnSecure(ignoreValidation = true)
    @PatchMapping(value = "/pressrelease/{id}/status/{status}")
    public ResponseEntity<?> changeStatus(@RequestHeader(value = "x-apikey") String apikey,
            @PathVariable("id") String releaseId,
            @PathVariable("status") PressReleaseStatus status
    ) throws Exception;


    @UnSecure(ignoreValidation = true)
    @RequestMapping(method = RequestMethod.GET, value = "/pressrelease")
    public ResponseEntity<?> getPressReleaseData(@RequestHeader(value = "x-apikey") String apikey,
            @RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
            @RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize
    ) throws Exception;

    @UnSecure(ignoreValidation = true)
    @RequestMapping(method = RequestMethod.GET, value = "/pressrelease/{filterValue}")
    public ResponseEntity<?> getDataByFilter(@RequestHeader(value = "x-apikey") String apikey,
            @RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
            @RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "filter") String filter,
            @PathVariable(value = "filterValue") String filterValue
    ) throws Exception;

    @UnSecure(ignoreValidation = true)
    @DeleteMapping(value = "/pressrelease/{id}")
    public ResponseEntity<?> deletePressRelease(@RequestHeader(value = "x-apikey") String apikey,
            @PathVariable("id") String releaseId
    ) throws Exception;

}
