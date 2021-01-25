package com.itorix.hyggee.mockserver.model;

import java.util.List;

/**
 *   
 */
public class Cookies extends KeysAndValues<Cookie, Cookies> {

    public Cookies(List<Cookie> cookies) {
        withEntries(cookies);
    }

    public Cookies(Cookie... cookies) {
        withEntries(cookies);
    }

    @Override
    public Cookie build(NottableString name, NottableString value) {
        return new Cookie(name, value);
    }

    public Cookies clone() {
        return new Cookies().withEntries(getEntries());
    }
}
