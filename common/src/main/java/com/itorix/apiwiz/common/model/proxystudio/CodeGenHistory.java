package com.itorix.apiwiz.common.model.proxystudio;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CodeGenHistory {

	private List<Category> policyTemplates;
	private String projectName;
	private Proxy proxy;
	private List<Target> target;
	private String downloadURL;
	private String scmType;
	private String scmURL;
	private String scmBranch;
	private String userCreated;
	private String dateCreated;

	private ProxySCMDetails proxySCMDetails;
	private ProxyPortfolio portfolio;

	private String swaggerId;
	private String oasVersion;
	private Integer swaggerRevision;

	private Map<String, Object> proxyMetadata;

	private String connectorId;

	public String getConnectorId() {
		return connectorId;
	}

	public void setConnectorId(String connectorId) {
		this.connectorId = connectorId;
	}

	public String getScmURL() {
		return scmURL;
	}

	public void setScmURL(String scmURL) {
		this.scmURL = scmURL;
	}

	public String getScmBranch() {
		return scmBranch;
	}

	public void setScmBranch(String scmBranch) {
		this.scmBranch = scmBranch;
	}

	public ProxySCMDetails getProxySCMDetails() {
		return proxySCMDetails;
	}

	public void setProxySCMDetails(ProxySCMDetails proxySCMDetails) {
		this.proxySCMDetails = proxySCMDetails;
	}

	public List<Category> getPolicyTemplates() {
		return policyTemplates;
	}

	public void setPolicyTemplates(List<Category> policyTemplates) {
		this.policyTemplates = policyTemplates;
	}

	public Proxy getProxy() {
		return proxy;
	}

	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

	public List<Target> getTarget() {
		return target;
	}

	public void setTarget(List<Target> target) {
		this.target = target;
	}

	@Override
	public String toString() {
		return "[policyTemplates = " + policyTemplates + ", proxy = " + proxy + ", target = " + target + "]";
	}

	public String getDownloadURL() {
		return downloadURL;
	}

	public void setDownloadURL(String downloadURL) {
		this.downloadURL = downloadURL;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	/** @return the userCreated */
	public String getUserCreated() {
		return userCreated;
	}

	/**
	 * @param userCreated
	 *            the userCreated to set
	 */
	public void setUserCreated(String userCreated) {
		this.userCreated = userCreated;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getScmType() {
		return scmType;
	}

	public void setScmType(String scmType) {
		this.scmType = scmType;
	}

	public ProxyPortfolio getPortfolio() {
		return portfolio;
	}

	public void setPortfolio(ProxyPortfolio portfolio) {
		this.portfolio = portfolio;
	}

	public String getSwaggerId() {
		return swaggerId;
	}

	public void setSwaggerId(String swaggerId) {
		this.swaggerId = swaggerId;
	}

	public String getOasVersion() {
		return oasVersion;
	}

	public void setOasVersion(String oasVersion) {
		this.oasVersion = oasVersion;
	}

	public Integer getSwaggerRevision() {
		return swaggerRevision;
	}

	public void setSwaggerRevision(Integer swaggerRevision) {
		this.swaggerRevision = swaggerRevision;
	}


	public Map<String, Object> getProxyMetadata() {
		return proxyMetadata;
	}

	public void setProxyMetadata(Map<String, Object> proxyMetadata) {
		this.proxyMetadata = proxyMetadata;
	}
}
