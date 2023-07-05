package com.itorix.apiwiz.common.model.azure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Connectors.Azure.Configuration")
@Data
public class AzureConfigurationVO {
  private String connectorName;
  private String managementHost;
  private String gatewayHost;
  private String subscriptionId;
  private String resourceGroup;
  private String serviceName;
  private String sharedAccessToken;
  @JsonIgnore
  private String apiVersion = "2021-08-01";
}
