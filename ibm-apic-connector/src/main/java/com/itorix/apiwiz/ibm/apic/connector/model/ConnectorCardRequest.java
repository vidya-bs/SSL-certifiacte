package com.itorix.apiwiz.ibm.apic.connector.model;

import java.io.Serializable;

public class ConnectorCardRequest implements Serializable {
	private String orgName;
	private String region;
	private String apiKey;
	private String clientId;
	private String clientSecret;

	public ConnectorCardRequest() {
	}

	public ConnectorCardRequest(String orgName, String region, String apiKey, String clientId, String clientSecret) {
		this.orgName = orgName;
		this.region = region;
		this.apiKey = apiKey;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	}

	public String getOrgName() {
		return orgName;
	}
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getClientSecret() {
		return clientSecret;
	}
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
}
