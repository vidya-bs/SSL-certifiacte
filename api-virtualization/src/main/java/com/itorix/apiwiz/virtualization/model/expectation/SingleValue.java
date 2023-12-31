package com.itorix.apiwiz.virtualization.model.expectation;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SingleValue {
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

	private String text;
	private Condition condition;
}
