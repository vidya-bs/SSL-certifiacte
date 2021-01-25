package com.itorix.mockserver.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

@Document(collection = "Users.Workspace.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Workspace {
	public Workspace() {
		// TODO Auto-generated constructor stub
	}
	@Id
	private String name;
	private String planId;
	private String status;
	private String activeTill;
	private String tenant;
	private String key;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPlanId() {
		return planId;
	}
	public void setPlanId(String planId) {
		this.planId = planId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getActiveTill() {
		return activeTill;
	}
	public void setActiveTill(String activeTill) {
		this.activeTill = activeTill;
	}
	public String getTenant() {
		return tenant;
	}
	public void setTenant(String tenant) {
		this.tenant = tenant;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
}
