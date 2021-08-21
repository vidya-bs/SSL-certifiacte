package com.itorix.apiwiz.portfolio.model;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itorix.apiwiz.common.model.AbstractObject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PortfolioRequest extends AbstractObject {
	@JsonProperty("name")
	String name;

	@JsonProperty("summary")
	String summary;

	@JsonProperty("description")
	String description;

	@JsonProperty("owner")
	String owner;

	String ownerEmail;
}
