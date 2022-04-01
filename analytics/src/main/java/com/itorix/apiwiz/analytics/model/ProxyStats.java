package com.itorix.apiwiz.analytics.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ProxyStats {

    private Map<String, Double> topFiveProxiesBasedOnCoverage;

    private List<PipelineStats> pipelineStatsList;
}
