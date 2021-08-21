package com.itorix.apiwiz.monitor.model.execute;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.monitor.model.request.MonitorRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component("executionResult")
@Document(collection = "Monitor.Collections.Events.History")
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
