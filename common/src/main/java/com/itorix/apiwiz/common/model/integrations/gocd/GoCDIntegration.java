package com.itorix.apiwiz.common.model.integrations.gocd;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GoCDIntegration {
	private String hostURL;
	private String username;
	private String password;
	private String version = "18.1.0";
	private String gradleHome;
	public String getHostURL() {
		return hostURL;
	}
	public void setHostURL(String hostURL) {
		this.hostURL = hostURL;
	}
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
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getGradleHome() {
		return gradleHome;
	}
	public void setGradleHome(String gradleHome) {
		this.gradleHome = gradleHome;
	}
	
}
