package com.itorix.apiwiz.common.model.ibmapic;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Document("Connectors.IBM.APIC.PolicyMapping.List")
public class PolicyMappingItem implements Serializable {

	@Id
	private String id;

	private String connectorId;
	private String ibmPolicyName;
	private String apigeePolicyName;

	public PolicyMappingItem() {
	}

	public PolicyMappingItem(String id,String connectorId,String ibmPolicyName, String apigeePolicyName) {
		this.id = id;
		this.connectorId = connectorId;
		this.ibmPolicyName = ibmPolicyName;
		this.apigeePolicyName = apigeePolicyName;
	}

	public String getId() {
		return id;
	}
	public String getConnectorId() {
		return connectorId;
	}
	public void setConnectorId(String connectorId) {
		this.connectorId = connectorId;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getIbmPolicyName() {
		return ibmPolicyName;
	}
	public void setIbmPolicyName(String ibmPolicyName) {
		this.ibmPolicyName = ibmPolicyName;
	}
	public String getApigeePolicyName() {
		return apigeePolicyName;
	}
	public void setApigeePolicyName(String apigeePolicyName) {
		this.apigeePolicyName = apigeePolicyName;
	}
}
