package com.itorix.apiwiz.identitymanagement.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document(collection = "APIWIZ.Subscriptions.V2.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionV2 {
	@Id
	private String id;
	private String name;
	private String summary;
	private String pricing;
	private String message;
	private Heading heading;
	private List<SubscriptionPrice> subscriptionPrices;
	private String pricingMessage;
	private List<String> features;
	private List<SubscriptionItem> items;
}
