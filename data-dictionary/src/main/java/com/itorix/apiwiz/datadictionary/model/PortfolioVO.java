package com.itorix.apiwiz.datadictionary.model;

import java.util.List;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.identitymanagement.model.AbstractObject;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Design.Dictionary.List")
// @Document(collection = "Portfolio.Portfolios")
public class PortfolioVO extends AbstractObject {

	private String summary;

	private String description;

	private String name;
	
	private Integer revision = 1;
	
	private String dictionaryId;
	
	@Transient
	private List<Object> models;

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getRevision() {
		if(null == revision)
			return 1;
		return revision;
	}

	public void setRevision(Integer revision) {
		this.revision = revision;
	}

	public String getDictionaryId() {
		return dictionaryId;
	}

	public void setDictionaryId(String dictionaryId) {
		this.dictionaryId = dictionaryId;
	}

	@JsonIgnore
	public List<Object> getModels() {
		return models;
	}

	@JsonIgnore
	public void setModels(List<Object> models) {
		this.models = models;
	}

	public List<Object> getSchemas() {
		return models;
	}

	public void setSchemas(List<Object> models) {
		this.models = models;
	}

	
}
