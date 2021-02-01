package com.itorix.apiwiz.identitymanagement.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Users.Plan.List")
public class Plan {
	@Id
	private String planId;
	private String uiPermissions;
	public String getPlanId() {
		return planId;
	}
	public void setPlanId(String planId) {
		this.planId = planId;
	}
	@JsonIgnore
	public String getUiPermissions() {
		return uiPermissions;
	}
	public void setUiPermissions(String uiPermissions) {
		this.uiPermissions = uiPermissions;
	}
}