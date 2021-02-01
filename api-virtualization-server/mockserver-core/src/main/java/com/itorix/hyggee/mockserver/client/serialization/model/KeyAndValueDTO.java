package com.itorix.hyggee.mockserver.client.serialization.model;

import com.itorix.hyggee.mockserver.model.KeyAndValue;
import com.itorix.hyggee.mockserver.model.NottableString;
import com.itorix.hyggee.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;

/**
 *   
 */
public class KeyAndValueDTO extends ObjectWithReflectiveEqualsHashCodeToString {
    private NottableString name;
    private NottableString value;

    protected KeyAndValueDTO(KeyAndValue keyAndValue) {
        name = keyAndValue.getName();
        value = keyAndValue.getValue();
    }

    protected KeyAndValueDTO() {
    }

    public NottableString getName() {
        return name;
    }

    public NottableString getValue() {
        return value;
    }
}
