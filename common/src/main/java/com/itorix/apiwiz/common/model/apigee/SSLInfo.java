package com.itorix.apiwiz.common.model.apigee;

public class SSLInfo {
	
	private String enabled;
	private String clientAuthEnabled;
	private String keyStore;
	private String keyAlias;
	private String trustStore;
	private String ignoreValidationErrors;
	
	public String getEnabled() {
		return enabled;
	}
	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}
	public String getClientAuthEnabled() {
		return clientAuthEnabled;
	}
	public void setClientAuthEnabled(String clientAuthEnabled) {
		this.clientAuthEnabled = clientAuthEnabled;
	}
	public String getKeyStore() {
		return keyStore;
	}
	public void setKeyStore(String keyStore) {
		this.keyStore = keyStore;
	}
	public String getKeyAlias() {
		return keyAlias;
	}
	public void setKeyAlias(String keyAlias) {
		this.keyAlias = keyAlias;
	}
	public String getTrustStore() {
		return trustStore;
	}
	public void setTrustStore(String trustStore) {
		this.trustStore = trustStore;
	}
	public String getIgnoreValidationErrors() {
		return ignoreValidationErrors;
	}
	public void setIgnoreValidationErrors(String ignoreValidationErrors) {
		this.ignoreValidationErrors = ignoreValidationErrors;
	}
	@Override
	public String toString() {
		return "SSLInfo [enabled=" + enabled + ", clientAuthEnabled=" + clientAuthEnabled + ", keyStore=" + keyStore
				+ ", keyAlias=" + keyAlias + "]";
	}
	

}
