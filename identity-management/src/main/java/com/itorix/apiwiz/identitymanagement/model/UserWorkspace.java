package com.itorix.apiwiz.identitymanagement.model;

import java.util.List;

import org.springframework.data.annotation.Reference;
import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.model.SwaggerTeam;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserWorkspace {

	private List<String> roles;
	private Workspace workspace;
	private String userType;
	private boolean acceptInvite = false;
	private boolean active = false;
	private String createdUserName;
	private Long cts;
	@Transient
	private List<SwaggerTeam> teams;

	public UserWorkspace() {
		// TODO Auto-generated constructor stub
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	@JsonIgnore
	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public boolean getAcceptInvite() {
		return acceptInvite;
	}

	public void setAcceptInvite(boolean acceptInvite) {
		this.acceptInvite = acceptInvite;
	}

	public boolean getActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getName() {
		return this.workspace != null ? this.workspace.getName() : null;
	}

	public String getPlanId() {
		return this.workspace != null ? this.workspace.getPlanId() : null;
	}

	public String getTenant() {
		return this.workspace != null ? this.workspace.getTenant() : null;
	}

	public String getStatus() {
		return this.workspace != null ? this.workspace.getStatus() : null;
	}

	public String getActiveTill() {
		return this.workspace != null ? this.workspace.getActiveTill() : null;
	}

	public String getKey() {
		return this.workspace != null ? this.workspace.getKey() : null;
	}

	public String getRegionCode() {
		return this.workspace != null ? this.workspace.getRegionCode() : null;
	}

	public String getPaymentReference() {
		return this.workspace != null ? this.workspace.getSubscriptionId() : null;
	}

	public String getSubscriptionId() {
		return this.workspace != null ? this.workspace.getPaymentSchedule() : null;
	}

	public String getTrialPeriod() {
		return this.workspace != null ? this.workspace.getTrialPeriod() : null;
	}

	public boolean getIsTrial() {
		return this.workspace != null ? this.workspace.getIsTrial() : null;
	}

	public List<SwaggerTeam> getTeams() {
		return teams;
	}

	public void setTeams(List<SwaggerTeam> teams) {
		this.teams = teams;
	}

	public String getCreatedUserName() {
		return createdUserName;
	}

	public void setCreatedUserName(String createdUserName) {
		this.createdUserName = createdUserName;
	}

	public Long getCts() {
		return cts;
	}

	public void setCts(Long cts) {
		this.cts = cts;
	}
}
