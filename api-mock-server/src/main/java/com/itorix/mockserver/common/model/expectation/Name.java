package com.itorix.mockserver.common.model.expectation;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Name {

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

	String key;
	Condition condition;
}
