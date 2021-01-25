
package com.itorix.apiwiz.test.executor.beans;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "Test.Collections.List")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestSuite {

	@JsonProperty("name")
	private String name;

	@JsonProperty("description")
	private String description;

	@Id
	private String id;

	@JsonProperty("executionId")
	private Integer executionId;

	@JsonProperty("date")
	private String date;

	@JsonProperty("status")
	private String status;

	@JsonProperty("variables")
	private List<Header> vars = new ArrayList<>();

	@JsonProperty("scenarios")
	private List<Scenario> scenarios = null;

	@JsonProperty("successRate")
	private int successRate;

	@JsonProperty("isActive")
	private Boolean active;

	@JsonProperty("timeout")
	private Long timeout;

	@JsonProperty("successRatio")
	private Double successRatio;

	@JsonProperty("executionStatus")
	private String executionStatus = "not executed";



	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public Integer getExecutionId() {
		return executionId;
	}

	public void setExecutionId(Integer executionId) {
		this.executionId = executionId;
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

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public Double getSuccessRatio() {
		return successRatio;
	}

	public void setSuccessRatio(Double successRatio) {
		this.successRatio = successRatio;
	}

	public String getExecutionStatus() {
		return executionStatus;
	}

	public void setExecutionStatus(String executionStatus) {
		this.executionStatus = executionStatus;
	}
}