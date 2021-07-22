package com.itorix.apiwiz.devstudio.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.itorix.apiwiz.common.model.apigee.ApigeeConfigurationVO;
import com.itorix.apiwiz.common.model.configmanagement.CacheConfig;
import com.itorix.apiwiz.common.model.configmanagement.KVMConfig;
import com.itorix.apiwiz.common.model.configmanagement.ServiceRequest;
import com.itorix.apiwiz.common.model.configmanagement.TargetConfig;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.proxystudio.Env;
import com.itorix.apiwiz.common.model.proxystudio.OrgEnv;
import com.itorix.apiwiz.common.model.proxystudio.OrgEnvs;
import com.itorix.apiwiz.common.model.proxystudio.ProxyArtifacts;
import com.itorix.apiwiz.common.model.proxystudio.ProxyData;
import com.itorix.apiwiz.common.model.proxystudio.apigeeassociations.Deployments;
import com.itorix.apiwiz.common.model.proxystudio.apigeeassociations.ProxyApigeeDetails;
import com.itorix.apiwiz.devstudio.businessImpl.ApigeeDetails;
import com.itorix.apiwiz.devstudio.model.Operations;
import com.itorix.apiwiz.devstudio.model.ProxyHistoryResponse;
import com.itorix.apiwiz.identitymanagement.model.Pagination;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.mongodb.operation.OrderBy;
import com.mongodb.MongoClient;

@Component("mongoConnection")
public class MongoConnection {

	Logger logger = Logger.getLogger(MongoConnection.class);

	private MongoClient mongo;

	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private ApigeeDetails apigeeDetails;
	@Autowired
	private Operations operations;

	private DB getDB() {
		MongoDatabase mongoDatabase = mongoTemplate.getDb();
		return mongoTemplate.getMongoDbFactory().getLegacyDb();
		// return mongoTemplate.getDb();
	}

