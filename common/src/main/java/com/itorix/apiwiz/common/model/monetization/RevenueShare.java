package com.itorix.apiwiz.common.model.monetization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class RevenueShare {

  public enum CalculationModel {
    GROSS, NET;
  }

  public enum SharingModel {
    FIXED, FLEXIBLE;
  }

  private CalculationModel calculationModel;
  private int calculationFrequencyInMonths;
  private SharingModel sharingModel;
  private double fixedSharePercentage;
  private List<RevenueShareBand> revenueShareBands;
  private Boolean allowUnlimitedUsage;

}
