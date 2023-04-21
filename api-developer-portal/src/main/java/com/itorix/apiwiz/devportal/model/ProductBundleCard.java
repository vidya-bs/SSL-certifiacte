package com.itorix.apiwiz.devportal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.itorix.apiwiz.common.model.monetization.ProductBundle;
import com.itorix.apiwiz.common.model.monetization.RatePlan;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class ProductBundleCard {

  private ProductBundle productBundle;
  private Map<String, Set<Specs>> specs;
  private List<RatePlan> ratePlans;

}