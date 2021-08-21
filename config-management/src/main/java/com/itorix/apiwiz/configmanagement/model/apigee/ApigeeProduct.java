package com.itorix.apiwiz.configmanagement.model.apigee;

import java.util.List;

import com.itorix.apiwiz.common.model.configmanagement.ProductAttributes;

public class ApigeeProduct {

	private String name;

	private String org;

	private String displayName;

	private String approvalType;

	private String description;

	private List<String> apiResources;

	private List<String> environments;

	private List<String> proxies;

	private String quota;

	private String quotaInterval;

	private String quotaTimeUnit;

	private List<String> scopes;

	private List<ProductAttributes> attributes;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getApiResources() {
		return apiResources;
	}

	public void setApiResources(List<String> apiResources) {
		this.apiResources = apiResources;
	}

	public List<String> getEnvironments() {
		return environments;
	}

	public void setEnvironments(List<String> environments) {
		this.environments = environments;
	}

	public List<String> getProxies() {
		return proxies;
	}

	public void setProxies(List<String> proxies) {
		this.proxies = proxies;
	}

	public String getQuota() {
		return quota;
	}

	public void setQuota(String quota) {
		this.quota = quota;
	}

	public String getQuotaInterval() {
		return quotaInterval;
	}

	public void setQuotaInterval(String quotaInterval) {
		this.quotaInterval = quotaInterval;
	}

	public String getQuotaTimeUnit() {
		return quotaTimeUnit;
	}

	public void setQuotaTimeUnit(String quotaTimeUnit) {
		this.quotaTimeUnit = quotaTimeUnit;
	}

	public List<String> getScopes() {
		return scopes;
	}

	public void setScopes(List<String> scopes) {
		this.scopes = scopes;
	}

	public List<ProductAttributes> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<ProductAttributes> attributes) {
		this.attributes = attributes;
	}
}
