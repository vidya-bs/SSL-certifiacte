package com.itorix.apiwiz.data.management.model;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import net.sf.json.JSONArray;

@Document(collection = "ResourceBackUpInfo")
public class ResourceBackUpInfo extends BackupCommon {

	public static final String LABEL_APP_INFO = "resourceInfo";
	public static final String LABEL_RESOURCE_TYPE = "resourceType";
	public static final String LABEL_OPERATION_ID="operationId";

	private JSONArray resourceInfo;
	private String resourceType;
	private String operationId;

	public ResourceBackUpInfo() {
		super();
	}

	@JsonProperty("resourceInfo")
	public JSONArray getResourceInfo() {
		return resourceInfo;
	}

	@JsonProperty("resourceInfo")
	public void setResourceInfo(JSONArray resourceInfo) {
		this.resourceInfo = resourceInfo;
	}

	@JsonProperty("resourceType")
	public String getResourceType() {
		return resourceType;
	}

	@JsonProperty("resourceType")
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
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