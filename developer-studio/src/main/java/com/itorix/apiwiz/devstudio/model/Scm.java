package com.itorix.apiwiz.devstudio.model;

public class Scm {
	private String baseBranch;
    private String destinationBranch;
    private String proxyId;
    private String scmType;
    private String reponame;
    private String branch;
    private String gitURL;
    private String commitMessage;
	public String getBaseBranch() {
		return baseBranch;
	}
	public void setBaseBranch(String baseBranch) {
		this.baseBranch = baseBranch;
	}
	public String getDestinationBranch() {
		return destinationBranch;
	}
	public void setDestinationBranch(String destinationBranch) {
		this.destinationBranch = destinationBranch;
	}
	public String getProxyId() {
		return proxyId;
	}
	public void setProxyId(String proxyId) {
		this.proxyId = proxyId;
	}
	public String getScmType() {
		return scmType;
	}
	public void setScmType(String scmType) {
		this.scmType = scmType;
	}
	public String getReponame() {
		return reponame;
	}
	public void setReponame(String reponame) {
		this.reponame = reponame;
	}
	public String getBranch() {
		return branch;
	}
	public void setBranch(String branch) {
		this.branch = branch;
	}
	public String getGitURL() {
		return gitURL;
	}
	public void setGitURL(String gitURL) {
		this.gitURL = gitURL;
	}
	public String getCommitMessage() {
		return commitMessage;
	}
	public void setCommitMessage(String commitMessage) {
		this.commitMessage = commitMessage;
	}
}
