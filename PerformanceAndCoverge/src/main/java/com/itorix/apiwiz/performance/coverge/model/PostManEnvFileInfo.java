package com.itorix.apiwiz.performance.coverge.model;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.identitymanagement.model.AbstractObject;

@Document(collection = "Connectors.Postman.Environments.List")
public class PostManEnvFileInfo extends AbstractObject {
	public static final String LABEL_ENV_NAME = "environment";
	public static final String LABEL_PROXY_INFO = "proxy";
	public static final String LABEL_ORG_NAME = "organization";
	public static final String LABEL_TYPE = "type";
	public static final String IS_SAAS = "isSaaS";

	public static final String UNIT_TEST = "unittest";
	public static final String CODE_COVERAGE = "codecoverage";

	private String environment;
	private String proxy;
	private String postManFileContent;
	private String envFileContent;
	private String organization;
	private String gridPostmanFSFileid;
	private String gridEnvFSFileid;
	private String type;
	private String originalPostManFileName;
	private String originalEnvFileName;

	public String getOriginalPostManFileName() {
		return originalPostManFileName;
	}

	public void setOriginalPostManFileName(String originalPostManFileName) {
		this.originalPostManFileName = originalPostManFileName;
	}

	public String getOriginalEnvFileName() {
		return originalEnvFileName;
	}

	public void setOriginalEnvFileName(String originalEnvFileName) {
		this.originalEnvFileName = originalEnvFileName;
	}

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private boolean isSaaS;

	public Boolean getIsSaaS() {
		return isSaaS;
	}

	public void setIsSaaS(Boolean isSaaS) {
		this.isSaaS = isSaaS;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getGridPostmanFSFileid() {
		return gridPostmanFSFileid;
	}

	public void setGridPostmanFSFileid(String gridPostmanFSFileid) {
		this.gridPostmanFSFileid = gridPostmanFSFileid;
	}

	public String getGridEnvFSFileid() {
		return gridEnvFSFileid;
	}

	public void setGridEnvFSFileid(String gridEnvFSFileid) {
		this.gridEnvFSFileid = gridEnvFSFileid;
	}

	@JsonProperty("organization")
	public String getOrganization() {
		return organization;
	}

	@JsonProperty("organization")
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	@JsonProperty("environment")
	public String getEnvironment() {
		return environment;
	}

	@JsonProperty("environment")
	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	@JsonProperty("proxy")
	public String getProxy() {
		return proxy;
	}

	@JsonProperty("proxy")
	public void setProxy(String proxy) {
		this.proxy = proxy;
	}

	@JsonProperty("postManFileContent")
	public String getPostManFileContent() {
		return postManFileContent;
	}

	@JsonProperty("postManFileContent")
	public void setPostManFileContent(String postManFileContent) {
		this.postManFileContent = postManFileContent;
	}

	@JsonProperty("envFileContent")
	public String getEnvFileContent() {
		return envFileContent;
	}

	@JsonProperty("envFileContent")
	public void setEnvFileContent(String envFileContent) {
		this.envFileContent = envFileContent;
	}
}
