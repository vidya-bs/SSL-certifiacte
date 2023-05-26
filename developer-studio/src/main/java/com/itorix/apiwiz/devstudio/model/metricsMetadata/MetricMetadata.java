package com.itorix.apiwiz.devstudio.model.metricsMetadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Api.Portfolio.Metric.Metadata.Lists")
public class MetricMetadata {
  @Id
  private String id;
  private String serviceId;
  private String swaggerId;
  private int oasRevision;
  private String portfolioId;
  private float overAllMaturity;
  private DesignGovernance designGovernance;
  private BuildGovernance buildGovernance;
  private Reliability reliability;
  private float designMaturity=0;
  private float buildMaturity=0;
  private float reliabilityMaturity=0;
}
