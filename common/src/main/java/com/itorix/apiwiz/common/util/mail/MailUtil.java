package com.itorix.apiwiz.common.util.mail;

import com.itorix.apiwiz.common.properties.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class MailUtil {

	private static final Logger logger = LoggerFactory.getLogger(MailUtil.class);

	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired
	private EmailHelper emailHelper;

	public void sendEmail(EmailTemplate emailTemplate) throws MessagingException {
		try {
			logger.debug("Initiating mail with subject {} to {} ", emailTemplate.getSubject(),
					emailTemplate.getToMailId());
			JavaMailSender javaMailSender = emailHelper.getJavaMailSender();
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
			helper.setFrom(emailHelper.getFromAddress());
			helper.setText(emailTemplate.getBody(), true);
			logger.info("Sending mail to with subject {} ", emailTemplate.getSubject());
			javaMailSender.send(msg);
			logger.info("Mail with subject {} sent successfully", emailTemplate.getSubject());
		} catch (MessagingException e) {
			logger.error(e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void sendEmailWtithAttachment(EmailTemplate emailTemplate, String path, String attachment)
			throws MessagingException {
		try {
			logger.debug("Initiating mail with subject {} to {} ", emailTemplate.getSubject(),
					emailTemplate.getToMailId());
			JavaMailSender javaMailSender = emailHelper.getJavaMailSender();
			MimeMessage msg = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(msg, true);

			FileSystemResource file = new FileSystemResource(new File(path));
			helper.addAttachment(attachment, file);

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
			helper.setFrom(emailHelper.getFromAddress());
			helper.setText(emailTemplate.getBody(), true);
			javaMailSender.send(msg);
		} catch (MessagingException e) {

			logger.error(e.getMessage(), e);
			throw e;
		} catch (Exception e) {

			logger.error(e.getMessage(), e);
		}
	}

}
