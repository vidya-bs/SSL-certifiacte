package com.itorix.apiwiz.test.executor.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "Test.Scenario.Timeout")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TimeOut {
    private boolean enabled;
    private int timeout;
    private String testAgentType="shared";
}
