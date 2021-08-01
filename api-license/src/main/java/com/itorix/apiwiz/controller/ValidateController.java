package com.itorix.apiwiz.controller;

import com.itorix.apiwiz.validator.license.business.LicenseBusiness;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ValidateController {

	@Autowired
	LicenseBusiness licenseBusiness;

	@GetMapping(value = "/v1/apiwiz/licenses/{emailId}/validate")
	public ResponseEntity validate(@PathVariable String emailId){
		Map response = new HashMap<String, Bool>();
		boolean licenseValid = licenseBusiness.isLicenseValid(emailId);
		response.put("valid", licenseValid);
		return new ResponseEntity(response, HttpStatus.OK);
	}
}
