package com.itorix.hyggee.mockserver.client.serialization.model;

import java.util.List;

import com.itorix.hyggee.mockserver.model.KeyToMultiValue;
import com.itorix.hyggee.mockserver.model.NottableString;
import com.itorix.hyggee.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;

/**
 *   
 */
public class KeyToMultiValueDTO extends ObjectWithReflectiveEqualsHashCodeToString {
    private NottableString name;
    private List<NottableString> values;

    protected KeyToMultiValueDTO(KeyToMultiValue keyToMultiValue) {
        name = keyToMultiValue.getName();
        values = keyToMultiValue.getValues();
    }

    protected KeyToMultiValueDTO() {
    }

    public NottableString getName() {
        return name;
    }

    public List<NottableString> getValues() {
        return values;
    }
}
