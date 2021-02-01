package com.itorix.apiwiz.projectmanagement.model.cicd;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "enabled", "acceptance" })
public class CodeCoverage {

	@JsonProperty("enabled")
	private String enabled;
	
	@JsonProperty("acceptance")
	private Integer acceptance;

	@JsonProperty("artifactType")
	private String artifactType;

	@JsonProperty("testsuites")
	List<TestSuiteAndConfig> testSuites;
	
	@JsonProperty("enabled")
	public String getEnabled() {
		return enabled;
	}

	@JsonProperty("enabled")
	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}

	@JsonProperty("acceptance")
	public Integer getAcceptance() {
		return acceptance;
	}

	@JsonProperty("acceptance")
	public void setAcceptance(Integer acceptance) {
		this.acceptance = acceptance;
	}

	public List<TestSuiteAndConfig> getTestSuites() {
		return testSuites;
	}

	public void setTestSuites(List<TestSuiteAndConfig> testSuites) {
		this.testSuites = testSuites;
	}

	public String getArtifactType() {
		return artifactType;
	}

	public void setArtifactType(String artifactType) {
		this.artifactType = artifactType;
	}
	
}