package com.itorix.apiwiz.validator.license.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class LicenseRequest {

	//private String userName;
	//private String password;
	private String emailId;
	private List<String> clientIp;
	private String clientName;
	private List<String> workspaceName;
	private String	status;
	private String expiry;
	private LicensePolicy licensePolicy;
	private Set<String> components;

}
