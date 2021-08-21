package com.itorix.apiwiz.common.model.apigee;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeveloperApp {
	private String[] scopes;

	private String callbackUrl;

	private String developerId;

	private String appId;

	private String createdBy;

	private String status;

	private String createdAt;

	private String name;

	private String lastModifiedBy;

	private String appFamily;

	private String[] credentials;

	private Attributes[] attributes;

	private String lastModifiedAt;

	public String[] getScopes() {
		return scopes;
	}

	public void setScopes(String[] scopes) {
		this.scopes = scopes;
	}

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}

	public String getDeveloperId() {
		return developerId;
	}

	public void setDeveloperId(String developerId) {
		this.developerId = developerId;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public String getAppFamily() {
		return appFamily;
	}

	public void setAppFamily(String appFamily) {
		this.appFamily = appFamily;
	}

	public String[] getCredentials() {
		return credentials;
	}

	public void setCredentials(String[] credentials) {
		this.credentials = credentials;
	}

	public Attributes[] getAttributes() {
		return attributes;
	}

	public void setAttributes(Attributes[] attributes) {
		this.attributes = attributes;
	}

	public String getLastModifiedAt() {
		return lastModifiedAt;
	}

	public void setLastModifiedAt(String lastModifiedAt) {
		this.lastModifiedAt = lastModifiedAt;
	}

	@Override
	public String toString() {
		return "ClassPojo [scopes = " + scopes + ", callbackUrl = " + callbackUrl + ", developerId = " + developerId
				+ ", appId = " + appId + ", createdBy = " + createdBy + ", status = " + status + ", createdAt = "
				+ createdAt + ", name = " + name + ", lastModifiedBy = " + lastModifiedBy + ", appFamily = " + appFamily
				+ ", credentials = " + credentials + ", attributes = " + attributes + ", lastModifiedAt = "
				+ lastModifiedAt + "]";
	}
}
