package com.itorix.apiwiz.common.model.projectmanagement;

public class TestSuiteAndConfig {
	private String testSuiteId;
	private String environmentId;

	@Override
	public String toString() {
		return "Testsuit [testSuiteId=" + testSuiteId + ", environmentId=" + environmentId + "]";
	}

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
}
