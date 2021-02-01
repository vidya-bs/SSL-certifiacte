package com.itorix.apiwiz.data.management.model.overview;

import java.util.List;

public class Apps {
	private String name;

	private List<String> developers;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getDevelopers() {
		return developers;
	}

	public void setDevelopers(List<String> developers) {
		this.developers = developers;
	}

	@Override
	public String toString() {
		return "ClassPojo [name = " + name + ", developers = " + developers + "]";
	}
}