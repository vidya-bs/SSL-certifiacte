package com.itorix.apiwiz.design.studio.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.model.AbstractObject;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "Api.Ratings.List")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ApiRatings extends AbstractObject {
    private String swaggerId;
    private String userName;
    private String email;
    private String comments;
    private int rating;
    private String oasVersion;
    private int revision;
}
