package com.itorix.apiwiz.configmanagement.model.apigee.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.model.configmanagement.CacheConfig;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.configmanagement.model.apigee.ApigeeCache;
import com.itorix.apiwiz.configmanagement.model.apigee.ExpiryDate;
import com.itorix.apiwiz.configmanagement.model.apigee.ExpirySettings;

@Component
public class CacheService {
	@Autowired
	private ApigeeUtil apigeeUtil;

	public String getCacheURL(CacheConfig config) throws ItorixException {
		try {
			String env = config.getEnv();
			String org = config.getOrg();
			String name = config.getName();
			return apigeeUtil.getApigeeHost(config.getType() == null ? "saas" : config.getType(), org)
					+ "v1/organizations/" + org + "/environments/" + env + "/caches?name=" + name;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public String getUpdateCacheURL(CacheConfig config) throws ItorixException {
		try {
			String env = config.getEnv();
			String org = config.getOrg();
			String name = config.getName();
			return apigeeUtil.getApigeeHost(config.getType() == null ? "saas" : config.getType(), org)
					+ "v1/organizations/" + org + "/environments/" + env + "/caches/" + name;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public ApigeeCache getCacheBody(CacheConfig config) throws ItorixException {
		try {
			ApigeeCache apigeeCache = new ApigeeCache();
			ExpirySettings expirySettings = new ExpirySettings();
			ExpiryDate expiryDate = new ExpiryDate();
			apigeeCache.setDescription(config.getDescription());
			apigeeCache.setExpirySettings(expirySettings);
			apigeeCache.setSkipCacheIfElementSizeInKBExceeds(
					config.getSkipCacheIfElementSizeInKBExceeds());
			if (config.isOverflowToDisk()) {
				apigeeCache.setOverflowToDisk("true");
			} else {
				apigeeCache.setOverflowToDisk("false");
			}
			if (StringUtils.equalsIgnoreCase(config.getExpiryType(), "expiryDate")
					&& config.getExpiryDate() != null) {
				expiryDate.setValue(config.getExpiryDate());
				expirySettings.setExpiryDate(expiryDate);
			} else if (StringUtils.equalsIgnoreCase(config.getExpiryType(), "timeOfDay")
					&& config.getTimeOfDay() != null) {
				expiryDate.setValue(config.getTimeOfDay());
				expirySettings.setTimeOfDay(expiryDate);
			} else if (StringUtils.equalsIgnoreCase(config.getExpiryType(), "timeoutInSec")
					&& config.getTimeoutInSec() != null) {
				expiryDate.setValue(config.getTimeoutInSec());
				expirySettings.setTimeoutInSec(expiryDate);
			}
			if (config.isValuesNull()) {
				expirySettings.setValuesNull("true");
			} else {
				expirySettings.setValuesNull("false");
			}
			return apigeeCache;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}
}
