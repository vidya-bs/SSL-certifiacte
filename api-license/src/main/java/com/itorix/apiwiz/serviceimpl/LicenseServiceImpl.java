package com.itorix.apiwiz.serviceimpl;
import com.itorix.apiwiz.business.LicenseBusiness;
import com.itorix.apiwiz.model.LicenseRequest;
import com.itorix.apiwiz.service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class LicenseServiceImpl implements LicenseService {

	@Autowired
	LicenseBusiness licenseBusiness;

	@Override public ResponseEntity getLicense(String emailId) {
		return null;
	}
	@Override public String updateLicense(String emailId) {
		return null;
	}
	@Override public String deleteLicense(String emailId) {
		return null;
	}
	@Override public ResponseEntity createLicense(LicenseRequest licenseRequest) {
		return null;
	}
	@Override public String getLicenses(String emailId) {
		return null;
	}
}
