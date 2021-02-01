package com.itorix.apiwiz.data.management.model.overview;

import java.util.List;

public class Products {
	private List<Apps> apps;

	private String name;

	public List<Apps> getApps() {
		return apps;
	}

	public void setApps(List<Apps> apps) {
		this.apps = apps;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "ClassPojo [apps = " + apps + ", name = " + name + "]";
	}
}