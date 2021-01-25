package com.itorix.apiwiz.performance.coverge.model;

import java.util.List;

public class Operations {
	private String operationName;

	private List<Metrics> metrics;

	
	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public List<Metrics> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<Metrics> metrics) {
		this.metrics = metrics;
	}

}
