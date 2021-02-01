package com.itorix.apiwiz.design.studio.model;

import java.util.Set;

import org.springframework.data.mongodb.core.mapping.Document;

import com.itorix.apiwiz.identitymanagement.model.AbstractObject;


@Document(collection = "Design.Swagger.Review")
public class SwaggerReview extends AbstractObject {

	private String swaggerName;
	private Integer revision;
	private Set<String> teamNames;
	private Set<String> contacts;
	private String reviewTitle;
	private String userName;
	private String userEmailId;

	public String getSwaggerName() {
		return swaggerName;
	}

	public void setSwaggerName(String swaggerName) {
		this.swaggerName = swaggerName;
	}

	public Integer getRevision() {
		return revision;
	}

	public void setRevision(Integer revision) {
		this.revision = revision;
	}

	public Set<String> getTeamNames() {
		return teamNames;
	}

	public void setTeamNames(Set<String> teamNames) {
		this.teamNames = teamNames;
	}

	public Set<String> getContacts() {
		return contacts;
	}

	public void setContacts(Set<String> contacts) {
		this.contacts = contacts;
	}

	public String getReviewTitle() {
		return reviewTitle;
	}

	public void setReviewTitle(String reviewTitle) {
		this.reviewTitle = reviewTitle;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserEmailId() {
		return userEmailId;
	}

	public void setUserEmailId(String userEmailId) {
		this.userEmailId = userEmailId;
	}

}
