package com.itorix.apiwiz.common.model.apigee.metrics;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PerformanceTrafficResponse {

	private Environments[] environments;

	// private MetaData metaData;

	public Environments[] getEnvironments() {
		return environments;
	}

	public void setEnvironments(Environments[] environments) {
		this.environments = environments;
	}

	/*
	 * public MetaData getMetaData () { return metaData; }
	 * 
	 * public void setMetaData (MetaData metaData) { this.metaData = metaData; }
	 */

}
