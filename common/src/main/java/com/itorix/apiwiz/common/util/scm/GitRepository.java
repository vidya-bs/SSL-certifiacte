package com.itorix.apiwiz.common.util.scm;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GitRepository {

	private String name;
	private String description;

	@JsonProperty("private")
	private String repoPrivate;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@JsonProperty("private")
	public String getRepoPrivate() {
		return repoPrivate;
	}

	@JsonProperty("private")
	public void setRepoPrivate(String repoPrivate) {
		this.repoPrivate = repoPrivate;
	}
}
