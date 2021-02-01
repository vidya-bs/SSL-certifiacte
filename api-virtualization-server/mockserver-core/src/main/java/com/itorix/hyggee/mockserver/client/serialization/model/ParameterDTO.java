package com.itorix.hyggee.mockserver.client.serialization.model;

import com.itorix.hyggee.mockserver.model.Parameter;

/**
 *   
 */
public class ParameterDTO extends KeyToMultiValueDTO implements DTO<Parameter> {

    public ParameterDTO(Parameter parameter) {
        super(parameter);
    }

    protected ParameterDTO() {
    }

    public Parameter buildObject() {
        return new Parameter(getName(), getValues());
    }
}
