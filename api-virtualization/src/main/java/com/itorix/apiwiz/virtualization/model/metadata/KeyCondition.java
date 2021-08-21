package com.itorix.apiwiz.virtualization.model.metadata;

public class KeyCondition {
	private String pattern;

	private String type;

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "ClassPojo [pattern = " + pattern + ", type = " + type + "]";
	}
}
