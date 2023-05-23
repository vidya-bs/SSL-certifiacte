package com.itorix.apiwiz.devstudio.model.metricsMetadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "API.Compliance.DashboardV2")
@JsonIgnoreProperties({ "_id"})
public class ComplianceOverAllMetrics {
  @Id
  private String id;
  @JsonProperty("totalRuns")
  private long totalRuns;
  @JsonProperty("compliant")
  private long compliant;
  @JsonProperty("nonCompliant")
  private long nonCompliant;
  @JsonProperty("safe")
  private long safe;
  @JsonProperty("threats")
  private long securityThreats;
  @JsonProperty("headerNonCompliant")
  private long headerNonCompliant;
  @JsonProperty("formDataNonCompliant")
  private long formDataNonCompliant;
  @JsonProperty("pathNonCompliant")
  private long pathNonCompliant;
  @JsonProperty("queryNonCompliant")
  private long queryNonCompliant;
  @JsonProperty("requestNonCompliant")
  private long requestNonCompliant;
  @JsonProperty("responseNonCompliant")
  private long responseNonCompliant;
  @JsonProperty("sqlSecurityThreats")
  private long sqlSecurityThreats;
  @JsonProperty("jsSecurityThreats")
  private long jsSecurityThreats;
  @JsonProperty("xPathSecurityThreats")
  private long xPathSecurityThreats;
  @JsonProperty("serverSideSecurityThreats")
  private long serverSideSecurityThreats;
  @JsonProperty("zombieRuns")
  private long zombieSpecs;
  @JsonProperty("shadowRuns")
  private long shadowSpecs;
  @JsonProperty("zombieThreats")
  private long zombieThreats;
  @JsonProperty("zombieSafe")
  private long zombieSafe;
  @JsonProperty("shadowThreats")
  private long shadowThreats;
  @JsonProperty("shadowSafe")
  private long shadowSafe;
  private float compliantRating=0;
  private float securityRating=0;
}
