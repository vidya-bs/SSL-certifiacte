package com.itorix.apiwiz.datapower.model.proxy;

import java.io.Serializable;
import java.util.List;

public class ServiceRegistry implements Serializable {
  private String name;
  private String org;
  private List<Endpoint> endpoints;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getOrg() {
    return org;
  }

  public void setOrg(String org) {
    this.org = org;
  }

  public List<Endpoint> getEndpoints() {
    return endpoints;
  }

  public void setEndpoints(List<Endpoint> endpoints) {
    this.endpoints = endpoints;
  }
}
