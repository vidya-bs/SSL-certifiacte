package com.itorix.apiwiz.validator.license.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
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
	private String encryptionType;
}
