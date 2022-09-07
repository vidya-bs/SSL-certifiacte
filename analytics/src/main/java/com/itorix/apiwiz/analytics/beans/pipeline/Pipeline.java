package com.itorix.apiwiz.analytics.beans.pipeline;

import com.fasterxml.jackson.annotation.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Component("pipelineDoc")
@Document(collection = "CICD.Pipeline.List")
public class Pipeline {

	@Id
	@JsonProperty("name")
	private String name;

	@JsonProperty("teams")
	private List<String> teams = null;

	@JsonProperty("projectName")
	private String projectName;

	@JsonProperty("portfolioId")
	private String portfolioId;

	@JsonProperty("portfolioName")
	private String portfolioName;

	@JsonProperty("defineName")
	private String defineName;

	@JsonProperty("displayName")
	private String displayName;

	@JsonProperty("proxyName")
	private String proxyName;

	@JsonProperty("version")
	private String version;

	@JsonProperty("type")
	private String type;

	@JsonProperty("stages")
	private List<Stage> stages = null;

	@JsonProperty("status")
	private String status;

	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("teams")
	public List<String> getTeams() {
		return teams;
	}

	@JsonProperty("teams")
	public void setTeams(List<String> teams) {
		this.teams = teams;
	}

	@JsonProperty("displayName")
	public String getDisplayName() {
		return displayName;
	}

	@JsonProperty("displayName")
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@JsonProperty("proxyName")
	public String getProxyName() {
		return proxyName;
	}

	@JsonProperty("proxyName")
	public void setProxyName(String proxyName) {
		this.proxyName = proxyName;
	}

	@JsonProperty("version")
	public String getVersion() {
		return version;
	}

	@JsonProperty("version")
	public void setVersion(String version) {
		this.version = version;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@JsonProperty("stages")
	public List<Stage> getStages() {
		return stages;
	}

	@JsonProperty("stages")
	public void setStages(List<Stage> stages) {
		this.stages = stages;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	@JsonProperty("projectName")
	public String getProjectName() {
		return projectName;
	}

	@JsonProperty("projectName")
	public void setProjectName(String projectName) {
		if (this.defineName == null)
			this.defineName = projectName;
		this.projectName = projectName; // .replaceAll(" " , "-");
	}

	@JsonProperty("defineName")
	public void setDefineName(String defineName) {
		this.defineName = defineName;
	}

	public String getDefineName() {
		return defineName;
	}

	public String getPortfolioId() {
		return portfolioId;
	}

	public void setPortfolioId(String portfolioId) {
		this.portfolioId = portfolioId;
	}

	public String getPortfolioName() {
		return portfolioName;
	}

	public void setPortfolioName(String portfolioName) {
		this.portfolioName = portfolioName;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Pipeline [name=");
		builder.append(name);
		builder.append(", projectName=");
		builder.append(projectName);
		builder.append(", displayName=");
		builder.append(displayName);
		builder.append(", proxyName=");
		builder.append(proxyName);
		builder.append(", version=");
		builder.append(version);
		builder.append(", type=");
		builder.append(type);
		builder.append(", stages=");
		builder.append(stages);
		builder.append(", additionalProperties=");
		builder.append(additionalProperties);
		builder.append("]");
		return builder.toString();
	}
}
