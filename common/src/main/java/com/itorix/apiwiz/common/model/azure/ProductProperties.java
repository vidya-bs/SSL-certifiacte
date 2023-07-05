package com.itorix.apiwiz.common.model.azure;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@JsonInclude
public class ProductProperties {
  private String displayName;
  private String description;
  private String terms;
  private Boolean subscriptionRequired;
  private Boolean approvalRequired;
  private String state;
  private Integer subscriptionsLimit;
}

