package com.itorix.apiwiz.marketing.dao;

import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.crypto.NoSuchPaddingException;

import com.itorix.apiwiz.common.model.apigee.StaticFields;
import com.itorix.apiwiz.marketing.contactus.model.*;
import com.itorix.apiwiz.marketing.db.NotificationExecutorEntity;
import com.itorix.apiwiz.marketing.db.NotificationExecutorSql;
import com.itorix.apiwiz.marketing.events.model.UserEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.common.util.mail.EmailTemplate;
import com.itorix.apiwiz.marketing.contactus.model.RequestModel.Type;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ContactUsDao {

	@Autowired
	private ApplicationProperties applicationProperties;

	@Qualifier("masterMongoTemplate")
	@Autowired
	private MongoTemplate masterMongoTemplate;

	@Autowired
	private NotificationExecutorSql sqlDao;

	@Autowired
	private RestTemplate restTemplate;

	private static final String API_KEY_NAME = "x-apikey";
	private static final String NOTIFICATION_AGENT_NOTIFY = "/v1/notification/";

	@Value("${itorix.notification.agent:null}")
	private String notificationAgentPath;

	@Value("${itorix.notification.agent.contextPath:null}")
	private String notificationContextPath;

	@Value("${itorix.notification.bookDemo.email.body:null}")
	private String bookDemoEmailBody;

	@Value("${itorix.notification.contactSales.email.body:null}")
	private String contactSalesEmailBody;

	@Value("${itorix.notification.requestAudit.email.body:null}")
	private String requestAuditReportEmailBody;

	RSAEncryption rsaEncryption;

	@PostConstruct
	private void setRSAKey() {
		try {
			rsaEncryption = new RSAEncryption();
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			log.error("error creating rsaEncryption", e);
		}
	}

	public void invokeNotificationAgent(ContactUsNotification contactUsNotification) {
		try {
			NotificatoinEvent notificatoinEvent = null;
			if (contactUsNotification.getEmailContent().getEvent() != null)
				notificatoinEvent = getNotificationConfig(contactUsNotification.getEmailContent().getEvent());
			if (notificatoinEvent != null) {
				RequestModel requestModel = new RequestModel();
				EmailTemplate emailTemplate = new EmailTemplate();
				EmailBody emailBody = contactUsNotification.getEmailContent().getBody();
				if(StringUtils.equalsIgnoreCase(contactUsNotification.getEmailContent().getEvent(),StaticFields.EMAIL_SUBJECT_CONTACT_SALES)){
					emailTemplate.setBody(MessageFormat.format(contactSalesEmailBody,notificatoinEvent.getSubject(), emailBody.getName(),emailBody.getEmail(),emailBody.getCompany(),emailBody.getJobTitle(),emailBody.getMessage()));
				}else if(StringUtils.equalsIgnoreCase(contactUsNotification.getEmailContent().getEvent(),StaticFields.EMAIL_SUBJECT_REQUEST_A_DEMO)){
					emailTemplate.setBody(MessageFormat.format(bookDemoEmailBody,notificatoinEvent.getSubject(), emailBody.getName(),emailBody.getEmail(),emailBody.getCompany(),emailBody.getJobTitle(),emailBody.getMessage()));
				}else if(StringUtils.equalsIgnoreCase(contactUsNotification.getEmailContent().getEvent(),StaticFields.REQUEST_AUDIT_REPORT_TAG)){
					emailTemplate.setBody(MessageFormat.format(requestAuditReportEmailBody,notificatoinEvent.getSubject(), emailBody.getName(),emailBody.getEmail(),emailBody.getCompany(),emailBody.getJobTitle(),emailBody.getMessage()));
				}else{
					emailTemplate.setBody(contactUsNotification.getEmailContent().getBody().toHTML());
				}
				emailTemplate.setSubject(notificatoinEvent.getSubject());
				emailTemplate.setToMailId(notificatoinEvent.getEmail());
				requestModel.setEmailContent(emailTemplate);
				requestModel.setType(Type.email);

				String notificationExecutionEventId = createNotificationEvent(requestModel);
				sqlDao.insertIntoNotificationEntity(null,
								notificationExecutionEventId, NotificationExecutorEntity.STATUSES.SCHEDULED.getValue(), null);
				log.error("inserted notification event into the postgres - {}", notificationExecutionEventId);
			}
		} catch (Exception e) {
			log.error("error returned from notification agent", e);
		}
	}

	public void updateNotificationConfigs(List<NotificatoinEvent> notificatoinEvents) {
		for (NotificatoinEvent notificatoinEvent : notificatoinEvents) {
			masterMongoTemplate.save(notificatoinEvent);
		}
	}

	public List<NotificatoinEvent> getNotificationConfigs() {
		Query query = new Query();
		query.fields().include("name");
		List<NotificatoinEvent> notificatoinEvents = masterMongoTemplate.find(query, NotificatoinEvent.class);
		return notificatoinEvents;
	}

	public NotificatoinEvent getNotificationConfig(String name) {
		NotificatoinEvent notificatoinEvent = masterMongoTemplate.findById(name, NotificatoinEvent.class);
		return notificatoinEvent;
	}

	public String createNotificationEvent(RequestModel requestModel) {
		NotificationExecutionEvent notificationExecutionEvent = new NotificationExecutionEvent();
		notificationExecutionEvent.setRequestModel(requestModel);
		notificationExecutionEvent.setCts(System.currentTimeMillis());
		notificationExecutionEvent.setStatus(NotificationExecutionEvent.STATUSES.SCHEDULED.getValue());
		masterMongoTemplate.save(notificationExecutionEvent);
		return notificationExecutionEvent.getId();
	}

	public void saveUserEvent(UserEvent userEvent){
		if(!masterMongoTemplate.exists(new Query(Criteria.where("email").is(userEvent.getEmail()).and("event").is(userEvent.getEvent())), UserEvent.class))
			masterMongoTemplate.save(userEvent);
	}
}
