
package com.itorix.apiwiz.test.executor.beans;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseAssertions {

    @JsonProperty("status")
    private List<Assertion> status;

    @JsonProperty("headers")
    private List<Assertion> headers = null;

    @JsonProperty("body")
    private List<ResponseBodyValidation> body = null;

    @JsonProperty("message")
    private String message;

    @JsonProperty("status")
    public List<Assertion> getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(List<Assertion> status) {
        this.status = status;
    }

    @JsonProperty("headers")
    public List<Assertion> getHeaders() {
        return headers;
    }

    @JsonProperty("headers")
    public void setHeaders(List<Assertion> headers) {
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

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

}