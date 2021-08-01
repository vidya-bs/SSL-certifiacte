package com.itorix.apiwiz.validator.license.service;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.validator.license.model.LicenseRequest;
import com.itorix.apiwiz.validator.license.model.LicenseResponse;
import com.itorix.apiwiz.validator.license.model.db.License;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public interface LicenseService {


	@PostMapping(value = "/v1/apiwiz/licenses")
	public ResponseEntity createLicense(@RequestBody LicenseRequest licenseRequest) throws ItorixException;

	@PutMapping(value = "/v1/apiwiz/licenses/{emailId}")
	public ResponseEntity updateLicense(@PathVariable String emailId, @RequestBody LicenseRequest licenseRequest)
	throws ItorixException;

	@DeleteMapping(value = "/v1/apiwiz/licenses/{emailId}")
	public ResponseEntity deleteLicense(@PathVariable String emailId);

	@GetMapping(value = "/v1/apiwiz/licenses/{emailId}")
	public ResponseEntity<License> getLicense(@PathVariable String emailId);

	@GetMapping(value = "/v1/apiwiz/licenses") public ResponseEntity<LicenseResponse> getLicenses(
			@RequestParam(value = "offset", defaultValue = "1", required = false) int offset,
			@RequestParam(value = "pagesize", defaultValue = "10", required = false) int pageSize);

}
