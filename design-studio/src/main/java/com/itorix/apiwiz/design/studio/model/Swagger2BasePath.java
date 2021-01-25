package com.itorix.apiwiz.design.studio.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Swagger2.BasePath")
public class Swagger2BasePath {
	private String name;
	private String basePath;
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
}
