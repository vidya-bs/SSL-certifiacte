package com.itorix.apiwiz.projectmanagement.model.cicd;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "scmType", "scmURL", "scmRepo", "scmBranch", "scmCredentials" })
public class Material {

	@JsonProperty("scmType")
	private String scmType;
	@JsonProperty("scmURL")
	private String scmURL;
	@JsonProperty("scmRepo")
	private String scmRepo;
	@JsonProperty("scmBranch")
	private String scmBranch;
	@JsonProperty("scmCredentials")
	private ScmCredentials scmCredentials;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("scmType")
	public String getScmType() {
		return scmType;
	}

	@JsonProperty("scmType")
	public void setScmType(String scmType) {
		this.scmType = scmType;
	}

	@JsonProperty("scmURL")
	public String getScmURL() {
		return scmURL;
	}

	@JsonProperty("scmURL")
	public void setScmURL(String scmURL) {
		this.scmURL = scmURL;
	}

	@JsonProperty("scmRepo")
	public String getScmRepo() {
		return scmRepo;
	}

	@JsonProperty("scmRepo")
	public void setScmRepo(String scmRepo) {
		this.scmRepo = scmRepo;
	}

	@JsonProperty("scmBranch")
	public String getScmBranch() {
		return scmBranch;
	}

	@JsonProperty("scmBranch")
	public void setScmBranch(String scmBranch) {
		this.scmBranch = scmBranch;
	}

	@JsonProperty("scmCredentials")
	public ScmCredentials getScmCredentials() {
		return scmCredentials;
	}

	@JsonProperty("scmCredentials")
	public void setScmCredentials(ScmCredentials scmCredentials) {
		this.scmCredentials = scmCredentials;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}
