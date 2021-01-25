
package com.itorix.apiwiz.testsuite.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dashboard {

	@JsonProperty("timeUnit")
	private List<Integer> timeUnit = null;

	@JsonProperty("totalExecution")
	private int totalExecution;

	@JsonProperty("totalSuccess")
	private int totalSuccess;

	@JsonProperty("totalFail")
	private int totalFail;

	@JsonProperty("totalCancelled")
	private int totalCancelled;

	@JsonProperty("stats")
	private Stats stats;
	
	public Dashboard() {}
	public Dashboard(int totalExecution, int totalSuccess, int totalFail, int totalCancelled, Stats stats) {
		super();
		this.totalExecution = totalExecution;
		this.totalSuccess = totalSuccess;
		this.totalFail = totalFail;
		this.totalCancelled = totalCancelled;
		this.stats = stats;
	}

	public List<Integer> getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(List<Integer> timeUnit) {
		this.timeUnit = timeUnit;
	}

	public int getTotalExecution() {
		return totalExecution;
	}

	public void setTotalExecution(int totalExecution) {
		this.totalExecution = totalExecution;
	}

	public int getTotalSuccess() {
		return totalSuccess;
	}

	public void setTotalSuccess(int totalSuccess) {
		this.totalSuccess = totalSuccess;
	}

	public int getTotalFail() {
		return totalFail;
	}

	public void setTotalFail(int totalFail) {
		this.totalFail = totalFail;
	}

	public int getTotalCancelled() {
		return totalCancelled;
	}

	public void setTotalCancelled(int totalCancelled) {
		this.totalCancelled = totalCancelled;
	}

	public Stats getStats() {
		return stats;
	}

	public void setStats(Stats stats) {
		this.stats = stats;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Dashboard [timeUnit=");
		builder.append(timeUnit);
		builder.append(", totalExecution=");
		builder.append(totalExecution);
		builder.append(", totalSuccess=");
		builder.append(totalSuccess);
		builder.append(", totalFail=");
		builder.append(totalFail);
		builder.append(", totalCancelled=");
		builder.append(totalCancelled);
		builder.append(", stats=");
		builder.append(stats);
		builder.append("]");
		return builder.toString();
	}

}