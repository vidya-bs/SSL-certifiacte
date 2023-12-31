package com.itorix.apiwiz.notification.agent.executor.exception;

public class ItorixException extends Exception {

    private static final long serialVersionUID = 1L;

    public String errorCode;

    public ItorixException() {
    }

    public ItorixException(String message) {
        super(message);
    }

    public ItorixException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;

    }

    public ItorixException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;

    }

}
