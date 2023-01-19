package com.itorix.apiwiz.datapower.model.db;

import java.util.List;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itorix.apiwiz.datapower.model.proxy.Proxies;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Projects {

	@Id
	private String id;
	private String name;
	private String summary;
	private String description;
	private String owner;

	@JsonProperty("ownerEmail")
	private String owner_email;

	@JsonProperty("isActive")
	private boolean isActive;

	private String status;

	@JsonProperty("teams")
	List<String> teams;

	@JsonProperty("products")
	List<String> products;

	@JsonProperty("consumers")
	List<String> consumers;

	@JsonProperty("metadata")
	List<Metadata> metadata;

	@JsonProperty("proxies")
	List<Proxies> proxies;
}
