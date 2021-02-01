package com.itorix.apiwiz.apimonitor.model.stats;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Event {
	public long timestamp;
	public String eventID;
	public String status;
	public long latency;
}
