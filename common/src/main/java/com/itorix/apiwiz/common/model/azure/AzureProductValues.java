package com.itorix.apiwiz.common.model.azure;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude
public class AzureProductValues {
  private String name;
  private ProductProperties properties;
}

