package com.itorix.apiwiz.monitor.agent.service;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public interface MonitorAgentService {

    @RequestMapping(method = RequestMethod.POST, value = "/v1/execute", consumes = { "application/json" }, produces = {
            "application/json" })
    public ResponseEntity<?> storeMonitorDetails(@RequestHeader HttpHeaders headers,
            @RequestBody Map<String, String> requestBody) throws Exception;
}
