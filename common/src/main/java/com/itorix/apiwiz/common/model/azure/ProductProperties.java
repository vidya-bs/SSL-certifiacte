package com.itorix.apiwiz.common.model.azure;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonInclude
public class ProductProperties {
    private String displayName;
    private String description;
    private String terms;
    private Boolean subscriptionRequired;

    private String scope;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    private Boolean approvalRequired;
    private String state;
    private Integer subscriptionsLimit;

    public ProductProperties(String displayName, String scope, String description, String terms, Boolean subscriptionRequired, Boolean approvalRequired, String state, Integer subscriptionsLimit) {
        this.displayName = displayName;
        this.description = description;
        this.terms = terms;
        this.subscriptionRequired = subscriptionRequired;
        this.approvalRequired = approvalRequired;
        this.state = state;
        this.subscriptionsLimit = subscriptionsLimit;
        this.scope=scope;
    }

    public ProductProperties() {
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public Boolean getSubscriptionRequired() {
        return subscriptionRequired;
    }

    public void setSubscriptionRequired(Boolean subscriptionRequired) {
        this.subscriptionRequired = subscriptionRequired;
    }

    public Boolean getApprovalRequired() {
        return approvalRequired;
    }

    public void setApprovalRequired(Boolean approvalRequired) {
        this.approvalRequired = approvalRequired;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getSubscriptionsLimit() {
        return subscriptionsLimit;
    }

    public void setSubscriptionsLimit(Integer subscriptionsLimit) {
        this.subscriptionsLimit = subscriptionsLimit;
    }
}