package com.itorix.hyggee.mockserver.callback;

import com.itorix.hyggee.mockserver.model.HttpResponse;

/**
 *   
 */
public interface WebSocketResponseCallback {

    void handle(HttpResponse httpResponse);
}
