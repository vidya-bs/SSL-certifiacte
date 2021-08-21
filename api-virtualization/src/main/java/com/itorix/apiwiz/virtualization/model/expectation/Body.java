package com.itorix.apiwiz.virtualization.model.expectation;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Body {

	public enum Type {
		@JsonProperty("JSON")
		json,

		@JsonProperty("JSON_SCHEMA")
		jsonSchema,

		@JsonProperty("XML")
		xml,

		@JsonProperty("XML_SCHEMA")
		xmlSchema,

		@JsonProperty("FORM_PARAMS")
		formParams,

		@JsonProperty("FORM_URL_ENCODED")
		formURLEncoded,
	}

	@JsonProperty("formParams")
	private List<NameMultiValue> formParams = null;

	@JsonProperty("formURLEncoded")
	private List<NameMultiValue> formURLEncoded = null;

	private Type type;
	private List<Data> data;
	private String value;

	@JsonProperty("isStrict")
	private boolean strict;
}
