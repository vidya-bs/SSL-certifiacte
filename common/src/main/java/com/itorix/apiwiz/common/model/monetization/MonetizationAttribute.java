package com.itorix.apiwiz.common.model.monetization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class MonetizationAttribute {


  private List<String> resources;
  private ResponseVariableLocation variableLocation;

}
