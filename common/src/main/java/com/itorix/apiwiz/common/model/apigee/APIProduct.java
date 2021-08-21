package com.itorix.apiwiz.common.model.apigee;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIProduct {
	private String[] apiResources;

	private String quotaTimeUnit;

	private String[] environments;

	private String[] scopes;

	private String quota;

	private String createdBy;

	private String createdAt;

	private String description;

	private String name;

	private String lastModifiedBy;

	private String quotaInterval;

	private Attributes[] attributes;

	private String displayName;

	private String approvalType;

	private String[] proxies;

	private String lastModifiedAt;

	public String[] getApiResources() {
		return apiResources;
	}

	public void setApiResources(String[] apiResources) {
		this.apiResources = apiResources;
	}

	public String getQuotaTimeUnit() {
		return quotaTimeUnit;
	}

	public void setQuotaTimeUnit(String quotaTimeUnit) {
		this.quotaTimeUnit = quotaTimeUnit;
	}

	public String[] getEnvironments() {
		return environments;
	}

	public void setEnvironments(String[] environments) {
		this.environments = environments;
	}

	public String[] getScopes() {
		return scopes;
	}

	public void setScopes(String[] scopes) {
		this.scopes = scopes;
	}

	public String getQuota() {
		return quota;
	}

	public void setQuota(String quota) {
		this.quota = quota;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public String getQuotaInterval() {
		return quotaInterval;
	}

	public void setQuotaInterval(String quotaInterval) {
		this.quotaInterval = quotaInterval;
	}

	public Attributes[] getAttributes() {
		return attributes;
	}

	public void setAttributes(Attributes[] attributes) {
		this.attributes = attributes;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getApprovalType() {
		return approvalType;
	}

	public void setApprovalType(String approvalType) {
		this.approvalType = approvalType;
	}

	public String[] getProxies() {
		return proxies;
	}

	public void setProxies(String[] proxies) {
		this.proxies = proxies;
	}

	public String getLastModifiedAt() {
		return lastModifiedAt;
	}

	public void setLastModifiedAt(String lastModifiedAt) {
		this.lastModifiedAt = lastModifiedAt;
	}

	@Override
	public String toString() {
		return "ClassPojo [apiResources = " + apiResources + ", quotaTimeUnit = " + quotaTimeUnit + ", environments = "
				+ environments + ", scopes = " + scopes + ", quota = " + quota + ", createdBy = " + createdBy
				+ ", createdAt = " + createdAt + ", description = " + description + ", name = " + name
				+ ", lastModifiedBy = " + lastModifiedBy + ", quotaInterval = " + quotaInterval + ", attributes = "
				+ attributes + ", displayName = " + displayName + ", approvalType = " + approvalType + ", proxies = "
				+ proxies + ", lastModifiedAt = " + lastModifiedAt + "]";
	}
}
