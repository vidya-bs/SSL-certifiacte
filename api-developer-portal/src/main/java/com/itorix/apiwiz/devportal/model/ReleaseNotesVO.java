package com.itorix.apiwiz.devportal.model;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.model.AbstractObject;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "API.ReleaseNotes")
public class ReleaseNotesVO extends AbstractObject {

	private String year;
	private String oas;
	private String notes;

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
}
