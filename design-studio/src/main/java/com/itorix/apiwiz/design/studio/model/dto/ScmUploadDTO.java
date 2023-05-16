package com.itorix.apiwiz.design.studio.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.design.studio.model.AsyncApi;
import com.itorix.apiwiz.design.studio.model.GraphQL;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScmUploadDTO {
	private boolean enableScm;
	private String repoName;
	private String branch;
	private String hostUrl;
	private String folderName;
	private String token;
	private String commitMessage;
	private String scmSource;
	private String username;
	private String password;
	private String authType;
	private String name;
	private int revision;

	public ScmUploadDTO() {}
	public ScmUploadDTO(AsyncApi asyncApi) {
		this.enableScm = asyncApi.isEnableScm();
		this.repoName = asyncApi.getRepoName();
		this.branch = asyncApi.getBranch();
		this.hostUrl = asyncApi.getHostUrl();
		this.folderName = asyncApi.getFolderName();
		this.token = asyncApi.getToken();
		this.scmSource = asyncApi.getScmSource();
		this.commitMessage = asyncApi.getCommitMessage();
		this.username = asyncApi.getUsername();
		this.password = asyncApi.getPassword();
		this.authType = asyncApi.getAuthType();
		this.name = asyncApi.getName();
		this.revision = asyncApi.getRevision();
	}
	public ScmUploadDTO(GraphQL graphQl) {
		this.enableScm = graphQl.isEnableScm();
		this.repoName = graphQl.getRepoName();
		this.branch = graphQl.getBranch();
		this.hostUrl = graphQl.getHostUrl();
		this.folderName = graphQl.getFolderName();
		this.token = graphQl.getToken();
		this.scmSource = graphQl.getScmSource();
		this.commitMessage = graphQl.getCommitMessage();
		this.username = graphQl.getUsername();
		this.password = graphQl.getPassword();
		this.authType = graphQl.getAuthType();
		this.name = graphQl.getName();
		this.revision = graphQl.getRevision();
	}
}
