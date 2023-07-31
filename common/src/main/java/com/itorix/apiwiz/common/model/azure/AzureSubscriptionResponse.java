package com.itorix.apiwiz.common.model.azure;

import java.util.List;


public class AzureSubscriptionResponse {
    private List<AzureSubscriptionValues> value;

    public AzureSubscriptionResponse(List<AzureSubscriptionValues> value) {
        this.value = value;
    }

    public AzureSubscriptionResponse() {
    }

    public List<AzureSubscriptionValues> getValue() {
        return value;
    }

    public void setValue(List<AzureSubscriptionValues> value) {
        this.value = value;
    }
}