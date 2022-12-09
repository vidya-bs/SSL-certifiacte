package com.itorix.apiwiz.marketing.news.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itorix.apiwiz.marketing.common.Tag;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewsMeta {

    @JsonProperty("title")
    private String  title;

    @JsonProperty("slug")
    private String slug;

    @JsonProperty("tags")
    private List<Tag> tags = new ArrayList<>();

    @JsonProperty("publicationName")
    private String publicationName;

    @JsonProperty("publishingDate")
    private String publishingDate;

    @JsonProperty("metaDescription")
    private String metaDescription;

    @JsonProperty("thumbnailImage")
    private String thumbnailImage;


    @JsonProperty("ctaLink")
    private String ctaLink;

    @JsonProperty("isNews")
    private boolean isNews;

    @JsonProperty("status")
    private NewsStatus status;

    @JsonProperty("categories")
    private List<NewsCategory> categories;

    private int year;

}