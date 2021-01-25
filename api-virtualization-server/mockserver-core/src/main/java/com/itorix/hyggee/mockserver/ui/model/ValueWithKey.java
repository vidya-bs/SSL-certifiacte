package com.itorix.hyggee.mockserver.ui.model;

import com.itorix.hyggee.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;

/**
 *   
 */
public class ValueWithKey {

    private String key;
    private Object value;

    public ValueWithKey(ObjectWithReflectiveEqualsHashCodeToString value) {
        this.value = value;
        this.key = String.valueOf(value.key());
    }

    public ValueWithKey(String value, String key) {
        this.value = value;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
}
