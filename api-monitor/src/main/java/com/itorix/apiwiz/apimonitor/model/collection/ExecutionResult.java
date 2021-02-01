package com.itorix.apiwiz.apimonitor.model.collection;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.apimonitor.model.request.MonitorRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component("executionResult")
@Document(collection = "Monitor.Collections.Events.History")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExecutionResult extends MonitorRequest{

	@Id
	String id;
	String requestId;
	String collectionId;
	String schedulerId;
	long executedTime;
	long latency;
	String status;
	int statusCode;
}
