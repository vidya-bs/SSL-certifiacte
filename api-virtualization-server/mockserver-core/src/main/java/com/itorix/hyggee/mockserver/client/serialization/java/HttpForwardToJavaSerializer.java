package com.itorix.hyggee.mockserver.client.serialization.java;

import com.google.common.base.Strings;
import com.itorix.hyggee.mockserver.model.HttpForward;

import static com.itorix.hyggee.mockserver.character.Character.NEW_LINE;
import static com.itorix.hyggee.mockserver.client.serialization.java.ExpectationToJavaSerializer.INDENT_SIZE;

/**
 *   
 */
public class HttpForwardToJavaSerializer implements ToJavaSerializer<HttpForward> {

    @Override
    public String serialize(int numberOfSpacesToIndent, HttpForward httpForward) {
        StringBuffer output = new StringBuffer();
        if (httpForward != null) {
            appendNewLineAndIndent(numberOfSpacesToIndent * INDENT_SIZE, output).append("forward()");
            if (httpForward.getHost() != null) {
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(".withHost(\"").append(httpForward.getHost()).append("\")");
            }
            if (httpForward.getPort() != null) {
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(".withPort(").append(httpForward.getPort()).append(")");
            }
            if (httpForward.getScheme() != null) {
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(".withScheme(HttpForward.Scheme.").append(httpForward.getScheme()).append(")");
            }
        }
        return output.toString();
    }

    private StringBuffer appendNewLineAndIndent(int numberOfSpacesToIndent, StringBuffer output) {
        return output.append(NEW_LINE).append(Strings.padStart("", numberOfSpacesToIndent, ' '));
    }
}
