package com.itorix.apiwiz.validator.license.businessimpl;
import com.itorix.apiwiz.LicenseValidator;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.validator.license.business.LicenseBusiness;
import com.itorix.apiwiz.validator.license.dao.LicenseRepository;
import com.itorix.apiwiz.validator.license.model.LicenseRequest;
import com.itorix.apiwiz.validator.license.model.LicenseResponse;
import com.itorix.apiwiz.validator.license.model.Pagination;
import com.itorix.apiwiz.validator.license.model.Status;
import com.itorix.apiwiz.validator.license.model.db.License;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
		License license = licenseRepository.findOne("emailId", licenseRequest.getEmailId(), License.class);
		if(license == null ) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("License-1002"), licenseRequest.getEmailId()));
		}

		updateLicenseObj(license, licenseRequest);
		licenseRepository.save(license);
	}

	@Override
	public void deleteLicense(String emailId) {
		licenseRepository.delete("emailId", emailId, License.class);
	}

	private void updateLicenseObj(License license, LicenseRequest licenseRequest) throws ItorixException {
		license.setClientName(licenseRequest.getClientName());
		license.setEmailId(licenseRequest.getEmailId());
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
		List trimmedList = new ArrayList<License>();
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
		} catch (Exception e) {
			log.error("license validation failed" + e.getMessage());
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
		LicenseValidator validator = new LicenseValidator();
		try {
			validator.isLicenseExpired(expiryDate);
		} catch (Exception e) {
			throw new ItorixException("License expiry validation failed" + e.getMessage());
		}
	}

}
