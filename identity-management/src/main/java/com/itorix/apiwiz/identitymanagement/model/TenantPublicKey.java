package com.itorix.apiwiz.identitymanagement.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Tenant.Public.Key")
@Getter
@Setter
public class TenantPublicKey extends AbstractObject {
	private String tenant;
	private String source;
	private String key;
}
