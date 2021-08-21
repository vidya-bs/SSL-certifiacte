package com.itorix.apiwiz.common.model.configmanagement;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@Component("targetConfig")
@Document(collection = "Connectors.Apigee.Configure.TargetServers.List")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TargetConfig {
	private String createdUser;
	private String modifiedUser;
	private String createdDate;
	private String modifiedDate;
	private String org;
	private String env;
	private String type = "saas";
	private String host;
	private int port;
	private boolean enabled;
	private String name;
	private String keyAlias;
	private String keyStore;
	private String trustStore;
	private boolean sslEnabled;
	private boolean clientAuthEnabled;
	private boolean ignoreValidationErrors;
	private boolean empty;
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

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKeyAlias() {
		return keyAlias;
	}

	public void setKeyAlias(String keyAlias) {
		this.keyAlias = keyAlias;
	}

	public String getKeyStore() {
		return keyStore;
	}

	public void setKeyStore(String keyStore) {
		this.keyStore = keyStore;
	}

	public String getTrustStore() {
		return trustStore;
	}

	public void setTrustStore(String trustStore) {
		this.trustStore = trustStore;
	}

	public boolean isClientAuthEnabled() {
		return clientAuthEnabled;
	}

	public void setClientAuthEnabled(boolean clientAuthEnabled) {
		this.clientAuthEnabled = clientAuthEnabled;
	}

	public boolean isSslEnabled() {
		return sslEnabled;
	}

	public void setSslEnabled(boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
	}

	public boolean isIgnoreValidationErrors() {
		return ignoreValidationErrors;
	}

	public void setIgnoreValidationErrors(boolean ignoreValidationErrors) {
		this.ignoreValidationErrors = ignoreValidationErrors;
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

	@JsonIgnore
	public ConfigMetadata getMetadata() {
		ConfigMetadata metadata = new ConfigMetadata();
		metadata.setName(this.name);
		metadata.setCreatedUser(this.createdUser);
		metadata.setCreatedDate(this.createdDate);
		metadata.setModifiedUser(this.modifiedUser);
		metadata.setModifiedDate(this.modifiedDate);
		return metadata;
	}

	public void trimData() {
		this.type = null;
		this.host = null;
		this.port = 0;
		this.enabled = this.empty;
		this.keyAlias = null;
		this.keyStore = null;
		this.trustStore = null;
		this.sslEnabled = this.empty;
		this.clientAuthEnabled = this.empty;
		this.ignoreValidationErrors = this.empty;
	}
}
