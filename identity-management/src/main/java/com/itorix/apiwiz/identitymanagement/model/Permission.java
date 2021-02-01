package com.itorix.apiwiz.identitymanagement.model;

import java.util.List;

public class Permission {
	private String name;
    private String slug;
    private boolean enabled;
    private List<Permission> sublinks;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSlug() {
		return slug;
	}
	public void setSlug(String slug) {
		this.slug = slug;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public List<Permission> getSublinks() {
		return sublinks;
	}
	public void setSublinks(List<Permission> sublinks) {
		this.sublinks = sublinks;
	}
}
