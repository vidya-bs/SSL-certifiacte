package com.itorix.apiwiz.analytics.model;

import org.json.simple.JSONObject;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

@Component("DashBoardOverview")
@Document(collection = "Apigee.Dashboard")
public class DashBoardOverview {

	public static final String FUNCTION_NAME = "overView";
	public static final String LABEL_DASH_BOARD_FUNCTIONNAME = "dashBoradFunctionName";
	public static final String LABEL_DASH_BOARD_ENVIROMENT = "environment";
	public static final String LABEL_DASH_BOARD_ORGANISATION = "organisation";
	public static final String LABEL_DASH_BOARD_TYPE = "type";

	private JSONObject overviewResponse;

	private String dashBoradFunctionName;

	private String environment;
	private String organisation;
	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getOrganisation() {
		return organisation;
	}

	public void setOrganisation(String organisation) {
		this.organisation = organisation;
	}

	public String getDashBoradFunctionName() {
		return dashBoradFunctionName;
	}

	public void setDashBoradFunctionName(String dashBoradFunctionName) {
		this.dashBoradFunctionName = dashBoradFunctionName;
	}

	public JSONObject getOverviewResponse() {
		return overviewResponse;
	}

	public void setOverviewResponse(JSONObject overviewResponse) {
		this.overviewResponse = overviewResponse;
	}
}
