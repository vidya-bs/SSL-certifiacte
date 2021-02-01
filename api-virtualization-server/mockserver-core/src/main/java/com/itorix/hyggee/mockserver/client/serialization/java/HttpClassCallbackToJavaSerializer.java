package com.itorix.hyggee.mockserver.client.serialization.java;

import com.google.common.base.Strings;
import com.itorix.hyggee.mockserver.model.HttpClassCallback;

import static com.itorix.hyggee.mockserver.character.Character.NEW_LINE;
import static com.itorix.hyggee.mockserver.client.serialization.java.ExpectationToJavaSerializer.INDENT_SIZE;

/**
 *   
 */
public class HttpClassCallbackToJavaSerializer implements ToJavaSerializer<HttpClassCallback> {

    @Override
    public String serialize(int numberOfSpacesToIndent, HttpClassCallback httpClassCallback) {
        StringBuffer output = new StringBuffer();
        if (httpClassCallback != null) {
            appendNewLineAndIndent(numberOfSpacesToIndent * INDENT_SIZE, output).append("callback()");
            if (httpClassCallback.getCallbackClass() != null) {
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(".withCallbackClass(\"").append(httpClassCallback.getCallbackClass()).append("\")");
            }
        }

        return output.toString();
    }

    private StringBuffer appendNewLineAndIndent(int numberOfSpacesToIndent, StringBuffer output) {
        return output.append(NEW_LINE).append(Strings.padStart("", numberOfSpacesToIndent, ' '));
    }
}
