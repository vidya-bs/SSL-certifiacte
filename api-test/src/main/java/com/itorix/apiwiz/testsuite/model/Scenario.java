package com.itorix.apiwiz.testsuite.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Scenario {

	@JsonProperty("id")
	private String id = UUID.randomUUID().toString();

	@JsonProperty("name")
	private String name;

	@JsonProperty("description")
	private String description;

	@JsonProperty("status")
	private String status;

	@JsonProperty("testCases")
	private List<TestCase> testCases = null;

	@JsonProperty("successRate")
	private int successRate;

	@JsonProperty("duration")
	private Long duration;

	@JsonProperty("continueOnError")
	private boolean continueOnError = true;

	@JsonProperty("timeout")
	private int timeOut=0;

	@JsonProperty("timeout")
	public int getTimeOut() {
		return timeOut;
	}

	@JsonProperty("timeout")
	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

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

	@JsonProperty("depends")
	private String dependsOn;

	@JsonProperty("testCases")
	public List<TestCase> getTestCases() {
		if (this.testCases == null)
			this.testCases = new ArrayList<TestCase>();
		return testCases;
	}

	@JsonProperty("testCases")
	public void setTestCases(List<TestCase> testCases) {
		this.testCases = testCases;
	}

	@JsonProperty("status")
	public String getStatus() {
		return status;
	}

	@JsonProperty("status")
	public void setStatus(String status) {
		this.status = status;
	}

	public int getSuccessRate() {
		return successRate;
	}

	public void setSuccessRate(int successRate) {
		this.successRate = successRate;
	}

	@JsonProperty("id")
	public String getId() {
		return id;
	}

	@JsonProperty("id")
	public void setId(String id) {
		this.id = id;
	}

	public Long getDuration() {
		duration = 0L;
		if (testCases != null) {
			for (TestCase testCase : testCases) {
				if (testCase.getDuration() != null) {
					duration += testCase.getDuration();
				}
			}
		}
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public Boolean hasTestCases() {
		if (this.testCases != null && this.testCases.size() > 0)
			return Boolean.TRUE;
		return Boolean.FALSE;
	}

	public String getDependsOn() {
		return dependsOn;
	}

	public void setDependsOn(String dependsOn) {
		this.dependsOn = dependsOn;
	}
}
