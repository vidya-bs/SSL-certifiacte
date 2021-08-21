package com.itorix.apiwiz.projectmanagement.model.cicd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"createdBy", "createdUserEmail", "modifiedBy", "modifiedUserEmail", "cts", "mts", "status"})
public class Metadata {

	@JsonProperty("createdBy")
	private String createdBy;

	@JsonProperty("modifiedBy")
	private String modifiedBy;

	@JsonProperty("createdUserEmail")
	private String createdUserEmail;

	@JsonProperty("modifiedUserEmail")
	private String modifiedUserEmail;

	@JsonProperty("cts")
	private Long cts;

	@JsonProperty("mts")
	private Long mts;

	@JsonProperty("status")
	private String status;

	public Metadata() {
	}

	public Metadata(String createdBy, Long cts, String modifiedBy, Long mts) {
		this.createdBy = createdBy;
		this.cts = cts;
		this.modifiedBy = modifiedBy;
		this.mts = mts;
	}

	public Metadata(String createdBy, String createdUserEmail, Long cts, String modifiedBy, String modifiedUserEmail,
			Long mts) {
		this.createdBy = createdBy;
		this.createdUserEmail = createdUserEmail;
		this.cts = cts;
		this.modifiedBy = modifiedBy;
		this.mts = mts;
		this.modifiedUserEmail = modifiedUserEmail;
	}

	@JsonProperty("createdUserEmail")
	public String getCreatedUserEmail() {
		return createdUserEmail;
	}

	@JsonProperty("createdUserEmail")
	public void setCreatedUserEmail(String createdUserEmail) {
		this.createdUserEmail = createdUserEmail;
	}

	@JsonProperty("modifiedUserEmail")
	public String getModifiedUserEmail() {
		return modifiedUserEmail;
	}

	@JsonProperty("modifiedUserEmail")
	public void setModifiedUserEmail(String modifiedUserEmail) {
		this.modifiedUserEmail = modifiedUserEmail;
	}

	@JsonProperty("createdBy")
	public String getCreatedBy() {
		return createdBy;
	}

	@JsonProperty("createdBy")
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	@JsonProperty("modifiedBy")
	public String getModifiedBy() {
		return modifiedBy;
	}

	@JsonProperty("modifiedBy")
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	@JsonProperty("cts")
	public Long getCts() {
		return cts;
	}

	@JsonProperty("cts")
	public void setCts(Long cts) {
		this.cts = cts;
	}

	@JsonProperty("mts")
	public Long getMts() {
		return mts;
	}

	@JsonProperty("mts")
	public void setMts(Long mts) {
		this.mts = mts;
	}

	@JsonProperty("status")
	public String getStatus() {
		return status;
	}

	@JsonProperty("status")
	public void setStatus(String status) {
		this.status = status;
	}
}
