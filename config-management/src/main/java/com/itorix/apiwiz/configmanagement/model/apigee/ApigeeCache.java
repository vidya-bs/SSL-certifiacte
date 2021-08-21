package com.itorix.apiwiz.configmanagement.model.apigee;

public class ApigeeCache {

	private String skipCacheIfElementSizeInKBExceeds;

	private ExpirySettings expirySettings;

	private String description;

	private String overflowToDisk;

	public String getSkipCacheIfElementSizeInKBExceeds() {
		return skipCacheIfElementSizeInKBExceeds;
	}

	public void setSkipCacheIfElementSizeInKBExceeds(String skipCacheIfElementSizeInKBExceeds) {
		this.skipCacheIfElementSizeInKBExceeds = skipCacheIfElementSizeInKBExceeds;
	}

	public ExpirySettings getExpirySettings() {
		return expirySettings;
	}

	public void setExpirySettings(ExpirySettings expirySettings) {
		this.expirySettings = expirySettings;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOverflowToDisk() {
		return overflowToDisk;
	}

	public void setOverflowToDisk(String overflowToDisk) {
		this.overflowToDisk = overflowToDisk;
	}
}
