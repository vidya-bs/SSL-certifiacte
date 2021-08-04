package com.itorix.apiwiz.cicd.gocd.beans;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"group", "pipeline"})
public class PipelineGroup {

	@JsonProperty("group")
	private String group;

	@JsonProperty("pipeline")
	private Pipeline pipeline;

	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	public PipelineGroup() {
	}

	public PipelineGroup(String group) {
		this.group = group;
	}

	@JsonProperty("group")
	public String getGroup() {
		return group;
	}

	@JsonProperty("group")
	public void setGroup(String group) {
		this.group = group;
	}

	@JsonProperty("pipeline")
	public Pipeline getPipeline() {
		return pipeline;
	}

	@JsonProperty("pipeline")
	public void setPipeline(Pipeline pipeline) {
		this.pipeline = pipeline;
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
