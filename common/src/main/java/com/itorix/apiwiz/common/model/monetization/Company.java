package com.itorix.apiwiz.common.model.monetization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(Include.NON_NULL)
@Data
@Document("Connectors.Apigee.Company")
public class Company {

  @Id
  private String id;
  private String name;
  private String organization;
  private String administrator;
  private String developerCategory;
  private String telephone;
  private String address1;
  private String address2;
  private String city;
  private String state;
  private String country;
  private String postalCode;
  private BillingDetail billingDetails;
  private List<Developer> developers;
  private Map<String,String> attributes;
  private String status;
  private String apigeeCompanyName;
  private boolean activeFlag = true;
  private String createdUser;
  private String modifiedUser;
  private Date createdDate;
  private Date modifiedDate;
  private String createdUserEmailId;
  private String modifiedUserEmailId;

}
