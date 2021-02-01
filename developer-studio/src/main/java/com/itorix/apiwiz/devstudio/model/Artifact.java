package com.itorix.apiwiz.devstudio.model;

import java.util.List;

import com.itorix.apiwiz.common.model.proxystudio.Env;
import com.itorix.apiwiz.common.model.proxystudio.OrgEnv;

public class Artifact {
	private String name;
	private List<OrgEnv> org;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<OrgEnv> getOrg() {
		return org;
	}
	public void setOrg(List<OrgEnv> org) {
		this.org = org;
	}

	
	public void setArtifactStatus(String orgName, String type, String envName, String status, String requestId){
		if(org != null){
			for(OrgEnv orgEnv: org){
				if(orgEnv.getName().equals(orgName) && orgEnv.getType().equals(type)){
					if(orgEnv.getEnvs() != null){
						for(Env env : orgEnv.getEnvs()){
							if(env.getName().equals(envName)){
								env.setStatus(status);
								env.setServiceRequestId(requestId);
							}
						}
					}
				}
			}
		}
	}
}
