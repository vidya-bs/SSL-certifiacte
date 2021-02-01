package com.itorix.hyggee.mockserver.client.serialization.model;

import com.itorix.hyggee.mockserver.model.Body;
import com.itorix.hyggee.mockserver.model.XmlBody;

/**
 *   
 */
public class XmlBodyDTO extends BodyWithContentTypeDTO {

    private String xml;

    public XmlBodyDTO(XmlBody xmlBody) {
        this(xmlBody, false);
    }

    public XmlBodyDTO(XmlBody xmlBody, Boolean not) {
        super(Body.Type.XML, not, xmlBody.getContentType());
        this.xml = xmlBody.getValue();
    }

    public String getXml() {
        return xml;
    }

    public XmlBody buildObject() {
        return new XmlBody(getXml());
    }
}
