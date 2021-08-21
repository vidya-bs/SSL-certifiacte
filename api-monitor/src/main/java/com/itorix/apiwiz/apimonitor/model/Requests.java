package com.itorix.apiwiz.apimonitor.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Requests {
	private String name;

	private String id;

	private String schedulerId;

	private String environmentName;

	private String collectionID;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public void setSchedulerId(String schedulerId) {
		this.schedulerId = schedulerId;
	}

	public String getSchedulerId() {
		return this.schedulerId;
	}

	public void setEnvironmentName(String environmentName) {
		this.environmentName = environmentName;
	}

	public String getEnvironmentName() {
		return this.environmentName;
	}

	public void setCollectionID(String collectionID) {
		this.collectionID = collectionID;
	}

	public String getCollectionID() {
		return this.collectionID;
	}
}
