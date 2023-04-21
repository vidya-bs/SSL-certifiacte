package com.itorix.apiwiz.common.model.monetization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class FreemiumDetail {

  public enum Mode {
    USAGE, TIMEPERIOD, WHICHEVER_COMES_FIRST;
  }

  public enum TimePeriodUnit {
    DAY, MONTH, YEAR;
  }

  private Mode mode;
  private double usage;
  private int timePeriod;
  private TimePeriodUnit timePeriodUnit;

}
