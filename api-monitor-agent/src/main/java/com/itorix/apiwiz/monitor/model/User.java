package com.itorix.apiwiz.monitor.model;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;

@Component("user")
@Document(collection = "UserManagement.Users")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

    public static final String LABEL_USERNAME = "userName";
    public static final String LABEL_LOGINID = "loginId";
    public static final String LABEL_EMAIL = "email";
    public static final String LABEL_PASSWORD = "password";
    public static final String LABEL_PASSWORD_HASH = "passwordHash";
    public static final String LABEL_ORGANIZATIONS = "organizations";
    public static final String LABEL_ORGANIZATION_INFO = "organizationInfo";
    public static final String LABEL_ROLES = "roles";
    public static final String LABEL_STATUS = "userStatus";

    private String loginId;
    private String email;
    private String password;
    private String organizationInfo;
    private List<String> roles;
    private String userRole;
    private String lastName;
    private String firstName;
    private String userStatus;
    private String displayMessage;
    private String verificationToken;
    private long userCount;
    private String reason;
    private String userId;
    private String newPassword;
    private String oldPassword;
    private String type;
    private Date tokenValidUpto;

    public Date getTokenValidUpto() {
        return tokenValidUpto;
    }

    public void setTokenValidUpto(Date tokenValidUpto) {
        this.tokenValidUpto = tokenValidUpto;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
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

    public User(String userName, String email, String password) {
        super();
        this.loginId = userName;
        this.email = email;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOrganizationInfo() {
        return organizationInfo;
    }

    public void setOrganizationInfo(String organizationInfo) {
        this.organizationInfo = organizationInfo;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

}