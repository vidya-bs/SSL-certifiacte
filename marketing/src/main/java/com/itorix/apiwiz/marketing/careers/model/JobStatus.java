package com.itorix.apiwiz.marketing.careers.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum JobStatus {
    @JsonProperty("active")
    ACTIVE,
    @JsonProperty("inactive")
    INACTIVE
}
