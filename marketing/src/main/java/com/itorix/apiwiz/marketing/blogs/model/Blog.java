package com.itorix.apiwiz.marketing.blogs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itorix.apiwiz.common.model.AbstractObject;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "Marketing.Blog.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Blog extends AbstractObject {
  private String blogId;

  @JsonProperty("meta")
  private BlogMeta meta;
  private Object content;
}