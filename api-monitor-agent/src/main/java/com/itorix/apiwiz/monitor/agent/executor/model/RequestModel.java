package com.itorix.apiwiz.monitor.agent.executor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestModel {

    public enum Type {
        @JsonProperty("EMAIL")
        email, @JsonProperty("SLACK")
        slack;
    }

    Type type;
    EmailTemplate emailContent;
}
