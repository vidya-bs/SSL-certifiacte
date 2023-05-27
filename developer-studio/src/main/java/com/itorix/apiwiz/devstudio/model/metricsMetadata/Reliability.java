package com.itorix.apiwiz.devstudio.model.metricsMetadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Reliability {
  private float maturity;
  private TestsuiteMetrics testsuiteMetrics;
  private MonitorMetrics monitorMetrics;
}
