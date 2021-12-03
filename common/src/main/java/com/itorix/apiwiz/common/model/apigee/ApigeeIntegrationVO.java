package com.itorix.apiwiz.common.model.apigee;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.itorix.apiwiz.common.model.AbstractObject;
import java.util.List;

public class ApigeeIntegrationVO extends AbstractObject {

	private String orgname;

	private String type;

	private String scheme;

	private String hostname;

	private String port;

	private String userName;

	private String password;

	private String authType = "basic";

	private String tokenURL;

	private String grantType = "password";

	private String basicToken = "Basic ZWRnZWNsaTplZGdlY2xpc2VjcmV0";

	private List<String> environments;

	public String getOrgname() {
		return orgname;
	}

	public void setOrgname(String orgname) {
		this.orgname = orgname;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		if (type.equalsIgnoreCase("isSaaS"))
			this.type = "saas";
		else
			this.type = type;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
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

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getTokenURL() {
		return tokenURL;
	}

	public void setTokenURL(String tokenURL) {
		this.tokenURL = tokenURL;
	}

	public String getGrantType() {
		return grantType;
	}

	public void setGrantType(String grantType) {
		this.grantType = grantType;
	}

	public String getBasicToken() {
		return basicToken;
	}

	public void setBasicToken(String basicToken) {
		this.basicToken = basicToken;
	}

	public List<String> getEnvironments() {
		return environments;
	}

	public void setEnvironments(List<String> environments) {
		this.environments = environments;
	}

	@JsonIgnore
	public ApigeeConfigurationVO getApigeeConfigObject() {
		ApigeeConfigurationVO apigeeConfigurationVO = new ApigeeConfigurationVO();
		ApigeeServiceUser apigeeServiceUser = new ApigeeServiceUser();
		apigeeConfigurationVO.setId(this.getId());
		apigeeConfigurationVO.setCreatedBy(this.getCreatedBy());
		apigeeConfigurationVO.setModifiedBy(this.getModifiedBy());
		apigeeConfigurationVO.setCreatedUserName(this.getCreatedUserName());
		apigeeConfigurationVO.setModifiedUserName(this.getModifiedUserName());
		apigeeConfigurationVO.setCts(this.getCts());
		apigeeConfigurationVO.setMts(this.getMts());
		apigeeConfigurationVO.setHostname(this.hostname);
		apigeeConfigurationVO.setOrgname(this.orgname);
		apigeeConfigurationVO.setType(this.type);
		apigeeConfigurationVO.setPort(this.port);
		apigeeConfigurationVO.setScheme(this.scheme);
		apigeeConfigurationVO.setEnvironments(this.environments);
		apigeeServiceUser.setUserName(this.userName);
		apigeeServiceUser.setPassword(this.password);
		apigeeServiceUser.setAuthType(this.authType);
		apigeeServiceUser.setTokenURL(this.tokenURL);
		apigeeServiceUser.setGrantType(this.grantType);
		apigeeServiceUser.setBasicToken(this.basicToken);
		apigeeServiceUser.setOrgName(this.orgname);
		apigeeServiceUser.setType(this.type);
		apigeeConfigurationVO.setApigeeServiceUser(apigeeServiceUser);
		return apigeeConfigurationVO;
	}

	@JsonIgnore
	public void setApigeeConfigObject(ApigeeConfigurationVO apigeeConfigurationVO) {
		this.setId(apigeeConfigurationVO.getId());
		this.setCreatedBy(apigeeConfigurationVO.getCreatedBy());
		this.setModifiedBy(apigeeConfigurationVO.getModifiedBy());
		this.setCreatedUserName(apigeeConfigurationVO.getCreatedUserName());
		this.setModifiedUserName(apigeeConfigurationVO.getModifiedUserName());
		this.setCts(apigeeConfigurationVO.getCts());
		this.setMts(apigeeConfigurationVO.getMts());
		this.hostname = apigeeConfigurationVO.getHostname();
		this.orgname = apigeeConfigurationVO.getOrgname();
		this.type = apigeeConfigurationVO.getType();
		this.port = apigeeConfigurationVO.getPort();
		this.scheme = apigeeConfigurationVO.getScheme();
		this.environments = apigeeConfigurationVO.getEnvironments();
		ApigeeServiceUser apigeeServiceUser = apigeeConfigurationVO.getApigeeServiceUser();
		if (apigeeServiceUser != null) {
			this.userName = apigeeServiceUser.getUserName();
			this.password = apigeeServiceUser.getPassword();
			this.authType = apigeeServiceUser.getAuthType();
			this.tokenURL = apigeeServiceUser.getTokenURL();
			this.grantType = apigeeServiceUser.getGrantType();
			this.basicToken = apigeeServiceUser.getBasicToken();
		}
	}

	public ApigeeIntegrationVO() {
		super();
	}

	public ApigeeIntegrationVO(ApigeeConfigurationVO apigeeConfigurationVO) {
		super();
		this.setId(apigeeConfigurationVO.getId());
		this.setCreatedBy(apigeeConfigurationVO.getCreatedBy());
		this.setModifiedBy(apigeeConfigurationVO.getModifiedBy());
		this.setCreatedUserName(apigeeConfigurationVO.getCreatedUserName());
		this.setModifiedUserName(apigeeConfigurationVO.getModifiedUserName());
		this.setCts(apigeeConfigurationVO.getCts());
		this.setMts(apigeeConfigurationVO.getMts());
		this.hostname = apigeeConfigurationVO.getHostname();
		this.orgname = apigeeConfigurationVO.getOrgname();
		this.type = apigeeConfigurationVO.getType();
		this.port = apigeeConfigurationVO.getPort();
		this.scheme = apigeeConfigurationVO.getScheme();
		this.environments = apigeeConfigurationVO.getEnvironments();
		ApigeeServiceUser apigeeServiceUser = apigeeConfigurationVO.getApigeeServiceUser();
		if (apigeeServiceUser != null) {
			this.userName = apigeeServiceUser.getUserName();
			this.password = apigeeServiceUser.getPassword();
			this.authType = apigeeServiceUser.getAuthType();
			this.tokenURL = apigeeServiceUser.getTokenURL();
			this.grantType = apigeeServiceUser.getGrantType();
			this.basicToken = apigeeServiceUser.getBasicToken();
		}
	}
}
