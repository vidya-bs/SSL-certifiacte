package com.itorix.apiwiz.common.model.projectmanagement;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Contacts {

	@Override
	public String toString() {
		return "Contacts [name=" + name + ", email=" + email + ", roles=" + roles + "]";
	}

	private String name;
	private String email;
	private Set<String> roles;

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

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

}
