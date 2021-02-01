package com.itorix.apiwiz.common.model.proxystudio.apigeeassociations;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProxyApigeeDetails {

	private String name;
	private List<Deployments> deployments ;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Deployments> getDeployments() {
		return deployments;
	}
	public void setDeployments(List<Deployments> deployments) {
		this.deployments = deployments;
	}
	
}
