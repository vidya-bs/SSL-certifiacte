package com.itorix.apiwiz.test.executor.beans;

import com.itorix.apiwiz.test.db.TestExecutorEntity;

public class ExecutionContext {

	private TestSuiteResponse testSuiteResponse;
	private String tenant;
	private TestExecutorEntity testExecutorEntity;
	private int globalTimeout = 4000;

	public TestSuiteResponse getTestSuiteResponse() {
		return testSuiteResponse;
	}

	public void setTestSuiteResponse(TestSuiteResponse testSuiteResponse) {
		this.testSuiteResponse = testSuiteResponse;
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public TestExecutorEntity getTestExecutorEntity() {
		return testExecutorEntity;
	}

	public void setTestExecutorEntity(TestExecutorEntity testExecutorEntity) {
		this.testExecutorEntity = testExecutorEntity;
	}

	public int getGlobalTimeout() {
		return globalTimeout;
	}

	public void setGlobalTimeout(int globalTimeout) {
		this.globalTimeout = globalTimeout;
	}
}
