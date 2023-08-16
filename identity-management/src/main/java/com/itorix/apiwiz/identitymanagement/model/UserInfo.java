package com.itorix.apiwiz.identitymanagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.SwaggerTeam;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfo {
	private String loginId;
	private String email;
	private String password;
	private boolean isServiceAccount=false;
	private String lastName;
	private String firstName;
	private String userStatus;
	private String workspaceId;
	private String planId;
	private List<String> roles;
	private String type;
	private String userType;
	private String workPhone;
	private boolean subscribeNewsLetter;
	private String paymentSchedule;
	private String trialPeriod = "14";
	private String subscriptionId;
	private long seats;
	private String name;
	private String regionCode;
	private String company;
	private String status;
	private List<SwaggerTeam> teams;
	private Map<String, String> metadata;
	private boolean isTrial;

	public boolean isServiceAccount() {
		return isServiceAccount;
	}

	public void setIsServiceAccount(boolean serviceAccount) {
		isServiceAccount = serviceAccount;
	}

	public boolean getIsTrial() {
		return isTrial;
	}

	public boolean isTrial() {
		return isTrial;
	}

	public void setTrial(boolean isTrial) {
		this.isTrial = isTrial;
	}

	public void setIsTrial(boolean isTrial) {
		this.isTrial = isTrial;
	}

	public String getPaymentSchedule() {
		return paymentSchedule;
	}

	public void setPaymentSchedule(String paymentSchedule) {
		this.paymentSchedule = paymentSchedule;
	}

	public String getTrialPeriod() {
		return trialPeriod;
	}

	public void setTrialPeriod(String trialPeriod) {
		this.trialPeriod = trialPeriod;
	}

	// @JsonIgnore
	// public boolean getIsTrial() {
	// return isTrial;
	// }
	//
	// public void setIsTrial(boolean isTrial) {
	// this.isTrial = isTrial;
	// }
	//
	//
	// public void setTrial(boolean isTrial) {
	// this.isTrial = isTrial;
	// }

	public String getWorkPhone() {
		return workPhone;
	}

	public void setWorkPhone(String workPhone) {
		this.workPhone = workPhone;
	}

	public String getRegionCode() {
		return regionCode;
	}

	public void setRegionCode(String regionCode) {
		this.regionCode = regionCode;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public UserInfo() {
		// TODO Auto-generated constructor stub
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email != null ? email.toLowerCase() : null;

	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(String userStatus) {
		this.userStatus = userStatus;
	}

	public String getWorkspaceId() {
		return workspaceId;
	}

	public void setWorkspaceId(String workspaceId) {
		this.workspaceId = workspaceId;
	}

	public String getPlanId() {
		return planId;
	}

	public void setPlanId(String planId) {
		this.planId = planId;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	@JsonIgnore
	public boolean allowUserRegistration() throws ItorixException {
		List<String> missingFields = new ArrayList<>();
		if (loginId == null || loginId.trim() == "")
			missingFields.add("loginId");
		if (password == null || password.trim() == "")
			missingFields.add("password");
		// if(email == null || email.trim()=="")
		// missingFields.add("email");
		if (firstName == null || firstName.trim() == "")
			missingFields.add("firstName");
		if (lastName == null || lastName.trim() == "")
			missingFields.add("lastName");
		if (workspaceId == null || workspaceId.trim() == "")
			missingFields.add("workspaceId");
		if (planId == null || planId.trim() == "")
			missingFields.add("planId");
		if (missingFields.size() > 0)
			raiseException(missingFields);
		return true;
	}

	@JsonIgnore
	public boolean allowInviteRegistration() throws ItorixException {
		List<String> missingFields = new ArrayList<>();
		if (loginId == null || loginId.trim() == "")
			missingFields.add("loginId");
		if (password == null || password.trim() == "")
			missingFields.add("password");
		if (firstName == null || firstName.trim() == "")
			missingFields.add("firstName");
		if (lastName == null || lastName.trim() == "")
			missingFields.add("lastName");
		if (missingFields.size() > 0)
			raiseException(missingFields);
		return true;
	}

	@JsonIgnore
	public boolean allowInviteUser() throws ItorixException {
		List<String> missingFields = new ArrayList<>();
		if (email == null || email.trim() == "")
			missingFields.add("email");
		if (roles == null || roles.size() == 0)
			missingFields.add("roles");
		if (workspaceId == null || workspaceId.trim() == "")
			missingFields.add("workspaceId");
		if (missingFields.size() > 0)
			raiseException(missingFields);
		return true;
	}

	@JsonIgnore
	public boolean allowLogin() throws ItorixException {
		List<String> missingFields = new ArrayList<>();
		if (loginId == null || loginId.trim() == "")
			missingFields.add("loginId");
		if (password == null || password.trim() == "")
			missingFields.add("password");
		if (workspaceId == null || workspaceId.trim() == "")
			missingFields.add("workspaceId");
		if (missingFields.size() > 0)
			raiseException(missingFields);
		return true;
	}

	@JsonIgnore
	public boolean allowEditUser() throws ItorixException {
		List<String> missingFields = new ArrayList<>();
		if (loginId != null && loginId.trim() == "")
			missingFields.add("loginId");
		if (email != null && email.trim() == "")
			missingFields.add("email");
		if (firstName != null && firstName.trim() == "")
			missingFields.add("firstName");
		if (lastName != null && lastName.trim() == "")
			missingFields.add("lastName");
		if (missingFields.size() > 0)
			raiseException(missingFields);
		return true;
	}

	@JsonIgnore
	public boolean allowCreateToken() throws ItorixException {
		List<String> missingFields = new ArrayList<>();

		if (type != null && type.equals("password-reset")) {
			if (email != null && email.trim() == "")
				missingFields.add("email");
		}
		if (type != null && type.equals("register")) {
			if (email != null && email.trim() == "")
				missingFields.add("email");
		}
		if (missingFields.size() > 0)
			raiseException(missingFields);
		return true;
	}

	@JsonIgnore
	public boolean allowChangeSubscription() throws ItorixException {
		List<String> missingFields = new ArrayList<>();
		if (planId != null && planId.trim() == "")
			missingFields.add("planId");
		if (paymentSchedule != null && paymentSchedule.trim() == "")
			missingFields.add("paymentSchedule");
		if (subscriptionId != null && subscriptionId.trim() == "")
			missingFields.add("subscriptionId");
		if (workspaceId != null && workspaceId.trim() == "")
			missingFields.add("workspaceId");
		if (missingFields.size() > 0)
			raiseException(missingFields);
		return true;
	}

	private void raiseException(List<String> fileds) throws ItorixException {
		try {
			String message = new ObjectMapper().writeValueAsString(fileds);
			message = message.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\"", "").replaceAll(",", ", ");
			message = "Invalid request data! Missing mandatory data: " + message;
			throw new ItorixException(message, "Identity-1007");
		} catch (JsonProcessingException e) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1007"), "Identity-1007");
		}
	}

	// @JsonIgnore
	public boolean getSubscribeNewsLetter() {
		return subscribeNewsLetter;
	}

	public void setSubscribeNewsLetter(boolean subscribeNewsLetter) {
		this.subscribeNewsLetter = subscribeNewsLetter;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<SwaggerTeam> getTeams() {
		return teams;
	}

	public void setTeams(List<SwaggerTeam> teams) {
		this.teams = teams;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	public long getSeats() {
		return seats;
	}

	public void setSeats(long seats) {
		this.seats = seats;
	}
}
