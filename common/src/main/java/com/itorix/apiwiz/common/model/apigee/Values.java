package com.itorix.apiwiz.common.model.apigee;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Values {

	private String name;
	private String value;
	private String delta;

	/*
	 * private String timestamp=null;
	 * 
	 * 
	 * public String getTimestamp() { return timestamp; } public void
	 * setTimestamp(String timestamp) { this.timestamp = timestamp; }
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDelta() {
		return delta;
	}

	public void setDelta(String delta) {
		this.delta = delta;
	}
}
