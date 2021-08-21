package com.itorix.apiwiz.virtualization.model;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

@Document(collection = "Mock.Groups.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupVO {
	@Id
	private String id;
	private String name;
	private String description;
	private String summary;
	private Metadata metadata;
	private List<Map<Object, Object>> expectations;

	@Transient
	private String mockURL;
	@Transient
	private String tenantId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public List<Map<Object, Object>> getExpectations() {
		return expectations;
	}

	public void setExpectations(List<Map<Object, Object>> expectations) {
		this.expectations = expectations;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getMockURL() {
		return mockURL;
	}

	public void setMockURL(String mockURL) {
		this.mockURL = mockURL;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
}
