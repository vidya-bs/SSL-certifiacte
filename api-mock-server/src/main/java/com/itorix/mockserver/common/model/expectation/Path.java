package com.itorix.mockserver.common.model.expectation;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Path {

	public enum Condition {

		@JsonProperty("equalTo")
		equalTo,

		@JsonProperty("notEqualTo")
		notEqualTo,

		// @JsonProperty("contains")
		// contains,

		@JsonProperty("regEx")
		regEx,
	}

	private String value;
	private Condition condition;
	@JsonProperty("isRegEx")
	private boolean regEx;
}
