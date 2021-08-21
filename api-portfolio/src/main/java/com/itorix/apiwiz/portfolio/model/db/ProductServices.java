package com.itorix.apiwiz.portfolio.model.db;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductServices {

	enum ServiceStatus {
		@JsonProperty("Active")
		active,

		@JsonProperty("Deprecated")
		deprecated;
	}

	private String name;
	private String summary;
	private String path;
	private String verb;
	private String swaggerName;
	private String oasVersion;
	private ServiceStatus serviceStatus;
}
