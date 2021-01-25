
package com.itorix.apiwiz.virtualization.model.expectation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class FormParam {

	public enum Condition {

		@JsonProperty("equalTo")
		equalTo,

		@JsonProperty("notEqualTo")
		notEqualTo,

		@JsonProperty("contains")
		contains,

		@JsonProperty("regEx")
		regEx,
	}

	private Condition condition;
	private String name;
	private String value;
}