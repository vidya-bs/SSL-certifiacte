package com.itorix.apiwiz.common.model.azure;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;


@JsonInclude
public class AzureProductValues {
    private String name;
    private ProductProperties properties;

    public AzureProductValues(String name, ProductProperties properties) {
        this.name = name;
        this.properties = properties;
    }

    public AzureProductValues() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProductProperties getProperties() {
        return properties;
    }

    public void setProperties(ProductProperties properties) {
        this.properties = properties;
    }
}
