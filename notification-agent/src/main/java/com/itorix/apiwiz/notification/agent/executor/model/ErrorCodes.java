package com.itorix.apiwiz.notification.agent.executor.model;

import java.util.HashMap;
import java.util.Map;

public enum ErrorCodes {

	;
	public static final Map<String, String> errorMessage = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("Notification-Agent-1", "Mandatory parameter type or content is missing");
			put("Notification-Agent-2", "content is improper");
			put("Notification-Agent-4", "missing or incorrect x-apikey");
		}
	};

	public static final Map<String, Integer> responseCode = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 1L;
		{
			put("Notification-Agent-1", 400);
			put("Notification-Agent-2", 400);
			put("Notification-Agent-4", 403);
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