package com.itorix.apiwiz.test.service;

import com.itorix.apiwiz.test.executor.beans.TimeOut;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public interface ScenarioTimeOutService {
    @PostMapping(value = "/v1/scenario/timeout", consumes = { "application/json" }, produces = {
            "application/json" })
    public ResponseEntity<?> createTimeOut(@RequestHeader(value = "JSESSIONID") String jsessionid,@RequestBody TimeOut requestBody) throws Exception;

    @PutMapping(value = "v1/scenario/timeout/", consumes = { "application/json" }, produces = {
            "application/json" })
    public ResponseEntity<?> updateTimeOut(@RequestHeader(value = "JSESSIONID") String jsessionid,@RequestBody TimeOut requestBody) throws Exception;

    @DeleteMapping(value = "v1/scenario/timeout/", consumes = { "application/json" }, produces = {
            "application/json"})
    public ResponseEntity<?> deleteTimeOut(@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

    @GetMapping(value = "v1/scenario/timeout/", consumes = { "application/json" }, produces = {
            "application/json"})
    public TimeOut getTimeOut(@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;
}
