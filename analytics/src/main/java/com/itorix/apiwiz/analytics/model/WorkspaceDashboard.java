package com.itorix.apiwiz.analytics.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document("Workspace.Dashboard.List")
public class WorkspaceDashboard {
    private LandingPageMetrics landingPageMetrics;
    private PortfolioStats portfolioStats;
    private DesignStudioStats designStudioStats;
    private ProxyStats proxyStats;
    private TestStudioStats testStudioStats;
    private MonitorStats monitorStats;
    private long createdTs;
    private String userId;

    private UserRoleMetrics userRoleMetrics;
}
