package com.itorix.apiwiz.monitor.model.stats;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestStats {
	public String collectionID;
	public String collectionName;
	public String requestID;
	public String requestName;
	public String environmentId;
	public int interval;
	public List<Long> timeseries;
	public List<Event> events;
}