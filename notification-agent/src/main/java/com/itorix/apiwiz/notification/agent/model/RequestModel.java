package com.itorix.apiwiz.notification.agent.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestModel {

    public enum Type {
        @JsonProperty("EMAIL")
        email, @JsonProperty("SLACK")
        slack;
    }

    Type type;
    EmailTemplate emailContent;
    Map<String, String> metadata = new HashMap<>();

}
