package com.itorix.apiwiz.design.studio.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

@Data
public class MetadataErrorDTO implements Serializable {

    private String Source;
    private String lineNumber;
    private String errorMessage;

    @JsonIgnore
    public MetadataErrorDTO(String source, String lineNumber, String errorMessage) {
        this.Source = source;
        this.lineNumber = lineNumber;
        this.errorMessage = errorMessage;
    }
}
