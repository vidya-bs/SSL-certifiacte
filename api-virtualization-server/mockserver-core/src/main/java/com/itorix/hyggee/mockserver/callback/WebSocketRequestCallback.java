package com.itorix.hyggee.mockserver.callback;

import com.itorix.hyggee.mockserver.model.HttpRequest;

/**
 *   
 */
public interface WebSocketRequestCallback {

    void handle(HttpRequest httpRequest);
}
