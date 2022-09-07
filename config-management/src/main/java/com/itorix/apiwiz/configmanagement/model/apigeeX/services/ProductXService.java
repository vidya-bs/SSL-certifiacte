package com.itorix.apiwiz.configmanagement.model.apigeeX.services;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.model.apigeeX.ProductConfig;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.apigeeX.ApigeeXUtill;
import com.itorix.apiwiz.configmanagement.model.apigee.ApigeeProduct;
import com.itorix.apiwiz.configmanagement.model.apigeeX.ApigeexProduct;

@Component
public class ProductXService {

	@Autowired
	private ApigeeXUtill apigeeXUtil;

	public String getProductURL(ProductConfig config) throws ItorixException {
		try {
			String org = config.getOrg();
			String URL = apigeeXUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/apiproducts";
			return URL;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public String getUpdateProductURL(ProductConfig config) throws ItorixException {
		try {
			String org = config.getOrg();
			String name = config.getName();
			String URL = apigeeXUtil.getApigeeHost(org) + "/v1/organizations/" + org + "/apiproducts/" + name;
			return URL;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public ApigeexProduct getProductBody(ProductConfig config) throws ItorixException {
		try {
			ApigeexProduct apigeeProduct = new ApigeexProduct();
			BeanUtils.copyProperties(config, apigeeProduct);
			return apigeeProduct;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

}
