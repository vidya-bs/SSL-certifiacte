package com.itorix.apiwiz.common.model.proxystudio;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Proxy {
	public static final String path_seperator = "$#$";

	private String basePath;

	private String name;

	private Flows flows;

	private String description;

	private String oas;

	private String buildProxyArtifact;

	private String buildProxyArtifactType;

	private String version;

	private String revision;

	private String branchType;

	private String isMaster = "false";

	private String swaggerId;

	public String getSwaggerId() {
		return swaggerId;
	}

	public void setSwaggerId(String swaggerId) {
		this.swaggerId = swaggerId;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Flows getFlows() {
		return flows;
	}

	public void setFlows(Flows flows) {
		this.flows = flows;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBuildProxyArtifact() {
		return buildProxyArtifact;
	}

	public void setBuildProxyArtifact(String buildProxyArtifact) {
		this.buildProxyArtifact = buildProxyArtifact;
	}

	public String getBuildProxyArtifactType() {
		return buildProxyArtifactType;
	}

	public void setBuildProxyArtifactType(String buildProxyArtifactType) {
		this.buildProxyArtifactType = buildProxyArtifactType;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getBranchType() {
		return branchType;
	}

	public void setBranchType(String branchType) {
		this.branchType = branchType;
	}

	public String getIsMaster() {
		return isMaster;
	}

	public void setIsMaster(String isMaster) {
		this.isMaster = isMaster;
	}

	@Override
	public String toString() {
		return "Proxy [basePath=" + basePath + ", name=" + name + ", flows=" + flows + ", description=" + description
				+ ", buildProxyArtifact=" + buildProxyArtifact + ", buildProxyArtifactType=" + buildProxyArtifactType
				+ ", version=" + version + ", revision=" + revision + ", branchType=" + branchType + ", isMaster="
				+ isMaster + "]";
	}

	public String getOas() {
		return oas;
	}

	public void setOas(String oas) {
		this.oas = oas;
	}
}
