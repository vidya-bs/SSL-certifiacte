package com.itorix.mockserver.common.model.expectation;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Data {

    public enum Condition {

        @JsonProperty("equalTo")
        equalTo,

        @JsonProperty("notEqualTo")
        notEqualTo,

        @JsonProperty("any")
        any,

        @JsonProperty("regEx")
        regEx,
    }

    private Condition condition;
    private String value;
    private String path;
}