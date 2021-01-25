package com.itorix.apiwiz.common.model.trace;

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
"completed",
"point"
})
public class Trace {

@JsonProperty("completed")
private boolean completed;
@JsonProperty("point")
private List<Point> point = new ArrayList<Point>();
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

/**
* 
* @return
* The completed
*/
@JsonProperty("completed")
public boolean isCompleted() {
return completed;
}

/**
* 
* @param completed
* The completed
*/
@JsonProperty("completed")
public void setCompleted(boolean completed) {
this.completed = completed;
}

/**
* 
* @return
* The point
*/
@JsonProperty("point")
public List<Point> getPoint() {
return point;
}

/**
* 
* @param point
* The point
*/
@JsonProperty("point")
public void setPoint(List<Point> point) {
this.point = point;
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