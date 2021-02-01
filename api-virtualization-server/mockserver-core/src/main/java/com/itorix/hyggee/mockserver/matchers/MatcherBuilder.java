package com.itorix.hyggee.mockserver.matchers;

import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.mock.Expectation;
import com.itorix.hyggee.mockserver.model.HttpRequest;

/**
 *   
 */
public class MatcherBuilder {

    private final MockServerLogger mockServerLogger;

    public MatcherBuilder(MockServerLogger mockServerLogger) {
        this.mockServerLogger = mockServerLogger;
    }

    public HttpRequestMatcher transformsToMatcher(HttpRequest httpRequest) {
        return new HttpRequestMatcher(httpRequest, mockServerLogger);
    }

    public HttpRequestMatcher transformsToMatcher(Expectation expectation) {
        return new HttpRequestMatcher(expectation, mockServerLogger);
    }

}
