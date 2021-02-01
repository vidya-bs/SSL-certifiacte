package com.itorix.hyggee.mockserver.ui;

import com.itorix.hyggee.mockserver.filters.MockServerEventLog;

/**
 *   
 */
public interface MockServerLogListener {

    void updated(MockServerEventLog mockServerLog);
}
