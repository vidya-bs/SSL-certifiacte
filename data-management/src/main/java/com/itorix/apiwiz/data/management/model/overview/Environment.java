package com.itorix.apiwiz.data.management.model.overview;

import java.util.List;

public class Environment {
	private String name;

	private List<Proxies> proxies;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Proxies> getProxies() {
		return proxies;
	}

	public void setProxies(List<Proxies> proxies) {
		this.proxies = proxies;
	}

	@Override
	public String toString() {
		return "ClassPojo [name = " + name + ", proxies = " + proxies + "]";
	}
}
