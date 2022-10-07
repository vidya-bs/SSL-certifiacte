package com.itorix.apiwiz.identitymanagement.model;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@Document(collection = "Users.Configuration.Tokens")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerificationToken {

	@Id
	@Indexed
	private String id;
	private String type;
	private String description;
	private Date created;
	private Date validTill;
	private String userEmail;
	private String workspaceId;
	private List<Roles> roles;
	private String userType;
	private boolean used = false;

	public VerificationToken() {
		super();
	}

	public VerificationToken(String id, String type, Date created, Date validTill, String userEmail,
			List<Roles> roles) {
		super();
		this.id = id;
		this.type = type;
		this.created = created;
		this.validTill = validTill;
		this.userEmail = userEmail;
		this.roles = roles;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getValidTill() {
		return validTill;
	}

	public void setValidTill(Date validTill) {
		this.validTill = validTill;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	@Override
	public String toString() {
		return "VerificationToken [id=" + id + ", type=" + type + ", created=" + created + ", validTill=" + validTill
				+ ", userEmail=" + userEmail + "]";
	}

	public boolean getUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Roles> getRoles() {
		return roles;
	}

	public void setRoles(List<Roles> roles) {
		this.roles = roles;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getWorkspaceId() {
		return workspaceId;
	}

	public void setWorkspaceId(String workspaceId) {
		this.workspaceId = workspaceId;
	}

	@JsonIgnore
	public boolean isAlive() {
		Date CurrentDate = new Date();
		if (CurrentDate.compareTo(this.validTill) < 0)
			return Boolean.TRUE;
		return Boolean.FALSE;
	}
}
