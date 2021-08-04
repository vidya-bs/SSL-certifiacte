package com.itorix.apiwiz.monitor.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The following configuration is used to store customer certificates.
 *
 * @author kishan
 *
 */
@Component("Certificates")
@Document(collection = "Monitor.Certificates")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Certificates {

    @Id
    @JsonProperty("name")
    private String name;

    @JsonProperty("content")
    private byte[] content;

    @JsonProperty("description")
    private String description;

    @JsonProperty("password")
    private String password;

    @JsonProperty("alias")
    private String alias;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

}
