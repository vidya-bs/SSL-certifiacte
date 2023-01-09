package com.itorix.apiwiz.serviceregistry.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


@JsonInclude(Include.NON_NULL)
public class NameIdContainer {
	String id;
	String name;

	public String getId() {
		return id;
	}

	public NameIdContainer(String id, String name) {
		this.id = id;
		this.name = name;
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

	public NameIdContainer() {
	}
}
