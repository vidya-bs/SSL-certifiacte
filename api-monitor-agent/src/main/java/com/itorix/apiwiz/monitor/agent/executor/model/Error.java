package com.itorix.apiwiz.monitor.agent.executor.model;

public class Error {
    private String userMessage;

    private String statusCode;

    public Error() {
    }

    /**
     * 
     * @param userMessage
     * @param statusCode
     */
    public Error(String userMessage, String statusCode) {
        this.userMessage = userMessage;
        this.statusCode = statusCode;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
}