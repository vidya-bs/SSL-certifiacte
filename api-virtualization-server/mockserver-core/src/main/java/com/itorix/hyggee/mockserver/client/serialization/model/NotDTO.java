package com.itorix.hyggee.mockserver.client.serialization.model;

import com.itorix.hyggee.mockserver.model.ObjectWithJsonToString;


public class NotDTO extends ObjectWithJsonToString {

    final Boolean not;

    public NotDTO(Boolean not) {
        if (not != null && not) {
            this.not = Boolean.TRUE;
        } else {
            this.not = Boolean.FALSE;
        }
    }

    public Boolean getNot() {
        return not;
    }

}
