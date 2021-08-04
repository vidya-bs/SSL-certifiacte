package com.itorix.apiwiz.identitymanagement.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Users {

	private List<UserDetails> users = null;

	public List<UserDetails> getUsers() {
		return users;
	}

	public void setUsers(List<UserDetails> users) {
		this.users = users;
	}
}
