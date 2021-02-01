package com.itorix.apiwiz.monitor.agent.executor.model;

import java.util.HashMap;
import java.util.Map;

public enum ErrorCodes {

	;
	public static final Map<String, String> errorMessage = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("MonitorAgent-1","you don't have enough permissions to access the resource");
			put("MonitorAgent-2","Login failed! Invalid or missing x-apikey.");
			put("MonitorAgent-4","Couldn't find workspace id");
			put("MonitorAgent-5","collectionId or schedulerId is missing");
			put("MonitorAgent-6","A request is already processed for executionId");
			put("MonitorAgent-7","mandatory parameter missing");
			put("MonitorAgent-8","There is no entry found for collectionId or schedulerId");
			put("MonitorAgent-9","workspace_id is missing.");
		}
	};

	public static final Map<String, Integer> responseCode = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 1L;
		{
			put("MonitorAgent-1",403);
			put("MonitorAgent-2",400);
			put("MonitorAgent-4",500);
			put("MonitorAgent-5",400);
			put("MonitorAgent-7",400);
			put("MonitorAgent-8",500);
			put("MonitorAgent-9",500);}
	};
	private String message;

	public String message() {
		return message;
	}

	ErrorCodes(String message) {
		this.message = message;
	}

}