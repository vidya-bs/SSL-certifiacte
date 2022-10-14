package com.itorix.apiwiz.testsuite.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itorix.apiwiz.common.model.AbstractObject;

@Document(collection = "Test.Collections.List")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestSuite extends AbstractObject {

	@JsonProperty("name")
	private String name;

	@JsonProperty("description")
	private String description;

	@JsonProperty("scenarioSequence")
	private List<String> scenarioSequence;

	@JsonProperty("date")
	private String date;

	@JsonProperty("status")
	private String status;

	@JsonProperty("variables")
	private List<Header> vars;

	@JsonProperty("scenarios")
	private List<Scenario> scenarios = null;

	@JsonProperty("successRate")
	private int successRate;

	@JsonProperty("isActive")
	private Boolean active;

	@JsonProperty("notifications")
	private List<Notification> notifications;

	@JsonProperty("schedule")
	private Schedule schedule;

	@JsonProperty("revisionStatus")
	private String revisionStatus;

	@JsonProperty("version")
	private String version;

	@JsonProperty("timeout")
	private Long timeout;

	@JsonProperty("duration")
	private Long duration;

	@JsonProperty("successRatio")
	private Double successRatio = 0.0;

	@JsonProperty("executionStatus")
	private String executionStatus = "not executed";

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name.trim();
	}

	public String getExecutionStatus() {
		return executionStatus;
	}

	public void setExecutionStatus(String executionStatus) {
		this.executionStatus = executionStatus;
	}

	@JsonProperty("description")
	public String getDescription() {
		return description;
	}

	@JsonProperty("description")
	public void setDescription(String description) {
		this.description = description;
	}

	@JsonProperty("scenarios")
	public List<Scenario> getScenarios() {
		return scenarios;
	}

	@JsonProperty("scenarios")
	public void setScenarios(List<Scenario> scenarios) {
		this.scenarios = scenarios;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<Header> getVars() {
		return vars;
	}

	public void setVars(List<Header> vars) {
		this.vars = vars;
	}

	public int getSuccessRate() {
		return successRate;
	}

	public void setSuccessRate(int successRate) {
		this.successRate = successRate;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public List<Notification> getNotifications() {
		return notifications;
	}

	public void setNotifications(List<Notification> notifications) {
		this.notifications = notifications;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	public String getRevisionStatus() {
		return revisionStatus;
	}

	public void setRevisionStatus(String revisionStatus) {
		this.revisionStatus = revisionStatus;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public Long getDuration() {
		duration = 0L;
		if (scenarios != null) {
			for (Scenario scenario : scenarios) {
				if (scenario.getDuration() != null) {
					duration += scenario.getDuration();
				}
			}
		}
		return duration;
	}

	public List<String> getScenarioSequence() {
		return scenarioSequence;
	}

	public void setScenarioSequence(List<String> scenarioSequence) {
		this.scenarioSequence = scenarioSequence;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public Double getSuccessRatio() {
		return successRatio;
	}

	public void setSuccessRatio(Double successRatio) {
		this.successRatio = successRatio;
	}

	public Boolean hasTestCases() {
		if (null != this.scenarios && scenarios.size() > 0) {
			for (Scenario scenario : scenarios) {
				if (scenario.hasTestCases())
					return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}
}
