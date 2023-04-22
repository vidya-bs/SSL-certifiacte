package com.itorix.apiwiz.common.model.monetization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class ResponseVariableLocation {

  public enum Location {
    FLOW,HEADER,JSON_BODY,XML_BODY
  }

  private String name;
  private Location location;
  private List<String> variables;

}
