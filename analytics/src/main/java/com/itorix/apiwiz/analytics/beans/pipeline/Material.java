package com.itorix.apiwiz.analytics.beans.pipeline;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"scmType", "scmURL", "scmRepo", "scmBranch", "scmCredentials"})
public class Material {

	@JsonProperty("scmType")
	private String scmType;

	@JsonProperty("scmURL")
	private String scmURL;

	@JsonProperty("scmRepo")
	private String scmRepo;

	@JsonProperty("scmBranch")
	private String scmBranch;

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

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}
}
