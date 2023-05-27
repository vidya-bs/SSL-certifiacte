package com.itorix.apiwiz.devstudio.model.metricsMetadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@JsonInclude(Include.NON_NULL)
@Document(collection = "API.Compliance.Resource.DashboardV2")
@JsonIgnoreProperties({ "_id"})
@CompoundIndexes({
    @CompoundIndex(name = "resource_swagger_app", def = "{'resourceKey.resourceName' : -1, 'resourceKey.swagger': -1}")
})
public class ComplianceSwaggerMetrics {
  @Id
  private String id;
  private ResourceTopKey resourceKey;
  private String swaggerId;
  private long totalRuns;
  private long compliant;
  private long nonCompliant;
  private long safe;
  private long threats;
  private long headerNonCompliant;
  private long formDataNonCompliant;
  private long pathNonCompliant;
  private long queryNonCompliant;
  private long requestNonCompliant;
  private long responseNonCompliant;
  private long sqlSecurityThreats;
  private long jsSecurityThreats;
  private long xPathSecurityThreats;
  private long serverSideSecurityThreats;
  private Boolean isShadowSpec = false;
  private long zombieRuns;
  private long zombieThreats;
  private long zombieNonCompliant;

  private float compliantRating=0;
  private float securityRating=0;
  private float safetyRating=0;
}
