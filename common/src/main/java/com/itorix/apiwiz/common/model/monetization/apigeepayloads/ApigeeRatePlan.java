package com.itorix.apiwiz.common.model.monetization.apigeepayloads;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ApigeeRatePlan {
	public boolean published;
	public boolean advance;
	public String startDate;
	public String type;

	public int recurringStartUnit;
	public String recurringType;

	public double setupFee;
	public double earlyTerminationFee;
	public double recurringFee;

	public int frequencyDuration;
	public String frequencyDurationType;

	public boolean prorate;

	public int contractDuration;
	public String contractDurationType;

	public boolean isPrivate;

	public Map<String, Object> organization;
	public Map<String, LinkedHashMap> currency;
	public Map<String, Object> monetizationPackage;

	public boolean customPaymentTerm;
	public boolean keepOriginalStartDate;
	public String displayName;
	public String name;
	public String description;
	public String paymentDueDays;
	public String id;
	public Integer freemiumDuration;
	public String freemiumDurationType;
	public Integer freemiumUnit;
	public List<Object> ratePlanDetails;

}
