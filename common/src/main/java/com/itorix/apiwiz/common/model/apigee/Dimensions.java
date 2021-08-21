package com.itorix.apiwiz.common.model.apigee;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dimensions {

	private Metrics[] metrics;
	private String name;

	public Metrics[] getMetrics() {
		return metrics;
	}

	public void setMetrics(Metrics[] metrics) {
		this.metrics = metrics;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
