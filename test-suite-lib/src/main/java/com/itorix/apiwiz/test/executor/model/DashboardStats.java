
package com.itorix.apiwiz.test.executor.model;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardStats {

    private String testSuiteId;

    private String configId;

    private Long cts;

    private Long mts;

    private String createdUserName;

    private String modifiedUserName;

    private String createdBy;

    private String modifiedBy;

    private int successRatio;

    private String status;

    private String testSuiteName;

    public DashboardStats(String testSuiteId, String configId, Long cts, Long mts, String createdUserName,
            String modifiedUserName, String createdBy, String modifiedBy, int successRatio, String status,
            String testSuiteName) {
        super();
        this.testSuiteId = testSuiteId;
        this.configId = configId;
        this.cts = cts;
        this.mts = mts;
        this.createdUserName = createdUserName;
        this.modifiedUserName = modifiedUserName;
        this.createdBy = createdBy;
        this.modifiedBy = modifiedBy;
        this.successRatio = successRatio;
        this.status = status;
        this.testSuiteName = testSuiteName;
    }

    public String getTestSuiteId() {
        return testSuiteId;
    }

    public void setTestSuiteId(String testSuiteId) {
        this.testSuiteId = testSuiteId;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public Long getCts() {
        return cts;
    }

    public void setCts(Long cts) {
        this.cts = cts;
    }

    public Long getMts() {
        return mts;
    }

    public void setMts(Long mts) {
        this.mts = mts;
    }

    public String getCreatedUserName() {
        return createdUserName;
    }

    public void setCreatedUserName(String createdUserName) {
        this.createdUserName = createdUserName;
    }

    public String getModifiedUserName() {
        return modifiedUserName;
    }

    public void setModifiedUserName(String modifiedUserName) {
        this.modifiedUserName = modifiedUserName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public int getSuccessRatio() {
        return successRatio;
    }

    public void setSuccessRatio(int successRatio) {
        this.successRatio = successRatio;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTestSuiteName() {
        return testSuiteName;
    }

    public void setTestSuiteName(String testSuiteName) {
        this.testSuiteName = testSuiteName;
    }

}