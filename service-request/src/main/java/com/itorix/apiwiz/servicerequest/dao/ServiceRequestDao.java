package com.itorix.apiwiz.servicerequest.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.itorix.apiwiz.common.model.CountryMetaData;
import com.itorix.apiwiz.common.model.MetaData;
import com.itorix.apiwiz.common.model.apigee.ApigeeConfigurationVO;
import com.itorix.apiwiz.common.model.apigee.ApigeeServiceUser;
import com.itorix.apiwiz.common.model.apigee.StaticFields;
import com.itorix.apiwiz.common.model.apigeeX.ApigeeXConfigurationVO;
import com.itorix.apiwiz.common.model.configmanagement.CompanyConfig;
import com.itorix.apiwiz.common.model.configmanagement.CompanyConfig.Attribute;
import com.itorix.apiwiz.common.model.configmanagement.DeveloperCategoryConfig;
import com.itorix.apiwiz.common.model.configmanagement.ProductAttributes;
import com.itorix.apiwiz.common.model.configmanagement.ProductBundleConfig;
import com.itorix.apiwiz.common.model.configmanagement.ProductBundleConfig.Status;
import com.itorix.apiwiz.common.model.configmanagement.WebhookConfig;
import com.itorix.apiwiz.common.model.monetization.*;
import com.itorix.apiwiz.common.model.monetization.ResponseVariableLocation.Location;
import com.itorix.apiwiz.common.model.monetization.apigeepayloads.ApigeeRatePlan;
import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.servicerequest.model.JSONPayload;
import com.itorix.apiwiz.servicerequest.model.MonetizationConfigComments;
import com.itorix.apiwiz.servicerequest.model.ResponseObject;
import com.itorix.apiwiz.servicerequest.model.ResponseObject.Extraction;
import com.itorix.apiwiz.servicerequest.model.ResponseObject.Header;
import com.itorix.apiwiz.servicerequest.model.ResponseObject.Pattern;
import com.itorix.apiwiz.servicerequest.model.ResponseObject.Source;
import com.itorix.apiwiz.servicerequest.model.TransactionRecordingPoliciesObject;
import com.itorix.apiwiz.servicerequest.model.TransactionRecordingPoliciesObject.Policies;
import com.itorix.apiwiz.servicerequest.model.XMLPayload;
import com.itorix.apiwiz.servicerequest.model.XMLPayload.Variable;
import com.itorix.apiwiz.servicerequest.model.XMLPayload.XPath;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.*;

import java.util.stream.Collectors;
import javax.mail.MessagingException;

import com.itorix.apiwiz.common.model.slack.*;
import com.itorix.apiwiz.common.util.slack.SlackUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.common.model.configmanagement.CacheConfig;
import com.itorix.apiwiz.common.model.configmanagement.KVMConfig;
import com.itorix.apiwiz.common.model.configmanagement.ProductConfig;
import com.itorix.apiwiz.common.model.configmanagement.TargetConfig;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.Date.DateUtil;
import com.itorix.apiwiz.common.util.mail.EmailTemplate;
import com.itorix.apiwiz.common.util.mail.MailUtil;
import com.itorix.apiwiz.configmanagement.dao.ApigeeXConfigDao;
import com.itorix.apiwiz.configmanagement.dao.ConfigManagementDao;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.Pagination;
import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContextHolder;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.itorix.apiwiz.servicerequest.model.ServiceRequest;
import com.itorix.apiwiz.servicerequest.model.ServiceRequestComments;
import com.itorix.apiwiz.servicerequest.model.ServiceRequestHistoryResponse;
import com.itorix.apiwiz.servicerequest.model.ServiceRequestTypes;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.UpdateResult;
@Component
@Slf4j
public class ServiceRequestDao {

	@Autowired
	SlackUtil slackUtil;

	@Autowired
	ApigeeUtil apigeeUtil;

	@Qualifier("masterMongoTemplate")
	@Autowired
	private MongoTemplate masterMongoTemplate;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private IdentityManagementDao identityManagementDao;
	@Autowired
	private MailUtil mailUtil;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private ConfigManagementDao configManagementDao;

	@Autowired
	private ApigeeXConfigDao apigeeXConfigDao;

	@Autowired
	private BaseRepository baseRepository;

