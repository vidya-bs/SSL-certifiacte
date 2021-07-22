package com.itorix.apiwiz.identitymanagement.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.identitymanagement.logging.LokiLogger;
import com.itorix.apiwiz.identitymanagement.model.errorlog.ErrorLog;
import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.identitymanagement.service.LogService;

@CrossOrigin
@RestController
public class LogServiceImpl implements LogService {
	@Autowired
	private LokiLogger lokiLogger;

	@UnSecure
	@Override
	public ResponseEntity<Void> register(String interactionid, String apikey, ErrorLog errorLog)
			throws ItorixException, Exception {
		lokiLogger.postLog(errorLog);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
