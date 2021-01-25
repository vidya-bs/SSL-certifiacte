package com.itorix.apiwiz.common.model.postman;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;



@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
"ActionResult",
"properties",
"timestamp",
"headers",
"reasonPhrase",
"statusCode",
"accessList"
})
public class Result {

@JsonProperty("ActionResult")
private String actionResult;
@JsonProperty("properties")
private Properties properties;
@JsonProperty("timestamp")
private String timestamp;
@JsonProperty("headers")
private List<Header> headers = new ArrayList<Header>();
@JsonProperty("reasonPhrase")
private String reasonPhrase;
@JsonProperty("statusCode")
private String statusCode;
@JsonProperty("accessList")
private List<AccessList> accessList = new ArrayList<AccessList>();
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

/**
* 
* @return
* The actionResult
*/
@JsonProperty("ActionResult")
public String getActionResult() {
return actionResult;
}

/**
* 
* @param actionResult
* The ActionResult
*/
@JsonProperty("ActionResult")
public void setActionResult(String actionResult) {
this.actionResult = actionResult;
}

/**
* 
* @return
* The properties
*/
@JsonProperty("properties")
public Properties getProperties() {
return properties;
}

/**
* 
* @param properties
* The properties
*/
@JsonProperty("properties")
public void setProperties(Properties properties) {
this.properties = properties;
}

/**
* 
* @return
* The timestamp
*/
@JsonProperty("timestamp")
public String getTimestamp() {
return timestamp;
}

/**
* 
* @param timestamp
* The timestamp
*/
@JsonProperty("timestamp")
public void setTimestamp(String timestamp) {
this.timestamp = timestamp;
}

/**
* 
* @return
* The headers
*/
@JsonProperty("headers")
public List<Header> getHeaders() {
return headers;
}

/**
* 
* @param headers
* The headers
*/
@JsonProperty("headers")
public void setHeaders(List<Header> headers) {
this.headers = headers;
}

/**
* 
* @return
* The reasonPhrase
*/
@JsonProperty("reasonPhrase")
public String getReasonPhrase() {
return reasonPhrase;
}

/**
* 
* @param reasonPhrase
* The reasonPhrase
*/
@JsonProperty("reasonPhrase")
public void setReasonPhrase(String reasonPhrase) {
this.reasonPhrase = reasonPhrase;
}

/**
* 
* @return
* The statusCode
*/
@JsonProperty("statusCode")
public String getStatusCode() {
return statusCode;
}

/**
* 
* @param statusCode
* The statusCode
*/
@JsonProperty("statusCode")
public void setStatusCode(String statusCode) {
this.statusCode = statusCode;
}

/**
* 
* @return
* The accessList
*/
@JsonProperty("accessList")
public List<AccessList> getAccessList() {
return accessList;
}

/**
* 
* @param accessList
* The accessList
*/
@JsonProperty("accessList")
public void setAccessList(List<AccessList> accessList) {
this.accessList = accessList;
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