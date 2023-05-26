package com.itorix.apiwiz.devstudio.model.metricsMetadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DesignGovernance {
  private int totalRuns;
  private float rating;
  private float maturity;
  private int warnings;
  private int info;
  private int errors;
  private String swaggerId;
  private int oasRevision;
}
