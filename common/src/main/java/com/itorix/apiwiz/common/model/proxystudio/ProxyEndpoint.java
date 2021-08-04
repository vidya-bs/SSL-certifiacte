package com.itorix.apiwiz.common.model.proxystudio;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProxyEndpoint {

	private String name;
	private String basePath;
	private List<String> virtualHosts;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public List<String> getVirtualHosts() {
		return virtualHosts;
	}

	public void setVirtualHosts(List<String> virtualHosts) {
		this.virtualHosts = virtualHosts;
	}
}
