package com.itorix.apiwiz.testsuite.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Sequences implements Serializable {
    private String scenarioId;
    private List<String> testCaseSequences;
}
