package com.itorix.hyggee.mockserver.client.serialization.java;

import com.google.common.base.Strings;
import com.itorix.hyggee.mockserver.model.Header;
import com.itorix.hyggee.mockserver.model.NottableString;

import java.util.Arrays;
import java.util.List;

import static com.itorix.hyggee.mockserver.character.Character.NEW_LINE;
import static com.itorix.hyggee.mockserver.client.serialization.java.ExpectationToJavaSerializer.INDENT_SIZE;

/**
 *   
 */
public class HeaderToJavaSerializer implements MultiValueToJavaSerializer<Header> {
    @Override
    public String serialize(int numberOfSpacesToIndent, Header header) {
        StringBuilder output = new StringBuilder();
        output.append(NEW_LINE).append(Strings.padStart("", numberOfSpacesToIndent * INDENT_SIZE, ' '));
        output.append("new Header(").append(NottableStringToJavaSerializer.serializeNottableString(header.getName()));
        for (NottableString value : header.getValues()) {
            output.append(", ").append(NottableStringToJavaSerializer.serializeNottableString(value));
        }
        output.append(")");
        return output.toString();
    }

    @Override
    public String serializeAsJava(int numberOfSpacesToIndent, List<Header> headers) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < headers.size(); i++) {
            output.append(serialize(numberOfSpacesToIndent, headers.get(i)));
            if (i < (headers.size() - 1)) {
                output.append(",");
            }
        }
        return output.toString();
    }

    @Override
    public String serializeAsJava(int numberOfSpacesToIndent, Header... object) {
        return serializeAsJava(numberOfSpacesToIndent, Arrays.asList(object));
    }
}
