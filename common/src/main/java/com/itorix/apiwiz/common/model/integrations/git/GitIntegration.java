package com.itorix.apiwiz.common.model.integrations.git;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GitIntegration {

	private String userType;
	private String username;
	private String password;
	private String token;
	private String authType;
	private String hostURL;
	private String gitRepoURL;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	public String getToken() {
		return token;
	}

	@JsonIgnore
	public String getDecryptedToken() {
		String decryptedPassword = "";
		try {
			RSAEncryption rSAEncryption = new RSAEncryption();
			decryptedPassword = rSAEncryption.decryptText(this.token);
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

	public void setToken(String token) {
		this.token = token;
	}

	public String getGitRepoURL() {
		return gitRepoURL;
	}

	public void setGitRepoURL(String gitRepoURL) {
		this.gitRepoURL = gitRepoURL;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getHostURL() {
		return hostURL;
	}

	public void setHostURL(String hostURL) {
		this.hostURL = hostURL;
	}
}
