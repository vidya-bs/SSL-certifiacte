package com.itorix.apiwiz.test.service;

import com.itorix.apiwiz.test.executor.beans.ScenarioTimeOut;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public interface ScenarioTimeOutService {
    @PostMapping(value = "/v1/scenario/timeout/{testsuiteId}", consumes = { "application/json" }, produces = {
            "application/json" })
    public ResponseEntity<?> createTimeOut(@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody ScenarioTimeOut requestBody, @PathVariable String testsuiteId) throws Exception;

    @PatchMapping(value = "v1/scenario/timeout/{testsuiteId}", consumes = { "application/json" }, produces = {
            "application/json" })
    public ResponseEntity<?> updateTimeOut(@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody ScenarioTimeOut requestBody, @PathVariable String testsuiteId) throws Exception;

    @DeleteMapping(value = "v1/scenario/timeout/{testsuiteId}", consumes = { "application/json" }, produces = {
            "application/json"})
    public ResponseEntity<?> deleteTimeOut(@RequestHeader(value = "JSESSIONID") String jsessionid,@PathVariable String testsuiteId) throws Exception;

    @GetMapping(value = "v1/scenario/timeout/{testsuiteId}", consumes = { "application/json" }, produces = {
            "application/json"})
    public ScenarioTimeOut getTimeOut(@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable String testsuiteId) throws Exception;
}
