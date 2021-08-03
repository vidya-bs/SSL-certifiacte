package com.itorix.apiwiz.validator.license.model;

import java.util.HashMap;
import java.util.Map;

public enum ErrorCodes {

	;
	public static final Map<String, String> errorMessage = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("License-1001","Request validation failed. License already exists for the email %s.");
			put("License-1002","Request validation failed. No license exists for the email %s.");
			put("License-1003","Request validation failed. Invalid API token.");
			put("License-1004","Login failed! Invalid or missing x-apikey.");
		}
	};

	public static final Map<String, Integer> responseCode = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 1L;
		{
			put("License-1001",400);
			put("License-1002",400);
			put("License-1003",400);
			put("License-1004", 400);
		}
	};
	private String message;

	public String message() {
		return message;
	}

	ErrorCodes(String message) {
		this.message = message;
	}

}