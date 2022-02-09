package com.itorix.apiwiz.analytics.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonitorExecCountByStatus {
    private String monitorCollectionName;
    private String status;
    private Integer count;
}
