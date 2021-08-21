package com.itorix.apiwiz.common.model.projectmanagement;

import java.util.List;

public class UnitTests {
	private String enabled;
	private int acceptance;
	private String artifactType;
	private List<TestSuiteAndConfig> testsuites;

	public String getEnabled() {
		return enabled;
	}

	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}

	public int getAcceptance() {
		return acceptance;
	}

	public void setAcceptance(int acceptance) {
		this.acceptance = acceptance;
	}

	public String getArtifactType() {
		return artifactType;
	}

	public void setArtifactType(String artifactType) {
		this.artifactType = artifactType;
	}

	public List<TestSuiteAndConfig> getTestsuites() {
		return testsuites;
	}

	public void setTestsuites(List<TestSuiteAndConfig> testsuites) {
		this.testsuites = testsuites;
	}

	@Override
	public String toString() {
		return "UnitTests [enabled=" + enabled + ", acceptance=" + acceptance + ", artifactType=" + artifactType
				+ ", testsuites=" + testsuites + "]";
	}
}
