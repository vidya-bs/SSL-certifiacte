package com.itorix.apiwiz.common.model.integrations.s3;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;

public class S3Integration {

	private String key;
	private String secret;
	private String bucketName;
	private String region;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getSecret() {
		return secret;
	}
	@JsonIgnore
	public String getDecryptedSecret() {
		String decryptedPassword = "";
		try {
			RSAEncryption rSAEncryption = new RSAEncryption();
			decryptedPassword = rSAEncryption.decryptText(this.secret);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decryptedPassword;
	}
	public void setSecret(String secret) {
		this.secret = secret;
	}
	public String getBucketName() {
		return bucketName;
	}
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}

}
