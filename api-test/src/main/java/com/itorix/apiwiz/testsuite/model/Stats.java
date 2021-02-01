
package com.itorix.apiwiz.testsuite.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Stats {

	private List<DashboardSummary> topSuccess;

	private List<DashboardSummary> topError;

	public Stats(List<DashboardSummary> topSuccess, List<DashboardSummary> topError) {
		super();
		this.topSuccess = topSuccess;
		this.topError = topError;
	}

	public List<DashboardSummary> gettopSuccess() {
		return topSuccess;
	}

	public void settopSuccess(List<DashboardSummary> topSuccess) {
		this.topSuccess = topSuccess;
	}

	public List<DashboardSummary> gettopError() {
		return topError;
	}

	public void settopError(List<DashboardSummary> topError) {
		this.topError = topError;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Stats [topSuccess=");
		builder.append(topSuccess);
		builder.append(", topError=");
		builder.append(topError);
		builder.append("]");
		return builder.toString();
	}
}