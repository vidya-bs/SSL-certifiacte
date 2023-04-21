package com.itorix.apiwiz.configmanagement.model.apigee.services;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.itorix.apiwiz.common.model.configmanagement.DeveloperCategoryConfig;
import com.itorix.apiwiz.common.model.configmanagement.ProductBundleConfig;
import com.itorix.apiwiz.common.model.configmanagement.ProductConfig;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.configmanagement.model.apigee.ApigeeProduct;

@Component
public class ProductService {

	@Autowired
	private ApigeeUtil apigeeUtil;

	public String getProductURL(ProductConfig config) throws ItorixException {
		try {
			String org = config.getOrg();
			String URL = apigeeUtil.getApigeeHost(config.getType() == null ? "saas" : config.getType(), config.getOrg())
					+ "v1/organizations/" + org + "/apiproducts";
			return URL;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public String getProductBundleURL(ProductBundleConfig productBundleConfig, boolean update) throws ItorixException {
		try {
			String org = productBundleConfig.getOrganization();
			String URL = "";
			if(update) {
				URL = apigeeUtil.getApigeeHost("", org)
						+ "v1/mint/organizations/" + org + "/monetization-packages/"+productBundleConfig.getId();
			}else{
				URL = apigeeUtil.getApigeeHost("", org)
						+ "v1/mint/organizations/" + org + "/monetization-packages/";
			}
			return URL;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public String getDeveloperCategoryURL(String id,String orgName, boolean update) throws ItorixException {
		try {
			String URL;
			if(update){
				URL = apigeeUtil.getApigeeHost("", orgName)
						+ "v1/mint/organizations/" + orgName + "/developer-categories/"+id;
			}else{
				URL = apigeeUtil.getApigeeHost("", orgName)
						+ "v1/mint/organizations/" + orgName + "/developer-categories";
			}
			return URL;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public String getWebhookURL(String id,String orgName, boolean update) throws ItorixException {
		try {
			String URL;
			if(update){
				URL = apigeeUtil.getApigeeHost("", orgName)
						+ "v1/mint/organizations/" + orgName + "/webhooks/"+id;
			}else{
				URL = apigeeUtil.getApigeeHost("", orgName)
						+ "v1/mint/organizations/" + orgName + "/webhooks";
			}
			return URL;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public String getCompanyURL(String name,String orgName, boolean update) throws ItorixException {
		try {
			String URL;
			if(update){
				URL = apigeeUtil.getApigeeHost("", orgName)
						+ "v1/organizations/" + orgName + "/companies/"+name;
			}else{
				URL = apigeeUtil.getApigeeHost("", orgName)
						+ "v1/organizations/" + orgName + "/companies";
			}
			return URL;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public String getUpdateProductURL(ProductConfig config) throws ItorixException {
		try {
			String org = config.getOrg();
			String name = config.getName();
			String URL = apigeeUtil.getApigeeHost(config.getType() == null ? "saas" : config.getType(), config.getOrg())
					+ "v1/organizations/" + org + "/apiproducts/" + name;
			return URL;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public ApigeeProduct getProductBody(ProductConfig config) throws ItorixException {
		try {
			ApigeeProduct apigeeProduct = new ApigeeProduct();
			BeanUtils.copyProperties(config, apigeeProduct);
			return apigeeProduct;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}
}
