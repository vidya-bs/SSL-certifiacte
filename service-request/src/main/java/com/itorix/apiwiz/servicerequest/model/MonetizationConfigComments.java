package com.itorix.apiwiz.servicerequest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(Include.NON_NULL)
@Document(collection = "Connectors.Apigee.MonetizationConfig.Comments")
@Data
public class MonetizationConfigComments {

  private String org;
  private String name;
  private String type;
  private String comments;
  private String status;
  private String createdUser;
  private String createdDate;

}