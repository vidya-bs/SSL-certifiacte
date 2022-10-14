package com.itorix.apiwiz.marketing.service;

import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.marketing.downloads.model.AppDownloadModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/v1/marketing")
public interface AppDownloadService {

    @UnSecure(ignoreValidation = true)
    @PostMapping(value = "/download")
    public ResponseEntity<?> postDownload(
            @RequestHeader(value = "interactionid", required = false) String interactionid,
            @RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
            @RequestHeader(value = "x-apikey",required = false) String apikey,
            @RequestBody AppDownloadModel appDownloadModel
            ) throws Exception;

    @UnSecure(ignoreValidation = true)
    @GetMapping(value = "/download")
    public ResponseEntity<?> getDownloads(
            @RequestHeader(value = "interactionid", required = false) String interactionid,
            @RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
            @RequestHeader(value = "x-apikey",required = false) String apikey
    ) throws Exception;
}
