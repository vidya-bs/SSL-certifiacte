package com.itorix.apiwiz.data.management.model;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

import net.sf.json.JSONObject;

@Document(collection = "DeveloperBackUpInfo")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeveloperBackUpInfo extends BackupCommon {

	public static final String LABEL_APP_INFO = "developerInfo";
	public static final String LABEL_OPERATION_ID = "operationId";

	private JSONObject developerInfo;
	private JSONObject appsInfo;
	private String operationId;

	public DeveloperBackUpInfo() {
		super();
	}

	@JsonProperty("developerInfo")
	public JSONObject getDeveloperInfo() {
		return developerInfo;
	}

	@JsonProperty("developerInfo")
	public void setDeveloperInfo(JSONObject developerInfo) {
		this.developerInfo = developerInfo;
	}

	@JsonProperty("appsInfo")
	public JSONObject getAppsInfo() {
		return appsInfo;
	}

	@JsonProperty("appsInfo")
	public void setAppsInfo(JSONObject appsInfo) {
		this.appsInfo = appsInfo;
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
