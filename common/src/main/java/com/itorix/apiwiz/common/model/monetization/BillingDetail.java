package com.itorix.apiwiz.common.model.monetization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class BillingDetail {

  public enum BillingType {
    PREPAID, POSTPAID;
  }

  private String registationId;
  private String taxExemptAuthNo;
  private BillingType billingType;

}
