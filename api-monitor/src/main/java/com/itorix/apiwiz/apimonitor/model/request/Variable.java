package com.itorix.apiwiz.apimonitor.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
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