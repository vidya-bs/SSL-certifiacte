package com.itorix.apiwiz.common.model.configmanagement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.itorix.apiwiz.common.model.apigee.Attributes;
import java.util.List;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class CompanyConfig {

  private String name;
  private String displayName;
  private List<Attribute> attributes;

  @Data
  public static class Attribute {
    private String name;
    private String value;
  }
}