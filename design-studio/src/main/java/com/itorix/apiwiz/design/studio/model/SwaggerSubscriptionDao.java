package com.itorix.apiwiz.design.studio.model;

import com.itorix.apiwiz.common.model.slack.PostMessage;
import com.itorix.apiwiz.common.model.slack.SlackChannel;
import com.itorix.apiwiz.common.model.slack.SlackWorkspace;
import com.itorix.apiwiz.common.model.slack.notificationScope;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.mail.EmailTemplate;
import com.itorix.apiwiz.common.util.mail.MailUtil;
import com.itorix.apiwiz.common.util.slack.SlackUtil;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
@Slf4j
@Component

public class SwaggerSubscriptionDao {

	@Autowired
	private SlackUtil slackUtil;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	BaseRepository baseRepository;

	@Autowired
	private MailUtil mailUtil;

	@Autowired
	ApplicationProperties applicationProperties;


	/**
	 * Adds subscriber to swagger
	 *
	 * @param swaggerId
	 * @param swaggerName
	 * @param oas
	 * @param subscriber
	 */

	public void swaggerSubscribe(String swaggerId, String swaggerName, String oas, Subscriber subscriber) {
		SwaggerSubscription swagger = baseRepository.findOne("swaggerId", swaggerId, SwaggerSubscription.class);
		if (swagger == null) {
			log.debug("Subscribing to swagger");
			swagger = new SwaggerSubscription();
			swagger.setSwaggerId(swaggerId);
			swagger.setSwaggerName(swaggerName);
			swagger.setOas(oas);
			swagger.setSubscribers(subscriber);
			baseRepository.save(swagger);
		} else {
			swagger.setSubscribers(subscriber);
			baseRepository.save(swagger);
		}
	}


	/**
	 * Unsubscribes user from swagger
	 *
	 * @param swaggerId
	 * @param emailId
	 */

	public void swaggerUnsubscribe(String swaggerId, String emailId) {
		SwaggerSubscription swagger = baseRepository.findOne("swaggerId", swaggerId, SwaggerSubscription.class);
		if (swagger != null) {
			log.debug("Unsubscribing to swagger");
			swagger.removeSubscribers(emailId);
			baseRepository.save(swagger);
		}
	}

	/**
	 * returns all the subscribers of a swagger
	 *
	 * @param swaggerId
	 * @return
	 */
	public Set<Subscriber> swaggerSubscribers(String swaggerId) {
		SwaggerSubscription swagger = baseRepository.findOne("swaggerId", swaggerId, SwaggerSubscription.class);
		if (swagger != null) {
			return swagger.getSubscribers();
		} else {
			return new HashSet<Subscriber>();
		}

	}


	/**
	 * Notifies subscribers about the changes
	 *
	 * @param swaggerId
	 * @param oas
	 * @param summary
	 * @param text
	 * @throws MessagingException
	 */

	public void swaggerNotification(String swaggerId, String oas, String summary, String text) throws MessagingException {
		SwaggerSubscription swaggerSubscription = baseRepository.findOne("swaggerId", swaggerId, SwaggerSubscription.class);
		if (swaggerSubscription	!= null) {

			Set<Subscriber> subscribers = swaggerSubscription.getSubscribers();
			if (!subscribers.isEmpty()) {
				Path path = Paths.get(applicationProperties.getTempDir() + "changeLog.md");
				try {
					File file = new File(path.toString());
					file.createNewFile();
					FileWriter fw = new FileWriter(file.getAbsoluteFile());
		            BufferedWriter bw = new BufferedWriter(fw);
		
		            // Write in file
		            bw.write(text);
		
		            // Close connection
		            bw.close();
		            
					String swaggerName = swaggerSubscription.getSwaggerName();
					String subject = MessageFormat.format(applicationProperties.getSwaggerSubscriptionSubject(),
							swaggerName);
					for (Subscriber subscriber : subscribers) {
						String body = MessageFormat.format(
								applicationProperties.getSwaggerSubscriptionMailBody(),
								subscriber.getName()!=null ? subscriber.getName() : "", swaggerName, summary);
						EmailTemplate emailTemplate = new EmailTemplate();
						emailTemplate.setToMailId(Arrays.asList(subscriber.getEmailId()));
						emailTemplate.setSubject(subject);
						emailTemplate.setBody(body);
						mailUtil.sendEmailWtithAttachment(emailTemplate, path.toString(),
								swaggerSubscription.getSwaggerName() + swaggerSubscription.getOas()
										.replace(".", "_") + ".md");
					}
					try {
						// Refer slackUtil to send slack Notif here
						log.info("Sending Slack notification:{}",mongoTemplate.getDb().getName());
						List<SlackWorkspace> slackWorkspaces = mongoTemplate.findAll(SlackWorkspace.class);
						SlackWorkspace slackWorkspace=slackWorkspaces.get(0);
						if (slackWorkspace != null) {
							String token = slackWorkspace.getToken();
							List<SlackChannel> channels = slackWorkspace.getChannelList();
							for (SlackChannel i : channels) {
								if (i.getScopeSet().contains(notificationScope.NotificationScope.DESIGN_STUDIO)) {
									PostMessage at = new PostMessage();
									at.setFileName(String.format("%s-changelog.md", swaggerName));
									at.setInitialComment("Swagger ChangeLog Notification");
									at.setFile(file);
									slackUtil.sendMessage(at, i.getChannelName(), token);
								}
							}
						}
					} catch (Exception e) {
						log.warn("Failed to Send Slack Notification", e);
					}
					file.delete();
				}

				catch (IOException e) {
					log.error("Exception occurred", e);
				}

			}
		}
	}
	/**
	 * returns if user is a subscriber of the swagger
	 *
	 * @param swaggerId
	 * @param emailId
	 * @return
	 */
	public IsSubscribedUser checkSubscriber(String swaggerId, String emailId) {
		IsSubscribedUser isSubscribedUser = new IsSubscribedUser();
		Query query = Query.query(Criteria.where("swaggerId").is(swaggerId).andOperator(Criteria.where("subscribers").elemMatch(Criteria.where("emailId").is(emailId))));
		List<SwaggerSubscription> swaggers = baseRepository.find(query, SwaggerSubscription.class);
		if (swaggers.size() > 0) {
			isSubscribedUser.setIsSubscribed(true);

		}else {
			isSubscribedUser.setIsSubscribed(false);
		}
		return isSubscribedUser;
	}
}
