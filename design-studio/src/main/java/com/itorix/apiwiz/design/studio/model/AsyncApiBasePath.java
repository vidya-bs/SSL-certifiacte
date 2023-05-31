package com.itorix.apiwiz.design.studio.model;

import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Design.AsyncApi.BasePath")
public class AsyncApiBasePath {
	private String name;

	public List<String> getBasePath() {
		return basePath;
	}

	public void setBasePath(List<String> basePath) {
		this.basePath = basePath;
	}

	private List<String> basePath;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
