package com.itorix.apiwiz.virtualization.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.virtualization.model.metadata.ExpectationMetadata;

@Document(collection = "mock.Expectation")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MockDTO {
	@Id
    private String id;
	private String name;
	private String description;
	private String summary;
	private String groupId;
	private String path;
	private ExpectationDTO expectation;
	private String expectationMetadata;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@JsonIgnore
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public ExpectationDTO getExpectation() {
		return expectation;
	}
	public void setDTO(ExpectationDTO expectation) {
		this.expectation = expectation;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroup(String groupId) {
		this.groupId = groupId;
	}
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
	public String getExpectationMetadata() {
		return expectationMetadata;
	}
	public void setExpectationMetadata(String expectationMetadata) {
		this.expectationMetadata = expectationMetadata;
	}
}
