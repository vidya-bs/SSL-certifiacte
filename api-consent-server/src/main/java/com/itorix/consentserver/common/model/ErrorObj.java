package com.itorix.consentserver.common.model;

public class ErrorObj {
    private Error error;

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
