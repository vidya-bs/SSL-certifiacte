package com.itorix.apiwiz.cicd.beans;

public class TestSuiteAndConfig {

	private String testSuiteId;

	private String environmentId;

	public String getTestSuiteId() {
		return testSuiteId;
	}

	public void setTestSuiteId(String testSuiteId) {
		this.testSuiteId = testSuiteId;
	}

	public String getEnvironmentId() {
		return environmentId;
	}

	public void setEnvironmentId(String environmentId) {
		this.environmentId = environmentId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TestSuiteAndConfig [testSuiteId=");
		builder.append(testSuiteId);
		builder.append(", environmentId=");
		builder.append(environmentId);
		builder.append("]");
		return builder.toString();
	}
}
