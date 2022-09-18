package com.itorix.apiwiz.data.management.model.overview;

import java.util.HashSet;
import java.util.Set;

public class Policy {

	private String name;
	private String type;
	private Set<String> references;
	private Set<String> flowType;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Set<String> getReferences() {
		if (this.references == null) {
			references = new HashSet<>();
			return references;
		} else
			return references;
	}
	public void setReferences(Set<String> references) {
		this.references = references;
	}
	public Set<String> getFlowType() {
		if (this.flowType == null) {
			flowType = new HashSet<>();
			return flowType;
		} else
			return flowType;
	}
	public void setFlowType(Set<String> flowType) {
		this.flowType = flowType;
	}
}
