package com.itorix.apiwiz.validator.license.business;

import com.itorix.apiwiz.validator.license.model.ItorixException;
import com.itorix.apiwiz.validator.license.model.LicenseRequest;
import com.itorix.apiwiz.validator.license.model.LicenseResponse;
import com.itorix.apiwiz.validator.license.model.db.License;

public interface LicenseBusiness {

	public void createLicense(LicenseRequest licenseRequest) throws ItorixException;

	public void updateLicense(String emailId, LicenseRequest licenseRequest) throws ItorixException;

	public void deleteLicense(String emailId) throws ItorixException;

	public License getLicense(String emailId);

	public LicenseResponse getLicenses(int offset, int pageSize);

	public boolean isLicenseValid(String emailId) throws ItorixException;
}
