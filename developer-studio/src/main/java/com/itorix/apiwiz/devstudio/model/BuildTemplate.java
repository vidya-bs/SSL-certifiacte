package com.itorix.apiwiz.devstudio.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Connectors.Apigee.Build.Templates.Folder")
public class BuildTemplate {
	private String connectorId;
	private String content;

	private String orgName;

	private String type;
	public BuildTemplate() {
	}
	public BuildTemplate(String connectorId, String content) {
		this.connectorId = connectorId;
		this.content = content;
	}

	public BuildTemplate(String connectorId, String content, String orgName, String type) {
		this.connectorId = connectorId;
		this.content = content;
		this.orgName = orgName;
		this.type = type;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

	public String getConnectorId() {
		return connectorId;
	}

	public void setConnectorId(String connectorId) {
		this.connectorId = connectorId;
	}
}
