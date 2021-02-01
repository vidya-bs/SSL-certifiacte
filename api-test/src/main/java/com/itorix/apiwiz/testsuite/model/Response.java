package com.itorix.apiwiz.testsuite.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

	@JsonProperty("description")
	private String description;

	@JsonProperty("statusCode")
	private int status;

	@JsonProperty("statusMessage")
	private String message;

	@JsonProperty("headers")
	private Map<String, String> headers;

	@JsonProperty("body")
	private Body body;

	@JsonProperty("variables")
	private List<Variable> variables;

	@JsonProperty("assertions")
	private ResponseAssertions assertions;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	public List<Variable> getVariables() {
		return variables;
	}

	public void setVariables(List<Variable> variables) {
		this.variables = variables;
	}

	public ResponseAssertions getAssertions() {
		return assertions;
	}

	public void setAssertions(ResponseAssertions assertions) {
		this.assertions = assertions;
	}

}