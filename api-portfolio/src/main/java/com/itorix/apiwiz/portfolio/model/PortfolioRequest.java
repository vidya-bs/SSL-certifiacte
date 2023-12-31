package com.itorix.apiwiz.portfolio.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itorix.apiwiz.common.model.AbstractObject;
import com.itorix.apiwiz.portfolio.model.db.Testsuite;
import com.itorix.apiwiz.portfolio.model.db.TestsuiteEnvironment;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

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

	@JsonProperty("testsuites")
	List<Testsuite> testsuites;

	@JsonProperty("testsuiteEnvironments")
	List<TestsuiteEnvironment> testsuiteEnvironments;
}
