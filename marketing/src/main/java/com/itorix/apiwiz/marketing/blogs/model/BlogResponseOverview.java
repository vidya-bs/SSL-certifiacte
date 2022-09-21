package com.itorix.apiwiz.marketing.blogs.model;

import com.itorix.apiwiz.identitymanagement.model.Pagination;
import lombok.Data;

import java.util.List;

@Data
public class BlogResponseOverview {

  private Pagination pagination;

  private List<Blog> blog;

}