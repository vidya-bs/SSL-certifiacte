package com.itorix.apiwiz.marketing.news.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itorix.apiwiz.common.model.AbstractObject;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "Marketing.News.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class News extends AbstractObject {
    private String newsId;
    @JsonProperty("meta")
    private NewsMeta meta;
    private Object content;
}
