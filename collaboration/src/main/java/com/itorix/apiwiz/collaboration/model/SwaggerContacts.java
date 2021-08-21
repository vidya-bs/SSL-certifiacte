package com.itorix.apiwiz.collaboration.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SwaggerContacts {
	private String name;
	private String email;
	private List<String> role;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<String> getRole() {
		return role;
	}

	public void setRole(List<String> role) {
		this.role = role;
	}

	@Override
	public String toString() {
		return "SwaggerContacts [name=" + name + ", email=" + email + ", role=" + role + "]";
	}
}
