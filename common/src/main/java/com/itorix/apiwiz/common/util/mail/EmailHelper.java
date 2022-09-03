package com.itorix.apiwiz.common.util.mail;

import com.itorix.apiwiz.common.properties.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;

import static com.itorix.apiwiz.common.util.mail.MailProperty.*;

@Component
public class EmailHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailHelper.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private ApplicationProperties applicationProperties;

	public JavaMailSender getJavaMailSender() {
		JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
		LOGGER.info("Fetching mailProperty from tenant {} ", mongoTemplate.getDb().getName());
		List<MailProperty> mailProperties = mongoTemplate.findAll(MailProperty.class);
		if (mailProperties.size() > 0) {
			LOGGER.debug("Getting Java mail sender");
			MailProperty mailProperty = mailProperties.get(0);
			javaMailSender.setJavaMailProperties(getMailProperty(mailProperty));
			javaMailSender.setUsername(mailProperty.getUserName());
			javaMailSender.setPassword(mailProperty.getPassword());
		} else {
			LOGGER.debug("Mail connector isn't configured for tenant {} falling back to default mail connection",
					mongoTemplate.getDb().getName());
			javaMailSender.setJavaMailProperties(getMailProperty(null));
			javaMailSender.setUsername(applicationProperties.getCicdUserName());
			javaMailSender.setPassword(applicationProperties.getCicdPassWord());
		}

		return javaMailSender;

	}

	public Properties getMailProperty(MailProperty mailProperty) {
		Properties props = new Properties();
		if (mailProperty != null) {
			LOGGER.debug("Getting Mail property");
			props.put(MAIL_PORT, mailProperty.getMailPort());
			props.put(MAIL_HOST, mailProperty.getMailHost());
			props.put(MAIL_ENABLE_START_TLS, mailProperty.isEnableStartTLS());
			props.put(MAIL_SMTP_AUTH, mailProperty.isSmtpAuth());
			props.put(MAIL_SMTP_CONN_TIMEOUT, mailProperty.getConnectionTimeOut());
			props.put(MAIL_TIMEOUT, mailProperty.getTimeOut());
			props.put(MAIL_WRITE_TIMEOUT, mailProperty.getWriteTimeOut());
			props.put(MAIL_SMTP_FROM, mailProperty.getMailFromAddress());
		} else {
			props.put(MAIL_PORT, applicationProperties.getCicdSmtpPort());
			props.put(MAIL_HOST, applicationProperties.getCicdSmtphostName());
			props.put(MAIL_ENABLE_START_TLS, applicationProperties.getCicdSmtpStartttls());
			props.put(MAIL_SMTP_AUTH, applicationProperties.getCicdSmtpAuth());
			props.put(MAIL_SMTP_CONN_TIMEOUT, applicationProperties.getSmtpConnectionTimeOut());
			props.put(MAIL_TIMEOUT, applicationProperties.getSmtpTimeOut());
			props.put(MAIL_WRITE_TIMEOUT, applicationProperties.getSmtpWriteTimeOut());
			props.put(MAIL_SMTP_FROM, applicationProperties.getSmtpFromAddress());
		}
		return props;
	}

	public String getFromAddress() {
		List<MailProperty> mailProperties = mongoTemplate.findAll(MailProperty.class);
		if (mailProperties.size() > 0) {
			MailProperty mailProperty = mailProperties.get(0);
			return mailProperty.getMailFromAddress();
		} else {
			return applicationProperties.getSmtpFromAddress();
		}
	}
}
