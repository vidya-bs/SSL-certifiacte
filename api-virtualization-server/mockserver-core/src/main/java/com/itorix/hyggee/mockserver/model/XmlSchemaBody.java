package com.itorix.hyggee.mockserver.model;

import com.itorix.hyggee.mockserver.file.FileReader;

/**
 *   
 */
public class XmlSchemaBody extends Body {

    private final String xmlSchema;

    public XmlSchemaBody(String xmlSchema) {
        super(Type.XML_SCHEMA);
        this.xmlSchema = xmlSchema;
    }

    public static XmlSchemaBody xmlSchema(String xmlSchema) {
        return new XmlSchemaBody(xmlSchema);
    }

    public static XmlSchemaBody xmlSchemaFromResource(String xmlSchemaPath) {
        return new XmlSchemaBody(FileReader.readFileFromClassPathOrPath(xmlSchemaPath));
    }

    public String getValue() {
        return xmlSchema;
    }
}
