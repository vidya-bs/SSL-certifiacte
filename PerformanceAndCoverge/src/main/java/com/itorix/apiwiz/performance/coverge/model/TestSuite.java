package com.itorix.apiwiz.performance.coverge.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itorix.apiwiz.common.util.scheduler.Schedule;
import com.itorix.apiwiz.identitymanagement.model.AbstractObject;
import com.itorix.test.executor.beans.Header;
import com.itorix.test.executor.beans.Scenario;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "Test.Collections.List")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestSuite extends AbstractObject {

  @JsonProperty("name")
  private String name;

  @JsonProperty("description")
  private String description;

  @JsonProperty("scenarioSequence")
  private List<String> scenarioSequence;

  @JsonProperty("date")
  private String date;

  @JsonProperty("status")
  private String status;

  @JsonProperty("variables")
  private List<Header> vars;

  @JsonProperty("scenarios")
  private List<Scenario> scenarios = null;

  @JsonProperty("successRate")
  private int successRate;

  @JsonProperty("isActive")
  private Boolean active;

  @JsonProperty("notifications")
  private List<Notification> notifications;

  @JsonProperty("schedule")
  private Schedule schedule;

  @JsonProperty("revisionStatus")
  private String revisionStatus;

  @JsonProperty("version")
  private String version;

  @JsonProperty("timeout")
  private Long timeout;

  @JsonProperty("duration")
  private Long duration;

  @JsonProperty("successRatio")
  private Double successRatio = 0.0;

  @JsonProperty("executionStatus")
  private String executionStatus = "not executed";

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name.trim();
  }

  public String getExecutionStatus() {
    return executionStatus;
  }

  public void setExecutionStatus(String executionStatus) {
    this.executionStatus = executionStatus;
  }

  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  @JsonProperty("scenarios")
  public List<Scenario> getScenarios() {
    return scenarios;
  }

  @JsonProperty("scenarios")
  public void setScenarios(List<Scenario> scenarios) {
    this.scenarios = scenarios;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }


  public int getSuccessRate() {
    return successRate;
  }


  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setDuration(Long duration) {
    this.duration = duration;
  }

}
