package com.itorix.apiwiz.projectmanagement.model.cicd;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "orgName", "envName", "sequenceID", "unitTests", "codeCoverage" })
public class Stage {

	@JsonProperty("name")
	private String name;
	@JsonProperty("orgName")
	private String orgName;
	@JsonProperty("envName")
	private String envName;
	@JsonProperty("sequenceID")
	private Integer sequenceID;
	@JsonProperty("unitTests")
	private UnitTests unitTests;
	@JsonProperty("codeCoverage")
	private CodeCoverage codeCoverage;
	@JsonProperty("type")
	private String type;
	@JsonProperty("isSaaS")
	private Boolean isSaas;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("orgName")
	public String getOrgName() {
		return orgName;
	}

	@JsonProperty("orgName")
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	@JsonProperty("envName")
	public String getEnvName() {
		return envName;
	}

	@JsonProperty("envName")
	public void setEnvName(String envName) {
		this.envName = envName;
	}

	@JsonProperty("sequenceID")
	public Integer getSequenceID() {
		return sequenceID;
	}

	@JsonProperty("sequenceID")
	public void setSequenceID(Integer sequenceID) {
		this.sequenceID = sequenceID;
	}

	@JsonProperty("unitTests")
	public UnitTests getUnitTests() {
		return unitTests;
	}

	@JsonProperty("unitTests")
	public void setUnitTests(UnitTests unitTests) {
		this.unitTests = unitTests;
	}

	@JsonProperty("codeCoverage")
	public CodeCoverage getCodeCoverage() {
		return codeCoverage;
	}

	@JsonProperty("codeCoverage")
	public void setCodeCoverage(CodeCoverage codeCoverage) {
		this.codeCoverage = codeCoverage;
	}

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getIsSaas() {
		return isSaas;
	}

	public void setIsSaas(Boolean isSaas) {
		this.isSaas = isSaas;
	}
}
