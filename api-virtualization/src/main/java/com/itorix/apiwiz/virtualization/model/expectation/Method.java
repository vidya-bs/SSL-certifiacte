package com.itorix.apiwiz.virtualization.model.expectation;
// import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1

// import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
/* ObjectMapper om = new ObjectMapper();
Root root = om.readValue(myJsonString), Root.class); */

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Method {

	public enum Condition {

		@JsonProperty("equalTo")
		equalTo,

		@JsonProperty("notEqualTo")
		notEqualTo,

		@JsonProperty("any")
		any,
	}

	String name;
	Condition condition;
}
