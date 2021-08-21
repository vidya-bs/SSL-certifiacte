package com.itorix.apiwiz.testsuite.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestSuiteStats {

	private Long timeStamp;

	private String testSuiteId;

	private String name;

	private int testSuiteCoverage;

	private Double latency;

	private List<ScenarioStats> scenarios;

	public TestSuiteStats() {
	}

	public TestSuiteStats(Long timeStamp, String testSuiteId, String name, int testSuiteCoverage, Double latency,
			List<ScenarioStats> scenarios) {
		super();
		this.timeStamp = timeStamp;
		this.testSuiteId = testSuiteId;
		this.name = name;
		this.testSuiteCoverage = testSuiteCoverage;
		this.latency = latency;
		this.scenarios = scenarios;
	}

	public Long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getTestSuiteId() {
		return testSuiteId;
	}

	public void setTestSuiteId(String testSuiteId) {
		this.testSuiteId = testSuiteId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTestSuiteCoverage() {
		return testSuiteCoverage;
	}

	public void setTestSuiteCoverage(int testSuiteCoverage) {
		this.testSuiteCoverage = testSuiteCoverage;
	}

	public Double getLatency() {
		return latency;
	}

	public void setLatency(Double latency) {
		this.latency = latency;
	}

	public List<ScenarioStats> getScenarios() {
		return scenarios;
	}

	public void setScenarios(List<ScenarioStats> scenarios) {
		this.scenarios = scenarios;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TestSuiteStats [timeStamp=");
		builder.append(timeStamp);
		builder.append(", name=");
		builder.append(name);
		builder.append(", testSuiteCoverage=");
		builder.append(testSuiteCoverage);
		builder.append(", latency=");
		builder.append(latency);
		builder.append(", scenarios=");
		builder.append(scenarios);
		builder.append("]");
		return builder.toString();
	}
}
