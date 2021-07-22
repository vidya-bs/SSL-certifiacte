package com.itorix.apiwiz.common.model.proxystudio.apigeeassociations;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Deployments {
	private String org;
	private String env;
	private String type;
	List<Proxy> proxies;

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public List<Proxy> getProxies() {
		return proxies;
	}

	public void setProxies(List<Proxy> proxies) {
		this.proxies = proxies;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
