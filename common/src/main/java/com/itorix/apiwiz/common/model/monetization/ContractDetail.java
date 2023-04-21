package com.itorix.apiwiz.common.model.monetization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class ContractDetail {

  public enum DurationUnit {
    DAY, MONTH, YEAR;
  }

  private String currency;
  private DurationUnit durationUnit;
  private double setupFee;
  private double earlyTerminationFee;
  private int duration;
  private int paymentDueDays;

}
