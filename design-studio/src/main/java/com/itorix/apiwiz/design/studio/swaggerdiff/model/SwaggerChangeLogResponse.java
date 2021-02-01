package com.itorix.apiwiz.design.studio.swaggerdiff.model;

import com.itorix.apiwiz.identitymanagement.model.Pagination;

public class SwaggerChangeLogResponse {
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
