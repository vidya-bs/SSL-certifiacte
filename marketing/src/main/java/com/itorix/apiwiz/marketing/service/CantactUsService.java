package com.itorix.apiwiz.marketing.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.marketing.contactus.model.ContactUsNotification;


@CrossOrigin
@RestController
public interface CantactUsService {
	
	@UnSecure(ignoreValidation=true)
	@RequestMapping(method = RequestMethod.POST, value = "/v1/send-notification")
	public ResponseEntity<?> createJobPosting(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value="x-apikey")String apikey,
			@RequestBody ContactUsNotification contactUsNotification) throws Exception;

}









	
	