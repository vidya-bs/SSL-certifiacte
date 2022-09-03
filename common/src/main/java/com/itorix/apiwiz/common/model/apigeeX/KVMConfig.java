package com.itorix.apiwiz.common.model.apigeeX;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.model.AbstractObject;
import com.itorix.apiwiz.common.model.configmanagement.KVMEntry;

@Component("KVMxConfig")
@Document(collection = "Connectors.ApigeeX.Configure.TargetServers.List")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class KVMConfig extends AbstractObject {

	private String name;
	private String org;
	private String env;
	private String type = "saas";
	private String encrypted;
	private List<KVMEntry> entry;
	private boolean activeFlag;

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
	public String getEncrypted() {
		return encrypted;
	}
	public void setEncrypted(String encrypted) {
		this.encrypted = encrypted;
	}
	public List<KVMEntry> getEntry() {
		return entry;
	}
	public void setEntry(List<KVMEntry> entry) {
		this.entry = entry;
	}
	public boolean isActiveFlag() {
		return activeFlag;
	}
	public void setActiveFlag(boolean activeFlag) {
		this.activeFlag = activeFlag;
	}

}
