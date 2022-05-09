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
	private List<ApigeeXEnvironment> evironments;
	
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
	public List<ApigeeXEnvironment> getEvironments() {
		return evironments;
	}
	public void setEvironments(List<ApigeeXEnvironment> evironments) {
		this.evironments = evironments;
	}
	public List<String> getEnvironmentNames(){
		if(null == evironments){
			return null;
		}else{
			List<String> envNames = new ArrayList<>();
			for(ApigeeXEnvironment env: evironments){
				envNames.add(env.getName());
			}
			return envNames;
		}
		
	}
	
}
