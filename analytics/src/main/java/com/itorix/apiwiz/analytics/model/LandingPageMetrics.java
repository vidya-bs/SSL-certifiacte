package com.itorix.apiwiz.analytics.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LandingPageMetrics {
    private int numberOfPortfoliosCreated;
    private int numberOfSwaggersCreated;
    private int numberOfProxiesCreated;
    private int numberOfMockScenariosCreated;
    private int numberOfTestSuitesCreated;
    private int numberOfPipelinesCreated;
    private int numberOfMonitorCollectionsCreated;
    private int numberOfTestsExecuted;
    private int numberOfPipelinesTriggered;
}
