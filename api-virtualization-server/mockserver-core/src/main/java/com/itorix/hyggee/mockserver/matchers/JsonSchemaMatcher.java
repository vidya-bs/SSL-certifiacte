package com.itorix.hyggee.mockserver.matchers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.validator.jsonschema.JsonSchemaValidator;

import static com.itorix.hyggee.mockserver.character.Character.NEW_LINE;

/**
 * See http://json-schema.org/
 *
 *   
 */
public class JsonSchemaMatcher extends BodyMatcher<String> {
    private static final String[] excludedFields = {"mockServerLogger", "jsonSchemaValidator"};
    private final MockServerLogger mockServerLogger;
    private String schema;
    private JsonSchemaValidator jsonSchemaValidator;

    public JsonSchemaMatcher(MockServerLogger mockServerLogger, String schema) {
        this.mockServerLogger = mockServerLogger;
        this.schema = schema;
        jsonSchemaValidator = new JsonSchemaValidator(mockServerLogger, schema);
    }

    public boolean matches(final HttpRequest context, String matched) {
        boolean result = false;

        try {
            String validation = jsonSchemaValidator.isValid(matched);

            result = validation.isEmpty();

            if (!result) {
                mockServerLogger.trace(context, "Failed to match JSON: {}with schema: {}because: {}", matched, this.schema, validation);
            }
        } catch (Exception e) {
            mockServerLogger.trace(context, "Failed to match JSON: {}with schema: {}because: {}", matched, this.schema, e.getMessage());
        }

        return not != result;
    }

    @Override
    @JsonIgnore
    protected String[] fieldsExcludedFromEqualsAndHashCode() {
        return excludedFields;
    }

}
