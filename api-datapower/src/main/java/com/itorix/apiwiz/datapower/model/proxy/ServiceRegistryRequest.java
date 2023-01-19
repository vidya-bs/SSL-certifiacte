package com.itorix.apiwiz.datapower.model.proxy;

import java.util.List;

public class ServiceRegistryRequest {

  private Metadata metadata;
  private List<ServiceRegistry> serviceRegistry;

  public Metadata getMetadata() {
    return metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  public List<ServiceRegistry> getServiceRegistry() {
    return serviceRegistry;
  }

  public void setServiceRegistry(
      List<ServiceRegistry> serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }
}