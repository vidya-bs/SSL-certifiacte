package com.itorix.apiwiz.datadictionary.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document("Design.Dictionary.Connectors")
public class DictionaryScmUpload {

	@Id
	public String portfolioId;
	public String dictionary;
	private String repoName;
	private String branch;
	private String hostUrl;
	private String folderName;
	private String commitMessage;
	private String token;
	private String scmSource;
	private String dictionaryName;
	private String username;
	private String password;
	private String authType;

}
