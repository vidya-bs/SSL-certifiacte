package com.itorix.hyggee.mockserver.client.serialization.model;

import com.itorix.hyggee.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;

/**
 *   
 */
public class WebSocketMessageDTO extends ObjectWithReflectiveEqualsHashCodeToString {

    private String type;

    private String value;

    public String getType() {
        return type;
    }

    public WebSocketMessageDTO setType(String type) {
        this.type = type;
        return this;
    }

    public String getValue() {
        return value;
    }

    public WebSocketMessageDTO setValue(String value) {
        this.value = value;
        return this;
    }
}
