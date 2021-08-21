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
@JsonPropertyOrder({"accessType", "apiProducts", "appFamily", "appId", "attributes", "callbackUrl", "companyName",
		"createdAt", "createdBy", "credentials", "developerId", "lastModifiedAt", "lastModifiedBy", "name", "scopes",
		"status"})
public class App {

	@JsonProperty("accessType")
	private String accessType;

	@JsonProperty("apiProducts")
	private List<Object> apiProducts = new ArrayList<Object>();

	@JsonProperty("appFamily")
	private String appFamily;

	@JsonProperty("appId")
	private String appId;

	@JsonProperty("attributes")
	private List<Attribute> attributes = new ArrayList<Attribute>();

	@JsonProperty("callbackUrl")
	private String callbackUrl;

	@JsonProperty("companyName")
	private String companyName;

	@JsonProperty("createdAt")
	private String createdAt;

	@JsonProperty("createdBy")
	private String createdBy;

	@JsonProperty("credentials")
	private List<Credential> credentials = new ArrayList<Credential>();

	@JsonProperty("developerId")
	private String developerId;

	@JsonProperty("lastModifiedAt")
	private String lastModifiedAt;

	@JsonProperty("lastModifiedBy")
	private String lastModifiedBy;

	@JsonProperty("name")
	private String name;

	@JsonProperty("scopes")
	private List<Object> scopes = new ArrayList<Object>();

	@JsonProperty("status")
	private String status;

	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/** @return The accessType */
	@JsonProperty("accessType")
	public String getAccessType() {
		return accessType;
	}

	/**
	 * @param accessType
	 *            The accessType
	 */
	@JsonProperty("accessType")
	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}

	/** @return The apiProducts */
	@JsonProperty("apiProducts")
	public List<Object> getApiProducts() {
		return apiProducts;
	}

	/**
	 * @param apiProducts
	 *            The apiProducts
	 */
	@JsonProperty("apiProducts")
	public void setApiProducts(List<Object> apiProducts) {
		this.apiProducts = apiProducts;
	}

	/** @return The appFamily */
	@JsonProperty("appFamily")
	public String getAppFamily() {
		return appFamily;
	}

	/**
	 * @param appFamily
	 *            The appFamily
	 */
	@JsonProperty("appFamily")
	public void setAppFamily(String appFamily) {
		this.appFamily = appFamily;
	}

	/** @return The appId */
	@JsonProperty("appId")
	public String getAppId() {
		return appId;
	}

	/**
	 * @param appId
	 *            The appId
	 */
	@JsonProperty("appId")
	public void setAppId(String appId) {
		this.appId = appId;
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

	/** @return The companyName */
	@JsonProperty("companyName")
	public String getCompanyNamel() {
		return companyName;
	}

	/**
	 * @param companyName
	 *            The companyName
	 */
	@JsonProperty("companyName")
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	/** @return The callbackUrl */
	@JsonProperty("callbackUrl")
	public String getCallbackUrl() {
		return callbackUrl;
	}

	/**
	 * @param callbackUrl
	 *            The callbackUrl
	 */
	@JsonProperty("callbackUrl")
	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
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

	/** @return The credentials */
	@JsonProperty("credentials")
	public List<Credential> getCredentials() {
		return credentials;
	}

	/**
	 * @param credentials
	 *            The credentials
	 */
	@JsonProperty("credentials")
	public void setCredentials(List<Credential> credentials) {
		this.credentials = credentials;
	}

	/** @return The developerId */
	@JsonProperty("developerId")
	public String getDeveloperId() {
		return developerId;
	}

	/**
	 * @param developerId
	 *            The developerId
	 */
	@JsonProperty("developerId")
	public void setDeveloperId(String developerId) {
		this.developerId = developerId;
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

	/** @return The scopes */
	@JsonProperty("scopes")
	public List<Object> getScopes() {
		return scopes;
	}

	/**
	 * @param scopes
	 *            The scopes
	 */
	@JsonProperty("scopes")
	public void setScopes(List<Object> scopes) {
		this.scopes = scopes;
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
