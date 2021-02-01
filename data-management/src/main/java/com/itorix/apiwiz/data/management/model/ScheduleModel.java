package com.itorix.apiwiz.data.management.model;

import com.itorix.apiwiz.identitymanagement.model.AbstractObject;

public class ScheduleModel extends AbstractObject {

	public static final String LABEL_ORGANAIZATION = "organization";
	public static final String LABEL_ENVIRONMENT = "environment";
	public static final String LABEL_PERIODICITY = "periodicity";
	public static final String LABEL_SELECTED_ENVIRONMENTS = "selectedEnvironments";
	public static final String LABEL_JSESSIONID = "jsessionId";

	private String organization;
	private String environment;
	private String periodicity;
	private String jsessionId;
	private String selectedEnvironments;
	private String userId;
	
	public String getOrganization() {
		return organization;
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	public String getEnvironment() {
		return environment;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	public String getPeriodicity() {
		return periodicity;
	}
	public void setPeriodicity(String periodicity) {
		this.periodicity = periodicity;
	}
	public String getJsessionId() {
		return jsessionId;
	}
	public void setJsessionId(String jsessionId) {
		this.jsessionId = jsessionId;
	}
	public String getSelectedEnvironments() {
		return selectedEnvironments;
	}
	public void setSelectedEnvironments(String selectedEnvironments) {
		this.selectedEnvironments = selectedEnvironments;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
}
