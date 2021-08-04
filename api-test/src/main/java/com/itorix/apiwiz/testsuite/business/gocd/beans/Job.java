package com.itorix.apiwiz.testsuite.business.gocd.beans;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name", "run_instance_count", "timeout", "environment_variables", "resources", "tasks", "tabs",
		"artifacts", "properties"})
public class Job {

	@JsonProperty("name")
	private String name;

	@JsonProperty("run_instance_count")
	private Object runInstanceCount;

	@JsonProperty("timeout")
	private Object timeout;

	@JsonProperty("environment_variables")
	private List<Object> environmentVariables = null;

	@JsonProperty("resources")
	private List<Object> resources = null;

	@JsonProperty("tasks")
	private List<Task> tasks = null;

	@JsonProperty("tabs")
	private List<Object> tabs = null;

	@JsonProperty("artifacts")
	private List<Artifact> artifacts = null;

	@JsonProperty("properties")
	private Object properties;

	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	public Job() {
	}

	public Job(String name) {
		this.name = name;
	}

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("run_instance_count")
	public Object getRunInstanceCount() {
		return runInstanceCount;
	}

	@JsonProperty("run_instance_count")
	public void setRunInstanceCount(Object runInstanceCount) {
		this.runInstanceCount = runInstanceCount;
	}

	@JsonProperty("timeout")
	public Object getTimeout() {
		return timeout;
	}

	@JsonProperty("timeout")
	public void setTimeout(Object timeout) {
		this.timeout = timeout;
	}

	@JsonProperty("environment_variables")
	public List<Object> getEnvironmentVariables() {
		return environmentVariables;
	}

	@JsonProperty("environment_variables")
	public void setEnvironmentVariables(List<Object> environmentVariables) {
		this.environmentVariables = environmentVariables;
	}

	@JsonProperty("resources")
	public List<Object> getResources() {
		return resources;
	}

	@JsonProperty("resources")
	public void setResources(List<Object> resources) {
		this.resources = resources;
	}

	@JsonProperty("tasks")
	public List<Task> getTasks() {
		return tasks;
	}

	@JsonProperty("tasks")
	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	@JsonProperty("tabs")
	public List<Object> getTabs() {
		return tabs;
	}

	@JsonProperty("tabs")
	public void setTabs(List<Object> tabs) {
		this.tabs = tabs;
	}

	@JsonProperty("artifacts")
	public List<Artifact> getArtifacts() {
		return artifacts;
	}

	@JsonProperty("artifacts")
	public void setArtifacts(List<Artifact> artifacts) {
		this.artifacts = artifacts;
	}

	@JsonProperty("properties")
	public Object getProperties() {
		return properties;
	}

	@JsonProperty("properties")
	public void setProperties(Object properties) {
		this.properties = properties;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}
}
