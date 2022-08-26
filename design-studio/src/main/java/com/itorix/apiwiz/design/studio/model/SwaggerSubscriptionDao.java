package com.itorix.apiwiz.design.studio.model;

import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.common.model.SearchItem;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.integrations.s3.S3Integration;
import com.itorix.apiwiz.common.model.projectmanagement.Organization;
import com.itorix.apiwiz.common.model.projectmanagement.ProjectProxyResponse;
import com.itorix.apiwiz.common.model.proxystudio.ProxyPortfolio;
import com.itorix.apiwiz.common.model.proxystudio.Scm;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.artifatory.JfrogUtilImpl;
import com.itorix.apiwiz.common.util.mail.EmailTemplate;
import com.itorix.apiwiz.common.util.mail.MailUtil;
import com.itorix.apiwiz.common.util.s3.S3Connection;
import com.itorix.apiwiz.common.util.s3.S3Utils;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.model.Pagination;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.itorix.apiwiz.design.studio.model.SwaggerSubscription;
import com.itorix.apiwiz.design.studio.swaggerdiff.dao.SwaggerDiffService;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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

@Component

public class SwaggerSubscriptionDao {
	
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
			swagger = new SwaggerSubscription();
			swagger.setSwaggerId(swaggerId);
			swagger.setSwaggerName(swaggerName);
			swagger.setOas(oas);
			swagger.setSubscribers(subscriber);
			baseRepository.save(swagger);
		}
		else {
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
		}else {
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
				Path path = Paths.get(applicationProperties.getTempDir()+"changeLog.md");
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
					String subject = MessageFormat.format(applicationProperties.getSwaggerSubscriptionSubject(), swaggerName);
					for (Subscriber subscriber: subscribers) {
						String body = MessageFormat.format(applicationProperties.getSwaggerSubscriptionMailBody(), subscriber.getName(), swaggerName, summary);
						EmailTemplate emailTemplate = new EmailTemplate();
						emailTemplate.setToMailId(Arrays.asList(subscriber.getEmailId()));
						emailTemplate.setSubject(subject);
						emailTemplate.setBody(body);
						mailUtil.sendEmailWtithAttachment(emailTemplate, path.toString(),"changeLog.md");
					}
					file.delete();
				}
					
				catch(IOException e) {
					e.printStackTrace();
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
