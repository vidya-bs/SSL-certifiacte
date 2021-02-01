package com.itorix.hyggee.mockserver.mock.action;

import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.HttpResponse;

/**
 *   
 */
public interface ExpectationResponseCallback {

    /**
     * Called for every request when expectation condition has been satisfied.
     * The request that satisfied the expectation condition is passed as the
     * parameter and the return value is the response that will be returned to the client.
     *
     * @param httpRequest the request that satisfied the expectation condition
     * @return the response that will be returned to the client
     */
    HttpResponse handle(HttpRequest httpRequest);
}
