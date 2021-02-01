package com.itorix.hyggee.mockserver.log.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.itorix.hyggee.mockserver.matchers.TimeToLive;
import com.itorix.hyggee.mockserver.matchers.Times;
import com.itorix.hyggee.mockserver.mock.Expectation;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.HttpResponse;

import java.util.List;

/**
 *   
 */
public class RequestResponseLogEntry extends LogEntry implements ExpectationLogEntry {

    private final HttpResponse httpResponse;

    public RequestResponseLogEntry(HttpRequest httpRequest, HttpResponse httpResponse) {
        super(httpRequest);
        this.httpResponse = httpResponse;
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    @JsonIgnore
    public Expectation getExpectation() {
        return new Expectation(getHttpRequests().get(0), Times.once(), null).thenRespond(httpResponse);
    }

}
