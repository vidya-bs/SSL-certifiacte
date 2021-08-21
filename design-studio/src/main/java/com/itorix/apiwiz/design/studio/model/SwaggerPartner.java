package com.itorix.apiwiz.design.studio.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Design.Swagger.Partner")
public class SwaggerPartner {

	@Id
	private String id;
	private String partnerName;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getPartnerName() {
		return partnerName;
	}

	public void setPartnerName(String partnerName) {
		this.partnerName = partnerName;
	}
}
