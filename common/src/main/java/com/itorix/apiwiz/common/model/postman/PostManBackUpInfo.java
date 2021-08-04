package com.itorix.apiwiz.common.model.postman;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Util.PostmanFile")
public class PostManBackUpInfo extends BackupCommon {

	public static final String LABEL_ENV_NAME = "environment";
	public static final String LABEL_PROXY_INFO = "proxy";

	private String environment;
	private String proxy;
	private String postManFileContent;
	private String envFileContent;

	public PostManBackUpInfo() {
		super();
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
