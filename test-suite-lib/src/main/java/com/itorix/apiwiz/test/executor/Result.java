package com.itorix.apiwiz.test.executor;

public class Result {

    private String testCaseId;

    private String status;

    private String exception;

    public Result() {
    }

    public Result(String testCaseId, String status, String exception) {
        this.testCaseId = testCaseId;
        this.status = status;
        this.exception = exception;
    }

    public String getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(String testCaseId) {
        this.testCaseId = testCaseId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Result [testCaseId=");
        builder.append(testCaseId);
        builder.append(", status=");
        builder.append(status);
        builder.append(", exception=");
        builder.append(exception);
        builder.append("]");
        return builder.toString();
    }

}