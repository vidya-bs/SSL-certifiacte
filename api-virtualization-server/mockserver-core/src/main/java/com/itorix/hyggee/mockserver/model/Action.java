package com.itorix.hyggee.mockserver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 *   
 */
public abstract class Action extends ObjectWithJsonToString {

    @JsonIgnore
    public abstract Type getType();

    public enum Type {
        FORWARD,
        FORWARD_TEMPLATE,
        FORWARD_CLASS_CALLBACK,
        FORWARD_OBJECT_CALLBACK,
        FORWARD_REPLACE,
        RESPONSE,
        RESPONSE_TEMPLATE,
        RESPONSE_CLASS_CALLBACK,
        RESPONSE_OBJECT_CALLBACK,
        ERROR
    }
}
