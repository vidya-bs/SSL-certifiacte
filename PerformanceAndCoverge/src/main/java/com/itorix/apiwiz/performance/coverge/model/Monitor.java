package com.itorix.apiwiz.performance.coverge.model;

import java.util.List;

public class Monitor {
	private Stats stats;

	private String enviornment;

	private List<Dimensions> dimensions;
	
	private List<Operations> operations;

	private String organisationName;

	private String projectName;

	public Stats getStats() {
		return stats;
	}

	public void setStats(Stats stats) {
		this.stats = stats;
	}

	public String getEnviornment() {
		return enviornment;
	}

	public void setEnviornment(String enviornment) {
		this.enviornment = enviornment;
	}

	public List<Dimensions> getDimensions() {
		return dimensions;
	}

	public void setDimensions(List<Dimensions> dimensions) {
		this.dimensions = dimensions;
	}

	public List<Operations> getOperations() {
		return operations;
	}

	public void setOperations(List<Operations> operations) {
		this.operations = operations;
	}

	public String getOrganisationName() {
		return organisationName;
	}

	public void setOrganisationName(String organisationName) {
		this.organisationName = organisationName;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	

}