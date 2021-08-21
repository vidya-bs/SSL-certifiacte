package com.itorix.apiwiz.monitor.agent.executor.model;

public class ErrorObj {
    private Error error;

    /**
     * 
     * @param userMessage
     * @param statusCode
     */
    public ErrorObj(String userMessage, String statusCode) {
        error = new Error(userMessage, statusCode);
    }

    public ErrorObj() {
        super();
        error = new Error();
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public void setErrorMessage(String userMessage, String statusCode) {
        error.setStatusCode(statusCode);
        error.setUserMessage(userMessage);
    }
}