package com.itorix.apiwiz.design.studio.swaggerdiff.model;

public class DiffVO {

	private String swaggerName;
	private int oldRevision;
	private int newRevision;

	public String getSwaggerName() {
		return swaggerName;
	}

	public void setSwaggerName(String swaggerName) {
		this.swaggerName = swaggerName;
	}

	public int getOldRevision() {
		return oldRevision;
	}

	public void setOldRevision(int oldRevision) {
		this.oldRevision = oldRevision;
	}

	public int getNewRevision() {
		return newRevision;
	}

	public void setNewRevision(int newRevision) {
		this.newRevision = newRevision;
	}
}
