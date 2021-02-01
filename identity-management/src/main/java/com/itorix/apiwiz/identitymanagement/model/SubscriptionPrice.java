package com.itorix.apiwiz.identitymanagement.model;

public class SubscriptionPrice {
	private String period;
	private String priceId;
	private String currency;
	private String amount;
	private String description;
	private String minimumUnits;
	
	public String getPeriod() {
		return period;
	}
	public void setPeriod(String period) {
		this.period = period;
	}
	public String getPriceId() {
		return priceId;
	}
	public void setPriceId(String priceId) {
		this.priceId = priceId;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getMinimumUnits() {
		return minimumUnits;
	}
	public void setMinimumUnits(String minimumUnits) {
		this.minimumUnits = minimumUnits;
	}
	
}
