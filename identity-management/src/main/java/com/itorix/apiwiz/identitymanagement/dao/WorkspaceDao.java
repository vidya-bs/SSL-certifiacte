package com.itorix.apiwiz.identitymanagement.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.itorix.apiwiz.common.model.MetaData;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.encryption.HmacSHA256;
import com.itorix.apiwiz.common.util.mail.MailProperty;
import com.itorix.apiwiz.identitymanagement.model.*;
import com.itorix.apiwiz.identitymanagement.model.sso.SAMLConfig;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class WorkspaceDao {

	private final String SUBSCRIPTION_ENDPOINT = "/webhooks/subscriptions/";

	@Autowired
	protected BaseRepository baseRepository;

	@Qualifier("masterMongoTemplate")
	@Autowired
	private MongoTemplate masterMongoTemplate;

	@Value("${itorix.core.accounts.api}")
	private String url;

	@Value("${itorix.core.hmac.password}")
	private String password;

	private Set<String> newPlans = new HashSet<>(Arrays.asList("starter", "growth", "enterprise"));

	public Workspace updateWorkspaceSubscription(Workspace workspace) throws ItorixException, InvalidKeyException,
			NoSuchAlgorithmException, UnsupportedEncodingException, JsonMappingException, JsonProcessingException {
		Workspace dBworkspace = getWorkspace(workspace.getName());
		if (dBworkspace != null) {
			Workspace subWorkspace = validateSubscriptionId(workspace.getSubscriptionId());
			dBworkspace.setIsTrial(false);
			dBworkspace.setPlanId(workspace.getPlanId());
			dBworkspace.setPaymentSchedule(workspace.getPaymentSchedule());
			dBworkspace.setSubscriptionId(workspace.getSubscriptionId());
			dBworkspace.setStatus("active");
			dBworkspace.setSeats(subWorkspace.getSeats());
			dBworkspace.setPaymentSchedule(subWorkspace.getPaymentSchedule());
			if (workspace.getCts() != null) {
				dBworkspace.setCts(workspace.getCts());
			} else {
				dBworkspace.setCts(System.currentTimeMillis());
			}
			dBworkspace.setMts(System.currentTimeMillis());
			masterMongoTemplate.save(dBworkspace);
		}
		return workspace;
	}

	private Workspace validateSubscriptionId(String subscriptionId) throws ItorixException, InvalidKeyException,
			NoSuchAlgorithmException, UnsupportedEncodingException, JsonMappingException, JsonProcessingException {
		String connectionUrl = url + SUBSCRIPTION_ENDPOINT + subscriptionId;
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		String reqBody = "{ \"timestamp\": " + String.valueOf(System.currentTimeMillis()) + " }";
		String signature = HmacSHA256.hmacDigest(reqBody, password);
		headers.set("signature", signature);
		HttpEntity<Object> httpEntity = new HttpEntity<>(reqBody, headers);
		ResponseEntity<String> response = null;
		try {
			log.debug("Making a call to {}", connectionUrl);
			response = restTemplate.postForEntity(connectionUrl, httpEntity, String.class);
		} catch (Exception e) {
			throw e;
		}
		if (!response.getStatusCode().is2xxSuccessful())
			throw new ItorixException("Subscription id provided is invalid", "USER_005");
		String respStr = response.getBody();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode json = mapper.readTree(respStr);
		JsonNode data = json.get("data");
		if (data != null) {
			Workspace workspace = new Workspace();
			int seats = data.get("quantity").intValue();
			String interval = data.get("interval").textValue();
			workspace.setSeats(seats);
			workspace.setPaymentSchedule(interval);
			return workspace;
		}
		throw new ItorixException("Subscription id provided is invalid", "USER_005");
	}

	public long getUsedSeats(String workspaceId) {
		Query query = new Query().addCriteria(Criteria.where("workspaces.workspace._id").is(workspaceId));
		return masterMongoTemplate.count(query, User.class);
	}

	public void addSeats(long count) {
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		Workspace workspace = getWorkspace(userSessionToken.getWorkspaceId());
		long seats = workspace.getSeats() + count;
		workspace.setSeats(seats);
		workspace.setMts(System.currentTimeMillis());
		masterMongoTemplate.save(workspace);
	}

	public void removeSeats(long count) throws ItorixException {
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		Workspace workspace = getWorkspace(userSessionToken.getWorkspaceId());
		long usedSeats = getUsedSeats(workspace.getName());
		if ((workspace.getSeats() - usedSeats) >= count) {
			long seats = workspace.getSeats() - count;
			workspace.setSeats(seats);
			workspace.setMts(System.currentTimeMillis());
			masterMongoTemplate.save(workspace);
		} else
			throw new ItorixException("current used seats is larger, delete current users before down sizing",
					"USER_005");
	}

	public Workspace updateWorkspaceStatus(String workapaceId, String status) {
		Workspace workspace = getWorkspace(workapaceId);
		if (workspace != null) {
			workspace.setStatus(status);
			workspace.setMts(System.currentTimeMillis());
			masterMongoTemplate.save(workspace);
		}
		return workspace;
	}

	public Workspace updateWorkspaceStatus(String status) {
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		Workspace workspace = getWorkspace(userSessionToken.getWorkspaceId());
		if (workspace != null) {
			workspace.setStatus(status);
			workspace.setMts(System.currentTimeMillis());
			masterMongoTemplate.save(workspace);
		}
		return workspace;
	}

	public Workspace getWorkspace(String workapaceId) {
		Query query = new Query();
		query.addCriteria(new Criteria().orOperator(Criteria.where("name").is(workapaceId)));
		Workspace workspace = masterMongoTemplate.findOne(query, Workspace.class);
		return workspace;
	}

	public void enableSso(Workspace workspace) {
		Workspace dbWorkspace = getWorkspace(workspace.getName());
		dbWorkspace.setSsoEnabled(true);
		dbWorkspace.setSsoHost(workspace.getSsoHost());
		dbWorkspace.setSsoPath(workspace.getSsoPath());
		dbWorkspace.setIdpProvider(workspace.getIdpProvider());
		dbWorkspace.setMts(System.currentTimeMillis());
		masterMongoTemplate.save(dbWorkspace);
	}

	public void createSubscriptionPlans(List<Subscription> subscriptions) {
		for (Subscription subscription : subscriptions) {
			masterMongoTemplate.save(subscription);
		}
	}

	public List<Subscription> getSubscriptions() {
		List<Subscription> subscriptions = masterMongoTemplate.findAll(Subscription.class);
		return subscriptions;
	}

	public Subscription getSubscription(String subscriptionId) {
		Query query = new Query();
		query.addCriteria(new Criteria().orOperator(Criteria.where("id").is(subscriptionId)));
		Subscription subscription = null;
		if (newPlans.contains(subscriptionId)) {
			SubscriptionV2 subscriptionV2 = masterMongoTemplate.findOne(query, SubscriptionV2.class);
			subscription = new Subscription();
			subscription.setSubscriptionPrice(subscriptionV2.getSubscriptionPrices());
			subscription.setPricing(subscriptionV2.getPricing());
		} else {
			subscription = masterMongoTemplate.findOne(query, Subscription.class);
		}
		return subscription;
	}

	public void createLandingData(String source, String metadataStr) {
		Query query = new Query().addCriteria(Criteria.where("key").is(source));
		MetaData metaData = masterMongoTemplate.findOne(query, MetaData.class);
		if (metaData != null) {
			Update update = new Update();
			update.set("metadata", metadataStr);
			masterMongoTemplate.updateFirst(query, update, MetaData.class);
		} else
			masterMongoTemplate.save(new MetaData(source, metadataStr));
	}

	public Object getLandingData(String source) {
		Query query = new Query().addCriteria(Criteria.where("key").is(source));
		MetaData metaData = masterMongoTemplate.findOne(query, MetaData.class);
		if (metaData != null)
			return metaData.getMetadata();
		return null;
	}

	public String getPublicKey(String tenant, String source) throws ItorixException {
		Query query = Query.query(Criteria.where("tenant").is(tenant).and("source").is(source));
		TenantPublicKey key = masterMongoTemplate.findOne(query, TenantPublicKey.class);
		if (key != null) {
			return key.getKey();
		}
		throw new ItorixException(ErrorCodes.errorMessage.get("Identity-1002"), "Identity-1002");
		//
		// String key = "-----BEGIN PUBLIC KEY-----\r\n"
		// +
		// "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyM7Y0lRFgJqVtju1Ma/o\r\n"
		// +
		// "n/yA0FeR9W9kq249436kKZagIJqZRJ/eS2A/J0+M5VdDg43NWZ4Q7+DmszgXQTfd\r\n"
		// +
		// "pH+1wpOGY8taHhAtNrBn2cWVtbLh/iF7PDiPnmilodLycKP0oVpp4VpZTLHNReCR\r\n"
		// +
		// "JgjtpqDQoaQJkhtFYcPgrCO+owBSUYMszcv9OZBhZH64f897nQLwDHJ3nFY9MHUt\r\n"
		// +
		// "7jbV1FhGaRGDxnIRL20SaYkwgoV9s4b5l7RH91AxAbHjZjRvNXrgWuZ2X60ILraa\r\n"
		// +
		// "luBrltMW/bXvCcDF1NaZ0PMpQThrskK+JtvVzexzHUtulsL8XDvUZotmsXPqVPvX\r\n"
		// + "AwIDAQAB\r\n"
		// + "-----END PUBLIC KEY-----";
		// return key;
	}

	public void disableSso(String workspaceName) {
		Query query = Query.query(Criteria.where("_id").is(workspaceName));
		Update update = new Update();
		update.set("ssoEnabled", false);
		update.set("mts", System.currentTimeMillis());
		UpdateResult updateResult = masterMongoTemplate.updateFirst(query, update,
				Workspace.class);
	}

	public void updatePublicKey(String tenant, String source, String key) {
		Query query = Query.query(Criteria.where("tenant").is(tenant).and("source").is(source));
		Update update = new Update();
		update.set("key", key);
		masterMongoTemplate.upsert(query, update, TenantPublicKey.class);
	}

	public Object getVideos(String category) throws JsonProcessingException {
		if (category != null) {
			Query query = Query.query(Criteria.where("key").is("videos"));
			MetaData metadata = masterMongoTemplate.findOne(query, MetaData.class);
			if (metadata != null) {
				ObjectMapper objectMapper = new ObjectMapper();
				TypeFactory typeFactory = objectMapper.getTypeFactory();
				List<Video> videos = objectMapper.readValue(metadata.getMetadata(),
						typeFactory.constructCollectionType(List.class, Video.class));
				return videos.stream().filter(v -> v.getCategory().equals(category)).collect(Collectors.toList());
			}
		} else {
			MetaData metadata = masterMongoTemplate.findOne(Query.query(Criteria.where("key").is("videos")),
					MetaData.class);
			if (metadata != null) {
				return metadata.getMetadata();
			}
		}
		return null;
	}

	public String getIdpMetadata(String workspaceId) {
		SAMLConfig samlConfig = getSamlConfig(workspaceId);
		return samlConfig == null ? null : new String(samlConfig.getMetadata());
	}

	public SAMLConfig getSamlConfig(String workspaceId) {
		UIMetadata uiuxMetadata = getUIUXMetadata(UIMetadata.SAML_CONFIG, workspaceId);
		try {
			return uiuxMetadata == null
					? null
					: new ObjectMapper().readValue(uiuxMetadata.getMetadata(), SAMLConfig.class);
		} catch (IOException e) {
			return null;
		}
	}

	public UIMetadata getUIUXMetadata(String query, String workspaceId) {
		Query dbQuery = new Query(Criteria.where("query").is(query).and("workspaceId").is(workspaceId));
		List<UIMetadata> UIMetadata = masterMongoTemplate.find(dbQuery, UIMetadata.class);
		if (UIMetadata != null && UIMetadata.size() > 0) {
			return UIMetadata.get(0);
		} else {
			return null;
		}
	}

	public void updateSMTPConnector(MailProperty mailProperty) {
		String userId = null;
		String username = null;

		MailProperty mailProp = baseRepository.findOne("tenantId", mailProperty.getTenantId(), MailProperty.class);
		try {
			UserSession userSession = UserSession.getCurrentSessionToken();
			userId = userSession.getUserId();
			username = userSession.getUsername();
		} catch (Exception e) {
			log.error("Internal Server Error {} ", e.getMessage());
		}

		long timestamp = System.currentTimeMillis();

		if (mailProp == null) {
			mailProperty.setCts(timestamp);
			mailProperty.setCreatedBy(userId);
			mailProperty.setCreatedUserName(username);
		} else {
			mailProperty.setPassword(mailProp.getPassword());
			mailProperty.setCts(mailProp.getCts());
			mailProperty.setCreatedBy(mailProp.getCreatedBy());
			mailProperty.setCreatedUserName(mailProp.getCreatedUserName());
		}

		mailProperty.setMts(timestamp);
		mailProperty.setModifiedBy(userId);
		mailProperty.setModifiedUserName(username);

		baseRepository.saveMongoDoc(mailProperty);
	}

    public Object getSMTPConnector() {
		List<MailProperty> allMailProps = baseRepository.findAll(MailProperty.class);
		if(allMailProps != null && allMailProps.size() > 0) {
			MailProperty mailProperty = allMailProps.get(0);
			mailProperty.setPassword(null);
			return mailProperty;
		}
		else return null;
	}

	public void deleteSMTPConnector() {
		baseRepository.remove(new Query(), MailProperty.class);
	}

	public List<SubscriptionV2> getSubscriptionsV2() {
		return masterMongoTemplate.findAll(SubscriptionV2.class);
	}

	public void createSubscriptionPlansV2(List<SubscriptionV2> subscriptions) {
		for (SubscriptionV2 subscriptionV2 : subscriptions) {
			masterMongoTemplate.save(subscriptionV2);
		}
	}
}
