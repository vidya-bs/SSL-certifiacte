package com.itorix.apiwiz.common.util.mail;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.properties.ApplicationProperties;

@Component
public class MailUtil {

	static Properties mailServerProperties;
	static Session getMailSession;
	static MimeMessage generateMailMessage;
	Logger logger = Logger.getLogger(MailUtil.class);

	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired
	private JavaMailSender javaMailSender;

	public void sendEmail(EmailTemplate emailTemplate) throws MessagingException {
		try {
			MimeMessage msg = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(msg, true);
			// SimpleMailMessage msg = new SimpleMailMessage();
			String[] internetAddress = new String[emailTemplate.getToMailId().size()];
			int i = 0;
			for (String tomail : emailTemplate.getToMailId()) {
				internetAddress[i] = tomail;
				i++;
			}
			helper.setTo(internetAddress);
			if (emailTemplate.getSubject() != null) {
				helper.setSubject(emailTemplate.getSubject());
			} else {
				helper.setSubject(applicationProperties.getMailSubject());
			}
			helper.setText(emailTemplate.getBody(), true);
			javaMailSender.send(msg);
		} catch (MessagingException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
	}

	// public void sendEmail1(EmailTemplate emailTemplate) throws
	// MessagingException {
	//
	// try{
	// mailServerProperties = System.getProperties();
	//
	// mailServerProperties.put("mail.smtp.port",
	// applicationProperties.getSmtpPort());
	// mailServerProperties.put("mail.smtp.auth",
	// applicationProperties.getSmtpAuth());
	// mailServerProperties.put("mail.smtp.starttls.enable",
	// applicationProperties.getSmtpStartttls());
	//
	// List<String> toMailId = emailTemplate.getToMailId();
	// InternetAddress[] internetAddress = new InternetAddress[toMailId.size()];
	// int i=0;
	// for (String tomail : toMailId) {
	// internetAddress[i] = new InternetAddress(tomail);
	// i++;
	// }
	// getMailSession = Session.getDefaultInstance(mailServerProperties, null);
	// generateMailMessage = new MimeMessage(getMailSession);
	// generateMailMessage.addRecipients(Message.RecipientType.TO,
	// internetAddress);
	//
	// if(emailTemplate.getSubject()!=null){
	// generateMailMessage.setSubject(emailTemplate.getSubject());
	// }else{
	// generateMailMessage.setSubject(applicationProperties.getMailSubject());
	// }
	// generateMailMessage.setContent(emailTemplate.getBody(), "text/html;
	// charset=utf-8");
	// Transport transport = getMailSession.getTransport("smtp");
	//
	// // if you have 2FA enabled then provide App Specific Password
	// String password = applicationProperties.getPassWord();
	// transport.connect(applicationProperties.getSmtphostName(),
	// applicationProperties.getUserName(),
	// applicationProperties.getPassWord());
	// transport.sendMessage(generateMailMessage,
	// generateMailMessage.getAllRecipients());
	// transport.close();
	// }catch (Exception e) {
	// e.printStackTrace();
	// //e.printStackTrace();
	// logger.error(e);
	// }
	// }

	public void sendEmailWithAttachments(EmailTemplate emailTemplate) throws MessagingException, IOException {

		Multipart multipart = new MimeMultipart();

		mailServerProperties = System.getProperties();
		mailServerProperties.put("mail.smtp.port", applicationProperties.getCicdSmtpPort());
		mailServerProperties.put("mail.smtp.auth", applicationProperties.getCicdSmtpAuth());
		mailServerProperties.put("mail.smtp.starttls.enable", applicationProperties.getCicdSmtpStartttls());

		List<String> toMailId = emailTemplate.getToMailId();
		InternetAddress[] internetAddress = new InternetAddress[toMailId.size()];
		int i = 0;
		for (String tomail : toMailId) {
			internetAddress[i] = new InternetAddress(tomail);
			i++;
		}
		getMailSession = Session.getDefaultInstance(mailServerProperties, null);
		generateMailMessage = new MimeMessage(getMailSession);
		generateMailMessage.addRecipients(Message.RecipientType.TO, internetAddress);

		if (emailTemplate.getSubject() != null) {
			generateMailMessage.setSubject(emailTemplate.getSubject());
		}

		generateMailMessage.setContent(generateMailMessage, "text/html; charset=utf-8");

		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setText(emailTemplate.getBody());
		multipart.addBodyPart(messageBodyPart);

		generateMailMessage.setContent(multipart);
		Transport transport = getMailSession.getTransport("smtp");

		// if you have 2FA enabled then provide App Specific Password
		// transport.connect(applicationProperties.getSmtphostName(),
		// applicationProperties.getCicdUserName(),
		// applicationProperties.getCicdPassWord());
		// transport.sendMessage(generateMailMessage,
		// generateMailMessage.getAllRecipients());
		transport.close();
	}
}