	public String getFile(String fileName) {
		String reader = null;
		DB db = getDB();
		try {
			GridFS gfs = new GridFS(db, "Files");
			GridFSDBFile file = gfs.findOne(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
			StringBuilder stringBuilder = new StringBuilder();
			String ls = System.getProperty("line.separator");
			String line = null;
			while ((line = br.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
			br.close();
			reader = stringBuilder.toString();
		} catch (MongoException e) {
			e.printStackTrace();
			logger.error("MongoConnection::getFile " + fileName + " : " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("MongoConnection::getFile " + fileName + " : " + e.getMessage());
		} finally {
			if (mongo != null)
				mongo.close();
		}
		return reader;
	}

	public String updateDocument(String content, String updateContent) {
		String reader = null;
		try {
			DB db = getDB();
			DBCollection collection = db.getCollection("Connectors.Apigee.Build.Templates.Folder");
			BasicDBObject document = new BasicDBObject();
			document.put("content", content);
			BasicDBObject updateDocument = new BasicDBObject();
			updateDocument.put("content", updateContent);
			DBCursor cursor = collection.find();
			while (cursor.hasNext()) {
				DBObject tmp = cursor.next();
				collection.remove(tmp);
			}
			collection.insert(updateDocument);
		} catch (MongoException e) {
			e.printStackTrace();
			logger.error("MongoConnection::updateDocument " + e.getMessage());
		} finally {
			if (mongo != null)
				mongo.close();
		}
		return reader;
	}

	private String updateDocument() {
		String reader = "{\"name\":\"API\",\"folder\":true}";
		try {
			DB db = getDB();
			DBCollection collection = db.getCollection("Connectors.Apigee.Build.Templates.Folder");
			BasicDBObject updateDocument = new BasicDBObject();
			updateDocument.put("content", reader);
			collection.insert(updateDocument);
		} catch (MongoException e) {
			e.printStackTrace();
			logger.error("MongoConnection::updateDocument " + e.getMessage());
		} finally {
			if (mongo != null)
				mongo.close();
		}
		return reader;
	}

	public String getFolder() {
		String reader = null;
		try {
			DB db = getDB();
			DBCollection collection = db.getCollection("Connectors.Apigee.Build.Templates.Folder");
			DBCursor cursor = collection.find();
			DBObject content = cursor.next();
			reader = (String) content.get("content");
		} catch (Exception e) {
			reader = updateDocument();
			e.printStackTrace();
			logger.error("MongoConnection::getFolder " + e.getMessage());
		} finally {
			if (mongo != null)
				mongo.close();
		}
		return reader;
	}

	public boolean insertFile(MultipartFile file) throws IOException {
		try {
			InputStream inStream = file.getInputStream();
			String fileName = file.getOriginalFilename();
			return insertFile(inStream, fileName);
		} catch (MongoException e) {
			e.printStackTrace();
			logger.error("MongoConnection::insertFile " + e.getMessage());
			throw e;
		} finally {
			if (mongo != null)
				mongo.close();
		}
	}

	public boolean insertFile(InputStream inStream, String fileName) throws IOException {
		try {
			DB db = getDB();
			GridFS gfs = new GridFS(db, "Files");
			GridFSDBFile dbFile = gfs.findOne(fileName);
			if (dbFile != null) {
				gfs.remove(fileName);
			}
			GridFSInputFile gfsFile = gfs.createFile(inStream);
			gfsFile.setFilename(fileName);
			gfsFile.save();
			return true;
		} catch (MongoException e) {
			e.printStackTrace();
			logger.error("MongoConnection::insertFile " + e.getMessage());
			throw e;
		} finally {
			if (mongo != null)
				mongo.close();
		}
	}

	public boolean removeFile(String fileName) throws IOException {
		try {
			DB db = getDB();
			GridFS gfs = new GridFS(db, "Files");

			GridFSDBFile dbFile = gfs.findOne(fileName);

			if (dbFile != null) {
				gfs.remove(fileName);
				return true;
			}
			return false;
		} catch (MongoException e) {
			e.printStackTrace();
			logger.error("MongoConnection::removeFile " + e.getMessage());
			throw e;
		} finally {
			if (mongo != null)
				mongo.close();
		}
	}

	public boolean saveProxyHistory(ProxyData data) throws JsonProcessingException {
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("proxyName").is(data.getProxyName()));
			ProxyData dbProxyData = mongoTemplate.findOne(query, ProxyData.class);
			if (dbProxyData != null)
				data.setId(dbProxyData.getId());
			mongoTemplate.save(data);
			return true;

		} catch (MongoException e) {
			e.printStackTrace();
			logger.error("MongoConnection::saveProxyHistory " + e.getMessage());
			throw e;
		} finally {
			if (mongo != null)
				mongo.close();
		}
	}

	public List<String> getProxyNames() {
		return mongoTemplate.findDistinct("proxyName", ProxyData.class, String.class);
	}

	public ProxyData getProxyHistory(String proxyName) {
		try {
			ProxyData dbProxyData = mongoTemplate.findById(proxyName, ProxyData.class);
			if (dbProxyData == null) {
				Query query = new Query();
				query.addCriteria(Criteria.where("proxyName").is(proxyName));
				dbProxyData = mongoTemplate.findOne(query, ProxyData.class);
			}
			return dbProxyData;

		} catch (MongoException e) {
			e.printStackTrace();
			logger.error("MongoConnection::getProxyHistory " + e.getMessage());
			throw e;
		} finally {
			if (mongo != null)
				mongo.close();
		}
	}

	public List<String> getProxyHistory() {
		List<String> history = new ArrayList<String>();
		try {
			DB db = getDB();
			DBCollection collection = db.getCollection("Build.Proxy");
			DBCursor cursor = collection.find();
			while (cursor.hasNext()) {
				DBObject content = cursor.next();
				history.add((String) content.get("ProxyData"));
			}
		} catch (MongoException e) {
			e.printStackTrace();
			logger.error("MongoConnection::getProxyHistory " + e.getMessage());
			throw e;
		} finally {
			if (mongo != null)
				mongo.close();
		}
		return history;
	}

	public ProxyHistoryResponse getProxyHistory(int offset, int pageSize, String proxy) {
		ProxyHistoryResponse response = new ProxyHistoryResponse();
		long totalRecords = 0;
		List<ProxyData> dbProxyData = null;
		try {
			Query query;
			if (proxy != null)
				query = new Query().addCriteria(Criteria.where("_id").is(proxy));
			else
				query = new Query().with(Sort.by(Direction.DESC, "dateModified"))
						.skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);

			dbProxyData = mongoTemplate.find(query, ProxyData.class);
			totalRecords = mongoTemplate.count(new Query(), ProxyData.class);
		} catch (MongoException e) {
			e.printStackTrace();
			logger.error("MongoConnection::getProxyHistory " + e.getMessage());
			throw e;
		} finally {
			if (mongo != null)
				mongo.close();
		}
		Pagination pagination = new Pagination();
		pagination.setOffset(offset);
		pagination.setTotal(totalRecords);
		pagination.setPageSize(pageSize);
		response.setPagination(pagination);
		response.setData(dbProxyData);
		return response;
	}

	public ProxyData saveProxyDetails(String proxy) throws JsonParseException, JsonMappingException, IOException {
		ProxyData proxyData = getProxyHistory(proxy);
		proxyData = apigeeDetails.getDetails(proxyData, operations.getUser());
		saveProxyHistory(proxyData);
		return proxyData;
	}

	public ProxyData saveProxyDetailsByOrgEnv(String proxy, String org, String env, String type)
			throws JsonParseException, JsonMappingException, IOException {
		ProxyData proxyData = getProxyHistory(proxy);
		ProxyApigeeDetails proxyApigeeDetails = apigeeDetails.getDetailsByProxy(proxyData, org, env, type);
		proxyData.setProxyApigeeDetails(proxyApigeeDetails);
		saveProxyHistory(proxyData);
		return proxyData;
	}

	public ProxyData saveProxyDetailsByOrgEnv(String proxy, OrgEnv orgEnv)
			throws JsonParseException, JsonMappingException, IOException {
		String deployedStatus = "";
		ProxyData proxyData = getProxyHistory(proxy);
		for (Env env : orgEnv.getEnvs()) {
			ProxyApigeeDetails proxyApigeeDetails = apigeeDetails.getDetailsByProxy(proxyData, orgEnv.getName(),
					env.getName(), orgEnv.getType());
			proxyData.setProxyApigeeDetails(proxyApigeeDetails);
			List<Deployments> deployments = proxyData.getProxyApigeeDetails().getDeployments();
			String revision = "created";
			try {
				for (Deployments deployment : deployments) {
					if (deployment.getOrg().equals(orgEnv.getName()) && deployment.getEnv().equals(env.getName())) {
						List<com.itorix.apiwiz.common.model.proxystudio.apigeeassociations.Proxy> proxiesList = deployment
								.getProxies();
						com.itorix.apiwiz.common.model.proxystudio.apigeeassociations.Proxy deployedProxy = proxiesList
								.get(0);
						revision = deployedProxy.getRevision();
					}
				}
			} catch (Exception e) {
			}
			deployedStatus = (revision != null && revision != "") ? "deployed" : "created";
			env.setStatus(deployedStatus);
		}
		return getOrgInfo(proxyData, orgEnv);
	}

	private ProxyData getOrgInfo(ProxyData proxyData, OrgEnv orgEnv) throws JsonProcessingException {
		OrgEnvs orgs = proxyData.getOrgEnvs();
		if (orgs != null) {
			boolean orgFound = false;
			for (OrgEnv proxyOrgEnv : orgs.getOrgEnvs()) {
				if (proxyOrgEnv.getName().equals(orgEnv.getName()) && proxyOrgEnv.getType().equals(orgEnv.getType())) {
					orgFound = true;
					boolean envFound = false;
					for (Env proxyEnv : proxyOrgEnv.getEnvs()) {
						if (proxyEnv.getName().equals(orgEnv.getEnvs().get(0).getName())) {
							proxyEnv.setStatus(orgEnv.getEnvs().get(0).getStatus());
							envFound = true;
						}
					}
					if (!envFound) {
						proxyOrgEnv.getEnvs().add(orgEnv.getEnvs().get(0));
					}
				}
			}
			if (!orgFound) {
				orgs.getOrgEnvs().add(orgEnv);
			}
		} else {
			orgs = new OrgEnvs();
			List<OrgEnv> orgEnvs = new ArrayList<OrgEnv>();
			orgEnvs.add(orgEnv);
			orgs.setOrgEnvs(orgEnvs);
		}
		proxyData.setOrgEnvs(orgs);
		saveProxyHistory(proxyData);
		return proxyData;
	}

	public ProxyData getProxyDetails(String proxy) {
		try {
			return getProxyHistory(proxy);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public OrgEnvs getAssociatedOrgs(String proxy) throws ItorixException {
		ProxyData data;
		try {
			data = getProxyHistory(proxy);
			return data.getOrgEnvs();
		} catch (Exception e) {
			logger.error("MongoConnection::getAssociatedOrgs " + e.getMessage());
			throw new ItorixException(e.getMessage(), "ProxyGen-1000", e);
		}
	}

	public boolean updateAssociatedOrgs(String proxy, OrgEnvs orgs) throws ItorixException {
		try {

			ProxyData data = getProxyHistory(proxy);
			data.setOrgEnvs(orgs);
			return saveProxyHistory(data);
		} catch (JsonParseException e) {
			e.printStackTrace();
			throw new ItorixException(e.getMessage(), "ProxyGen-1000", e);
		} catch (JsonMappingException e) {
			e.printStackTrace();
			throw new ItorixException(e.getMessage(), "ProxyGen-1000", e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ItorixException(e.getMessage(), "ProxyGen-1000", e);
		}
	}

	public boolean updateArtifacts(String proxy, ProxyArtifacts proxyArtifacts) throws ItorixException {
		try {
			ProxyData data = getProxyHistory(proxy);
			ProxyArtifacts dataArtifacts = data.getProxyArtifacts();
			if (dataArtifacts != null) {
				if (proxyArtifacts.getCaches() != null)
					dataArtifacts.setCaches(proxyArtifacts.getCaches());
				if (proxyArtifacts.getKvms() != null)
					dataArtifacts.setKvms(proxyArtifacts.getKvms());
				if (proxyArtifacts.getTargetServers() != null)
					dataArtifacts.setTargetServers(proxyArtifacts.getTargetServers());
				data.setProxyArtifacts(dataArtifacts);
			} else {
				data.setProxyArtifacts(proxyArtifacts);
			}
			return saveProxyHistory(data);
		} catch (JsonParseException e) {
			e.printStackTrace();
			throw new ItorixException(e.getMessage(), "ProxyGen-1000", e);
		} catch (JsonMappingException e) {
			e.printStackTrace();
			throw new ItorixException(e.getMessage(), "ProxyGen-1000", e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ItorixException(e.getMessage(), "ProxyGen-1000", e);
		}
	}

	public List<String> getActiveSchedules() {
		List<String> history = new ArrayList<String>();
		try {
			DB db = getDB();
			DBCollection collection = db.getCollection("Build.Schedule");
			BasicDBObject whereQuery = new BasicDBObject();
			whereQuery.put("status", "scheduled");
			DBCursor cursor = collection.find(whereQuery);
			while (cursor.hasNext()) {
				DBObject content = cursor.next();
				history.add((String) content.get("schedulerData"));
			}
		} catch (MongoException e) {
			e.printStackTrace();
			logger.error("MongoConnection::getActiveSchedules " + e.getMessage());
			throw e;
		} finally {
			if (mongo != null)
				mongo.close();
		}
		return history;
	}

	public String deleteScheduler(String id) throws ItorixException {
		try {
			DB db = getDB();
			DBCollection collection = db.getCollection("Build.Schedule");

			if (getScheduler(id) == null) {
				throw new ItorixException("No scheduler found with ID " + id, "ProxyGen-1002");
			} else {
				BasicDBObject searchQuery = new BasicDBObject().append("id", id);
				collection.remove(searchQuery);
				return "true";
			}
		} catch (MongoException e) {
			e.printStackTrace();
			logger.error("MongoConnection::deleteScheduler " + e.getMessage());
			throw e;
		} finally {
			if (mongo != null)
				mongo.close();
		}
	}

	public String getScheduler(String id) {
		if (id == null)
			return null;
		String data = null;
		try {
			DB db = getDB();
			DBCollection collection = db.getCollection("Build.Schedule");
			BasicDBObject whereQuery = new BasicDBObject();
			whereQuery.put("id", id);
			DBCursor cursor = collection.find(whereQuery);
			while (cursor.hasNext()) {
				DBObject content = cursor.next();
				data = (String) content.get("schedulerData");
			}
		} catch (MongoException e) {
			e.printStackTrace();
			logger.error("MongoConnection::getScheduler " + e.getMessage());
			throw e;
		} finally {
			if (mongo != null)
				mongo.close();
		}
		return data;
	}

	public List<String> getSchedulers() {
		List<String> schedulers = new ArrayList<String>();
		try {
			DB db = getDB();
			DBCollection collection = db.getCollection("Build.Schedule");
			DBCursor cursor = collection.find();
			while (cursor.hasNext()) {
				DBObject content = cursor.next();
				schedulers.add((String) content.get("schedulerData"));
			}
		} catch (MongoException e) {
			e.printStackTrace();
			logger.error("MongoConnection::getSchedulers " + e.getMessage());
			throw e;
		} finally {
			if (mongo != null)
				mongo.close();
		}
		return schedulers;
	}

	private String getUniqueId() {
		String string = Instant.now().toString();
		byte[] byteArray = Base64.encodeBase64(string.getBytes());
		String encodedString = new String(byteArray);
		return encodedString;
	}

	public String getResourceFile(String projectName, String proxyName, String type, String fileName,
			String destinationLocation) {
		String projectFileName = findProjectFile(projectName, proxyName, type, fileName);
		System.out.println("file name : " + projectFileName);
		String reader = null;
		DB db = getDB();
		try {
			GridFS gfs = new GridFS(db, "Project.Files");
			GridFSDBFile file = gfs.findOne(projectFileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
			StringBuilder stringBuilder = new StringBuilder();
			String ls = System.getProperty("line.separator");
			String line = null;
			while ((line = br.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
			br.close();
			reader = stringBuilder.toString();
		} catch (MongoException e) {
			e.printStackTrace();
			logger.error("MongoConnection::getFile " + projectFileName + " : " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("MongoConnection::getFile " + projectFileName + " : " + e.getMessage());
		} finally {
			if (mongo != null)
				mongo.close();
		}
		try {
			reader = reader.replaceAll("schemaLocation=\"", "schemaLocation=\"xsd://").replaceAll("\" location=\"",
					"\" location=\"wsdl://");
			FileUtils.writeStringToFile(new File(destinationLocation + fileName), reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return reader;
	}

	public String findProjectFile(String projectName, String proxyName, String type, String fileName) {
		String location = null;
		try {
			DB db = getDB();
			DBCollection collection = db.getCollection("Plan.ProjectFiles");
			BasicDBObject whereQuery = new BasicDBObject();
			whereQuery.put("projectName", projectName);
			whereQuery.put("proxyName", proxyName);
			whereQuery.put("type", type);
			whereQuery.put("fileName", fileName);
			DBCursor cursor = collection.find(whereQuery);
			while (cursor.hasNext()) {
				DBObject content = cursor.next();
				location = (String) content.get("location");
			}
		} catch (MongoException e) {
			e.printStackTrace();
			logger.error("MongoConnection::getProxyHistory " + e.getMessage());
			throw e;
		} finally {
			if (mongo != null)
				mongo.close();
		}
		return location;
	}

	public List<OrgEnv> getApigeeOrgs() {
		List<ApigeeConfigurationVO> apigeeOrgs = mongoTemplate.findAll(ApigeeConfigurationVO.class);
		if (apigeeOrgs != null) {
			List<OrgEnv> orgList = new ArrayList<>();
			for (ApigeeConfigurationVO apigeeConfigurationVO : apigeeOrgs) {
				OrgEnv org = new OrgEnv();
				org.setName(apigeeConfigurationVO.getOrgname());
				org.setType(apigeeConfigurationVO.getType());
				if (apigeeConfigurationVO.getEnvironments() != null) {
					orgList.add(org);
					List<Env> envs = new ArrayList<>();
					org.setEnvs(envs);
					for (String apigeeEnv : apigeeConfigurationVO.getEnvironments()) {
						Env env = new Env();
						env.setName(apigeeEnv);
						env.setStatus("new");
						envs.add(env);
					}
				}
			}
			return orgList;
		}
		return null;
	}

	public List<KVMConfig> getKVM(String name) throws ItorixException {
		try {
			if (name != null) {
				Query query = new Query(Criteria.where("name").is(name).and("activeFlag").is(Boolean.TRUE));
				List<KVMConfig> obj = mongoTemplate.find(query, KVMConfig.class);
				return obj;
			} else {
				throw new ItorixException("No Record exists", "Configuration-1003");
			}
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public List<ServiceRequest> getKVMRequests(String name) throws ItorixException {
		try {
			if (name != null) {
				Query query = new Query(Criteria.where("activeFlag").is(Boolean.TRUE));
				query.addCriteria(Criteria.where("type").is("KVM"));
				query.addCriteria(Criteria.where("name").is(name));
				List<ServiceRequest> requests = mongoTemplate.find(query, ServiceRequest.class);
				return requests;
			} else {
				throw new ItorixException("No Record exists", "Configuration-1003");
			}
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public List<ServiceRequest> getCacheRequests(String name) throws ItorixException {
		try {
			if (name != null) {
				Query query = new Query(Criteria.where("activeFlag").is(Boolean.TRUE));
				query.addCriteria(Criteria.where("type").is("Cache"));
				query.addCriteria(Criteria.where("name").is(name));
				List<ServiceRequest> requests = mongoTemplate.find(query, ServiceRequest.class);
				return requests;
			} else {
				throw new ItorixException("No Record exists", "Configuration-1003");
			}
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public List<ServiceRequest> getTargetRequests(String name) throws ItorixException {
		try {
			if (name != null) {
				Query query = new Query(Criteria.where("activeFlag").is(Boolean.TRUE));
				query.addCriteria(Criteria.where("type").is("TargetServer"));
				query.addCriteria(Criteria.where("name").is(name));
				List<ServiceRequest> requests = mongoTemplate.find(query, ServiceRequest.class);
				return requests;
			} else {
				throw new ItorixException("No Record exists", "Configuration-1003");
			}
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public List<CacheConfig> getCache(String name) throws ItorixException {
		try {
			if (name != null) {
				Query query = new Query(Criteria.where("name").is(name).and("activeFlag").is(true));
				return mongoTemplate.find(query, CacheConfig.class);
			} else {
				throw new ItorixException("No Record exists", "Configuration-1003");
			}
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public List<TargetConfig> getTarget(String name) throws ItorixException {
		try {
			if (name != null) {
				Query query = new Query(Criteria.where("name").is(name).and("activeFlag").is(Boolean.TRUE));
				return mongoTemplate.find(query, TargetConfig.class);
			} else {
				throw new ItorixException("No Record exists", "Configuration-1003");
			}
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public Object removeProxy(String proxy) {
		try {
			ProxyData dbProxyData = mongoTemplate.findById(proxy, ProxyData.class);
			if (dbProxyData == null) {
				Query query = new Query();
				query.addCriteria(Criteria.where("proxyName").is(proxy));
				dbProxyData = mongoTemplate.findOne(query, ProxyData.class);
			}
			mongoTemplate.remove(dbProxyData);

		} catch (MongoException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (mongo != null)
				mongo.close();
		}
		return null;
	}
}
