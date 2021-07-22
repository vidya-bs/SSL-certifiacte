package com.itorix.apiwiz.identitymanagement.cofiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.itorix.apiwiz.common.beans.CommonApplicationBeans;
import com.itorix.apiwiz.common.util.mail.MailUtil;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.logging.LoggerService;

@Configuration
@Import({CommonApplicationBeans.class})
@EnableMongoRepositories("com.itorix.apiwiz.identitymanagement.dao")
public class IdentityBeans {

	// @Autowired
	// private UserSessionRepository userSessionRepository;

	@Bean(name = "baseRepository")
	public BaseRepository baseRepositoryBean() {
		BaseRepository baseRepository = new BaseRepository();
		return baseRepository;
	}

	@Bean(name = "mailUtil")
	public MailUtil mailUtilBean() {
		MailUtil mailUtil = new MailUtil();
		return mailUtil;
	}

	@Bean(name = "loggerService")
	public LoggerService loggerServiceBean() {
		LoggerService loggerService = new LoggerService();
		return loggerService;
	}

	@Bean(name = "identityManagementDao")
	public IdentityManagementDao identityManagementDaoBean() {
		IdentityManagementDao identityManagementDao = new IdentityManagementDao();
		return identityManagementDao;
	}
}
