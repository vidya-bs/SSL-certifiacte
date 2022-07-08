package com.itorix.apiwiz.configmanagement.model.apigeeX.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.model.apigeeX.KVMConfig;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.apigeeX.ApigeeXUtill;
import com.itorix.apiwiz.configmanagement.model.apigee.ApigeeKVM;

@Component
public class XKVMService {
	@Autowired
	private ApigeeXUtill apigeeXUtil;

	public String getKVMURL(KVMConfig config) throws ItorixException {
		try {
			String env = config.getEnv();
			String org = config.getOrg();
			String URL = apigeeXUtil.getApigeeHost(org)
					+ "/v1/organizations/" + org + "/environments/" + env + "/keyvaluemaps";
			return URL;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public String getUpdateKVMURL(KVMConfig config) throws ItorixException {
		try {
			String env = config.getEnv();
			String org = config.getOrg();
			String URL = apigeeXUtil.getApigeeHost(org)
					+ "/v1/organizations/" + org + "/environments/" + env + "/keyvaluemaps/" + config.getName();
			return URL;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public String getCPSURL(KVMConfig config) throws ItorixException {
		try {
			return apigeeXUtil.getApigeeHost(config.getOrg())
					+ "/v1/organizations/" + config.getOrg();
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public String getCPSKVMDeleteURL(KVMConfig config) throws ItorixException {
		try {
			return apigeeXUtil.getApigeeHost(config.getOrg())
					+ "/v1/organizations/" + config.getOrg() + "/environments/" + config.getEnv() + "/keyvaluemaps/"
					+ config.getName();
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public ApigeeKVM getKVMBody(KVMConfig config) throws ItorixException {
		try {
			ApigeeKVM apigeeKVM = new ApigeeKVM();
			apigeeKVM.setName(config.getName());
			apigeeKVM.setEncrypted("true");
			//apigeeKVM.setEntry(config.getEntry());
			return apigeeKVM;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}
}
