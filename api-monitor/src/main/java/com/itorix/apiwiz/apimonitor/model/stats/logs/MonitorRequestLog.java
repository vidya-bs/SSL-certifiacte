package com.itorix.apiwiz.apimonitor.model.stats.logs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MonitorRequestLog {
	public String collectionID;
	public String collectionName;
	public String requestID;
	public String requestName;
	public long timestamp;
	@JsonProperty("event")
	public LogEvent event;
}
