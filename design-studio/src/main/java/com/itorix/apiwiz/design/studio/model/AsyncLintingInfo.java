package com.itorix.apiwiz.design.studio.model;

import java.util.List;

public class AsyncLintingInfo {

  private String asyncApiId;
  private Integer revision;
  private List<String> ruleSetIds;
  private String workspaceId;

  public String getAsyncApiId() {
    return asyncApiId;
  }

  public void setAsyncApiId(String asyncApiId) {
    this.asyncApiId = asyncApiId;
  }

  public Integer getRevision() {
    return revision;
  }

  public void setRevision(Integer revision) {
    this.revision = revision;
  }

  public List<String> getRuleSetIds() {
    return ruleSetIds;
  }

  public void setRuleSetIds(List<String> ruleSetIds) {
    this.ruleSetIds = ruleSetIds;
  }

  public String getWorkspaceId() {
    return workspaceId;
  }

  public void setWorkspaceId(String workspaceId) {
    this.workspaceId = workspaceId;
  }
}