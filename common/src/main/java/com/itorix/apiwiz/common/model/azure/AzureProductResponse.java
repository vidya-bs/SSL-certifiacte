package com.itorix.apiwiz.common.model.azure;

import java.util.List;
import lombok.Getter;
import lombok.Setter;


public class AzureProductResponse {
  private List<AzureProductValues> value;

  public AzureProductResponse(List<AzureProductValues> value) {
    this.value = value;
  }

  public AzureProductResponse() {
  }

  public List<AzureProductValues> getValue() {
    return value;
  }

  public void setValue(List<AzureProductValues> value) {
    this.value = value;
  }
}

