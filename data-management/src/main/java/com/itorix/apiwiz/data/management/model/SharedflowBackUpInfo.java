package com.itorix.apiwiz.data.management.model;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import net.sf.json.JSONObject;

@Document(collection = "SharedflowBackUpInfo")
public class SharedflowBackUpInfo extends BackupCommon {


	public static final String LABEL_SHAREDFLOW_INFO = "sharedflowInfo";
	public static final String LABEL_OPERATION_ID="operationId";


	private JSONObject sharedflowInfo;
	private String operationId;

	public SharedflowBackUpInfo() {
		super();
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