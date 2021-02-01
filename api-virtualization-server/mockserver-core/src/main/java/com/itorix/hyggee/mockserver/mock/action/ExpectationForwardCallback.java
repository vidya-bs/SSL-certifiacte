package com.itorix.hyggee.mockserver.mock.action;

import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.HttpResponse;

/**
 *   
 */
public interface ExpectationForwardCallback {

    /**
     * Called for every request when expectation condition has been satisfied.
     * The request that satisfied the expectation condition is passed as the
     * parameter and the return value is the request that will be proxied.
     *
     * @param httpRequest the request that satisfied the expectation condition
     * @return the request that will be proxied
     */
    HttpRequest handle(HttpRequest httpRequest);
}
