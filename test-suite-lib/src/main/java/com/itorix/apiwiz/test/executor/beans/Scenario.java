package com.itorix.apiwiz.test.executor.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Scenario {

    @JsonProperty("name")
    private String name;

    @JsonProperty("testCaseSequence")
    private List<String> testCaseSequence;
    @JsonProperty("description")
    private String description;

    @JsonProperty("status")
    private String status;

    @JsonProperty("testCases")
    private List<TestCase> testCases = null;

    @JsonProperty("successRate")
    private int successRate;

    @JsonProperty("duration")
    private Long duration;

    @JsonProperty("timeout")
    private int timeout;

    @JsonProperty("timeout")
    public int getTimeout() {
        return timeout;
    }

    @JsonProperty("timeout")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @JsonProperty("continueOnError")
    private boolean continueOnError = true;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("testCases")
    public List<TestCase> getTestCases() {
        return testCases;
    }

    @JsonProperty("testCases")
    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    public int getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(int successRate) {
        this.successRate = successRate;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public List<String> getTestCaseSequence() {
        return testCaseSequence;
    }

    public void setTestCaseSequence(List<String> testCaseSequence) {
        this.testCaseSequence = testCaseSequence;
    }

    public boolean isContinueOnError() {
        return continueOnError;
    }

    public void setContinueOnError(boolean continueOnError) {
        this.continueOnError = continueOnError;
    }
}