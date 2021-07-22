package com.itorix.apiwiz.monitor.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class Body {

    enum Type {
        @JsonProperty("JSON")
        json,

        @JsonProperty("XML")
        xml,

        @JsonProperty("TEXT")
        text,

        @JsonProperty("HTML")
        html;
    }

    @JsonProperty("type")
    private String type;

    @JsonProperty("data")
    private String data;
}