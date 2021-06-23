package com.itorix.apiwiz.configmanagement.model.apigee.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.model.configmanagement.KVMConfig;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.configmanagement.model.apigee.ApigeeKVM;


@Component
public class KVMService {
	@Autowired
	private ApigeeUtil apigeeUtil;
	
	public String getKVMURL(KVMConfig config) throws ItorixException{
		try{
			String env = config.getEnv();
			String org = config.getOrg();
			String URL  = apigeeUtil.getApigeeHost(config.getType()==null?"saas":config.getType(), org) + "v1/organizations/" + org + "/environments/" + env + "/keyvaluemaps";
			return URL;
		}
		catch( Exception ex){
			throw new ItorixException(ex.getMessage(),"Configuration-1000", ex );
		}
	}
	
	public String getUpdateKVMURL(KVMConfig config) throws ItorixException{
		try{
			String env = config.getEnv();
			String org = config.getOrg();
			String URL  = apigeeUtil.getApigeeHost(config.getType()==null?"saas":config.getType(), org) + "v1/organizations/" + org + "/environments/" + env + "/keyvaluemaps/" + config.getName();
			return URL;
		}
		catch( Exception ex){
			throw new ItorixException(ex.getMessage(),"Configuration-1000", ex );
		}
	}
	
	public String getCPSURL(KVMConfig config) throws ItorixException{
		try{
			return  apigeeUtil.getApigeeHost(config.getType()==null?"saas":config.getType(), config.getOrg()) +  "v1/organizations/" + config.getOrg();
		}
		catch( Exception ex){
			throw new ItorixException(ex.getMessage(),"Configuration-1000", ex );
		}
	}
	
	public String getCPSKVMDeleteURL(KVMConfig config) throws ItorixException{
		try{
			return  apigeeUtil.getApigeeHost(config.getType()==null?"saas":config.getType(), config.getOrg()) +  "v1/organizations/" + config.getOrg()+"/environments/"+ config.getEnv() +"/keyvaluemaps/" + config.getName();
		}
		catch( Exception ex){
			throw new ItorixException(ex.getMessage(),"Configuration-1000", ex );
		}
	}

	public ApigeeKVM getKVMBody(KVMConfig config) throws ItorixException{
		try{
			ApigeeKVM apigeeKVM = new ApigeeKVM();
			apigeeKVM.setName(config.getName());
			apigeeKVM.setEncrypted(config.getEncrypted());
			apigeeKVM.setEntry(config.getEntry());
			return apigeeKVM;
		}
		catch( Exception ex){
			throw new ItorixException(ex.getMessage(),"Configuration-1000", ex );
		}
	}
}
