package com.itorix.apiwiz.testsuite.business.gocd.beans;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"type", "attributes"})
public class Task {

	@JsonProperty("type")
	private String type;

	@JsonProperty("attributes")
	private Attributes_ attributes;

	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	public Task() {
	}

	public Task(String type, String runCondition, String command, String arguments, String workingDirectory,
			boolean isCommand) {
		this.type = type;
		attributes = new Attributes_(runCondition, command, arguments, null, true);
	}

	public Task(String type, String runCondition, String taskName, String additionalOptions, String gradleHome) {
		this.type = type;
		attributes = new Attributes_(runCondition, taskName, additionalOptions, gradleHome);
	}

	public Task(String type, String runCondition, String pipeline, String stage, String job, String source,
			String destination) {
		this.type = type;
		attributes = new Attributes_(runCondition, pipeline, stage, job, source, destination);
	}

	@JsonProperty("type")
	public String getType() {
		return type;
	}

	@JsonProperty("type")
	public void setType(String type) {
		this.type = type;
	}

	@JsonProperty("attributes")
	public Attributes_ getAttributes() {
		return attributes;
	}

	@JsonProperty("attributes")
	public void setAttributes(Attributes_ attributes) {
		this.attributes = attributes;
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
