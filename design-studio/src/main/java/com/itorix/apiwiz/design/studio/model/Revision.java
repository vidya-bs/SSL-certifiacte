package com.itorix.apiwiz.design.studio.model;

public class Revision implements Comparable<Revision> {
	private String id;
	private Integer revision;
	private String status;

	public Revision() {
	}

	public Revision(Integer revision, String status) {
		this.revision = revision;
		this.status = status;
	}

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

	@Override
	public int compareTo(Revision o) {
		// TODO Auto-generated method stub
		return (revision - o.revision);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
