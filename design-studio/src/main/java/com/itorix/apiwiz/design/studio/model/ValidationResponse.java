package com.itorix.apiwiz.design.studio.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationResponse {
    private List<String> messages;
    private List<SchemaValidationError> schemaValidationMessages = null;

    public void addValidationMessage(SchemaValidationError schemaValidationError) {
        if(schemaValidationMessages == null) {
            this.schemaValidationMessages = new ArrayList<SchemaValidationError>();
        }
        this.schemaValidationMessages.add(schemaValidationError);
    }

    public void addMessage(String message) {
        if(this.messages == null) {
            this.messages = new ArrayList<String>();
        }
        this.messages.add(message);
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public List<SchemaValidationError> getSchemaValidationMessages() {
        return schemaValidationMessages;
    }

    public void setSchemaValidationMessages(List<SchemaValidationError> validationMessages) {
        this.schemaValidationMessages = validationMessages;
    }
}
