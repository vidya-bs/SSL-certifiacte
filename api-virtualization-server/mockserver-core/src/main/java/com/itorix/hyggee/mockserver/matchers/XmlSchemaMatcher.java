package com.itorix.hyggee.mockserver.matchers;

import static com.itorix.hyggee.mockserver.character.Character.NEW_LINE;

import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.validator.xmlschema.XmlSchemaValidator;

/**
 * See http://xml-schema.org/
 *
 *   
 */
public class XmlSchemaMatcher extends BodyMatcher<String> {
    private final MockServerLogger mockServerLogger;
    private String schema;
    private XmlSchemaValidator xmlSchemaValidator;

    public XmlSchemaMatcher(MockServerLogger mockServerLogger, String schema) {
        this.mockServerLogger = mockServerLogger;
        this.schema = schema;
        xmlSchemaValidator = new XmlSchemaValidator(mockServerLogger, schema);
    }

    protected String[] fieldsExcludedFromEqualsAndHashCode() {
        return new String[]{"logger", "xmlSchemaValidator"};
    }

    public boolean matches(final HttpRequest context, String matched) {
        boolean result = false;

        try {
            String validation = xmlSchemaValidator.isValid(matched);

            result = validation.isEmpty();

            if (!result) {
                mockServerLogger.trace(context, "Failed to match XML: {}with schema: {}because: {}", matched, this.schema, validation);
            }
        } catch (Exception e) {
            mockServerLogger.trace(context, "Failed to match XML: {}with schema: {}because: {}", matched, this.schema, e.getMessage());
        }

        return not != result;
    }

}
