package com.itorix.apiwiz.servicerequest.dao;

import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.util.Set;
import javax.mail.MessagingException;

import com.itorix.apiwiz.common.model.slack.*;
import com.itorix.apiwiz.common.util.slack.SlackUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
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

	@SuppressWarnings({"unchecked", "unused"})
	public boolean updateServiceRequest(ServiceRequest serviceRequest) throws ItorixException {
		try {
			Query query = null;
			List<ServiceRequest> serviceRequests = (ArrayList<ServiceRequest>) getAllActiveServiceRequests(
					serviceRequest);

			if (serviceRequests.size() > 0) {
				log.debug("Updating service request");
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

				Update update = new Update();
				update.set("activeFlag", Boolean.FALSE);
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
	public boolean revertServiceRequest(String requestId) throws ItorixException, MessagingException {
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
			throws ItorixException, MessagingException {
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
			throws ItorixException, MessagingException {
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
			throws ItorixException, MessagingException {
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
						isCreatedorUpdated = configManagementDao.updateProduct(productconfig);
						configManagementDao.createApigeeProduct(productconfig, user);
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
						productconfig.setAttributes(serviceRequest.getAttributes());
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
						if (serviceRequest.getIsSaaS()) {
							productconfig.setType("saas");
						} else {
							productconfig.setType("onprem");
						}
						isCreatedorUpdated = configManagementDao.updateProductConfig(productconfig);
						configManagementDao.createApigeeProduct(productconfig, user);
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
		String[] dates = timerange.split("~");
		Date startDate = null;
		Date endDate = null;
		Date orgStartDateinit = null;
		Date origEnddate = null;
		if (dates != null && dates.length > 0) {
			startDate = dateFormat.parse(dates[0]);
			endDate = dateFormat.parse(dates[1]);
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
			while (startDate.compareTo(endDate) <= 0) {
				Query query = new Query();
				query.addCriteria(
						Criteria.where(ServiceRequest.LABEL_CREATED_TIME).gte(DateUtil.getStartOfDay(startDate))
								.lt(DateUtil.getEndOfDay(endDate)).and("type").is(type));
				List<ServiceRequest> list = baseRepository.find(query, ServiceRequest.class);
				// if(list!=null && list.size()>0){
				ObjectNode valueNode = mapper.createObjectNode();
				valueNode.put("timestamp", DateUtil.getStartOfDay(startDate).getTime() + "");
				valueNode.put("value", list.size());
				valuesNode.add(valueNode);
				// }
				startDate = DateUtil.addDays(startDate, 1);
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
				query.addCriteria(
						Criteria.where("modifiedDate").gte(DateUtil.getStartOfDay(startDate))
								.lt(DateUtil.getEndOfDay(endDate)));
				query.addCriteria((Criteria.where("activeFlag").is(Boolean.TRUE)));
				List<ServiceRequest> listByStatus = baseRepository.find(query, ServiceRequest.class);
				typesStatusNode.put("status", status);
				typesStatusNode.put("count", listByStatus.size());
				for (String type : distincttypes) {
					ArrayNode namesNode = mapper.createArrayNode();
					query = new Query();
					query.addCriteria(Criteria.where("status").is(status).and("type").is(type));
					query.addCriteria(
							Criteria.where("modifiedDate").gte(DateUtil.getStartOfDay(startDate))
									.lt(DateUtil.getEndOfDay(endDate)));
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
}
