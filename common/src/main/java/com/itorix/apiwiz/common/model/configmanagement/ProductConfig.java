package com.itorix.apiwiz.common.model.configmanagement;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;


@Component("ProductConfig")
@Document(collection = "Connectors.Apigee.Configure.Products.List")
public class ProductConfig {

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String createdUser;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String modifiedUser;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String createdDate;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String modifiedDate;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String name;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String org;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String type="saas";
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String displayName;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String approvalType;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String description;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<String> apiResources;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<String> environments;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<String> proxies;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String quota;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String quotaInterval;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String quotaTimeUnit;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<String> scopes;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<ProductAttributes> attributes;

	private boolean activeFlag;
	
	private String _id;
	
	
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	
	public boolean isActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(boolean activeFlag) {
		this.activeFlag = activeFlag;
	}

	public String getCreatedUser() {
		return createdUser;
	}

	public void setCreatedUser(String createdUser) {
		this.createdUser = createdUser;
	}

	public String getModifiedUser() {
		return modifiedUser;
	}

	public void setModifiedUser(String modifiedUser) {
		this.modifiedUser = modifiedUser;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(String modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

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
	
	
	public ConfigMetadata getMetadata(){
		ConfigMetadata metadata = new ConfigMetadata();
		metadata.setName(this.name);
		metadata.setCreatedUser(this.createdUser);
		metadata.setCreatedDate(this.createdDate);
		metadata.setModifiedUser(this.modifiedUser);
		metadata.setModifiedDate(this.modifiedDate);
		return metadata;
	}
	
}
