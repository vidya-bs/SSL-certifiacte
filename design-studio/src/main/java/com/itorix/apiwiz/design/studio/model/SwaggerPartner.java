package com.itorix.apiwiz.design.studio.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;
import javax.ws.rs.DefaultValue;
import lombok.AccessLevel;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Design.Swagger.Partner")
@Data
@JsonInclude(Include.NON_NULL)
public class SwaggerPartner implements Serializable {

  @Id
  private String id;
  private String partnerName;
  private String partnerDisplayName;
  private String partnerDescription;
  private Boolean isDefault = false;


}
