package com.itorix.apiwiz.design.studio.model;

import lombok.Data;

@Data
public class GraphQLImport {

  private String name;
  private boolean isLoaded;
  private String reason;
  private String graphQLId;
}
