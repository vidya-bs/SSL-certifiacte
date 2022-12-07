package com.itorix.apiwiz.serviceregistry.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Objects;


@JsonInclude(Include.NON_NULL)
public class ServiceRegistriesResponseWrapper {

	String projectId;
	String proxyName;
	List<NameIdContainer> serviceRegistryNames = new ArrayList<>();

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ServiceRegistriesResponseWrapper that = (ServiceRegistriesResponseWrapper) o;
		return projectId.equals(that.projectId) && proxyName.equals(that.proxyName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectId, proxyName);
	}

	public String getProxyName() {
		return proxyName;
	}

	public void setProxyName(String proxyName) {
		this.proxyName = proxyName;
	}

	public List<NameIdContainer> getServiceRegistryNames() {
		return serviceRegistryNames;
	}

	public void setServiceRegistryNames(
			List<NameIdContainer> serviceRegistryNames) {
		this.serviceRegistryNames = serviceRegistryNames;
	}
}
