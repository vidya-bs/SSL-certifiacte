package com.itorix.apiwiz.data.management.model;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Document(collection = "EnvironmentBackUpInfo")
public class EnvironmentBackUpInfo extends BackupCommon {

	public static final String LABEL_APP_INFO = "envProxyInfo";
	public static final String LABEL_RESOURCE_INFO = "resourceInfo";
	public static final String LABEL_OPERATION_ID="operationId";

	private JSONObject envProxyInfo;
	private JSONArray resourceInfo;
	private JSONObject productsInfo;
	private JSONArray appsInfo;
	private String developersInfo;
	private String operationId;

	public EnvironmentBackUpInfo() {
		super();
	}

	@JsonProperty("envProxyInfo")
	public JSONObject getEnvProxyInfo() {
		return envProxyInfo;
	}

	@JsonProperty("envProxyInfo")
	public void setEnvProxyInfo(JSONObject envProxyInfo) {
		this.envProxyInfo = envProxyInfo;
	}

	@JsonProperty("resourceInfo")
	public JSONArray getResourceInfo() {
		return resourceInfo;
	}

	@JsonProperty("resourceInfo")
	public void setResourceInfo(JSONArray resourceInfo) {
		this.resourceInfo = resourceInfo;
	}

	@JsonProperty("productsInfo")
	public JSONObject getProductsInfo() {
		return productsInfo;
	}

	@JsonProperty("productsInfo")
	public void setProductsInfo(JSONObject productsInfo) {
		this.productsInfo = productsInfo;
	}

	@JsonProperty("appsInfo")
	public JSONArray getAppsInfo() {
		return appsInfo;
	}

	@JsonProperty("appsInfo")
	public void setAppsInfo(JSONArray appsInfo) {
		this.appsInfo = appsInfo;
	}

	@JsonProperty("developersInfo")
	public String getDevelopersInfo() {
		return developersInfo;
	}

	@JsonProperty("developersInfo")
	public void setDevelopersInfo(String developersInfo) {
		this.developersInfo = developersInfo;
	}
	
	@JsonProperty("operationId")
	public String getOperationId() {
		return operationId;
	}
	@JsonProperty("operationId")
	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}

}
