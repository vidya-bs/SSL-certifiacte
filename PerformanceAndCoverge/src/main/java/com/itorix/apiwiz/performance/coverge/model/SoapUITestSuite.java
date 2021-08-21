package com.itorix.apiwiz.performance.coverge.model;

import java.util.List;

public class SoapUITestSuite {

	private String testSuiteName;

	private List<SoapUITestCase> testCases;
	private Integer totalTestCases;
	private Integer totalFailed;
	private Integer totalPassed;

	public SoapUITestSuite(String testSuiteName, List<SoapUITestCase> testCases, Integer totalTestCases,
			Integer totalFailed, Integer totalPassed) {
		super();
		this.testSuiteName = testSuiteName;
		this.testCases = testCases;
		this.totalTestCases = totalTestCases;
		this.totalFailed = totalFailed;
		this.totalPassed = totalPassed;
	}

	public String getTestSuiteName() {
		return testSuiteName;
	}

	public void setTestSuiteName(String testSuiteName) {
		this.testSuiteName = testSuiteName;
	}

	public List<SoapUITestCase> getTestCases() {
		return testCases;
	}

	public void setTestCases(List<SoapUITestCase> testCases) {
		this.testCases = testCases;
	}

	public Integer getTotalTestCases() {
		return totalTestCases;
	}

	public void setTotalTestCases(Integer totalTestCases) {
		this.totalTestCases = totalTestCases;
	}

	public Integer getTotalFailed() {
		return totalFailed;
	}

	public void setTotalFailed(Integer totalFailed) {
		this.totalFailed = totalFailed;
	}

	public Integer getTotalPassed() {
		return totalPassed;
	}

	public void setTotalPassed(Integer totalPassed) {
		this.totalPassed = totalPassed;
	}
}
