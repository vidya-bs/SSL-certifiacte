package com.itorix.apiwiz.data.management.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;

@Component
@Document(collection = "Apigee.PolicyMapping")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PolicyMapping {

	public String name;
	public String value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
