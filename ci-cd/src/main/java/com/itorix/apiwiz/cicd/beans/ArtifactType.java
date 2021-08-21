package com.itorix.apiwiz.cicd.beans;

public enum ArtifactType {
	SOAPUI("SOAPUI"), Postman("Postman"), Testsuite("Testsuite");

	private String value;

	public String getResponse() {
		return value;
	}

	ArtifactType(String value) {
		this.value = value;
	}
}
