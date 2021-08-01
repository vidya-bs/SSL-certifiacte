package com.itorix.apiwiz.validator.license.model.db;

import com.itorix.apiwiz.common.model.AbstractObject;
import com.itorix.apiwiz.validator.license.model.LicensePolicy;
import com.itorix.apiwiz.validator.license.model.Status;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document(collection = "Apiwiz.License.List")
public class License extends AbstractObject {

	private String emailId;
	private List<String> clientIp;
	private String clientName;
	private List<String> workspaceName;
	private Status status;
	private String expiry;
	private LicensePolicy licensePolicy;
	private String encryptedToken;

}
