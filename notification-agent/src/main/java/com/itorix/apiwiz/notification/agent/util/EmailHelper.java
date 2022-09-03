package com.itorix.apiwiz.notification.agent.util;

import com.itorix.apiwiz.notification.agent.properties.NotificationAgentProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;

import static com.itorix.apiwiz.notification.agent.util.MailProperty.*;

@Slf4j
@Component
public class EmailHelper {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private NotificationAgentProperties applicationProperties;

    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl  javaMailSender = new JavaMailSenderImpl();
        log.debug("Fetching mailProperty from tenant {} ", mongoTemplate.getDb().getName());
        List<MailProperty> mailProperties = mongoTemplate.findAll(MailProperty.class);
        if(mailProperties.size() > 0 ) {
            MailProperty mailProperty = mailProperties.get(0);
            javaMailSender.setJavaMailProperties(getMailProperty(mailProperty));
            javaMailSender.setUsername(mailProperty.getUserName());
            javaMailSender.setPassword(mailProperty.getPassword());
        } else {
            log.debug("Mail connector isn't configured for tenant {} falling back to default mail connection", mongoTemplate.getDb().getName());
            javaMailSender.setJavaMailProperties(getMailProperty(null));
            javaMailSender.setUsername(applicationProperties.getSmtpUserName());
            javaMailSender.setPassword(applicationProperties.getSmtpPassword());
        }

        return javaMailSender;

    }

    public Properties getMailProperty(MailProperty mailProperty) {
        Properties props = new Properties();
        if(mailProperty != null) {
            log.debug("Getting Mail properties");
            props.put(MAIL_PORT, mailProperty.getMailPort());
            props.put(MAIL_HOST, mailProperty.getMailHost());
            props.put(MAIL_ENABLE_START_TLS, mailProperty.isEnableStartTLS());
            props.put(MAIL_SMTP_AUTH, mailProperty.isSmtpAuth());
            props.put(MAIL_SMTP_CONN_TIMEOUT, mailProperty.getConnectionTimeOut());
            props.put(MAIL_TIMEOUT, mailProperty.getTimeOut());
            props.put(MAIL_WRITE_TIMEOUT, mailProperty.getWriteTimeOut());
            props.put(MAIL_SMTP_FROM, mailProperty.getMailFromAddress());
        } else {
            props.put(MAIL_PORT, applicationProperties.getSmtpPort());
            props.put(MAIL_HOST, applicationProperties.getSmtpHost());
            props.put(MAIL_ENABLE_START_TLS, applicationProperties.isSMTPStartTLSEnabled());
            props.put(MAIL_SMTP_AUTH, applicationProperties.isSMTPAutEnabled());
            props.put(MAIL_SMTP_CONN_TIMEOUT, applicationProperties.getSmtpConnectionTimeOut());
            props.put(MAIL_TIMEOUT, applicationProperties.getSmtpTimeOut());
            props.put(MAIL_WRITE_TIMEOUT, applicationProperties.getSmtpWriteTimeOut());
            props.put(MAIL_SMTP_FROM, applicationProperties.getSmtpFromAddress());
        }
        return props;
    }

    public String getFromAddress() {
        List<MailProperty> mailProperties = mongoTemplate.findAll(MailProperty.class);
        if(mailProperties.size() > 0 ) {
            MailProperty mailProperty = mailProperties.get(0);
            return mailProperty.getMailFromAddress();
        } else {
            return applicationProperties.getSmtpFromAddress();
        }
    }
}
