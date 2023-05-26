package com.itorix.apiwiz.devstudio.model.metricsMetadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Api.Product.Metric.Metadata.Lists")
public class ProductsMetricMetadata {
  @Id
  private String id;
  private List<String> swaggerIds;
  private String productId;
  private float overAllMaturity;
  private Reliability reliability;
  private DesignGovernance designGovernance;
}
