package com.itorix.apiwiz.common.model.azure;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AzureProductResponse {
  private List<AzureProductValues> value;
}

