package com.itorix.hyggee.mockserver.model;

/**
 *   
 */
public class Cookie extends KeyAndValue {

    public Cookie(String name, String value) {
        super(name, value);
    }

    public Cookie(NottableString name, NottableString value) {
        super(name, value);
    }

    public static Cookie cookie(String name, String value) {
        return new Cookie(name, value);
    }

    public static Cookie cookie(NottableString name, NottableString value) {
        return new Cookie(name, value);
    }
}
