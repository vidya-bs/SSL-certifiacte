package com.itorix.apiwiz.virtualization.model.metadata;

public class ExpectationMetadata {
	private HttpRequest httpRequest;

	public HttpRequest getHttpRequest() {
		return httpRequest;
	}

	public void setHttpRequest(HttpRequest httpRequest) {
		this.httpRequest = httpRequest;
	}

	@Override
	public String toString() {
		return "ClassPojo [httpRequest = " + httpRequest + "]";
	}
}
