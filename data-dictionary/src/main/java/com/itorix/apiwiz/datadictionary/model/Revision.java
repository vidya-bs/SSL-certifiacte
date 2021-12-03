package com.itorix.apiwiz.datadictionary.model;

public class Revision {

	private Integer revision = 1;
	private String status;

	public Integer getRevision() {
		return revision;
	}
	public void setRevision(Integer revision) {
		this.revision = revision;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
