package com.itorix.apiwiz.configmanagement.model.apigee.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.model.configmanagement.TargetConfig;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.configmanagement.model.apigee.ApigeeTarget;
import com.itorix.apiwiz.configmanagement.model.apigee.SSLInfo;



@Component
public class TargetConnection {
	@Autowired
	private ApigeeUtil apigeeUtil;

	public String getTargetURL(TargetConfig config) throws ItorixException{
		try{
			String env = config.getEnv();
			String org = config.getOrg();
			String URL  =  apigeeUtil.getApigeeHost(config.getType()==null?"saas":config.getType(), org) + "v1/organizations/" + org + "/environments/" + env + "/targetservers";
			return URL;
		}
		catch( Exception ex){
			throw new ItorixException(ex.getMessage(),"ProxyGen-1000", ex );
		}
	}
	
	public String getUpdateTargetURL(TargetConfig config) throws ItorixException{
		try{
			String env = config.getEnv();
			String org = config.getOrg();
			String URL  = apigeeUtil.getApigeeHost(config.getType()==null?"saas":config.getType(), org) + "v1/organizations/" + org + "/environments/" + env + "/targetservers/"+ config.getName();
			return URL;
		}
		catch( Exception ex){
			throw new ItorixException(ex.getMessage(),"ProxyGen-1000", ex );
		}
	}

	public ApigeeTarget getTargetBody(TargetConfig config) throws ItorixException{
		try{
			ApigeeTarget target = new ApigeeTarget();
			SSLInfo info = new SSLInfo();
			target.setSSLInfo(info);
			target.setHost(config.getHost());
			target.setName(config.getName());
			target.setIsEnabled(config.isEnabled());
			target.setPort(config.getPort());
			if(config.isClientAuthEnabled())
				info.setClientAuthEnabled("true");
			else
				info.setClientAuthEnabled("false");
			if(config.isSslEnabled())
				info.setEnabled("true");
			else
				info.setEnabled("false");
			info.setIgnoreValidationErrors(config.isIgnoreValidationErrors());
			info.setKeyAlias(config.getKeyAlias());
			info.setKeyStore(config.getKeyStore());
			info.setTrustStore(config.getTrustStore());
			return target;
		}
		catch( Exception ex){
			throw new ItorixException(ex.getMessage(),"ProxyGen-1000", ex );
		}
	}
}
