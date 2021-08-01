package com.itorix.apiwiz.validator.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LicenseToken {
	private String emailId;
	private String expiry;
	private List<String> nodeIds;
	private LicensePolicy licensePolicy;

	public LicenseToken() {

	}
}
