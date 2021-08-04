package com.itorix.apiwiz.data.management.model;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import net.sf.json.JSONObject;

@Document(collection = "ProductsBackUpInfo")
public class ProductsBackUpInfo extends BackupCommon {

	public static final String LABEL_PRODUCT_INFO = "productInfo";
	public static final String LABEL_APPS_INFO = "appsInfo";
	public static final String LABEL_OPERATION_ID = "operationId";

	private JSONObject productInfo;
	private JSONObject appsInfo;
	private JSONObject developersInfo;
	private String operationId;

	public ProductsBackUpInfo() {
		super();
	}

	@JsonProperty("productInfo")
	public JSONObject getProductInfo() {
		return productInfo;
	}

	@JsonProperty("productInfo")
	public void setProductInfo(JSONObject productInfo) {
		this.productInfo = productInfo;
	}

	@JsonProperty("appsInfo")
	public JSONObject getAppsInfo() {
		return appsInfo;
	}

	@JsonProperty("appsInfo")
	public void setAppsInfo(JSONObject appsInfo) {
		this.appsInfo = appsInfo;
	}

	@JsonProperty("developersInfo")
	public JSONObject getDevelopersInfo() {
		return developersInfo;
	}

	@JsonProperty("developersInfo")
	public void setDevelopersInfo(JSONObject developersInfo) {
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
