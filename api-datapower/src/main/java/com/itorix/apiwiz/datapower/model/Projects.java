package com.itorix.apiwiz.datapower.model;

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

	private String id;

	@JsonProperty("proxies")
	Proxies proxies;
}
