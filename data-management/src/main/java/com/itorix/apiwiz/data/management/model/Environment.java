package com.itorix.apiwiz.data.management.model;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "environment", "revision" })
public class Environment {

	@JsonProperty("environment")
	private String environment;
	@JsonProperty("revision")
	private String revision;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/**
	 * 
	 * @return The environment
	 */
	@JsonProperty("environment")
	public String getEnvironment() {
		return environment;
	}

	/**
	 * 
	 * @param environment
	 *            The environment
	 */
	@JsonProperty("environment")
	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	/**
	 * 
	 * @return The revision
	 */
	@JsonProperty("revision")
	public String getRevision() {
		return revision;
	}

	/**
	 * 
	 * @param revision
	 *            The revision
	 */
	@JsonProperty("revision")
	public void setRevision(String revision) {
		this.revision = revision;
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