package com.itorix.apiwiz.datapower.model.proxy;

import java.io.Serializable;

public class ScmHistory implements Serializable {

  private boolean gitPush;
  private String gitRepoName;
  private String gitBranch;
  private String pipelineName;

  private long cts;

  private String createdBy;

  private String createdUser;

  public ScmHistory(boolean gitPush, String gitRepoName, String gitBranch, String pipelineName,
      long cts, String createdBy, String createdUser) {
    this.gitPush = gitPush;
    this.gitRepoName = gitRepoName;
    this.gitBranch = gitBranch;
    this.pipelineName = pipelineName;
    this.cts = cts;
    this.createdBy = createdBy;
    this.createdUser = createdUser;
  }

  public ScmHistory() {
  }

  public boolean isGitPush() {
    return gitPush;
  }

  public void setGitPush(boolean gitPush) {
    this.gitPush = gitPush;
  }

  public String getGitRepoName() {
    return gitRepoName;
  }

  public void setGitRepoName(String gitRepoName) {
    this.gitRepoName = gitRepoName;
  }

  public String getGitBranch() {
    return gitBranch;
  }

  public void setGitBranch(String gitBranch) {
    this.gitBranch = gitBranch;
  }

  public String getPipelineName() {
    return pipelineName;
  }

  public long getCts() {
    return cts;
  }

  public void setCts(long cts) {
    this.cts = cts;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public void setPipelineName(String pipelineName) {
    this.pipelineName = pipelineName;
  }
}
