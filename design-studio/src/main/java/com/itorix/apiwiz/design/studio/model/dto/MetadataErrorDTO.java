package com.itorix.apiwiz.design.studio.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

@Data
public class MetadataErrorDTO implements Serializable {

    private String Source;
    private String lineNumber;
    private String errorMessage;
    private String value;

    @JsonIgnore
    public MetadataErrorDTO(String source, String lineNumber, String value, String errorMessage) {
        this.Source = source;
        this.lineNumber = lineNumber;
        this.value = value;
        this.errorMessage = errorMessage;
    }
}
