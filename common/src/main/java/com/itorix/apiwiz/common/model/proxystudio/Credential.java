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
@JsonPropertyOrder({"apiProducts", "attributes", "consumerKey", "consumerSecret", "expiresAt", "issuedAt", "scopes",
		"status"})
public class Credential {

	@JsonProperty("apiProducts")
	private List<APIProduct> apiProducts = new ArrayList<APIProduct>();

	@JsonProperty("attributes")
	private List<Object> attributes = new ArrayList<Object>();

	@JsonProperty("consumerKey")
	private String consumerKey;

	@JsonProperty("consumerSecret")
	private String consumerSecret;

	@JsonProperty("expiresAt")
	private String expiresAt;

	@JsonProperty("issuedAt")
	private String issuedAt;

	@JsonProperty("scopes")
	private List<Object> scopes = new ArrayList<Object>();

	@JsonProperty("status")
	private String status;

	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/** @return The apiProducts */
	@JsonProperty("apiProducts")
	public List<APIProduct> getApiProducts() {
		return apiProducts;
	}

	/**
	 * @param apiProducts
	 *            The apiProducts
	 */
	@JsonProperty("apiProducts")
	public void setApiProducts(List<APIProduct> apiProducts) {
		this.apiProducts = apiProducts;
	}

	/** @return The attributes */
	@JsonProperty("attributes")
	public List<Object> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes
	 *            The attributes
	 */
	@JsonProperty("attributes")
	public void setAttributes(List<Object> attributes) {
		this.attributes = attributes;
	}

	/** @return The consumerKey */
	@JsonProperty("consumerKey")
	public String getConsumerKey() {
		return consumerKey;
	}

	/**
	 * @param consumerKey
	 *            The consumerKey
	 */
	@JsonProperty("consumerKey")
	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	/** @return The consumerSecret */
	@JsonProperty("consumerSecret")
	public String getConsumerSecret() {
		return consumerSecret;
	}

	/**
	 * @param consumerSecret
	 *            The consumerSecret
	 */
	@JsonProperty("consumerSecret")
	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	/** @return The expiresAt */
	@JsonProperty("expiresAt")
	public String getExpiresAt() {
		return expiresAt;
	}

	/**
	 * @param expiresAt
	 *            The expiresAt
	 */
	@JsonProperty("expiresAt")
	public void setExpiresAt(String expiresAt) {
		this.expiresAt = expiresAt;
	}

	/** @return The issuedAt */
	@JsonProperty("issuedAt")
	public String getIssuedAt() {
		return issuedAt;
	}

	/**
	 * @param issuedAt
	 *            The issuedAt
	 */
	@JsonProperty("issuedAt")
	public void setIssuedAt(String issuedAt) {
		this.issuedAt = issuedAt;
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
