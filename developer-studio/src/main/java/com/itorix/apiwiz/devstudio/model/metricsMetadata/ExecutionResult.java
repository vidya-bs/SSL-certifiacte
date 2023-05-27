package com.itorix.apiwiz.devstudio.model.metricsMetadata;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component("executionResult")
@Document(collection = "Monitor.Collections.Events.History")
public class ExecutionResult {

    @Id
    String id;
    String requestId;
    String collectionId;
    String environmentId;
    String schedulerId;
    long executedTime;
    long scheduledTime;
    long latency;
    String status;
    int statusCode;
    private String collectionCreatedBy;
    String complianceId;
}
