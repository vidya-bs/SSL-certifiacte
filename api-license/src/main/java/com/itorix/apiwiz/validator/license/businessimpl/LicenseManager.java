package com.itorix.apiwiz.validator.license.businessimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.validator.license.crypto.HybridEncryption;
import com.itorix.apiwiz.validator.license.model.ItorixException;
import com.itorix.apiwiz.validator.license.model.LicenseRequest;
import com.itorix.apiwiz.validator.license.model.LicenseToken;
import com.itorix.apiwiz.validator.license.util.RSAEncryption;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;
//import org.springframework.util.StringUtils;

@Component
public class LicenseManager {

	@Autowired
	private HybridEncryption encryption;

	@Autowired
	private RSAEncryption rsaEncryption;


	public String getEncryptedLicense(LicenseRequest licenseRequest,String encryptionType) throws ItorixException {
		LicenseToken licenseToken = new LicenseToken();
		try {
			if(StringUtils.equalsIgnoreCase("tink",encryptionType)) {
				licenseToken.setLicensePolicy(licenseRequest.getLicensePolicy());
				licenseToken.setNodeIds(licenseRequest.getClientIp());
				licenseToken.setEmailId(licenseRequest.getEmailId());
				licenseToken.setExpiry(licenseRequest.getExpiry());
				licenseToken.setComponents(licenseRequest.getComponents());
				return encryption.encrypt(convertLicenseToJson(licenseToken));
			}
			else if(StringUtils.equalsIgnoreCase("rsa",encryptionType)){
				licenseToken.setExpiry(licenseRequest.getExpiry());
				return rsaEncryption.encryptText(convertLicenseToJson(licenseToken));
			}
			else
				return null;
		} catch (GeneralSecurityException e) {
			throw new ItorixException("Sorry! Internal server error. Please try again later.");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	private String convertLicenseToJson(LicenseToken token) throws ItorixException {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.writeValueAsString(token);
		} catch (JsonProcessingException e) {
			throw new ItorixException("Sorry! Internal server error. Please try again later.");
		}
	}


}
