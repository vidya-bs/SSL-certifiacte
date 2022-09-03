package com.itorix.apiwiz.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.validator.license.crypto.HybridDecryption;
import com.itorix.apiwiz.validator.model.LicenseToken;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Set;

@Slf4j
@Component
@Getter
@Setter
//@ConditionalOnProperty(prefix = "license", name = "check", havingValue = "true")
public class LicenseValidator {


	@Autowired
	private HybridDecryption hybridDecryption;

	@Value("${itorix.license.token:}")
	private String licenseToken;

	@Value("${app.name:}")
	private String appName;

	@Value("${application.name:}")
	private String applicationName;

	@PostConstruct
	private void init() throws GeneralSecurityException, IOException, ParseException {
		validateLicense();
	}

	private void validateLicense() throws GeneralSecurityException, IOException, ParseException {
		if(licenseToken == null || licenseToken.isEmpty()) {
			String errorMsg = "Missing License Key Configuration. Configure a valid license and restart the app";
			logBlockMessage(errorMsg);
			throw new IllegalArgumentException();
		}
		String decryptedLicenseToken = hybridDecryption.decrypt(licenseToken);
		ObjectMapper objectMapper = new ObjectMapper();
		LicenseToken licenseTokenObj = objectMapper.readValue(decryptedLicenseToken, LicenseToken.class);
		if(licenseTokenObj.getLicensePolicy().isCheckExpiry()) {
			isLicenseExpired(licenseTokenObj.getExpiry());
		}
		if(licenseTokenObj.getLicensePolicy().isCheckAllowedComponents()){
			isComponentsAllowed(licenseTokenObj.getComponents());
		}
	}

	private void logBlockMessage(String errorMsg) {
		log.error("\n\n" +
				"**********************************************************************************************************************************************" +
				"\n**********************************************************************************************************************************************\n" +
				"\n" + errorMsg + "\n" +
				"\n**********************************************************************************************************************************************\n" +
				"**********************************************************************************************************************************************\n\n");
	}

	public boolean isLicenseExpired(String expiryDate) throws ParseException {
		log.info("Checking license expiry date {} ", expiryDate);
		OffsetDateTime expiry = OffsetDateTime.parse(expiryDate, getDateFormatter());
		ZonedDateTime now = ZonedDateTime.now(Clock.systemUTC());
		if(expiry.toInstant().isBefore(now.toInstant())) {
			String errorMsg = String.format("The License has expired on %s. Please contact APIwiz support to renew the license", expiry);
			logBlockMessage(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		};
		return false;
	}

	public boolean isComponentsAllowed(Set<String> components) throws ParseException {
		log.info("Checking Allowed Components {} ", components);
		if (!appName.isEmpty() && !components.contains(appName)) {
			String errorMsg = String.format("The app : %s is not supported in this License.Please contact APIwiz support to generate a valid license that supports this component", appName);
			logBlockMessage(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		} else if (appName.isEmpty() && !components.contains(applicationName)) {
			String errorMsg = String.format("The app : %s is not supported in this License.Please contact APIwiz support to generate a valid license that supports this component", applicationName);
			logBlockMessage(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		}
		;
		return false;
	}
	private DateTimeFormatter getDateFormatter() {
		return new DateTimeFormatterBuilder()
				// date/time
				.append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
				// offset (hh:mm - "+00:00" when it's zero)
				.optionalStart().appendOffset("+HH:MM", "+00:00").optionalEnd()
				// offset (hhmm - "+0000" when it's zero)
				.optionalStart().appendOffset("+HHMM", "+0000").optionalEnd()
				// offset (hh - "Z" when it's zero)
				.optionalStart().appendOffset("+HH", "Z").optionalEnd()
				// create formatter
				.toFormatter();
	}

}
