package com.itorix.apiwiz.servicerequest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class JSONPayload {

  private List<Variable> variable;

  @Data
  public static class Variable {
    private String name;
    @JsonProperty("jSONPath")
    private List<String> jSONPath;

  }
}