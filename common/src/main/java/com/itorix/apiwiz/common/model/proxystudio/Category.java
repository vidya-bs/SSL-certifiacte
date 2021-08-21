package com.itorix.apiwiz.common.model.proxystudio;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;

@Component("category")
@Document(collection = "Connectors.Apigee.Policy.Templates")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Category {
	private String type;
	private String name;
	private String description;
	private List<Policy> policies;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Policy> getPolicies() {
		return policies;
	}

	public void setPolicies(List<Policy> policies) {
		this.policies = policies;
	}

	public void addPolicy(Policy policy) {
		if (this.policies != null)
			this.policies.add(policy);
		else {
			this.policies = new ArrayList<Policy>();
			this.policies.add(policy);
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
