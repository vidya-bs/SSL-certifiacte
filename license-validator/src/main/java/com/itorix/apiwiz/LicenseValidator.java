package com.itorix.apiwiz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.validator.license.crypto.HybridDecryption;
import com.itorix.apiwiz.validator.model.LicenseToken;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

@Slf4j
@Component
@Getter
@Setter
@ConditionalOnProperty(prefix = "license", name = "check", havingValue = "true")
public class LicenseValidator {

	@Autowired
	private HybridDecryption hybridDecryption;

	@Value("${itorix.license.token}")
	private String licenseToken;

	@PostConstruct
	private void init() throws GeneralSecurityException, IOException, ParseException {
		validateLicense();
	}

	private void validateLicense() throws GeneralSecurityException, IOException, ParseException {
		String decryptedLicenseToken = hybridDecryption.decrypt(licenseToken);
		ObjectMapper objectMapper = new ObjectMapper();
		LicenseToken licenseTokenObj = objectMapper.readValue(decryptedLicenseToken, LicenseToken.class);
		if(licenseTokenObj.getLicensePolicy().isCheckExpiry()) {
			isLicenseExpired(licenseTokenObj.getExpiry());
		}
	}

	public boolean isLicenseExpired(String expiryDate) throws ParseException {
		log.info("Checking license expiry date {} ", expiryDate);
		OffsetDateTime expiry = OffsetDateTime.parse(expiryDate, getDateFormatter());
		ZonedDateTime now = ZonedDateTime.now(Clock.systemUTC());
		if(expiry.toInstant().isBefore(now.toInstant())) {
			String errorMsg = String.format("The License has expired on %s. Please contact Itorix support to renew the license", expiry);
			log.error(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		};
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


//	public static void main(String[] args) throws IOException, GeneralSecurityException {
//		HybridDecryption decryption = new HybridDecryption();
//		decryption.loadKey();
//		String token = "";
//		String decrypt = decryption.decrypt(token);
//		System.out.println(decrypt);
//		ObjectMapper objectMapper = new ObjectMapper();
//		LicenseToken licenseToken = objectMapper.readValue(decrypt, LicenseToken.class);
//
//	}

}
