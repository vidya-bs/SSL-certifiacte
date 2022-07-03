package com.itorix.apiwiz.analytics.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRoleMetrics {
    private MetricForAdminUser metricForAdminUser;
    private MetricsForOperationsUser metricsForOperationsUser;
    private OtherMetric otherMetric;
}
