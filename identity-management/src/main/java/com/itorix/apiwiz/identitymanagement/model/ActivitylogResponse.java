package com.itorix.apiwiz.identitymanagement.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivitylogResponse {

	private Pagination pagination;
	private List<ActivityLog> data;

	public List<ActivityLog> getData() {
		return data;
	}

	public void setData(List<ActivityLog> data) {
		this.data = data;
	}

	public Pagination getPagination() {
		return pagination;
	}

	public void setPagination(Pagination pagination) {
		this.pagination = pagination;
	}
}
