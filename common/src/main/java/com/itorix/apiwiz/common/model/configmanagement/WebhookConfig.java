package com.itorix.apiwiz.common.model.configmanagement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class WebhookConfig {

  private String name;
  private String postUrl;
  private boolean enabled = true;
  private String id;

}