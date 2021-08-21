package com.itorix.apiwiz.virtualization.model.expectation;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Value {
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

	private List<String> text;
	private Condition condition;
}
