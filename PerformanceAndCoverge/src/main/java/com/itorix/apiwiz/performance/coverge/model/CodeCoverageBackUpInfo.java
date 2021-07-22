package com.itorix.apiwiz.performance.coverge.model;

import org.springframework.data.mongodb.core.mapping.Document;

import com.itorix.apiwiz.common.model.postman.BackupCommon;

@Document(collection = "Connectors.Apigee.CodeCoverage.List")
public class CodeCoverageBackUpInfo extends BackupCommon {

	public static final String LABEL_ENV_NAME = "environment";
	public static final String LABEL_PROXY_INFO = "proxy";

	private String environment;
	private String proxy;
	private String postManFileContent;
	private String envFileContent;
	private String htmlReportLoc;
	private ProxyStat proxyStat;
	private String apigeeUser;
	private String url;

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getProxy() {
		return proxy;
	}

	public void setProxy(String proxy) {
		this.proxy = proxy;
	}

	public String getPostManFileContent() {
		return postManFileContent;
	}

	public void setPostManFileContent(String postManFileContent) {
		this.postManFileContent = postManFileContent;
	}

	public String getEnvFileContent() {
		return envFileContent;
	}

	public void setEnvFileContent(String envFileContent) {
		this.envFileContent = envFileContent;
	}

	public String getHtmlReportLoc() {
		return htmlReportLoc;
	}

	public void setHtmlReportLoc(String htmlReportLoc) {
		this.htmlReportLoc = htmlReportLoc;
	}

	public ProxyStat getProxyStat() {
		return proxyStat;
	}

	public void setProxyStat(ProxyStat proxyStat) {
		this.proxyStat = proxyStat;
	}

	public String getApigeeUser() {
		return apigeeUser;
	}

	public void setApigeeUser(String apigeeUser) {
		this.apigeeUser = apigeeUser;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
