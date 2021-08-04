package com.itorix.apiwiz.analytics.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.identitymanagement.model.AbstractObject;

@Component("DashBoardEnvironments")
@Document(collection = "Apigee.DashBoardEnvironments")
public class DashBoardEnvironments extends AbstractObject {

	public static final String LABEL_DASH_BOARD_ENV_ORGANISATION = "org";

	private String org;
	private List<String> environments;

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public List<String> getEnvironments() {
		return environments;
	}

	public void setEnvironments(List<String> environments) {
		this.environments = environments;
	}
}
