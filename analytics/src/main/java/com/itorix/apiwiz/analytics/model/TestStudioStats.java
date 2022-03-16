package com.itorix.apiwiz.analytics.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestStudioStats {
    private Map<String, Integer> topFiveTestsBasedOnSuccessRatio;
    private List<TestSuiteExecutionMetric> testSuiteExecutionMetricsList;
}
