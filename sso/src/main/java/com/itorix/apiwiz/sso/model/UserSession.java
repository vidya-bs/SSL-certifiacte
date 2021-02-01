package com.itorix.apiwiz.sso.model;

import java.io.Serializable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;



@Document(collection = "Users.Sessions.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSession implements Serializable {

	private static final long serialVersionUID = -8723630138842849229L;

	@Id
	@Indexed
	protected String id;
	private String userId;
	private String username;
	private String ipAddress;
	private String referer;
	private String host;
	private String userAgent;
	private long loginTimestamp;
	private boolean expired;
	private String email;
	private List<String> roles;
	private String loginId;
	private String lastName;
	private String firstName;
	private String userStatus;
	private String status;
	private String userType;
	private String tenant;
	private String workspaceId;
	private boolean isTrial;
	private String trialPeriod;
	private String trialExpired;
	private String expiresOn;
	private String planId;
	private String paymentSchedule;
	private String subscriptionId;

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
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

	public String getTrialPeriod() {
		return trialPeriod;
	}

	public void setTrialPeriod(String trialPeriod) {
		this.trialPeriod = trialPeriod;
	}

	public String getTrialExpired() {
		return trialExpired;
	}

	public void setTrialExpired(String trialExpired) {
		this.trialExpired = trialExpired;
	}

	public String getExpiresOn() {
		return expiresOn;
	}

	public void setExpiresOn(String expiresOn) {
		this.expiresOn = expiresOn;
	}

	public String getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(String userStatus) {
		this.userStatus = userStatus;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Transient
	private User user;

	public UserSession() {
		super();
	}


	public UserSession(User user) {
		super();
		this.setUserId(user.getId());
		this.setLoginTimestamp(System.currentTimeMillis());
		this.setEmail(user.getEmail());
		this.setFirstName(user.getFirstName());
		this.setLastName(user.getLastName());
		this.setUsername(user.getFirstName() + " " + user.getLastName());
		this.setUserStatus(user.getUserStatus());
	}

	public void setRequestAttributes(HttpServletRequest request) {
		String ipAddress = request.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = request.getRemoteAddr();
		}
		this.setIpAddress(ipAddress);
		this.setHost(request.getHeader("Host"));
		this.setReferer(request.getHeader("Referer"));
		this.setUserAgent(request.getHeader("User-Agent"));
	}

	public synchronized static UserSession getCurrentSessionToken() {
		UserSession userSessionToken = null;
		try {
			userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		} catch (Exception ex) {
		}
		return userSessionToken;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getReferer() {
		return referer;
	}

	public void setReferer(String referer) {
		this.referer = referer;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public boolean isExpired() {
		return expired;
	}

	public void setExpired(boolean expired) {
		this.expired = expired;
	}

	public long getLoginTimestamp() {
		return loginTimestamp;
	}

	public void setLoginTimestamp(long loginTimestamp) {
		this.loginTimestamp = loginTimestamp;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public String getWorkspaceId() {
		return workspaceId;
	}

	public void setWorkspaceId(String workspaceId) {
		this.workspaceId = workspaceId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean getIsTrial() {
		return isTrial;
	}

	public void setIsTrial(boolean isTrial) {
		this.isTrial = isTrial;
	}

	public String getPlanId() {
		return planId;
	}

	public void setPlanId(String planId) {
		this.planId = planId;
	}

	public String getPaymentSchedule() {
		return paymentSchedule;
	}

	public void setPaymentSchedule(String paymentSchedule) {
		this.paymentSchedule = paymentSchedule;
	}

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}
}
