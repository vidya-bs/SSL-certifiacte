package com.itorix.hyggee.mockserver.mock.action;

import com.itorix.hyggee.mockserver.model.HttpResponse;

/**
 *   
 */
public class HttpResponseActionHandler {

    public HttpResponse handle(HttpResponse httpResponse) {
        return httpResponse.clone();
    }
}
