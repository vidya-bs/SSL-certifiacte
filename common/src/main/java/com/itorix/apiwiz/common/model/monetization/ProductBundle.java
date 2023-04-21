package com.itorix.apiwiz.common.model.monetization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(Include.NON_NULL)
@Data
@Document("Connectors.Apigee.ProductBundle")
public class ProductBundle {
  @Id
  private String id;
  private String name;
  private String description;
  private String organization;
  private TreeSet<String> products;
  private TreeMap<String,RatePlan> ratePlans;
  private String termsOfUse;
  private String status;
  private String apigeeProductBundleId;
  private boolean activeFlag = true;
  private String createdUser;
  private String modifiedUser;
  private Date createdDate;
  private Date modifiedDate;
  private String createdUserEmailId;
  private String modifiedUserEmailId;
  private List<String> partners;
  private Map<String,TransactionRecordingPolicy> productTRP;

}
