package com.itorix.apiwiz.servicerequest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class ResponseObject {

  private boolean async = false;
  private boolean continueOnError = false;
  private String displayName;
  private boolean enabled = true;
  private List<Object> faultRules;
  private List<Extraction> extractions;
  private boolean ignoreUnresolvedVariables = true;
  private String name;
  private String policyType;
  private Source source;
  @JsonProperty("jSONPayload")
  private JSONPayload jSONPayload;
  @JsonProperty("xMLPayload")
  private XMLPayload xMLPayload;
  private String variablePrefix;


  @Data
  public static class Extraction{
    @JsonProperty("Header")
    private Header header;
    @JsonProperty("Variable")
    private Variable variable;
  }

  @Data
  public static class Header {
    private String name;
    private List<Pattern> pattern;
  }

  @Data
  public static class Variable {
    private String name;
    private List<Pattern> pattern;
  }

  @Data
  public static class Pattern {
    private boolean ignoreCase;
    private String value;
  }

  @Data
  public static class Source {
    private boolean clearPayload;
    private String value;
  }
}