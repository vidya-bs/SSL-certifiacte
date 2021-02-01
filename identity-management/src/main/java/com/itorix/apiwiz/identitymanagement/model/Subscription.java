package com.itorix.apiwiz.identitymanagement.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

@Document(collection = "APIWIZ.Subscriptions.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Subscription {
	@Id
	private String id;
	private String name;
	private String summary;
	private String pricing;
	private String message;
	private List<SubscriptionPrice> subscriptionPrices;
	private String pricingMessage;
	private List<String> features;
	private List<SubscriptionItem> items;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<SubscriptionItem> getItems() {
		return items;
	}
	public void setItems(List<SubscriptionItem> items) {
		this.items = items;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getPricing() {
		return pricing;
	}
	public void setPricing(String pricing) {
		this.pricing = pricing;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public List<String> getFeatures() {
		return features;
	}
	public void setFeatures(List<String> features) {
		this.features = features;
	}
	public List<SubscriptionPrice> getSubscriptionPrices() {
		return subscriptionPrices;
	}
	public void setSubscriptionPrice(List<SubscriptionPrice> subscriptionPrices) {
		this.subscriptionPrices = subscriptionPrices;
	}
	public String getPricingMessage() {
		return pricingMessage;
	}
	public void setPricingMessage(String pricingMessage) {
		this.pricingMessage = pricingMessage;
	}

}
