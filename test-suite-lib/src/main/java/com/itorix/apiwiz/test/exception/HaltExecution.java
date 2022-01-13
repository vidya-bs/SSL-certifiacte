package com.itorix.apiwiz.test.exception;

public class HaltExecution extends Exception{

    public HaltExecution() {
    }

    public HaltExecution(String message) {
        super(message);
    }
}
