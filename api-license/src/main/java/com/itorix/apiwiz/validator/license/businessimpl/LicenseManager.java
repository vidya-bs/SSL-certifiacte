package com.itorix.apiwiz.validator.license.businessimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.validator.license.crypto.HybridEncryption;
import com.itorix.apiwiz.validator.license.model.LicenseRequest;
import com.itorix.apiwiz.validator.license.model.LicenseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;

@Component
public class LicenseManager {

	@Autowired
	private HybridEncryption encryption;


	public String getEncryptedLicense(LicenseRequest licenseRequest) throws ItorixException {
		LicenseToken licenseToken = new LicenseToken();
		licenseToken.setLicensePolicy(licenseRequest.getLicensePolicy());
		licenseToken.setNodeIds(licenseRequest.getClientIp());
		licenseToken.setEmailId(licenseRequest.getEmailId());
		licenseToken.setExpiry(licenseRequest.getExpiry());
		try {
			return encryption.encrypt(convertLicenseToJson(licenseToken));
		} catch (GeneralSecurityException e) {
			throw new ItorixException("Sorry! Internal server error. Please try again later.");
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
