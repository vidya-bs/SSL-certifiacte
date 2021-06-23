package com.itorix.apiwiz.apimonitor.dao;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.apimonitor.model.Certificates;
import com.itorix.apiwiz.apimonitor.model.MonitorCollectionsResponse;
import com.itorix.apiwiz.apimonitor.model.NotificationDetails;
import com.itorix.apiwiz.apimonitor.model.SummaryNotification;
import com.itorix.apiwiz.apimonitor.model.Variables;
import com.itorix.apiwiz.apimonitor.model.collection.APIMonitorResponse;
import com.itorix.apiwiz.apimonitor.model.collection.ExecutionResult;
import com.itorix.apiwiz.apimonitor.model.collection.MonitorCollections;
import com.itorix.apiwiz.apimonitor.model.collection.Schedulers;
import com.itorix.apiwiz.apimonitor.model.request.CollectionVariables;
import com.itorix.apiwiz.apimonitor.model.request.MonitorRequest;
import com.itorix.apiwiz.apimonitor.model.request.RequestVariable;
import com.itorix.apiwiz.apimonitor.model.request.Variable;
import com.itorix.apiwiz.apimonitor.model.stats.Event;
import com.itorix.apiwiz.apimonitor.model.stats.RequestStats;
import com.itorix.apiwiz.apimonitor.model.stats.logs.LogEvent;
import com.itorix.apiwiz.apimonitor.model.stats.logs.MonitorRequestLog;
import com.itorix.apiwiz.apimonitor.model.stats.logs.Request;
import com.itorix.apiwiz.apimonitor.model.stats.logs.Response;
import com.itorix.apiwiz.common.model.AbstractObject;
import com.itorix.apiwiz.common.model.MetaData;
import com.itorix.apiwiz.common.model.SearchItem;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.Pagination;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.mongodb.MongoClient;

@Component
public class ApiMonitorDAO {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Qualifier("masterMongoTemplate")
	@Autowired
	private MongoTemplate masterMongoTemplate;

	@Autowired
	private ApplicationProperties config;

	@Autowired
	private IdentityManagementDao identityManagementDao;

	private static final Logger log = LoggerFactory.getLogger(ApiMonitorDAO.class);

	public String createCollection(MonitorCollections monitorCollections, String jsessionid) {
		monitorCollections.getSchedulers().stream().forEach(s -> s.setId(new ObjectId().toString()));
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		monitorCollections.setCreatedBy(user.getFirstName() + " " + user.getLastName());
		monitorCollections.setCts(System.currentTimeMillis());
		addUpdateDetails(monitorCollections, jsessionid);
		mongoTemplate.save(monitorCollections);
		return monitorCollections.getId();
	}

	public void updateCollection(MonitorCollections monitorCollections, String id, String jsessionid)
			throws ItorixException {

		Query query = new Query().addCriteria(Criteria.where("id").is(id));
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);

		monitorCollections.getSchedulers().stream().forEach(s -> {
			if (!StringUtils.hasText(s.getId()))
				s.setId(new ObjectId().toString());
		});

