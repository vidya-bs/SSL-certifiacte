package com.itorix.apiwiz.performance.coverge.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.annotation.Id;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardSummary {

  @Id
  private String testSuiteName;

  private String testSuiteId;

  private int successCount;

  private int failureCount;

  private int cancelledCount;

  private int successRatio;

  public DashboardSummary(String testSuiteId, String testSuiteName, int successCount, int failureCount,
                          int cancelledCount, int successRatio) {
    super();
    this.testSuiteId = testSuiteId;
    this.testSuiteName = testSuiteName;
    this.successCount = successCount;
    this.failureCount = failureCount;
    this.cancelledCount = cancelledCount;
    this.successRatio = successRatio;
  }

  public String getTestSuiteId() {
    return testSuiteId;
  }

  public void setTestSuiteId(String testSuiteId) {
    this.testSuiteId = testSuiteId;
  }

  public String getTestSuiteName() {
    return testSuiteName;
  }

  public void setTestSuiteName(String testSuiteName) {
    this.testSuiteName = testSuiteName;
  }

  public int getSuccessCount() {
    return successCount;
  }

  public void setSuccessCount(int successCount) {
    this.successCount = successCount;
  }

  public int getFailureCount() {
    return failureCount;
  }

  public void setFailureCount(int failureCount) {
    this.failureCount = failureCount;
  }

  public int getCancelledCount() {
    return cancelledCount;
  }

  public void setCancelledCount(int cancelledCount) {
    this.cancelledCount = cancelledCount;
  }

  public int getSuccessRatio() {
    return successRatio;
  }

  public void setSuccessRatio(int successRatio) {
    this.successRatio = successRatio;
  }
}
