package com.itorix.apiwiz.ibm.apic.connector.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Connectors.IBM.APIC.SpecSchedule.List")
public class IBMAPICSpecSchedule{
	@Id
	private String id;
	private String orgName;
	private String status;

	public IBMAPICSpecSchedule() {
	}
	public IBMAPICSpecSchedule(String id, String orgName, String status) {
		this.id = id;
		this.orgName = orgName;
		this.status = status;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getOrgName() {
		return orgName;
	}
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
