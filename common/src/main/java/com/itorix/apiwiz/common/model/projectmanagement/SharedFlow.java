package com.itorix.apiwiz.common.model.projectmanagement;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SharedFlow {

	@Override
	public String toString() {
		return "SharedFlow [name=" + name + ", organization=" + Arrays.toString(organization) + "]";
	}
	private String name;
	private Organization[] organization;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Organization[] getOrganization() {
		return organization;
	}
	public void setOrganization(Organization[] organization) {
		this.organization = organization;
	}
	
	
	
}
