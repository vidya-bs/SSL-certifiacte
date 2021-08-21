package com.itorix.apiwiz.common.model.integrations.workspace;

public class WorkspaceIntegration {
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
