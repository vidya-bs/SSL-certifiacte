package com.itorix.apiwiz.validator.license.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LicenseToken {
	private String emailId;
	private String expiry;
	private List<String> nodeIds;
	private LicensePolicy licensePolicy;
	private Set<String> components;
	private String encryptionType;
}
