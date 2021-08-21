package com.itorix.apiwiz.analytics.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.identitymanagement.model.AbstractObject;
import com.itorix.apiwiz.identitymanagement.model.Apigee;

@Component("DashBoardSetUp")
@Document(collection = "Apigee.DashboardSetup")
public class DashBoardSetUp extends AbstractObject {

	public static final String DASH_BOARD_SETUP = "dashBoardSetUpDetails";
	public static final String DASH_BOARD_FUNCTION_NAME = "dashBoardSetUpDetails";

	private Apigee apigee;
	private String dashBoardSetUpDetails;

	public String getDashBoardSetUpDetails() {
		return dashBoardSetUpDetails;
	}

	public void setDashBoardSetUpDetails(String dashBoardSetUpDetails) {
		this.dashBoardSetUpDetails = dashBoardSetUpDetails;
	}

	public Apigee getApigee() {
		return apigee;
	}

	public void setApigee(Apigee apigee) {
		this.apigee = apigee;
	}
}
