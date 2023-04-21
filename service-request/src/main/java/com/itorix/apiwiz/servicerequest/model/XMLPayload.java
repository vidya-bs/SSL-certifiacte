package com.itorix.apiwiz.servicerequest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class XMLPayload {

  private List<Variable> Variable;
  private List<Object> namespaces;
  private boolean stopPayloadProcessing = false;

  @Data
  public static class Variable {
    private String name;
    private String type;
    @JsonProperty("xPath")
    private List<Object> xPath;
  }

  @Data
  public static class XPath {
    private String value;
  }

}