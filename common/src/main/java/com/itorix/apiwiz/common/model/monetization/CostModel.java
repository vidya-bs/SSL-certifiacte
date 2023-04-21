package com.itorix.apiwiz.common.model.monetization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class CostModel {

  public enum BillingPeriodUnit {
    DAY, WEEK, MONTH;
  }

  private double baseFee;
  private int billingPeriod;
  private BillingPeriodUnit billingPeriodUnit;
  private Boolean prepaidFee;
  private Boolean proratedFee;

}
