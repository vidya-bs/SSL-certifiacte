package com.itorix.apiwiz.common.model.apigeeX;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.model.AbstractObject;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Connectors.ApigeeX.Configuration")
public class ApigeeXConfigurationVO extends AbstractObject{
	
	private String orgName;
	private String jsonKey;
	private List<ApigeeXEnvironment> environments;
	
	public String getOrgName() {
		return orgName;
	}
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	public String getJsonKey() {
		return jsonKey;
	}
	public void setJsonKey(String jsonKey) {
		this.jsonKey = jsonKey;
	}
	public List<ApigeeXEnvironment> getEnvironments() {
		return environments;
	}
	public void setEvironments(List<ApigeeXEnvironment> environments) {
		this.environments = environments;
	}
	public List<String> getEnvironmentNames(){
		if(null == environments){
			return null;
		}else{
			List<String> envNames = new ArrayList<>();
			for(ApigeeXEnvironment env: environments){
				envNames.add(env.getName());
			}
			return envNames;
		}
		
	}
	
}
