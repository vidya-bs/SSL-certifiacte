package com.itorix.apiwiz.devportal.model.monetization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.model.AbstractObject;
import com.itorix.apiwiz.common.model.monetization.ProductBundle;
import com.itorix.apiwiz.common.model.monetization.RatePlan;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Document("App.Purchase.Record")
public class PurchaseRecord extends AbstractObject {

	public enum PaymentMode{
		WALLET,UPI,CARD;
	}

	private String appName;
	private String appId;
	private String developerEmailId;
	private PaymentMode paymentMode;

	private RatePlan ratePlan;
	private ProductBundle productBundle;
}
