package com.itorix.apiwiz.test.executor.beans;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Execution {

	@JsonProperty("executionID")
	private String id;

	@JsonProperty("date")
	private String date;

	@JsonProperty("status")
	private String status;

	@JsonProperty("variables")
	private Map<String, String> vars;

	public Execution() {
	}

	public Execution(String id, String date, String status, Map<String, String> vars) {
		super();
		this.id = id;
		this.date = date;
		this.status = status;
		this.vars = vars;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Map<String, String> getVars() {
		return vars;
	}

	public void setVars(Map<String, String> vars) {
		this.vars = vars;
	}
}