package com.itorix.apiwiz.devstudio.model.metricsMetadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MonitorMetrics {
  private int uptime;
  private float avgLatency;
  private float latency;
  private int monitorsCount;
  private String lastOperation;
}
