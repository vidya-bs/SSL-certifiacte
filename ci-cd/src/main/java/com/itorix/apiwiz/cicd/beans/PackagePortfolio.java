package com.itorix.apiwiz.cicd.beans;

import java.util.List;

public class PackagePortfolio {
	private String portfolioId;
	private List<String> projects;

	public String getPortfolioId() {
		return portfolioId;
	}

	public void setPortfolioId(String portfolioId) {
		this.portfolioId = portfolioId;
	}

	public List<String> getProjects() {
		return projects;
	}

	public void setProjects(List<String> projects) {
		this.projects = projects;
	}
}
