package com.itorix.apiwiz.identitymanagement.model;

import java.util.Set;

import org.apache.tomcat.util.codec.binary.Base64;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonPropertyOrder({"userName", "password", "organizations"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Apigee {
	private static final Logger logger = LoggerFactory.getLogger(Apigee.class);
	private String userName;
	private String password;
	private Set<DashBoardOrganisations> organizations;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@JsonIgnore
	public String getDecryptedPassword() {
		String decryptedPassword = "";
		try {
			RSAEncryption rSAEncryption = new RSAEncryption();
			decryptedPassword = rSAEncryption.decryptText(this.password);
		} catch (Exception e) {
			logger.error("Exception occurred", e);
		}
		return decryptedPassword;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<DashBoardOrganisations> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(Set<DashBoardOrganisations> organizations) {
		this.organizations = organizations;
	}

	@JsonIgnore
	public String getEncodedCredentials() {
		return Base64.encodeBase64String((this.userName + ":" + this.getDecryptedPassword()).getBytes());
	}

	@Override
	public String toString() {
		return "Apigee [userName=" + userName + ", password=" + password + ", organizations=" + organizations + "]";
	}
}
