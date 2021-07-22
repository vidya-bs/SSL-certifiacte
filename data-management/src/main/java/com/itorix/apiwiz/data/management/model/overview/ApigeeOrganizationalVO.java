package com.itorix.apiwiz.data.management.model.overview;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.itorix.apiwiz.identitymanagement.model.AbstractObject;

@Document(collection = "Apigee.Organization.Overview")
public class ApigeeOrganizationalVO extends AbstractObject {
	private List<Environment> environment;

	private String name;

	@JsonIgnore
	private String type;

	public List<Environment> getEnvironment() {
		return environment;
	}

	public void setEnvironment(List<Environment> environment) {
		this.environment = environment;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "ApigeeOverView [environment=" + environment + ", name=" + name + "]";
	}
}
