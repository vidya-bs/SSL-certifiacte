package com.itorix.apiwiz.common.model.apigee;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApigeeResponse {

	private Environments[] environments;

	private ApigeeMetaData metaData;

	public Environments[] getEnvironments() {
		return environments;
	}

	public void setEnvironments(Environments[] environments) {
		this.environments = environments;
	}

	public ApigeeMetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(ApigeeMetaData metaData) {
		this.metaData = metaData;
	}
}
