package com.itorix.apiwiz.performance.coverge.model;

import java.util.List;

public class SoapUIProject {
	
	private List<SoapUITestSuite> testSuiteList;
	
	private Integer total;
	
	private Integer failed;
	
	private Integer passed;

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public Integer getFailed() {
		return failed;
	}

	public void setFailed(Integer failed) {
		this.failed = failed;
	}

	public Integer getPassed() {
		return passed;
	}

	public void setPassed(Integer passed) {
		this.passed = passed;
	}

	public List<SoapUITestSuite> getTestSuiteList() {
		return testSuiteList;
	}

	public void setTestSuiteList(List<SoapUITestSuite> testSuiteList) {
		this.testSuiteList = testSuiteList;
	}

	public SoapUIProject(List<SoapUITestSuite> testSuiteList, Integer total, Integer failed, Integer passed) {
		super();
		this.testSuiteList = testSuiteList;
		this.total = total;
		this.failed = failed;
		this.passed = passed;
	}

	

}