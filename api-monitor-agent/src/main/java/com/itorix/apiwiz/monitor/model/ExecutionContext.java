package com.itorix.apiwiz.monitor.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExecutionContext {

    private long executionId;
    private String tenant;
    private int globalTimeout = 4000;
    private String schedulerId;
    private String collectionId;
}
