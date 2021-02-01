package com.itorix.apiwiz.sso.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public enum UserStatus {

	ACTIVE("Active"), LOCKED("Locked"), PENDING("Pending"),INRESETPASSWORD("InResetPassword"),VERIFY("Verify");

	private String value;

	private UserStatus(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public static  String  getStatus(UserStatus status) {

		String userStatus = null;
		for (UserStatus user : UserStatus.values()) {
			if (user.equals(status)) {
				userStatus = user.getValue();
			}

		}
		return userStatus;
	}
	
}