	@SuppressWarnings({"unchecked", "unused"})
	public ServiceRequest createServiceRequest(ServiceRequest config) throws ItorixException {
		try {
			if(isApproved(config)){
				throw new ItorixException(StaticFields.ERR_MSG_APIGEE_1009, StaticFields.ERR_CODE_APIGEE_1009);
			}
			boolean isAnyRequestPending = false;

			if (config != null && StringUtils.isNotBlank(config.getType()) && StringUtils.isNotBlank(
					config.getOrg())
					&& (StringUtils.isNotBlank(config.getEnv()) || "Product".equalsIgnoreCase(
					config.getType()))
					&& StringUtils.isNotBlank(config.getName())) {
				List<ServiceRequest> serviceRequest = (List<ServiceRequest>) getservicerequest(config);
				if (ServiceRequestTypes.isServiceRequestTypeValid(config.getType())
						&& serviceRequest.size() == 0) {
					config = mongoTemplate.save(config);
					sendEmailTo(config);
					return config;
				} else {
					if (serviceRequest.size() > 0) {
						for (ServiceRequest req : serviceRequest) {
							if (req.getStatus().equalsIgnoreCase("Review")) {
								isAnyRequestPending = true;
							}
						}
						if (!isAnyRequestPending) {
							Query query = null;
							if (StringUtils.isNotBlank(config.getType()) && StringUtils.isNotBlank(
									config.getOrg())
									&& StringUtils.isNotBlank(config.getEnv())
									&& StringUtils.isNotBlank(config.getName())) {
								query = new Query(Criteria.where("org").is(config.getOrg()).and("env")
										.is(config.getEnv()).and("name").is(config.getName()).and("type")
										.is(config.getType()).and("isSaaS").is(config.getIsSaaS()));
							} else if (StringUtils.isNotBlank(config.getType())
									&& StringUtils.isNotBlank(config.getOrg())
									&& StringUtils.isNotBlank(config.getName())) {
								query = new Query(
										Criteria.where("org").is(config.getOrg()).and("name").is(config.getName())
												.and("type").is(config.getType()).and("isSaaS").is(config.getIsSaaS()));
							}

							Update update = new Update();
							update.set("activeFlag", Boolean.FALSE);
							UpdateResult result = mongoTemplate.updateMulti(query, update, ServiceRequest.class);
							config.setActiveFlag(Boolean.TRUE);
							config = mongoTemplate.save(config);
							sendEmailTo(config);
							return config;
						} else {
							throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1026"),
									"Configuration-1026");
						}
					}
				}
			} else {
				throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1028"),
						"Configuration-1028");
			}
		} catch (ItorixException ex) {
			throw ex;
		} catch (MessagingException ex) {
			log.error("Exception while sending email", ex.getMessage());
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
		return null;
	}

	public Object getservicerequest(ServiceRequest config) throws ItorixException {
		try {
			Query query = null;
			if (config != null) {
				if (StringUtils.isNotBlank(config.getType()) && StringUtils.isNotBlank(config.getOrg())
						&& StringUtils.isNotBlank(config.getEnv()) && StringUtils.isNotBlank(config.getName())) {
					query = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv())
							.and("name").is(config.getName()).and("type").is(config.getType()).and("isSaaS")
							.is(config.getIsSaaS())).with(Sort.by(Direction.DESC, "mts"));
				} else if (StringUtils.isNotBlank(config.getType()) && StringUtils.isNotBlank(config.getOrg())
						&& StringUtils.isNotBlank(config.getName())) {
					query = new Query(Criteria.where("org").is(config.getOrg()).and("name").is(config.getName())
							.and("type").is(config.getType()).and("isSaaS").is(config.getIsSaaS()))
									.with(Sort.by(Direction.DESC, "mts"));
				} else if (StringUtils.isNotBlank(config.getStatus()) && StringUtils.isNotBlank(config.getType())) {
					query = new Query(Criteria.where("status").is(config.getStatus()).and("type").is(config.getType())
							.and("isSaaS").is(config.getIsSaaS())).with(Sort.by(Direction.DESC, "mts"));
				}
				return mongoTemplate.find(query, ServiceRequest.class);
			} else {
				return mongoTemplate.findAll(ServiceRequest.class);
			}
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public Object getAllActiveServiceRequests(ServiceRequest config) throws ItorixException {
		try {
			Query query = null;
			if (config != null) {
				log.debug("Fetching all active service requests");
				if (StringUtils.isNotBlank(config.getType()) && StringUtils.isNotBlank(config.getOrg())
						&& StringUtils.isNotBlank(config.getEnv()) && StringUtils.isNotBlank(config.getName())) {
					query = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv())
							.and("name").is(config.getName()).and("type").is(config.getType()).and("isSaaS")
							.is(config.getIsSaaS()).and("activeFlag").is(Boolean.TRUE));
				} else if (StringUtils.isNotBlank(config.getType()) && StringUtils.isNotBlank(config.getOrg())
						&& StringUtils.isNotBlank(config.getName())) {
					query = new Query(Criteria.where("org").is(config.getOrg()).and("name").is(config.getName())
							.and("type").is(config.getType()).and("isSaaS").is(config.getIsSaaS()).and("activeFlag")
							.is(Boolean.TRUE));
				} else if (StringUtils.isNotBlank(config.getStatus()) && StringUtils.isNotBlank(config.getType())) {
					query = new Query(Criteria.where("status").is(config.getStatus()).and("type").is(config.getType())
							.and("isSaaS").is(config.getIsSaaS()).and("activeFlag").is(Boolean.TRUE));
				}
				return mongoTemplate.find(query, ServiceRequest.class);
			} else {
				query = new Query(Criteria.where("activeFlag").is(Boolean.TRUE));
				return mongoTemplate.find(query, ServiceRequest.class);
			}
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public Object getServiceRequests(ServiceRequest config, int offset, int pageSize,String timerange) throws ItorixException {
		try {
			Query query=new Query();
			Query countquery = new Query();
			if(timerange!=null){
				SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
				String[] timeRanges = timerange.split("~");
				Date startDate=new Date(format.parse(timeRanges[0]).getTime());
				Date endDate=DateUtil.getEndOfDay(new Date(format.parse(timeRanges[1]).getTime()));
				query.addCriteria(Criteria.where("modifiedDate").gte(startDate).lte(endDate));
				countquery.addCriteria(Criteria.where("modifiedDate").gte(startDate).lte(endDate));
			}

			ServiceRequestHistoryResponse response = new ServiceRequestHistoryResponse();
			countquery.addCriteria(Criteria.where("activeFlag").is(Boolean.TRUE));
			query.addCriteria(Criteria.where("activeFlag").is(Boolean.TRUE))
					.with(Sort.by(Direction.DESC, "modifiedDate")).skip(offset > 0 ? ((offset - 1) * pageSize) : 0)
					.limit(pageSize);

			if (config != null && StringUtils.isNotBlank(config.getType())) {
				query.addCriteria(Criteria.where("type").is(config.getType()));
				countquery.addCriteria(Criteria.where("type").is(config.getType()));
			}
			if (config != null && StringUtils.isNotBlank(config.getStatus())) {
				query.addCriteria(Criteria.where("status").is(config.getStatus()));
				countquery.addCriteria(Criteria.where("status").is(config.getStatus()));
			}
			if (config != null && StringUtils.isNotBlank(config.getName())) {
				query.addCriteria(Criteria.where("name").is(config.getName()));
				countquery.addCriteria(Criteria.where("name").is(config.getName()));
			}
			response.setData(mongoTemplate.find(query, ServiceRequest.class));

			Pagination pagination = new Pagination();
			Long counter;
			counter = mongoTemplate.count(countquery, ServiceRequest.class);
			pagination.setOffset(offset);
			pagination.setTotal(counter);
			pagination.setPageSize(pageSize);
			response.setPagination(pagination);
			return response;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public Object getAllServiceRequests(String type,String status,String orgName,String name) throws ItorixException {
		try {
			Query query=new Query();

			query.addCriteria(Criteria.where("activeFlag").is(Boolean.TRUE))
					.with(Sort.by(Direction.DESC, "modifiedDate"));

			if (StringUtils.isNotBlank(type)) {
				query.addCriteria(Criteria.where("type").is(type));
			}
			if (StringUtils.isNotBlank(status)) {
				query.addCriteria(Criteria.where("status").is(status));
			}
			if (StringUtils.isNotBlank(orgName)) {
				query.addCriteria(Criteria.where("org").is(orgName));
			}
			if(StringUtils.isNotBlank(name)) {
				query.addCriteria(Criteria.where("name").is(name));
			}
			return mongoTemplate.find(query, ServiceRequest.class);
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public Object getAllMonetizationConfigs(String type,String status,String orgName) throws ItorixException {
		try {
			Query query=new Query();
			query.addCriteria(Criteria.where("activeFlag").is(Boolean.TRUE))
					.with(Sort.by(Direction.DESC, "modifiedDate"));
			if (StringUtils.isNotBlank(status)) {
				query.addCriteria(Criteria.where("status").is(status));
			}
			if (StringUtils.isNotBlank(orgName)) {
				query.addCriteria(Criteria.where("organization").is(orgName));
			}
			if(type.equalsIgnoreCase("ProductBundle")){
				return mongoTemplate.find(query, ProductBundle.class);
			}else if(type.equalsIgnoreCase("Company")){
				return mongoTemplate.find(query, Company.class);
			}else if(type.equalsIgnoreCase("RatePlan")){
				return mongoTemplate.find(query, RatePlan.class);
			}else if(type.equalsIgnoreCase("DeveloperCategory")){
				return mongoTemplate.find(query, DeveloperCategory.class);
			}else if(type.equalsIgnoreCase("Webhook")){
				return mongoTemplate.find(query, Webhook.class);
			}
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
		return new ArrayList<>();
	}

	private void sendEmailTo(ServiceRequest config) throws MessagingException {
		try {
			EmailTemplate emailTemplate = new EmailTemplate();
			ArrayList<String> toMailId = new ArrayList<String>();
			String body = getMailBody(config);
			if (config.getStatus().equalsIgnoreCase("Review")) {
				List<String> allUsers = new ArrayList<String>();
				allUsers = identityManagementDao.getAllUsersWithRoleDevOPS();
				toMailId.addAll(allUsers);
			} else if (config.getStatus().equalsIgnoreCase("Approved")) {
				toMailId.add(config.getCreatedUserEmailId());
				if (config.getModifiedUserEmailId() != null) {
					toMailId.add(config.getModifiedUserEmailId());
				}
			} else {
				toMailId.add(config.getCreatedUserEmailId());
			}
			emailTemplate.setToMailId(toMailId);
			emailTemplate.setBody(body);
			emailTemplate.setSubject(
					MessageFormat.format(applicationProperties.getServiceRequestSubject(), config.getName()));
			mailUtil.sendEmail(emailTemplate);
		} catch (Exception e) {

			log.error("Exception occurred", e);
		}
		try {
			UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
			User user = masterMongoTemplate.findById(userSessionToken.getUserId(), User.class);
			String userName = user.getFirstName() + " " + user.getLastName();
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			String formatedDate = dateFormat.format(date);

			log.info("Sending slack message");
			List<SlackWorkspace> slackWorkspaces = mongoTemplate.findAll(SlackWorkspace.class);
			if (slackWorkspaces.isEmpty()) return;
			SlackWorkspace slackWorkspace = slackWorkspaces.get(0);
			String token = slackWorkspace.getToken();
			List<SlackChannel> channels = slackWorkspace.getChannelList();
			for (SlackChannel i : channels) {
				if (i.getScopeSet().contains(NotificationScope.Scopes.Gateway)) {
					PostMessage postMessage = new PostMessage();
					ArrayList<Attachment> attachmentsToSend = new ArrayList<>();
					Attachment attachment = new Attachment();
					attachment.setMrkdwn_in("text");
					attachment.setTitle_link("https://www.apiwiz.io/");
					attachment.setColor("#0000FF");
					attachment.setPretext("GATEWAY");
					attachment.setText ("Name: "+ config.getName()+"\n"+"Date: "+formatedDate+"\n"+
							"Request Count: "+getRequestCount()+"\n"+"Type: "+ config.getType()
							+"\n"+"Count: "+getCountbyType(config.getType())+"\n"+"Status: "+config.getStatus()
							+"\n"+ "Status Count: "+getCountbyStatus(config.getStatus())+"\n"+
							"UserName:"+userName+"\n"+
							"Count by UserId: "+getCountbyuserId(userName));
					attachmentsToSend.add(attachment);
					postMessage.setAttachments(attachmentsToSend);
					slackUtil.sendMessage(postMessage, i.getChannelName(), token);
				}
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean updateServiceRequestInApigee(String id,ServiceRequest config,User user)
			throws ItorixException, MessagingException, JsonProcessingException {
		Query query = new Query(Criteria.where("_id").is(id));
		ServiceRequest serviceRequests = mongoTemplate.findOne(query,ServiceRequest.class);
		if(serviceRequests.getStatus()!=null && serviceRequests.getStatus().equals("Approved")){
			throw new ItorixException(StaticFields.ERR_MSG_APIGEE_1008, StaticFields.ERR_CODE_APIGEE_1008);
		}
		Update update = new Update();
		serviceRequests.setTransactionRecordingPolicy(config.getTransactionRecordingPolicy());
		update.set("transactionRecordingPolicy", config.getTransactionRecordingPolicy());
		UpdateResult result = mongoTemplate.updateMulti(query, update, ServiceRequest.class);
		updateServiceRequestStatus(serviceRequests,user);
		return true;
	}

	private String getMailBody(ServiceRequest config) {
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User user = masterMongoTemplate.findById(userSessionToken.getUserId(), User.class);
		String userName = user.getFirstName() + " " + user.getLastName();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String formatedDate = dateFormat.format(date);
		return MessageFormat.format(applicationProperties.getServiceRequestReviewBody(), formatedDate, config.getName(),
				getRequestCount(), config.getType(), getCountbyType(config.getType()), config.getStatus(),
				getCountbyStatus(config.getStatus()), userName, getCountbyuserId(userName));
	}

	private long getRequestCount() {
		try {
			Query query = new Query(Criteria.where("activeFlag").is(true));
			return mongoTemplate.getCollection(mongoTemplate.getCollectionName(ServiceRequest.class))
					.countDocuments(query.getQueryObject());
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return 0;
	}

	private long getCountbyType(String type) {
		try {
			Query query = new Query(Criteria.where("activeFlag").is(true).and("type").is(type));
			return mongoTemplate.getCollection(mongoTemplate.getCollectionName(ServiceRequest.class))
					.countDocuments(query.getQueryObject());
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return 0;
	}

	private long getCountbyStatus(String status) {
		try {
			Query query = new Query(Criteria.where("activeFlag").is(true).and("status").is(status));
			return mongoTemplate.getCollection(mongoTemplate.getCollectionName(ServiceRequest.class))
					.countDocuments(query.getQueryObject());
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return 0;
	}

	private long getCountbyuserId(String userId) {
		try {
			Query query = new Query(Criteria.where("activeFlag").is(true).and("createdUser").is(userId));
			return mongoTemplate.getCollection(mongoTemplate.getCollectionName(ServiceRequest.class))
					.countDocuments(query.getQueryObject());
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return 0;
	}
	public boolean isApproved(ServiceRequest serviceRequest) throws ItorixException{
		Query query = new Query(Criteria.where(StaticFields.ORG_NAME).is(serviceRequest.getOrg()).and(StaticFields.ENV_NAME).is(serviceRequest.getEnv())
				.and(StaticFields.NAME).is(serviceRequest.getName()).and(StaticFields.TYPE_NAME).is(serviceRequest.getType()).and(StaticFields.ACTIVE_FLAG).is(Boolean.TRUE));
		List<ServiceRequest> serviceRequestList = mongoTemplate.find(query,ServiceRequest.class);
		for(ServiceRequest request:serviceRequestList){
			if(request.getStatus()!=null && request.getStatus().equals(StaticFields.STATUS_APPROVED)){
				return true;
			}
		}
		return false;
	}
	@SuppressWarnings({"unchecked", "unused"})
	public boolean updateServiceRequest(ServiceRequest serviceRequest) throws ItorixException {
		try {
			if(isApproved(serviceRequest)){
				throw new ItorixException(StaticFields.ERR_MSG_APIGEE_1008,StaticFields.ERR_CODE_APIGEE_1008);
			}

			Query query = null;
			List<ServiceRequest> serviceRequests = (ArrayList<ServiceRequest>) getAllActiveServiceRequests(
					serviceRequest);

			if (serviceRequests.size() > 0) {
				log.debug("Updating service request");
				if (StringUtils.isNotBlank(serviceRequest.getType()) && StringUtils.isNotBlank(serviceRequest.getOrg())
						&& StringUtils.isNotBlank(serviceRequest.getEnv())
						&& StringUtils.isNotBlank(serviceRequest.getName())) {
					query = new Query(Criteria.where(StaticFields.ORG_NAME).is(serviceRequest.getOrg()).and(StaticFields.ENV_NAME)
							.is(serviceRequest.getEnv()).and(StaticFields.NAME).is(serviceRequest.getName()).and(StaticFields.TYPE_NAME)
							.is(serviceRequest.getType()).and(StaticFields.IS_SAAS).is(serviceRequest.getIsSaaS()));
				} else if (StringUtils.isNotBlank(serviceRequest.getType())
						&& StringUtils.isNotBlank(serviceRequest.getOrg())
						&& StringUtils.isNotBlank(serviceRequest.getName())) {
					query = new Query(Criteria.where(StaticFields.ORG_NAME).is(serviceRequest.getOrg()).and(StaticFields.NAME)
							.is(serviceRequest.getName()).and(StaticFields.TYPE_NAME).is(serviceRequest.getType()).and(StaticFields.IS_SAAS)
							.is(serviceRequest.getIsSaaS()));
				}

				Update update = new Update();
				update.set(StaticFields.ACTIVE_FLAG, Boolean.FALSE);
				UpdateResult result = mongoTemplate.updateMulti(query, update, ServiceRequest.class);
			}
			mongoTemplate.insert(serviceRequest);
			try{
				sendEmailTo(serviceRequest);
			}catch (MessagingException ex) {
				log.error("Exception while sending email", ex.getMessage());
			}
			return true;
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	@SuppressWarnings({"unchecked", "unused"})
	public boolean revertServiceRequest(String requestId) throws ItorixException, MessagingException, JsonProcessingException {
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User user = identityManagementDao.getUserDetailsFromSessionID(userSessionToken.getId());
		boolean isRevertApplicable = true;

		ServiceRequest serviceRequest = findServiceRequestByRequestId(requestId);
		List<ServiceRequest> existingServiceRequests = (List<ServiceRequest>) getservicerequest(serviceRequest);

		if(existingServiceRequests.size()>1){
			for (ServiceRequest existingServiceRequest : existingServiceRequests) {
				if (existingServiceRequest.getStatus().equalsIgnoreCase("Approved")) {
					isRevertApplicable = false;
					return isRevertApplicable;
				}
			}
		} else {
			if (!existingServiceRequests.isEmpty()) {
				if (StringUtils.equalsIgnoreCase(existingServiceRequests.get(0).getStatus(), "Review")) {
					isRevertApplicable = false;
					return isRevertApplicable;
				}
			}
		}

		if (isRevertApplicable) {
			List<ServiceRequest> serviceRequests = (List<ServiceRequest>) getservicerequest(serviceRequest);
			if (serviceRequests.size() > 0) {
				Query query = null;
				if (StringUtils.isNotBlank(serviceRequest.getType()) && StringUtils.isNotBlank(serviceRequest.getOrg())
						&& StringUtils.isNotBlank(serviceRequest.getEnv())
						&& StringUtils.isNotBlank(serviceRequest.getName())) {
					query = new Query(Criteria.where("org").is(serviceRequest.getOrg()).and("env")
							.is(serviceRequest.getEnv()).and("name").is(serviceRequest.getName()).and("type")
							.is(serviceRequest.getType()).and("isSaaS").is(serviceRequest.getIsSaaS()));
				} else if (StringUtils.isNotBlank(serviceRequest.getType())
						&& StringUtils.isNotBlank(serviceRequest.getOrg())
						&& StringUtils.isNotBlank(serviceRequest.getName())) {
					query = new Query(Criteria.where("org").is(serviceRequest.getOrg()).and("name")
							.is(serviceRequest.getName()).and("type").is(serviceRequest.getType()).and("isSaaS")
							.is(serviceRequest.getIsSaaS()));
				}
				revertServiceRequestStatus(serviceRequest, user);
				Update update = new Update();
				update.set("activeFlag", Boolean.FALSE);
				UpdateResult result = mongoTemplate.updateMulti(query, update, ServiceRequest.class);
			}

			Query query = new Query(Criteria.where("_id").is(requestId));
			Update update = new Update();
			update.set("activeFlag", true);
			UpdateResult result = mongoTemplate.updateMulti(query, update, ServiceRequest.class);
			try{
				sendEmailTo(serviceRequest);
			}catch (MessagingException ex) {
				log.error("Exception while sending email", ex.getMessage());
			}
		}
		return isRevertApplicable;
	}

	public ServiceRequest findServiceRequestByRequestId(String requestId) {
		return mongoTemplate.findById(requestId, ServiceRequest.class);
	}

	@SuppressWarnings("unchecked")
	public boolean changeServiceRequestStatus(ServiceRequest config, User user)
			throws ItorixException, MessagingException, JsonProcessingException {
		List<ServiceRequest> serviceRequests = (ArrayList<ServiceRequest>) getAllActiveServiceRequests(config);
		if (serviceRequests.size() > 0) {
			log.debug("Changing service request status");
			ServiceRequest serviceRequest = serviceRequests.get(0);
			if (config.getStatus().equals("Review")) {
				if (serviceRequest.getStatus().equals("Change Required"))
					serviceRequest.setStatus(config.getStatus());
				else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			} else if (config.getStatus().equals("Approved")) {
				if (serviceRequest.getStatus().equals("Review")) {
					serviceRequest.setStatus(config.getStatus());
				} else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			} else if (config.getStatus().equals("Change Required")) {
				if(serviceRequest.getStatus().equals("Review")) {
					serviceRequest.setStatus(config.getStatus());
				}else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			}else if (config.getStatus().equals("Rejected")) {
				if(serviceRequest.getStatus().equals("Review")
						|| serviceRequest.getStatus().equals("Change Required")) {
					serviceRequest.setStatus(config.getStatus());
				}else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			}
			updateServiceRequestStatus(serviceRequest, user);
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1029"), "Configuration-1029");
		}
		return false;
	}

	public boolean revertServiceRequestStatus(ServiceRequest config, User user)
			throws ItorixException, MessagingException, JsonProcessingException {
		return updateServiceRequestStatus(config, user);
	}

	private com.itorix.apiwiz.common.model.apigeeX.TargetConfig getTargetConf(ServiceRequest serviceRequest) {
		com.itorix.apiwiz.common.model.apigeeX.TargetConfig targetConfig = new com.itorix.apiwiz.common.model.apigeeX.TargetConfig();
		targetConfig.setMts(Instant.now().toEpochMilli());
		targetConfig.setModifiedUserName(serviceRequest.getModifiedUser());
		targetConfig.setOrg(serviceRequest.getOrg());
		targetConfig.setEnv(serviceRequest.getEnv());
		targetConfig.setHost(serviceRequest.getHost());
		targetConfig.setPort(serviceRequest.getPort());
		targetConfig.setClientAuthEnabled(serviceRequest.isClientAuthEnabled());
		targetConfig.setIgnoreValidationErrors(serviceRequest.isIgnoreValidationErrors());
		targetConfig.setKeyAlias(serviceRequest.getKeyAlias());
		targetConfig.setTrustStore(serviceRequest.getTrustStore());
		targetConfig.setSslEnabled(serviceRequest.isSslEnabled());
		targetConfig.setKeyStore(serviceRequest.getKeyStore());
		targetConfig.setEnabled(serviceRequest.isEnabled());
		targetConfig.setName(serviceRequest.getName());
		if (serviceRequest.getIsSaaS()) {
			targetConfig.setType("saas");
		} else {
			targetConfig.setType("onprem");
		}
		return targetConfig;
	}
	private com.itorix.apiwiz.common.model.apigeeX.KVMConfig getKVMConfig(ServiceRequest serviceRequest) {
		com.itorix.apiwiz.common.model.apigeeX.KVMConfig kvmconfig = new com.itorix.apiwiz.common.model.apigeeX.KVMConfig();
		kvmconfig.setMts(Instant.now().getEpochSecond());
		kvmconfig.setModifiedBy(serviceRequest.getModifiedUser());
		kvmconfig.setOrg(serviceRequest.getOrg());
		kvmconfig.setEnv(serviceRequest.getEnv());
		kvmconfig.setName(serviceRequest.getName());
		kvmconfig.setEntry(serviceRequest.getEntry());
		if (serviceRequest.getIsSaaS()) {
			kvmconfig.setType("saas");
		} else {
			kvmconfig.setType("onprem");
		}
		return kvmconfig;
	}

	private com.itorix.apiwiz.common.model.apigeeX.ProductConfig getProductConfig(ServiceRequest serviceRequest) {
		com.itorix.apiwiz.common.model.apigeeX.ProductConfig productconfig = new com.itorix.apiwiz.common.model.apigeeX.ProductConfig();
		productconfig.setOrg(serviceRequest.getOrg());
		productconfig.setName(serviceRequest.getName());
		productconfig.setApiResources(serviceRequest.getApiResources());
		productconfig.setApprovalType(serviceRequest.getApprovalType());
		productconfig.setAttributes(serviceRequest.getAttributes());
		productconfig.setDescription(serviceRequest.getDescription());
		productconfig.setDisplayName(serviceRequest.getDisplayName());
		productconfig.setEnvironments(serviceRequest.getEnvironments());
		productconfig.setProxies(serviceRequest.getProxies());
		productconfig.setQuota(serviceRequest.getQuota());
		productconfig.setQuotaInterval(serviceRequest.getQuotaInterval());
		productconfig.setQuotaTimeUnit(serviceRequest.getQuotaTimeUnit());
		productconfig.setScopes(serviceRequest.getScopes());
		if (serviceRequest.getIsSaaS()) {
			productconfig.setType("saas");
		} else {
			productconfig.setType("onprem");
		}
		return productconfig;
	}

	public boolean updateServiceRequestStatus(ServiceRequest config, User user)
			throws ItorixException, MessagingException, JsonProcessingException {
		@SuppressWarnings("unused")
		boolean isCreatedorUpdated = false;
		ServiceRequest serviceRequest = config;
		Query query = null;
		log.debug("Update service request status : {}", config);
		if (config.getStatus().equalsIgnoreCase("Approved")) {
			log.debug("Update service request status");
			if (serviceRequest.isCreated()) {
				if ("TargetServer".equalsIgnoreCase(serviceRequest.getType())) {

					if (config.getGwType() != null && config.getGwType().equalsIgnoreCase("apigeex")) {
						com.itorix.apiwiz.common.model.apigeeX.TargetConfig targetConfig = getTargetConf(
								serviceRequest);
						isCreatedorUpdated = apigeeXConfigDao.updateTarget(targetConfig);
						apigeeXConfigDao.createApigeeTarget(targetConfig, user);
					} else {
						TargetConfig targetConfig = new TargetConfig();
						targetConfig.setModifiedDate(Instant.now().toString());
						targetConfig.setModifiedUser(serviceRequest.getModifiedUser());
						targetConfig.setOrg(serviceRequest.getOrg());
						targetConfig.setEnv(serviceRequest.getEnv());
						targetConfig.setHost(serviceRequest.getHost());
						targetConfig.setPort(serviceRequest.getPort());
						targetConfig.setClientAuthEnabled(serviceRequest.isClientAuthEnabled());
						targetConfig.setIgnoreValidationErrors(serviceRequest.isIgnoreValidationErrors());
						targetConfig.setKeyAlias(serviceRequest.getKeyAlias());
						targetConfig.setTrustStore(serviceRequest.getTrustStore());
						targetConfig.setSslEnabled(serviceRequest.isSslEnabled());
						targetConfig.setKeyStore(serviceRequest.getKeyStore());
						targetConfig.setEnabled(serviceRequest.isEnabled());
						targetConfig.setName(serviceRequest.getName());
						if (serviceRequest.getIsSaaS()) {
							targetConfig.setType("saas");
						} else {
							targetConfig.setType("onprem");
						}
						isCreatedorUpdated = configManagementDao.updateTarget(targetConfig);
						configManagementDao.createApigeeTarget(targetConfig, user);
					}
				} else if ("Cache".equalsIgnoreCase(serviceRequest.getType())) {
					CacheConfig cacheConfig = new CacheConfig();
					cacheConfig.setModifiedDate(Instant.now().toString());
					cacheConfig.setModifiedUser(serviceRequest.getModifiedUser());
					cacheConfig.setOrg(serviceRequest.getOrg());
					cacheConfig.setEnv(serviceRequest.getEnv());
					cacheConfig.setDescription(serviceRequest.getDescription());
					cacheConfig.setExpiryDate(serviceRequest.getExpiryDate());
					cacheConfig.setTimeOfDay(serviceRequest.getTimeOfDay());
					cacheConfig.setTimeoutInSec(serviceRequest.getTimeoutInSec());
					cacheConfig.setExpiryType(serviceRequest.getExpiryType());
					cacheConfig.setOverflowToDisk(serviceRequest.isOverflowToDisk());
					cacheConfig.setName(serviceRequest.getName());
					cacheConfig.setSkipCacheIfElementSizeInKBExceeds(
							serviceRequest.getSkipCacheIfElementSizeInKBExceeds());
					cacheConfig.setValuesNull(serviceRequest.isValuesNull());
					if (serviceRequest.getIsSaaS()) {
						cacheConfig.setType("saas");
					} else {
						cacheConfig.setType("onprem");
					}
					isCreatedorUpdated = configManagementDao.updateCache(cacheConfig);
					configManagementDao.createApigeeCache(cacheConfig, user);
				} else if ("KVM".equalsIgnoreCase(serviceRequest.getType())) {
					if (config.getGwType() != null && config.getGwType().equalsIgnoreCase("apigeex")) {
						com.itorix.apiwiz.common.model.apigeeX.KVMConfig kvmconfig = getKVMConfig(serviceRequest);
						isCreatedorUpdated = apigeeXConfigDao.updateKVM(kvmconfig);
						apigeeXConfigDao.createApigeeKVM(kvmconfig, user);
					} else {
						KVMConfig kvmconfig = new KVMConfig();
						kvmconfig.setModifiedDate(Instant.now().toString());
						kvmconfig.setModifiedUser(serviceRequest.getModifiedUser());
						kvmconfig.setOrg(serviceRequest.getOrg());
						kvmconfig.setEnv(serviceRequest.getEnv());
						kvmconfig.setName(serviceRequest.getName());
						kvmconfig.setEntry(serviceRequest.getEntry());
						kvmconfig.setEncrypted(serviceRequest.getEncrypted());
						if (serviceRequest.getIsSaaS()) {
							kvmconfig.setType("saas");
						} else {
							kvmconfig.setType("onprem");
						}
						isCreatedorUpdated = configManagementDao.updateKVM(kvmconfig);
						configManagementDao.createApigeeKVM(kvmconfig, user);
					}
				} else if ("Product".equalsIgnoreCase(serviceRequest.getType())) {
					if (config.getGwType() != null && config.getGwType().equalsIgnoreCase("apigeex")) {
						com.itorix.apiwiz.common.model.apigeeX.ProductConfig productconfig = getProductConfig(
								serviceRequest);
						isCreatedorUpdated = apigeeXConfigDao.updateProduct(productconfig);;
						apigeeXConfigDao.createApigeeProduct(productconfig, user);
					} else {
						ProductConfig productconfig = new ProductConfig();
						productconfig.setModifiedDate(Instant.now().toString());
						productconfig.setModifiedUser(serviceRequest.getModifiedUser());
						productconfig.setOrg(serviceRequest.getOrg());
						productconfig.setName(serviceRequest.getName());
						productconfig.setApiResources(serviceRequest.getApiResources());
						productconfig.setApprovalType(serviceRequest.getApprovalType());
						productconfig.setDescription(serviceRequest.getDescription());
						productconfig.setDisplayName(serviceRequest.getDisplayName());
						productconfig.setEnvironments(serviceRequest.getEnvironments());
						productconfig.setProxies(serviceRequest.getProxies());
						productconfig.setQuota(serviceRequest.getQuota());
						productconfig.setQuotaInterval(serviceRequest.getQuotaInterval());
						productconfig.setQuotaTimeUnit(serviceRequest.getQuotaTimeUnit());
						productconfig.setScopes(serviceRequest.getScopes());
						List<ProductAttributes> productAttributes = serviceRequest.getAttributes();
						List<ProductAttributes> oldProductAttributes = new ArrayList<>(serviceRequest.getAttributes());
						createTransactionRecordingPolicies(productAttributes,serviceRequest);
						if (serviceRequest.getIsSaaS()) {
							productconfig.setType("saas");
						} else {
							productconfig.setType("onprem");
						}
						productconfig.setAttributes(productAttributes);
						isCreatedorUpdated = configManagementDao.updateProduct(productconfig);
						configManagementDao.createApigeeProduct(productconfig, user);
						serviceRequest.setAttributes(oldProductAttributes);
					}
				}
				serviceRequest.setCreated(true);
				serviceRequest.setStatus("Approved");
				serviceRequest.setApprovedBy(serviceRequest.getModifiedUser());
				try{
					sendEmailTo(serviceRequest);
				}catch (MessagingException ex) {
					log.error("Exception while sending email", ex.getMessage());
				}
				query = new Query(Criteria.where("_id").is(serviceRequest.get_id()));
				Document dbDoc = new Document();
				mongoTemplate.getConverter().write(serviceRequest, dbDoc);
				Update update = Update.fromDocument(dbDoc, "_id");
				UpdateResult result = mongoTemplate.updateFirst(query, update, ServiceRequest.class);
				return result.wasAcknowledged();
			} else {
				if ("TargetServer".equalsIgnoreCase(serviceRequest.getType())) {
					if (config.getGwType() != null && config.getGwType().equalsIgnoreCase("apigeex")) {
						com.itorix.apiwiz.common.model.apigeeX.TargetConfig targetConfig = getTargetConf(
								serviceRequest);
						isCreatedorUpdated = apigeeXConfigDao.updateTarget(targetConfig);
						apigeeXConfigDao.createApigeeTarget(targetConfig, user);
					} else {
						TargetConfig targetConfig = new TargetConfig();
						targetConfig.setCreatedDate(Instant.now().toString());
						targetConfig.setCreatedUser(serviceRequest.getCreatedUser());
						targetConfig.setModifiedDate(Instant.now().toString());
						targetConfig.setModifiedUser(serviceRequest.getModifiedUser());
						targetConfig.setOrg(serviceRequest.getOrg());
						targetConfig.setEnv(serviceRequest.getEnv());
						targetConfig.setHost(serviceRequest.getHost());
						targetConfig.setPort(serviceRequest.getPort());
						targetConfig.setClientAuthEnabled(serviceRequest.isClientAuthEnabled());
						targetConfig.setIgnoreValidationErrors(serviceRequest.isIgnoreValidationErrors());
						targetConfig.setKeyAlias(serviceRequest.getKeyAlias());
						targetConfig.setTrustStore(serviceRequest.getTrustStore());
						targetConfig.setSslEnabled(serviceRequest.isSslEnabled());
						targetConfig.setKeyStore(serviceRequest.getKeyStore());
						targetConfig.setEnabled(serviceRequest.isEnabled());
						targetConfig.setName(serviceRequest.getName());
						targetConfig.setActiveFlag(Boolean.TRUE);
						if (serviceRequest.getIsSaaS()) {
							targetConfig.setType("saas");
						} else {
							targetConfig.setType("onprem");
						}
						isCreatedorUpdated = configManagementDao.saveTarget(targetConfig);
						configManagementDao.createApigeeTarget(targetConfig, user);
					}
				} else if ("Cache".equalsIgnoreCase(serviceRequest.getType())) {
					CacheConfig cacheConfig = new CacheConfig();
					cacheConfig.setCreatedDate(Instant.now().toString());
					cacheConfig.setCreatedUser(serviceRequest.getCreatedUser());
					cacheConfig.setModifiedDate(Instant.now().toString());
					cacheConfig.setModifiedUser(serviceRequest.getModifiedUser());
					cacheConfig.setOrg(serviceRequest.getOrg());
					cacheConfig.setEnv(serviceRequest.getEnv());
					cacheConfig.setDescription(serviceRequest.getDescription());
					cacheConfig.setExpiryDate(serviceRequest.getExpiryDate());
					cacheConfig.setOverflowToDisk(serviceRequest.isOverflowToDisk());
					cacheConfig.setName(serviceRequest.getName());
					cacheConfig.setSkipCacheIfElementSizeInKBExceeds(
							serviceRequest.getSkipCacheIfElementSizeInKBExceeds());
					cacheConfig.setValuesNull(serviceRequest.isValuesNull());
					cacheConfig.setTimeOfDay(serviceRequest.getTimeOfDay());
					cacheConfig.setTimeoutInSec(serviceRequest.getTimeoutInSec());
					cacheConfig.setExpiryType(serviceRequest.getExpiryType());
					cacheConfig.setActiveFlag(Boolean.TRUE);
					if (serviceRequest.getIsSaaS()) {
						cacheConfig.setType("saas");
					} else {
						cacheConfig.setType("onprem");
					}
					isCreatedorUpdated = configManagementDao.saveCache(cacheConfig);
					configManagementDao.createApigeeCache(cacheConfig, user);
				} else if ("KVM".equalsIgnoreCase(serviceRequest.getType())) {
					if (config.getGwType() != null && config.getGwType().equalsIgnoreCase("apigeex")) {
						com.itorix.apiwiz.common.model.apigeeX.KVMConfig kvmconfig = getKVMConfig(serviceRequest);
						isCreatedorUpdated = apigeeXConfigDao.updateKVM(kvmconfig);
						apigeeXConfigDao.createApigeeKVM(kvmconfig, user);
					} else {
						KVMConfig kvmconfig = new KVMConfig();
						kvmconfig.setCreatedDate(Instant.now().toString());
						kvmconfig.setCreatedUser(serviceRequest.getCreatedUser());
						kvmconfig.setModifiedDate(Instant.now().toString());
						kvmconfig.setModifiedUser(serviceRequest.getModifiedUser());
						kvmconfig.setOrg(serviceRequest.getOrg());
						kvmconfig.setEnv(serviceRequest.getEnv());
						kvmconfig.setName(serviceRequest.getName());
						kvmconfig.setEntry(serviceRequest.getEntry());
						kvmconfig.setEncrypted(serviceRequest.getEncrypted());
						kvmconfig.setActiveFlag(Boolean.TRUE);
						if (serviceRequest.getIsSaaS()) {
							kvmconfig.setType("saas");
						} else {
							kvmconfig.setType("onprem");
						}
						isCreatedorUpdated = configManagementDao.saveKVM(kvmconfig);
						configManagementDao.createApigeeKVM(kvmconfig, user);
					}
				} else if ("Product".equalsIgnoreCase(serviceRequest.getType())) {
					if (config.getGwType() != null && config.getGwType().equalsIgnoreCase("apigeex")) {
						com.itorix.apiwiz.common.model.apigeeX.ProductConfig productconfig = getProductConfig(
								serviceRequest);
						isCreatedorUpdated = apigeeXConfigDao.updateProduct(productconfig);;
						apigeeXConfigDao.createApigeeProduct(productconfig, user);
					} else {
						ProductConfig productconfig = new ProductConfig();
						productconfig.setModifiedDate(Instant.now().toString());
						productconfig.setModifiedUser(serviceRequest.getModifiedUser());
						productconfig.setOrg(serviceRequest.getOrg());
						productconfig.setName(serviceRequest.getName());
						productconfig.setApiResources(serviceRequest.getApiResources());
						productconfig.setApprovalType(serviceRequest.getApprovalType());
						productconfig.setCreatedDate(Instant.now().toString());
						productconfig.setCreatedUser(serviceRequest.getCreatedUser());
						productconfig.setDescription(serviceRequest.getDescription());
						productconfig.setDisplayName(serviceRequest.getDisplayName());
						productconfig.setEnvironments(serviceRequest.getEnvironments());
						productconfig.setProxies(serviceRequest.getProxies());
						productconfig.setQuota(serviceRequest.getQuota());
						productconfig.setQuotaInterval(serviceRequest.getQuotaInterval());
						productconfig.setQuotaTimeUnit(serviceRequest.getQuotaTimeUnit());
						productconfig.setScopes(serviceRequest.getScopes());
						productconfig.setActiveFlag(Boolean.TRUE);
						List<ProductAttributes> productAttributes = serviceRequest.getAttributes();
						List<ProductAttributes> oldProductAttributes = new ArrayList<>(serviceRequest.getAttributes());
						createTransactionRecordingPolicies(productAttributes,serviceRequest);
						if (serviceRequest.getIsSaaS()) {
							productconfig.setType("saas");
						} else {
							productconfig.setType("onprem");
						}
						productconfig.setAttributes(productAttributes);
						isCreatedorUpdated = configManagementDao.updateProductConfig(productconfig);
						configManagementDao.createApigeeProduct(productconfig, user);
						serviceRequest.setAttributes(oldProductAttributes);
					}
				}
				serviceRequest.setCreated(true);
				serviceRequest.setStatus("Approved");
				serviceRequest.setApprovedBy(serviceRequest.getModifiedUser());
				try{
					sendEmailTo(serviceRequest);
				}catch (MessagingException ex) {
					log.error("Exception while sending email", ex.getMessage());
				}
				query = new Query(Criteria.where("_id").is(serviceRequest.get_id()));
				Document dbDoc = new Document();
				mongoTemplate.getConverter().write(serviceRequest, dbDoc);
				Update update = Update.fromDocument(dbDoc, "_id");
				UpdateResult result = mongoTemplate.updateFirst(query, update, ServiceRequest.class);
				return result.wasAcknowledged();
			}

		} else if (config.getStatus().equalsIgnoreCase("Change Required")) {
			if ("Product".equalsIgnoreCase(serviceRequest.getType())) {
				query = new Query(Criteria.where("org").is(serviceRequest.getOrg()).and("name").is(config.getName())
						.and("type").is(serviceRequest.getType()).and("isSaaS").is(config.getIsSaaS()).and("activeFlag")
						.is(Boolean.TRUE));
			} else {
				query = new Query(
						Criteria.where("org").is(serviceRequest.getOrg()).and("env").is(serviceRequest.getEnv())
								.and("name").is(config.getName()).and("type").is(serviceRequest.getType()).and("isSaaS")
								.is(config.getIsSaaS()).and("activeFlag").is(Boolean.TRUE));
			}
			serviceRequest.setStatus("Change Required");
			serviceRequest.setApprovedBy(serviceRequest.getModifiedUser());
			try{
				sendEmailTo(serviceRequest);
			}catch (MessagingException ex) {
				log.error("Exception while sending email", ex.getMessage());
			}
			// DBObject dbDoc = new BasicDBObject();
			// mongoTemplate.getConverter().write(serviceRequest, dbDoc);
			// Update update = Update.fromDBObject(dbDoc, "_id");

			Document dbDoc = new Document();
			mongoTemplate.getConverter().write(serviceRequest, dbDoc);
			Update update = Update.fromDocument(dbDoc, "_id");
			UpdateResult result = mongoTemplate.updateFirst(query, update, ServiceRequest.class);
			return result.wasAcknowledged();
		} else if (config.getStatus().equalsIgnoreCase("Rejected")) {
			if ("Product".equalsIgnoreCase(serviceRequest.getType())) {
				query = new Query(Criteria.where("org").is(serviceRequest.getOrg()).and("name").is(config.getName())
						.and("type").is(serviceRequest.getType()).and("isSaaS").is(config.getIsSaaS()).and("activeFlag")
						.is(Boolean.TRUE));
			} else {
				query = new Query(
						Criteria.where("org").is(serviceRequest.getOrg()).and("env").is(serviceRequest.getEnv())
								.and("name").is(config.getName()).and("type").is(serviceRequest.getType()).and("isSaaS")
								.is(config.getIsSaaS()).and("activeFlag").is(Boolean.TRUE));
			}
			serviceRequest.setStatus("Rejected");
			serviceRequest.setApprovedBy(serviceRequest.getModifiedUser());
			try{
				sendEmailTo(serviceRequest);
			}catch (MessagingException ex) {
				log.error("Exception while sending email", ex.getMessage());
			}
			Document dbDoc = new Document();
			mongoTemplate.getConverter().write(serviceRequest, dbDoc);
			Update update = Update.fromDocument(dbDoc, "_id");
			UpdateResult result = mongoTemplate.updateFirst(query, update, ServiceRequest.class);
			return result.wasAcknowledged();
		}else if (config.getStatus().equalsIgnoreCase("Review")) {
			if ("Product".equalsIgnoreCase(serviceRequest.getType())) {
				query = new Query(Criteria.where("org").is(serviceRequest.getOrg()).and("name").is(config.getName())
						.and("type").is(serviceRequest.getType()).and("isSaaS").is(config.getIsSaaS()).and("activeFlag")
						.is(Boolean.TRUE));
			} else {
				query = new Query(
						Criteria.where("org").is(serviceRequest.getOrg()).and("env").is(serviceRequest.getEnv())
								.and("name").is(config.getName()).and("type").is(serviceRequest.getType()).and("isSaaS")
								.is(config.getIsSaaS()).and("activeFlag").is(Boolean.TRUE));
			}
			serviceRequest.setStatus("Review");
			serviceRequest.setApprovedBy(serviceRequest.getModifiedUser());
			try{
				sendEmailTo(serviceRequest);
			}catch (MessagingException ex) {
				log.error("Exception while sending email", ex.getMessage());
			}
			Document dbDoc = new Document();
			mongoTemplate.getConverter().write(serviceRequest, dbDoc);
			Update update = Update.fromDocument(dbDoc, "_id");
			UpdateResult result = mongoTemplate.updateFirst(query, update, ServiceRequest.class);
			return result.wasAcknowledged();
		}  else {
			if ((!config.getStatus().equalsIgnoreCase("Change Required")
					&& (!config.getStatus().equalsIgnoreCase("Approved")))
					&& (!config.getStatus().equalsIgnoreCase("Review"))
					&& (!config.getStatus().equalsIgnoreCase("Rejected")))
				throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1030"), "Configuration-1030");

			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1031"), "Configuration-1031");
		}

	}

	public void createLogHistory(ServiceRequestComments serviceRequestComments) {
		mongoTemplate.save(serviceRequestComments);
	}


	public void createMonetizationLogHistory(MonetizationConfigComments monetizationConfigComments) {
		mongoTemplate.save(monetizationConfigComments);
	}

	public Object getLogHistory(ServiceRequestComments serviceRequestComments) throws ItorixException {
		try {
			Query query = null;
			if (StringUtils.isNotBlank(serviceRequestComments.getType())
					&& StringUtils.isNotBlank(serviceRequestComments.getOrg())
					&& (StringUtils.isNotBlank(serviceRequestComments.getEnv())
							|| "Product".equalsIgnoreCase(serviceRequestComments.getType()))
					&& StringUtils.isNotBlank(serviceRequestComments.getName())) {

				if ("Product".equalsIgnoreCase(serviceRequestComments.getType())) {
					query = new Query(Criteria.where("org").is(serviceRequestComments.getOrg()).and("name")
							.is(serviceRequestComments.getName()).and("type").is(serviceRequestComments.getType())
							.and("isSaaS").is(serviceRequestComments.getIsSaaS()));
				} else {
					query = new Query(Criteria.where("org").is(serviceRequestComments.getOrg()).and("env")
							.is(serviceRequestComments.getEnv()).and("name").is(serviceRequestComments.getName())
							.and("type").is(serviceRequestComments.getType()).and("isSaaS")
							.is(serviceRequestComments.getIsSaaS()));
				}
			}
			return mongoTemplate.find(query, ServiceRequestComments.class);
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public Object getMonetizationLogHistory(MonetizationConfigComments monetizationConfigComments) throws ItorixException {
		try {
			Query query = null;
			if (StringUtils.isNotBlank(monetizationConfigComments.getType())
					&& StringUtils.isNotBlank(monetizationConfigComments.getOrg())
					&& StringUtils.isNotBlank(monetizationConfigComments.getName())) {
				query = new Query(Criteria.where("org").is(monetizationConfigComments.getOrg()).and("name")
						.is(monetizationConfigComments.getName()).and("type").is(monetizationConfigComments.getType()));
			}
			return mongoTemplate.find(query, MonetizationConfigComments.class);
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	@SuppressWarnings("unchecked")
	public Object getservicerequestsList() throws ItorixException {
		try {
			List listData = new ArrayList();
			List<ServiceRequest> serviceRequests = (ArrayList<ServiceRequest>) getAllActiveServiceRequests(null);
			for (ServiceRequest cache : serviceRequests) {
				Map map = new HashMap();
				map.put("name", cache.getName());
				map.put("type", cache.getType());
				map.put("org", cache.getOrg());
				map.put("env", cache.getEnv());
				map.put("createdDate", cache.getCreatedDate());
				map.put("createdBy", cache.getCreatedUser());
				map.put("modifiedBy", cache.getModifiedUser());
				map.put("status", cache.getStatus());
				map.put("modifiedDate", cache.getModifiedDate());
				map.put("isSaaS", cache.getIsSaaS());
				if ("Change Required".equalsIgnoreCase(cache.getStatus())) {
					map.put("rejectedBy", cache.getApprovedBy());
				} else {
					map.put("approvedBy", cache.getApprovedBy());
				}
				listData.add(map);
			}
			return listData;
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}



	@SuppressWarnings("unchecked")
	public Object getservicerequests(ServiceRequest serviceRequest) throws ItorixException {
		try {
			List<ServiceRequest> serviceRequests = (ArrayList<ServiceRequest>) getAllActiveServiceRequests(
					serviceRequest);
			List listData = new ArrayList();
			for (ServiceRequest cache : serviceRequests) {
				Map map = new HashMap();
				map.put("name", cache.getName());
				map.put("type", cache.getType());
				map.put("org", cache.getOrg());
				map.put("env", cache.getEnv());
				map.put("createdDate", cache.getCreatedDate());
				map.put("createdBy", cache.getCreatedUser());
				map.put("modifiedBy", cache.getModifiedUser());
				map.put("status", cache.getStatus());
				map.put("modifiedDate", cache.getModifiedDate());
				map.put("isSaaS", cache.getIsSaaS());
				if ("Change Required".equalsIgnoreCase(cache.getStatus())) {
					map.put("rejectedBy", cache.getApprovedBy());
				} else {
					map.put("approvedBy", cache.getApprovedBy());
				}
				listData.add(map);
			}
			if (listData.size() == 0) {
				throw new ItorixException("No Record exists", "Configuration-1003");
			}
			return listData;
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}
	@SuppressWarnings("unchecked")
	public Object getservicerequestsFullDetails(ServiceRequest serviceRequest) throws ItorixException {
		try {
			List<ServiceRequest> serviceRequests = (ArrayList<ServiceRequest>) getAllActiveServiceRequests(
					serviceRequest);
			if (serviceRequests.size() == 0)
				throw new ItorixException("No Record exists", "Configuration-1003");
			return serviceRequests;
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}
	public ObjectNode getServiceRequestStats(String timeunit, String timerange) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		String[] dates = timerange != null ? timerange.split("~") : null;
		long startDate = -1L;
		long endDate = -1L;
		long orgStartDateinit = -1L;
		long origEnddate =-1L;
		if (dates != null && dates.length > 0) {
			startDate = Long.parseLong(dates[0]);
			endDate = Long.parseLong(dates[1]);
			orgStartDateinit = startDate;
			origEnddate = endDate;
		}
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		ObjectNode metricsNode = mapper.createObjectNode();
		metricsNode.put("name", timeunit);
		ArrayNode typesNode = mapper.createArrayNode();
		ObjectNode statussNode = mapper.createObjectNode();
		ArrayNode typesStatusArrayNode = mapper.createArrayNode();
		List<String> distincttypes = baseRepository.findDistinctValuesByColumnName(ServiceRequest.class, "type");
		for (String type : distincttypes) {
			ObjectNode typeNode = mapper.createObjectNode();
			ArrayNode valuesNode = mapper.createArrayNode();
			typeNode.put("name", type);
			if(startDate == -1L ){
				Query query = new Query();
				query.addCriteria(Criteria.where("type").is(type));
				List<ServiceRequest> list = baseRepository.find(query, ServiceRequest.class);
				ObjectNode valueNode = mapper.createObjectNode();
				valueNode.put("value", list.size());
				valuesNode.add(valueNode);
			} else {
				while (startDate<endDate) {
					Query query = new Query();
					query.addCriteria(
							Criteria.where(ServiceRequest.LABEL_CREATED_TIME).gte(new Date(DateUtil.convertToStartOfDay(startDate)))
									.lt(new Date(DateUtil.convertToEndOfDay(endDate))).and("type").is(type));
					List<ServiceRequest> list = baseRepository.find(query, ServiceRequest.class);
					// if(list!=null && list.size()>0){
					ObjectNode valueNode = mapper.createObjectNode();
					valueNode.put("timestamp", startDate + "");
					valueNode.put("value", list.size());
					valuesNode.add(valueNode);
//					 }
					startDate += 86400000L;//(24 * 60 * 60 * 1000)
				}
			}
			typeNode.put("values", valuesNode);
			typesNode.add(typeNode);
			startDate = orgStartDateinit;
		}
		metricsNode.set("type", typesNode);
		List<String> distinctList = baseRepository.findDistinctValuesByColumnName(ServiceRequest.class, "status");
		if (distinctList != null && distinctList.size() > 0) {
			for (String status : distinctList) {
				ObjectNode typesStatusNode = mapper.createObjectNode();
				ArrayNode statsNode = mapper.createArrayNode();
				ObjectNode statusnode = mapper.createObjectNode();
				ObjectNode dimesionNode = mapper.createObjectNode();
				Query query = new Query();
				query.addCriteria(Criteria.where("status").is(status));
				if(startDate != -1L && endDate != -1L) {
					query.addCriteria(
							Criteria.where("modifiedDate").gte(new Date(DateUtil.convertToStartOfDay(startDate)))
									.lt(new Date(DateUtil.convertToEndOfDay(endDate))));
				}
				query.addCriteria((Criteria.where("activeFlag").is(Boolean.TRUE)));
				List<ServiceRequest> listByStatus = baseRepository.find(query, ServiceRequest.class);
				typesStatusNode.put("status", status);
				typesStatusNode.put("count", listByStatus.size());
				for (String type : distincttypes) {
					ArrayNode namesNode = mapper.createArrayNode();
					query = new Query();
					query.addCriteria(Criteria.where("status").is(status).and("type").is(type));
					if(startDate != -1L && endDate != -1L) {
						query.addCriteria(
								Criteria.where("modifiedDate").gte(new Date(DateUtil.convertToStartOfDay(startDate)))
										.lt(new Date(DateUtil.convertToEndOfDay(endDate))));
					}
					query.addCriteria((Criteria.where("activeFlag").is(Boolean.TRUE)));
					List<ServiceRequest> listByStatusType = baseRepository.find(query, ServiceRequest.class);
					ObjectNode statNode = mapper.createObjectNode();
					statNode.put("type", type);
					ArrayList<String> names = new ArrayList<>();
					for (ServiceRequest serviceRequest : listByStatusType) {
						if (names.add(serviceRequest.getName())) {
							namesNode.add(serviceRequest.getName());
						}
					}
					statNode.put("count", namesNode.size());
					statNode.put("names", namesNode);
					statsNode.add(statNode);
				}
				typesStatusNode.put("dimensions", statsNode);
				typesStatusArrayNode.add(typesStatusNode);
			}
		}
		rootNode.set("metrics", metricsNode);
		statussNode.set("type", typesStatusArrayNode);
		rootNode.set("stats", statussNode);
		return rootNode;
	}

	public Object configSearchOnServiceRequest(String type, String name, int limit) throws ItorixException {
		BasicQuery query = new BasicQuery("{\"name\": {$regex : '" + name + "', $options: 'i'}}");
		query.limit(limit > 0 ? limit : 10);
		query.addCriteria(Criteria.where("type").is(type));
		List<String> allServiceRequests = mongoTemplate.findDistinct(query, "name", ServiceRequest.class, String.class);
//		List<String> allServiceRequests = getList(mongoTemplate.getCollection("Config.ServiceRequests").distinct("name",
//				query.getQueryObject(), String.class));
		net.sf.json.JSONObject serviceRequestList = new net.sf.json.JSONObject();
		Collections.sort(allServiceRequests);
		serviceRequestList.put("ServiceRequest", allServiceRequests);
		return serviceRequestList;
	}

	private List<String> getList(DistinctIterable<String> iterable) {
		List<String> list = new ArrayList<>();
		if (iterable != null) {
			MongoCursor<String> cursor = iterable.iterator();
			while (cursor.hasNext()) {
				list.add(cursor.next());
			}
		}
		return list;
	}

	// Monetization Producer Configs below

	public ProductBundle createProductBundle(ProductBundle productBundle) throws ItorixException {

		boolean isAnyRequestPending = false;
		if (productBundle != null && productBundle.getName() != null &&
				productBundle.getOrganization() != null) {
			List<ProductBundle> productBundles = mongoTemplate.find(Query.query(Criteria.where("organization").is(productBundle.getOrganization()).
					and("name").is(productBundle.getName())), ProductBundle.class);
			if (productBundles.size() == 0) {
				productBundle = mongoTemplate.save(productBundle);
				return productBundle;
			} else {
				if (productBundles.size() > 0) {
					for (ProductBundle bundle : productBundles) {
						if (bundle.getStatus().equalsIgnoreCase("Review")) {
							isAnyRequestPending = true;
						}
					}
					if (!isAnyRequestPending) {
						Query query =  new Query(Criteria.where("organization").is(productBundle.getOrganization()).and("name")
								.is(productBundle.getName()));
						Update update = new Update();
						update.set("activeFlag", Boolean.FALSE);
						UpdateResult result = mongoTemplate.updateMulti(query, update, ProductBundle.class);
						productBundle.setActiveFlag(Boolean.TRUE);
						productBundle = mongoTemplate.save(productBundle);
						return productBundle;
					} else {
						throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1026"),
								"Configuration-1026");
					}
				}
			}
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1028"),
					"Configuration-1028");
		}
		return null;
	}

	public String updateProductBundle(ProductBundle productBundle) throws ItorixException {

		try {
			Query query = null;
			List<ProductBundle> productBundles = mongoTemplate.find(Query.query(Criteria.where("organization").is(productBundle.getOrganization()).
							and("name").is(productBundle.getName()).and("activeFlag").is(Boolean.TRUE)),
					ProductBundle.class);

			if (productBundles.size() > 0) {
				if (productBundle != null &&productBundle.getName() != null &&
						productBundle.getOrganization() != null) {
					query = new Query(Criteria.where("organization").is(productBundle.getOrganization()).and("name")
							.is(productBundle.getName()));
				}
				Update update = new Update();
				update.set("activeFlag", Boolean.FALSE);
				UpdateResult result = mongoTemplate.updateMulti(query, update, ProductBundle.class);
			}
			return mongoTemplate.insert(productBundle).getId();
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public void deleteProductBundle(String bundleId) throws ItorixException {
		ProductBundle bundle = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(bundleId).and("status").ne("Approved")), ProductBundle.class);
		if(bundle == null){
			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1034"),
					"Configuration-1034");
		}
		mongoTemplate.remove(bundle);
	}

	public Object getProductBundles(String timerange,String status, int offset, int pagesize,String orgName,String id, String name) throws ItorixException {

		try {
			if(id != null){
				return mongoTemplate.findById(id, ProductBundle.class);
			}
			Query query=new Query();
			Query countquery = new Query();
			if(timerange!=null){
				SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
				String[] timeRanges = timerange.split("~");
				Date startDate=new Date(format.parse(timeRanges[0]).getTime());
				Date endDate=DateUtil.getEndOfDay(new Date(format.parse(timeRanges[1]).getTime()));
				query.addCriteria(Criteria.where("modifiedDate").gte(startDate).lte(endDate));
				countquery.addCriteria(Criteria.where("modifiedDate").gte(startDate).lte(endDate));
			}
			ServiceRequestHistoryResponse response = new ServiceRequestHistoryResponse();
			countquery.addCriteria(Criteria.where("activeFlag").is(Boolean.TRUE));
			query.addCriteria(Criteria.where("activeFlag").is(Boolean.TRUE))
					.with(Sort.by(Direction.DESC, "modifiedDate")).skip(offset > 0 ? ((offset - 1) * pagesize) : 0)
					.limit(pagesize);
			if(orgName != null){
				countquery.addCriteria(Criteria.where("organization").is(orgName));
				query.addCriteria(Criteria.where("organization").is(orgName));
			}
			if(status != null){
				countquery.addCriteria(Criteria.where("status").is(status));
				query.addCriteria(Criteria.where("status").is(status));
			}
			if(name != null){
				countquery.addCriteria(Criteria.where("name").regex(name));
				query.addCriteria(Criteria.where("name").regex(name));
			}
			response.setData(mongoTemplate.find(query, ProductBundle.class));
			Pagination pagination = new Pagination();
			Long counter;
			counter = mongoTemplate.count(countquery, ProductBundle.class);
			pagination.setOffset(offset);
			pagination.setTotal(counter);
			pagination.setPageSize(pagesize);
			response.setPagination(pagination);
			return response;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public boolean changeProductBundleStatus(ProductBundle productBundle, User user)
			throws ItorixException, ParseException, MessagingException, JsonProcessingException {

		Query query = new Query(Criteria.where("name").is(productBundle.getName())
				.and("organization").is(productBundle.getOrganization()).and("activeFlag").is(Boolean.TRUE));
		List<ProductBundle> productBundles = mongoTemplate.find(query,ProductBundle.class);
		if (productBundles.size() > 0) {
			ProductBundle bundle = productBundles.get(0);
			if (productBundle.getStatus().equals("Review")) {
				if (bundle.getStatus().equals("Change Required"))
					bundle.setStatus(productBundle.getStatus());
				else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			} else if (productBundle.getStatus().equals("Approved")) {
				if (bundle.getStatus().equals("Review")) {
					bundle.setStatus(productBundle.getStatus());
				} else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			} else if (productBundle.getStatus().equals("Change Required")) {
				if(bundle.getStatus().equals("Review")) {
					bundle.setStatus(productBundle.getStatus());
				}else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			}else if (productBundle.getStatus().equals("Rejected")) {
				if(bundle.getStatus().equals("Review")
						|| bundle.getStatus().equals("Change Required")) {
					bundle.setStatus(productBundle.getStatus());
				}else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			}
			String id = updateProductBundleStatus(bundle,user);
			Update update = new Update();
			Query updateQuery = new Query(Criteria.where("_id").is(productBundle.getId()));
			update.set("status",productBundle.getStatus());
			if(id != null) {
				update.set("apigeeProductBundleId", id);
			}
			mongoTemplate.updateFirst(query, update, ProductBundle.class);
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1029"), "Configuration-1029");
		}
		return false;
	}


	public String updateProductBundleStatus(ProductBundle bundle,User user)
			throws ItorixException, MessagingException, JsonProcessingException, ParseException {
		if(bundle.getStatus().equalsIgnoreCase("Approved")){
			if(bundle.getProducts() != null && !bundle.getProducts().isEmpty()) {
				for (String product : bundle.getProducts()) {
					Query query = new Query(Criteria.where("name").is(product).and("type").is("Product").
							and("activeFlag").is(true).and("status").is("Approved"));
					ServiceRequest products = mongoTemplate.findOne(query,ServiceRequest.class);
					if(bundle.getProductTRP().get(product) != null) {
						products.setTransactionRecordingPolicy(bundle.getProductTRP().get(product));
						updateServiceRequestInApigee(products.get_id(), products, user);
					}
				}
			}
			if(!bundle.getName().isEmpty()){
				ProductBundleConfig productBundleConfig = new ProductBundleConfig();
				productBundleConfig.setName(bundle.getName());
				productBundleConfig.setDisplayName(bundle.getName());
				productBundleConfig.setDescription(bundle.getName());
				productBundleConfig.setStatus(Status.CREATED);
				productBundleConfig.setProduct(bundle.getProducts() != null?bundle.getProducts().stream().collect(Collectors.toList()) : null);
				productBundleConfig.setOrganization(bundle.getOrganization());
				if(bundle.getApigeeProductBundleId() != null){
					productBundleConfig.setId(bundle.getApigeeProductBundleId());
					configManagementDao.createApigeeProductBundle(productBundleConfig,true);
				}else{
					Object body = configManagementDao.createApigeeProductBundle(productBundleConfig,false);
					JSONParser parser = new JSONParser();
					JSONObject json = (JSONObject) parser.parse(body.toString());
					String id = json.get("id").toString();
					return id;
				}
			}
		}
		return null;
	}

	public Object getDeveloperCategories(String orgName,String timerange,String status,int offset,int pagesize,String name)
			throws ItorixException {
		try {
			Query query=new Query();
			Query countquery = new Query();
			if(timerange!=null){
				SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
				String[] timeRanges = timerange.split("~");
				Date startDate=new Date(format.parse(timeRanges[0]).getTime());
				Date endDate=DateUtil.getEndOfDay(new Date(format.parse(timeRanges[1]).getTime()));
				query.addCriteria(Criteria.where("modifiedDate").gte(startDate).lte(endDate));
				countquery.addCriteria(Criteria.where("modifiedDate").gte(startDate).lte(endDate));
			}
			ServiceRequestHistoryResponse response = new ServiceRequestHistoryResponse();
			countquery.addCriteria(Criteria.where("activeFlag").is(Boolean.TRUE));
			query.addCriteria(Criteria.where("activeFlag").is(Boolean.TRUE))
					.with(Sort.by(Direction.DESC, "modifiedDate")).skip(offset > 0 ? ((offset - 1) * pagesize) : 0)
					.limit(pagesize);
			if(orgName != null){
				countquery.addCriteria(Criteria.where("organization").is(orgName));
				query.addCriteria(Criteria.where("organization").is(orgName));
			}
			if(status != null){
				countquery.addCriteria(Criteria.where("status").is(status));
				query.addCriteria(Criteria.where("status").is(status));
			}
			if(name != null){
				countquery.addCriteria(Criteria.where("name").regex(name));
				query.addCriteria(Criteria.where("name").regex(name));
			}
			response.setData(mongoTemplate.find(query, DeveloperCategory.class));
			Pagination pagination = new Pagination();
			Long counter;
			counter = mongoTemplate.count(countquery, DeveloperCategory.class);
			pagination.setOffset(offset);
			pagination.setTotal(counter);
			pagination.setPageSize(pagesize);
			response.setPagination(pagination);
			return response;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public DeveloperCategory createDeveloperCategory(DeveloperCategory developerCategory)
			throws ItorixException, ParseException {

		boolean isAnyRequestPending = false;
		if (developerCategory != null && developerCategory.getName() != null &&
				developerCategory.getOrganization() != null) {
			List<DeveloperCategory> developerCategories = mongoTemplate.find(
					Query.query(Criteria.where("organization")
							.is(developerCategory.getOrganization()).and("name").is(developerCategory.getName())),
					DeveloperCategory.class);
			if (developerCategories.size() == 0) {
				developerCategory = mongoTemplate.save(developerCategory);
				return developerCategory;
			} else {
				if (developerCategories.size() > 0) {
					for (DeveloperCategory category : developerCategories) {
						if (category.getStatus().equalsIgnoreCase("Review")) {
							isAnyRequestPending = true;
						}
					}
					if (!isAnyRequestPending) {
						Query query = new Query(
								Criteria.where("organization").is(developerCategory.getOrganization()).and("name")
										.is(developerCategory.getName()));
						Update update = new Update();
						update.set("activeFlag", Boolean.FALSE);
						UpdateResult result = mongoTemplate.updateMulti(query, update, DeveloperCategory.class);
						developerCategory.setActiveFlag(Boolean.TRUE);
						developerCategory = mongoTemplate.save(developerCategory);
						return developerCategory;
					} else {
						throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1026"),
								"Configuration-1026");
					}
				}
			}
		}else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1028"),
					"Configuration-1028");
		}
		return null;
	}

	public String updateDeveloperCategory(DeveloperCategory developerCategory) throws ItorixException {
		try{
			Query query = null;
			List<DeveloperCategory> developerCategories = mongoTemplate.find(Query.query(Criteria.where("organization").is(developerCategory.getOrganization()).
							and("name").is(developerCategory.getName()).and("activeFlag").is(Boolean.TRUE)),
					DeveloperCategory.class);
			if (developerCategories.size() > 0) {
				if (developerCategory != null && developerCategory.getName() != null &&
						developerCategory.getOrganization() != null) {
					query = new Query(Criteria.where("organization").is(developerCategory.getOrganization()).and("name")
							.is(developerCategory.getName()));
				}
				Update update = new Update();
				update.set("activeFlag", Boolean.FALSE);
				UpdateResult result = mongoTemplate.updateMulti(query, update, DeveloperCategory.class);
			}
			return mongoTemplate.insert(developerCategory).getId();
		}catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public void deleteDeveloperCategory(String categoryId) throws ItorixException {
		Query query = new Query(Criteria.where("_id").is(categoryId).and("status").ne("Approved"));
		DeveloperCategory category  = mongoTemplate.findOne(query,DeveloperCategory.class);
		if(category == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1034"),
					"Configuration-1034");
		}
		mongoTemplate.remove(category);
	}


	public boolean changeDeveloperCategoryStatus(DeveloperCategory developerCategory)
			throws ItorixException, ParseException {

		Query query = new Query(Criteria.where("name").is(developerCategory.getName())
				.and("organization").is(developerCategory.getOrganization()).and("activeFlag").is(Boolean.TRUE));
		List<DeveloperCategory> developerCategories = mongoTemplate.find(query,DeveloperCategory.class);
		if (developerCategories.size() > 0) {
			DeveloperCategory category = developerCategories.get(0);
			if (developerCategory.getStatus().equals("Review")) {
				if (category.getStatus().equals("Change Required"))
					category.setStatus(developerCategory.getStatus());
				else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			} else if (developerCategory.getStatus().equals("Approved")) {
				if (category.getStatus().equals("Review")) {
					category.setStatus(developerCategory.getStatus());
				} else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			} else if (developerCategory.getStatus().equals("Change Required")) {
				if(category.getStatus().equals("Review")) {
					category.setStatus(developerCategory.getStatus());
				}else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			}else if (developerCategory.getStatus().equals("Rejected")) {
				if(category.getStatus().equals("Review")
						|| category.getStatus().equals("Change Required")) {
					category.setStatus(developerCategory.getStatus());
				}else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			}
			String id = updateDeveloperCategoryStatus(category);
			Update update = new Update();
			Query updateQuery = new Query(Criteria.where("_id").is(developerCategory.getId()));
			update.set("status",developerCategory.getStatus());
			if(id != null) {
				update.set("apigeeDeveloperCategoryId", id);
			}
			mongoTemplate.updateFirst(query, update, DeveloperCategory.class);
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1029"), "Configuration-1029");
		}
		return false;
	}


	public String updateDeveloperCategoryStatus(DeveloperCategory developerCategory)
			throws ItorixException, ParseException {
		if(developerCategory.getStatus().equalsIgnoreCase("Approved")){
			if(!developerCategory.getName().isEmpty()){
				DeveloperCategoryConfig developerCategoryConfig = new DeveloperCategoryConfig();
				developerCategoryConfig.setName(developerCategory.getName());
				developerCategoryConfig.setDescription(developerCategory.getDescription());
				if(developerCategory.getApigeeDeveloperCategoryId() != null){
					developerCategoryConfig.setId(developerCategory.getApigeeDeveloperCategoryId());
					configManagementDao.createApigeeDeveloperCategory(developerCategoryConfig,developerCategory.getOrganization(),true);
				}else{
					Object body = configManagementDao.createApigeeDeveloperCategory(developerCategoryConfig,developerCategory.getOrganization(),false);
					JSONParser parser = new JSONParser();
					JSONObject json = (JSONObject) parser.parse(body.toString());
					String id = json.get("id").toString();
					return id;
				}
			}
		}
		return null;
	}

	public Company createCompany(Company company) throws ItorixException, ParseException {

		boolean isAnyRequestPending = false;
		if (company != null && company.getName() != null &&
				company.getOrganization() != null) {
			List<Company> companies = mongoTemplate.find(Query.query(Criteria.where("organization").is(company.getOrganization()).
					and("name").is(company.getName())), Company.class);
			if (companies.size() == 0) {
				company = mongoTemplate.save(company);
				return company;
			} else {
				if (companies.size() > 0) {
					for (Company company1 : companies) {
						if (company1.getStatus().equalsIgnoreCase("Review")) {
							isAnyRequestPending = true;
						}
					}
					if (!isAnyRequestPending) {
						Query query =  new Query(Criteria.where("organization").is(company.getOrganization()).and("name")
								.is(company.getName()));
						Update update = new Update();
						update.set("activeFlag", Boolean.FALSE);
						UpdateResult result = mongoTemplate.updateMulti(query, update, Company.class);
						company.setActiveFlag(Boolean.TRUE);
						company = mongoTemplate.save(company);
						return company;
					} else {
						throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1026"),
								"Configuration-1026");
					}
				}
			}
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1028"),
					"Configuration-1028");
		}
		return null;
	}

	public String updateCompany(Company company) throws ItorixException {
		try{
			Query query = null;
			List<Company> companies = mongoTemplate.find(Query.query(Criteria.where("organization").is(company.getOrganization()).
							and("name").is(company.getName()).and("activeFlag").is(Boolean.TRUE)),
					Company.class);
			if (companies.size() > 0) {
				if (company != null && company.getName() != null &&
						company.getOrganization() != null) {
					query = new Query(Criteria.where("organization").is(company.getOrganization()).and("name")
							.is(company.getName()));
				}
				Update update = new Update();
				update.set("activeFlag", Boolean.FALSE);
				UpdateResult result = mongoTemplate.updateMulti(query, update, Company.class);
			}
			return mongoTemplate.insert(company).getId();
		}catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public void deleteCompany(String companyId) throws ItorixException {
		Query query = new Query(Criteria.where("_id").is(companyId).and("status").ne("Approved"));
		Company company  = mongoTemplate.findOne(query,Company.class);
		if(company == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1034"),
					"Configuration-1034");
		}
		mongoTemplate.remove(company);
	}

	public Object getCompanies(String orgName,String timerange,String status,int offset,int pagesize,String name)
			throws ItorixException {
		try {
			Query query=new Query();
			Query countquery = new Query();
			if(timerange!=null){
				SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
				String[] timeRanges = timerange.split("~");
				Date startDate=new Date(format.parse(timeRanges[0]).getTime());
				Date endDate=DateUtil.getEndOfDay(new Date(format.parse(timeRanges[1]).getTime()));
				query.addCriteria(Criteria.where("modifiedDate").gte(startDate).lte(endDate));
				countquery.addCriteria(Criteria.where("modifiedDate").gte(startDate).lte(endDate));
			}
			ServiceRequestHistoryResponse response = new ServiceRequestHistoryResponse();
			countquery.addCriteria(Criteria.where("activeFlag").is(Boolean.TRUE));
			query.addCriteria(Criteria.where("activeFlag").is(Boolean.TRUE))
					.with(Sort.by(Direction.DESC, "modifiedDate")).skip(offset > 0 ? ((offset - 1) * pagesize) : 0)
					.limit(pagesize);
			if(orgName != null){
				countquery.addCriteria(Criteria.where("organization").is(orgName));
				query.addCriteria(Criteria.where("organization").is(orgName));
			}
			if(status != null){
				countquery.addCriteria(Criteria.where("status").is(status));
				query.addCriteria(Criteria.where("status").is(status));
			}
			if(name != null){
				countquery.addCriteria(Criteria.where("name").regex(name));
				query.addCriteria(Criteria.where("name").regex(name));
			}
			response.setData(mongoTemplate.find(query, Company.class));
			Pagination pagination = new Pagination();
			Long counter;
			counter = mongoTemplate.count(countquery, Company.class);
			pagination.setOffset(offset);
			pagination.setTotal(counter);
			pagination.setPageSize(pagesize);
			response.setPagination(pagination);
			return response;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public boolean changeCompanyStatus(Company company)
			throws ItorixException, ParseException, JsonProcessingException {

		Query query = new Query(Criteria.where("name").is(company.getName())
				.and("organization").is(company.getOrganization()).and("activeFlag").is(Boolean.TRUE));
		List<Company> companies = mongoTemplate.find(query,Company.class);
		if (companies.size() > 0) {
			Company comp = companies.get(0);
			if (company.getStatus().equals("Review")) {
				if (comp.getStatus().equals("Change Required"))
					comp.setStatus(company.getStatus());
				else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			} else if (company.getStatus().equals("Approved")) {
				if (comp.getStatus().equals("Review")) {
					comp.setStatus(company.getStatus());
				} else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			} else if (company.getStatus().equals("Change Required")) {
				if(comp.getStatus().equals("Review")) {
					comp.setStatus(company.getStatus());
				}else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			}else if (company.getStatus().equals("Rejected")) {
				if(comp.getStatus().equals("Review")
						|| comp.getStatus().equals("Change Required")) {
					comp.setStatus(company.getStatus());
				}else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			}
			updateCompanyStatus(comp);
			Update update = new Update();
			Query updateQuery = new Query(Criteria.where("_id").is(company.getId()));
			update.set("status",company.getStatus());
			update.set("apigeeCompanyName",company.getName());
			mongoTemplate.updateFirst(query, update, Company.class);
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1029"), "Configuration-1029");
		}
		return false;
	}


	public String updateCompanyStatus(Company company)
			throws ItorixException, ParseException, JsonProcessingException {
		if(company.getStatus().equalsIgnoreCase("Approved")){
			if(!company.getName().isEmpty()){
				CompanyConfig companyConfig = new CompanyConfig();
				createAttributesForCompany(companyConfig,company);
				if(company.getApigeeCompanyName() != null){
					configManagementDao.createApigeeCompany(companyConfig,company.getOrganization(),true);
				}else{
					configManagementDao.createApigeeCompany(companyConfig,company.getOrganization(),false);
				}
				if(company.getDevelopers() != null) {
					configManagementDao.addDevelopersToCompany(companyConfig.getName(),
							company.getOrganization(),company.getDevelopers());
				}
			}
		}
		return null;
	}

	private void createAttributesForCompany(CompanyConfig companyConfig,Company company)
			throws JsonProcessingException {
		companyConfig.setName(company.getName());
		companyConfig.setDisplayName(company.getName());
		List<Attribute> attributes = new ArrayList<>();
		if(company.getAdministrator() != null){
			Attribute adminAttributes = new Attribute();
			adminAttributes.setName("ADMIN_EMAIL");
			adminAttributes.setValue(company.getAdministrator());
			attributes.add(adminAttributes);
		}
		if(company.getDeveloperCategory() != null){
			Attribute developerCategory = new Attribute();
			developerCategory.setName("MINT_DEVELOPER_CATEGORY");
			developerCategory.setValue(company.getDeveloperCategory());
			attributes.add(developerCategory);
		}
		if(company.getTelephone() != null){
			Attribute telephone = new Attribute();
			telephone.setName("MINT_DEVELOPER_PHONE");
			telephone.setValue(company.getTelephone());
			attributes.add(telephone);
		}
		if(company.getBillingDetails().getBillingType() != null){
			Attribute billingType = new Attribute();
			billingType.setName("MINT_BILLING_TYPE");
			billingType.setValue(String.valueOf(company.getBillingDetails().getBillingType()));
			attributes.add(billingType);
		}
		if(company.getBillingDetails().getRegistationId() != null){
			Attribute registrationId = new Attribute();
			registrationId.setName("MINT_REGISTRATION_ID");
			registrationId.setValue(company.getBillingDetails().getRegistationId());
			attributes.add(registrationId);
		}
		if(company.getBillingDetails().getTaxExemptAuthNo() != null){
			Attribute taxExemptAuthNo = new Attribute();
			taxExemptAuthNo.setName("MINT_TAX_EXEMPT_AUTH_NO");
			taxExemptAuthNo.setValue(company.getBillingDetails().getTaxExemptAuthNo());
			attributes.add(taxExemptAuthNo);
		}
		if(!company.getAttributes().isEmpty()){
			for (var entry : company.getAttributes().entrySet()) {
				Attribute customAttribute = new Attribute();
				customAttribute.setName(entry.getKey());
				customAttribute.setValue(entry.getValue());
				attributes.add(customAttribute);
			}
		}
		if(company.getAddress1() != null){
			Attribute address = new Attribute();
			address.setName("MINT_DEVELOPER_ADDRESS");
			StringBuilder builder = new StringBuilder();
			builder.append("{");
			builder.append("\"address1\":\""+company.getAddress1()+"\"");
			if(company.getAddress2() != null){
				builder.append(",\"address2\":\""+company.getAddress2()+"\"");
			}
			if(company.getCity() != null){
				builder.append(",\"city\":\""+company.getCity()+"\"");
			}
			if(company.getState() != null){
				builder.append(",\"state\":\""+company.getState()+"\"");
			}
			if(company.getCountry() != null){
				List<CountryMetaData> countryMetaData = getCountryMetaData();
				String code = "";
				for(CountryMetaData country : countryMetaData) {
					if (country.getName().equalsIgnoreCase(company.getCountry())) {
						code = country.getCode();
						break;
					}
				}
				if(code != "") {
					builder.append(",\"country\":\"" + code + "\"");
				}else{
					builder.append(",\"country\":\"" + company.getCountry() + "\"");
				}
			}
			if(company.getPostalCode() != null){
				builder.append(",\"zip\":\""+company.getPostalCode()+"\"");
			}
			builder.append("}");
			address.setValue(builder.toString());
			attributes.add(address);
		}
		companyConfig.setAttributes(attributes);
	}

	public Object getApigeeDevelopers(String org) throws ItorixException, ParseException {
		return configManagementDao.getApigeeDevelopers(org);
	}

	public Webhook createWebhook(Webhook webhook) throws ItorixException, ParseException {

		boolean isAnyRequestPending = false;
		if (webhook != null && webhook.getName() != null &&
				webhook.getOrganization() != null) {
			List<Webhook> companies = mongoTemplate.find(Query.query(Criteria.where("organization").is(webhook.getOrganization()).
					and("name").is(webhook.getName())), Webhook.class);
			if (companies.size() == 0) {
				webhook = mongoTemplate.save(webhook);
				return webhook;
			} else {
				if (companies.size() > 0) {
					for (Webhook webhook1 : companies) {
						if (webhook1.getStatus().equalsIgnoreCase("Review")) {
							isAnyRequestPending = true;
						}
					}
					if (!isAnyRequestPending) {
						Query query =  new Query(Criteria.where("organization").is(webhook.getOrganization()).and("name")
								.is(webhook.getName()));
						Update update = new Update();
						update.set("activeFlag", Boolean.FALSE);
						UpdateResult result = mongoTemplate.updateMulti(query, update, Webhook.class);
						webhook.setActiveFlag(Boolean.TRUE);
						webhook = mongoTemplate.save(webhook);
						return webhook;
					} else {
						throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1026"),
								"Configuration-1026");
					}
				}
			}
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1028"),
					"Configuration-1028");
		}
		return null;
	}

	public String updateWebhook(Webhook webhook) throws ItorixException {
		try{
			Query query = null;
			List<Webhook> webhooks = mongoTemplate.find(Query.query(Criteria.where("organization").is(webhook.getOrganization()).
							and("name").is(webhook.getName()).and("activeFlag").is(Boolean.TRUE)),
					Webhook.class);
			if (webhooks.size() > 0) {
				if (webhook != null && webhook.getName() != null &&
						webhook.getOrganization() != null) {
					query = new Query(Criteria.where("organization").is(webhook.getOrganization()).and("name")
							.is(webhook.getName()));
				}
				Update update = new Update();
				update.set("activeFlag", Boolean.FALSE);
				UpdateResult result = mongoTemplate.updateMulti(query, update, Webhook.class);
			}
			return mongoTemplate.insert(webhook).getId();
		}catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public void deleteWebhook(String webhookId) throws ItorixException {
		Query query = new Query(Criteria.where("_id").is(webhookId).and("status").ne("Approved"));
		Webhook webhook  = mongoTemplate.findOne(query,Webhook.class);
		if(webhook == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1034"),
					"Configuration-1034");
		}
		mongoTemplate.remove(webhook);
	}

	public boolean changeWebhookStatus(Webhook webhook)
			throws ItorixException, ParseException {

		Query query = new Query(Criteria.where("name").is(webhook.getName())
				.and("organization").is(webhook.getOrganization()).and("activeFlag").is(Boolean.TRUE));
		List<Webhook> webhooks = mongoTemplate.find(query,Webhook.class);
		if (webhooks.size() > 0) {
			Webhook web = webhooks.get(0);
			if (webhook.getStatus().equals("Review")) {
				if (web.getStatus().equals("Change Required"))
					web.setStatus(webhook.getStatus());
				else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			} else if (webhook.getStatus().equals("Approved")) {
				if (web.getStatus().equals("Review")) {
					web.setStatus(webhook.getStatus());
				} else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			} else if (webhook.getStatus().equals("Change Required")) {
				if(web.getStatus().equals("Review")) {
					web.setStatus(webhook.getStatus());
				}else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			}else if (webhook.getStatus().equals("Rejected")) {
				if(web.getStatus().equals("Review")
						|| web.getStatus().equals("Change Required")) {
					web.setStatus(webhook.getStatus());
				}else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			}
			String id = updateWebhookStatus(web);
			Update update = new Update();
			Query updateQuery = new Query(Criteria.where("_id").is(webhook.getId()));
			update.set("status",webhook.getStatus());
			if(id != null) {
				update.set("apigeeWebhookId", id);
			}
			mongoTemplate.updateFirst(query, update, Webhook.class);
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1029"), "Configuration-1029");
		}
		return false;
	}

	public String updateWebhookStatus(Webhook webhook)
			throws ItorixException, ParseException {
		if(webhook.getStatus().equalsIgnoreCase("Approved")){
			if(!webhook.getName().isEmpty()){
				WebhookConfig webhookConfig = new WebhookConfig();
				webhookConfig.setName(webhook.getName());
				webhookConfig.setPostUrl(webhook.getUrl());
				if(webhook.getApigeeWebhookId() != null){
					webhookConfig.setId(webhook.getApigeeWebhookId());
					configManagementDao.createApigeeWebhook(webhookConfig,webhook.getOrganization(),true);
				}else{
					Object body = configManagementDao.createApigeeWebhook(webhookConfig,webhook.getOrganization(),false);
					JSONParser parser = new JSONParser();
					JSONObject json = (JSONObject) parser.parse(body.toString());
					String id = json.get("id").toString();
					return id;
				}
			}
		}
		return null;
	}

	public Object getWebhooks(String orgName,String timerange,String status,int offset,int pagesize,String name)
			throws ItorixException {
		try {
			Query query=new Query();
			Query countquery = new Query();
			if(timerange!=null){
				SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
				String[] timeRanges = timerange.split("~");
				Date startDate=new Date(format.parse(timeRanges[0]).getTime());
				Date endDate=DateUtil.getEndOfDay(new Date(format.parse(timeRanges[1]).getTime()));
				query.addCriteria(Criteria.where("modifiedDate").gte(startDate).lte(endDate));
				countquery.addCriteria(Criteria.where("modifiedDate").gte(startDate).lte(endDate));
			}
			ServiceRequestHistoryResponse response = new ServiceRequestHistoryResponse();
			countquery.addCriteria(Criteria.where("activeFlag").is(Boolean.TRUE));

			query.addCriteria(Criteria.where("activeFlag").is(Boolean.TRUE))
					.with(Sort.by(Direction.DESC, "modifiedDate")).skip(offset > 0 ? ((offset - 1) * pagesize) : 0)
					.limit(pagesize);
			if(orgName != null){
				countquery.addCriteria(Criteria.where("organization").is(orgName));
				query.addCriteria(Criteria.where("organization").is(orgName));
			}
			if(status != null){
				countquery.addCriteria(Criteria.where("status").is(status));
				query.addCriteria(Criteria.where("status").is(status));
			}
			if(name != null){
				countquery.addCriteria(Criteria.where("name").regex(name));
				query.addCriteria(Criteria.where("name").regex(name));
			}
			Pagination pagination = new Pagination();
			Long counter;
			response.setData(mongoTemplate.find(query, Webhook.class));
			counter = mongoTemplate.count(countquery, Webhook.class);
			pagination.setOffset(offset);
			pagination.setTotal(counter);
			pagination.setPageSize(pagesize);
			response.setPagination(pagination);
			return response;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	private void createTransactionRecordingPolicies(List<ProductAttributes> productAttributes, ServiceRequest serviceRequest)
			throws JsonProcessingException, ItorixException {
		if(serviceRequest.getTransactionRecordingPolicy() != null){
			if(serviceRequest.getTransactionRecordingPolicy().getSuccessCriteria() != null){
				ProductAttributes successCriteria = new ProductAttributes();
				successCriteria.setName("MINT_TRANSACTION_SUCCESS_CRITERIA");
				successCriteria.setValue(serviceRequest.getTransactionRecordingPolicy().getSuccessCriteria());
				productAttributes.add(successCriteria);
			}
			if(serviceRequest.getTransactionRecordingPolicy().getRefund() != null &&
					serviceRequest.getTransactionRecordingPolicy().getRefund().getSuccessCriteria() != null){
				ProductAttributes successCriteria = new ProductAttributes();
				successCriteria.setName("MINT_REFUND_TRANSACTION_SUCCESS_CRITERIA");
				successCriteria.setValue(serviceRequest.getTransactionRecordingPolicy().getRefund().getSuccessCriteria());
				productAttributes.add(successCriteria);
			}
			if(serviceRequest.getTransactionRecordingPolicy().getUseCustomAttributes() &&
					serviceRequest.getTransactionRecordingPolicy().getCustomAttributes() != null &&
					serviceRequest.getTransactionRecordingPolicy().getCustomAttributes().size() <= 10){
				for(int i =1 ; i <= serviceRequest.getTransactionRecordingPolicy().getCustomAttributes().size() ; i++) {
					ProductAttributes successCriteria = new ProductAttributes();
					successCriteria.setName("MINT_CUSTOM_ATTRIBUTE_"+String.valueOf(i));
					successCriteria.setValue(serviceRequest.getTransactionRecordingPolicy().getCustomAttributes().get(i-1).getVariableLocation()
							.getName());
					productAttributes.add(successCriteria);
				}
			}
//			else{
//				throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1034"),
//						"Configuration-1034");
//			}
			if(serviceRequest.getApiResources() != null){
				List<TransactionRecordingPoliciesObject> transactionRecordingPoliciesObjectsList = new ArrayList<>();
				for(String resource : serviceRequest.getApiResources()){
					TransactionRecordingPoliciesObject trpObject = new TransactionRecordingPoliciesObject();
					trpObject.setName(resource);
					Policies policies = new Policies();
					policies.setRequest(new ArrayList<>());
					ResponseObject responseObject = new ResponseObject();
					responseObject.setDisplayName("Transaction Policy");
					responseObject.setFaultRules(new ArrayList<>());
					responseObject.setName(resource);
					responseObject.setPolicyType("ExtractVariables");
					Source source = new Source();
					source.setClearPayload(false);
					source.setValue("response");
					responseObject.setSource(source);
					responseObject.setVariablePrefix("apigee");
					TransactionRecordingPolicy recordingPolicy = serviceRequest.getTransactionRecordingPolicy();
					JSONPayload jSONPayload;
					XMLPayload xMLPayload = new XMLPayload();
					List<Variable> VariableList;
					List<Extraction> extractionList = new ArrayList<>();
					if(recordingPolicy.getRefund() != null && recordingPolicy.getRefund().getResource().equals(resource)){
						jSONPayload = getRefundJsonPayload(recordingPolicy.getRefund());
						VariableList = getRefundXMLPayload(recordingPolicy.getRefund());
						extractionList = getRefundExtractionPayload(recordingPolicy.getRefund());
					}else{
						jSONPayload = getJsonPayload(recordingPolicy,resource);
						VariableList = getXMLPayload(recordingPolicy,resource);
						extractionList = getExtractionPayload(recordingPolicy,resource);
					}
					responseObject.setJSONPayload(jSONPayload);
					xMLPayload.setVariable(VariableList);
					xMLPayload.setNamespaces(new ArrayList<>());
					xMLPayload.setStopPayloadProcessing(false);
					responseObject.setXMLPayload(xMLPayload);
					responseObject.setExtractions(extractionList);
					List<Object> responseObjectLists = new ArrayList<>();
					String responseString = new ObjectMapper().writeValueAsString(responseObject);
					responseObjectLists.add(responseString);
					policies.setResponse(responseObjectLists);
					trpObject.setPolicies(policies);
					transactionRecordingPoliciesObjectsList.add(trpObject);
				}
				ProductAttributes transaction = new ProductAttributes();
				transaction.setName("transactionRecordingPolicies");
				String jsonString = new ObjectMapper().writeValueAsString(transactionRecordingPoliciesObjectsList);
				transaction.setValue(jsonString);
				productAttributes.add(transaction);
			}

		}
	}


	public JSONPayload getJsonPayload(TransactionRecordingPolicy policy,String resource){
		List<JSONPayload.Variable> variableList = new ArrayList<>();
		if(policy.getStatus() != null){
			MonetizationAttribute statusAttribute = policy.getStatus();
			if(statusAttribute.getResources().contains(resource)) {
				if (statusAttribute.getVariableLocation().getLocation().equals(Location.JSON_BODY)) {
					for (String variable : statusAttribute.getVariableLocation().getVariables()) {
						JSONPayload.Variable variables = new JSONPayload.Variable();
						variables.setName("mint.tx.status");
						variables.setJSONPath(Collections.singletonList(variable));
						variableList.add(variables);
					}
				}
			}
		}
		if(policy.getUseOptionalAttribute() == null){
			policy.setUseOptionalAttribute(Boolean.FALSE);
		}

		if(policy.getUseOptionalAttribute() && policy.getGrossPrice() != null){
			MonetizationAttribute grossPriceAttribute = policy.getGrossPrice();
			if(grossPriceAttribute.getResources().contains(resource)){
				if (grossPriceAttribute.getVariableLocation().getLocation().equals(Location.JSON_BODY)) {
					for (String variable : grossPriceAttribute.getVariableLocation().getVariables()) {
						JSONPayload.Variable variables = new JSONPayload.Variable();
						variables.setName("mint.tx.gross_price");
						variables.setJSONPath(Collections.singletonList(variable));
						variableList.add(variables);
					}
				}
			}
		}

		if(policy.getUseOptionalAttribute() && policy.getNetPrice() != null){
			MonetizationAttribute netPriceAttribute = policy.getNetPrice();
			if(netPriceAttribute.getResources().contains(resource)){
				if (netPriceAttribute.getVariableLocation().getLocation().equals(Location.JSON_BODY)) {
					for (String variable : netPriceAttribute.getVariableLocation().getVariables()) {
						JSONPayload.Variable variables = new JSONPayload.Variable();
						variables.setName("mint.tx.net_price");
						variables.setJSONPath(Collections.singletonList(variable));
						variableList.add(variables);
					}
				}
			}
		}

		if(policy.getUseOptionalAttribute() && policy.getCurrency() != null){
			MonetizationAttribute currencyAttribute = policy.getCurrency();
			if(currencyAttribute.getResources().contains(resource)){
				if (currencyAttribute.getVariableLocation().getLocation().equals(Location.JSON_BODY)) {
					for (String variable : currencyAttribute.getVariableLocation().getVariables()) {
						JSONPayload.Variable variables = new JSONPayload.Variable();
						variables.setName("mint.tx.currency");
						variables.setJSONPath(Collections.singletonList(variable));
						variableList.add(variables);
					}
				}
			}
		}

		if(policy.getUseOptionalAttribute() && policy.getTax() != null){
			MonetizationAttribute taxAttribute = policy.getTax();
			if(taxAttribute.getResources().contains(resource)){
				if (taxAttribute.getVariableLocation().getLocation().equals(Location.JSON_BODY)) {
					for (String variable : taxAttribute.getVariableLocation().getVariables()) {
						JSONPayload.Variable variables = new JSONPayload.Variable();
						variables.setName("mint.tx.tax");
						variables.setJSONPath(Collections.singletonList(variable));
						variableList.add(variables);
					}
				}
			}
		}

		if(policy.getUseOptionalAttribute() && policy.getErrorCode() != null){
			MonetizationAttribute errorCodeAttribute = policy.getErrorCode();
			if(errorCodeAttribute.getResources().contains(resource)){
				if (errorCodeAttribute.getVariableLocation().getLocation().equals(Location.JSON_BODY)) {
					for (String variable : errorCodeAttribute.getVariableLocation().getVariables()) {
						JSONPayload.Variable variables = new JSONPayload.Variable();
						variables.setName("mint.tx.error_code");
						variables.setJSONPath(Collections.singletonList(variable));
						variableList.add(variables);
					}
				}
			}
		}

		if(policy.getUseOptionalAttribute() && policy.getItemDescription() != null){
			MonetizationAttribute itemDescriptionAttribute = policy.getItemDescription();
			if(itemDescriptionAttribute.getResources().contains(resource)){
				if (itemDescriptionAttribute.getVariableLocation().getLocation().equals(Location.JSON_BODY)) {
					for (String variable : itemDescriptionAttribute.getVariableLocation().getVariables()) {
						JSONPayload.Variable variables = new JSONPayload.Variable();
						variables.setName("mint.tx.item_desc");
						variables.setJSONPath(Collections.singletonList(variable));
						variableList.add(variables);
					}
				}
			}
		}

		if(policy.getUseCustomAttributes() && policy.getCustomAttributes() != null){
			for(int i=1;i<=policy.getCustomAttributes().size();i++) {
				MonetizationAttribute customAttribute = policy.getCustomAttributes().get(i-1);
				if (customAttribute.getResources().contains(resource)) {
					if (customAttribute.getVariableLocation().getLocation().equals(Location.JSON_BODY)) {
						for (String variable : customAttribute.getVariableLocation().getVariables()) {
							JSONPayload.Variable variables = new JSONPayload.Variable();
							variables.setName("mint.tx.cust_att"+String.valueOf(i));
							variables.setJSONPath(Collections.singletonList(variable));
							variableList.add(variables);
						}
					}
				}
			}
		}

		if(policy.getResourceTxnLinks() != null){
			Map<String, ResponseVariableLocation> resourceTxnLinks = policy.getResourceTxnLinks();
			for (Map.Entry<String, ResponseVariableLocation> entry : resourceTxnLinks.entrySet()) {
				if(entry.getKey().equals(resource)) {
					if (entry.getValue().getLocation().equals(Location.JSON_BODY)) {
						for (String variable : entry.getValue().getVariables()) {
							JSONPayload.Variable variables = new JSONPayload.Variable();
							variables.setName("mint.tx.provider_tx_id");
							variables.setJSONPath(Collections.singletonList(variable));
							variableList.add(variables);
						}
					}
				}
			}
		}
		JSONPayload jsonPayload = new JSONPayload();
		jsonPayload.setVariable(variableList);

		return jsonPayload;
	}


	public List<Variable> getXMLPayload(TransactionRecordingPolicy policy,String resource){
		List<Variable> VariableList = new ArrayList<>();
		if(policy.getStatus() != null){
			MonetizationAttribute statusAttribute = policy.getStatus();
			if(statusAttribute.getResources().contains(resource)) {
				if (statusAttribute.getVariableLocation().getLocation().equals(Location.XML_BODY)) {
					for (String variable : statusAttribute.getVariableLocation().getVariables()) {
						Variable variables = new Variable();
						variables.setName("mint.tx.status");
						variables.setType("string");
						XPath xpath = new XPath();
						xpath.setValue(variable);
						variables.setXPath(Collections.singletonList(xpath));
						VariableList.add(variables);
					}
				}
			}
		}

		if(policy.getUseOptionalAttribute() && policy.getGrossPrice() != null){
			MonetizationAttribute grossPriceAttribute = policy.getGrossPrice();
			if(grossPriceAttribute.getResources().contains(resource)){
				if (grossPriceAttribute.getVariableLocation().getLocation().equals(Location.XML_BODY)) {
					for (String variable : grossPriceAttribute.getVariableLocation().getVariables()) {
						Variable variables = new Variable();
						variables.setName("mint.tx.gross_price");
						variables.setType("string");
						XPath xpath = new XPath();
						xpath.setValue(variable);
						variables.setXPath(Collections.singletonList(xpath));
						VariableList.add(variables);
					}
				}
			}
		}

		if(policy.getUseOptionalAttribute() && policy.getNetPrice() != null){
			MonetizationAttribute netPriceAttribute = policy.getNetPrice();
			if(netPriceAttribute.getResources().contains(resource)){
				if (netPriceAttribute.getVariableLocation().getLocation().equals(Location.XML_BODY)) {
					for (String variable : netPriceAttribute.getVariableLocation().getVariables()) {
						Variable variables = new Variable();
						variables.setName("mint.tx.net_price");
						variables.setType("string");
						XPath xpath = new XPath();
						xpath.setValue(variable);
						variables.setXPath(Collections.singletonList(xpath));
						VariableList.add(variables);
					}
				}
			}
		}

		if(policy.getUseOptionalAttribute() && policy.getCurrency() != null){
			MonetizationAttribute currencyAttribute = policy.getCurrency();
			if(currencyAttribute.getResources().contains(resource)){
				if (currencyAttribute.getVariableLocation().getLocation().equals(Location.XML_BODY)) {
					for (String variable : currencyAttribute.getVariableLocation().getVariables()) {
						Variable variables = new Variable();
						variables.setName("mint.tx.currency");
						variables.setType("string");
						XPath xpath = new XPath();
						xpath.setValue(variable);
						variables.setXPath(Collections.singletonList(xpath));
						VariableList.add(variables);
					}
				}
			}
		}

		if(policy.getUseOptionalAttribute() && policy.getTax() != null){
			MonetizationAttribute taxAttribute = policy.getTax();
			if(taxAttribute.getResources().contains(resource)){
				if (taxAttribute.getVariableLocation().getLocation().equals(Location.XML_BODY)) {
					for (String variable : taxAttribute.getVariableLocation().getVariables()) {
						Variable variables = new Variable();
						variables.setName("mint.tx.tax");
						variables.setType("string");
						XPath xpath = new XPath();
						xpath.setValue(variable);
						variables.setXPath(Collections.singletonList(xpath));
						VariableList.add(variables);
					}
				}
			}
		}

		if(policy.getUseOptionalAttribute() && policy.getErrorCode() != null){
			MonetizationAttribute errorCodeAttribute = policy.getErrorCode();
			if(errorCodeAttribute.getResources().contains(resource)){
				if (errorCodeAttribute.getVariableLocation().getLocation().equals(Location.XML_BODY)) {
					for (String variable : errorCodeAttribute.getVariableLocation().getVariables()) {
						Variable variables = new Variable();
						variables.setName("mint.tx.error_code");
						variables.setType("string");
						XPath xpath = new XPath();
						xpath.setValue(variable);
						variables.setXPath(Collections.singletonList(xpath));
						VariableList.add(variables);
					}
				}
			}
		}

		if(policy.getUseOptionalAttribute() && policy.getItemDescription() != null){
			MonetizationAttribute itemDescriptionAttribute = policy.getItemDescription();
			if(itemDescriptionAttribute.getResources().contains(resource)){
				if (itemDescriptionAttribute.getVariableLocation().getLocation().equals(Location.XML_BODY)) {
					for (String variable : itemDescriptionAttribute.getVariableLocation().getVariables()) {
						Variable variables = new Variable();
						variables.setName("mint.tx.item_desc");
						variables.setType("string");
						XPath xpath = new XPath();
						xpath.setValue(variable);
						variables.setXPath(Collections.singletonList(xpath));
						VariableList.add(variables);
					}
				}
			}
		}

		if(policy.getCustomAttributes() != null){
			for(int i=1;i<=policy.getCustomAttributes().size();i++) {
				MonetizationAttribute customAttribute = policy.getCustomAttributes().get(i-1);
				if (customAttribute.getResources().contains(resource)) {
					if (customAttribute.getVariableLocation().getLocation().equals(Location.XML_BODY)) {
						for (String variable : customAttribute.getVariableLocation().getVariables()) {
							Variable variables = new Variable();
							variables.setName("mint.tx.cust_att"+String.valueOf(i));
							variables.setType("string");
							XPath xpath = new XPath();
							xpath.setValue(variable);
							variables.setXPath(Collections.singletonList(xpath));
							VariableList.add(variables);
						}
					}
				}
			}
		}

		if(policy.getResourceTxnLinks() != null){
			Map<String, ResponseVariableLocation> resourceTxnLinks = policy.getResourceTxnLinks();
			for (Map.Entry<String, ResponseVariableLocation> entry : resourceTxnLinks.entrySet()) {
				if(entry.getKey().equals(resource)) {
					if (entry.getValue().getLocation().equals(Location.XML_BODY)) {
						for (String variable : entry.getValue().getVariables()) {
							Variable variables = new Variable();
							variables.setName("mint.tx.provider_tx_id");
							variables.setType("string");
							XPath xpath = new XPath();
							xpath.setValue(variable);
							variables.setXPath(Collections.singletonList(xpath));
							VariableList.add(variables);
						}
					}
				}
			}
		}

		return VariableList;
	}

	public List<Extraction> getExtractionPayload(TransactionRecordingPolicy policy,String resource){
		List<Extraction> extractionList = new ArrayList<>();
		if(policy.getStatus() != null){
			MonetizationAttribute statusAttribute = policy.getStatus();
			if(statusAttribute.getResources().contains(resource)) {
				Location location = statusAttribute.getVariableLocation().getLocation();
				if (location.equals(Location.HEADER) || location.equals(Location.FLOW)) {
					for (String variable : statusAttribute.getVariableLocation().getVariables()) {
						Extraction extraction = new Extraction();
						Pattern pattern = new Pattern();
						pattern.setIgnoreCase(true);
						pattern.setValue("{mint.tx.status}");
						if(location.equals(Location.HEADER)){
							Header header = new Header();
							header.setName(variable);
							header.setPattern(Collections.singletonList(pattern));
							extraction.setHeader(header);
							extractionList.add(extraction);
						}else{
							ResponseObject.Variable extractionVariable = new ResponseObject.Variable();
							extractionVariable.setName(variable);
							extractionVariable.setPattern(Collections.singletonList(pattern));
							extraction.setVariable(extractionVariable);
							extractionList.add(extraction);
						}
					}
				}
			}
		}

		if(policy.getUseOptionalAttribute() && policy.getGrossPrice() != null){
			MonetizationAttribute grossPriceAttribute = policy.getGrossPrice();
			if(grossPriceAttribute.getResources().contains(resource)){
				Location location = grossPriceAttribute.getVariableLocation().getLocation();
				if (location.equals(Location.HEADER) || location.equals(Location.FLOW)) {
					for (String variable : grossPriceAttribute.getVariableLocation().getVariables()) {
						Extraction extraction = new Extraction();
						Pattern pattern = new Pattern();
						pattern.setIgnoreCase(true);
						pattern.setValue("{mint.tx.gross_price}");
						if(location.equals(Location.HEADER)){
							Header header = new Header();
							header.setName(variable);
							header.setPattern(Collections.singletonList(pattern));
							extraction.setHeader(header);
							extractionList.add(extraction);
						}else{
							ResponseObject.Variable extractionVariable = new ResponseObject.Variable();
							extractionVariable.setName(variable);
							extractionVariable.setPattern(Collections.singletonList(pattern));
							extraction.setVariable(extractionVariable);
							extractionList.add(extraction);
						}
					}
				}
			}
		}

		if(policy.getUseOptionalAttribute() && policy.getNetPrice() != null){
			MonetizationAttribute netPriceAttribute = policy.getNetPrice();
			if(netPriceAttribute.getResources().contains(resource)){
				Location location = netPriceAttribute.getVariableLocation().getLocation();
				if (location.equals(Location.HEADER) || location.equals(Location.FLOW)) {
					for (String variable : netPriceAttribute.getVariableLocation().getVariables()) {
						Extraction extraction = new Extraction();
						Pattern pattern = new Pattern();
						pattern.setIgnoreCase(true);
						pattern.setValue("{mint.tx.net_price}");
						if(location.equals(Location.HEADER)){
							Header header = new Header();
							header.setName(variable);
							header.setPattern(Collections.singletonList(pattern));
							extraction.setHeader(header);
							extractionList.add(extraction);
						}else{
							ResponseObject.Variable extractionVariable = new ResponseObject.Variable();
							extractionVariable.setName(variable);
							extractionVariable.setPattern(Collections.singletonList(pattern));
							extraction.setVariable(extractionVariable);
							extractionList.add(extraction);
						}
					}
				}
			}
		}

		if(policy.getUseOptionalAttribute() && policy.getCurrency() != null){
			MonetizationAttribute currencyAttribute = policy.getCurrency();
			if(currencyAttribute.getResources().contains(resource)){
				Location location = currencyAttribute.getVariableLocation().getLocation();
				if (location.equals(Location.HEADER) || location.equals(Location.FLOW)) {
					for (String variable : currencyAttribute.getVariableLocation().getVariables()) {
						Extraction extraction = new Extraction();
						Pattern pattern = new Pattern();
						pattern.setIgnoreCase(true);
						pattern.setValue("{mint.tx.currency}");
						if(location.equals(Location.HEADER)){
							Header header = new Header();
							header.setName(variable);
							header.setPattern(Collections.singletonList(pattern));
							extraction.setHeader(header);
							extractionList.add(extraction);
						}else{
							ResponseObject.Variable extractionVariable = new ResponseObject.Variable();
							extractionVariable.setName(variable);
							extractionVariable.setPattern(Collections.singletonList(pattern));
							extraction.setVariable(extractionVariable);
							extractionList.add(extraction);
						}
					}
				}
			}
		}

		if(policy.getUseOptionalAttribute() && policy.getTax() != null){
			MonetizationAttribute taxAttribute = policy.getTax();
			if(taxAttribute.getResources().contains(resource)){
				Location location = taxAttribute.getVariableLocation().getLocation();
				if (location.equals(Location.HEADER) || location.equals(Location.FLOW)) {
					for (String variable : taxAttribute.getVariableLocation().getVariables()) {
						Extraction extraction = new Extraction();
						Pattern pattern = new Pattern();
						pattern.setIgnoreCase(true);
						pattern.setValue("{mint.tx.tax}");
						if(location.equals(Location.HEADER)){
							Header header = new Header();
							header.setName(variable);
							header.setPattern(Collections.singletonList(pattern));
							extraction.setHeader(header);
							extractionList.add(extraction);
						}else{
							ResponseObject.Variable extractionVariable = new ResponseObject.Variable();
							extractionVariable.setName(variable);
							extractionVariable.setPattern(Collections.singletonList(pattern));
							extraction.setVariable(extractionVariable);
							extractionList.add(extraction);
						}
					}
				}
			}
		}

		if(policy.getUseOptionalAttribute() && policy.getErrorCode() != null){
			MonetizationAttribute errorCodeAttribute = policy.getErrorCode();
			if(errorCodeAttribute.getResources().contains(resource)){
				Location location = errorCodeAttribute.getVariableLocation().getLocation();
				if (location.equals(Location.HEADER) || location.equals(Location.FLOW)) {
					for (String variable : errorCodeAttribute.getVariableLocation().getVariables()) {
						Extraction extraction = new Extraction();
						Pattern pattern = new Pattern();
						pattern.setIgnoreCase(true);
						pattern.setValue("{mint.tx.error_code}");
						if(location.equals(Location.HEADER)){
							Header header = new Header();
							header.setName(variable);
							header.setPattern(Collections.singletonList(pattern));
							extraction.setHeader(header);
							extractionList.add(extraction);
						}else{
							ResponseObject.Variable extractionVariable = new ResponseObject.Variable();
							extractionVariable.setName(variable);
							extractionVariable.setPattern(Collections.singletonList(pattern));
							extraction.setVariable(extractionVariable);
							extractionList.add(extraction);
						}
					}
				}
			}
		}

		if(policy.getUseOptionalAttribute() && policy.getItemDescription() != null){
			MonetizationAttribute itemDescriptionAttribute = policy.getItemDescription();
			if(itemDescriptionAttribute.getResources().contains(resource)){
				Location location = itemDescriptionAttribute.getVariableLocation().getLocation();
				if (location.equals(Location.HEADER) || location.equals(Location.FLOW)) {
					for (String variable : itemDescriptionAttribute.getVariableLocation().getVariables()) {
						Extraction extraction = new Extraction();
						Pattern pattern = new Pattern();
						pattern.setIgnoreCase(true);
						pattern.setValue("{mint.tx.item_desc}");
						if(location.equals(Location.HEADER)){
							Header header = new Header();
							header.setName(variable);
							header.setPattern(Collections.singletonList(pattern));
							extraction.setHeader(header);
							extractionList.add(extraction);
						}else{
							ResponseObject.Variable extractionVariable = new ResponseObject.Variable();
							extractionVariable.setName(variable);
							extractionVariable.setPattern(Collections.singletonList(pattern));
							extraction.setVariable(extractionVariable);
							extractionList.add(extraction);
						}
					}
				}
			}
		}

		if(policy.getUseCustomAttributes() && policy.getCustomAttributes() != null){
			for(int i=1;i<=policy.getCustomAttributes().size();i++) {
				MonetizationAttribute customAttribute = policy.getCustomAttributes().get(i-1);
				if (customAttribute.getResources().contains(resource)) {
					Location location = customAttribute.getVariableLocation().getLocation();
					if (location.equals(Location.HEADER) || location.equals(Location.FLOW)) {
						for (String variable : customAttribute.getVariableLocation().getVariables()) {
							Extraction extraction = new Extraction();
							Pattern pattern = new Pattern();
							pattern.setIgnoreCase(true);
							pattern.setValue("{mint.tx.cust_att"+String.valueOf(i)+"}");
							if(location.equals(Location.HEADER)){
								Header header = new Header();
								header.setName(variable);
								header.setPattern(Collections.singletonList(pattern));
								extraction.setHeader(header);
								extractionList.add(extraction);
							}else{
								ResponseObject.Variable extractionVariable = new ResponseObject.Variable();
								extractionVariable.setName(variable);
								extractionVariable.setPattern(Collections.singletonList(pattern));
								extraction.setVariable(extractionVariable);
								extractionList.add(extraction);
							}
						}
					}
				}
			}
		}

		if(policy.getResourceTxnLinks() != null){
			Map<String, ResponseVariableLocation> resourceTxnLinks = policy.getResourceTxnLinks();
			for (Map.Entry<String, ResponseVariableLocation> entry : resourceTxnLinks.entrySet()) {
				if(entry.getKey().equals(resource)) {
					Location location = entry.getValue().getLocation();
					if (location.equals(Location.HEADER) || location.equals(Location.FLOW)) {
						for (String variable : entry.getValue().getVariables()) {
							Extraction extraction = new Extraction();
							Pattern pattern = new Pattern();
							pattern.setIgnoreCase(true);
							pattern.setValue("{mint.tx.provider_tx_id}");
							if (location.equals(Location.HEADER)) {
								Header header = new Header();
								header.setName(variable);
								header.setPattern(Collections.singletonList(pattern));
								extraction.setHeader(header);
								extractionList.add(extraction);
							} else {
								ResponseObject.Variable extractionVariable = new ResponseObject.Variable();
								extractionVariable.setName(variable);
								extractionVariable.setPattern(Collections.singletonList(pattern));
								extraction.setVariable(extractionVariable);
								extractionList.add(extraction);
							}
						}
					}
				}
			}
		}

		return extractionList;
	}


	public JSONPayload getRefundJsonPayload(Refund refund){
		List<JSONPayload.Variable> variableList = new ArrayList<>();
		if(refund.getStatus() != null){
			ResponseVariableLocation statusAttribute = refund.getStatus();
			if (statusAttribute.getLocation().equals(Location.JSON_BODY)) {
				for (String variable : statusAttribute.getVariables()) {
					JSONPayload.Variable variables = new JSONPayload.Variable();
					variables.setName("mint.tx.status");
					variables.setJSONPath(Collections.singletonList(variable));
					variableList.add(variables);
				}
			}
		}

		if(refund.getParentId() != null){
			ResponseVariableLocation parentAttribute = refund.getStatus();
			if (parentAttribute.getLocation().equals(Location.JSON_BODY)) {
				for (String variable : parentAttribute.getVariables()) {
					JSONPayload.Variable variables = new JSONPayload.Variable();
					variables.setName("mint.tx.parent_id");
					variables.setJSONPath(Collections.singletonList(variable));
					variableList.add(variables);
				}
			}
		}

		if(refund.getUseOptionalRefundAttributes() && refund.getGrossPrice() != null){
			ResponseVariableLocation grossPriceAttribute = refund.getGrossPrice();
			if (grossPriceAttribute.getLocation().equals(Location.JSON_BODY)) {
				for (String variable : grossPriceAttribute.getVariables()) {
					JSONPayload.Variable variables = new JSONPayload.Variable();
					variables.setName("mint.tx.gross_price");
					variables.setJSONPath(Collections.singletonList(variable));
					variableList.add(variables);
				}
			}
		}

		if(refund.getUseOptionalRefundAttributes() && refund.getNetPrice() != null){
			ResponseVariableLocation netPriceAttribute = refund.getNetPrice();
			if (netPriceAttribute.getLocation().equals(Location.JSON_BODY)) {
				for (String variable : netPriceAttribute.getVariables()) {
					JSONPayload.Variable variables = new JSONPayload.Variable();
					variables.setName("mint.tx.net_price");
					variables.setJSONPath(Collections.singletonList(variable));
					variableList.add(variables);
				}
			}
		}

		if(refund.getUseOptionalRefundAttributes() && refund.getCurrency() != null){
			ResponseVariableLocation currencyAttribute = refund.getCurrency();
			if (currencyAttribute.getLocation().equals(Location.JSON_BODY)) {
				for (String variable : currencyAttribute.getVariables()) {
					JSONPayload.Variable variables = new JSONPayload.Variable();
					variables.setName("mint.tx.currency");
					variables.setJSONPath(Collections.singletonList(variable));
					variableList.add(variables);
				}
			}
		}

		if(refund.getUseOptionalRefundAttributes() && refund.getTax() != null){
			ResponseVariableLocation taxAttribute = refund.getTax();
			if (taxAttribute.getLocation().equals(Location.JSON_BODY)) {
				for (String variable : taxAttribute.getVariables()) {
					JSONPayload.Variable variables = new JSONPayload.Variable();
					variables.setName("mint.tx.tax");
					variables.setJSONPath(Collections.singletonList(variable));
					variableList.add(variables);
				}
			}
		}

		if(refund.getUseOptionalRefundAttributes() && refund.getErrorCode() != null){
			ResponseVariableLocation errorCodeAttribute = refund.getErrorCode();
			if (errorCodeAttribute.getLocation().equals(Location.JSON_BODY)) {
				for (String variable : errorCodeAttribute.getVariables()) {
					JSONPayload.Variable variables = new JSONPayload.Variable();
					variables.setName("mint.tx.error_code");
					variables.setJSONPath(Collections.singletonList(variable));
					variableList.add(variables);
				}
			}
		}

		if(refund.getUseOptionalRefundAttributes() && refund.getItemDescription() != null){
			ResponseVariableLocation itemDescriptionAttribute = refund.getItemDescription();
			if (itemDescriptionAttribute.getLocation().equals(Location.JSON_BODY)) {
				for (String variable : itemDescriptionAttribute.getVariables()) {
					JSONPayload.Variable variables = new JSONPayload.Variable();
					variables.setName("mint.tx.item_desc");
					variables.setJSONPath(Collections.singletonList(variable));
					variableList.add(variables);
				}
			}
		}
		JSONPayload jsonPayload = new JSONPayload();
		jsonPayload.setVariable(variableList);

		return jsonPayload;
	}


	public List<Variable> getRefundXMLPayload(Refund refund){
		List<Variable> VariableList = new ArrayList<>();
		if(refund.getStatus() != null){
			ResponseVariableLocation statusAttribute = refund.getStatus();
			if (statusAttribute.getLocation().equals(Location.XML_BODY)) {
				for (String variable : statusAttribute.getVariables()) {
					Variable variables = new Variable();
					variables.setName("mint.tx.status");
					variables.setType("string");
					XPath xpath = new XPath();
					xpath.setValue(variable);
					variables.setXPath(Collections.singletonList(xpath));
					VariableList.add(variables);
				}
			}
		}

		if(refund.getParentId() != null){
			ResponseVariableLocation parentAttribute = refund.getStatus();
			if (parentAttribute.getLocation().equals(Location.XML_BODY)) {
				for (String variable : parentAttribute.getVariables()) {
					Variable variables = new Variable();
					variables.setName("mint.tx.parent_id");
					variables.setType("string");
					XPath xpath = new XPath();
					xpath.setValue(variable);
					variables.setXPath(Collections.singletonList(xpath));
					VariableList.add(variables);
				}
			}
		}

		if(refund.getUseOptionalRefundAttributes() && refund.getGrossPrice() != null){
			ResponseVariableLocation grossPriceAttribute = refund.getGrossPrice();
			if (grossPriceAttribute.getLocation().equals(Location.XML_BODY)) {
				for (String variable : grossPriceAttribute.getVariables()) {
					Variable variables = new Variable();
					variables.setName("mint.tx.gross_price");
					variables.setType("string");
					XPath xpath = new XPath();
					xpath.setValue(variable);
					variables.setXPath(Collections.singletonList(xpath));
					VariableList.add(variables);
				}
			}
		}

		if(refund.getUseOptionalRefundAttributes() && refund.getNetPrice() != null){
			ResponseVariableLocation netPriceAttribute = refund.getNetPrice();
			if (netPriceAttribute.getLocation().equals(Location.XML_BODY)) {
				for (String variable : netPriceAttribute.getVariables()) {
					Variable variables = new Variable();
					variables.setName("mint.tx.net_price");
					variables.setType("string");
					XPath xpath = new XPath();
					xpath.setValue(variable);
					variables.setXPath(Collections.singletonList(xpath));
					VariableList.add(variables);
				}
			}
		}

		if(refund.getUseOptionalRefundAttributes() && refund.getCurrency() != null){
			ResponseVariableLocation currencyAttribute = refund.getCurrency();
			if (currencyAttribute.getLocation().equals(Location.XML_BODY)) {
				for (String variable : currencyAttribute.getVariables()) {
					Variable variables = new Variable();
					variables.setName("mint.tx.currency");
					variables.setType("string");
					XPath xpath = new XPath();
					xpath.setValue(variable);
					variables.setXPath(Collections.singletonList(xpath));
					VariableList.add(variables);
				}
			}
		}

		if(refund.getUseOptionalRefundAttributes() && refund.getTax() != null){
			ResponseVariableLocation taxAttribute = refund.getTax();
			if (taxAttribute.getLocation().equals(Location.XML_BODY)) {
				for (String variable : taxAttribute.getVariables()) {
					Variable variables = new Variable();
					variables.setName("mint.tx.tax");
					variables.setType("string");
					XPath xpath = new XPath();
					xpath.setValue(variable);
					variables.setXPath(Collections.singletonList(xpath));
					VariableList.add(variables);
				}
			}
		}

		if(refund.getUseOptionalRefundAttributes() && refund.getErrorCode() != null){
			ResponseVariableLocation errorCodeAttribute = refund.getErrorCode();
			if (errorCodeAttribute.getLocation().equals(Location.XML_BODY)) {
				for (String variable : errorCodeAttribute.getVariables()) {
					Variable variables = new Variable();
					variables.setName("mint.tx.error_code");
					variables.setType("string");
					XPath xpath = new XPath();
					xpath.setValue(variable);
					variables.setXPath(Collections.singletonList(xpath));
					VariableList.add(variables);
				}
			}
		}

		if(refund.getUseOptionalRefundAttributes() && refund.getItemDescription() != null){
			ResponseVariableLocation itemDescriptionAttribute = refund.getItemDescription();
			if (itemDescriptionAttribute.getLocation().equals(Location.XML_BODY)) {
				for (String variable : itemDescriptionAttribute.getVariables()) {
					Variable variables = new Variable();
					variables.setName("mint.tx.item_desc");
					variables.setType("string");
					XPath xpath = new XPath();
					xpath.setValue(variable);
					variables.setXPath(Collections.singletonList(xpath));
					VariableList.add(variables);
				}
			}
		}

		return VariableList;
	}

	public List<Extraction> getRefundExtractionPayload(Refund refund){
		List<Extraction> extractionList = new ArrayList<>();
		if(refund.getStatus() != null){
			ResponseVariableLocation statusAttribute = refund.getStatus();
			Location location = statusAttribute.getLocation();
			if (location.equals(Location.HEADER) || location.equals(Location.FLOW)) {
				for (String variable : statusAttribute.getVariables()) {
					Extraction extraction = new Extraction();
					Pattern pattern = new Pattern();
					pattern.setIgnoreCase(true);
					pattern.setValue("{mint.tx.status}");
					if(location.equals(Location.HEADER)){
						Header header = new Header();
						header.setName(variable);
						header.setPattern(Collections.singletonList(pattern));
						extraction.setHeader(header);
						extractionList.add(extraction);
					}else{
						ResponseObject.Variable extractionVariable = new ResponseObject.Variable();
						extractionVariable.setName(variable);
						extractionVariable.setPattern(Collections.singletonList(pattern));
						extraction.setVariable(extractionVariable);
						extractionList.add(extraction);
					}
				}
			}
		}

		if(refund.getParentId() != null){
			ResponseVariableLocation parentAttribute = refund.getStatus();
			Location location = parentAttribute.getLocation();
			if (location.equals(Location.HEADER) || location.equals(Location.FLOW)) {
				for (String variable : parentAttribute.getVariables()) {
					Extraction extraction = new Extraction();
					Pattern pattern = new Pattern();
					pattern.setIgnoreCase(true);
					pattern.setValue("{mint.tx.parent_id}");
					if(location.equals(Location.HEADER)){
						Header header = new Header();
						header.setName(variable);
						header.setPattern(Collections.singletonList(pattern));
						extraction.setHeader(header);
						extractionList.add(extraction);
					}else{
						ResponseObject.Variable extractionVariable = new ResponseObject.Variable();
						extractionVariable.setName(variable);
						extractionVariable.setPattern(Collections.singletonList(pattern));
						extraction.setVariable(extractionVariable);
						extractionList.add(extraction);
					}
				}
			}
		}

		if(refund.getUseOptionalRefundAttributes() && refund.getGrossPrice() != null){
			ResponseVariableLocation grossPriceAttribute = refund.getGrossPrice();
			Location location = grossPriceAttribute.getLocation();
			if (location.equals(Location.HEADER) || location.equals(Location.FLOW)) {
				for (String variable : grossPriceAttribute.getVariables()) {
					Extraction extraction = new Extraction();
					Pattern pattern = new Pattern();
					pattern.setIgnoreCase(true);
					pattern.setValue("{mint.tx.gross_price}");
					if(location.equals(Location.HEADER)){
						Header header = new Header();
						header.setName(variable);
						header.setPattern(Collections.singletonList(pattern));
						extraction.setHeader(header);
						extractionList.add(extraction);
					}else{
						ResponseObject.Variable extractionVariable = new ResponseObject.Variable();
						extractionVariable.setName(variable);
						extractionVariable.setPattern(Collections.singletonList(pattern));
						extraction.setVariable(extractionVariable);
						extractionList.add(extraction);
					}
				}
			}
		}

		if(refund.getUseOptionalRefundAttributes() && refund.getNetPrice() != null){
			ResponseVariableLocation netPriceAttribute = refund.getNetPrice();
			Location location = netPriceAttribute.getLocation();
			if (location.equals(Location.HEADER) || location.equals(Location.FLOW)) {
				for (String variable : netPriceAttribute.getVariables()) {
					Extraction extraction = new Extraction();
					Pattern pattern = new Pattern();
					pattern.setIgnoreCase(true);
					pattern.setValue("{mint.tx.net_price}");
					if(location.equals(Location.HEADER)){
						Header header = new Header();
						header.setName(variable);
						header.setPattern(Collections.singletonList(pattern));
						extraction.setHeader(header);
						extractionList.add(extraction);
					}else{
						ResponseObject.Variable extractionVariable = new ResponseObject.Variable();
						extractionVariable.setName(variable);
						extractionVariable.setPattern(Collections.singletonList(pattern));
						extraction.setVariable(extractionVariable);
						extractionList.add(extraction);
					}
				}
			}
		}

		if(refund.getUseOptionalRefundAttributes() && refund.getCurrency() != null){
			ResponseVariableLocation currencyAttribute = refund.getCurrency();
			Location location = currencyAttribute.getLocation();
			if (location.equals(Location.HEADER) || location.equals(Location.FLOW)) {
				for (String variable : currencyAttribute.getVariables()) {
					Extraction extraction = new Extraction();
					Pattern pattern = new Pattern();
					pattern.setIgnoreCase(true);
					pattern.setValue("{mint.tx.currency}");
					if(location.equals(Location.HEADER)){
						Header header = new Header();
						header.setName(variable);
						header.setPattern(Collections.singletonList(pattern));
						extraction.setHeader(header);
						extractionList.add(extraction);
					}else{
						ResponseObject.Variable extractionVariable = new ResponseObject.Variable();
						extractionVariable.setName(variable);
						extractionVariable.setPattern(Collections.singletonList(pattern));
						extraction.setVariable(extractionVariable);
						extractionList.add(extraction);
					}
				}
			}
		}

		if(refund.getUseOptionalRefundAttributes() && refund.getTax() != null){
			ResponseVariableLocation taxAttribute = refund.getTax();
			Location location = taxAttribute.getLocation();
			if (location.equals(Location.HEADER) || location.equals(Location.FLOW)) {
				for (String variable : taxAttribute.getVariables()) {
					Extraction extraction = new Extraction();
					Pattern pattern = new Pattern();
					pattern.setIgnoreCase(true);
					pattern.setValue("{mint.tx.tax}");
					if(location.equals(Location.HEADER)){
						Header header = new Header();
						header.setName(variable);
						header.setPattern(Collections.singletonList(pattern));
						extraction.setHeader(header);
						extractionList.add(extraction);
					}else{
						ResponseObject.Variable extractionVariable = new ResponseObject.Variable();
						extractionVariable.setName(variable);
						extractionVariable.setPattern(Collections.singletonList(pattern));
						extraction.setVariable(extractionVariable);
						extractionList.add(extraction);
					}
				}
			}
		}

		if(refund.getUseOptionalRefundAttributes() && refund.getErrorCode() != null){
			ResponseVariableLocation errorCodeAttribute = refund.getErrorCode();
			Location location = errorCodeAttribute.getLocation();
			if (location.equals(Location.HEADER) || location.equals(Location.FLOW)) {
				for (String variable : errorCodeAttribute.getVariables()) {
					Extraction extraction = new Extraction();
					Pattern pattern = new Pattern();
					pattern.setIgnoreCase(true);
					pattern.setValue("{mint.tx.error_code}");
					if(location.equals(Location.HEADER)){
						Header header = new Header();
						header.setName(variable);
						header.setPattern(Collections.singletonList(pattern));
						extraction.setHeader(header);
						extractionList.add(extraction);
					}else{
						ResponseObject.Variable extractionVariable = new ResponseObject.Variable();
						extractionVariable.setName(variable);
						extractionVariable.setPattern(Collections.singletonList(pattern));
						extraction.setVariable(extractionVariable);
						extractionList.add(extraction);
					}
				}
			}
		}

		if(refund.getUseOptionalRefundAttributes() && refund.getItemDescription() != null){
			ResponseVariableLocation itemDescriptionAttribute = refund.getItemDescription();
			Location location = itemDescriptionAttribute.getLocation();
			if (location.equals(Location.HEADER) || location.equals(Location.FLOW)) {
				for (String variable : itemDescriptionAttribute.getVariables()) {
					Extraction extraction = new Extraction();
					Pattern pattern = new Pattern();
					pattern.setIgnoreCase(true);
					pattern.setValue("{mint.tx.item_desc}");
					if(location.equals(Location.HEADER)){
						Header header = new Header();
						header.setName(variable);
						header.setPattern(Collections.singletonList(pattern));
						extraction.setHeader(header);
						extractionList.add(extraction);
					}else{
						ResponseObject.Variable extractionVariable = new ResponseObject.Variable();
						extractionVariable.setName(variable);
						extractionVariable.setPattern(Collections.singletonList(pattern));
						extraction.setVariable(extractionVariable);
						extractionList.add(extraction);
					}
				}
			}
		}

		return extractionList;
	}

	public Object monetizationConfigSearch(String type, String name, int limit) throws ItorixException {
		BasicQuery query = new BasicQuery("{\"name\": {$regex : '" + name + "', $options: 'i'}}");
		query.limit(limit > 0 ? limit : 10);
		query.addCriteria(Criteria.where("activeFlag").is(true));
		net.sf.json.JSONObject serviceRequestList = new net.sf.json.JSONObject();
		List<String> monetizationRequests ;
		if(type.equalsIgnoreCase("ProductBundle")){
			monetizationRequests	= mongoTemplate.findDistinct(query, "name", ProductBundle.class, String.class);
			Collections.sort(monetizationRequests);
			serviceRequestList.put("ProductBundles", monetizationRequests);
		}else if(type.equalsIgnoreCase("DeveloperCategory")){
			monetizationRequests	= mongoTemplate.findDistinct(query, "name", DeveloperCategory.class, String.class);
			Collections.sort(monetizationRequests);
			serviceRequestList.put("DeveloperCategory", monetizationRequests);
		}else if(type.equalsIgnoreCase("Company")){
			monetizationRequests	= mongoTemplate.findDistinct(query, "name", Company.class, String.class);
			Collections.sort(monetizationRequests);
			serviceRequestList.put("Company", monetizationRequests);
		}else if(type.equalsIgnoreCase("RatePlan")){
			monetizationRequests	= mongoTemplate.findDistinct(query, "name", RatePlan.class, String.class);
			Collections.sort(monetizationRequests);
			serviceRequestList.put("RatePlan", monetizationRequests);
		}else if(type.equalsIgnoreCase("Webhook")){
			monetizationRequests	= mongoTemplate.findDistinct(query, "name", Webhook.class, String.class);
			Collections.sort(monetizationRequests);
			serviceRequestList.put("Webhook", monetizationRequests);
		}
		return serviceRequestList;
	}

	public Object getMonetizationConfigHistory(String monetizationConfigType, String orgName, String name) throws ItorixException {
		try {
			Query query = new Query(Criteria.where("organization").is(orgName).and("name").is(name))
					.with(Sort.by(Direction.DESC, "modifiedDate"));
			List<Object> configList = new ArrayList<>();
			if(monetizationConfigType.equalsIgnoreCase("ProductBundle")){
				return mongoTemplate.find(query, ProductBundle.class);
			}else if(monetizationConfigType.equalsIgnoreCase("DeveloperCategory")){
				return mongoTemplate.find(query, DeveloperCategory.class);
			}else if(monetizationConfigType.equalsIgnoreCase("Company")){
				return mongoTemplate.find(query, Company.class);
			}else if(monetizationConfigType.equalsIgnoreCase("RatePlan")){
				return mongoTemplate.find(query, RatePlan.class);
			}else if(monetizationConfigType.equalsIgnoreCase("Webhook")){
				return mongoTemplate.find(query, Webhook.class);
			}
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
		return new ArrayList<>();
	}

	public Object getRatePlans(String orgName, String name,String status, String timerange, int offset, int pagesize)
			throws ItorixException {
		try {
			Query query=new Query();
			Query countquery = new Query();
			if(timerange!=null){
				SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
				String[] timeRanges = timerange.split("~");
				Date startDate=new Date(format.parse(timeRanges[0]).getTime());
				Date endDate=DateUtil.getEndOfDay(new Date(format.parse(timeRanges[1]).getTime()));
				query.addCriteria(Criteria.where("modifiedDate").gte(startDate).lte(endDate));
				countquery.addCriteria(Criteria.where("modifiedDate").gte(startDate).lte(endDate));
			}

			if(name != null && !name.isEmpty()){
				query.addCriteria(Criteria.where("name").regex(name));
				countquery.addCriteria(Criteria.where("name").regex(name));
			}

			if(status != null && !status.isEmpty()){
				query.addCriteria(Criteria.where("configStatus").is(status));
				countquery.addCriteria(Criteria.where("configStatus").is(status));
			}

			ServiceRequestHistoryResponse response = new ServiceRequestHistoryResponse();
			countquery.addCriteria(Criteria.where("activeFlag").is(Boolean.TRUE));
			query.addCriteria(Criteria.where("activeFlag").is(Boolean.TRUE))
					.with(Sort.by(Direction.DESC, "modifiedDate")).skip(offset > 0 ? ((offset - 1) * pagesize) : 0)
					.limit(pagesize);
			if(orgName != null){
				countquery.addCriteria(Criteria.where("organization").is(orgName));
				query.addCriteria(Criteria.where("organization").is(orgName));
			}
			response.setData(mongoTemplate.find(query, RatePlan.class));
			Pagination pagination = new Pagination();
			Long counter;
			counter = mongoTemplate.count(countquery, RatePlan.class);
			pagination.setOffset(offset);
			pagination.setTotal(counter);
			pagination.setPageSize(pagesize);
			response.setPagination(pagination);
			return response;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public RatePlan createRatePlan(String jsessionid,RatePlan ratePlan)
			throws ItorixException {

		boolean isAnyRequestPending = false;
		ratePlan.setCts(System.currentTimeMillis());
		ratePlan.setMts(System.currentTimeMillis());
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		String userName = user.getFirstName() + " " + user.getLastName();
		ratePlan.setCreatedBy(userName);
		ratePlan.setModifiedBy(userName);
		ratePlan.setCreatedDate(new Date(System.currentTimeMillis()));
		ratePlan.setModifiedDate(new Date(System.currentTimeMillis()));
		if (ratePlan != null && StringUtils.isNotBlank(ratePlan.getName()) && StringUtils.isNotBlank(
				ratePlan.getOrganization())) {
			List<RatePlan> ratePlans = mongoTemplate.find(
					Query.query(Criteria.where("organization")
							.is(ratePlan.getOrganization()).and("name").is(ratePlan.getName())),
					RatePlan.class);
			if (ratePlans.size() == 0) {
				ratePlan.setActiveFlag(Boolean.TRUE);
				ratePlan = mongoTemplate.save(ratePlan);
				return ratePlan;
			} else {
				if (ratePlans.size() > 0) {
					for (RatePlan plan : ratePlans) {
						if (plan.getConfigStatus().equalsIgnoreCase("Review")) {
							isAnyRequestPending = true;
							break;
						}
					}
					if (!isAnyRequestPending) {
						Query query = new Query(
								Criteria.where("organization").is(ratePlan.getOrganization()).and("name")
										.is(ratePlan.getName()));
						Update update = new Update();
						update.set("activeFlag", Boolean.FALSE);
						UpdateResult result = mongoTemplate.updateMulti(query, update, RatePlan.class);
						ratePlan.setActiveFlag(Boolean.TRUE);
						ratePlan = mongoTemplate.save(ratePlan);
						return ratePlan;
					} else {
						throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1026"),
								"Configuration-1026");
					}
				}
			}
		}else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1028"),
					"Configuration-1028");
		}
		return null;
	}


	public RatePlan updateRatePlan(String jsessionid,RatePlan ratePlan) throws ItorixException {

		try {
			ratePlan.setMts(System.currentTimeMillis());
			User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
			String userName = user.getFirstName() + " " + user.getLastName();
			ratePlan.setModifiedBy(userName);
			ratePlan.setModifiedDate(new Date(System.currentTimeMillis()));
			Query query = null;
			List<RatePlan> ratePlans;
			if(ratePlan.getApigeeId() != null){
				throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1033"),"Configuration-1033");
			}else {
				ratePlans = mongoTemplate.find(Query.query(Criteria.where("organization").is(ratePlan.getOrganization()).
								and("name").is(ratePlan.getName()).and("activeFlag").is(Boolean.TRUE)),
						RatePlan.class);
			}

			if (ratePlans.size() > 0) {
				if (ratePlan != null && StringUtils.isNotBlank(ratePlan.getName()) && StringUtils.isNotBlank(
						ratePlan.getOrganization())) {
					if(ratePlan.getApigeeId()!=null){
						query = new Query(Criteria.where("organization").is(ratePlan.getOrganization()).and("apigeeId")
								.is(ratePlan.getApigeeId()));
					}else {
						query = new Query(Criteria.where("organization").is(ratePlan.getOrganization()).and("name")
								.is(ratePlan.getName()));
					}
				}
				Update update = new Update();
				update.set("activeFlag", Boolean.FALSE);
				UpdateResult result = mongoTemplate.updateMulti(query, update, RatePlan.class);
			}
			ratePlan.setActiveFlag(Boolean.TRUE);
			return mongoTemplate.insert(ratePlan);
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public boolean changeRatePlanStatus(String jsessionid,RatePlan ratePlan)
			throws ItorixException, ParseException {

		Query query = new Query(Criteria.where("name").is(ratePlan.getName())
				.and("organization").is(ratePlan.getOrganization()).and("activeFlag").is(Boolean.TRUE));
		List<RatePlan> ratePlans = mongoTemplate.find(query,RatePlan.class);
		if (ratePlans.size() > 0) {
			RatePlan plan = ratePlans.get(0);
			if (ratePlan.getConfigStatus().equals("Review")) {
				if (plan.getConfigStatus().equals("Change Required"))
					plan.setConfigStatus(ratePlan.getConfigStatus());
				else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			} else if (ratePlan.getConfigStatus().equals("Approved")) {
				if (plan.getConfigStatus().equals("Review")) {
					plan.setConfigStatus(ratePlan.getConfigStatus());
					//Add Rateplan publishing logic here
				} else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			} else if (ratePlan.getConfigStatus().equals("Change Required")) {
				if(plan.getConfigStatus().equals("Review")) {
					plan.setConfigStatus(ratePlan.getConfigStatus());
				}else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			}else if (ratePlan.getConfigStatus().equals("Rejected")) {
				if(plan.getConfigStatus().equals("Review")
						|| plan.getConfigStatus().equals("Change Required")) {
					plan.setConfigStatus(ratePlan.getConfigStatus());
				}else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1032"), "Configuration-1032");
				}
			}
			String id = publishRatePlanToApigee(plan);
			Update update = new Update();
			Query updateQuery = new Query(Criteria.where("_id").is(plan.getId()));
			update.set("configStatus",ratePlan.getConfigStatus());
			if(id != null) {
				update.set("apigeeId", id);
			}

			User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
			String userName = user.getFirstName() + " " + user.getLastName();
			update.set("mts",System.currentTimeMillis());
			update.set("modifiedBy",userName);

			mongoTemplate.updateFirst(query, update, RatePlan.class);
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1029"), "Configuration-1029");
		}
		return false;
	}


	public void saveCountryMetadata(String countryMetadata) throws ItorixException {
		Query query = new Query().addCriteria(Criteria.where("key").is("country-code"));
		MetaData metaData = masterMongoTemplate.findOne(query, MetaData.class);
		if (metaData != null) {
			log.debug("Updating masterMongoTemplate");
			Update update = new Update();
			update.set("metadata", countryMetadata);
			masterMongoTemplate.updateFirst(query, update, MetaData.class);
		} else
			masterMongoTemplate.save(new MetaData("country-code", countryMetadata));
	}

	public List<CountryMetaData> getCountryMetaData() throws JsonProcessingException {
		Query query = new Query().addCriteria(Criteria.where("key").is("country-code"));
		MetaData metaData = masterMongoTemplate.findOne(query, MetaData.class);
		if (metaData == null) {
			return new ArrayList<>();
		}else{
			return new ObjectMapper().readValue(metaData.getMetadata(), new TypeReference<List<CountryMetaData>>(){});
		}
	}


	public String publishRatePlanToApigee(RatePlan ratePlan)
			throws ItorixException, ParseException {
		try{
			if(ratePlan.getConfigStatus().equalsIgnoreCase("Approved")){
				String organization = ratePlan.getOrganization();
				Query query = new Query();
				query.addCriteria(Criteria.where("orgname").is(organization));
				ApigeeConfigurationVO apigeeConfigurationVO = mongoTemplate.findOne(query,ApigeeConfigurationVO.class);
				ApigeeXConfigurationVO apigeeXConfigurationVO = mongoTemplate.findOne(query,ApigeeXConfigurationVO.class);

				if(apigeeConfigurationVO != null){
					//OnPrem
					ApigeeServiceUser serviceUser = apigeeConfigurationVO.getApigeeServiceUser();

					// We need 3 Common data from Apigee which will be reused across the payload : 1. Organization, 2. Currency, 3. Monetization Package

					// Get Organization Profile Details
					Map<String, Object> orgProfile = apigeeUtil.getOrganizationProfile(organization);
					if(orgProfile != null && orgProfile.containsKey("currency")){
						String ccy = orgProfile.get("currency").toString();

						// Get Currency Profile Details
						Map<String, LinkedHashMap> currencyProfile = apigeeUtil.getCurrencyProfile(organization,ccy);

						//Get Monetization Package Details
						Map<String, Object> monetizationPackageDetails = apigeeUtil.getMonetizationPackage(organization,ratePlan.getProductBundleId());

						ApigeeRatePlan apigeePayload = new ApigeeRatePlan();

						//General Field Sets
						apigeePayload.setPublished(true);
						apigeePayload.setAdvance(ratePlan.getCostModel()!=null?ratePlan.getCostModel().getPrepaidFee().booleanValue():true);
						apigeePayload.setType("STANDARD");
						apigeePayload.setStartDate(ratePlan.getStartDate());
						apigeePayload.setRecurringStartUnit(1);
						apigeePayload.setRecurringType("CALENDAR");
						apigeePayload.setPrivate(ratePlan.getVisibleToPortals().booleanValue());
						apigeePayload.setProrate(ratePlan.getCostModel()!=null?ratePlan.getCostModel().getProratedFee().booleanValue():true);
						apigeePayload.setOrganization(orgProfile);
						apigeePayload.setCurrency(currencyProfile);
						apigeePayload.setMonetizationPackage(monetizationPackageDetails);
						apigeePayload.setCustomPaymentTerm(true);
						apigeePayload.setKeepOriginalStartDate(false);
						apigeePayload.setDisplayName(ratePlan.getName());
						apigeePayload.setName(ratePlan.getName());
						apigeePayload.setDescription(ratePlan.getName());
						apigeePayload.setId(ratePlan.getApigeeId()!=null?ratePlan.getApigeeId():null);
						apigeePayload.setRatePlanDetails(new ArrayList<>());

						//Contract Field Sets
						apigeePayload.setSetupFee(ratePlan.getContractDetail()!=null?ratePlan.getContractDetail().getSetupFee():0);
						apigeePayload.setEarlyTerminationFee(ratePlan.getContractDetail()!=null?ratePlan.getContractDetail().getEarlyTerminationFee():0);
						apigeePayload.setContractDuration(ratePlan.getContractDetail()!=null?ratePlan.getContractDetail().getDuration():1);
						apigeePayload.setContractDurationType(ratePlan.getContractDetail()!=null?ratePlan.getContractDetail().getDurationUnit().name().toUpperCase():"MONTH");
						apigeePayload.setPaymentDueDays(ratePlan.getContractDetail()!=null? String.valueOf(
								ratePlan.getContractDetail().getPaymentDueDays()) :"30");
						//Cost Field Sets
						apigeePayload.setRecurringFee(ratePlan.getCostModel()!=null?ratePlan.getCostModel().getBaseFee():0);
						apigeePayload.setFrequencyDuration(ratePlan.getCostModel()!=null?ratePlan.getCostModel().getBillingPeriod():1);
						apigeePayload.setFrequencyDurationType(ratePlan.getCostModel()!=null?ratePlan.getCostModel().getBillingPeriodUnit().name().toUpperCase():"MONTH");

						Map<String,Object> ratePlanDetails = new HashMap<>();

						List<Map<String,Object>> ratePlanRates = new ArrayList<>();

						if(ratePlan.getType().name().toUpperCase().contains("RATECARD")){
							ratePlanDetails.put("ratingParameter","VOLUME");
							ratePlanDetails.put("duration",ratePlan.getRateCard().getCalculationFrequency());
							ratePlanDetails.put("durationType",ratePlan.getRateCard().getCalculationFrequencyUnit());
							ratePlanDetails.put("type",ratePlan.getType().name().toUpperCase());
							ratePlanDetails.put("revenueType","NET");
							ratePlanDetails.put("aggregateFreemiumCounters",true);
							ratePlanDetails.put("freemiumDuration",0);
							ratePlanDetails.put("freemiumDurationType","MONTH");
							ratePlanDetails.put("freemiumUnit",0);
							ratePlanDetails.put("paymentDueDays",apigeePayload.getPaymentDueDays());
							ratePlanDetails.put("aggregateStandardCounters",true);
							ratePlanDetails.put("aggregateTransactions",true);
							ratePlanDetails.put("customPaymentTerm",true);
							ratePlanDetails.put("organization",orgProfile);
							ratePlanDetails.put("currency",currencyProfile);

							if(ratePlan.getRateCard().getFlatRate() > 0){
								//FLATRATE
								Map<String,Object> ratePlanRate = new HashMap<>();
								ratePlanDetails.put("meteringType","UNIT");
								ratePlanRate.put("startUnit",0);
								ratePlanRate.put("rate",ratePlan.getRateCard().getFlatRate());
								ratePlanRate.put("type","RATECARD");
								ratePlanRates.add(ratePlanRate);
							}

							if(ratePlan.getRateCard().getVolumeBundles() != null){
								//VolumeBanded
								ratePlanDetails.put("meteringType","VOLUME");
								double startUnit = 0;

								Map<String,Object> ratePlanRate = new HashMap<>();

								int bandBeingProcessed = 1;
								int totalBands = ratePlan.getRateCard().getVolumeBundles().size();
								for(RateCardVolumeBand band : ratePlan.getRateCard().getVolumeBundles()){
									ratePlanRate.put("startUnit",startUnit);
									if(bandBeingProcessed == totalBands && ratePlan.getRateCard().getAllowUnlimitedUsage().booleanValue()){
										//Don't Add EndUnit if unlimited allowed and on the last band
									}else{
										ratePlanRate.put("endUnit",band.getUsage());
									}
									ratePlanRate.put("rate",band.getRate());
									ratePlanRate.put("type","RATECARD");
									startUnit = band.getUsage();
									ratePlanRates.add(ratePlanRate);
									++bandBeingProcessed;
								}
							}

							if(ratePlan.getRateCard().getBundles() != null){
								//Bundled Pricing
								ratePlanDetails.put("meteringType","STAIR_STEP");
								double startUnit = 0;

								Map<String,Object> ratePlanRate = new HashMap<>();

								int bundleBeingProcessed = 1;
								int totalBundles = ratePlan.getRateCard().getBundles().size();
								for(RateCardBundle bundle : ratePlan.getRateCard().getBundles()){
									ratePlanRate.put("startUnit",startUnit);
									if(bundleBeingProcessed == totalBundles && ratePlan.getRateCard().getAllowUnlimitedUsage().booleanValue()){
										//Don't Add EndUnit if unlimited allowed and on the last bundle
									}else{
										ratePlanRate.put("endUnit",bundle.getUsage());
									}
									ratePlanRate.put("rate",bundle.getRate());
									ratePlanRate.put("type","RATECARD");
									startUnit = bundle.getUsage();
									ratePlanRates.add(ratePlanRate);
									++bundleBeingProcessed;
								}
							}
						}

						if(ratePlan.getType().name().toUpperCase().contains("REVSHARE")){
							//REVENUE SHARE RatePlan
							ratePlanDetails.put("aggregateFreemiumCounters",true);
							ratePlanDetails.put("freemiumDuration",0);
							ratePlanDetails.put("freemiumDurationType","MONTH");
							ratePlanDetails.put("freemiumUnit",0);
							ratePlanDetails.put("paymentDueDays",apigeePayload.getPaymentDueDays());
							ratePlanDetails.put("aggregateStandardCounters",true);
							ratePlanDetails.put("aggregateTransactions",true);
							ratePlanDetails.put("customPaymentTerm",true);
							ratePlanDetails.put("organization",orgProfile);
							ratePlanDetails.put("currency",currencyProfile);
							ratePlanDetails.put("ratingParameter","VOLUME");
							ratePlanDetails.put("duration",ratePlan.getRateCard()!=null?ratePlan.getRateCard().getCalculationFrequency():1);
							ratePlanDetails.put("durationType",ratePlan.getRateCard()!=null?ratePlan.getRateCard().getCalculationFrequencyUnit():"MONTH");
							ratePlanDetails.put("type",ratePlan.getType().name().toUpperCase());
							ratePlanDetails.put("revenueType",ratePlan.getRevenueShare().getCalculationModel().name().toUpperCase());

							if(ratePlan.getRevenueShare().getSharingModel().name().equalsIgnoreCase("FLEXIBLE")){
								ratePlanDetails.put("meteringType","VOLUME");
								Map<String,Object> ratePlanRate = new HashMap<>();
								double startUnit = 0;

								int bandBeingProcessed = 1;
								int totalBands = ratePlan.getRevenueShare().getRevenueShareBands().size();
								for(RevenueShareBand band : ratePlan.getRevenueShare().getRevenueShareBands()){
									ratePlanRate.put("startUnit",startUnit);
									if(bandBeingProcessed == totalBands && ratePlan.getRevenueShare().getAllowUnlimitedUsage().booleanValue()){
										//Don't Add EndUnit if unlimited allowed and on the last band
									}else{
										ratePlanRate.put("endUnit",band.getUsage());
									}
									ratePlanRate.put("revshare",band.getRate());
									ratePlanRate.put("type","REVSHARE");
									startUnit = band.getUsage();
									ratePlanRates.add(ratePlanRate);
								}
							}else{
								ratePlanDetails.put("meteringType","UNIT");
								Map<String,Object> ratePlanRate = new HashMap<>();
								ratePlanRate.put("startUnit",0);
								ratePlanRate.put("revshare",ratePlan.getRevenueShare().getFixedSharePercentage());
								ratePlanRate.put("type","REVSHARE");
								ratePlanRates.add(ratePlanRate);
							}


						}

						if(ratePlan.getType().name().equalsIgnoreCase("REVSHARE_RATECARD")){
							ratePlanDetails.put("aggregateFreemiumCounters",true);
							ratePlanDetails.put("freemiumDuration",0);
							ratePlanDetails.put("freemiumDurationType","MONTH");
							ratePlanDetails.put("freemiumUnit",0);
							ratePlanDetails.put("paymentDueDays",apigeePayload.getPaymentDueDays());
							ratePlanDetails.put("aggregateStandardCounters",true);
							ratePlanDetails.put("aggregateTransactions",true);
							ratePlanDetails.put("customPaymentTerm",true);
							ratePlanDetails.put("organization",orgProfile);
							ratePlanDetails.put("currency",currencyProfile);
							ratePlanDetails.put("type","REVSHARE_RATECARD");
						}

						if(ratePlan.getType().name().equalsIgnoreCase("ADJUSTABLE_NOTIFICATION")){
							ratePlanDetails.put("aggregateFreemiumCounters",true);
							apigeePayload.setPaymentDueDays(String.valueOf(ratePlan.getAdjNotificationCalcFreqInMonths()*30));
							ratePlanDetails.put("freemiumDuration",0);
							ratePlanDetails.put("freemiumDurationType","MONTH");
							ratePlanDetails.put("freemiumUnit",0);
							ratePlanDetails.put("paymentDueDays",String.valueOf(ratePlan.getAdjNotificationCalcFreqInMonths()*30));
							ratePlanDetails.put("aggregateStandardCounters",true);
							ratePlanDetails.put("aggregateTransactions",true);
							ratePlanDetails.put("customPaymentTerm",true);
							ratePlanDetails.put("organization",orgProfile);
							ratePlanDetails.put("currency",currencyProfile);
							ratePlanDetails.put("type","USAGE_TARGET");
							ratePlanDetails.put("meteringType","DEV_SPECIFIC");
							ratePlanDetails.put("ratingParameter","VOLUME");
						}
						if(!ratePlanRates.isEmpty()){
							ratePlanDetails.put("ratePlanRates",ratePlanRates);
						}
						apigeePayload.setRatePlanDetails(ratePlanDetails.isEmpty() ? new ArrayList<>(): Arrays.asList(ratePlanDetails));
						if(!ratePlan.getIsFreemium()){
							apigeePayload.setFreemiumDuration(null);
							apigeePayload.setFreemiumDurationType(null);
							apigeePayload.setFreemiumUnit(null);
						}
						Map<String,Object> response = apigeeUtil.createOrUpdateRatePlan(organization,ratePlan.getApigeeId(),apigeePayload,ratePlan.getProductBundleId());
						if(response != null && response.containsKey("id")){
							return response.get("id").toString();
						}
						return null;
					}
				}

				if(apigeeXConfigurationVO != null){
					//ApigeeX Cloud
				}
			}
		}catch (Exception ex){
			log.error("Could Not Publish RatePlan to Apigee:" + ex.getMessage());
		}
		return null;
	}

	public List<RatePlan> getRatePlanHistory(String orgName, String name) {
		Query query = new Query().with(Sort.by(Sort.Direction.DESC, "mts"));
		query.addCriteria(Criteria.where("name").is(name)
				.and("organization").is(orgName));

		List<RatePlan> ratePlans = mongoTemplate.find(query,RatePlan.class);

		return ratePlans.isEmpty() ? new ArrayList<>() : ratePlans;
	}

	public List<String> getSupportedCurrencies(String orgName) throws ItorixException, ParseException{
		return configManagementDao.getSupportedCurrencies(orgName);
	}
	public void deleteRatePlan(String ratePlanId) throws ItorixException {
		Query query = new Query(Criteria.where("_id").is(ratePlanId).and("configStatus").ne("Approved"));
		RatePlan ratePlan  = mongoTemplate.findOne(query,RatePlan.class);
		if(ratePlan == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1034"),
					"Configuration-1034");
		}
		mongoTemplate.remove(ratePlan);
	}

}
