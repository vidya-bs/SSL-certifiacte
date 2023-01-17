package com.itorix.apiwiz.datapower.model.proxy;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class CodeCoverage {
	private String enabled;

	private int acceptance;

	private String testArtifactType;

	private List<TestArtifact> testArtifact;

	private String artifactType;

	@JsonProperty("testsuites")
	private List<Testsuites> testsuites;
}
