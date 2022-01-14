package com.itorix.apiwiz.notification.agent.util;

import com.itorix.apiwiz.notification.agent.model.EmailTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

@Component
@Slf4j
public class MailUtil {

    @Autowired
    private EmailHelper emailHelper;

    public void sendEmail(EmailTemplate emailTemplate) {
        try {
            log.debug("Initiating mail with subject {} to {}", emailTemplate.getSubject(), emailTemplate.getToMailId());
            JavaMailSender javaMailSender = emailHelper.getJavaMailSender();
            MimeMessage msg = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);
            String[] internetAddress = new String[emailTemplate.getToMailId().size()];
            int i = 0;
            for (String toMail : emailTemplate.getToMailId()) {
                internetAddress[i] = toMail;
                i++;
            }
            helper.setTo(internetAddress);
            helper.setFrom(emailHelper.getFromAddress());
            helper.setSubject(emailTemplate.getSubject());
            helper.setText(emailTemplate.getBody(), true);
            javaMailSender.send(msg);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
