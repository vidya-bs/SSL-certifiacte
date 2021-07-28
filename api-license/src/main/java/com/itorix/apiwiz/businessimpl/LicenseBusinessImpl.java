package com.itorix.apiwiz.businessimpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.business.LicenseBusiness;
import com.itorix.apiwiz.model.LicensePolicy;
import com.itorix.apiwiz.model.LicenseToken;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class LicenseBusinessImpl implements LicenseBusiness {

	@SneakyThrows
	@Override
	public String getLicenseToken(String emailId) {
		LicenseToken licenseToken = new LicenseToken();
		licenseToken.setEmailId("balaji@itorix.com");
		licenseToken.setExpiry(LocalDateTime.of(2021, 12, 20, 01, 00).toEpochSecond(ZoneOffset.UTC));
		LicensePolicy licensePolicy = new LicensePolicy();
		licensePolicy.setCheckExpiry(true);
		licenseToken.setLicensePolicy(licensePolicy);

		ObjectMapper obj = new ObjectMapper();

		return obj.writeValueAsString(licenseToken);

	}
}
