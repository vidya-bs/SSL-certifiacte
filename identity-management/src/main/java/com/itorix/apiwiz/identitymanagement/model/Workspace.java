package com.itorix.apiwiz.identitymanagement.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "Users.Workspace.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Workspace {
	public Workspace() {
		// TODO Auto-generated constructor stub
	}

	@Id
	private String name;
	private String planId;
	private String status;
	private String activeTill;
	private String tenant;
	private String key;
	private long seats;
	private String paymentSchedule;
	private String trialPeriod;
	private boolean isTrial;
	private String subscriptionId;
	private Date expiresOn;
	private String regionCode;
	private String licenceKey;
	private boolean ssoEnabled = false;
	private String ssoHost;
	private String ssoPath;
	private IDPProvider idpProvider;

	private Long cts;

	private Long mts;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPlanId() {
		return planId;
	}

	public void setPlanId(String planId) {
		this.planId = planId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getActiveTill() {
		return activeTill;
	}

	public void setActiveTill(String activeTill) {
		this.activeTill = activeTill;
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getPaymentSchedule() {
		return paymentSchedule;
	}

	public void setPaymentSchedule(String paymentSchedule) {
		this.paymentSchedule = paymentSchedule;
	}

	public String getTrialPeriod() {
		return trialPeriod;
	}

	public void setTrialPeriod(String trialPeriod) {
		this.trialPeriod = trialPeriod;
	}

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	public String getRegionCode() {
		return regionCode;
	}

	public void setRegionCode(String regionCode) {
		this.regionCode = regionCode;
	}

	public boolean getIsTrial() {
		return isTrial;
	}

	public void setIsTrial(boolean isTrial) {
		this.isTrial = isTrial;
	}

	public Date getExpiresOn() {
		return expiresOn;
	}

	public void setExpiresOn(Date expiresOn) {
		this.expiresOn = expiresOn;
	}

	public String getLicenceKey() {
		return licenceKey;
	}

	public void setLicenceKey(String licenceKey) {
		this.licenceKey = licenceKey;
	}

	public long getSeats() {
		return seats;
	}

	public void setSeats(long seats) {
		this.seats = seats;
	}

	public boolean getSsoEnabled() {
		return ssoEnabled;
	}

	public void setSsoEnabled(boolean ssoEnabled) {
		this.ssoEnabled = ssoEnabled;
	}

	public String getSsoHost() {
		return ssoHost;
	}

	public void setSsoHost(String ssoHost) {
		this.ssoHost = ssoHost;
	}

	public String getSsoPath() {
		return ssoPath;
	}

	public void setSsoPath(String ssoPath) {
		this.ssoPath = ssoPath;
	}

	public boolean isSsoEnabled() {
		return ssoEnabled;
	}
	public IDPProvider getIdpProvider() {
		return idpProvider;
	}
	public void setIdpProvider(IDPProvider idpProvider) {
		this.idpProvider = idpProvider;
	}

	public Long getCts() {
		return cts;
	}
	public void setCts(Long cts) {
		this.cts = cts;
	}

	public Long getMts() {
		return mts;
	}
	public void setMts(Long mts) {
		this.mts = mts;
	}
}
