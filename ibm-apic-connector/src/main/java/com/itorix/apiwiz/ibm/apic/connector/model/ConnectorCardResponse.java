package com.itorix.apiwiz.ibm.apic.connector.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;


@Document("Connectors.IBM.APIC.Runtime.List")
public class ConnectorCardResponse extends ConnectorCardRequest implements Serializable {

	@Id
	@JsonProperty("_id")
	private String id;

	private long cts;
	private long mts;
	private String createdBy;
	private String modifiedBy;

	public ConnectorCardResponse(){

	}

	public ConnectorCardResponse(String id, long cts, long mts, String createdBy, String modifiedBy) {
		this.id = id;
		this.cts = cts;
		this.mts = mts;
		this.createdBy = createdBy;
		this.modifiedBy = modifiedBy;
	}
	public ConnectorCardResponse(String orgName, String region, String apiKey, String clientId, String clientSecret,
			String id, long cts, long mts, String createdBy, String modifiedBy) {
		super(orgName, region, apiKey, clientId, clientSecret);
		this.id = id;
		this.cts = cts;
		this.mts = mts;
		this.createdBy = createdBy;
		this.modifiedBy = modifiedBy;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public long getCts() {
		return cts;
	}
	public void setCts(long cts) {
		this.cts = cts;
	}
	public long getMts() {
		return mts;
	}
	public void setMts(long mts) {
		this.mts = mts;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
}
