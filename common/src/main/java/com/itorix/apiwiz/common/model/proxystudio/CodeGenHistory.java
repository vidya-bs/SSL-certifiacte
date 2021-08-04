package com.itorix.apiwiz.common.model.proxystudio;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

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
}
