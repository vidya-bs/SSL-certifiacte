package com.itorix.apiwiz.common.model.policyperformance;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApigeeVO implements Serializable {
	private String fileOid;
	private String org;
	private String api;
	private String env;
	private String rev;
	private String username;
	private String password;
	private String tempToken;
	private String testType;

	private MultipartFile postmanFile;
	private MultipartFile envFile;

	public String getFileOid() {
		return fileOid;
	}

	public void setFileOid(String fileOid) {
		this.fileOid = fileOid;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public String getApi() {
		return api;
	}

	public void setApi(String api) {
		this.api = api;
	}

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public String getRev() {
		return rev;
	}

	public void setRev(String rev) {
		this.rev = rev;
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

	public String getTempToken() {
		return tempToken;
	}

	public void setTempToken(String tempToken) {
		this.tempToken = tempToken;
	}

	public String getTestType() {
		return testType;
	}

	public void setTestType(String testType) {
		this.testType = testType;
	}

	public MultipartFile getPostmanFile() {
		return postmanFile;
	}

	public void setPostmanFile(MultipartFile postmanFile) {
		this.postmanFile = postmanFile;
	}

	public MultipartFile getEnvFile() {
		return envFile;
	}

	public void setEnvFile(MultipartFile envFile) {
		this.envFile = envFile;
	}
}
