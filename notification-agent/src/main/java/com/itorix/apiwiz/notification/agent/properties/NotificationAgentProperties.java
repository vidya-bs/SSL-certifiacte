package com.itorix.apiwiz.notification.agent.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource(value = "file:${config.properties}", ignoreResourceNotFound = true)
public class NotificationAgentProperties {

	@Value("${spring.mail.username}")
	private String smtpUserName;

	@Value("${spring.mail.password}")
	private String smtpPassword;

	@Value("${spring.mail.port}")
	private String smtpPort;

	@Value("${spring.mail.properties.mail.smtp.auth}")
	private boolean isSMTPAutEnabled;

	@Value("${spring.mail.properties.mail.smtp.starttls.enable}")
	private boolean isSMTPStartTLSEnabled;

	@Value("${spring.mail.host}")
	private String smtpHost;

	@Value("${spring.mail.properties.mail.smtp.connectiontimeout}")
	private long smtpConnectionTimeOut;

	@Value("${spring.mail.properties.mail.smtp.timeout}")
	private long smtpTimeOut;

	@Value("${spring.mail.properties.mail.smtp.writetimeout}")
	private long smtpWriteTimeOut;

	@Value("${spring.mail.properties.mail.smtp.from}")
	private String smtpFromAddress;

	public String getSmtpUserName() {
		return smtpUserName;
	}

	public void setSmtpUserName(String smtpUserName) {
		this.smtpUserName = smtpUserName;
	}

	public String getSmtpPassword() {
		return smtpPassword;
	}

	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}

	public String getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(String smtpPort) {
		this.smtpPort = smtpPort;
	}

	public boolean isSMTPAutEnabled() {
		return isSMTPAutEnabled;
	}

	public void setSMTPAutEnabled(boolean SMTPAutEnabled) {
		isSMTPAutEnabled = SMTPAutEnabled;
	}

	public boolean isSMTPStartTLSEnabled() {
		return isSMTPStartTLSEnabled;
	}

	public void setSMTPStartTLSEnabled(boolean SMTPStartTLSEnabled) {
		isSMTPStartTLSEnabled = SMTPStartTLSEnabled;
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public long getSmtpConnectionTimeOut() {
		return smtpConnectionTimeOut;
	}

	public void setSmtpConnectionTimeOut(long smtpConnectionTimeOut) {
		this.smtpConnectionTimeOut = smtpConnectionTimeOut;
	}

	public long getSmtpTimeOut() {
		return smtpTimeOut;
	}

	public void setSmtpTimeOut(long smtpTimeOut) {
		this.smtpTimeOut = smtpTimeOut;
	}

	public long getSmtpWriteTimeOut() {
		return smtpWriteTimeOut;
	}

	public void setSmtpWriteTimeOut(long smtpWriteTimeOut) {
		this.smtpWriteTimeOut = smtpWriteTimeOut;
	}

	public String getSmtpFromAddress() {
		return smtpFromAddress;
	}

	public void setSmtpFromAddress(String smtpFromAddress) {
		this.smtpFromAddress = smtpFromAddress;
	}
}
