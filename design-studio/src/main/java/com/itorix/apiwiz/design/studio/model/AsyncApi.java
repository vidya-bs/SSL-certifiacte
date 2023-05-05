package com.itorix.apiwiz.design.studio.model;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.itorix.apiwiz.identitymanagement.model.AbstractObject;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Design.AsyncApi.List")
@JsonInclude(Include.NON_NULL)
public class AsyncApi extends AbstractObject {
  private Integer revision;

  private String status;

  private String name;

  private String asyncApiId;

  private Boolean lock;
  private List<String> ruleSetIds;

  private String lockedBy;

  private Long lockedAt;

  private String lockedByUserId;

  private String description;

  private String asyncApi;

  private String scm_folder;

  private String scm_url;

  private String scm_authorizationType;

  private String scm_branch;

  private String scm_repository;

  private String scm_username;

  private String scm_token;

  private String scm_type;

  private String scm_password;

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

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
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

  public String getScm_folder() {
    return scm_folder;
  }

  public void setScm_folder(String scm_folder) {
    this.scm_folder = scm_folder;
  }

  public String getScm_url() {
    return scm_url;
  }

  public void setScm_url(String scm_url) {
    this.scm_url = scm_url;
  }

  public String getScm_authorizationType() {
    return scm_authorizationType;
  }

  public void setScm_authorizationType(String scm_authorizationType) {
    this.scm_authorizationType = scm_authorizationType;
  }

  public String getScm_branch() {
    return scm_branch;
  }

  public void setScm_branch(String scm_branch) {
    this.scm_branch = scm_branch;
  }

  public String getScm_repository() {
    return scm_repository;
  }

  public void setScm_repository(String scm_repository) {
    this.scm_repository = scm_repository;
  }

  public String getScm_username() {
    return scm_username;
  }

  public void setScm_username(String scm_username) {
    this.scm_username = scm_username;
  }

  public String getScm_token() {
    return scm_token;
  }

  public void setScm_token(String scm_token) {
    this.scm_token = scm_token;
  }

  public String getScm_type() {
    return scm_type;
  }

  public void setScm_type(String scm_type) {
    this.scm_type = scm_type;
  }

  public String getScm_password() {
    return scm_password;
  }

  public void setScm_password(String scm_password) {
    this.scm_password = scm_password;
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
}
