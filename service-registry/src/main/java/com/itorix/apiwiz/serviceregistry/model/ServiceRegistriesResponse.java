package com.itorix.apiwiz.serviceregistry.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class ServiceRegistriesResponse {

	List<ServiceRegistriesResponseWrapper> serviceRegistries = new ArrayList<>();

	public List<ServiceRegistriesResponseWrapper> getServiceRegistries() {
		return serviceRegistries;
	}

	public void setServiceRegistries(
			List<ServiceRegistriesResponseWrapper> serviceRegistries) {
		this.serviceRegistries = serviceRegistries;
	}
}
