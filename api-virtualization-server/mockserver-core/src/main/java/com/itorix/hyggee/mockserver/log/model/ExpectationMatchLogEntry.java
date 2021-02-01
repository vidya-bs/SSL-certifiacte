package com.itorix.hyggee.mockserver.log.model;

import com.itorix.hyggee.mockserver.mock.Expectation;
import com.itorix.hyggee.mockserver.model.HttpRequest;

/**
 *   
 */
public class ExpectationMatchLogEntry extends LogEntry implements ExpectationLogEntry {

    private final Expectation expectation;

    public ExpectationMatchLogEntry(HttpRequest httpRequest, Expectation expectation) {
        super(httpRequest);
        this.expectation = expectation.clone();
    }

    public Expectation getExpectation() {
        return expectation;
    }

}
