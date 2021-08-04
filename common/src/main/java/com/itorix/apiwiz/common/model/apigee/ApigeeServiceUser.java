package com.itorix.apiwiz.common.model.apigee;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Apigee.ServiceAccount")
public class ApigeeServiceUser {

	private String orgName;
	private String type;
	private String userName;
	private String password;
	private String authType = "basic";
	private String tokenURL;
	private String grantType = "password";
	private String basicToken = "Basic ZWRnZWNsaTplZGdlY2xpc2VjcmV0";

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@JsonIgnore
	public String getDecryptedPassword() {
		String decryptedPassword = "";
		try {
			RSAEncryption rSAEncryption = new RSAEncryption();
			decryptedPassword = rSAEncryption.decryptText(this.password);
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return decryptedPassword;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getTokenURL() {
		return tokenURL;
	}

	public void setTokenURL(String tokenURL) {
		this.tokenURL = tokenURL;
	}

	public String getGrantType() {
		return grantType;
	}

	public void setGrantType(String grantType) {
		this.grantType = grantType;
	}

	public String getBasicToken() {
		return basicToken;
	}

	public void setBasicToken(String basicToken) {
		this.basicToken = basicToken;
	}
}
