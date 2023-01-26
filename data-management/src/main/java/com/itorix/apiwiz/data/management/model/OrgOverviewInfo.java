package com.itorix.apiwiz.data.management.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.identitymanagement.model.AbstractObject;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "OrgOverviewInfo")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrgOverviewInfo extends AbstractObject {

	private String organization;
	private String type;
	private int percentage;
	private String status;

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getPercentage() {
		return percentage;
	}

	public void setPercentage(int percentage) {
		this.percentage = percentage;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
