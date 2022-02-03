package com.itorix.apiwiz.analytics.beans.monitor;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "Monitor.Collections.Events.History")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExecutionResult extends MonitorRequest {

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
