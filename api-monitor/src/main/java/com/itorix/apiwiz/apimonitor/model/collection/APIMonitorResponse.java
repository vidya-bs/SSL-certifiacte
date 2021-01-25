package com.itorix.apiwiz.apimonitor.model.collection;

import com.itorix.apiwiz.identitymanagement.model.Pagination;

public class APIMonitorResponse {

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
