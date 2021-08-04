package com.itorix.apiwiz.identitymanagement.dao;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.MetaData;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.encryption.HmacSHA256;
import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContextHolder;
import com.itorix.apiwiz.identitymanagement.model.Subscription;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.itorix.apiwiz.identitymanagement.model.Workspace;

@Component
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
		masterMongoTemplate.save(workspace);
	}

	public void removeSeats(long count) throws ItorixException {
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		Workspace workspace = getWorkspace(userSessionToken.getWorkspaceId());
		long usedSeats = getUsedSeats(workspace.getName());
		if ((workspace.getSeats() - usedSeats) >= count) {
			long seats = workspace.getSeats() - count;
			workspace.setSeats(seats);
			masterMongoTemplate.save(workspace);
		} else
			throw new ItorixException("current used seats is larger, delete current users before down sizing",
					"USER_005");
	}

	public Workspace updateWorkspaceStatus(String workapaceId, String status) {
		Workspace workspace = getWorkspace(workapaceId);
		if (workspace != null) {
			workspace.setStatus(status);
			masterMongoTemplate.save(workspace);
		}
		return workspace;
	}

	public Workspace updateWorkspaceStatus(String status) {
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		Workspace workspace = getWorkspace(userSessionToken.getWorkspaceId());
		if (workspace != null) {
			workspace.setStatus(status);
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
		Subscription subscription = masterMongoTemplate.findOne(query, Subscription.class);
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

	public String getPublicKey(String tenant, String source) {
		String key = "-----BEGIN PUBLIC KEY-----\r\n"
				+ "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyM7Y0lRFgJqVtju1Ma/o\r\n"
				+ "n/yA0FeR9W9kq249436kKZagIJqZRJ/eS2A/J0+M5VdDg43NWZ4Q7+DmszgXQTfd\r\n"
				+ "pH+1wpOGY8taHhAtNrBn2cWVtbLh/iF7PDiPnmilodLycKP0oVpp4VpZTLHNReCR\r\n"
				+ "JgjtpqDQoaQJkhtFYcPgrCO+owBSUYMszcv9OZBhZH64f897nQLwDHJ3nFY9MHUt\r\n"
				+ "7jbV1FhGaRGDxnIRL20SaYkwgoV9s4b5l7RH91AxAbHjZjRvNXrgWuZ2X60ILraa\r\n"
				+ "luBrltMW/bXvCcDF1NaZ0PMpQThrskK+JtvVzexzHUtulsL8XDvUZotmsXPqVPvX\r\n" + "AwIDAQAB\r\n"
				+ "-----END PUBLIC KEY-----";
		return key;
	}
}
