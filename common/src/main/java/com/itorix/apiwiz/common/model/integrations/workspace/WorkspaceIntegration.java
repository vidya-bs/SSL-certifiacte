package com.itorix.apiwiz.common.model.integrations.workspace;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Connectors.Workspace.List")
public class WorkspaceIntegration {
	
	@Id
	private String propertyKey;
	private String propertyValue;
	private boolean encrypt = false;

	public boolean getEncrypt() {
		return encrypt;
	}
	public void setEncrypt(boolean encrypt) {
		this.encrypt = encrypt;
	}
	public String getPropertyValue() {
		return propertyValue;
	}
	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}
	public String getPropertyKey() {
		return propertyKey;
	}
	public void setPropertyKey(String propertyKey) {
		this.propertyKey = propertyKey;
	}
}
