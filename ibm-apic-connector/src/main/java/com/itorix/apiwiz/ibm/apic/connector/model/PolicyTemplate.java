package com.itorix.apiwiz.ibm.apic.connector.model;

import com.itorix.apiwiz.common.model.proxystudio.Policy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("Connectors.Apigee.Policy.Templates")
public class PolicyTemplate {
	@Id
	private String id;

	private String type;
	private String description;
	private String name;
	private List<Policy> policies;

	public PolicyTemplate(String id, String type, String description, String name, List<Policy> policies) {
		this.id = id;
		this.type = type;
		this.description = description;
		this.name = name;
		this.policies = policies;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
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
}
