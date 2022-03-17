package com.itorix.apiwiz.analytics.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonitorExecutionMetric {
    private String monitorCollectionName;
    private Integer failedExecutionCount;
    private Integer successFullExecutionCount;
    private Integer totalExecutionCount;
    private Double avgLatency;
}
