package com.itorix.apiwiz.common.model.apigeeX;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.model.AbstractObject;

@Component("targetXConfig")
@Document(collection = "Connectors.ApigeeX.Configure.TargetServers.List")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TargetConfig extends AbstractObject {
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
