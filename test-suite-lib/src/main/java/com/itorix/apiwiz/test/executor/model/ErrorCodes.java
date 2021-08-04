package com.itorix.apiwiz.test.executor.model;

import java.util.HashMap;
import java.util.Map;

public enum ErrorCodes {

    ;
    public static final Map<String, String> errorMessage = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;
        {
            put("TestSuiteAgent-1", "Resource authorization validation failed. Please contact your workspace admin.");
            put("TestSuiteAgent-2", "Request validation failed. Invalid session token.");
            put("TestSuiteAgent-4", "Couldn't find tenant");
            put("TestSuiteAgent-5", "executionId is missing");
            put("TestSuiteAgent-6", "A request is already processed for executionId");
            put("TestSuiteAgent-7", "Internal server error");
            put("TestSuiteAgent-8", "There is no entry found in TestSuiteResponse for executionId");
        }
    };

    public static final Map<String, Integer> responseCode = new HashMap<String, Integer>() {
        private static final long serialVersionUID = 1L;
        {
            put("TestSuiteAgent-1", 403);
            put("TestSuiteAgent-2", 401);
            put("TestSuiteAgent-4", 500);
            put("TestSuiteAgent-5", 400);
            put("TestSuiteAgent-7", 500);
            put("TestSuiteAgent-8", 500);
        }
    };
    private String message;

    public String message() {
        return message;
    }

    ErrorCodes(String message) {
        this.message = message;
    }

}