package com.itorix.hyggee.mockserver.log.model;

import com.itorix.hyggee.mockserver.model.HttpRequest;

/**
 *   
 */
public class RequestLogEntry extends LogEntry {
    public RequestLogEntry(HttpRequest httpRequest) {
        super(httpRequest);
    }
}
