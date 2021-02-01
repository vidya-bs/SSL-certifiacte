package com.itorix.hyggee.mockserver.client.serialization.java;

import com.google.common.base.Strings;
import com.itorix.hyggee.mockserver.model.NottableString;
import com.itorix.hyggee.mockserver.model.Parameter;

import java.util.Arrays;
import java.util.List;

import static com.itorix.hyggee.mockserver.character.Character.NEW_LINE;
import static com.itorix.hyggee.mockserver.client.serialization.java.ExpectationToJavaSerializer.INDENT_SIZE;

/**
 *   
 */
public class ParameterToJavaSerializer implements MultiValueToJavaSerializer<Parameter> {
    @Override
    public String serialize(int numberOfSpacesToIndent, Parameter parameter) {
        StringBuilder output = new StringBuilder();
        output.append(NEW_LINE).append(Strings.padStart("", numberOfSpacesToIndent * INDENT_SIZE, ' '));
        output.append("new Parameter(").append(NottableStringToJavaSerializer.serializeNottableString(parameter.getName()));
        for (NottableString value : parameter.getValues()) {
            output.append(", ").append(NottableStringToJavaSerializer.serializeNottableString(value));
        }
        output.append(")");
        return output.toString();
    }

    @Override
    public String serializeAsJava(int numberOfSpacesToIndent, List<Parameter> parameters) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < parameters.size(); i++) {
            output.append(serialize(numberOfSpacesToIndent, parameters.get(i)));
            if (i < (parameters.size() - 1)) {
                output.append(",");
            }
        }
        return output.toString();
    }

    @Override
    public String serializeAsJava(int numberOfSpacesToIndent, Parameter... object) {
        return serializeAsJava(numberOfSpacesToIndent, Arrays.asList(object));
    }
}
