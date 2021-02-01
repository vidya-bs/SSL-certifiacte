package com.itorix.hyggee.mockserver.client.serialization.java;

import com.google.common.base.Strings;
import com.itorix.hyggee.mockserver.model.ConnectionOptions;

import static com.itorix.hyggee.mockserver.character.Character.NEW_LINE;
import static com.itorix.hyggee.mockserver.client.serialization.java.ExpectationToJavaSerializer.INDENT_SIZE;

/**
 *   
 */
public class ConnectionOptionsToJavaSerializer implements ToJavaSerializer<ConnectionOptions> {

    @Override
    public String serialize(int numberOfSpacesToIndent, ConnectionOptions connectionOptions) {
        StringBuffer output = new StringBuffer();
        if (connectionOptions != null) {
            appendNewLineAndIndent(numberOfSpacesToIndent * INDENT_SIZE, output).append("connectionOptions()");
            if (connectionOptions.getSuppressContentLengthHeader() != null) {
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(".withSuppressContentLengthHeader(").append(connectionOptions.getSuppressContentLengthHeader()).append(")");
            }
            if (connectionOptions.getContentLengthHeaderOverride() != null) {
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(".withContentLengthHeaderOverride(").append(connectionOptions.getContentLengthHeaderOverride()).append(")");
            }
            if (connectionOptions.getSuppressConnectionHeader() != null) {
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(".withSuppressConnectionHeader(").append(connectionOptions.getSuppressConnectionHeader()).append(")");
            }
            if (connectionOptions.getKeepAliveOverride() != null) {
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(".withKeepAliveOverride(").append(connectionOptions.getKeepAliveOverride()).append(")");
            }
            if (connectionOptions.getCloseSocket() != null) {
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(".withCloseSocket(").append(connectionOptions.getCloseSocket()).append(")");
            }
        }
        return output.toString();
    }

    private StringBuffer appendNewLineAndIndent(int numberOfSpacesToIndent, StringBuffer output) {
        return output.append(NEW_LINE).append(Strings.padStart("", numberOfSpacesToIndent, ' '));
    }
}
