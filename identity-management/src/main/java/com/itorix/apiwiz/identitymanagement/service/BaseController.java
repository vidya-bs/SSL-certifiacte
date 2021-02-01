package com.itorix.apiwiz.identitymanagement.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

public class BaseController {

	@Autowired
	protected HttpServletRequest request;
	@Autowired
	protected HttpServletResponse response;

	public static final String SESSION_TOKEN_NAME = "JSESSIONID";
	
	public static final String API_KEY_NAME = "x-apikey";
	
	public static final String INTERACTION_ID="interactionId";

}