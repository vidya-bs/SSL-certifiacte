package com.itorix.apiwiz.test.exception;

public class HaltExecution extends Exception{

    public HaltExecution() {
    }

    public HaltExecution(String message) {
        super(message);
    }

    public HaltExecution(String s, Exception e) {
        super(s, e);
    }
}
