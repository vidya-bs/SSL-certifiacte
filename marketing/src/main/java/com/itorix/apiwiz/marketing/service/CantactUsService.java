package com.itorix.apiwiz.marketing.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.marketing.contactus.model.ContactUsNotification;
import com.itorix.apiwiz.marketing.contactus.model.NotificatoinEvent;


@CrossOrigin
@RestController
public interface CantactUsService {
	
	@UnSecure(ignoreValidation=true)
	@RequestMapping(method = RequestMethod.POST, value = "/v1/send-notification")
	public ResponseEntity<?> createJobPosting(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value="x-apikey")String apikey,
			@RequestBody ContactUsNotification contactUsNotification) throws Exception;

	@UnSecure(ignoreValidation=true)
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/notifications/config")
	public ResponseEntity<?> createNotificatonConfig(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value="x-apikey")String apikey,
			@RequestBody List<NotificatoinEvent> notificatoinEvents) throws Exception;
	
	@UnSecure(ignoreValidation=true)
	@RequestMapping(method = RequestMethod.GET, value = "/v1/notifications/config")
	public ResponseEntity<?> getNotificatonConfig(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value="x-apikey")String apikey) throws Exception;

}

	