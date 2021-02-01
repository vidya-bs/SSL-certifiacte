package com.itorix.hyggee.mockserver.client.serialization.java;

import org.apache.commons.text.StringEscapeUtils;

import com.itorix.hyggee.mockserver.model.NottableString;

/**
 *   
 */
public class NottableStringToJavaSerializer {

    public static String serializeNottableString(NottableString nottableString) {
        if (nottableString.isNot()) {
            return "not(\"" + StringEscapeUtils.escapeJava(nottableString.getValue()) + "\")";
        } else {
            return "\"" + StringEscapeUtils.escapeJava(nottableString.getValue()) + "\"";
        }
    }
}
