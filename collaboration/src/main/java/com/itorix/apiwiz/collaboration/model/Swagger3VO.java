package com.itorix.apiwiz.collaboration.model;

import java.util.Map;
import java.util.Set;

import org.springframework.data.mongodb.core.mapping.Document;

import com.itorix.apiwiz.identitymanagement.model.AbstractObject;

@Document(collection = "Design.Swagger3.List")
public class Swagger3VO extends AbstractObject {

	private Integer revision;

	private String status;

	private String name;

	private Boolean lock;

	private String description;

	private String swagger;

	private Map<String, String> proxies;

	private Set<String> teams;
	private Set<String> products;
	private Set<String> portfolios;

	public Integer getRevision() {
		return revision;
	}

	public void setRevision(Integer revision) {
		this.revision = revision;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getLock() {
		return lock;
	}

	public void setLock(Boolean lock) {
		this.lock = lock;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSwagger() {
		return swagger;
	}

	public void setSwagger(String swagger) {
		this.swagger = swagger;
	}

	public Map<String, String> getProxies() {
		return proxies;
	}

	public void setProxies(Map<String, String> proxies) {
		this.proxies = proxies;
	}

	public Set<String> getTeams() {
		return teams;
	}

	public void setTeams(Set<String> teams) {
		this.teams = teams;
	}

	public Set<String> getProducts() {
		return products;
	}

	public void setProducts(Set<String> products) {
		this.products = products;
	}

	public Set<String> getPortfolios() {
		return portfolios;
	}

	public void setPortfolios(Set<String> portfolios) {
		this.portfolios = portfolios;
	}
}
