package com.itorix.apiwiz.analytics.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class MonitorStats {

    private Map<String, Integer> topFiveMonitorsBasedOnUptime;

    private Map<String, Long> topFiveMonitorsBasedOnLatency;
}
