package com.itorix.apiwiz.common.model.proxystudio.apigeeassociations;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Product {

	private String name;
	private List<DevApp> devApps;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<DevApp> getDevApps() {
		return devApps;
	}

	public void setDevApps(List<DevApp> devApps) {
		this.devApps = devApps;
	}
}
