package com.itorix.apiwiz.servicerequest.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;

@Component("serviceRequestComments")
@Document(collection = "Connectors.Apigee.ServiceRequest.Comments")
public class ServiceRequestComments {

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String org;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String env;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String name;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String type;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String comments;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String status;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String createdUser;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String createdDate;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private boolean isSaaS;

	public Boolean getIsSaaS() {
		return isSaaS;
	}

	public void setIsSaaS(Boolean isSaaS) {
		this.isSaaS = isSaaS;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getCreatedUser() {
		return createdUser;
	}

	public void setCreatedUser(String createdUser) {
		this.createdUser = createdUser;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isSaaS() {
		return isSaaS;
	}

	public void setSaaS(boolean saaS) {
		isSaaS = saaS;
	}
}
