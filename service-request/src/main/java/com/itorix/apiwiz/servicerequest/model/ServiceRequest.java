package com.itorix.apiwiz.servicerequest.model;

import java.util.Date;
import java.util.List;

import com.itorix.apiwiz.common.model.monetization.TransactionRecordingPolicy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itorix.apiwiz.common.model.configmanagement.KVMEntry;
import com.itorix.apiwiz.common.model.configmanagement.ProductAttributes;

@Component("serviceRequest")
@Document(collection = "Connectors.Apigee.ServiceRequest.Lists")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ServiceRequest {

	public static final String LABEL_CREATED_TIME = "createdDate";
	// common parameters

	public enum AccessType {

		Internal("internal"),
		External("external"),
		Private("private"),
		Public("public");
		private String accessType;

		AccessType(String accessType) {
			this.accessType = accessType;
		}

		public String getAccessType() {
			return accessType;
		}

	}

	private String type;
	private String gwType;
	private String createdUser;
	private String modifiedUser;
	private Date createdDate;
	private Date modifiedDate;
	private String org;
	private String env;
	private String name;
	private String createdUserEmailId;
	private String modifiedUserEmailId;
	private boolean isSaaS;
	// TargetConfig parameters
	private String host;
	private int port;
	private boolean enabled;
	private String keyAlias;
	private String keyStore;
	private String trustStore;
	private boolean sslEnabled;
	private boolean clientAuthEnabled;
	private boolean ignoreValidationErrors;
	// CacheConfig parameters
	private String description;
	private String expiryDate;
	private String expiryType;
	private boolean valuesNull;
	private boolean overflowToDisk;
	private String skipCacheIfElementSizeInKBExceeds;
	private String timeOfDay;
	private String timeoutInSec;
	// KVMConfig
	private String encrypted;
	private List<KVMEntry> entry;
	private boolean isCreated;
	// productConfig Parameters
	private String displayName;
	private String approvalType;
	private List<String> apiResources;
	private List<String> environments;
	private List<String> proxies;
	private String quota;
	private String quotaInterval;
	private String quotaTimeUnit;
	private List<String> scopes;
	private List<ProductAttributes> attributes;
	private String status;
	private String approvedBy;
	private List<String> userRole;
	private boolean activeFlag = true;
	private String _id;
	private AccessType accessType;
	private TransactionRecordingPolicy transactionRecordingPolicy;

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

	public String getExpiryType() {
		return expiryType;
	}

	public void setExpiryType(String expiryType) {
		this.expiryType = expiryType;
	}

	public Boolean getIsSaaS() {
		return isSaaS;
	}

	public void setIsSaaS(Boolean isSaaS) {
		this.isSaaS = isSaaS;
	}

	public String getModifiedUserEmailId() {
		return modifiedUserEmailId;
	}

	public void setModifiedUserEmailId(String modifiedUserEmailId) {
		this.modifiedUserEmailId = modifiedUserEmailId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getApprovalType() {
		return approvalType;
	}

	public void setApprovalType(String approvalType) {
		this.approvalType = approvalType;
	}

	public List<String> getApiResources() {
		return apiResources;
	}

	public void setApiResources(List<String> apiResources) {
		this.apiResources = apiResources;
	}

	public List<String> getEnvironments() {
		return environments;
	}

	public void setEnvironments(List<String> environments) {
		this.environments = environments;
	}

	public List<String> getProxies() {
		return proxies;
	}

	public void setProxies(List<String> proxies) {
		this.proxies = proxies;
	}

	public String getQuota() {
		return quota;
	}

	public void setQuota(String quota) {
		this.quota = quota;
	}

	public String getQuotaInterval() {
		return quotaInterval;
	}

	public void setQuotaInterval(String quotaInterval) {
		this.quotaInterval = quotaInterval;
	}

	public String getQuotaTimeUnit() {
		return quotaTimeUnit;
	}

	public void setQuotaTimeUnit(String quotaTimeUnit) {
		this.quotaTimeUnit = quotaTimeUnit;
	}

	public List<String> getScopes() {
		return scopes;
	}

	public void setScopes(List<String> scopes) {
		this.scopes = scopes;
	}

	public List<ProductAttributes> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<ProductAttributes> attributes) {
		this.attributes = attributes;
	}

	public String getCreatedUserEmailId() {
		return createdUserEmailId;
	}

	public void setCreatedUserEmailId(String createdUserEmailId) {
		this.createdUserEmailId = createdUserEmailId;
	}

	public List<String> getUserRole() {
		return userRole;
	}

	public void setUserRole(List<String> userRole) {
		this.userRole = userRole;
	}

	public boolean isCreated() {
		return isCreated;
	}

	public void setCreated(boolean isCreated) {
		this.isCreated = isCreated;
	}

	public String getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(String approvedBy) {
		this.approvedBy = approvedBy;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getGwType() {
		return gwType;
	}

	public void setGwType(String gwType) {
		this.gwType = gwType;
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

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public boolean isSslEnabled() {
		return sslEnabled;
	}

	public void setSslEnabled(boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
	}

	public boolean isClientAuthEnabled() {
		return clientAuthEnabled;
	}

	public void setClientAuthEnabled(boolean clientAuthEnabled) {
		this.clientAuthEnabled = clientAuthEnabled;
	}

	public boolean isIgnoreValidationErrors() {
		return ignoreValidationErrors;
	}

	public void setIgnoreValidationErrors(boolean ignoreValidationErrors) {
		this.ignoreValidationErrors = ignoreValidationErrors;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
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

	public AccessType getAccessType() {
		return accessType;
	}

	public void setAccessType(AccessType accessType) {
		this.accessType = accessType;
	}

	public TransactionRecordingPolicy getTransactionRecordingPolicy() {
		return transactionRecordingPolicy;
	}

	public void setTransactionRecordingPolicy(
			TransactionRecordingPolicy transactionRecordingPolicy) {
		this.transactionRecordingPolicy = transactionRecordingPolicy;
	}

	@Override
	public String toString() {
		return "ServiceRequest [type=" + type + ", createdUser=" + createdUser + ", modifiedUser=" + modifiedUser
				+ ", createdDate=" + createdDate + ", modifiedDate=" + modifiedDate + ", org=" + org + ", env=" + env
				+ ", name=" + name + ", createdUserEmailId=" + createdUserEmailId + ", modifiedUserEmailId="
				+ modifiedUserEmailId + ", isSaaS=" + isSaaS + ", host=" + host + ", port=" + port + ", enabled="
				+ enabled + ", keyAlias=" + keyAlias + ", keyStore=" + keyStore + ", trustStore=" + trustStore
				+ ", sslEnabled=" + sslEnabled + ", clientAuthEnabled=" + clientAuthEnabled
				+ ", ignoreValidationErrors=" + ignoreValidationErrors + ", description=" + description
				+ ", expiryDate=" + expiryDate + ", expiryType=" + expiryType + ", valuesNull=" + valuesNull
				+ ", overflowToDisk=" + overflowToDisk + ", skipCacheIfElementSizeInKBExceeds="
				+ skipCacheIfElementSizeInKBExceeds + ", timeOfDay=" + timeOfDay + ", timeoutInSec=" + timeoutInSec
				+ ", encrypted=" + encrypted + ", entry=" + entry + ", isCreated=" + isCreated + ", displayName="
				+ displayName + ", approvalType=" + approvalType + ", apiResources=" + apiResources + ", environments="
				+ environments + ", proxies=" + proxies + ", quota=" + quota + ", quotaInterval=" + quotaInterval
				+ ", quotaTimeUnit=" + quotaTimeUnit + ", scopes=" + scopes + ", attributes=" + attributes + ", status="
				+ status + ", approvedBy=" + approvedBy + ", userRole=" + userRole + ", activeFlag=" + activeFlag+ ", accessType=" + accessType
				+ ", transactionRecordingPolicy=" + transactionRecordingPolicy
				+ ", _id=" + _id + "]";
	}
}
