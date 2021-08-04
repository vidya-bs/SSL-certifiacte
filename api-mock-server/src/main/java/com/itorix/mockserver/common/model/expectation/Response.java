package com.itorix.mockserver.common.model.expectation;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Response {

    public enum Type {

        @JsonProperty("JSON")
        json,

        @JsonProperty("XML")
        xml,

        @JsonProperty("HTML")
        html,

        @JsonProperty("Text")
        text

    }

    int statusCode;
    String statusMessage;
    String cookies;
    @JsonProperty("headers")
    Map<String, String> headers = new HashMap<>();
    String body;
    Type responseType;
}
