package com.itorix.apiwiz.testsuite.model;

import java.util.List;

public class ScenarioStats {

	private String name;

	private int coverage;

	private Double latency;

	private List<TestCaseStats> testCases;

	public ScenarioStats() {
	}

	public ScenarioStats(String name, int coverage, Double latency, List<TestCaseStats> testCases) {
		super();
		this.name = name;
		this.coverage = coverage;
		this.latency = latency;
		this.testCases = testCases;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCoverage() {
		return coverage;
	}

	public void setCoverage(int coverage) {
		this.coverage = coverage;
	}

	public Double getLatency() {
		return latency;
	}

	public void setLatency(Double latency) {
		this.latency = latency;
	}

	public List<TestCaseStats> getTestCases() {
		return testCases;
	}

	public void setTestCases(List<TestCaseStats> testCases) {
		this.testCases = testCases;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ScenarioStats [name=");
		builder.append(name);
		builder.append(", coverage=");
		builder.append(coverage);
		builder.append(", latency=");
		builder.append(latency);
		builder.append(", testCases=");
		builder.append(testCases);
		builder.append("]");
		return builder.toString();
	}

}