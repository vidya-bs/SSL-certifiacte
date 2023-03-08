package com.itorix.apiwiz.design.studio.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScmUpload {

	public String swagger;
	private String repoName;
	private String branch;
	private String hostUrl;
	private String folderName;
	private String commitMessage;
	private String token;
	private String scmSource;
	private String swaggerName;
	private String username;
	private String password;
	private String authType;

}
