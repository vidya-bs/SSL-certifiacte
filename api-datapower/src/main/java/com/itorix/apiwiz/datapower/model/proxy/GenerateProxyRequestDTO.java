package com.itorix.apiwiz.datapower.model.proxy;

import java.util.List;

public class GenerateProxyRequestDTO {
  List<Pipelines> pipelinesList;
  ServiceRegistryRequest serviceRegistryRequest;

  public List<Pipelines> getPipelinesList() {
    return pipelinesList;
  }

  public void setPipelinesList(
      List<Pipelines> pipelinesList) {
    this.pipelinesList = pipelinesList;
  }

  public ServiceRegistryRequest getServiceRegistryRequest() {
    return serviceRegistryRequest;
  }

  public void setServiceRegistryRequest(
      ServiceRegistryRequest serviceRegistryRequest) {
    this.serviceRegistryRequest = serviceRegistryRequest;
  }
}
