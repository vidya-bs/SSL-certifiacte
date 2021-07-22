package com.itorix.apiwiz.common.model.proxystudio;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.model.projectmanagement.ProxyConnection;
import com.itorix.apiwiz.common.model.proxystudio.apigeeassociations.ProxyApigeeDetails;

@Document(collection = "Connectors.Apigee.Proxies")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProxyData {
	@Indexed
	@Id
	private String id;
	private String proxyName;
	private String projectName;
	private String lastModifiedUser;
	private String lastModifiedUserName;
	private String downloadURI;
	private String dateModified;
	private OrgEnvs associatedProxyEnvs;
	private List<CodeGenHistory> codeGenHistory;
	private ProxyApigeeDetails proxyApigeeDetails;
	private ProxyArtifacts proxyArtifacts = new ProxyArtifacts();
	private List<ProxyConnection> proxyConnections;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getProxyName() {
		return proxyName;
	}

	public void setProxyName(String proxyName) {
		this.proxyName = proxyName;
	}

	public String getLastModifiedUser() {
		return lastModifiedUser;
	}

	public void setLastModifiedUser(String lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}

	public String getDownloadURI() {
		return downloadURI;
	}

	public void setDownloadURI(String downloadURI) {
		this.downloadURI = downloadURI;
	}

	public String getDateModified() {
		return dateModified;
	}

	public void setDateModified(String dateModified) {
		this.dateModified = dateModified;
	}

	public List<CodeGenHistory> getCodeGenHistory() {
		return codeGenHistory;
	}

	public void setCodeGenHistory(List<CodeGenHistory> codeGenHistory) {
		this.codeGenHistory = codeGenHistory;
	}

	public void addCodeGenHistory(CodeGenHistory codeGenHistory) {
		if (this.codeGenHistory != null)
			this.codeGenHistory.add(codeGenHistory);
		else {
			this.codeGenHistory = new ArrayList<CodeGenHistory>();
			this.codeGenHistory.add(codeGenHistory);
		}
	}

	public OrgEnvs getOrgEnvs() {
		return associatedProxyEnvs;
	}

	public void setOrgEnvs(OrgEnvs orgEnvs) {
		this.associatedProxyEnvs = orgEnvs;
	}

	public ProxyApigeeDetails getProxyApigeeDetails() {
		return proxyApigeeDetails;
	}

	public void setProxyApigeeDetails(ProxyApigeeDetails proxyApigeeDetails) {
		this.proxyApigeeDetails = proxyApigeeDetails;
	}

	public ProxyArtifacts getProxyArtifacts() {
		return proxyArtifacts;
	}

	public void setProxyArtifacts(ProxyArtifacts proxyArtifacts) {
		this.proxyArtifacts = proxyArtifacts;
	}

	public String getLastModifiedUserName() {
		return lastModifiedUserName;
	}

	public void setLastModifiedUserName(String lastModifiedUserName) {
		this.lastModifiedUserName = lastModifiedUserName;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public List<ProxyConnection> getProxyConnections() {
		return proxyConnections;
	}

	public void setProxyConnections(List<ProxyConnection> proxyConnections) {
		this.proxyConnections = proxyConnections;
	}
}
