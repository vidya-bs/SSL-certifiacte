package com.itorix.apiwiz.design.studio.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.itorix.apiwiz.common.model.AbstractObject;
import com.itorix.apiwiz.design.studio.model.swagger.sync.StatusHistory;
import java.util.List;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("Design.GraphQL.List")
@Data
@JsonInclude(Include.NON_NULL)
public class GraphQL extends AbstractObject {
  private String name;
  private Integer revision;
  private String graphQLSchema;
  private String graphQLId;
  private Status status;
  private Boolean lock;
  private String lockedBy;
  private Long lockedAt;
  private String lockedByUserId;
  private List<StatusHistory> history;

  public enum Status {

    Draft("Draft"),
    Publish("Publish"),
    Review("Review"),
    Change_Required("Change Required"),
    Approved("Approved"),
    Deprecate("Deprecate"),
    Retired("Retired");
    private String status;

    Status(String status) {
      this.status = status;
    }

    public String getStatus() {
      return status;
    }

  }
}
