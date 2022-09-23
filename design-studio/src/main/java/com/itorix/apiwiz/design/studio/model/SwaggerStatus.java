package com.itorix.apiwiz.design.studio.model;

public enum SwaggerStatus {
	DRAFT("Draft"), REVIEW("Review"), CHANGE_REQUIRED("Change Required"), APPROVED("Approved"), PUBLISH(
			"Publish"), DEPRECATE("Deprecate"), RETIRED("Retired");

	private String status;

	private SwaggerStatus(String s) {
		status = s;
	}

	public String getStatus() {
		return status;
	}
}
