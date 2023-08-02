package com.itorix.apiwiz.common.model.azure;

public class AzureSubscriptionResponseDTO {
    private String name;
    private String displayName;

    public String getName() {
        return name;
    }

    public AzureSubscriptionResponseDTO(String name, String displayName) {
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