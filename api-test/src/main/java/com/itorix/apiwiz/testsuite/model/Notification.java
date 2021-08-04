package com.itorix.apiwiz.testsuite.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Notification {

	private String type;
	private List<String> notifieres;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getNotifieres() {
		return notifieres;
	}

	public void setNotifieres(List<String> notifieres) {
		this.notifieres = notifieres;
	}
}
