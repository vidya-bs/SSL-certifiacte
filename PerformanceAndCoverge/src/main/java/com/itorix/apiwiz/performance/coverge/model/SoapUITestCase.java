package com.itorix.apiwiz.performance.coverge.model;

public class SoapUITestCase {
	
	@Override
	public String toString() {
		return "SoapUITestCase [testCaseName=" + testCaseName + ", testCaseStatus=" + testCaseStatus + ", failedReason="
				+ failedReason + ", duration=" + duration + "]";
	}

	private String testCaseName;
	private String testCaseStatus;
	private String failedReason;
	private Long duration;
	

	public SoapUITestCase(String testCaseName, String testCaseStatus, String failedReason, Long duration) {
		super();
		this.testCaseName = testCaseName;
		this.testCaseStatus = testCaseStatus;
		this.failedReason = failedReason;
		this.duration = duration;
	}

	public String getTestCaseName() {
		return testCaseName;
	}

	public void setTestCaseName(String testCaseName) {
		this.testCaseName = testCaseName;
	}

	public String getTestCaseStatus() {
		return testCaseStatus;
	}

	public void setTestCaseStatus(String testCaseStatus) {
		this.testCaseStatus = testCaseStatus;
	}

	public String getFailedReason() {
		return failedReason;
	}

	public void setFailedReason(String failedReason) {
		this.failedReason = failedReason;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

}
