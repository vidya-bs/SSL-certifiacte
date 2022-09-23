package com.itorix.apiwiz.marketing.blogs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import nonapi.io.github.classgraph.json.Id;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlogMeta {

  @JsonProperty("isCaseStudy")
  private Boolean isCaseStudy;
  @JsonProperty("tags")
  private List<BlogTag> tags = new ArrayList<>();

  @JsonProperty("title")
  private String title;

  @JsonProperty("slug")
  private String slug;

  @JsonProperty("author")
  private String author;

  @JsonProperty("designation")
  private String designation;

  @JsonProperty("author_image_url")
  private String author_image_url;

  @JsonProperty("banner_image_url")
  private String banner_image_url;

  @JsonProperty("thumbnail_image_url")
  private String thumbnail_image_url;

  @JsonProperty("status")
  private BlogStatus status;

}