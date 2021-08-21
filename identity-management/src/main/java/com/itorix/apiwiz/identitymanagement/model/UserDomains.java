package com.itorix.apiwiz.identitymanagement.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

@Document(collection = "Users.Domains")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDomains extends AbstractObject {

	public static final String NAME = "userDomains";

	private List<String> domains;

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getDomains() {
		return domains;
	}

	public void setDomains(List<String> domains) {
		this.domains = domains;
	}
}
