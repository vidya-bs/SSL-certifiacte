package com.itorix.apiwiz.cicd.dashboard.beans;

import org.springframework.data.mongodb.core.mapping.Document;

import com.itorix.apiwiz.identitymanagement.model.AbstractObject;

@Document(collection = "Apigee.PipeLineDashBoard")
public class PipeLineDashBoard extends AbstractObject {

	public static final String FUNCTION_NAME = "pipeLineResponse";
	public static final String LABEL_DASH_BOARD_FUNCTIONNAME = "dashBoradFunctionName";
	private String dashBoradFunctionName;

	private CicdDashBoardResponse cicdDashBoardResponse;

	public CicdDashBoardResponse getCicdDashBoardResponse() {
		return cicdDashBoardResponse;
	}

	public void setCicdDashBoardResponse(CicdDashBoardResponse cicdDashBoardResponse) {
		this.cicdDashBoardResponse = cicdDashBoardResponse;
	}

	public String getDashBoradFunctionName() {
		return dashBoradFunctionName;
	}

	public void setDashBoradFunctionName(String dashBoradFunctionName) {
		this.dashBoradFunctionName = dashBoradFunctionName;
	}
}
