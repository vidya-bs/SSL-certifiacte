package com.itorix.apiwiz.common.model.configmanagement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class ProductBundleConfig {

  public enum Status{
    CREATED,ACTIVE,INACTIVE
  }
  private String name;
  private String description;
  private String displayName;
  private String organization;
  private List<String> product;
  private Status status;
  private String id;

}