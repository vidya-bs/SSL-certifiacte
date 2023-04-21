package com.itorix.apiwiz.serviceregistry.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itorix.apiwiz.common.model.configmanagement.KVMEntry;
import com.itorix.apiwiz.common.model.projectmanagement.Endpoints;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.servicerequest.dao.ServiceRequestDao;
import com.itorix.apiwiz.servicerequest.model.ServiceRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import org.bson.Document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.common.model.SearchItem;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.projectmanagement.Organization;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.itorix.apiwiz.serviceregistry.model.documents.Metadata;
import com.itorix.apiwiz.serviceregistry.model.documents.ServiceRegistry;
import com.itorix.apiwiz.serviceregistry.model.documents.ServiceRegistryColumns;
import com.itorix.apiwiz.serviceregistry.model.documents.ServiceRegistryList;
import com.itorix.apiwiz.serviceregistry.model.documents.ServiceRegistryResponse;
import com.mongodb.client.result.UpdateResult;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ServiceRegistryDao {
	@Value("${server.port}")
	private String port;
	@Value("${server.contextPath}")
	private String context;
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private BaseRepository baseRepository;

	@Autowired
	private IdentityManagementDao identityManagementDao;
	@Autowired
	private ServiceRequestDao serviceRequestDao;

	public ServiceRegistryColumns getServiceRegistryColumns() {
		List<ServiceRegistryColumns> findAll = mongoTemplate.findAll(ServiceRegistryColumns.class);
		return findAll.isEmpty() ? null : findAll.get(0);
	}

	public void createOrUpdateSRColumns(ServiceRegistryColumns registryColumns) {
		Query query = new Query();
		query.addCriteria(Criteria.where("columns").exists(true));
		Update update = new Update();
		update.set("columns", registryColumns.getColumns());
		mongoTemplate.upsert(query, update, ServiceRegistryColumns.class);
	}

	public void deleteServiceRegistry(String serviceRegistryId) throws ItorixException {
		if (mongoTemplate.remove(new Query(Criteria.where("_id").is(serviceRegistryId)), ServiceRegistryList.class)
				.getDeletedCount() > 0) {
			mongoTemplate.remove(new Query(Criteria.where("serviceRegistryId").is(serviceRegistryId)),
					ServiceRegistry.class);
		} else {
			log.error("no record found in Service Registry for %", serviceRegistryId);
			throw new ItorixException(
					String.format(ErrorCodes.errorMessage.get("ServiceRegistry-1000"), serviceRegistryId),
					"ServiceRegistry-1000");
		}
	}

	public ServiceRegistryList createServiceRegistry(ServiceRegistryList serviceRegistry) throws ItorixException {
		try {
			return baseRepository.save(serviceRegistry);
		} catch (DuplicateKeyException e) {
			log.error("DuplicateKeyException entry found", e);
			throw new ItorixException(
					String.format(ErrorCodes.errorMessage.get("ServiceRegistry-1001"),
							String.join(",", serviceRegistry.getName(), serviceRegistry.getEnvironment())),
					"ServiceRegistry-1001");
		}
	}

	public void updateServiceRegistry(String serviceRegistryId, ServiceRegistryList serviceRegistry)
			throws ItorixException {
		UserSession userSession = UserSession.getCurrentSessionToken();
		String userId = userSession.getUserId();
		String username = userSession.getUsername();
		serviceRegistry.setModifiedBy(userId);
		serviceRegistry.setModifiedUserName(username);
		long mts = System.currentTimeMillis();
		serviceRegistry.setMts(mts);
		updateDocument(serviceRegistry, serviceRegistryId);
	}

	public List<ServiceRegistry> getServiceRegistries() {
		return mongoTemplate.findAll(ServiceRegistry.class);
	}

	public List<ServiceRegistry> getServiceRegistryEntries(String serviceRegistryId) {
		return mongoTemplate.find(new Query(Criteria.where("serviceRegistryId").is(serviceRegistryId)),
				ServiceRegistry.class);
	}

	private List<Map<String, String>> getServiceRegistryEntriesMap(String serviceRegistryId) {
		List<com.itorix.apiwiz.serviceregistry.model.documents.ServiceRegistry> rowList = mongoTemplate
				.find(new Query(Criteria.where("serviceRegistryId").is(serviceRegistryId)),
						ServiceRegistry.class);
		if (rowList != null) {
			List<Map<String, String>> data = new ArrayList<>();
			for (com.itorix.apiwiz.serviceregistry.model.documents.ServiceRegistry row : rowList) {
				data.add(row.getData());
			}
			return data;
		}
		return null;

	}

	private String getEndpoints(List<Map<String, String>> registryEndpoints) {
		Endpoints endpoints = new Endpoints();
		endpoints.setEndpoints(registryEndpoints);
		try {
			String value = new ObjectMapper().writeValueAsString(endpoints);
			value = value.replaceAll("\"endpoints\"", "\"Endpoints\"")
					.replaceAll("\"endpoint\"", "\"Endpoint\"");
			return value;
		} catch (JsonProcessingException e) {
			log.error("Exception occurred", e);
			return "";
		}
	}

	public void updateServiceRegistryEntry(String serviceRegistryId, String rowId, Map<String, String> data)
			throws ItorixException {
		Query query = new Query(Criteria.where("id").is(rowId));
		Update update = new Update();
		update.set("data", data);
		if (mongoTemplate.updateFirst(query, update, ServiceRegistry.class).getModifiedCount() == 0)
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("ServiceRegistry-1000"), rowId),
					"ServiceRegistry-1000");
	}

	public ServiceRegistry createServiceRegistryEntry(String serviceRegistryId, Map<String, String> entryies)
			throws ItorixException {
		List<Criteria> criteriaList = new ArrayList<>();
		criteriaList.add(Criteria.where("id").is(serviceRegistryId));
		Query query = Query.query(new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()])));
		if (mongoTemplate.exists(query, ServiceRegistryList.class)) {
			log.debug("Creating service registry entry");
			ServiceRegistry serviceRegistry = new ServiceRegistry();
			serviceRegistry.setServiceRegistryId(serviceRegistryId);
			serviceRegistry.setData(entryies);
			serviceRegistry = mongoTemplate.save(serviceRegistry);
			return serviceRegistry;
		} else {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("ServiceRegistry-1000"),
					String.join(",", serviceRegistryId)), "ServiceRegistry-1000");
		}
	}

	public List<ServiceRegistryList> getServiceaRegistry(String projectId, String proxyName) {
		List<Criteria> criteriaList = new ArrayList<>();
		if (StringUtils.hasText(projectId)) {
			criteriaList.add(Criteria.where("projectId").is(projectId));
		}
		if (StringUtils.hasText(proxyName)) {
			criteriaList.add(Criteria.where("proxyName").is(proxyName));
		}
		if (criteriaList.isEmpty()) {
			return mongoTemplate.findAll(ServiceRegistryList.class);
		} else {
			Query query = Query
					.query(new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()])));
			return mongoTemplate.find(query, ServiceRegistryList.class);
		}
	}

	public List<ServiceRegistryList> getServiceaRegistry() {
		return mongoTemplate.findAll(ServiceRegistryList.class);
	}

	public ServiceRegistryResponse getServiceaRegistry(int offset, int pageSize) {

		Query query = new Query().with(Sort.by(Direction.DESC, "mts")).skip(offset > 0 ? ((offset - 1) * pageSize) : 0)
				.limit(pageSize);
		ServiceRegistryResponse response = new ServiceRegistryResponse();
		List<ServiceRegistryList> testSuites = mongoTemplate.find(query, ServiceRegistryList.class);
		if (testSuites != null) {
			log.debug("Getting service registry");
			Long counter = mongoTemplate.count(new Query(), ServiceRegistryList.class);
			com.itorix.apiwiz.identitymanagement.model.Pagination pagination = new com.itorix.apiwiz.identitymanagement.model.Pagination();
			pagination.setOffset(offset);
			pagination.setTotal(counter);
			pagination.setPageSize(pageSize);
			response.setPagination(pagination);
			response.setData(testSuites);
		}

		return response;
	}

	public void deleteServiceRegistryEntry(String rowId) throws ItorixException {

		if (mongoTemplate.remove(new Query(Criteria.where("_id").is(rowId)), ServiceRegistry.class)
				.getDeletedCount() <= 0) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("ServiceRegistry-1000"), rowId),
					"ServiceRegistry-1000");
		}
	}

	public <T> void updateDocument(T t, String id) throws ItorixException {
		Query query = new Query(Criteria.where("_id").is(id));
		Document dbDoc = new Document();
		mongoTemplate.getConverter().write(t, dbDoc);
		Update update = Update.fromDocument(dbDoc, "_id");
		try {
			UpdateResult result = mongoTemplate.updateFirst(query, update, t.getClass());
			if (!(result.getModifiedCount() > 0)) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("ServiceRegistry-1000"), id),
						"ServiceRegistry-1000");
			}
		} catch (DuplicateKeyException e) {
			log.error("DuplicateKeyException entry found", e);
			throw new ItorixException(e.getMessage(), "ServiceRegistry-1002");
		}
	}

	public ServiceRegistryList getServiceRegistryListById(String serviceRegistryId) throws ItorixException {
		ServiceRegistryList registryList = mongoTemplate.findById(serviceRegistryId, ServiceRegistryList.class);
		if (registryList == null) {
			throw new ItorixException(
					String.format(ErrorCodes.errorMessage.get("ServiceRegistry-1000"), serviceRegistryId),
					"ServiceRegistry-1000");
		}
		return registryList;
	}

	public void publishServiceRegistry(String serviceRegistryId, String jsessionid) throws ItorixException {
		ServiceRegistryList registry = mongoTemplate.findOne(new Query(Criteria.where("_id").is(serviceRegistryId)),
				ServiceRegistryList.class);
		if (null != registry) {
			log.debug("Publishing service registry");
			List<Metadata> metadata = registry.getMetadata();
			String proxyName = null;
			String org = null;
			String env = null;
			String type = null;
			try {
				proxyName = metadata.stream().filter(r -> r.getKey().equalsIgnoreCase("proxy")).findFirst().get()
						.getValue();
				org = metadata.stream().filter(r -> r.getKey().equalsIgnoreCase("organization")).findFirst().get()
						.getValue();
				env = metadata.stream().filter(r -> r.getKey().equalsIgnoreCase("environment")).findFirst().get()
						.getValue();
				type = metadata.stream().filter(r -> r.getKey().equalsIgnoreCase("type")).findFirst().get().getValue();
			} catch (Exception e) {
			}
			if (null != proxyName && null != org && null != env && null != type) {
				Organization organization = new Organization();
				organization.setEnv(env);
				organization.setName(org);
				organization.setType(type);
				publish(organization, proxyName, serviceRegistryId, jsessionid);
			}
		} else {
			log.error("no record found in Service Registry for %", serviceRegistryId);
			throw new ItorixException(
					String.format(ErrorCodes.errorMessage.get("ServiceRegistry-1000"), serviceRegistryId),
					"ServiceRegistry-1000");
		}
	}

	public Object search(String name, int limit) {
		BasicQuery query = new BasicQuery("{\"name\": {$regex : '" + name + "', $options: 'i'}}");
		query.limit(limit > 0 ? limit : 10);
		List<ServiceRegistryList> registryList = mongoTemplate.find(query, ServiceRegistryList.class);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode response = mapper.createObjectNode();
		ArrayNode responseFields = mapper.createArrayNode();
		for (ServiceRegistryList vo : registryList) {
			SearchItem searchItem = new SearchItem();
			searchItem.setId(vo.getId());
			searchItem.setName(vo.getName());
			responseFields.addPOJO(searchItem);
		}
		response.set("serviceRegistries", responseFields);
		return response;
	}

	private void publish(Organization organization, String proxyName, String registryId, String jsessionId)
			throws ItorixException {
		try {
			List<Map<String, String>> serviceRegistry = getServiceRegistryEntriesMap(registryId);
			if (serviceRegistry == null) {
				throw new ItorixException(
						"no service registry for proxy " + proxyName + " registry Id " + registryId,
						"");
			}
			ServiceRequest config = new ServiceRequest();
			config.setType("KVM");
			config.setName(proxyName);
			config.setOrg(organization.getName());
			config.setEnv(organization.getEnv());
			config.setEncrypted("false");
			config.setIsSaaS(organization.getType().equalsIgnoreCase("saas") ? true : false);
			KVMEntry entry = new KVMEntry();
			entry.setName("endpoints");
			entry.setValue(getEndpoints(serviceRegistry));
			List<KVMEntry> entries = new ArrayList<KVMEntry>();
			entries.add(entry);
			config.setEntry(entries);
			User user = identityManagementDao.getUserDetailsFromSessionID(jsessionId);
			config.setCreatedUser(user.getFirstName() + " " + user.getLastName());
			config.setCreatedUserEmailId(user.getEmail());
			config.setCreatedDate(new Date(System.currentTimeMillis()));
			config.setModifiedUser(user.getFirstName() + " " + user.getLastName());
			config.setModifiedDate(new Date(System.currentTimeMillis()));
			config.setStatus("Review");
			config.setCreated(false);
			config.setActiveFlag(Boolean.TRUE);
			serviceRequestDao.createServiceRequest(config);
			config.setStatus("Approved");
			List<String> roles = identityManagementDao.getUserRoles(jsessionId); // user.getRoles();
			if (!roles.contains("Admin")) {
				roles.add("Admin");
			}
			config.setUserRole(roles);
			serviceRequestDao.changeServiceRequestStatus(config, user);
		} catch (MessagingException e) {
			log.error("Exception occurred", e);
		}catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
