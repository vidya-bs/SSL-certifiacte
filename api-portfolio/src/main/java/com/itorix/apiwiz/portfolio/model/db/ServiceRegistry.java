package com.itorix.apiwiz.portfolio.model.db;

import java.util.List;

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
public class ServiceRegistry {

	enum Schemes {
		http, https;
	}

	enum ServiceStatus {
		@JsonProperty("Active")
		active,

		@JsonProperty("Deprecated")
		deprecated;
	}

	private String id;
	private String name;
	private String summary;
	private String description;
	private String path;
	private String verb;
	private String swaggerName;
	private String oasVersion;
	private ServiceStatus serviceStatus;
	private Schemes schemes;
	private List<Environments> environments;
	private List<Metadata> metadata;
}
