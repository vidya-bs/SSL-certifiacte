package com.itorix.apiwiz.devstudio.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Connectors.Apigee.Build.Templates.Folder")
public class BuildTemplate {
	private String content;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	
}
