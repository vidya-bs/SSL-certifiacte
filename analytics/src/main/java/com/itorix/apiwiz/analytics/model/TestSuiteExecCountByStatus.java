package com.itorix.apiwiz.analytics.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestSuiteExecCountByStatus {
    private String testSuiteName;
    private String status;
    private Integer count;
}
