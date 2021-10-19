package com.itorix.apiwiz.sso.model;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Document(collection = "SSO.Configurations.List")
public class UIMetadata extends AbstractObject {

    public static String SAML_CONFIG = "sAMLConfig";
    public static String ROLE_MAPPER = "roleMapper";

    private String query;
    @JsonIgnore
    private String metadata;

    public UIMetadata() {
    }

    public UIMetadata(String query, String metadata) {
        this.query = query;
        this.metadata = metadata;
    }
    @JsonIgnore
    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
