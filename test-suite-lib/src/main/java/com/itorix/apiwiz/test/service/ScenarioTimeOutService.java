package com.itorix.apiwiz.test.service;

import com.itorix.apiwiz.test.executor.beans.TimeOut;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public interface ScenarioTimeOutService {
    @PostMapping(value = "/v1/execute/timeout", consumes = { "application/json" }, produces = {
            "application/json" })
    public ResponseEntity<?> createTimeOut(@RequestBody TimeOut requestBody) throws Exception;

    @PutMapping(value = "/v1/execute/timeout", consumes = { "application/json" }, produces = {
            "application/json" })
    public ResponseEntity<?> updateTimeOut(@RequestBody TimeOut requestBody) throws Exception;

    @DeleteMapping(value = "/v1/execute/timeout", consumes = { "application/json" }, produces = {
            "application/json"})
    public ResponseEntity<?> deleteTimeOut(@RequestBody TimeOut requestBody) throws Exception;

    @GetMapping(value = "/v1/execute/timeout/{tenant}", consumes = { "application/json" }, produces = {
            "application/json"})
    public TimeOut getTimeOut(@PathVariable String tenant) throws Exception;
}
