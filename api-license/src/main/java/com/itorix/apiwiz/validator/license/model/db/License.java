package com.itorix.apiwiz.validator.license.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.validator.license.model.LicensePolicy;
import com.itorix.apiwiz.validator.license.model.Status;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Document(collection = "Apiwiz.License.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class License {

	@Id
	private String id;
	private String emailId;
//	private String userName;
//	@JsonIgnore
//	private String password;
	private List<String> clientIp;
	private String clientName;
	private List<String> workspaceName;
	private Status status;
	private String expiry;
	private LicensePolicy licensePolicy;
	private String encryptedToken;
	private Long cts;
	private String createdUserName;
	private String modifiedUserName;
	private Long mts;
	private Set<String> components;
	private String encryptionType;
	@JsonIgnore
	private int auditCount;
}
