package com.itorix.apiwiz.monitor.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Variable {

	@JsonProperty("name")
	private String name;

	@JsonProperty("reference")
	private String reference;

	@JsonProperty("value")
	private String value;

	@JsonProperty("runTimevalue")
	private String runTimevalue;
}