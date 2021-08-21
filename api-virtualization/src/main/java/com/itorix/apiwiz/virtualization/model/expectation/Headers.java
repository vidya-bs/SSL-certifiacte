package com.itorix.apiwiz.virtualization.model.expectation;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Headers {
	@JsonProperty("content-type")
	String contentType;

	String id;
}
