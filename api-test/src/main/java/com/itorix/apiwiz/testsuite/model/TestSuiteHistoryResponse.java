package com.itorix.apiwiz.testsuite.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@Document(collection = "TestSuites.testSuiteHistoryResponse")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestSuiteHistoryResponse {

	private Pagination pagination;


	private List<TestSuiteResponse> responses;



	public List<TestSuiteResponse> getData() {
		return responses;
	}

	public void setResponses(List<TestSuiteResponse> responses) {
		this.responses = responses;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TestSuiteHistoryResponse [counter=");
		builder.append(pagination.getTotal());
		builder.append(", offset=");
		builder.append(pagination.getOffset());
		builder.append(", responses=");
		builder.append(responses);
		builder.append("]");
		return builder.toString();
	}

	public Pagination getPagination() {
		return pagination;
	}

	public void setPagination(Pagination pagination) {
		this.pagination = pagination;
	}

}