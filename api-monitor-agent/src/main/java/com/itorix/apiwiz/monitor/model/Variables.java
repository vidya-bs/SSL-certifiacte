
package com.itorix.apiwiz.monitor.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itorix.apiwiz.monitor.model.request.Header;

@Document(collection = "Monitor.Collections.Environments")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Variables {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("variables")
    private List<Header> variables;

    @JsonProperty("variables")
    public List<Header> getVariables() {
        return variables;
    }

    @JsonProperty("variables")
    public void setVariables(List<Header> headers) {
        this.variables = headers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}