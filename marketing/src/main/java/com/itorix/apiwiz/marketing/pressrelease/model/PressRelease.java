package com.itorix.apiwiz.marketing.pressrelease.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itorix.apiwiz.common.model.AbstractObject;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "Marketing.PressRelease.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PressRelease extends AbstractObject {
    private String pressReleaseId;
    @JsonProperty("meta")
    private PressReleaseMeta meta;
    private Object content;
}