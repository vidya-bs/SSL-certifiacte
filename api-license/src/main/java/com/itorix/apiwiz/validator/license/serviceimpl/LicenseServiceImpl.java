package com.itorix.apiwiz.validator.license.serviceimpl;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.validator.license.business.LicenseBusiness;
import com.itorix.apiwiz.validator.license.model.LicenseRequest;
import com.itorix.apiwiz.validator.license.model.LicenseResponse;
import com.itorix.apiwiz.validator.license.model.db.License;
import com.itorix.apiwiz.validator.license.service.LicenseService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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

	@ApiOperation(value = "Create License", notes = "", code = 201)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Created"),
			@ApiResponse(code = 400, message = "Request validation failed. License already exists for the email %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class) })
	@Override
	public ResponseEntity<?> createLicense(LicenseRequest licenseRequest) {
		try {
			licenseBusiness.createLicense(licenseRequest);
		} catch (ItorixException e) {
			return new ResponseEntity(new ErrorObj(e.getMessage(), e.getErrorCode()), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity(HttpStatus.CREATED);
	}

	@ApiOperation(value = "Update License", notes = "", code = 202)
	@ApiResponses(value = { @ApiResponse(code = 202, message = "Accepted"),
			@ApiResponse(code = 400, message = "Request validation failed. No license exists for the email %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class) })
	@Override
	public ResponseEntity updateLicense(String emailId, LicenseRequest licenseRequest) {
		try {
			licenseBusiness.updateLicense(emailId, licenseRequest);
		} catch (ItorixException e) {
			return new ResponseEntity(new ErrorObj(e.getMessage(), e.getErrorCode()), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity(HttpStatus.ACCEPTED);
	}

	@ApiOperation(value = "Delete License", notes = "", code = 202) @ApiResponses(value = {
			@ApiResponse(code = 202, message = "Accepted"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@Override public ResponseEntity deleteLicense(String emailId) {
		licenseBusiness.deleteLicense(emailId);
		return new ResponseEntity(HttpStatus.ACCEPTED);
	}

	@ApiOperation(value = "Get License", notes = "", code = 200) @ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@Override
	public ResponseEntity<License> getLicense(String emailId) {
		License license = licenseBusiness.getLicense(emailId);
		if(license != null) {
			return new ResponseEntity<License>(license, HttpStatus.OK);
		} else {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
	}


	@ApiOperation(value = "Get All Licenses", notes = "", code = 200) @ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@Override
	public ResponseEntity<LicenseResponse> getLicenses(int offset, int pageSize) {
		LicenseResponse licenses = licenseBusiness.getLicenses(offset, pageSize);
		return new ResponseEntity<>(licenses, HttpStatus.OK);
	}
}
