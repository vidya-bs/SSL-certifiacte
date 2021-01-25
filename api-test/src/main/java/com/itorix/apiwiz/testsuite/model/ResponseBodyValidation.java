package com.itorix.apiwiz.testsuite.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseBodyValidation {

	@JsonProperty("path")
	private String path;

	@JsonProperty("value")
	private Object value;

	@JsonProperty("condition")
	private String condition;
		
	@JsonProperty("status")
	private String status = "Did Not Execute";

	@JsonProperty("errorMessage")
	private String message;

	@JsonProperty("continueOnError")
	private boolean continueOnError;
	
	@JsonProperty("continueOnError")
	public boolean isContinueOnError() {
		return continueOnError;
	}

	@JsonProperty("continueOnError")
	public void setContinueOnError(boolean continueOnError) {
		this.continueOnError = continueOnError;
	}

	@JsonProperty("status")
	public String getStatus() {
		return status;
	}

	@JsonProperty("status")
	public void setStatus(String status) {
		this.status = status;
	}

	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("path")
	public String getPath() {
		return path;
	}

	@JsonProperty("path")
	public void setPath(String path) {
		this.path = path;
	}

	@JsonProperty("value")
	public Object getValue() {
		return value;
	}

	@JsonProperty("value")
	public void setValue(Object value) {
		this.value = value;
	}

	@JsonProperty("condition")
	public String getCondition() {
		return condition;
	}

	@JsonProperty("condition")
	public void setCondition(String condition) {
		this.condition = condition;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}