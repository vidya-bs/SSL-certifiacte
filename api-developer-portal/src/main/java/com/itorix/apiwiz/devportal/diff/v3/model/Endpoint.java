package com.itorix.apiwiz.devportal.diff.v3.model;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;

public class Endpoint {

	private String pathUrl;
	private HttpMethod method;
	private String summary;

	private PathItem path;
	private Operation operation;

	public String getPathUrl() {
		return pathUrl;
	}

	public void setPathUrl(String pathUrl) {
		this.pathUrl = pathUrl;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod httpMethod) {
		this.method = httpMethod;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public PathItem getPath() {
		return path;
	}

	public void setPath(PathItem path) {
		this.path = path;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}
}
