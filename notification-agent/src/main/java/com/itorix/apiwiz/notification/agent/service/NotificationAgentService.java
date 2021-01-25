package com.itorix.apiwiz.notification.agent.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.notification.agent.model.RequestModel;

@CrossOrigin
@RestController
public interface NotificationAgentService {

	@RequestMapping(method = RequestMethod.POST, value = "/v1/notification", consumes = { "application/json" }, produces = {
			"application/json" })
	public ResponseEntity<?> createNotification(@RequestHeader HttpHeaders headers , @RequestBody RequestModel model) throws Exception;
}
