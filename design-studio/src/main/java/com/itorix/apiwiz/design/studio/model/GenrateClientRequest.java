package com.itorix.apiwiz.design.studio.model;

public class GenrateClientRequest {

	private String swaggerName;
	private Integer revision;

	public String getSwaggerName() {
		return swaggerName;
	}

	public void setSwaggerName(String swaggerName) {
		this.swaggerName = swaggerName;
	}

	public Integer getRevision() {
		return revision;
	}

	public void setRevision(Integer revision) {
		this.revision = revision;
	}

}
