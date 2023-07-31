package com.itorix.apiwiz.common.model.azure;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude
public class AzureSubscriptionValues {
    private String name;
    private SubscriptionProperties properties;

    public AzureSubscriptionValues(String name, SubscriptionProperties properties) {
        this.name = name;
        this.properties = properties;
    }

    public AzureSubscriptionValues() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SubscriptionProperties getProperties() {
        return properties;
    }

    public void setProperties(SubscriptionProperties properties) {
        this.properties = properties;
    }
}
