package com.itorix.apiwiz.analytics.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestSuiteExecutionMetric {
	private String testSuiteName;
	private Integer failedExecutionCount;
	private Integer successFullExecutionCount;
	private Integer totalExecutionCount;
	private Integer averageExecutionTime;
}
