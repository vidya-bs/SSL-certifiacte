package com.itorix.apiwiz.test.db;

public class TestExecutorEntity {
	public static final String TABLE_NAME = "test_executor";

	public enum STATUSES {

		SCHEDULED("Scheduled"), IN_PROGRESS("In Progress") , COMPLETED("Completed"),CANCELLED("Cancelled");
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

	private Long id;
	private String tenant;
	private String testSuiteExecutionId;
	private String status;
	private String errorDescription;

	public String getTenant() {
		return tenant;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public String getTestSuiteExecutionId() {
		return testSuiteExecutionId;
	}

	public void setTestSuiteExecutionId(String executionId) {
		this.testSuiteExecutionId = executionId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

}
