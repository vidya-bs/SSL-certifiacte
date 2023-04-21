package com.itorix.apiwiz.common.model.monetization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class RateCard {

  public enum ChargingModel {
    FLATRATE, VOLUMEBANDED, BUNDLES;
  }

  private ChargingModel chargingModel;
  private int calculationFrequency;
  private String calculationFrequencyUnit;
  private double flatRate;
  private List<RateCardVolumeBand> volumeBundles;
  private List<RateCardBundle> bundles;
  private Boolean allowUnlimitedUsage;

}
