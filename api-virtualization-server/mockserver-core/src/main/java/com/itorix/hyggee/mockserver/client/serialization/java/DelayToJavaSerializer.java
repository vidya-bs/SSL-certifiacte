package com.itorix.hyggee.mockserver.client.serialization.java;

import com.itorix.hyggee.mockserver.model.Delay;

/**
 *   
 */
public class DelayToJavaSerializer implements ToJavaSerializer<Delay> {

    @Override
    public String serialize(int numberOfSpacesToIndent, Delay delay) {
        StringBuilder output = new StringBuilder();
        if (delay != null) {
            output.append("new Delay(TimeUnit.").append(delay.getTimeUnit().name()).append(", ").append(delay.getValue()).append(")");
        }
        return output.toString();
    }
}
