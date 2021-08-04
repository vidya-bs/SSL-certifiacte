package com.itorix.apiwiz.validator.license.businessimpl;
import com.itorix.apiwiz.validator.license.business.LicenseBusiness;
import com.itorix.apiwiz.validator.license.dao.LicenseRepository;
import com.itorix.apiwiz.validator.license.model.*;
import com.itorix.apiwiz.validator.license.model.db.License;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LicenseBusinessImpl implements LicenseBusiness {

	@Autowired
	private LicenseManager manager;

	@Autowired
	private LicenseRepository licenseRepository;

	@Override
	public void createLicense(LicenseRequest licenseRequest) throws ItorixException {

		License license = licenseRepository.findOne("emailId", licenseRequest.getEmailId(), License.class);

		if(license != null ) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("License-1001"), licenseRequest.getEmailId()), "License-1001");
		}

		licenseRequest.setStatus(String.valueOf(Status.VALID));
		License licenseToSave = createLicenseObj(licenseRequest);

		licenseRepository.save(licenseToSave);

	}
	private License createLicenseObj(LicenseRequest licenseRequest) throws ItorixException {
		License licenseToSave = new License();
		licenseToSave.setUserName(licenseRequest.getUserName());
		licenseToSave.setPassword(licenseRequest.getPassword());
		licenseToSave.setClientName(licenseRequest.getClientName());
		licenseToSave.setEmailId(licenseRequest.getEmailId());
		licenseToSave.setLicensePolicy(licenseRequest.getLicensePolicy());
		licenseToSave.setClientIp(licenseRequest.getClientIp());
		licenseToSave.setWorkspaceName(licenseRequest.getWorkspaceName());
		licenseToSave.setExpiry(licenseRequest.getExpiry());
		licenseToSave.setEncryptedToken(manager.getEncryptedLicense(licenseRequest));
		licenseToSave.setStatus(Status.VALID);
		return licenseToSave;
	}

	@Override
	public void updateLicense(String emailId, LicenseRequest licenseRequest) throws ItorixException {
		License license = licenseRepository.findOne("emailId", emailId, License.class);
		if(license == null ) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("License-1002"), emailId), "License-1002");
		}

		updateLicenseObj(license, licenseRequest);
		licenseRepository.save(license);
	}

	@Override
	public void deleteLicense(String emailId) throws ItorixException {
		License license = licenseRepository.findOne("emailId", emailId, License.class);
		if(license == null ) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("License-1002"), emailId), "License-1002");
		}
		license.setStatus(Status.TERMINATED);
		licenseRepository.save(license);
	}

	private void updateLicenseObj(License license, LicenseRequest licenseRequest) throws ItorixException {
		license.setUserName(licenseRequest.getUserName());
		license.setPassword(licenseRequest.getPassword());
		license.setClientName(licenseRequest.getClientName());
		license.setLicensePolicy(licenseRequest.getLicensePolicy());
		license.setClientIp(licenseRequest.getClientIp());
		license.setWorkspaceName(licenseRequest.getWorkspaceName());
		license.setExpiry(licenseRequest.getExpiry());
		license.setEncryptedToken(manager.getEncryptedLicense(licenseRequest));
		license.setStatus(Status.valueOf(licenseRequest.getStatus()));
	}

	@Override
	public License getLicense(String emailId) {
		return licenseRepository.findOne("emailId", emailId, License.class);
	}

	@Override
	public LicenseResponse getLicenses(int offset, int pageSize) {
		LicenseResponse licenseResponse = new LicenseResponse();
		Pagination pagination = new Pagination();
		List<License> licenses = licenseRepository.findAll(License.class);
		int total = licenses.size();
		licenses = trimList(licenses, offset, pageSize);

		licenseResponse.setLicenses(licenses);
		pagination.setTotal(total);
		pagination.setOffset(offset);
		pagination.setPageSize(pageSize);
		licenseResponse.setPage(pagination);

		return licenseResponse;
	}

	private List<License> trimList(List<License> licenses, int offset, int pageSize){
		List<License> trimmedList = new ArrayList<>();
		int i = offset > 0 ? ((offset - 1) * pageSize) : 0;
		int end = i + pageSize;
		for (; i<licenses.size() && i < end ; i++){
			trimmedList.add(licenses.get(i));
		}
		return trimmedList;
	}

	@Override
	public boolean isLicenseValid(String emailId) {
		License license = licenseRepository.findOne("emailId", emailId, License.class);
		try {
			checkLicenseStatus(license.getStatus());
			checkLicenseExpiry(license.getExpiry());
		} catch (ItorixException e) {
			log.error("license validation failed " + e.getMessage());
			return false;
		}
		return true;
	}
	private void checkLicenseStatus(Status status) throws ItorixException {
		if(!status.equals(Status.VALID)) {
			throw new ItorixException("License is " + status.name());
		}
	}

	private void checkLicenseExpiry(String expiryDate) throws ItorixException {
		try {
			isLicenseExpired(expiryDate);
		} catch (Exception e) {
			throw new ItorixException(e.getMessage());
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

}
