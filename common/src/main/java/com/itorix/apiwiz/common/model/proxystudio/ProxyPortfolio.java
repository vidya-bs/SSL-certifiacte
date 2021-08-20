package com.itorix.apiwiz.common.model.proxystudio;

import java.util.List;

public class ProxyPortfolio {

	private String id;
	private String name;
	private List<ProxyProject> projects;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<ProxyProject> getProjects() {
		return projects;
	}
	public void setProjects(List<ProxyProject> projects) {
		this.projects = projects;
	}

}
