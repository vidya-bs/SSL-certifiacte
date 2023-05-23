package com.itorix.apiwiz.devstudio.model.metricsMetadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestsuiteMetrics {
  private int avgSuccessRate;
  private float avgDuration;
  private int testsuiteCount;
  private String lastOperation;
}
