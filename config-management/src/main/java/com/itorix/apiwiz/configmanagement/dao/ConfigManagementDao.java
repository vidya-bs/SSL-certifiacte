package com.itorix.apiwiz.configmanagement.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.common.model.SearchItem;
import com.itorix.apiwiz.common.model.configmanagement.*;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.common.util.http.HTTPUtil;
import com.itorix.apiwiz.configmanagement.model.apigee.ApigeeCache;
import com.itorix.apiwiz.configmanagement.model.apigee.ApigeeKVM;
import com.itorix.apiwiz.configmanagement.model.apigee.ApigeeProduct;
import com.itorix.apiwiz.configmanagement.model.apigee.ApigeeTarget;
import com.itorix.apiwiz.configmanagement.model.apigee.services.CacheService;
import com.itorix.apiwiz.configmanagement.model.apigee.services.KVMService;
import com.itorix.apiwiz.configmanagement.model.apigee.services.ProductService;
import com.itorix.apiwiz.configmanagement.model.apigee.services.TargetConnection;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.*;
@Slf4j
@Component
public class ConfigManagementDao {

	private static final Logger logger = LoggerFactory.getLogger(ConfigManagementDao.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private TargetConnection targetConn;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private KVMService kVMService;

	@Autowired
	private ProductService productService;

	@Autowired
	private ApigeeUtil apigeeUtil;

	public boolean saveTarget(TargetConfig config) throws ItorixException {
		try {
			List<TargetConfig> obj = getTargets(config);
			ConfigMetadata metadata = config.getMetadata();
			metadata.setResourceType("target");

			if (obj.size() > 0) {
				Query query = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv())
						.and("name").is(config.getName()).and("type").is(config.getType()));
				Update update = new Update();
				update.set("activeFlag", false);
				UpdateResult result = mongoTemplate.updateMulti(query, update, TargetConfig.class);
			}
			mongoTemplate.insert(config);
			saveMetadata(metadata);
			return true;

		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public List<TargetConfig> getTargetSummary() throws ItorixException {
		logger.info("inside getTargetSummary(): Start");
		try {
			List<TargetConfig> targets = (ArrayList<TargetConfig>) getAllActiveTargets(null);
			for (TargetConfig target : targets) {
				target.trimData();
			}
			// logger.info("inside getTargetSummary(): end");
			return targets;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	/*
	 * private List<TargetConfig> getTargets() throws ItorixException { try {
	 * logger.info("inside getTargets(): Start"); List<TargetConfig> targetList;
	 * targetList = mongoTemplate.findAll(TargetConfig.class);
	 * logger.info("inside getTargets(): end"); return targetList; } catch
	 * (Exception ex) { throw new ItorixException(ex.getMessage(),
	 * "Configuration-1000", ex); } }
	 */

	public List<TargetConfig> getTargets(TargetConfig config) throws ItorixException {
		try {
			if (config != null) {
				Query query = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv())
						.and("name").is(config.getName()).and("type").is(config.getType()));
				return mongoTemplate.find(query, TargetConfig.class);
			} else {
				throw new ItorixException("", "Configuration-1000");
			}
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public List<TargetConfig> getAllActiveTargets(TargetConfig config) throws ItorixException {
		try {
			if (config != null) {
				Query query = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv())
						.and("name").is(config.getName()).and("type").is(config.getType()).and("activeFlag")
						.is(Boolean.TRUE));
				return mongoTemplate.find(query, TargetConfig.class);
			} else {
				Query query = new Query(Criteria.where("activeFlag").is(Boolean.TRUE));
				return mongoTemplate.find(query, TargetConfig.class);
			}
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public boolean saveMetadata(ConfigMetadata config) throws ItorixException {
		try {
			ConfigMetadata obj = getMetadata(config.getName(), config.getResourceType());
			if (obj == null) {
				mongoTemplate.save(config);
			} else {
				config.setCreatedDate(obj.getCreatedDate());
				config.setCreatedUser(obj.getCreatedUser());
				Update update = new Update();
				update.set("resourceType", config.getResourceType());
				update.set("name", config.getName());
				update.set("createdUser", config.getCreatedUser());
				update.set("modifiedUser", config.getModifiedUser());
				update.set("createdDate", config.getCreatedDate());
				update.set("modifiedDate", config.getModifiedDate());
				Query query = new Query(
						Criteria.where("resourceType").is(config.getResourceType()).and("name").is(config.getName()));
				mongoTemplate.updateFirst(query, update, ConfigMetadata.class);
			}
			return true;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public ConfigMetadata getMetadata(String name, String type) throws ItorixException {
		try {
			if (name != null && type != null) {
				Query query = new Query(Criteria.where("name").is(name).and("resourceType").is(type));
				List<ConfigMetadata> list = mongoTemplate.find(query, ConfigMetadata.class);
				return list.get(0);
			} else {
				return null;
			}
		} catch (Exception ex) {
			return null;
		}
	}

	public boolean deleteTarget(String targetName) throws ItorixException {
		try {
			Query query1 = new Query(Criteria.where("name").is(targetName).and("activeFlag").is(Boolean.TRUE));
			List<TargetConfig> targets = mongoTemplate.find(query1, TargetConfig.class);
			if(targets != null) {
				for (TargetConfig targetConfig : targets) {
					try {
						deleteApigeeTarget( targetConfig);
					}catch(Exception e) {
						logger.error(e.getMessage(),e);
						e.printStackTrace();
					}
				}
			}
			Query query = new Query(Criteria.where("name").is(targetName));
			DeleteResult result = mongoTemplate.remove(query, TargetConfig.class);
			if (result.getDeletedCount() > 0)
				return result.wasAcknowledged();
			else
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Configuration-1023"), targetName),
						"Configuration-1023");
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public boolean deleteTarget(TargetConfig config) throws ItorixException {
		try {
			
			Query query1 = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv())
					.and("name").is(config.getName()).and("type").is(config.getType()).and("activeFlag")
					.is(Boolean.TRUE));
			List<TargetConfig> targets = mongoTemplate.find(query1, TargetConfig.class);
			if(targets != null) {
				for (TargetConfig targetConfig : targets) {
					try {
						deleteApigeeTarget( targetConfig);
					}catch(Exception e) {
						logger.error(e.getMessage(),e);
						e.printStackTrace();
					}
				}
			}
			
			Query query = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv()).and("name")
					.is(config.getName()).and("type").is(config.getType()));
			DeleteResult result = mongoTemplate.remove(query, TargetConfig.class);
			if (result.getDeletedCount() > 0)
				return result.wasAcknowledged();
			else
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Configuration-1022"),
						config.getName(), config.getOrg(), config.getEnv()), "Configuration-1022");
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public boolean updateTarget(TargetConfig config) throws ItorixException {
		try {
			config.setActiveFlag(Boolean.TRUE);
			return saveTarget(config);

		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	@SuppressWarnings("unchecked")
	public Object getTargetList() throws ItorixException {
		try {
			List listData = new ArrayList();
			List<TargetConfig> targets = (ArrayList<TargetConfig>) getAllActiveTargets(null);
			if (targets != null && targets.size() > 0) {
				Set<String> avlTargets = new HashSet<String>();
				for (TargetConfig target : targets) {
					avlTargets.add(target.getName());
				}
				for (String target : avlTargets) {
					ConfigMetadata metadata = getMetadata(target, "target");
					Map targetMap = new HashMap();
					targetMap.put("name", target);
					if (metadata != null) {
						targetMap.put("createdBy", metadata.getCreatedUser());
						targetMap.put("createdDate", metadata.getCreatedDate());
						targetMap.put("modifiedBy", metadata.getModifiedUser());
						targetMap.put("modifiedDate", metadata.getModifiedDate());
					}
					listData.add(targetMap);
				}
			}
			return listData;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public List<TargetConfig> getTarget(String name) throws ItorixException {
		try {
			if (name != null) {
				Query query = new Query(Criteria.where("name").is(name).and("activeFlag").is(Boolean.TRUE));
				List<TargetConfig> obj = mongoTemplate.find(query, TargetConfig.class);
				if (obj.size() > 0)
					return obj;
				else
					throw new ItorixException("No Record exists", "Configuration-1003");
			} else {
				throw new ItorixException("No Record exists", "Configuration-1003");
			}
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public Object createApigeeTarget(TargetConfig config, User user) throws ItorixException {
		try {
			@SuppressWarnings("unchecked")
			List<TargetConfig> data = (ArrayList) getAllActiveTargets(config);
			TargetConfig targetConfig = data.get(0);
			ApigeeTarget target = targetConn.getTargetBody(targetConfig);
			String URL = targetConn.getTargetURL(targetConfig);
			logger.debug("Target Server Request {} with body {} ", URL, target);
			if (isResourceAvailable(targetConn.getUpdateTargetURL(targetConfig),
					getApigeeCredentials(targetConfig.getOrg(), targetConfig.getType()))) {
				return updateApigeeTarget(config, user);
			} else {
				HTTPUtil httpConn = new HTTPUtil(target, URL,
						getApigeeCredentials(targetConfig.getOrg(), targetConfig.getType()));
				ResponseEntity<String> response = httpConn.doPost();
				HttpStatus statusCode = response.getStatusCode();
				if (statusCode.is2xxSuccessful())
					return true;
				else if (statusCode.value() >= 401 && statusCode.value() <= 403)
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1025"), "Configuration-1025");
				else
					throw new ItorixException("invalid request data " + statusCode.value(), "Configuration-1000");
			}
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}
	
	public Object deleteApigeeTarget(TargetConfig config) throws ItorixException {
		try {
			@SuppressWarnings("unchecked")
			List<TargetConfig> data = (ArrayList) getAllActiveTargets(config);
			TargetConfig targetConfig = data.get(0);
			//ApigeeTarget target = targetConn.getTargetBody(targetConfig);
			String URL = targetConn.getUpdateTargetURL(targetConfig);
			HTTPUtil httpConn = new HTTPUtil( URL,
					getApigeeCredentials(targetConfig.getOrg(), targetConfig.getType()));
			ResponseEntity<String> response = httpConn.doDelete();
			HttpStatus statusCode = response.getStatusCode();
			if (statusCode.is2xxSuccessful())
				return true;
			else if (statusCode.value() >= 401 && statusCode.value() <= 403)
				throw new ItorixException(
						"Request validation failed. Exception connecting to apigee connector. " + statusCode.value(),
						"Configuration-1006");
			else
				throw new ItorixException("invalid request data " + statusCode.value(), "Configuration-1000");
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	private boolean isResourceAvailable(String URL, String credentials) {
		boolean isPresent = false;
		try {
			HTTPUtil httpConn = new HTTPUtil(null, URL, credentials);
			ResponseEntity<String> response = httpConn.doGet();
			HttpStatus statusCode = response.getStatusCode();
			if (statusCode.is2xxSuccessful())
				isPresent = true;
		} catch (Exception ex) {
			log.error("Exception occurred", ex);
		}
		return isPresent;
	}

	public Object updateApigeeTarget(TargetConfig config, User user) throws ItorixException {
		try {
			@SuppressWarnings("unchecked")
			List<TargetConfig> data = (ArrayList) getAllActiveTargets(config);
			TargetConfig targetConfig = data.get(0);
			ApigeeTarget target = targetConn.getTargetBody(targetConfig);
			String URL = targetConn.getUpdateTargetURL(targetConfig);
			HTTPUtil httpConn = new HTTPUtil(target, URL,
					getApigeeCredentials(targetConfig.getOrg(), targetConfig.getType()));
			ResponseEntity<String> response = httpConn.doPost();
			HttpStatus statusCode = response.getStatusCode();
			if (statusCode.is2xxSuccessful())
				return true;
			else if (statusCode.value() >= 401 && statusCode.value() <= 403)
				throw new ItorixException(
						"Request validation failed. Exception connecting to apigee connector. " + statusCode.value(),
						"Configuration-1006");
			else
				throw new ItorixException("invalid request data " + statusCode.value(), "Configuration-1000");
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	private String getApigeeCredentials(String org, String type) {
		return apigeeUtil.getApigeeAuth(org, type);
	}

	public boolean saveCache(CacheConfig config) throws ItorixException {
		try {
			List<CacheConfig> cacheConfigs = (ArrayList) getCaches(config);
			ConfigMetadata metadata = config.getMetadata();
			metadata.setResourceType("cache");

			if (cacheConfigs.size() > 0) {
				Query query = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv())
						.and("name").is(config.getName()).and("type").is(config.getType()));
				Update update = new Update();
				update.set("activeFlag", false);
				UpdateResult result = mongoTemplate.updateMulti(query, update, CacheConfig.class);
			}
			mongoTemplate.insert(config);
			saveMetadata(metadata);
			return true;

		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public List<CacheConfig> getCaches(CacheConfig config) throws ItorixException {
		try {
			if (config != null) {
				Query query = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv())
						.and("name").is(config.getName()).and("type").is(config.getType()));
				return mongoTemplate.find(query, CacheConfig.class);
			} else {
				return mongoTemplate.findAll(CacheConfig.class);
			}
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public List<CacheConfig> getAllActiveCaches(CacheConfig config) throws ItorixException {
		try {
			if (config != null) {
				Query query = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv())
						.and("name").is(config.getName()).and("type").is(config.getType()).and("activeFlag")
						.is(Boolean.TRUE));
				return mongoTemplate.find(query, CacheConfig.class);
			} else {
				Query query = new Query(Criteria.where("activeFlag").is(Boolean.TRUE));
				return mongoTemplate.find(query, CacheConfig.class);
			}
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public boolean updateCache(CacheConfig config) throws ItorixException {
		try {
			/*
			 * @SuppressWarnings("unchecked") List<CacheConfig> caches =
			 * (List<CacheConfig>) getCaches(config); CacheConfig cache =
			 * caches.get(0); try { if (cache != null) {
			 * config.setCreatedUser(cache.getCreatedUser());
			 * config.setCreatedDate(cache.getCreatedDate()); } } catch
			 * (Exception e) { } ConfigMetadata metadata = config.getMetadata();
			 * metadata.setResourceType("cache"); Query query = new
			 * Query(Criteria.where("org").is(config.getOrg()).and("env").is(
			 * config.getEnv()).and("name")
			 * .is(config.getName()).and("type").is(config.getType())); DBObject
			 * dbDoc = new BasicDBObject();
			 * mongoTemplate.getConverter().write(config, dbDoc); Update update
			 * = Update.fromDBObject(dbDoc, "_id"); WriteResult result =
			 * mongoTemplate.updateFirst(query, update, CacheConfig.class); if
			 * (result.isUpdateOfExisting()) { saveMetadata(metadata); return
			 * result.isUpdateOfExisting(); } else throw new
			 * ItorixException(String.format(ErrorCodes.errorMessage.get(
			 * "Configuration-1012"),config.getName(),config.getOrg(),config.
			 * getEnv () ),"Configuration-1012");
			 */
			config.setActiveFlag(Boolean.TRUE);
			return saveCache(config);
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public boolean deleteCache(CacheConfig config) throws ItorixException {
		try {
			Query query1 = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv())
					.and("name").is(config.getName()).and("type").is(config.getType()).and("activeFlag")
					.is(Boolean.TRUE));
			List<CacheConfig> cacheConfigs = mongoTemplate.find(query1, CacheConfig.class);
			if(cacheConfigs != null) {
				for (CacheConfig cacheConfig : cacheConfigs) {
					try {
						deleteApigeeCache( cacheConfig);
					}catch(Exception e) {
						logger.error(e.getMessage(),e);
						e.printStackTrace();
					}
				}
			}
			Query query = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv()).and("name")
					.is(config.getName()).and("type").is(config.getType()));
			DeleteResult result = mongoTemplate.remove(query, CacheConfig.class);
			if (result.getDeletedCount() > 0)
				return result.wasAcknowledged();
			else
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Configuration-1012"),
						config.getName(), config.getOrg(), config.getEnv()), "Configuration-1012");

		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public boolean deleteCache(String cache) throws ItorixException {
		try {
			Query query1 = new Query(Criteria.where("name").is(cache).and("activeFlag").is(Boolean.TRUE));
			List<CacheConfig> cacheConfigs = mongoTemplate.find(query1, CacheConfig.class);
			if(cacheConfigs != null) {
				for (CacheConfig cacheConfig : cacheConfigs) {
					try {
						deleteApigeeCache(cacheConfig);
					}catch(Exception e) {
						logger.error(e.getMessage(),e);
						e.printStackTrace();
					}
				}
			}
			Query query = new Query(Criteria.where("name").is(cache));
			DeleteResult result = mongoTemplate.remove(query, CacheConfig.class);
			if (result.getDeletedCount() > 0)
				return result.wasAcknowledged();
			else
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Configuration-1013"), cache),
						"Configuration-1013");

		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	@SuppressWarnings("unchecked")
	public Object getCacheSummary() throws ItorixException {
		try {
			List cacheList = new ArrayList();
			List<CacheConfig> caches = (ArrayList<CacheConfig>) getAllActiveCaches(null);
			if (caches != null && caches.size() > 0)
				for (CacheConfig cache : caches) {
					Map<String, String> summary = new HashMap<String, String>();
					summary.put("name", cache.getName());
					summary.put("org", cache.getOrg());
					summary.put("env", cache.getEnv());
					summary.put("createdBy", cache.getCreatedUser());
					summary.put("createdDate", cache.getCreatedDate());
					summary.put("modifiedBy", cache.getModifiedUser());
					summary.put("modifiedDate", cache.getModifiedDate());
					cacheList.add(summary);
				}
			return cacheList;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	@SuppressWarnings("unchecked")
	public Object getCacheList() throws ItorixException {
		try {
			Set<String> cacheList = new HashSet<String>();
			List<CacheConfig> caches = (ArrayList<CacheConfig>) getAllActiveCaches(null);
			for (CacheConfig cache : caches) {
				cacheList.add(cache.getName());
			}
			List listData = new ArrayList();
			for (String cache : cacheList) {
				ConfigMetadata metadata = getMetadata(cache, "cache");
				Map map = new HashMap();
				map.put("name", cache);
				if (metadata != null) {
					map.put("createdBy", metadata.getCreatedUser());
					map.put("createdDate", metadata.getCreatedDate());
					map.put("modifiedBy", metadata.getModifiedUser());
					map.put("modifiedDate", metadata.getModifiedDate());
				}
				listData.add(map);
			}
			return listData;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public Object getCache(String name) throws ItorixException {
		try {
			if (name != null) {
				Query query = new Query(Criteria.where("name").is(name).and("activeFlag").is(true));
				List obj = mongoTemplate.find(query, CacheConfig.class);
				if (obj.size() > 0)
					return obj;
				else
					throw new ItorixException("No Record exists", "Configuration-1003");
			} else {
				throw new ItorixException("No Record exists", "Configuration-1003");
			}
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public Object createApigeeCache(CacheConfig config, User user) throws ItorixException {
		try {
			@SuppressWarnings("unchecked")
			// List<CacheConfig> data = (ArrayList<CacheConfig>)
			// getAllActiveCaches(config);
			CacheConfig cacheConfig = config;
			logger.debug("Creating cache"+ cacheConfig);
			if (isResourceAvailable(cacheService.getUpdateCacheURL(cacheConfig),
					getApigeeCredentials(cacheConfig.getOrg(), cacheConfig.getType()))) {
				return updateApigeeCache(config, user);
			} else {
				logger.debug("Cache is not available"+ cacheConfig.getDescription());
				ApigeeCache cache = cacheService.getCacheBody(cacheConfig);
				String URL = cacheService.getCacheURL(cacheConfig);
				HTTPUtil httpConn = new HTTPUtil(cache, URL,
						getApigeeCredentials(cacheConfig.getOrg(), cacheConfig.getType()));
				ResponseEntity<String> response = httpConn.doPost();
				HttpStatus statusCode = response.getStatusCode();
				if (statusCode.is2xxSuccessful())
					return true;
				else if (statusCode.value() >= 401 && statusCode.value() <= 403)
					// throw new ItorixException("Request validation failed.
					// Exception connecting to apigee
					// connector. " +
					// statusCode.value(), "Configuration-1006");
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1015"), "Configuration-1015");
				else if (statusCode.value() == 409)
					throw new ItorixException("Request resource already available in Apigee " + statusCode.value(),
							"Configuration-1007");
				else
					throw new ItorixException("invalid request data " + statusCode.value(), "Configuration-1000");
			}
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public Object updateApigeeCache(CacheConfig config, User user) throws ItorixException {
		try {
			@SuppressWarnings("unchecked")
			List<CacheConfig> data = (ArrayList) getAllActiveCaches(config);
			CacheConfig cacheConfig = config;
			ApigeeCache cache = cacheService.getCacheBody(cacheConfig);
			String URL = cacheService.getUpdateCacheURL(cacheConfig);
			HTTPUtil httpConn = new HTTPUtil(cache, URL,
					getApigeeCredentials(cacheConfig.getOrg(), cacheConfig.getType()));
			ResponseEntity<String> response = httpConn.doPut();
			HttpStatus statusCode = response.getStatusCode();
			if (statusCode.is2xxSuccessful())
				return true;
			else if (statusCode.value() >= 401 && statusCode.value() <= 403)
				// throw new ItorixException("Request validation failed.
				// Exception connecting to apigee
				// connector. " +
				// statusCode.value(), "Configuration-1006");
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Configuration-1015")),
						"Configuration-1015");
			else
				throw new ItorixException("invalid request data " + statusCode.value(), "Configuration-1000");
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}
	
	public Object deleteApigeeCache(CacheConfig config) throws ItorixException {
		try {
			String URL = cacheService.getUpdateCacheURL(config);
			HTTPUtil httpConn = new HTTPUtil( URL,
					getApigeeCredentials(config.getOrg(), config.getType()));
			ResponseEntity<String> response = httpConn.doDelete();
			HttpStatus statusCode = response.getStatusCode();
			if (statusCode.is2xxSuccessful())
				return true;
			else if (statusCode.value() >= 401 && statusCode.value() <= 403)
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Configuration-1015")),
						"Configuration-1015");
			else
				throw new ItorixException("invalid request data " + statusCode.value(), "Configuration-1000");
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public boolean saveKVM(KVMConfig config) throws ItorixException {
		try {
			List obj = (ArrayList) getKVMs(config);
			ConfigMetadata metadata = config.getMetadata();
			metadata.setResourceType("kvm");

			if (obj.size() > 0) {
				Query query = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv())
						.and("name").is(config.getName()).and("type").is(config.getType()));
				Update update = new Update();
				update.set("activeFlag", false);
				UpdateResult result = mongoTemplate.updateMulti(query, update, KVMConfig.class);
			}
			mongoTemplate.insert(config);
			saveMetadata(metadata);
			return true;

		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	@SuppressWarnings("unchecked")
	public boolean updateKVM(KVMConfig config) throws ItorixException {
		try {
			config.setActiveFlag(Boolean.TRUE);
			return saveKVM(config);
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public Object getKVMs(KVMConfig config) throws ItorixException {
		try {
			if (config != null) {
				Query query = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv())
						.and("name").is(config.getName()).and("type").is(config.getType()));
				return mongoTemplate.find(query, KVMConfig.class);
			} else {
				return mongoTemplate.findAll(KVMConfig.class);
			}
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public Object getAllActiveKVMs(KVMConfig config) throws ItorixException {
		try {
			if (config != null) {
				Query query = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv())
						.and("name").is(config.getName()).and("type").is(config.getType()).and("activeFlag")
						.is(Boolean.TRUE));
				return mongoTemplate.find(query, KVMConfig.class);
			} else {
				Query query = new Query(Criteria.where("activeFlag").is(Boolean.TRUE));
				return mongoTemplate.find(query, KVMConfig.class);
			}
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public boolean deleteKVM(KVMConfig config) throws ItorixException {
		try {
			Query query1 = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv())
					.and("name").is(config.getName()).and("type").is(config.getType()).and("activeFlag")
					.is(Boolean.TRUE));
			List<KVMConfig> kVMConfigs = mongoTemplate.find(query1, KVMConfig.class);
			if(kVMConfigs != null) {
				for (KVMConfig kVMConfig : kVMConfigs) {
					try {
						deleteApigeeKVM( kVMConfig);
					}catch(Exception e) {
						logger.error(e.getMessage(),e);
						e.printStackTrace();
					}
				}
			}
			
			Query query = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv()).and("name")
					.is(config.getName()).and("type").is(config.getType()));
			DeleteResult result = mongoTemplate.remove(query, KVMConfig.class);
			if (result.getDeletedCount() > 0)
				return result.wasAcknowledged();
			else
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Configuration-1017"),
						config.getName(), config.getOrg(), config.getEnv()), "Configuration-1017");
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public boolean deleteKVM(String kvm) throws ItorixException {
		try {
			Query query1 = new Query(Criteria.where("name").is(kvm).and("activeFlag").is(Boolean.TRUE));
			List<KVMConfig> kVMConfigs = mongoTemplate.find(query1, KVMConfig.class);
			if(kVMConfigs != null) {
				for (KVMConfig kVMConfig : kVMConfigs) {
					try {
						deleteApigeeKVM( kVMConfig);
					}catch(Exception e) {
						logger.error(e.getMessage(),e);
						e.printStackTrace();
					}
				}
			}
			Query query = new Query(Criteria.where("name").is(kvm));
			DeleteResult result = mongoTemplate.remove(query, KVMConfig.class);
			if (result.getDeletedCount() > 0)
				return result.wasAcknowledged();
			else
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Configuration-1018"), kvm),
						"Configuration-1018");
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	@SuppressWarnings("unchecked")
	public Object getKVMSummary() throws ItorixException {
		try {
			List kvmList = new ArrayList();
			List<KVMConfig> KVMs = (ArrayList<KVMConfig>) getAllActiveKVMs(null);
			if (KVMs != null && KVMs.size() > 0)
				for (KVMConfig kvm : KVMs) {
					Map<String, String> summary = new HashMap<String, String>();
					summary.put("name", kvm.getName());
					summary.put("org", kvm.getOrg());
					summary.put("env", kvm.getEnv());
					summary.put("createdBy", kvm.getCreatedUser());
					summary.put("createdDate", kvm.getCreatedDate());
					summary.put("modifiedBy", kvm.getModifiedUser());
					summary.put("modifiedDate", kvm.getModifiedDate());
					kvmList.add(summary);
				}
			return kvmList;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	@SuppressWarnings("unchecked")
	public Object getKVMList() throws ItorixException {
		try {
			List<KVMConfig> KVMs = (ArrayList<KVMConfig>) getAllActiveKVMs(null);
			List listData = new ArrayList();
			if (KVMs != null && KVMs.size() > 0) {
				Set<String> avlKVM = new HashSet<String>();
				for (KVMConfig kvm : KVMs) {
					avlKVM.add(kvm.getName());
				}
				for (String kvm : avlKVM) {
					ConfigMetadata metadata = getMetadata(kvm, "kvm");
					Map map = new HashMap();
					map.put("name", kvm);
					if (metadata != null) {
						map.put("createdBy", metadata.getCreatedUser());
						map.put("createdDate", metadata.getCreatedDate());
						map.put("modifiedBy", metadata.getModifiedUser());
						map.put("modifiedDate", metadata.getModifiedDate());
					}
					listData.add(map);
				}
			}
			return listData;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public Object getKVM(String name) throws ItorixException {
		try {
			if (name != null) {
				Query query = new Query(Criteria.where("name").is(name).and("activeFlag").is(Boolean.TRUE));
				List obj = mongoTemplate.find(query, KVMConfig.class);
				if (obj.size() > 0)
					return obj;
				else
					throw new ItorixException("No Record exists", "Configuration-1003");
			} else {
				throw new ItorixException("No Record exists", "Configuration-1003");
			}
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public Object createApigeeKVM(KVMConfig config, User user) throws ItorixException {
		try {
			@SuppressWarnings("unchecked")
			List<KVMConfig> data = (ArrayList) getAllActiveKVMs(config);
			KVMConfig KVMConfig = data.get(0);
			if (isResourceAvailable(kVMService.getUpdateKVMURL(KVMConfig),
					getApigeeCredentials(KVMConfig.getOrg(), KVMConfig.getType()))) {
				return updateApigeeKVM(config, user);
			} else {
				ApigeeKVM kvm = kVMService.getKVMBody(KVMConfig);
				String URL = kVMService.getKVMURL(KVMConfig);
				HTTPUtil httpConn = new HTTPUtil(kvm, URL,
						getApigeeCredentials(KVMConfig.getOrg(), KVMConfig.getType()));
				ResponseEntity<String> response = httpConn.doPost();
				HttpStatus statusCode = response.getStatusCode();
				if (statusCode.is2xxSuccessful())
					return true;
				else if (statusCode.value() >= 401 && statusCode.value() <= 403)
					// throw new ItorixException("Request validation failed.
					// Exception connecting to apigee
					// connector. " +
					// statusCode.value(), "Configuration-1006");
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1020"), "Configuration-1020");
				else if (statusCode.value() == 409)
					throw new ItorixException("Request resource already available in Apigee " + statusCode.value(),
							"Configuration-1007");
				else
					// throw new
					// ItorixException(ErrorCodes.errorMessage.get("Configuration-1015")
					// ,"Configuration-1015");
					throw new ItorixException("invalid request data " + statusCode.value(), "Configuration-1000");
			}
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public Object updateApigeeKVM(KVMConfig config, User user) throws ItorixException {
		try {
			if (isCPSEnabled(config, user)) {
				deleteKVMEntries(config, user);
				return updateKVMEntries(config, user);
			} else {
				@SuppressWarnings("unchecked")
				List<KVMConfig> data = (ArrayList) getAllActiveKVMs(config);
				KVMConfig KVMConfig = data.get(0);
				ApigeeKVM kvm = kVMService.getKVMBody(KVMConfig);
				String URL = kVMService.getUpdateKVMURL(KVMConfig);
				HTTPUtil httpConn = new HTTPUtil(kvm, URL,
						getApigeeCredentials(KVMConfig.getOrg(), KVMConfig.getType()));
				ResponseEntity<String> response = httpConn.doPost();
				HttpStatus statusCode = response.getStatusCode();
				if (statusCode.is2xxSuccessful())
					return true;
				else if (statusCode.value() >= 401 && statusCode.value() <= 403)
					throw new ItorixException("Request validation failed. Exception connecting to apigee connector. "
							+ statusCode.value(), "Configuration-1006");
				else
					throw new ItorixException("invalid request data " + statusCode.value(), "Configuration-1000");
			}
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public Object deleteApigeeKVM(KVMConfig config) throws ItorixException {
		try {
			String URL = kVMService.getUpdateKVMURL(config);
			HTTPUtil httpConn = new HTTPUtil( URL,
					getApigeeCredentials(config.getOrg(), config.getType()));
			ResponseEntity<String> response = httpConn.doDelete();
			HttpStatus statusCode = response.getStatusCode();
			if (statusCode.is2xxSuccessful())
				return true;
			else if (statusCode.value() >= 401 && statusCode.value() <= 403)
				throw new ItorixException("Request validation failed. Exception connecting to apigee connector. "
						+ statusCode.value(), "Configuration-1006");
			else
				throw new ItorixException("invalid request data " + statusCode.value(), "Configuration-1000");

		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	private boolean isCPSEnabled(KVMConfig config, User user) throws ItorixException {
		boolean cps = false;
		try {
			String URL = kVMService.getCPSURL(config);
			HTTPUtil httpConn = new HTTPUtil(URL, getApigeeCredentials(config.getOrg(), config.getType()));
			ResponseEntity<String> response = httpConn.doGet();
			HttpStatus statusCode = response.getStatusCode();
			if (statusCode.is2xxSuccessful()) {
				String stringResponse = response.getBody();
				ObjectMapper mapper = new ObjectMapper();
				try {
					JsonNode jsonResponse = mapper.readTree(stringResponse);
					JsonNode properties = jsonResponse.path("properties");
					JsonNode property = properties.get("property");
					Iterator<JsonNode> iterator = property.elements();
					while (iterator.hasNext()) {
						JsonNode propertyNode = iterator.next();
						try {
							String name = propertyNode.path("name").textValue();
							if (name.equals("features.isCpsEnabled")
									&& propertyNode.path("value").textValue().equals("true"))
								cps = true;
						} catch (Exception e) {
							log.error("Exception occurred", e);
						}
					}
				} catch (Exception e) {
					log.error("Exception occurred", e);
				}
				return cps;
			} else if (statusCode.value() >= 401 && statusCode.value() <= 403)
				throw new ItorixException(
						"Request validation failed. Exception connecting to apigee connector. " + statusCode.value(),
						"Configuration-1006");
			else
				throw new ItorixException("invalid request data " + statusCode.value(), "Configuration-1000");
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	private boolean deleteKVMEntries(KVMConfig config, User user) throws ItorixException {
		try {
			String URL = kVMService.getCPSKVMDeleteURL(config) + "/keys";
			HTTPUtil httpConn = new HTTPUtil(URL, getApigeeCredentials(config.getOrg(), config.getType()));
			ResponseEntity<String> response = httpConn.doGet();
			HttpStatus statusCode = response.getStatusCode();
			if (statusCode.is2xxSuccessful()) {
				ObjectMapper mapper = new ObjectMapper();
				@SuppressWarnings("unchecked")
				ArrayList<String> keys = mapper.readValue(response.getBody(), ArrayList.class);
				for (String key : keys) {
					httpConn.setuRL(kVMService.getCPSKVMDeleteURL(config) + "/entries/" + key);
					httpConn.setBody(null);
					httpConn.doDelete();
				}
				return true;
			} else if (statusCode.value() >= 401 && statusCode.value() <= 403)
				throw new ItorixException(
						"Request validation failed. Exception connecting to apigee connector. " + statusCode.value(),
						"Configuration-1006");
			else
				throw new ItorixException("invalid request data " + statusCode.value(), "Configuration-1000");
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	private boolean updateKVMEntries(KVMConfig config, User user) throws ItorixException {
		try {
			HTTPUtil httpConn = new HTTPUtil(kVMService.getCPSKVMDeleteURL(config) + "/entries/",
					getApigeeCredentials(config.getOrg(), config.getType()));
			for (KVMEntry key : config.getEntry()) {
				httpConn.setBody(key);
				ResponseEntity<String> response = httpConn.doPost();
				HttpStatus statusCode = response.getStatusCode();
				if (statusCode.is2xxSuccessful()) {

				} else if (statusCode.value() >= 401 && statusCode.value() <= 403)
					throw new ItorixException("Request validation failed. Exception connecting to apigee connector. "
							+ statusCode.value(), "Configuration-1006");
				else
					throw new ItorixException("invalid request data " + statusCode.value(), "Configuration-1000");
			}
			return true;
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public boolean updateProductConfig(ProductConfig productConfig) throws ItorixException {
		try {
			List obj = getProducts(productConfig);
			ConfigMetadata metadata = productConfig.getMetadata();
			metadata.setResourceType("product");

			Query query = new Query(
					Criteria.where("org").is(productConfig.getOrg()).and("name").is(productConfig.getName()));
			Update update = new Update();
			update.set("createdUser", productConfig.getCreatedUser());
			update.set("modifiedUser", productConfig.getModifiedUser());
			update.set("createdDate", productConfig.getCreatedDate());
			update.set("modifiedDate", productConfig.getModifiedDate());
			update.set("description", productConfig.getDescription());
			update.set("type", productConfig.getType());
			update.set("apiResources", productConfig.getApiResources());
			update.set("displayName", productConfig.getDisplayName());
			update.set("environments", productConfig.getEnvironments());
			update.set("proxies", productConfig.getProxies());
			update.set("approvalType", productConfig.getApprovalType());
			update.set("activeFlag", productConfig.isActiveFlag());
			if (productConfig.getQuota() != null) {
				update.set("quota", productConfig.getQuota());
			}
			if (productConfig.getQuotaInterval() != null) {
				update.set("quotaInterval", productConfig.getQuotaInterval());
			}
			if (productConfig.getQuotaTimeUnit() != null) {
				update.set("quotaTimeUnit", productConfig.getQuotaTimeUnit());
			}
			if (productConfig.getScopes() != null && productConfig.getScopes().size() > 0) {
				update.set("scopes", productConfig.getScopes());
			}
			update.set("attributes", productConfig.getAttributes());
			mongoTemplate.upsert(query, update, ProductConfig.class);
			saveMetadata(metadata);
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}

		return true;
	}

	public boolean saveProduct(ProductConfig config) throws ItorixException {
		try {
			List obj = (ArrayList) getProducts(config);
			ConfigMetadata metadata = config.getMetadata();
			metadata.setResourceType("product");

			if (obj.size() > 0) {
				throw new ItorixException("Resource already exists", "Configuration-1000");
			}
			mongoTemplate.insert(config);
			saveMetadata(metadata);
			return true;

		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public List<ProductConfig> getProducts(ProductConfig config) throws ItorixException {
		try {
			Query query = null;
			if (config != null) {
				if (config.getName() != null && config.getOrg() != null) {
					query = new Query(Criteria.where("org").is(config.getOrg()).and("name").is(config.getName()));
				} else if (config.getName() != null) {
					query = new Query(Criteria.where("name").is(config.getName()));
				} else if (config.getOrg() != null) {
					query = new Query(Criteria.where("org").is(config.getOrg()));
				}

				return mongoTemplate.find(query, ProductConfig.class);
			} else {
				return mongoTemplate.findAll(ProductConfig.class);
			}
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public List<ProductConfig> getAllActiveProducts(ProductConfig config) throws ItorixException {
		try {
			Query query = null;
			if (config != null) {
				if (config.getName() != null && config.getOrg() != null) {
					query = new Query(Criteria.where("org").is(config.getOrg()).and("name").is(config.getName())
							.and("activeFlag").is(Boolean.TRUE));
				} else if (config.getName() != null) {
					query = new Query(Criteria.where("name").is(config.getName()).and("activeFlag").is(Boolean.TRUE));
				} else if (config.getOrg() != null) {
					query = new Query(Criteria.where("org").is(config.getOrg()).and("activeFlag").is(Boolean.TRUE));
				}

				return mongoTemplate.find(query, ProductConfig.class);
			} else {
				query = new Query(Criteria.where("activeFlag").is(Boolean.TRUE));
				return mongoTemplate.find(query, ProductConfig.class);
			}
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public boolean updateProduct(ProductConfig config) throws ItorixException {
		try {
			config.setActiveFlag(Boolean.TRUE);
			return updateProductConfig(config);

		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public boolean deleteProduct(String productName) throws ItorixException {
		try {
			Query query = new Query(Criteria.where("name").is(productName));
			DeleteResult result = mongoTemplate.remove(query, ProductConfig.class);
			if (result.getDeletedCount() > 0)
				return result.wasAcknowledged();
			else
				throw new ItorixException("No Record exists", "Configuration-1003");
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public boolean deleteProduct(String productName, String org, String type) throws ItorixException {
		try {
			Query query = new Query(Criteria.where("name").is(productName).and("org").is(org).and("type").is(type));
			DeleteResult result = mongoTemplate.remove(query, ProductConfig.class);
			if (result.getDeletedCount() > 0)
				return result.wasAcknowledged();
			else
				throw new ItorixException("No Record exists", "Configuration-1003");
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	@SuppressWarnings("unchecked")
	public Object getProductList() throws ItorixException {
		try {
			Set<String> productList = new HashSet<String>();
			List<ProductConfig> products = (ArrayList<ProductConfig>) getAllActiveProducts(null);
			for (ProductConfig product : products) {
				productList.add(product.getName());
			}
			List listData = new ArrayList();
			for (String product : productList) {
				ConfigMetadata metadata = getMetadata(product, "product");
				Map map = new HashMap();
				map.put("name", product);
				if (metadata != null) {
					map.put("createdBy", metadata.getCreatedUser());
					map.put("createdDate", metadata.getCreatedDate());
					map.put("modifiedBy", metadata.getModifiedUser());
					map.put("modifiedDate", metadata.getModifiedDate());
				}
				listData.add(map);
			}
			return listData;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public Object createApigeeProduct(ProductConfig config, User user) throws ItorixException {
		try {
			@SuppressWarnings("unchecked")
			List<ProductConfig> data = (ArrayList) getAllActiveProducts(config);
			ProductConfig productConfig = null;
			if (data.stream().findFirst().isPresent()) {
				productConfig = data.stream().findFirst().get();
			}
			if (productConfig != null) {
				if (isResourceAvailable(productService.getUpdateProductURL(productConfig),
						getApigeeCredentials(config.getOrg(), config.getType()))) {
					return updateApigeeProduct(config, user);
				} else {
					ApigeeProduct product = productService.getProductBody(productConfig);
					String URL = productService.getProductURL(productConfig);
					HTTPUtil httpConn = new HTTPUtil(product, URL,
							getApigeeCredentials(config.getOrg(), config.getType()));
					ResponseEntity<String> response = httpConn.doPost();
					HttpStatus statusCode = response.getStatusCode();
					if (statusCode.is2xxSuccessful()) {
						return true;
					} else if (statusCode.value() >= 401 && statusCode.value() <= 403) {
						throw new ItorixException(
								"Request validation failed. Exception connecting to apigee connector. "
										+ statusCode.value(), "Configuration-1006");
					} else if (statusCode.value() == 409) {
						throw new ItorixException(
								"Request resource already available in Apigee " + statusCode.value(),
								"Configuration-1007");
					} else {
						throw new ItorixException("invalid request data " + statusCode.value(),
								"Configuration-1000");
					}
				}
			} else {
				throw new ItorixException("Product is not present", "Configuration-1000");
			}
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public Object updateApigeeProduct(ProductConfig config, User user) throws ItorixException {
		try {
			@SuppressWarnings("unchecked")
			List<ProductConfig> data = null;
			data = (ArrayList) getAllActiveProducts(config);
			ProductConfig productConfig = data.get(0);
			ApigeeProduct product = productService.getProductBody(productConfig);
			String URL = productService.getUpdateProductURL(productConfig);
			product.setName(null);
			HTTPUtil httpConn = new HTTPUtil(product, URL, getApigeeCredentials(config.getOrg(), config.getType()));
			ResponseEntity<String> response = httpConn.doPut();
			HttpStatus statusCode = response.getStatusCode();
			if (statusCode.is2xxSuccessful())
				return true;
			else if (statusCode.value() >= 401 && statusCode.value() <= 403)
				throw new ItorixException(
						"Request validation failed. Exception connecting to apigee connector. " + statusCode.value(),
						"Configuration-1006");
			else
				throw new ItorixException("invalid request data " + statusCode.value(), "Configuration-1000");
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public Object getHistory(String type, String org, String env, String isSaaS, String name) throws ItorixException {

		if (type.equalsIgnoreCase("cache")) {
			CacheConfig config = new CacheConfig();
			config.setOrg(org);
			config.setEnv(env);
			config.setName(name);
			config.setType(isSaaS);
			return getCaches(config);
		} else if (type.equalsIgnoreCase("kvm")) {
			KVMConfig config = new KVMConfig();
			config.setOrg(org);
			config.setEnv(env);
			config.setName(name);
			config.setType(isSaaS);
			return getKVMs(config);
		} else if (type.equalsIgnoreCase("TargetServer")) {
			TargetConfig config = new TargetConfig();
			config.setOrg(org);
			config.setEnv(env);
			config.setName(name);
			config.setType(isSaaS);
			return getTargets(config);
		} else if (type.equalsIgnoreCase("Product")) {
			ProductConfig config = new ProductConfig();
			config.setOrg(org);
			config.setName(name);
			config.setType(isSaaS);
			return getProducts(config);
		} else {
			throw new ItorixException("Not a valid ServiceRequest type!!", "Configuration-1000");
		}
	}

	public void revertConfig(String type, String requestId) throws ItorixException {

		if (type.equalsIgnoreCase("cache")) {
			CacheConfig config = findCacheByRequestId(requestId);
			List<CacheConfig> caches = getCaches(config);

			if (caches.size() > 0) {
				Query query = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv())
						.and("name").is(config.getName()).and("type").is(config.getType()));
				Update update = new Update();
				update.set("activeFlag", false);
				UpdateResult result = mongoTemplate.updateMulti(query, update, CacheConfig.class);
			}
			Query query = new Query(Criteria.where("_id").is(requestId));
			Update update = new Update();
			update.set("activeFlag", true);
			UpdateResult result = mongoTemplate.updateMulti(query, update, CacheConfig.class);
			createApigeeCache(config, null);
		} else if (type.equalsIgnoreCase("kvm")) {
			KVMConfig config = findKvmByRequestId(requestId);
			List<KVMConfig> kvms = (List<KVMConfig>) getKVMs(config);

			if (kvms.size() > 0) {
				Query query = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv())
						.and("name").is(config.getName()).and("type").is(config.getType()));
				Update update = new Update();
				update.set("activeFlag", false);
				UpdateResult result = mongoTemplate.updateMulti(query, update, KVMConfig.class);
			}

			Query query = new Query(Criteria.where("_id").is(requestId));
			Update update = new Update();
			update.set("activeFlag", true);
			UpdateResult result = mongoTemplate.updateMulti(query, update, KVMConfig.class);

			createApigeeKVM(config, null);

		} else if (type.equalsIgnoreCase("TargetServer")) {
			TargetConfig config = findTargetByRequestId(requestId);
			List<TargetConfig> targets = getTargets(config);

			if (targets.size() > 0) {
				Query query = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv())
						.and("name").is(config.getName()).and("type").is(config.getType()));
				Update update = new Update();
				update.set("activeFlag", false);
				UpdateResult result = mongoTemplate.updateMulti(query, update, TargetConfig.class);
			}

			Query query = new Query(Criteria.where("_id").is(requestId));
			Update update = new Update();
			update.set("activeFlag", true);
			UpdateResult result = mongoTemplate.updateMulti(query, update, TargetConfig.class);

			createApigeeTarget(config, null);

		} else if (type.equalsIgnoreCase("Product")) {
			ProductConfig config = findProductByRequestId(requestId);
			List<ProductConfig> products = getProducts(config);

			if (products.size() > 0) {
				Query query = new Query(Criteria.where("org").is(config.getOrg()).and("name").is(config.getName()));
				Update update = new Update();
				update.set("activeFlag", false);
				UpdateResult result = mongoTemplate.updateMulti(query, update, ProductConfig.class);
			}

			Query query = new Query(Criteria.where("_id").is(requestId));
			Update update = new Update();
			update.set("activeFlag", true);
			UpdateResult result = mongoTemplate.updateMulti(query, update, ProductConfig.class);
			createApigeeProduct(config, null);
		}
	}

	public CacheConfig findCacheByRequestId(String requestId) {
		return mongoTemplate.findById(requestId, CacheConfig.class);
	}

	public KVMConfig findKvmByRequestId(String requestId) {
		return mongoTemplate.findById(requestId, KVMConfig.class);
	}

	public ProductConfig findProductByRequestId(String requestId) {
		return mongoTemplate.findById(requestId, ProductConfig.class);
	}

	public TargetConfig findTargetByRequestId(String requestId) {
		return mongoTemplate.findById(requestId, TargetConfig.class);
	}

	public Object configCacheSearch(String name, int limit) throws ItorixException {
		BasicQuery query = new BasicQuery("{\"name\": {$regex : '" + name + "', $options: 'i'}}");
		query.limit(limit > 0 ? limit : 10);
		List<CacheConfig> allCaches = mongoTemplate.find(query, CacheConfig.class);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode response = mapper.createObjectNode();
		ArrayNode responseFields = mapper.createArrayNode();
		for (CacheConfig vo : allCaches) {
			SearchItem searchItem = new SearchItem();
			searchItem.setId(vo.get_id());
			searchItem.setName(vo.getName());
			responseFields.addPOJO(searchItem);
		}
		response.set("Caches", responseFields);
		return response;
	}

	public Object configProductSearch(String name, int limit) throws ItorixException {
		BasicQuery query = new BasicQuery("{\"name\": {$regex : '" + name + "', $options: 'i'}}");
		query.limit(limit > 0 ? limit : 10);
		List<CacheConfig> allCaches = mongoTemplate.find(query, CacheConfig.class);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode response = mapper.createObjectNode();
		ArrayNode responseFields = mapper.createArrayNode();
		for (CacheConfig vo : allCaches) {
			SearchItem searchItem = new SearchItem();
			searchItem.setId(vo.get_id());
			searchItem.setName(vo.getName());
			responseFields.addPOJO(searchItem);
		}
		response.set("Caches", responseFields);
		return response;
	}

	public Object configKvmSearch(String name, int limit) throws ItorixException {
		BasicQuery query = new BasicQuery("{\"name\": {$regex : '" + name + "', $options: 'i'}}");
		query.limit(limit > 0 ? limit : 10);
		List<KVMConfig> allKvms = mongoTemplate.find(query, KVMConfig.class);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode response = mapper.createObjectNode();
		ArrayNode responseFields = mapper.createArrayNode();
		for (KVMConfig vo : allKvms) {
			SearchItem searchItem = new SearchItem();
			searchItem.setId(vo.get_id());
			searchItem.setName(vo.getName());
			responseFields.addPOJO(searchItem);
		}
		response.set("Kvms", responseFields);
		return response;
	}

	public Object configTargetServerSearch(String name, int limit) throws ItorixException {
		BasicQuery query = new BasicQuery("{\"name\": {$regex : '" + name + "', $options: 'i'}}");
		query.limit(limit > 0 ? limit : 10);
		List<TargetConfig> allTargets = mongoTemplate.find(query, TargetConfig.class);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode response = mapper.createObjectNode();
		ArrayNode responseFields = mapper.createArrayNode();
		for (TargetConfig vo : allTargets) {
			SearchItem searchItem = new SearchItem();
			searchItem.setId(vo.get_id());
			searchItem.setName(vo.getName());
			responseFields.addPOJO(searchItem);
		}
		response.set("Targets", responseFields);
		return response;
	}
}
