package com.itorix.apiwiz.ibm.apic.connector.model;

import java.io.Serializable;
public class APIDropdownListItem implements Serializable {
	private String swaggerId;
	private Integer revision;
	private String oasType;
	private String name;

	public APIDropdownListItem() {
	}

	public APIDropdownListItem(String swaggerId, Integer revision, String oasType, String name) {
		this.swaggerId = swaggerId;
		this.revision = revision;
		this.oasType = oasType;
		this.name = name;
	}

	public String getSwaggerId() {
		return swaggerId;
	}
	public void setSwaggerId(String swaggerId) {
		this.swaggerId = swaggerId;
	}
	public Integer getRevision() {
		return revision;
	}
	public void setRevision(Integer revision) {
		this.revision = revision;
	}
	public String getOasType() {
		return oasType;
	}
	public void setOasType(String oasType) {
		this.oasType = oasType;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
