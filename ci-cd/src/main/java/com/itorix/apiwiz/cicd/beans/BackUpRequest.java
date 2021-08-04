package com.itorix.apiwiz.cicd.beans;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ci-cd.backup")
public class BackUpRequest {

	private String interval;

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

	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		this.interval = interval;
	}
}
