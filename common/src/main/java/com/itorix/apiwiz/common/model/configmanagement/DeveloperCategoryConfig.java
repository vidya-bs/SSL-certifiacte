package com.itorix.apiwiz.common.model.configmanagement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class DeveloperCategoryConfig {

  private String id;
  private String name;
  private String description;

}