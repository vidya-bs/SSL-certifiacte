package com.itorix.apiwiz.common.model.apigee;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Environments {

	private String name;
	private Metrics[] metrics;
	private Dimensions[] dimensions;

	public Dimensions[] getDimensions() {
		return dimensions;
	}

	public void setDimensions(Dimensions[] dimensions) {
		this.dimensions = dimensions;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Metrics[] getMetrics() {
		return metrics;
	}

	public void setMetrics(Metrics[] metrics) {
		this.metrics = metrics;
	}
	
	
	
}
