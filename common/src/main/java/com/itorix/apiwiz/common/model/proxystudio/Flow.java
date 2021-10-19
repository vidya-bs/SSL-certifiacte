package com.itorix.apiwiz.common.model.proxystudio;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Flow {
	private String condition;
	private String name;
	private String verb;
	private String description;
	private String path;
	private String targetName;
	private String targetOperation;
	private String targetBasepath;

	/** @return the targetName */
	public String getTargetName() {
		return targetName;
	}

	/**
	 * @param targetName
	 *            the targetName to set
	 */
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	/** @return the targetOperation */
	public String getTargetOperation() {
		return targetOperation;
	}

	/**
	 * @param targetOperation
	 *            the targetOperation to set
	 */
	public void setTargetOperation(String targetOperation) {
		this.targetOperation = targetOperation;
	}

	public String getVerb() {
		return verb;
	}

	public void setVerb(String verb) {
		this.verb = verb;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "[condition = " + condition + ", name = " + name + "]";
	}

	public String getTargetBasepath() {
		return targetBasepath;
	}

	public void setTargetBasepath(String targetBasepath) {
		this.targetBasepath = targetBasepath;
	}
	
	
}
