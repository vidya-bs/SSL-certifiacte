package com.itorix.apiwiz.apimonitor.model;

import java.util.List;

public class SchedulerResponse {
	private List<Requests> requests;

	public void setRequests(List<Requests> requests) {
		this.requests = requests;
	}

	public List<Requests> getRequests() {
		return this.requests;
	}
}
