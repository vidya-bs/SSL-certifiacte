package com.itorix.apiwiz.datapower.model.proxy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class CodeCoverage implements Serializable {
	private String enabled;

	private int acceptance;

	private String testArtifactType;

	private List<TestArtifact> testArtifact;

	private String artifactType;

	@JsonProperty("testsuites")
	private List<Testsuites> testsuites;
}
