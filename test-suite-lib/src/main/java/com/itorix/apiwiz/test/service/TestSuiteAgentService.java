package com.itorix.apiwiz.test.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public interface TestSuiteAgentService {

	@RequestMapping(method = RequestMethod.POST, value = "/v1/execute", consumes = { "application/json" }, produces = {
			"application/json" })
	public ResponseEntity<?> storeExecutionId(@RequestBody Map<String, String> requestBody) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/cancelExecution", consumes = { "application/json" }, produces = {
			"application/json" })
	public ResponseEntity<?> cancelExecution(@RequestBody Map<String, String> requestBody) throws Exception;
}
