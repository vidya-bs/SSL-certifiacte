package com.itorix.hyggee.mockserver.client.serialization.model;

import com.itorix.hyggee.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;

/**
 *   
 */
public class WebSocketClientIdDTO extends ObjectWithReflectiveEqualsHashCodeToString {

    private String clientId;

    public String getClientId() {
        return clientId;
    }

    public WebSocketClientIdDTO setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }
}
