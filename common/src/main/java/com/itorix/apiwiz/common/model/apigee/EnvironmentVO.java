package com.itorix.apiwiz.common.model.apigee;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnvironmentVO {

	private String environment;
	private List<String> caches;
	private List<String> keyValueMaps;
	private List<String> targetServers;
	private List<String> virtualHosts;

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public List<String> getCaches() {
		return caches;
	}

	public void setCaches(List<String> caches) {
		this.caches = caches;
	}

	public List<String> getKeyValueMaps() {
		return keyValueMaps;
	}

	public void setKeyValueMaps(List<String> keyValueMaps) {
		this.keyValueMaps = keyValueMaps;
	}

	public List<String> getTargetServers() {
		return targetServers;
	}

	public void setTargetServers(List<String> targetServers) {
		this.targetServers = targetServers;
	}

	public List<String> getVirtualHosts() {
		return virtualHosts;
	}

	public void setVirtualHosts(List<String> virtualHosts) {
		this.virtualHosts = virtualHosts;
	}
}
