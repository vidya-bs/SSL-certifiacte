package com.itorix.apiwiz.service;

import com.itorix.apiwiz.model.LicenseRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public interface LicenseService {


	@GetMapping(value = "/v1/apiwiz/licenses/{emailId}")
	public ResponseEntity getLicense(String emailId);

	@PutMapping(value = "/v1/apiwiz/licenses/{emailId}")
	public String updateLicense(String emailId);

	@DeleteMapping(value = "/v1/apiwiz/licenses/{emailId}")
	public String deleteLicense(String emailId);

	@PostMapping(value = "/v1/apiwiz/licenses")
	public ResponseEntity createLicense(@RequestBody LicenseRequest licenseRequest);

	@PostMapping(value = "/v1/apiwiz/licenses")
	public String getLicenses(String emailId);

}
