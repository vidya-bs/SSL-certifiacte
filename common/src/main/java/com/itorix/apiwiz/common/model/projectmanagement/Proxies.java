package com.itorix.apiwiz.common.model.projectmanagement;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Proxies {

	@Override
	public String toString() {
		return "Proxies [name=" + name + ", version=" + version + ", basePath=" + basePath + ", interfaceType="
				+ interfaceType + ", repoName=" + repoName + ", products=" + products + ", applications=" + applications
				+ ", developers=" + developers + ", organization=" + Arrays.toString(organization) + ", wsdlFiles="
				+ wsdlFiles + ", xsdFiles=" + xsdFiles + ", attachments=" + attachments + ", apigeeVirtualHosts="
				+ apigeeVirtualHosts + ", policyTemplates=" + policyTemplates + ", projectMetaData=" + projectMetaData
				+ ", pipelines=" + pipelines + ", serviceRegistries=" + serviceRegistries + ", external=" + external
				+ ", defaultVirtualHosts=" + defaultVirtualHosts + "]";
	}

	private String name;
	private String version;
	private List<String> basePath;
	private String interfaceType;
	private String repoName;
	private String twoWaySSL;
	private String swaggerName;
	private Set<String> products;
	private Set<String> applications;
	private Set<String> developers;
	private Organization[] organization;

	private List<ProjectFile> wsdlFiles;
	private List<ProjectFile> xsdFiles;
	private List<ProjectFile> attachments;
	private Set<String> apigeeVirtualHosts;
	private List<Category> policyTemplates;
	private List<ProjectMetaData> projectMetaData;
	private List<Pipeline> pipelines;
	private List<ServiceRegistry> serviceRegistries;
	private boolean external;
	private boolean defaultVirtualHosts = true;
	private List<ProxyConnection> proxyConnections;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<String> getProducts() {
		return products;
	}

	public void setProducts(Set<String> products) {
		this.products = products;
	}

	public Set<String> getApplications() {
		return applications;
	}

	public void setApplications(Set<String> applications) {
		this.applications = applications;
	}

	public Set<String> getDevelopers() {
		return developers;
	}

	public void setDevelopers(Set<String> developers) {
		this.developers = developers;
	}

	public Organization[] getOrganization() {
		return organization;
	}

	public void setOrganization(Organization[] organization) {
		this.organization = organization;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<String> getBasePath() {
		return basePath;
	}

	public void setBasePath(List<String> basePath) {
		this.basePath = basePath;
	}

	public String getInterfaceType() {
		return interfaceType;
	}

	public void setInterfaceType(String interfaceType) {
		this.interfaceType = interfaceType;
	}

	public List<ProjectFile> getWsdlFiles() {
		return wsdlFiles;
	}

	public void setWsdlFiles(List<ProjectFile> wsdlFiles) {
		this.wsdlFiles = wsdlFiles;
	}

	public List<ProjectFile> getXsdFiles() {
		return xsdFiles;
	}

	public void setXsdFiles(List<ProjectFile> xsdFiles) {
		this.xsdFiles = xsdFiles;
	}

	public Set<String> getApigeeVirtualHosts() {
		return apigeeVirtualHosts;
	}

	public void setApigeeVirtualHosts(Set<String> apigeeVirtualHosts) {
		this.apigeeVirtualHosts = apigeeVirtualHosts;
	}

	public List<Category> getPolicyTemplates() {
		return policyTemplates;
	}

	public void setPolicyTemplates(List<Category> policyTemplates) {
		this.policyTemplates = policyTemplates;
	}

	public List<ProjectMetaData> getProjectMetaData() {
		return projectMetaData;
	}

	public void setProjectMetaData(List<ProjectMetaData> projectMetaData) {
		this.projectMetaData = projectMetaData;
	}

	public List<Pipeline> getPipelines() {
		return pipelines;
	}

	public void setPipelines(List<Pipeline> pipelines) {
		this.pipelines = pipelines;
	}

	public List<ServiceRegistry> getServiceRegistries() {
		return serviceRegistries;
	}

	public void setServiceRegistries(List<ServiceRegistry> serviceRegisties) {
		this.serviceRegistries = serviceRegisties;
	}

	public boolean getExternal() {
		return external;
	}

	public void setExternal(boolean external) {
		this.external = external;
	}

	public List<ProjectFile> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<ProjectFile> attachments) {
		this.attachments = attachments;
	}

	public String getRepoName() {
		return repoName;
	}

	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

	public boolean getDefaultVirtualHosts() {
		return defaultVirtualHosts;
	}

	public void setDefaultVirtualHosts(boolean defaultVirtualHosts) {
		this.defaultVirtualHosts = defaultVirtualHosts;
	}

	public String getTwoWaySSL() {
		return twoWaySSL;
	}

	public void setTwoWaySSL(String twoWaySSL) {
		this.twoWaySSL = twoWaySSL;
	}

	public String getSwaggerName() {
		return swaggerName;
	}

	public void setSwaggerName(String swaggerName) {
		this.swaggerName = swaggerName;
	}

	public List<ProxyConnection> getProxyConnections() {
		return proxyConnections;
	}

	public void setProxyConnections(List<ProxyConnection> proxyConnections) {
		this.proxyConnections = proxyConnections;
	}
}
