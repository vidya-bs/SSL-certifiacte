package com.itorix.apiwiz.testsuite.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VariableRef {
	@JsonProperty("name")
	private String name;

	@JsonProperty("reference")
	private String reference;

	@JsonProperty("value")
	private String value;

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("reference")
	public String getReference() {
		return reference;
	}

	@JsonProperty("reference")
	public void setReference(String reference) {
		this.reference = reference;
	}

	@JsonProperty("value")
	public String getValue() {
		return value;
	}

	@JsonProperty("value")
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Variable [name=");
		builder.append(name);
		builder.append(", reference=");
		builder.append(reference);
		builder.append(", value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}
}
