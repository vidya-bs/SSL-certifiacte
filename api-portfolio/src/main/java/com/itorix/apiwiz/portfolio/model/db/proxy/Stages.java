package com.itorix.apiwiz.portfolio.model.db.proxy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class Stages {
	private String orgId;

	private String orgName;

	private String envName;

	private int sequenceID;

	private UnitTests unitTests;

	private CodeCoverage codeCoverage;

	private String name;

	private String type;

	@JsonProperty("isSaaS")
	private boolean isSaaS;
}
