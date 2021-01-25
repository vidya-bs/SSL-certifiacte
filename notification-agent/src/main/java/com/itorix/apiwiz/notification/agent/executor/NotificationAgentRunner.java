package com.itorix.apiwiz.notification.agent.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import com.itorix.apiwiz.notification.agent.dao.NotificationAgentExecutorSQLDao;
import com.itorix.apiwiz.notification.agent.db.NotificationExecutorEntity;
import com.itorix.apiwiz.notification.agent.logging.LoggerService;
import com.itorix.apiwiz.notification.agent.model.EmailTemplate;
import com.itorix.apiwiz.notification.agent.model.ExecutionContext;
import com.itorix.apiwiz.notification.agent.util.MailUtil;

@SuppressWarnings("unused")
@Component
public class NotificationAgentRunner {

	private final static Logger logger = LoggerFactory.getLogger(NotificationAgentRunner.class);

	private static final MustacheFactory mf = new DefaultMustacheFactory();

	@Autowired
	private NotificationAgentExecutorSQLDao sqlDao;

	@Autowired
	LoggerService loggerService;

	@Autowired
	MailUtil mailUtil;

	ObjectMapper mapper = new ObjectMapper();

	public enum API {
		GET, PUT, POST, DELETE, OPTIONS, PATCH;
	}

	public void run(ExecutionContext context) {
		loggerService.logServiceRequest();
		try {
			String identity = context.getNotificationExecutorEntity().getContent();
			EmailTemplate template = mapper.readValue(identity, EmailTemplate.class);
			mailUtil.sendEmail(template);
			sqlDao.updateField(context.getNotificationExecutorEntity().getId(), "status", NotificationExecutorEntity.STATUSES.COMPLETED.getValue());
		} catch (Exception ex) {
			logger.error("error when executing monitor requests", ex);
			sqlDao.updateField(context.getNotificationExecutorEntity().getId(), "status", NotificationExecutorEntity.STATUSES.ERROR.getValue());
		} finally {
			loggerService.logServiceResponse();
		}
	}
}