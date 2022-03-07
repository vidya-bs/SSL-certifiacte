package com.itorix.apiwiz.data.management.model;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

import net.sf.json.JSONObject;

@Document(collection = "AppBackUpInfo")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppBackUpInfo extends BackupCommon {

	public static final String LABEL_APP_INFO = "appInfo";
	public static final String LABEL_OPERATION_ID = "operationId";

	private JSONObject appInfo;
	private String operationId;

	public AppBackUpInfo() {
		super();
	}

	@JsonProperty("appInfo")
	public JSONObject getAppInfo() {
		return appInfo;
	}

	@JsonProperty("appInfo")
	public void setAppInfo(JSONObject appInfo) {
		this.appInfo = appInfo;
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
