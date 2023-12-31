package com.itorix.apiwiz.data.management.model;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

import net.sf.json.JSONObject;

@Document(collection = "DMB.Proxy")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProxyBackUpInfo extends BackupCommon {

	public static final String LABEL_PROXY_INFO = "proxyInfo";
	public static final String LABEL_DEVELOPERS_INFO = "developersInfo";
	public static final String LABEL_PRODUCTS_INFO = "productsInfo";
	public static final String LABEL_APPS_INFO = "appsInfo";
	public static final String LABEL_SHAREDFLOW_INFO = "sharedflowInfo";
	public static final String LABEL_OPERATION_ID = "operationId";

	private JSONObject proxyInfo;
	private JSONObject developersInfo;
	private JSONObject productsInfo;
	private JSONObject appsInfo;
	private JSONObject sharedflowInfo;
	private String operationId;

	public ProxyBackUpInfo() {
		super();
	}

	@JsonProperty("proxyInfo")
	public JSONObject getProxyInfo() {
		return proxyInfo;
	}

	@JsonProperty("proxyInfo")
	public void setProxyInfo(JSONObject proxyInfo) {
		this.proxyInfo = proxyInfo;
	}

	@JsonProperty("developersInfo")
	public JSONObject getDevelopersInfo() {
		return developersInfo;
	}

	@JsonProperty("developersInfo")
	public void setDevelopersInfo(JSONObject developersInfo) {
		this.developersInfo = developersInfo;
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
	public JSONObject getAppsInfo() {
		return appsInfo;
	}

	@JsonProperty("appsInfo")
	public void setAppsInfo(JSONObject appsInfo) {
		this.appsInfo = appsInfo;
	}

	@JsonProperty("sharedflowInfo")
	public JSONObject getSharedflowInfo() {
		return sharedflowInfo;
	}

	@JsonProperty("sharedflowInfo")
	public void setSharedflowInfo(JSONObject sharedflowInfo) {
		this.sharedflowInfo = sharedflowInfo;
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
