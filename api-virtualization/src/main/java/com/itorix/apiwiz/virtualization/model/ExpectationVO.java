package com.itorix.apiwiz.virtualization.model;


import com.itorix.apiwiz.virtualization.model.metadata.ExpectationMetadata;
import com.itorix.hyggee.mockserver.client.serialization.ObjectMapperFactory;
import com.itorix.hyggee.mockserver.client.serialization.model.Metadata;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

@Document(collection = "mock.Expectation")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpectationVO {
	@Id
    private String id;
	private String name;
	private String description;
	private String summary;
	private String groupId;
	private String path;
	private String DTO;
	private Metadata metadata;
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
	@JsonIgnore
	public String getDTO() {
		return DTO;
	}
	public void setDTO(String dTO) {
		DTO = dTO;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ExpectationDTO getExpectation() {
		ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();
		try {
			return objectMapper.readValue(DTO, ExpectationDTO.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public Metadata getMetadata() {
		return metadata;
	}
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getExpectationMetadata() {
		return expectationMetadata;
	}
	public void setExpectationMetadata(String expectationMetadata) {
		this.expectationMetadata = expectationMetadata;
	}
	
}
