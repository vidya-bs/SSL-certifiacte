package com.itorix.hyggee.mockserver.client.serialization.model;

import com.itorix.hyggee.mockserver.model.Cookie;

/**
 *   
 */
public class CookieDTO extends KeyAndValueDTO implements DTO<Cookie> {

    public CookieDTO(Cookie cookie) {
        super(cookie);
    }

    protected CookieDTO() {
    }

    public Cookie buildObject() {
        return new Cookie(getName(), getValue());
    }
}
