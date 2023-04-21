package com.itorix.apiwiz.common.model.monetization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class Refund {

  private String resource;
  private String successCriteria;
  private ResponseVariableLocation status;
  private ResponseVariableLocation parentId;
  private Boolean useOptionalRefundAttributes;
  private ResponseVariableLocation netPrice;
  private ResponseVariableLocation currency;
  private ResponseVariableLocation grossPrice;
  private ResponseVariableLocation errorCode;
  private ResponseVariableLocation tax;
  private ResponseVariableLocation itemDescription;

}
