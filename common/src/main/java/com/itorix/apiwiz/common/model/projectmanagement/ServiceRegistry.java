package com.itorix.apiwiz.common.model.projectmanagement;

import java.util.List;

public class ServiceRegistry {

	private String name;
	private List<RegistryEndpoint> endpoints;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<RegistryEndpoint> getEndpoints() {
		return endpoints;
	}

	public void setEndpoints(List<RegistryEndpoint> endpoints) {
		this.endpoints = endpoints;
	}

	@Override
	public String toString() {
		return "ServiceRegistry [name=" + name + ", endpoints=" + endpoints + "]";
	}
}
