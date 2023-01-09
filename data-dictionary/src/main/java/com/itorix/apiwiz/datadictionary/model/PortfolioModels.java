package com.itorix.apiwiz.datadictionary.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PortfolioModels {
  private String modelId;
  private Integer Revision;
  private List<String> ruleSetIds;

  public PortfolioModels() {
  }

  public PortfolioModels(String modelId, Integer revision, List<String> ruleSetIds) {
    this.modelId = modelId;
    Revision = revision;
    this.ruleSetIds = ruleSetIds;
  }

  public String getModelId() {
    return modelId;
  }

  public void setModelId(String modelId) {
    this.modelId = modelId;
  }

  public Integer getRevision() {
    return Revision;
  }

  public void setRevision(Integer revision) {
    Revision = revision;
  }

  public List<String> getRuleSetIds() {
    return ruleSetIds;
  }

  public void setRuleSetIds(List<String> ruleSetIds) {
    this.ruleSetIds = ruleSetIds;
  }
}