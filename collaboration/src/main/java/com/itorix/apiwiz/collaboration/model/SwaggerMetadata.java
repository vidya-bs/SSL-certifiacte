package com.itorix.apiwiz.collaboration.model;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Design.Swagger.Metadata")
public class SwaggerMetadata {
	@Indexed
	@Id
	private String id;
	private String swaggerName;
	private String oas;
	private String swaggerId;
	private Set<String> teams;
	private Set<String> products;
	private Set<String> portfolios;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSwaggerName() {
		return swaggerName;
	}
	public void setSwaggerName(String swaggerName) {
		this.swaggerName = swaggerName;
	}
	public String getOas() {
		return oas;
	}
	public void setOas(String oas) {
		this.oas = oas;
	}
	public String getSwaggerId() {
		return swaggerId;
	}
	public void setSwaggerId(String swaggerId) {
		this.swaggerId = swaggerId;
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
