
package com.itorix.apiwiz.testsuite.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName("testSuite")
public class TestSuiteAnalysis {

	@JsonProperty("testSuiteName")
	private String testSuiteName;

	@JsonProperty("environment")
	private String environment;

	@JsonProperty("testSuiteId")
	private String testSuiteID;

	@JsonProperty("configId")
	private String configID;

	@JsonProperty("timeseries")
	private List<TestSuiteStats> stats;

	public TestSuiteAnalysis(List<TestSuiteStats> stats) {
		super();
		this.stats = stats;
	}

	public String getTestSuiteName() {
		return testSuiteName;
	}

	public void setTestSuiteName(String testSuiteName) {
		this.testSuiteName = testSuiteName;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getTestSuiteID() {
		return testSuiteID;
	}

	public void setTestSuiteID(String testSuiteID) {
		this.testSuiteID = testSuiteID;
	}

	public String getConfigID() {
		return configID;
	}

	public void setConfigID(String configID) {
		this.configID = configID;
	}

	public List<TestSuiteStats> getStats() {
		return stats;
	}

	public void setStats(List<TestSuiteStats> stats) {
		this.stats = stats;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TestSuiteAnalysis [testSuiteName=");
		builder.append(testSuiteName);
		builder.append(", environment=");
		builder.append(environment);
		builder.append(", testSuiteID=");
		builder.append(testSuiteID);
		builder.append(", configID=");
		builder.append(configID);
		builder.append(", stats=");
		builder.append(stats);
		builder.append("]");
		return builder.toString();
	}

}