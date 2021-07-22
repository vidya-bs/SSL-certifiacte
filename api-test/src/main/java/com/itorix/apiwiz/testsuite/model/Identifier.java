package com.itorix.apiwiz.testsuite.model;

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
@JsonPropertyOrder({"names", "values"})
public class Identifier {

	@JsonProperty("names")
	private List<String> names = null;

	@JsonProperty("values")
	private List<String> values = null;

	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("names")
	public List<String> getNames() {
		return names;
	}

	@JsonProperty("names")
	public void setNames(List<String> names) {
		this.names = names;
	}

	@JsonProperty("values")
	public List<String> getValues() {
		return values;
	}

	@JsonProperty("values")
	public void setValues(List<String> values) {
		this.values = values;
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
