package com.itorix.apiwiz.common.model.apigeeX;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.model.AbstractObject;
import com.itorix.apiwiz.common.model.configmanagement.ProductAttributes;

@Component("productXConfig")
@Document(collection = "Connectors.ApigeeX.Configure.Products.List")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProductConfig extends AbstractObject{
	private String name;

	private String org;

	private String type = "saas";

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

	private boolean activeFlag;

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public boolean isActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(boolean activeFlag) {
		this.activeFlag = activeFlag;
	}
	
}
