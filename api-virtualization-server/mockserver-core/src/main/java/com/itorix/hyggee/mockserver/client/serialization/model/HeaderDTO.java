package com.itorix.hyggee.mockserver.client.serialization.model;

import com.itorix.hyggee.mockserver.model.Header;

/**
 *   
 */
public class HeaderDTO extends KeyToMultiValueDTO implements DTO<Header> {

    public HeaderDTO(Header header) {
        super(header);
    }

    protected HeaderDTO() {
    }

    public Header buildObject() {
        return new Header(getName(), getValues());
    }
}
