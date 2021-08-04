package com.itorix.apiwiz.test.logging;

import java.util.Map;

public class LoggingContext {
    private static ThreadLocal<Map<String, String>> currentTenant = new ThreadLocal<>();

    public static void setLogMap(Map<String, String> tenant) {
        currentTenant.set(tenant);
    }

    public static Map<String, String> getLogMap() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.set(null);
    }
}
