package com.itorix.apiwiz.monitor.agent.util;

public class MonitorAgentConstants {

    private MonitorAgentConstants () throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    public static final String EXPECTED_LATENCY = "EXPECTED_LATENCY";
    public static final String MEASURED_LATENCY = "MEASURED_LATENCY";
    public static final String STATUS = "STATUS";
    public static final String SCHEDULER_ID = "SCHEDULER_ID";
    public static final String SUMMARY_NOTIFICATION = "SUMMARY_NOTIFICATION";
    public static final String LATENCY_THRESHOLD_BREACH = "LATENCY_THRESHOLD_BREACH";
    public static final String MONITORING_TEST_SUBJECT = "itorix.app.monitor.error.report.email.subject";
    public static final String MONITORING_TEST_BODY = "itorix.app.monitor.summary.report.email.body";
    public static final String MONITORING_LATENCY_TEST_SUBJECT = "itorix.app.monitor.latency.report.email.subject";
}
