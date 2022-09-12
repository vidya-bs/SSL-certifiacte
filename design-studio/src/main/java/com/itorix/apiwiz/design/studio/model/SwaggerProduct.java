package com.itorix.apiwiz.design.studio.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Design.Swagger.Product")
@Data
public class SwaggerProduct {

  @Id
  @JsonProperty(value = "productId")
  private String id;
  private String productName;
  private String productDescription;

}
