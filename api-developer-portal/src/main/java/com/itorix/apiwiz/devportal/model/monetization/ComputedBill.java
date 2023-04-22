package com.itorix.apiwiz.devportal.model.monetization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.model.AbstractObject;
import com.itorix.apiwiz.common.model.monetization.RatePlan;
import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ComputedBill extends AbstractObject {
	private double transaction;
	private String startDate;
	private String endDate;
	private List<RatePlan> ratePlans;
	private String appId;
	private double billedAmount;
}
