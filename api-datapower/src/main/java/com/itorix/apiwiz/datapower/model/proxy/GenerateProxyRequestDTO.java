package com.itorix.apiwiz.datapower.model.proxy;

import java.io.Serializable;
import java.util.List;

public class GenerateProxyRequestDTO implements Serializable{
  List<Pipelines> pipelinesList;
  ServiceRegistryRequest serviceRegistryRequest;

  DesignArtifacts designArtifacts;

  public DesignArtifacts getDesignArtifacts() {
    return designArtifacts;
  }

  public void setDesignArtifacts(DesignArtifacts designArtifacts) {
    this.designArtifacts = designArtifacts;
  }

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
