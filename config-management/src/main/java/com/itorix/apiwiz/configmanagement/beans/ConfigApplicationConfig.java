package com.itorix.apiwiz.configmanagement.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.itorix.apiwiz.common.beans.CommonApplicationBeans;
import com.itorix.apiwiz.configmanagement.dao.ConfigManagementDao;
import com.itorix.apiwiz.identitymanagement.cofiguration.IdentityBeans;

@Configuration
@Import({CommonApplicationBeans.class, IdentityBeans.class})
public class ConfigApplicationConfig {
	@Autowired
	@Bean(name = "configManagementDAO")
	public ConfigManagementDao configManagementdao() {
		ConfigManagementDao configManagementDao = new ConfigManagementDao();
		return configManagementDao;
	}
}
