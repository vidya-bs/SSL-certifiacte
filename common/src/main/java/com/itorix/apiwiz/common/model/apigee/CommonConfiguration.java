package com.itorix.apiwiz.common.model.apigee;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import net.sf.json.JSONArray;
@JsonInclude(JsonInclude.Include.NON_NULL)
@Component
public class CommonConfiguration {
	
	private String apigeeEmail;
	private String apigeePassword;
	private String apigeeCred;
	private String apiName;
	private String sharedflowName;
	
	private String organization;
	private String environment;
	
	private String jsessionId;
	
	
	private Boolean isCleanUpAndBackUp;
	
	private String jfrogUrl;
	
	private String newOrg;
	
	private String backUpLocation;
	
	private Boolean isDepoyedOnly;
	private List<String> selectedEnvironments;
	private List<String> selectedProxies;
	private List<String> selectedSharedflows;
	private String backUpLevel;
	private String type;
	private String oldOrg;
	private String oldEnv;
	private String operationId;
	private String interactionid;
	
	//Unused

	
	
	private String userName;
	private String password;
	private String url = "";

	//private String apiProxyName;
	
	private String revision;

	
	private String newEnv;

	private String dir;

	private String restoreFrom;
	private boolean rollOver;
	private boolean activate;

	private Object proxyInfo;
	private Object resourceInfo;
	private Object developersInfo;
	private Object productsInfo;
	private Object appsInfo;
	

	private List<String> proxiesList;

	private List<EnvironmentVO> environments;

	//Apigee path variables
	
	private String appID;
	private String apiProductName;
	
	private String revisionNumber;
	private  String developerId;
	private String developerEmail;
	private String appName;
	private String consumerKey;
	
	private String cacheName;
	private String keyValueMapName;
	private String virtualHostName;
	private String targetServerName;
	
	
	private String apiProduct;
	private String appDeveloper;
	private String resource;
	
	private List<String> envApiList;
	private JSONArray jsonArray;
	private String sys;
	
	
	private Long startTime; 
	
	private String organizations[];
	
   private String timeRangestartDate;
	
	private String timeRangeendDate;
	
	private String timeRange;
	
	private String timeUnit;
	
	private String testsuiteId;
	
	private String variableId;
	
	
	@JsonIgnore
	private MultipartFile postmanFile;
	@JsonIgnore
	private MultipartFile envFile;
	
	private boolean isCodeCoverage;
	private boolean isPolicyPerformance;
	
	

	//properties to passed from ui as extra parameters
	Set<String> developersToBackup;
	
	private boolean expand;

	public CommonConfiguration() {

	}

	/*public CommonConfiguration(String backUpLocation, String organization, String userName, String password, String url,
			String apiProxyName, String environment, String revision) {
		super();
		this.backUpLocation = backUpLocation;
		this.organization = organization;
		this.userName = userName;
		this.password = password;
		this.url = "https://api.enterprise.apigee.com";
		//this.apiProxyName = apiProxyName;
		this.environment = environment;
		this.revision = revision;
	}

	public CommonConfiguration(String backUpLocation, String organization, String userName, String password, String url,
			String apiProxyName, String environment, String revision, String restoreFrom, boolean rollOver,
			boolean activate) {
		super();
		this.backUpLocation = backUpLocation;
		this.organization = organization;
		this.userName = userName;
		this.password = password;
		this.url = "https://api.enterprise.apigee.com";
		//this.apiProxyName = apiProxyName;
		this.environment = environment;
		this.revision = revision;
		this.restoreFrom = restoreFrom;
		this.rollOver = rollOver;
		this.activate = activate;
	}*/

	public String getRestoreFrom() {
		return restoreFrom;
	}

	

	public String getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(String timeUnit) {
		this.timeUnit = timeUnit;
	}

	public String getTimeRange() {
		return timeRange;
	}

	public void setTimeRange(String timeRange) {
		this.timeRange = timeRange;
	}

