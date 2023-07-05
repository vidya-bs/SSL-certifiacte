package com.itorix.apiwiz.common.model.azure;

public class AzureProductResponseDTO {
  private String name;
  private String displayName;

  public String getName() {
    return name;
  }

  public AzureProductResponseDTO(String name, String displayName) {
    this.name = name;
    this.displayName = displayName;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
}
