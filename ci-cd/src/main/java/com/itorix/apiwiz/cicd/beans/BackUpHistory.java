package com.itorix.apiwiz.cicd.beans;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ci-cd.history")
public class BackUpHistory {

	private String path;
	
	private String time;
	
	
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	
	
}
