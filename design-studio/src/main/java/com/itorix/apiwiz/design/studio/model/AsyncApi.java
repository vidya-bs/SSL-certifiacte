package com.itorix.apiwiz.design.studio.model;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.itorix.apiwiz.design.studio.model.swagger.sync.StatusHistory;
import com.itorix.apiwiz.identitymanagement.model.AbstractObject;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Design.AsyncApi.List")
@JsonInclude(Include.NON_NULL)
public class AsyncApi extends AbstractObject {
  private Integer revision;

  private Status status;

  private String name;

  private String asyncApiId;

  private Boolean lock;
  private List<String> ruleSetIds;

  private String lockedBy;

  private Long lockedAt;

  private String lockedByUserId;

  private String description;

  private String asyncApi;
  private boolean enableScm;
  private String repoName;
  private String branch;
  private String hostUrl;
  private String folderName;
  private String token;
  private String scmSource;
  private String username;
  private String password;
  private String authType;
  private List<StatusHistory> history;
  public List<String> getRuleSetIds() {
    return ruleSetIds;
  }

  public void setRuleSetIds(List<String> ruleSetIds) {
    this.ruleSetIds = ruleSetIds;
  }

  public Integer getRevision() {
    return revision;
  }

  public void setRevision(Integer revision) {
    this.revision = revision;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Boolean getLock() {
    return lock;
  }

  public void setLock(Boolean lock) {
    this.lock = lock;
  }

  public String getLockedBy() {
    return lockedBy;
  }

  public void setLockedBy(String lockedBy) {
    this.lockedBy = lockedBy;
  }

  public Long getLockedAt() {
    return lockedAt;
  }

  public void setLockedAt(Long lockedAt) {
    this.lockedAt = lockedAt;
  }

  public String getLockedByUserId() {
    return lockedByUserId;
  }

  public void setLockedByUserId(String lockedByUserId) {
    this.lockedByUserId = lockedByUserId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAsyncApiId() {
    return asyncApiId;
  }

  public void setAsyncApiId(String asyncApiId) {
    this.asyncApiId = asyncApiId;
  }

  public String getAsyncApi() {
    return asyncApi;
  }

  public void setAsyncApi(String asyncApi) {
    this.asyncApi = asyncApi;
  }
  public List<StatusHistory> getHistory() {
    return history;
  }

  public void setHistory(List<StatusHistory> history) {
    this.history = history;
  }
  public boolean isEnableScm() {
    return enableScm;
  }

  public void setEnableScm(boolean enableScm) {
    this.enableScm = enableScm;
  }

  public String getRepoName() {
    return repoName;
  }

  public void setRepoName(String repoName) {
    this.repoName = repoName;
  }

  public String getBranch() {
    return branch;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public String getHostUrl() {
    return hostUrl;
  }

  public void setHostUrl(String hostUrl) {
    this.hostUrl = hostUrl;
  }

  public String getFolderName() {
    return folderName;
  }

  public void setFolderName(String folderName) {
    this.folderName = folderName;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getScmSource() {
    return scmSource;
  }

  public void setScmSource(String scmSource) {
    this.scmSource = scmSource;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getAuthType() {
    return authType;
  }

  public void setAuthType(String authType) {
    this.authType = authType;
  }

}
