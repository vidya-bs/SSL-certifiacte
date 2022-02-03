package com.itorix.apiwiz.analytics.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkspaceDashboard {
    private LandingPageMetrics landingPageMetrics;
    private PortfolioStats portfolioStats;
    private DesignStudioStats designStudioStats;
    private ProxyStats proxyStats;
    private TestStudioStats testStudioStats;
    private PipelineStats pipelineStats;
    private MonitorStats monitorStats;
}