	public String getTimeRangestartDate() {
		return timeRangestartDate;
	}

	public void setTimeRangestartDate(String timeRangestartDate) {
		this.timeRangestartDate = timeRangestartDate;
	}

	public String getTimeRangeendDate() {
		return timeRangeendDate;
	}

	public void setTimeRangeendDate(String timeRangeendDate) {
		this.timeRangeendDate = timeRangeendDate;
	}

	public void setRestoreFrom(String restoreFrom) {
		this.restoreFrom = restoreFrom;
	}

	public boolean getRollOver() {
		return rollOver;
	}

	public void setRollOver(boolean rollOver) {
		this.rollOver = rollOver;
	}

	public boolean getActivate() {
		return activate;
	}

	public void setActivate(boolean activate) {
		this.activate = activate;
	}

	public String getBackUpLocation() {
		return backUpLocation;
	}

	public void setBackUpLocation(String backUpLocation) {
		this.backUpLocation = backUpLocation;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	/*public String getApiProxyName() {
		return apiProxyName;
	}

	public void setApiProxyName(String apiProxyName) {
		this.apiProxyName = apiProxyName;
	}*/

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public Object getProxyInfo() {
		return proxyInfo;
	}

	public void setProxyInfo(Object proxyInfo) {
		this.proxyInfo = proxyInfo;
	}

	public Object getResourceInfo() {
		return resourceInfo;
	}

	public void setResourceInfo(Object resourceInfo) {
		this.resourceInfo = resourceInfo;
	}

	public Object getDevelopersInfo() {
		return developersInfo;
	}

	public void setDevelopersInfo(Object developersInfo) {
		this.developersInfo = developersInfo;
	}

	public Object getProductsInfo() {
		return productsInfo;
	}

	public void setProductsInfo(Object productsInfo) {
		this.productsInfo = productsInfo;
	}

	public Object getAppsInfo() {
		return appsInfo;
	}

	public void setAppsInfo(Object appsInfo) {
		this.appsInfo = appsInfo;
	}

	public String getNewOrg() {
		return newOrg;
	}

	public void setNewOrg(String newOrg) {
		this.newOrg = newOrg;
	}

	public String getNewEnv() {
		return newEnv;
	}

	public void setNewEnv(String newEnv) {
		this.newEnv = newEnv;
	}

	public List<String> getProxiesList() {
		return proxiesList;
	}

	public void setProxiesList(List<String> proxiesList) {
		this.proxiesList = proxiesList;
	}

	public List<String> getSelectedEnvironments() {
		return selectedEnvironments;
	}

	public void setSelectedEnvironments(List<String> selectedEnvironments) {
		this.selectedEnvironments = selectedEnvironments;
	}

	public List<String> getSelectedSharedflows() {
		return selectedSharedflows;
	}

	public void setSelectedSharedflows(List<String> selectedSharedflows) {
		this.selectedSharedflows = selectedSharedflows;
	}

	public List<EnvironmentVO> getEnvironments() {
		return environments;
	}

	public void setEnvironments(List<EnvironmentVO> environments) {
		this.environments = environments;
	}

	public String getAppID() {
		return appID;
	}

	public void setAppID(String appID) {
		this.appID = appID;
	}

	public String getApiProductName() {
		return apiProductName;
	}

	public void setApiProductName(String apiProductName) {
		this.apiProductName = apiProductName;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public String getSharedflowName() {
		return sharedflowName;
	}

	public void setSharedflowName(String sharedflowName) {
		this.sharedflowName = sharedflowName;
	}

	public String getRevisionNumber() {
		return revisionNumber;
	}

	public void setRevisionNumber(String revisionNumber) {
		this.revisionNumber = revisionNumber;
	}

	public String getDeveloperId() {
		return developerId;
	}

	public void setDeveloperId(String developerId) {
		this.developerId = developerId;
	}
	public String getDeveloperEmail() {
		return developerEmail;
	}

	public void setDeveloperEmail(String developerEmail) {
		this.developerEmail = developerEmail;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getCacheName() {
		return cacheName;
	}

	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	public String getKeyValueMapName() {
		return keyValueMapName;
	}

	public void setKeyValueMapName(String keyValueMapName) {
		this.keyValueMapName = keyValueMapName;
	}

	public String getVirtualHostName() {
		return virtualHostName;
	}

	public void setVirtualHostName(String virtualHostName) {
		this.virtualHostName = virtualHostName;
	}

	public String getTargetServerName() {
		return targetServerName;
	}

	public void setTargetServerName(String targetServerName) {
		this.targetServerName = targetServerName;
	}

	public List<String> getSelectedProxies() {
		return selectedProxies;
	}

	public void setSelectedProxies(List<String> selectedProxies) {
		this.selectedProxies = selectedProxies;
	}

	public Set<String> getDevelopersToBackup() {
		return developersToBackup;
	}

	public void setDevelopersToBackup(Set<String> developersToBackup) {
		this.developersToBackup = developersToBackup;
	}

	public String getApiProduct() {
		return apiProduct;
	}

	public void setApiProduct(String apiProduct) {
		this.apiProduct = apiProduct;
	}

	public String getAppDeveloper() {
		return appDeveloper;
	}

	public void setAppDeveloper(String appDeveloper) {
		this.appDeveloper = appDeveloper;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}
	
	public List<String> getEnvApiList() {
		return envApiList;
	}

	public void setEnvApiList(List<String> envApiList) {
		this.envApiList = envApiList;
	}

	public String getSys() {
		return sys;
	}

	public void setSys(String sys) {
		this.sys = sys;
	}

	public JSONArray getJsonArray() {
		return jsonArray;
	}

	public void setJsonArray(JSONArray jsonArray) {
		this.jsonArray = jsonArray;
	}

	public Boolean getIsCleanUpAreBackUp() {
		return isCleanUpAndBackUp;
	}

	public void setIsCleanUpAreBackUp(Boolean isCleanUpAreBackUp) {
		this.isCleanUpAndBackUp = isCleanUpAreBackUp;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public String[] getOrganizations() {
		return organizations;
	}

	public void setOrganizations(String[] organizations) {
		this.organizations = organizations;
	}

	public String getJsessionId() {
		return jsessionId;
	}

	public void setJsessionId(String jsessionId) {
		this.jsessionId = jsessionId;
	}

	public String getApigeeEmail() {
		return apigeeEmail;
	}

	public void setApigeeEmail(String apigeeEmail) {
		this.apigeeEmail = apigeeEmail;
	}

	public String getApigeePassword() {
		return apigeePassword;
	}

	public void setApigeePassword(String apigeePassword) {
		this.apigeePassword = apigeePassword;
	}
	public String getApigeeCred() {
		return apigeeCred;
	}
	public void setApigeeCred(String apigeeCred) {
		this.apigeeCred = apigeeCred;
	}

	public MultipartFile getPostmanFile() {
		return postmanFile;
	}

	public void setPostmanFile(MultipartFile postmanFile) {
		this.postmanFile = postmanFile;
	}

	public MultipartFile getEnvFile() {
		return envFile;
	}

	public void setEnvFile(MultipartFile envFile) {
		this.envFile = envFile;
	}

	public String getJfrogUrl() {
		return jfrogUrl;
	}
	public void setJfrogUrl(String jfrogUrl) {
		this.jfrogUrl = jfrogUrl;
	}
	
	public Boolean getIsDepoyedOnly() {
		return isDepoyedOnly;
	}
	public void setIsDepoyedOnly(Boolean isDepoyedOnly) {
		this.isDepoyedOnly = isDepoyedOnly;
	}
	public String getBackUpLevel() {
		return backUpLevel;
	}
	
	public void setBackUpLevel(String backUpLevel) {
		this.backUpLevel = backUpLevel;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOldOrg() {
		return oldOrg;
	}

	public void setOldOrg(String oldOrg) {
		this.oldOrg = oldOrg;
	}

	public String getOldEnv() {
		return oldEnv;
	}

	public void setOldEnv(String oldEnv) {
		this.oldEnv = oldEnv;
	}

	public String getOperationId() {
		return operationId;
	}

	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}

	public String getInteractionid() {
		return interactionid;
	}

	public void setInteractionid(String interactionid) {
		this.interactionid = interactionid;
	}

	public boolean isCodeCoverage() {
		return isCodeCoverage;
	}

	public void setCodeCoverage(boolean isCodeCoverage) {
		this.isCodeCoverage = isCodeCoverage;
	}

	public boolean isPolicyPerformance() {
		return isPolicyPerformance;
	}

	public void setPolicyPerformance(boolean isPolicyPerformance) {
		this.isPolicyPerformance = isPolicyPerformance;
	}

	
	public boolean isExpand() {
		return expand;
	}

	public void setExpand(boolean expand) {
		this.expand = expand;
	}

	@Override
	public String toString() {
		return "CommonConfiguration [apigeeEmail=" + apigeeEmail + ", apigeePassword=" + apigeePassword + ", apiName="
				+ apiName + ", sharedflowName=" + sharedflowName + ", organization=" + organization + ", environment="
				+ environment + ", jsessionId=" + jsessionId + ", isCleanUpAndBackUp=" + isCleanUpAndBackUp
				+ ", jfrogUrl=" + jfrogUrl + ", newOrg=" + newOrg + ", backUpLocation=" + backUpLocation
				+ ", isDepoyedOnly=" + isDepoyedOnly + ", selectedEnvironments=" + selectedEnvironments
				+ ", selectedProxies=" + selectedProxies + ", selectedSharedflows=" + selectedSharedflows
				+ ", backUpLevel=" + backUpLevel + ", type=" + type + ", oldOrg=" + oldOrg + ", oldEnv=" + oldEnv
				+ ", operationId=" + operationId + ", interactionid=" + interactionid + ", userName=" + userName
				+ ", password=" + password + ", url=" + url + ", revision=" + revision + ", newEnv=" + newEnv + ", dir="
				+ dir + ", restoreFrom=" + restoreFrom + ", rollOver=" + rollOver + ", activate=" + activate
				+ ", proxyInfo=" + proxyInfo + ", resourceInfo=" + resourceInfo + ", developersInfo=" + developersInfo
				+ ", productsInfo=" + productsInfo + ", appsInfo=" + appsInfo + ", proxiesList=" + proxiesList
				+ ", environments=" + environments + ", appID=" + appID + ", apiProductName=" + apiProductName
				+ ", revisionNumber=" + revisionNumber + ", developerId=" + developerId + ", developerEmail="
				+ developerEmail + ", appName=" + appName + ", consumerKey=" + consumerKey + ", cacheName=" + cacheName
				+ ", keyValueMapName=" + keyValueMapName + ", virtualHostName=" + virtualHostName
				+ ", targetServerName=" + targetServerName + ", apiProduct=" + apiProduct + ", appDeveloper="
				+ appDeveloper + ", resource=" + resource + ", envApiList=" + envApiList + ", jsonArray=" + jsonArray
				+ ", sys=" + sys + ", startTime=" + startTime + ", organizations=" + Arrays.toString(organizations)
				+ ", timeRangestartDate=" + timeRangestartDate + ", timeRangeendDate=" + timeRangeendDate
				+ ", postmanFile=" + postmanFile + ", envFile=" + envFile + ", developersToBackup=" + developersToBackup
				+ "]";
	}

	public String getTestsuiteId() {
		return testsuiteId;
	}

	public void setTestsuiteId(String testsuiteId) {
		this.testsuiteId = testsuiteId;
	}

	public String getVariableId() {
		return variableId;
	}

	public void setVariableId(String variableId) {
		this.variableId = variableId;
	}

	
	
	
	
	
}
