package com.itorix.apiwiz.common.model.apigee;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApigeeMetaData {
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
}
