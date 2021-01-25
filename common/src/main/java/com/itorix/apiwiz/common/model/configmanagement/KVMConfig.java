package com.itorix.apiwiz.common.model.configmanagement;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;

@Component("KVMConfig")
@Document(collection = "Connectors.Apigee.Configure.KVM.List")
public class KVMConfig {
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String createdUser;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String modifiedUser;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String createdDate;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String modifiedDate;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String name;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String org;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String env;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String type="saas";
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String encrypted;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<KVMEntry> entry;
	
	private boolean activeFlag;
	
	private String _id;
		

	
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	
	public boolean isActiveFlag() {
		return activeFlag;
	}
	public void setActiveFlag(boolean activeFlag) {
		this.activeFlag = activeFlag;
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
	public ConfigMetadata getMetadata(){
		ConfigMetadata metadata = new ConfigMetadata();
		metadata.setName(this.name);
		metadata.setCreatedUser(this.createdUser);
		metadata.setCreatedDate(this.createdDate);
		metadata.setModifiedUser(this.modifiedUser);
		metadata.setModifiedDate(this.modifiedDate);
		return metadata;
	}
	
}
