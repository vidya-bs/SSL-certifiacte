package com.itorix.hyggee.mockserver.client.serialization.model;

import java.util.Objects;

import com.itorix.hyggee.mockserver.model.Body;
import com.itorix.hyggee.mockserver.model.ParameterBody;
import com.itorix.hyggee.mockserver.model.Parameters;

/**
 *   
 */
public class ParameterBodyDTO extends BodyWithContentTypeDTO {

    private Parameters parameters;

    public ParameterBodyDTO(ParameterBody parameterBody) {
        this(parameterBody, false);
    }

    public ParameterBodyDTO(ParameterBody parameterBody, Boolean not) {
        super(parameterBody.getType(), not, parameterBody.getContentType());
        parameters = parameterBody.getValue();
    }

    public Parameters getParameters() {
        return parameters;
    }

    public ParameterBody buildObject() {
        return new ParameterBody(parameters);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ParameterBodyDTO)) {
            return false;
        }
        ParameterBodyDTO that = (ParameterBodyDTO) o;
        return Objects.equals(parameters, that.parameters) &&
            Objects.equals(contentType, that.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters, contentType);
    }
}
