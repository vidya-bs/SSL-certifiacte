package com.itorix.apiwiz.datadictionary.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PortfolioReport {
  private String portfolioId;
  private List<PortfolioModels> models;

  public PortfolioReport() {
  }

  public PortfolioReport(String portfolioId, List<PortfolioModels> models) {
    this.portfolioId = portfolioId;
    this.models = models;
  }

  public String getPortfolioId() {
    return portfolioId;
  }

  public void setPortfolioId(String portfolioId) {
    this.portfolioId = portfolioId;
  }

  public List<PortfolioModels> getModels() {
    return models;
  }

  public void setModels(List<PortfolioModels> models) {
    this.models = models;
  }
}