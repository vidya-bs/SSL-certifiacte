package com.itorix.apiwiz.data.management.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.itorix.apiwiz.data.management.model.mappers.Environment;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({"environments", "versions", "maxversion"})
public class RestoreProxyInfo extends BackupCommon {

	@JsonProperty("environments")
	private List<Environment> environments = new ArrayList<Environment>();

	@JsonProperty("versions")
	private List<String> versions = new ArrayList<String>();

	@JsonProperty("maxversion")
	private Integer maxversion;

	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/** @return The environments */
	@JsonProperty("environments")
	public List<Environment> getEnvironments() {
		return environments;
	}

	/**
	 * @param environments
	 *            The environments
	 */
	@JsonProperty("environments")
	public void setEnvironments(List<Environment> environments) {
		this.environments = environments;
	}

	/** @return The versions */
	@JsonProperty("versions")
	public List<String> getVersions() {
		return versions;
	}

	/**
	 * @param versions
	 *            The versions
	 */
	@JsonProperty("versions")
	public void setVersions(List<String> versions) {
		this.versions = versions;
	}

	/** @return The maxversion */
	@JsonProperty("maxversion")
	public Integer getMaxversion() {
		return maxversion;
	}

	/**
	 * @param maxversion
	 *            The maxversion
	 */
	@JsonProperty("maxversion")
	public void setMaxversion(Integer maxversion) {
		this.maxversion = maxversion;
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
