package com.itorix.apiwiz.apimonitor.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonitorCollectionsResponse {
	private String id;
	private String name;
	private String summary;
	private long latency;
	private int uptime;
	private Long cts;
	private Long mts;
	private String createdBy;
	private String modifiedBy;
}
