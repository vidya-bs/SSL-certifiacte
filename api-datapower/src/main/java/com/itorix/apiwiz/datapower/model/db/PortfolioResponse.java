package com.itorix.apiwiz.datapower.model.db;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.identitymanagement.model.Pagination;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PortfolioResponse {

	private Pagination pagination;
	private Object data;

	public Pagination getPagination() {
		return pagination;
	}

	public void setPagination(Pagination pagination) {
		this.pagination = pagination;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
