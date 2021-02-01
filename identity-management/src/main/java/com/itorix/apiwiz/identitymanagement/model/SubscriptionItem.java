package com.itorix.apiwiz.identitymanagement.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionItem {
	
	private boolean comingSoon;
	private String id;
	private String label;
	private boolean enabled;
	private List<SubscriptionItem> items;
	private Limit limit;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public List<SubscriptionItem> getItems() {
		return items;
	}
	public void setItems(List<SubscriptionItem> items) {
		this.items = items;
	}
	public boolean getEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public Limit getLimit() {
		return limit;
	}
	public void setLimit(Limit limit) {
		this.limit = limit;
	}
	public boolean isComingSoon() {
		return comingSoon;
	}
	public void setComingSoon(boolean comingSoon) {
		this.comingSoon = comingSoon;
	}

}
