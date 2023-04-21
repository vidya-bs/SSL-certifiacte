package com.itorix.apiwiz.common.model.monetization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(Include.NON_NULL)
@Data
@Document("Connectors.Apigee.DeveloperCategory")
public class DeveloperCategory {

  @Id
  private String id;
  private String organization;
  private String name;
  private String description;
  private String status;
  private String apigeeDeveloperCategoryId;
  private boolean activeFlag = true;
  private String createdUser;
  private String modifiedUser;
  private Date createdDate;
  private Date modifiedDate;
  private String createdUserEmailId;
  private String modifiedUserEmailId;

}
