package com.itorix.consentserver.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Mock.Execution.Logs")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsentLog {
    @Id
    private String id;
    private String expectationId;
    private String expectationName;
    private String groupId;
    private String groupName;
    private String path;
    private String clientIp;
    private String loggedDate;
    private boolean wasMatched;
    private long loggedTime;
    private long traceId;

    private String httpRequest;
    private String httpResponse;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExpectationId() {
        return expectationId;
    }

    public void setExpectationId(String expectationId) {
        this.expectationId = expectationId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getLoggedDate() {
        return loggedDate;
    }

    public void setLoggedDate(String loggedDate) {
        this.loggedDate = loggedDate;
    }

    public boolean isWasMatched() {
        return wasMatched;
    }

    public void setWasMatched(boolean wasMatched) {
        this.wasMatched = wasMatched;
    }

    public long getLoggedTime() {
        return loggedTime;
    }

    public void setLoggedTime(long loggedTime) {
        this.loggedTime = loggedTime;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getTraceId() {
        return traceId;
    }

    public void setTraceId(long traceId) {
        this.traceId = traceId;
    }

    public String getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(String httpRequest) {
        this.httpRequest = httpRequest;
    }

    public String getHttpResponse() {
        return httpResponse;
    }

    public void setHttpResponse(String httpResponse) {
        this.httpResponse = httpResponse;
    }

    public String getExpectationName() {
        return expectationName;
    }

    public void setExpectationName(String expectationName) {
        this.expectationName = expectationName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
