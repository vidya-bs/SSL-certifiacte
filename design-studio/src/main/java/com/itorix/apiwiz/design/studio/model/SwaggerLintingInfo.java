package com.itorix.apiwiz.design.studio.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SwaggerLintingInfo {

  private String swaggerId;
  private String oasVersion;
  private Integer revision;
  private List<String> ruleSetIds;
  private String workspaceId;
}