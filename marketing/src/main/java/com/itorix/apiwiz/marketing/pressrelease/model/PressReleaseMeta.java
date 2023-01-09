package com.itorix.apiwiz.marketing.pressrelease.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itorix.apiwiz.marketing.blogs.model.BlogStatus;
import com.itorix.apiwiz.marketing.common.Tag;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PressReleaseMeta {

    @JsonProperty("title")
    private String title;

    @JsonProperty("slug")
    private String slug;

    @JsonProperty("tags")
    private List<Tag> tags = new ArrayList<>();

    @JsonProperty("place")
    private String place;

    @JsonProperty("publishingDate")
    private String publishingDate;

    @JsonProperty("metaDescription")
    private String metaDescription;

    @JsonProperty("thumbnailImage")
    private String thumbnailImage;

    @JsonProperty("bannerImage")
    private String bannerImage;

    @JsonProperty("status")
    private PressReleaseStatus status;

    @JsonProperty("isNews")
    private boolean isNews;

    private int year;

}