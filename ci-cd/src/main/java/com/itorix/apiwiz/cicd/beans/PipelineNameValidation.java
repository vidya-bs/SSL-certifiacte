package com.itorix.apiwiz.cicd.beans;

public class PipelineNameValidation {

	private String projectName;
	private String proxyName;
	private String version;
	private String scmBranch;

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProxyName() {
		return proxyName;
	}

	public void setProxyName(String proxyName) {
		this.proxyName = proxyName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getScmBranch() {
		return scmBranch;
	}

	public void setScmBranch(String scmBranch) {
		this.scmBranch = scmBranch;
	}

	public String getPipelineName() {
		return this.projectName + "_" + this.proxyName + "_" + this.version + "_" + this.scmBranch;
	}
}
