package com.itorix.apiwiz.common.model.apigee.metrics;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetaData {
	private String[] errors;

	private String[] notices;

	public String[] getErrors() {
		return errors;
	}

	public void setErrors(String[] errors) {
		this.errors = errors;
	}

	public String[] getNotices() {
		return notices;
	}

	public void setNotices(String[] notices) {
		this.notices = notices;
	}

	@Override
	public String toString() {
		return "ClassPojo [errors = " + errors + ", notices = " + notices + "]";
	}
}
