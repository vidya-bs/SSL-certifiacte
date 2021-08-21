package com.itorix.apiwiz.cicd.beans;

import java.util.List;

import org.springframework.data.annotation.Id;

public class ProjectGroup {

	@Id
	private String name;

	private List<String> pipelines;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getPipelines() {
		return pipelines;
	}

	public void setPipelines(List<String> pipelines) {
		this.pipelines = pipelines;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ProjectGroup [name=");
		builder.append(name);
		builder.append(", pipelines=");
		builder.append(pipelines);
		builder.append("]");
		return builder.toString();
	}
}
