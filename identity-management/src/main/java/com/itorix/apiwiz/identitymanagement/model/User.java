package com.itorix.apiwiz.identitymanagement.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@Component("user")
@Document(collection = "Users.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User extends AbstractObject {

	public static final String LABEL_USERNAME = "userName";
	public static final String LABEL_LOGINID = "loginId";
	public static final String LABEL_EMAIL = "email";
	public static final String LABEL_PASSWORD = "password";
	public static final String LABEL_PASSWORD_HASH = "passwordHash";
	public static final String LABEL_MEMBER = "Member";
	public static final String LABEL_SITEADMIN = "organizationInfo";
	public static final String LABEL_ROLES = "roles";
	public static final String LABEL_STATUS = "userStatus";
	public static final String LABEL_USER_ID = "userId";

	private String loginId;
	private String email;
	private String password;
	private long passwordLastChangedDate;

	private boolean isServiceAccount;
	private String displayMessage;
	private String lastName;
	private String firstName;
	private String userStatus;
	private List<UserWorkspace> workspaces;
	private long userCount;
	private String reason;
	private String userId;
	private String newPassword;
	private String oldPassword;
	private String verificationToken;
	private Date tokenValidUpto;
	private String workPhone;
	private boolean subscribeNewsLetter = true;
	private String regionCode;
	private String company;
	private Apigee apigee;
	private boolean invited;
	private Map<String, String> metadata;

	@Transient
	private String verificationStatus;

	public String getVerificationStatus() {
		return verificationStatus;
	}

	public boolean isServiceAccount() {
		return isServiceAccount;
	}

	public void setIsServiceAccount(boolean serviceAccount) {
		isServiceAccount = serviceAccount;
	}
	public long getPasswordLastChangedDate() {
		return passwordLastChangedDate;
	}

	public void setPasswordLastChangedDate(long passwordLastChangedDate) {
		this.passwordLastChangedDate = passwordLastChangedDate;
	}
	public void setVerificationStatus(String verificationStatus) {
		this.verificationStatus = verificationStatus;
	}

	public Date getTokenValidUpto() {
		return tokenValidUpto;
	}

	public void setTokenValidUpto(Date tokenValidUpto) {
		this.tokenValidUpto = tokenValidUpto;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public User() {
		super();
	}

	public User(String userName, String email, String password) {
		super();
		this.loginId = userName;
		this.email = email;
		this.password = password;
	}

	public User(User user) {
		super(user);
		this.loginId = user.loginId;
		this.email = user.email;
		this.password = user.password;
		this.lastName = user.lastName;
		this.firstName = user.firstName;
		this.userStatus = user.userStatus;
		this.verificationToken = user.verificationToken;
		this.userCount = user.userCount;
		this.reason = user.reason;
		this.userId = user.userId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	@JsonIgnore
	public long getUserCount() {
		return userCount;
	}

	public void setUserCount(long userCount) {
		this.userCount = userCount;
	}

	public String getVerificationToken() {
		return verificationToken;
	}

	public void setVerificationToken(String verificationToken) {
		this.verificationToken = verificationToken;
	}

	public String getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(String userStatus) {
		this.userStatus = userStatus;
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

	public String getEmail() {
		return email;
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@JsonIgnore
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDisplayMessage() {
		return displayMessage;
	}

	public void setDisplayMessage(String displayMessage) {
		this.displayMessage = displayMessage;
	}

	public List<UserWorkspace> getWorkspaces() {
		return workspaces;
	}

	public void setWorkspaces(List<UserWorkspace> workspaces) {
		this.workspaces = workspaces;
	}

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

	@JsonIgnore
	public void setLastLoginTime(String workspace) {
		for (UserWorkspace userWorkspace : this.workspaces){
			if (userWorkspace.getName().equals(workspace)){
				userWorkspace.setLastLoginTs(System.currentTimeMillis());
			}
		}
	}


	@JsonIgnore
	public boolean containsWorkspace(String workspaceId) {
		try {
			for (UserWorkspace workspace : workspaces)
				if (workspace.getWorkspace().getName().equals(workspaceId))
					return true;
		} catch (Exception e) {
		}
		return false;
	}

	@JsonIgnore
	public UserWorkspace getUserWorkspace(String workspaceId) {
		try {
			for (UserWorkspace workspace : workspaces)
				if (workspace.getWorkspace().getName().equals(workspaceId))
					return workspace;
		} catch (Exception e) {
		}
		return null;
	}

	@JsonIgnore
	public boolean isWorkspaceAdmin(String workspaceId) {
		try {
			for (UserWorkspace workspace : workspaces)
				if (workspace.getWorkspace().getName().equals(workspaceId)) {
					if (workspace.getUserType() != null && workspace.getUserType().equalsIgnoreCase("Site-Admin"))
						return Boolean.TRUE;
					if (workspace.getRoles() != null && workspace.getRoles().contains("Admin"))
						return Boolean.TRUE;
				}
		} catch (Exception e) {
		}
		return Boolean.FALSE;
	}

	@JsonIgnore
	public boolean isWorkspaceSuperAdmin(String workspaceId) {
		try {
			for (UserWorkspace workspace : workspaces)
				if (workspace.getWorkspace().getName().equals(workspaceId)) {
					if (workspace.getUserType() != null && workspace.getUserType().equalsIgnoreCase("Super-Admin"))
						return Boolean.TRUE;
				}
		} catch (Exception e) {
		}
		return Boolean.FALSE;
	}

	@JsonIgnore
	public boolean canLogin() {
		if (userStatus != null && userStatus.trim() != "")
			if (userStatus.equalsIgnoreCase("Active"))
				return true;
			else
				return false;
		else
			return true;
	}

	@JsonIgnore
	public boolean isNew() {

		try {
			if (workspaces.size() == 1)
				if (workspaces.get(0).getAcceptInvite() == true)
					return false;
				else
					return true;
		} catch (Exception e) {
		}
		return false;
	}

	public boolean getSubscribeNewsLetter() {
		return subscribeNewsLetter;
	}

	public void setSubscribeNewsLetter(boolean subscribeNewsLetter) {
		this.subscribeNewsLetter = subscribeNewsLetter;
	}

	@JsonIgnore
	public Apigee getApigee() {
		return apigee;
	}

	public void setApigee(Apigee apigee) {
		this.apigee = apigee;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}
}