		Update update = new Update();
		update.set("name", monitorCollections.getName());
		update.set("summary", monitorCollections.getSummary());
		update.set("description", monitorCollections.getDescription());
		update.set("notifications", monitorCollections.getNotifications());
		update.set("schedulers", monitorCollections.getSchedulers());
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());

		if (mongoTemplate.updateFirst(query, update, MonitorCollections.class).getModifiedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-1000"), "Monitor-1000");
		}

	}

	public APIMonitorResponse getCollections(int offset, int pageSize) {

		Query query = new Query().with(Sort.by(Direction.DESC, "_id")).skip(offset > 0 ? ((offset - 1) * pageSize) : 0)
				.limit(pageSize);

		query.fields().include("id").include("name").include("summary").include("cts").include("createdBy")
		.include("modifiedBy").include("mts").include("schedulers").include("monitorRequest.id").include("monitorRequest.name");
		APIMonitorResponse response = new APIMonitorResponse();

		List<MonitorCollections> monitorCollections = mongoTemplate.find(query, MonitorCollections.class);

		List<String> collectionIds = monitorCollections.stream().map(s->s.getId()).collect(Collectors.toList());

		Aggregation aggForLatency = Aggregation.newAggregation(Aggregation.match(Criteria.where("collectionId").in(collectionIds)),
				Aggregation.group("collectionId").avg("$latency").as("latency"));

		List<Document> latency = mongoTemplate.aggregate(aggForLatency, ExecutionResult.class, Document.class).getMappedResults();

		Aggregation aggForCount = Aggregation.newAggregation(Aggregation.match(Criteria.where("collectionId").in(collectionIds)),
				Aggregation.group("collectionId").count().as("count"));
		List<Document> countDoc = mongoTemplate.aggregate(aggForCount, ExecutionResult.class, Document.class).getMappedResults();


		Aggregation aggForSuccess = Aggregation.newAggregation(Aggregation.match(Criteria.where("collectionId").in(collectionIds).and("status").is("Success")),
				Aggregation.group("collectionId").count().as("count"));
		List<Document> successDoc = mongoTemplate.aggregate(aggForSuccess, ExecutionResult.class, Document.class).getMappedResults();

		List<MonitorCollectionsResponse> monitorResponse = new ArrayList<>();

		if (!CollectionUtils.isEmpty(monitorCollections)) {
			Long counter = mongoTemplate.count(query, MonitorCollections.class);
			Pagination pagination = new Pagination();
			pagination.setOffset(offset);
			pagination.setTotal(counter);
			pagination.setPageSize(pageSize);
			response.setPagination(pagination);
			for(MonitorCollections monitor : monitorCollections){
				MonitorCollectionsResponse colectionResponse = new MonitorCollectionsResponse();
				colectionResponse.setCreatedBy(monitor.getCreatedBy());
				colectionResponse.setCts(monitor.getCts());
				colectionResponse.setId(monitor.getId());
				int uptime = 0;
				long latencyInt = 0l;
				int count = 0;
				int success = 0;

				Optional<Document> latencyDoc = latency.stream().filter(f->f.getString("_id").equals(monitor.getId())).findFirst();
				if(latencyDoc.isPresent()){
					latencyInt = Math.round(latencyDoc.get().getDouble("latency"));
				}

				Optional<Document> countOptional = countDoc.stream().filter(f->f.getString("_id").equals(monitor.getId())).findFirst();
				if(countOptional.isPresent()){
					count = countOptional.get().getInteger("count");
				}

				Optional<Document> successOptional = successDoc.stream().filter(f->f.getString("_id").equals(monitor.getId())).findFirst();
				if(successOptional.isPresent()){
					success = successOptional.get().getInteger("count");
				}

				if(success!= 0 || count != 0){
					uptime = Math.round((( float) success/count)*100);
				}
				colectionResponse.setUptime(uptime);
				colectionResponse.setLatency(latencyInt);
				colectionResponse.setModifiedBy(monitor.getModifiedBy());
				colectionResponse.setMts(monitor.getMts());
				colectionResponse.setName(monitor.getName());
				colectionResponse.setSummary(monitor.getSummary());
				monitorResponse.add(colectionResponse);
			}
			response.setData(monitorResponse);
		}

		return response;
	}

	public MonitorCollections getCollection(String id) throws ItorixException {

		Query query = new Query().addCriteria(Criteria.where("id").is(id));
		query.fields().exclude("monitorRequest");

		MonitorCollections monitorCollection = mongoTemplate.findOne(query, MonitorCollections.class);
		if (monitorCollection == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-1000"), "Monitor-1000");
		}
		return monitorCollection;
	}

	public void deleteCollection(String id) throws ItorixException {

		Query query = new Query().addCriteria(Criteria.where("id").is(id));
		if (mongoTemplate.remove(query, MonitorCollections.class).getDeletedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-1000"), "Monitor-1000");
		}
	}

	public String createRequest(String id, MonitorRequest monitorRequest, String jsessionid) throws ItorixException {

		Query queryForDuplicateCheck = new Query(new Criteria().andOperator(Criteria.where("id").is(id),
				Criteria.where("monitorRequest").elemMatch(Criteria.where("name").is(monitorRequest.getName()))));

		if(mongoTemplate.findOne(queryForDuplicateCheck, MonitorCollections.class) != null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-1006"), "Monitor-1006");
		}

		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		monitorRequest.setId(new ObjectId().toString());
		Query query = new Query().addCriteria(Criteria.where("id").is(id));
		Update update = new Update();
		update.push("monitorRequest", monitorRequest);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());
		update.push("sequence", monitorRequest.getId());
		if (mongoTemplate.updateFirst(query, update, MonitorCollections.class).getMatchedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-1000"), "Monitor-1000");
		}
		return monitorRequest.getId();

	}

	public void updateRequest(String id, String requestId, MonitorRequest monitorRequest, String jsessionid)
			throws ItorixException {

		monitorRequest.setId(requestId);
		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(id),
				Criteria.where("monitorRequest").elemMatch(Criteria.where("id").is(requestId))));

		Update update = new Update();
		update.set("monitorRequest.$", monitorRequest);
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());
		if (mongoTemplate.updateFirst(query, update, MonitorCollections.class).getMatchedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-1000"), "Monitor-1000");
		}
	}

	public MonitorCollections getRequests(String id, int offset, int pageSize) throws ItorixException {

		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(id)));

		query.fields().include("id").include("monitorRequest._id").include("monitorRequest.name")
		.include("monitorRequest.summary");
		MonitorCollections monitorCollection = mongoTemplate.findOne(query, MonitorCollections.class);
		if (monitorCollection == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-1000"), "Monitor-1000");
		}
		return monitorCollection;

		// Aggregation agg =
		// Aggregation.newAggregation(Aggregation.match(Criteria.where("id").is(id)),
		// Aggregation.project("monitorRequest"),
		// Aggregation.unwind("$monitorRequest"),
		// Aggregation.skip(Long.valueOf(offset > 0 ? ((offset - 1) * pageSize)
		// : 0)), Aggregation.limit(pageSize),
		// Aggregation.project("monitorRequest._id", "monitorRequest.name",
		// "monitorRequest.summary"));
		//
		// AggregationResults<MonitorRequest> resultData =
		// mongoTemplate.aggregate(agg, MonitorCollections.class,
		// MonitorRequest.class);
		// List<MonitorRequest> project = resultData.getMappedResults();
		//
		// ProjectionOperation projectForCount =
		// Aggregation.project("monitorRequest.id")
		// .and(ArrayOperators.arrayOf("monitorRequest").length()).as("count");
		//
		// Aggregation aggForCount =
		// Aggregation.newAggregation(Aggregation.match(Criteria.where("id").is(id)),
		// projectForCount);
		//
		// AggregationResults<Map> result = mongoTemplate.aggregate(aggForCount,
		// MonitorCollections.class, Map.class);
		// Long count = 0L;
		// if (!result.getMappedResults().isEmpty()) {
		// count = Long.valueOf((int)
		// result.getMappedResults().get(0).get("count"));
		// }
		// return getPaginatedResponse(offset, count, project, pageSize);

	}

	public MonitorRequest getRequest(String id, String requestId) throws ItorixException {
		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(id),
				Criteria.where("monitorRequest").elemMatch(Criteria.where("id").is(requestId))));

		query.fields().include("monitorRequest.$");

		List<MonitorCollections> find = mongoTemplate.find(query, MonitorCollections.class);
		if (CollectionUtils.isEmpty(find)) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-1000"), "Monitor-1000");
		}
		return find.get(0).getMonitorRequest().get(0);
	}

	public void deleteRequest(String id, String requestId, String jsessionId) throws ItorixException {

		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(id),
				Criteria.where("monitorRequest").elemMatch(Criteria.where("id").is(requestId))));

		if (mongoTemplate
				.updateMulti(query,
						new Update().pull("monitorRequest",
								new Query().addCriteria(Criteria.where("_id").is(requestId))),
						MonitorCollections.class)
				.getModifiedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-1000"), "Monitor-1000");
		}

	}

	public RequestStats getRequestStats(String collectionId, String requestId, String schedulerId, Date date)
			throws ItorixException {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, 1);
		Date endDate = c.getTime();
		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(collectionId),
				Criteria.where("schedulers").elemMatch(Criteria.where("id").is(schedulerId))));

		RequestStats requestStats = new RequestStats();
		MonitorCollections collection = mongoTemplate.findOne(query, MonitorCollections.class);

		if (collection == null || CollectionUtils.isEmpty(collection.getSchedulers())) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-1000"), "Monitor-1000");
		}

		requestStats.setCollectionID(collection.getId());
		requestStats.setCollectionName(collection.getName());
		requestStats.setEnvironmentId(collection.getSchedulers().get(0).getEnvironmentId());
		requestStats.setRequestID(requestId);
		requestStats.setInterval(collection.getSchedulers().get(0).getInterval());

		Query queryReqExecution = new Query(new Criteria().andOperator(Criteria.where("collectionId").is(collectionId),
				Criteria.where("schedulerId").is(schedulerId) , Criteria.where("requestId").is(requestId) ,
				Criteria.where("executedTime").gte(date.getTime()).lt(endDate.getTime())));

		List<ExecutionResult> requestExecution = mongoTemplate.find(queryReqExecution, ExecutionResult.class);
		if(!CollectionUtils.isEmpty(requestExecution)){
			List<Long> timeSeries = requestExecution.stream().map(s -> s.getExecutedTime()).collect(Collectors.toList());
			List<Event> events = requestExecution.stream().map(s -> {
				Event event = new Event();
				event.setEventID(s.getId());
				event.setLatency(s.getLatency());
				event.setStatus(s.getStatus());
				event.setTimestamp(s.getExecutedTime());
				return event;
			}).collect(Collectors.toList());

			requestStats.setEvents(events);
			requestStats.setTimeseries(timeSeries);
			requestStats.setRequestName(requestExecution.get(0).getName());
		}
		MonitorCollections monitorCollection = getCollection(collectionId);
		requestStats.setCts(monitorCollection.getCts());
		return requestStats;
	}

	public MonitorRequestLog getRequestStatLogs(String collectionId, String requestId, String eventId) throws ItorixException {

		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(collectionId),
				Criteria.where("monitorRequest").elemMatch(Criteria.where("id").is(requestId))));

		MonitorCollections collection = mongoTemplate.findOne(query, MonitorCollections.class);

		if (collection == null || CollectionUtils.isEmpty(collection.getSchedulers())) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-1000"), "Monitor-1000");
		}

		MonitorRequestLog monitorLogs = new MonitorRequestLog();

		Query queryForEvent = new Query(new Criteria().andOperator(Criteria.where("collectionId").is(collectionId),
				Criteria.where("requestId").is(requestId) , Criteria.where("id").is(eventId)));

		ExecutionResult requestExecution = mongoTemplate.findOne(queryForEvent, ExecutionResult.class);

		monitorLogs.setCollectionID(collection.getId());
		monitorLogs.setCollectionName(collection.getName());
		monitorLogs.setRequestID(requestExecution.getRequestId());
		monitorLogs.setRequestName(requestExecution.getName());
		monitorLogs.setTimestamp(requestExecution.getExecutedTime());
		LogEvent event = new LogEvent();
		monitorLogs.setEvent(event);
		Request request = new Request();
		request.setUri(requestExecution.getPath());
		request.setMethod(requestExecution.getVerb());

		if(requestExecution.getRequest() != null){
			request.setBody(requestExecution.getRequest().getBody());
			request.setFormParams(requestExecution.getRequest().getFormParams());
			request.setFormURLEncoded(requestExecution.getRequest().getFormURLEncoded());
			request.setHeaders(requestExecution.getRequest().getHeaders());
			request.setQueryParams(requestExecution.getRequest().getQueryParams());
		}
		event.setRequest(request);

		Response response = new Response();
		response.setStatusMessage(requestExecution.getStatus());
		response.setBody(requestExecution.getResponse().getBody());
		response.setVariables(requestExecution.getResponse().getVariables());
		response.setStatusCode(requestExecution.getStatusCode());

		response.setHeaders(requestExecution.getResponse().getHeaders());

		event.setResponse(response);

		return monitorLogs;
	}

	private <T extends AbstractObject> void addUpdateDetails(T t, String jsessionid) {
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		t.setMts(System.currentTimeMillis());
		t.setModifiedBy(user.getFirstName() + " " + user.getLastName());

	}

	private APIMonitorResponse getPaginatedResponse(int offset, Long counter, Object data, int pageSize) {
		APIMonitorResponse response = new APIMonitorResponse();
		if (data != null) {
			Pagination pagination = new Pagination();
			pagination.setOffset(offset);
			pagination.setTotal(counter);
			pagination.setPageSize(pageSize);
			response.setPagination(pagination);
			response.setData(data);
		}
		return response;
	}

	public List<MonitorCollections> getCollections() {
		return mongoTemplate.findAll(MonitorCollections.class);
	}

	public void updateLastExecution(String collectionId, String schedulerId) {

		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(collectionId),
				Criteria.where("schedulers").elemMatch(Criteria.where("id").is(schedulerId))));
		Update update = new Update();
		update.set("schedulers.$.lastExecutionTime", Instant.now().toEpochMilli());
		mongoTemplate.updateFirst(query, update, MonitorCollections.class);
	}


	public void createVariables(Variables variables) throws ItorixException {
		if (findByConfigName(variables.getName()) == null) {
			mongoTemplate.save(variables);
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-1005"), "Monitor-1005");
		}
	}

	public Object updateVariables(Variables variables, String id, String jsessionid) throws ItorixException {
		Query query = new Query(Criteria.where("_id").is(id));
		Variables variable = mongoTemplate.findOne(query, Variables.class);
		//		DBObject dbDoc = new BasicDBObject();
		//		mongoTemplate.getConverter().write(variables, dbDoc);
		//		Update update = Update.fromDBObject(dbDoc, "_id");
		//		UpdateResult result = mongoTemplate.updateFirst(query, update, Variables.class);
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		if (variable != null) {
			variables.setCreatedBy(variable.getCreatedBy());
			variables.setCts(variable.getCts());
			variables.setId(id);
			mongoTemplate.save(variables);
			return true;
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-1000"), "Monitor-1000");
		}
	}

	public Variables findByVariableName(String name) {
		Query query = new Query(Criteria.where("name").is(name));
		Variables varibale = mongoTemplate.findOne(query, Variables.class);
		return varibale;
	}

	public List<Variables> getVariables() {
		return mongoTemplate.findAll(Variables.class);
	}

	public void deleteVariable(String id) {
		mongoTemplate.remove(new Query(Criteria.where("id").is(id)), Variables.class);
	}

	public Variables findByConfigName(String configName) {
		Query query = new Query(Criteria.where("name").is(configName));
		return mongoTemplate.findOne(query, Variables.class);
	}

	public Variables getVariablesById(String id) {
		Query query = new Query(Criteria.where("id").is(id));
		Variables var = mongoTemplate.findOne(query, Variables.class);
		return var;
	}

	public List<Certificates> getCertificates(boolean names) {
		if (names) {
			Query searchQuery = new Query();
			searchQuery.fields().include("name");
			return mongoTemplate.find(searchQuery, Certificates.class);
		}
		Query searchQuery = new Query();
		searchQuery.fields().exclude("content").exclude("password");
		return mongoTemplate.find(searchQuery, Certificates.class);
	}

	public void deleteCertificate(String name) throws ItorixException {
		if(mongoTemplate.remove(new Query(Criteria.where("name").is(name)),
				Certificates.class).getDeletedCount() == 0){
			throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-1002"), "Monitor-1002");
		}
	}

	public Certificates getCertificate(String name){
		Query searchQuery = new Query(Criteria.where("name").is(name));
		searchQuery.fields().exclude("content").exclude("password");
		return mongoTemplate.findOne(searchQuery, Certificates.class);
	}

	public void createOrUpdateCertificate(String name, byte[] jKSFile, String description, String password,
			String alias, String jsessionid) throws ItorixException {

		if (StringUtils.hasText(password) && (jKSFile != null && jKSFile.length > 0)) {
			try {
				KeyStore ks = KeyStore.getInstance("jks");
				ks.load(new ByteArrayInputStream(jKSFile), password.toCharArray());
			} catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
				log.error("Issue in uploaded certificate", e);
				throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-1004"), "Monitor-1004");
			}
		}

		Update update = new Update();
		update.set("name", name);
		update.set("content", jKSFile);
		update.set("description", description);
		try {
			if(StringUtils.hasText(password)){
				update.set("password", new RSAEncryption().encryptText(password));
			} else {
				update.set("password", password);
			}
		} catch (Exception e) {
			log.error("exception during pwd encryption" , e);
		}
		update.set("alias", alias);

		Query query = new Query(Criteria.where("name").is(name));
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		if(CollectionUtils.isEmpty(mongoTemplate.find(query, Certificates.class))){
			update.set("cts", System.currentTimeMillis());
			update.set("createdBy", user.getFirstName() + " " + user.getLastName());
		} else {
			update.set("mts", System.currentTimeMillis());
			update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());
		}

		mongoTemplate.upsert(query, update, Certificates.class);
	}

	public byte[] downloadCertificate(String name) {
		Query searchQuery = new Query(Criteria.where("name").is(name));
		searchQuery.fields().include("content");
		Certificates certificate = mongoTemplate.findOne(searchQuery, Certificates.class);
		if(certificate != null){
			return certificate.getContent();
		}
		return null;
	}

	public void updateRequestSequence(String collectionId, List<String> sequence) throws ItorixException {
		Query query = new Query().addCriteria(Criteria.where("id").is(collectionId));
		Update update = new Update();
		update.set("sequence", sequence);
		if (mongoTemplate.updateFirst(query, update, MonitorCollections.class).getModifiedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-1000"), "Monitor-1000");
		}
	}

	public MonitorCollections getRequestSequence(String collectionId) throws ItorixException {
		Query query = new Query().addCriteria(Criteria.where("id").is(collectionId));
		query.fields().include("sequence");
		MonitorCollections monitorCollection = mongoTemplate.findOne(query, MonitorCollections.class);
		if (monitorCollection == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-1000"), "Monitor-1000");
		}
		return monitorCollection;
	}

	public MonitorCollections getSchedulers(String collectionId) throws ItorixException {
		Query query = new Query().addCriteria(Criteria.where("id").is(collectionId));
		query.fields().include("monitorRequest").include("schedulers");
		MonitorCollections monitorCollection = mongoTemplate.findOne(query, MonitorCollections.class);
		if (monitorCollection == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-1000"), "Monitor-1000");
		}
		return monitorCollection;
	}

	public Object search(String name, int limit) throws ItorixException {
		BasicQuery query = new BasicQuery("{\"name\": {$regex : '" + name + "', $options: 'i'}}");
		query.limit(limit > 0 ? limit : 10);
		List<MonitorCollections> items = mongoTemplate.find(query, MonitorCollections.class);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode response = mapper.createObjectNode();
		ArrayNode responseFields = mapper.createArrayNode();
		for (MonitorCollections vo : items) {
			SearchItem searchItem = new SearchItem();
			searchItem.setId(vo.getId());
			searchItem.setName(vo.getName());
			responseFields.addPOJO(searchItem);
		}
		response.set("MonitorCollections", responseFields);
		return response;
	}

	public void createMetaData(String metadataStr) {
		Query query = new Query().addCriteria(Criteria.where("key").is("monitor"));
		MetaData metaData = masterMongoTemplate.findOne(query, MetaData.class);
		if(metaData != null)
		{
			Update update = new Update();
			update.set("metadata", metadataStr);
			masterMongoTemplate.updateFirst(query, update, MetaData.class);
		}else
			masterMongoTemplate.save(new MetaData("monitor",metadataStr));
	}

	public Object getMetaData() {
		Query query = new Query().addCriteria(Criteria.where("key").is("monitor"));
		MetaData metaData = masterMongoTemplate.findOne(query, MetaData.class);
		if (metaData != null)
			return metaData.getMetadata();
		return null;
	}

	public CollectionVariables getCollectionsVariable(String collectionId) throws ItorixException {

		Query query = new Query().addCriteria(Criteria.where("id").is(collectionId));
		MonitorCollections monitorCollection = mongoTemplate.findOne(query, MonitorCollections.class);
		if (monitorCollection == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Monitor-1000"), "Monitor-1000");
		}

		CollectionVariables collectionVariables = new CollectionVariables();
		collectionVariables.setCollectionID(monitorCollection.getId());
		List<MonitorRequest> monitorRequests = monitorCollection.getMonitorRequest();
		for (MonitorRequest monitorRequest : monitorRequests) {

			List<Variable> variables = monitorRequest.getResponse().getVariables();
			if (!CollectionUtils.isEmpty(variables)) {
				RequestVariable requestVariable = new RequestVariable();
				requestVariable.setRequestId(monitorRequest.getId());
				requestVariable.setRequestName(monitorRequest.getName());
				requestVariable.setVariables(variables);
				collectionVariables.getRequests().add(requestVariable);
			}
			;
		}
		return collectionVariables;
	}

	public List<NotificationDetails> getNotificationDetails(String workSpace) {

		Query query = new Query();

		query.fields().include("id").include("name").include("schedulers").include("monitorRequest.id")
		.include("monitorRequest.name").include("notifications");

		List<MonitorCollections> monitorCollections = mongoTemplate.find(query, MonitorCollections.class);
		List<NotificationDetails> notificationDetails = new ArrayList<>();

		if (!CollectionUtils.isEmpty(monitorCollections)) {
			for (MonitorCollections monitor : monitorCollections) {
				for (Schedulers scheduler : monitor.getSchedulers()) {
					if(!scheduler.isPause()){
						NotificationDetails notificationDetail = new NotificationDetails();
						notificationDetail.setNotifications(monitor.getNotifications());
						notificationDetail.setEnvironmentName(scheduler.getEnvironmentName());
						notificationDetail.setSchedulerId(scheduler.getId());
						setDailyNotificationResult(notificationDetail, monitor.getId(), scheduler.getId());
						setAvegareNotificationResult(notificationDetail, monitor.getId(), scheduler.getId());
						notificationDetail.setCollectionname(monitor.getName());
						notificationDetail.setWorkspaceName(workSpace);
						notificationDetails.add(notificationDetail);
					}
				}
			}
		}

		return notificationDetails;
	}

	public boolean canExecute(){
		Date endDate = new Date();
		int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
		java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yy");
		try {
			String previousDate = dateFormat.format(endDate.getTime() - MILLIS_IN_DAY);
			Date startDate = dateFormat.parse(previousDate);
			Query query = new Query().addCriteria(Criteria.where("date").gt(startDate).lte(endDate));
			SummaryNotification summaryNotification = mongoTemplate.findOne(query, SummaryNotification.class);
			if(summaryNotification != null)
				return false;
		} catch (ParseException e) {
			log.error(e.getMessage(), e);
		}
		return true;
	}

	public void updateExecution(){
		Date date = new Date();
		SummaryNotification summaryNotification = new SummaryNotification();
		summaryNotification.setDate(date);
		mongoTemplate.save(summaryNotification);
	}

	private void setDailyNotificationResult(NotificationDetails notificationDetails, String collectionId,
			String schedulerId) {

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date startDate = new Date(calendar.getTime().getTime());

		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 59);
		Date endDate = new Date(calendar.getTime().getTime());

		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		notificationDetails.setDate(dateFormat.format(startDate));
		Criteria criteria = new Criteria().andOperator(Criteria.where("collectionId").is(collectionId),
				Criteria.where("collectionId").is(collectionId), Criteria.where("schedulerId").is(schedulerId),
				Criteria.where("executedTime").gte(startDate.getTime()).lt(endDate.getTime()));

		Aggregation aggForLatency = Aggregation.newAggregation(Aggregation.match(criteria),
				Aggregation.group("collectionId").avg("$latency").as("latency"));

		List<Document> latency = mongoTemplate.aggregate(aggForLatency, ExecutionResult.class, Document.class)
				.getMappedResults();
		Aggregation aggForCount = Aggregation.newAggregation(Aggregation.match(criteria),
				Aggregation.group("collectionId").count().as("count"));
		List<Document> countDoc = mongoTemplate.aggregate(aggForCount, ExecutionResult.class, Document.class)
				.getMappedResults();

		Criteria successCriteria = new Criteria().andOperator(criteria, Criteria.where("status").is("Success"));

		Aggregation aggForSuccess = Aggregation.newAggregation(Aggregation.match(successCriteria),
				Aggregation.group("collectionId").count().as("count"));
		List<Document> successDoc = mongoTemplate.aggregate(aggForSuccess, ExecutionResult.class, Document.class)
				.getMappedResults();

		int uptime = 0;
		long latencyInt = 0l;
		int count = 0;
		int success = 0;

		Optional<Document> latencyDoc = latency.stream().filter(f -> f.getString("_id").equals(collectionId))
				.findFirst();
		if (latencyDoc.isPresent()) {
			latencyInt = Math.round(latencyDoc.get().getDouble("latency"));
		}

		Optional<Document> countOptional = countDoc.stream().filter(f -> f.getString("_id").equals(collectionId))
				.findFirst();
		if (countOptional.isPresent()) {
			count = countOptional.get().getInteger("count");
		}

		Optional<Document> successOptional = successDoc.stream().filter(f -> f.getString("_id").equals(collectionId))
				.findFirst();
		if (successOptional.isPresent()) {
			success = successOptional.get().getInteger("count");
		}

		if (success != 0 || count != 0) {
			uptime = Math.round(((float) success / count) * 100);
		}
		notificationDetails.setDailyLatency(latencyInt);
		notificationDetails.setDailyUptime(uptime);
	}

	private void setAvegareNotificationResult(NotificationDetails notificationDetails, String collectionId,
			String schedulerId) {

		Criteria criteria = new Criteria().andOperator(Criteria.where("collectionId").is(collectionId),
				Criteria.where("collectionId").is(collectionId), Criteria.where("schedulerId").is(schedulerId));

		Aggregation aggForLatency = Aggregation.newAggregation(Aggregation.match(criteria),
				Aggregation.group("collectionId").avg("$latency").as("latency"));

		List<Document> latency = mongoTemplate.aggregate(aggForLatency, ExecutionResult.class, Document.class)
				.getMappedResults();
		Aggregation aggForCount = Aggregation.newAggregation(Aggregation.match(criteria),
				Aggregation.group("collectionId").count().as("count"));
		List<Document> countDoc = mongoTemplate.aggregate(aggForCount, ExecutionResult.class, Document.class)
				.getMappedResults();

		Criteria successCriteria = new Criteria().andOperator(criteria, Criteria.where("status").is("Success"));

		Aggregation aggForSuccess = Aggregation.newAggregation(Aggregation.match(successCriteria),
				Aggregation.group("collectionId").count().as("count"));
		List<Document> successDoc = mongoTemplate.aggregate(aggForSuccess, ExecutionResult.class, Document.class)
				.getMappedResults();

		int uptime = 0;
		long latencyInt = 0l;
		int count = 0;
		int success = 0;

		Optional<Document> latencyDoc = latency.stream().filter(f -> f.getString("_id").equals(collectionId))
				.findFirst();
		if (latencyDoc.isPresent()) {
			latencyInt = Math.round(latencyDoc.get().getDouble("latency"));
		}

		Optional<Document> countOptional = countDoc.stream().filter(f -> f.getString("_id").equals(collectionId))
				.findFirst();
		if (countOptional.isPresent()) {
			count = countOptional.get().getInteger("count");
		}

		Optional<Document> successOptional = successDoc.stream().filter(f -> f.getString("_id").equals(collectionId))
				.findFirst();
		if (successOptional.isPresent()) {
			success = successOptional.get().getInteger("count");
		}

		if (success != 0 || count != 0) {
			uptime = Math.round(((float) success / count) * 100);
		}
		notificationDetails.setAvgLatency(latencyInt);
		notificationDetails.setAvgUptime(uptime);
	}
}