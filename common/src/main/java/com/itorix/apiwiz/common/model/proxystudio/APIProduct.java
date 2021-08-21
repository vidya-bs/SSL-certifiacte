package com.itorix.apiwiz.common.model.proxystudio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({"apiResources", "approvalType", "attributes", "createdAt", "createdBy", "description",
		"displayName", "environments", "lastModifiedAt", "lastModifiedBy", "name", "proxies", "quota", "quotaInterval",
		"quotaTimeUnit", "scopes", "apiproduct", "status"})
public class APIProduct {

	@JsonProperty("apiResources")
	private List<Object> apiResources = new ArrayList<Object>();

	@JsonProperty("approvalType")
	private String approvalType;

	@JsonProperty("attributes")
	private List<Attribute> attributes = new ArrayList<Attribute>();

	@JsonProperty("createdAt")
	private String createdAt;

	@JsonProperty("createdBy")
	private String createdBy;

	@JsonProperty("description")
	private String description;

	@JsonProperty("displayName")
	private String displayName;

	@JsonProperty("environments")
	private List<String> environments = new ArrayList<String>();

	@JsonProperty("lastModifiedAt")
	private String lastModifiedAt;

	@JsonProperty("lastModifiedBy")
	private String lastModifiedBy;

	@JsonProperty("name")
	private String name;

	@JsonProperty("proxies")
	private List<Object> proxies = new ArrayList<Object>();

	@JsonProperty("quota")
	private String quota;

	@JsonProperty("quotaInterval")
	private String quotaInterval;

	@JsonProperty("quotaTimeUnit")
	private String quotaTimeUnit;

	@JsonProperty("scopes")
	private List<String> scopes = new ArrayList<String>();

	@JsonProperty("apiproduct")
	private String apiproduct;

	@JsonProperty("status")
	private String status;

	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/** @return The apiResources */
	@JsonProperty("apiResources")
	public List<Object> getApiResources() {
		return apiResources;
	}

	/**
	 * @param apiResources
	 *            The apiResources
	 */
	@JsonProperty("apiResources")
	public void setApiResources(List<Object> apiResources) {
		this.apiResources = apiResources;
	}

	/** @return The approvalType */
	@JsonProperty("approvalType")
	public String getApprovalType() {
		return approvalType;
	}

	/**
	 * @param approvalType
	 *            The approvalType
	 */
	@JsonProperty("approvalType")
	public void setApprovalType(String approvalType) {
		this.approvalType = approvalType;
	}

	/** @return The attributes */
	@JsonProperty("attributes")
	public List<Attribute> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes
	 *            The attributes
	 */
	@JsonProperty("attributes")
	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	/** @return The createdAt */
	@JsonProperty("createdAt")
	public String getCreatedAt() {
		return createdAt;
	}

	/**
	 * @param createdAt
	 *            The createdAt
	 */
	@JsonProperty("createdAt")
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	/** @return The createdBy */
	@JsonProperty("createdBy")
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * @param createdBy
	 *            The createdBy
	 */
	@JsonProperty("createdBy")
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/** @return The description */
	@JsonProperty("description")
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            The description
	 */
	@JsonProperty("description")
	public void setDescription(String description) {
		this.description = description;
	}

	/** @return The displayName */
	@JsonProperty("displayName")
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param displayName
	 *            The displayName
	 */
	@JsonProperty("displayName")
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/** @return The environments */
	@JsonProperty("environments")
	public List<String> getEnvironments() {
		return environments;
	}

	/**
	 * @param environments
	 *            The environments
	 */
	@JsonProperty("environments")
	public void setEnvironments(List<String> environments) {
		this.environments = environments;
	}

	/** @return The lastModifiedAt */
	@JsonProperty("lastModifiedAt")
	public String getLastModifiedAt() {
		return lastModifiedAt;
	}

	/**
	 * @param lastModifiedAt
	 *            The lastModifiedAt
	 */
	@JsonProperty("lastModifiedAt")
	public void setLastModifiedAt(String lastModifiedAt) {
		this.lastModifiedAt = lastModifiedAt;
	}

	/** @return The lastModifiedBy */
	@JsonProperty("lastModifiedBy")
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	/**
	 * @param lastModifiedBy
	 *            The lastModifiedBy
	 */
	@JsonProperty("lastModifiedBy")
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	/** @return The name */
	@JsonProperty("name")
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name
	 */
	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	/** @return The proxies */
	@JsonProperty("proxies")
	public List<Object> getProxies() {
		return proxies;
	}

	/**
	 * @param proxies
	 *            The proxies
	 */
	@JsonProperty("proxies")
	public void setProxies(List<Object> proxies) {
		this.proxies = proxies;
	}

	/** @return The quota */
	@JsonProperty("quota")
	public String getQuota() {
		return quota;
	}

	/**
	 * @param quota
	 *            The quota
	 */
	@JsonProperty("quota")
	public void setQuota(String quota) {
		this.quota = quota;
	}

	/** @return The quotaInterval */
	@JsonProperty("quotaInterval")
	public String getQuotaInterval() {
		return quotaInterval;
	}

	/**
	 * @param quotaInterval
	 *            The quotaInterval
	 */
	@JsonProperty("quotaInterval")
	public void setQuotaInterval(String quotaInterval) {
		this.quotaInterval = quotaInterval;
	}

	/** @return The quotaTimeUnit */
	@JsonProperty("quotaTimeUnit")
	public String getQuotaTimeUnit() {
		return quotaTimeUnit;
	}

	/**
	 * @param quotaTimeUnit
	 *            The quotaTimeUnit
	 */
	@JsonProperty("quotaTimeUnit")
	public void setQuotaTimeUnit(String quotaTimeUnit) {
		this.quotaTimeUnit = quotaTimeUnit;
	}

	/** @return The scopes */
	@JsonProperty("scopes")
	public List<String> getScopes() {
		return scopes;
	}

	/**
	 * @param scopes
	 *            The scopes
	 */
	@JsonProperty("scopes")
	public void setScopes(List<String> scopes) {
		this.scopes = scopes;
	}

	/** @return The apiproduct */
	@JsonProperty("apiproduct")
	public String getApiproduct() {
		return apiproduct;
	}

	/**
	 * @param apiproduct
	 *            The apiproduct
	 */
	@JsonProperty("apiproduct")
	public void setApiproduct(String apiproduct) {
		this.apiproduct = apiproduct;
	}

	/** @return The status */
	@JsonProperty("status")
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            The status
	 */
	@JsonProperty("status")
	public void setStatus(String status) {
		this.status = status;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}
}
