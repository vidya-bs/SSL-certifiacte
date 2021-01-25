package com.itorix.hyggee.mockserver.client.serialization.model;

import com.itorix.hyggee.mockserver.model.Body;
import com.itorix.hyggee.mockserver.model.XmlSchemaBody;

/**
 *   
 */
public class XmlSchemaBodyDTO extends BodyDTO {

    private String xmlSchema;

    public XmlSchemaBodyDTO(XmlSchemaBody xmlSchemaBody) {
        this(xmlSchemaBody, false);
    }

    public XmlSchemaBodyDTO(XmlSchemaBody xmlSchemaBody, Boolean not) {
        super(Body.Type.XML_SCHEMA, not);
        this.xmlSchema = xmlSchemaBody.getValue();
    }

    public String getXml() {
        return xmlSchema;
    }

    public XmlSchemaBody buildObject() {
        return new XmlSchemaBody(getXml());
    }
}
