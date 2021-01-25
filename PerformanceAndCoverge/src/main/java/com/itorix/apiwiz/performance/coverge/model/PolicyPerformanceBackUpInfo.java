package com.itorix.apiwiz.performance.coverge.model;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import com.itorix.apiwiz.common.model.postman.BackupCommon;


@Document(collection = "Connectors.Apigee.PolicyPerformance.List")
public class PolicyPerformanceBackUpInfo extends BackupCommon {

	public static final String LABEL_ENV_NAME = "environment";
	public static final String LABEL_PROXY_INFO = "proxy";

	private String environment;
	private String proxy;
	private String postManFileContent;
	private String envFileContent;
	private String htmlReportLoc;
	private Map<String, Object> proxyStat;
	private String user;

	public PolicyPerformanceBackUpInfo() {
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

	@JsonProperty("htmlReportLoc")
	public String getHtmlReportLoc() {
		return htmlReportLoc;
	}

	@JsonProperty("htmlReportLoc")
	public void setHtmlReportLoc(String htmlReportLoc) {
		this.htmlReportLoc = htmlReportLoc;
	}

	
	
	public Map<String, Object> getProxyStat() {
		return proxyStat;
	}

	public void setProxyStat(Map<String, Object> proxyStat) {
		this.proxyStat = proxyStat;
	}

	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	

}