package com.itorix.apiwiz.common.model.proxystudio;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name", "value"})
public class Attribute {

	@JsonProperty("name")
	private String name;

	@JsonProperty("value")
	private String value;

	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/** @return The name */
	@JsonProperty("name")
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name
	 */
	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	/** @return The value */
	@JsonProperty("value")
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            The value
	 */
	@JsonProperty("value")
	public void setValue(String value) {
		this.value = value;
	}

	@JsonIgnore
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonIgnore
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}
}
