package com.itorix.apiwiz.testsuite.model;

public class TestCaseStats {

	private String name;

	private Double coverage;

	private Double latency;

	public TestCaseStats() {
	}

	public TestCaseStats(String name, Double coverage, Double latency) {
		super();
		this.name = name;
		this.coverage = coverage;
		this.latency = latency;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getCoverage() {
		return coverage;
	}

	public void setCoverage(Double coverage) {
		this.coverage = coverage;
	}

	public Double getLatency() {
		return latency;
	}

	public void setLatency(Double latency) {
		this.latency = latency;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TestCaseStats [name=");
		builder.append(name);
		builder.append(", coverage=");
		builder.append(coverage);
		builder.append(", latency=");
		builder.append(latency);
		builder.append("]");
		return builder.toString();
	}

}