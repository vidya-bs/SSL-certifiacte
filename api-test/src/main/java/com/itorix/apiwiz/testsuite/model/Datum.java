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
@JsonPropertyOrder({"identifier", "metric"})
public class Datum {

	@JsonProperty("identifier")
	private Identifier identifier;

	@JsonProperty("metric")
	private List<Metric> metric = null;

	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("identifier")
	public Identifier getIdentifier() {
		return identifier;
	}

	@JsonProperty("identifier")
	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}

	@JsonProperty("metric")
	public List<Metric> getMetric() {
		return metric;
	}

	@JsonProperty("metric")
	public void setMetric(List<Metric> metric) {
		this.metric = metric;
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
