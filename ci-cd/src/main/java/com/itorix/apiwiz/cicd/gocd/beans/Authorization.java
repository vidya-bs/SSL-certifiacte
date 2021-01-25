package com.itorix.apiwiz.cicd.gocd.beans;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "roles",
    "users"
})
public class Authorization {

    @JsonProperty("roles")
    private List<Object> roles = null;
    @JsonProperty("users")
    private List<Object> users = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("roles")
    public List<Object> getRoles() {
        return roles;
    }

    @JsonProperty("roles")
    public void setRoles(List<Object> roles) {
        this.roles = roles;
    }

    @JsonProperty("users")
    public List<Object> getUsers() {
        return users;
    }

    @JsonProperty("users")
    public void setUsers(List<Object> users) {
        this.users = users;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
