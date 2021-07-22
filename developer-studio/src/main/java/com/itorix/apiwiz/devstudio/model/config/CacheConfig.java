package com.itorix.apiwiz.devstudio.model.config;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;

/** @author sudhakar */
@Component("devStudiocacheConfig")
@Document(collection = "Connectors.Apigee.Configure.Cache.List")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CacheConfig {

	private String createdUser;
	private String modifiedUser;
	private String createdDate;
	private String modifiedDate;
	private String description;
	private String name;
	private String org;
	private String env;
	private String type = "saas";
	private String expiryDate;
	private String timeOfDay;
	private String timeoutInSec;
	private boolean valuesNull;
	private boolean overflowToDisk;
	private String skipCacheIfElementSizeInKBExceeds;

	/** @return */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getTimeOfDay() {
		return timeOfDay;
	}

	public void setTimeOfDay(String timeOfDay) {
		this.timeOfDay = timeOfDay;
	}

	public String getTimeoutInSec() {
		return timeoutInSec;
	}

	public void setTimeoutInSec(String timeoutInSec) {
		this.timeoutInSec = timeoutInSec;
	}

	public boolean isValuesNull() {
		return valuesNull;
	}

	public void setValuesNull(boolean valuesNull) {
		this.valuesNull = valuesNull;
	}

	public boolean isOverflowToDisk() {
		return overflowToDisk;
	}

	public void setOverflowToDisk(boolean overflowToDisk) {
		this.overflowToDisk = overflowToDisk;
	}

	public String getSkipCacheIfElementSizeInKBExceeds() {
		return skipCacheIfElementSizeInKBExceeds;
	}

	public void setSkipCacheIfElementSizeInKBExceeds(String skipCacheIfElementSizeInKBExceeds) {
		this.skipCacheIfElementSizeInKBExceeds = skipCacheIfElementSizeInKBExceeds;
	}

	public String getCreatedUser() {
		return createdUser;
	}

	public void setCreatedUser(String createdUser) {
		this.createdUser = createdUser;
	}

	public String getModifiedUser() {
		return modifiedUser;
	}

	public void setModifiedUser(String modifiedUser) {
		this.modifiedUser = modifiedUser;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(String modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public ConfigMetadata getMetadata() {
		ConfigMetadata metadata = new ConfigMetadata();
		metadata.setName(this.name);
		metadata.setCreatedUser(this.createdUser);
		metadata.setCreatedDate(this.createdDate);
		metadata.setModifiedUser(this.modifiedUser);
		metadata.setModifiedDate(this.modifiedDate);
		return metadata;
	}
}
