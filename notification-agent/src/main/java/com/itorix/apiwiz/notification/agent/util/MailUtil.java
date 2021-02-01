package com.itorix.apiwiz.notification.agent.util;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.notification.agent.model.EmailTemplate;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MailUtil {

	static Properties mailServerProperties;
	static Session getMailSession;
	static MimeMessage generateMailMessage;

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
			helper.setSubject(emailTemplate.getSubject());
			helper.setText(emailTemplate.getBody(), true);
			javaMailSender.send(msg);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
