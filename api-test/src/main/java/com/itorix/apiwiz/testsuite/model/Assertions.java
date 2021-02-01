
package com.itorix.apiwiz.testsuite.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Assertions {

    @JsonProperty("status")
    private Status status;
    
    @JsonProperty("headers")
    private List<ResponseAssertionHeader> headers = null;
    
    @JsonProperty("body")
    private List<ResponseBodyValidation> body = null;
    
    @JsonProperty("status")
    public Status getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(Status status) {
        this.status = status;
    }

    @JsonProperty("headers")
    public List<ResponseAssertionHeader> getHeaders() {
        return headers;
    }

    @JsonProperty("headers")
    public void setHeaders(List<ResponseAssertionHeader> headers) {
        this.headers = headers;
    }

    @JsonProperty("body")
    public List<ResponseBodyValidation> getBody() {
        return body;
    }

    @JsonProperty("body")
    public void setBody(List<ResponseBodyValidation> body) {
        this.body = body;
    }

}