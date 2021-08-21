package com.itorix.apiwiz.common.model.proxystudio;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProxySCMDetails {

	private String reponame;

	private String username;

	private String password;

	private String branch;

	private String hostUrl;

	private String scmSource;

	private String commitMessage;

	public String getCommitMessage() {
		return commitMessage;
	}

	public void setCommitMessage(String commitMessage) {
		this.commitMessage = commitMessage;
	}

	public String getScmSource() {
		return scmSource;
	}

	public void setScmSource(String scmSource) {
		this.scmSource = scmSource;
	}

	public String getHostUrl() {
		return hostUrl;
	}

	public void setHostUrl(String hostUrl) {
		this.hostUrl = hostUrl;
	}

	public String getReponame() {
		return reponame;
	}

	public void setReponame(String reponame) {
		this.reponame = reponame;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}
}
