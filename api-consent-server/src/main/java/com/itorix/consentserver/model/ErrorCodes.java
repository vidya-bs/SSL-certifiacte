package com.itorix.consentserver.model;

import java.util.HashMap;
import java.util.Map;

public enum ErrorCodes {

    ;
    public static final Map<String, String> errorMessage = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;
        {
            put("Consent-001", "Resource not found. No records found for selected Category name - %s.");
            put("Consent-002", "Resource not found. No records found for selected Consent Id - %s.");
            put("Consent-003", "Mandatory Consent Field {%s} is missing in the request");

        }
    };

    public static final Map<String, Integer> responseCode = new HashMap<String, Integer>() {
        private static final long serialVersionUID = 1L;
        {
            /* Api Monitoring Error codes START */

            put("General-1000", 500);
            put("Consent-001", 400);
            put("Consent-002", 400);
            put("Consent-003", 400);
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
