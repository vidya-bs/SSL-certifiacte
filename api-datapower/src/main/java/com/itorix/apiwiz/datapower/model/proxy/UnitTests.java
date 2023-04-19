package com.itorix.apiwiz.datapower.model.proxy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class UnitTests implements Serializable {
	private String enabled;

	private int acceptance;

	private String testArtifactType;
	private String artifactType;
	private List<TestArtifact> testArtifact;

	@JsonProperty("testsuites")
	private List<Testsuites> testsuites;
}
