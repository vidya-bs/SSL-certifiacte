package com.itorix.hyggee.mockserver.client.serialization.java;

import com.google.common.base.Strings;
import com.itorix.hyggee.mockserver.matchers.Times;

import static com.itorix.hyggee.mockserver.character.Character.NEW_LINE;
import static com.itorix.hyggee.mockserver.client.serialization.java.ExpectationToJavaSerializer.INDENT_SIZE;

/**
 *   
 */
public class TimesToJavaSerializer implements ToJavaSerializer<Times> {

    @Override
    public String serialize(int numberOfSpacesToIndent, Times times) {
        StringBuffer output = new StringBuffer();
        if (times != null) {
            appendNewLineAndIndent(numberOfSpacesToIndent * INDENT_SIZE, output);
            if (times.isUnlimited()) {
                output.append("Times.unlimited()");
            } else if (times.getRemainingTimes() == 1) {
                output.append("Times.once()");
            } else {
                output.append("Times.exactly(").append(times.getRemainingTimes()).append(")");
            }
        }

        return output.toString();
    }

    private StringBuffer appendNewLineAndIndent(int numberOfSpacesToIndent, StringBuffer output) {
        return output.append(NEW_LINE).append(Strings.padStart("", numberOfSpacesToIndent, ' '));
    }
}
