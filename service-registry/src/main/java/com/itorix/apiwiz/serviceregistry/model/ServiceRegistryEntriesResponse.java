package com.itorix.apiwiz.serviceregistry.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
public class ServiceRegistryEntriesResponse {

	String name;
	String serviceRregistryId;
	List<Map<String, String>> data;

	public ServiceRegistryEntriesResponse() {
	}

	public ServiceRegistryEntriesResponse(String name, String serviceRregistryId,
			List<Map<String, String>> data) {
		this.name = name;
		this.serviceRregistryId = serviceRregistryId;
		this.data = data;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServiceRregistryId() {
		return serviceRregistryId;
	}

	public void setServiceRregistryId(String serviceRregistryId) {
		this.serviceRregistryId = serviceRregistryId;
	}

	public List<Map<String, String>> getData() {
		return data;
	}

	public void setData(List<Map<String, String>> data) {
		this.data = data;
	}
}
