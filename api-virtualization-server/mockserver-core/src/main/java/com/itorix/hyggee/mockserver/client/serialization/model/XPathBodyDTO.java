package com.itorix.hyggee.mockserver.client.serialization.model;

import com.itorix.hyggee.mockserver.model.Body;
import com.itorix.hyggee.mockserver.model.XPathBody;

/**
 *   
 */
public class XPathBodyDTO extends BodyDTO {

    private String xpath;

    public XPathBodyDTO(XPathBody xPathBody) {
        this(xPathBody, false);
    }

    public XPathBodyDTO(XPathBody xPathBody, Boolean not) {
        super(Body.Type.XPATH, not);
        this.xpath = xPathBody.getValue();
    }

    public String getXPath() {
        return xpath;
    }

    public XPathBody buildObject() {
        return new XPathBody(getXPath());
    }
}
