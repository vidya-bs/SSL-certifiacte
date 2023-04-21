package com.itorix.apiwiz.common.model.monetization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import java.util.Map;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class TransactionRecordingPolicy {

  private String successCriteria;
  private MonetizationAttribute status;
  private Boolean useOptionalAttribute;
  private MonetizationAttribute grossPrice;
  private MonetizationAttribute netPrice;
  private MonetizationAttribute currency;
  private MonetizationAttribute errorCode;
  private MonetizationAttribute itemDescription;
  private MonetizationAttribute tax;
  private Boolean useCustomAttributes;
  private List<MonetizationAttribute> customAttributes;
  private Boolean useUniqueTransactionIds;
  private Map<String, ResponseVariableLocation> resourceTxnLinks;
  private Boolean useRefundAttributes;
  private Refund refund;

}
