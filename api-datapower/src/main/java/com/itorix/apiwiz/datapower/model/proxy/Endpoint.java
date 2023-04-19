package com.itorix.apiwiz.datapower.model.proxy;

import java.io.Serializable;

public class Endpoint implements Serializable{
  private String name;
  private String url;
  private String environment;
  private String state;
  private String regions;
  private String envlbl;
  private String urn;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getRegions() {
    return regions;
  }

  public void setRegions(String regions) {
    this.regions = regions;
  }

  public String getEnvlbl() {
    return envlbl;
  }

  public void setEnvlbl(String envlbl) {
    this.envlbl = envlbl;
  }

  public String getUrn() {
    return urn;
  }

  public void setUrn(String urn) {
    this.urn = urn;
  }
}
