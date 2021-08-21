package com.itorix.apiwiz.common.model.postman;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.model.AbstractObject;
import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.data.annotation.Transient;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BackupCommon extends AbstractObject implements BackupInfo {

	public static final String LABEL_STATUS = "status";
	public static final String LABEL_TIME_TAKEN = "timeTaken";
	public static final String LABEL_FILE_OID = "jfrogUrl";
	public static final String LABEL_ORG_NAME = "organization";
	public static final String LABEL_RESOURCE_BACKUP_LEVEL = "backUpLevel";

	private String status;
	private long timeTaken;
	private String jfrogUrl;
	private String organization;
	private String backUpLevel;

	@Transient
	private String tempToken;

	@JsonProperty("jfrogUrl")
	public String getJfrogUrl() {
		return jfrogUrl;
	}

	@JsonProperty("jfrogUrl")
	public void setJfrogUrl(String jfrogUrl) {
		this.jfrogUrl = jfrogUrl;
	}

	@JsonProperty("timeTaken")
	public long getTimeTaken() {
		return timeTaken;
	}

	@JsonProperty("timeTaken")
	public void setTimeTaken(long timeTaken) {
		this.timeTaken = timeTaken;
	}

	@JsonProperty("organization")
	public String getOrganization() {
		return organization;
	}

	@JsonProperty("organization")
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	@JsonProperty("status")
	public String getStatus() {
		return status;
	}

	@JsonProperty("status")
	public void setStatus(String status) {
		this.status = status;
	}

	@JsonProperty("backUpLevel")
	public String getBackUpLevel() {
		return backUpLevel;
	}

	@JsonProperty("backUpLevel")
	public void setBackUpLevel(String backUpLevel) {
		this.backUpLevel = backUpLevel;
	}

	@JsonProperty("tempToken")
	public String getTempToken() {
		return tempToken;
	}

	@JsonProperty("tempToken")
	public void setTempToken(String tempToken) {
		this.tempToken = tempToken;
	}
}
