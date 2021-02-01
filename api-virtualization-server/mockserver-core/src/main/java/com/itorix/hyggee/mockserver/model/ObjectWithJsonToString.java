package com.itorix.hyggee.mockserver.model;

import com.itorix.hyggee.mockserver.client.serialization.ObjectMapperFactory;

/**
 *   
 */
public abstract class ObjectWithJsonToString extends ObjectWithReflectiveEqualsHashCodeToString {

    @Override
    public String toString() {
        try {
            String valueAsString = ObjectMapperFactory
                .createObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(this);
            if (valueAsString.startsWith("\"") && valueAsString.endsWith("\"")) {
                valueAsString = valueAsString
                    .replaceAll("^\"", "")
                    .replaceAll("\"$", "");
            }
            return valueAsString;
        } catch (Exception e) {
            return super.toString();
        }
    }
}
