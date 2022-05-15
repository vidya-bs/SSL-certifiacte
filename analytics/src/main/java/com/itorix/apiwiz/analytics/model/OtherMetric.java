package com.itorix.apiwiz.analytics.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OtherMetric {
    private Map<String, Integer> oas2CountByStatus = new HashMap<>();
    private Map<String, Integer> oas3CountByStatus = new HashMap<>();
    private Integer virtualizationRequestsWithoutMatch;
    private Integer noOfTestsWithLessThanFiftyPercentCoverage;
}
