package com.itorix.apiwiz.common.model;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.model.AbstractObject;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Design.API.Changelog.List")
public class SwaggerChangeLog extends AbstractObject {

	private String year;
	private String oas;
	private String notes;
	private String swaggerName;
	private String oldRevision;
	private String newRevision;
	private String swaggerId;
	private String summary;

	public SwaggerChangeLog(String year, String oas, String notes, String swaggerName, String oldRevision,
			String newRevision, String swaggerId, String summary) {
		super();
		this.year = year;
		this.oas = oas;
		this.notes = notes;
		this.swaggerName = swaggerName;
		this.oldRevision = oldRevision;
		this.newRevision = newRevision;
		this.swaggerId = swaggerId;
		this.summary = summary;
	}

	public String getOldRevision() {
		return oldRevision;
	}

	public void setOldRevision(String oldRevision) {
		this.oldRevision = oldRevision;
	}

	public String getNewRevision() {
		return newRevision;
	}

	public void setNewRevision(String newRevision) {
		this.newRevision = newRevision;
	}

	public String getSwaggerId() {
		return swaggerId;
	}

	public void setSwaggerId(String swaggerId) {
		this.swaggerId = swaggerId;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getOas() {
		return oas;
	}

	public void setOas(String oas) {
		this.oas = oas;
	}

	public String getSwaggerName() {
		return swaggerName;
	}

	public void setSwaggerName(String swaggerName) {
		this.swaggerName = swaggerName;
	}
}
