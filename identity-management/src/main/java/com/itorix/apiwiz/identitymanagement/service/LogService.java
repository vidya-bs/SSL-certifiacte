package com.itorix.apiwiz.identitymanagement.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.identitymanagement.model.errorlog.ErrorLog;
import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;

@CrossOrigin
@RestController
public interface LogService {

	@UnSecure
	@RequestMapping(method = RequestMethod.POST, value = "/v1/ui/error-logs", consumes = {"application/json"})
	public @ResponseBody ResponseEntity<Void> register(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "x-apikey") String apikey, @RequestBody ErrorLog errorLog)
			throws ItorixException, Exception;
}
