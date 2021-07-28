package com.itorix.apiwiz.model.db;

import com.itorix.apiwiz.model.LicensePolicy;
import com.itorix.apiwiz.model.Status;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "Apiwiz.License.List")
public class License extends AbstractObject{
	private String emailId;
	private String name;
	private List<String> clientIp;
	private String clientName;
	private List<String> workspaceName;
	private Status status;
	private String expiry;
	private LicensePolicy licensePolicy;
	private String encryptedToken;
}
