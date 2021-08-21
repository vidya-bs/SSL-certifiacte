package com.itorix.apiwiz.common.model.apigee;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIProxyDeploymentEnvResponse {
	private APIProxyDeployment APIProxyDeployment;

	public APIProxyDeployment getAPIProxyDeployment() {
		return APIProxyDeployment;
	}

	public void setAPIProxyDeployment(APIProxyDeployment APIProxyDeployment) {
		this.APIProxyDeployment = APIProxyDeployment;
	}

	@Override
	public String toString() {
		return "ClassPojo [APIProxyDeployment = " + APIProxyDeployment + "]";
	}
}
