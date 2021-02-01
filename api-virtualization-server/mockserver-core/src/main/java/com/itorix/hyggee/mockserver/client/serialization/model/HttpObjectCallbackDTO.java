package com.itorix.hyggee.mockserver.client.serialization.model;

import com.itorix.hyggee.mockserver.model.HttpObjectCallback;
import com.itorix.hyggee.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;

/**
 *   
 */
public class HttpObjectCallbackDTO extends ObjectWithReflectiveEqualsHashCodeToString implements DTO<HttpObjectCallback> {

    private String clientId;

    public HttpObjectCallbackDTO(HttpObjectCallback httpObjectCallback) {
        if (httpObjectCallback != null) {
            clientId = httpObjectCallback.getClientId();
        }
    }

    public HttpObjectCallbackDTO() {
    }

    public HttpObjectCallback buildObject() {
        return new HttpObjectCallback()
            .withClientId(clientId);
    }

    public String getClientId() {
        return clientId;
    }

    public HttpObjectCallbackDTO setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }
}

