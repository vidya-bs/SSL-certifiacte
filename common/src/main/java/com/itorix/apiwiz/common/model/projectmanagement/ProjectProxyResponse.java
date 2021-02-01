package com.itorix.apiwiz.common.model.projectmanagement;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectProxyResponse {

	private String gitPush;
	private String gitRepoName;
	private String gitBranch;
	private String pipelineCreated;
	private String pipelineName;
	private String configKVM;
	
	public String getGitPush() {
		return gitPush;
	}
	public void setGitPush(String gitPush) {
		this.gitPush = gitPush;
	}
	public String getGitRepoName() {
		return gitRepoName;
	}
	public void setGitRepoName(String gitRepoName) {
		this.gitRepoName = gitRepoName;
	}
	public String getGitBranch() {
		return gitBranch;
	}
	public void setGitBranch(String gitBranch) {
		this.gitBranch = gitBranch;
	}
	public String getPipelineCreated() {
		return pipelineCreated;
	}
	public void setPipelineCreated(String pipelineCreated) {
		this.pipelineCreated = pipelineCreated;
	}
	public String getPipelineName() {
		return pipelineName;
	}
	public void setPipelineName(String pipelineName) {
		this.pipelineName = pipelineName;
	}
	public String getConfigKVM() {
		return configKVM;
	}
	public void setConfigKVM(String configKVM) {
		this.configKVM = configKVM;
	}
}
