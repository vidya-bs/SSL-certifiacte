package com.itorix.apiwiz.testsuite.model;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.model.AbstractObject;

@Document(collection = "Test.Events.History")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestSuiteResponse extends AbstractObject {

	public enum STATUSES {
		SCHEDULED("Scheduled"), IN_PROGRESS("In Progress"), COMPLETED("Completed"), CANCELLED("Cancelled");

		private String value;

		private STATUSES(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public static String getStatus(STATUSES status) {

			String userRoles = null;
			for (STATUSES role : STATUSES.values()) {
				if (role.equals(status)) {
					userRoles = role.getValue();
				}
			}
			return userRoles;
		}
	}

	private String testSuiteId;

	private String configId;

	private TestSuite testSuite;

	private String status;

	private String testSuiteName;

	private String counter;

	private int successRate;

	private String testStatus;

	private Long duration;

	private boolean isManual = false;

	private String testSuiteAgent;

	private String userId;

	public TestSuiteResponse(String testSuiteId, String configId, TestSuite testSuite, String status, String counter) {
		super();
		this.setCts(System.currentTimeMillis());
		this.testSuiteId = testSuiteId;
		this.configId = configId;
		this.testSuite = testSuite;
		this.status = status;
		this.counter = counter;
	}

	public TestSuiteResponse(String testSuiteId, String configId, TestSuite testSuite, String status) {
		super();
		this.setCts(System.currentTimeMillis());
		this.testSuiteId = testSuiteId;
		this.configId = configId;
		this.testSuite = testSuite;
		this.status = status;
	}

	public TestSuiteResponse() {
	}

	public String getCounter() {
		return counter;
	}

	public void setCounter(String counter) {
		this.counter = counter;
	}

	public String getTestSuiteName() {
		if (testSuite != null) {
			return testSuite.getName();
		}
		return testSuiteName;
	}

	public void setTestSuiteName(String testSuiteName) {
		this.testSuiteName = testSuiteName;
	}

	public String getConfigName() {
		return configName;
	}

	public void setConfigName(String configName) {
		this.configName = configName;
	}

	private String configName;

	public String getTestSuiteId() {
		return testSuiteId;
	}

	public void setTestSuiteId(String testSuiteId) {
		this.testSuiteId = testSuiteId;
	}

	public String getConfigId() {
		return configId;
	}

	public void setConfigId(String configId) {
		this.configId = configId;
	}

	public TestSuite getTestSuite() {
		return testSuite;
	}

	public void setTestSuite(TestSuite testSuite) {
		this.testSuite = testSuite;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getSuccessRate() {
		return successRate;
	}

	public void setSuccessRate(int successRate) {
		this.successRate = successRate;
	}

	public String getTestStatus() {
		return testStatus;
	}

	public void setTestStatus(String testStatus) {
		this.testStatus = testStatus;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public boolean isManual() {
		return isManual;
	}

	public void setManual(boolean isManual) {
		this.isManual = isManual;
	}

	public String getTestSuiteAgent() {
		return testSuiteAgent;
	}

	public void setTestSuiteAgent(String testSuiteAgent) {
		this.testSuiteAgent = testSuiteAgent;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
