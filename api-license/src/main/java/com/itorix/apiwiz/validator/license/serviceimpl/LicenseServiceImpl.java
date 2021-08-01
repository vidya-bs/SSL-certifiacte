package com.itorix.apiwiz.validator.license.serviceimpl;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.validator.license.business.LicenseBusiness;
import com.itorix.apiwiz.validator.license.service.LicenseService;
import com.itorix.apiwiz.validator.license.model.LicenseRequest;
import com.itorix.apiwiz.validator.license.model.LicenseResponse;
import com.itorix.apiwiz.validator.license.model.db.License;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class LicenseServiceImpl implements LicenseService {

	@Autowired
	LicenseBusiness licenseBusiness;

	@Override
	public ResponseEntity createLicense(LicenseRequest licenseRequest) throws ItorixException{
		licenseBusiness.createLicense(licenseRequest);
		return new ResponseEntity(HttpStatus.OK);
	}

	@Override
	public ResponseEntity updateLicense(String emailId, LicenseRequest licenseRequest) throws ItorixException {
		licenseBusiness.updateLicense(emailId, licenseRequest);
		return new ResponseEntity(HttpStatus.ACCEPTED);
	}

	@Override public ResponseEntity deleteLicense(String emailId) {
		licenseBusiness.deleteLicense(emailId);
		return new ResponseEntity(HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<License> getLicense(String emailId) {
		License license = licenseBusiness.getLicense(emailId);
		if(license != null) {
			return new ResponseEntity<License>(license, HttpStatus.OK);
		} else {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
	}


	@Override
	public ResponseEntity<LicenseResponse> getLicenses(int offset, int pageSize) {
		LicenseResponse licenses = licenseBusiness.getLicenses(offset, pageSize);
		return new ResponseEntity<>(licenses, HttpStatus.OK);
	}
}
