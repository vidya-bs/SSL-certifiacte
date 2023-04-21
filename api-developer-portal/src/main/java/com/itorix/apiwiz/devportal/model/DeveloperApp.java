package com.itorix.apiwiz.devportal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.itorix.apiwiz.common.model.monetization.ProductBundle;
import com.itorix.apiwiz.common.model.monetization.RatePlan;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(Include.NON_NULL)
@Data
@Document("Apigee.DeveloperApp.List")
public class DeveloperApp {

  private String appId;
  private String appName;
  private String email;
  private String organization;
  private String description;
  private RatePlan ratePlan;
  private ProductBundle productBundle;

}