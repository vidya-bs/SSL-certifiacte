package com.itorix.apiwiz.configmanagement.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.apigeeX.ApigeeXEnvironment;
import com.itorix.apiwiz.common.model.apigeeX.KVMConfig;
import com.itorix.apiwiz.common.model.apigeeX.TargetConfig;
import com.itorix.apiwiz.common.model.configmanagement.ConfigMetadata;
import com.itorix.apiwiz.common.model.configmanagement.KVMEntry;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.apigeeX.ApigeeXUtill;
import com.itorix.apiwiz.common.util.http.HTTPUtil;
import com.itorix.apiwiz.configmanagement.model.apigee.ApigeeKVM;
import com.itorix.apiwiz.configmanagement.model.apigee.ApigeeTarget;
import com.itorix.apiwiz.configmanagement.model.apigeeX.ApigeexTarget;
import com.itorix.apiwiz.configmanagement.model.apigeeX.services.TargetConnectionX;
import com.itorix.apiwiz.configmanagement.model.apigeeX.services.XKVMService;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.mongodb.client.result.UpdateResult;

@Component
public class ApigeeXConfigDao {
	
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private BaseRepository baseRepository;
	
	@Autowired
	private TargetConnectionX targetConn;
	
	@Autowired 
	private ApigeeXUtill apigeeUtil;
	
	@Autowired 
	private XKVMService KVMService;
	
	public boolean saveTarget(TargetConfig config) throws ItorixException {
		try {
			List<TargetConfig> obj = getTargets(config);

			if (obj.size() > 0) {
				Query query = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv())
						.and("name").is(config.getName()));
				Update update = new Update();
				update.set("activeFlag", false);
				UpdateResult result = mongoTemplate.updateMulti(query, update, TargetConfig.class);
			}
			baseRepository.save(config);
			return true;

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
	
	public List<TargetConfig> getTargets(TargetConfig config) throws ItorixException {
		try {
			if (config != null) {
				Query query = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv())
						.and("name").is(config.getName()));
				return mongoTemplate.find(query, TargetConfig.class);
			} else {
				throw new ItorixException("", "Configuration-1000");
			}
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}
	
	
	public Object createApigeeTarget(TargetConfig config, User user) throws ItorixException {
		try {
			@SuppressWarnings("unchecked")
			List<TargetConfig> data = (ArrayList) getAllActiveTargets(config);
			TargetConfig targetConfig = data.get(0);
			ApigeexTarget target = targetConn.getTargetBody(targetConfig);
			String URL = targetConn.getTargetURL(targetConfig);
			if (isResourceAvailable(targetConn.getUpdateTargetURL(targetConfig),
					apigeeUtil.getApigeeCredentials(targetConfig.getOrg(), targetConfig.getType()))) {
				return updateApigeeTarget(config, user);
			} else {
				ObjectMapper mapper = new ObjectMapper();
				
				System.out.println(mapper.writeValueAsString(target));
				HTTPUtil httpConn = new HTTPUtil(target, URL,
						apigeeUtil.getApigeeCredentials(targetConfig.getOrg(), targetConfig.getType()));
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
	
	public Object updateApigeeTarget(TargetConfig config, User user) throws ItorixException {
		try {
			@SuppressWarnings("unchecked")
			List<TargetConfig> data = (ArrayList) getAllActiveTargets(config);
			TargetConfig targetConfig = data.get(0);
			ApigeexTarget target = targetConn.getTargetBody(targetConfig);
			String URL = targetConn.getUpdateTargetURL(targetConfig);
			HTTPUtil httpConn = new HTTPUtil(target, URL,
					apigeeUtil.getApigeeCredentials(targetConfig.getOrg(), targetConfig.getType()));
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
	
	private boolean isResourceAvailable(String URL, String credentials) {
		boolean isPresent = false;
		try {
			HTTPUtil httpConn = new HTTPUtil(null, URL, credentials);
			ResponseEntity<String> response = httpConn.doGet();
			HttpStatus statusCode = response.getStatusCode();
			if (statusCode.is2xxSuccessful())
				isPresent = true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return isPresent;
	}
	
	private boolean isKVMResourceAvailable(String URL, String credentials, String name) {
		boolean isPresent = false;
		try {
			HTTPUtil httpConn = new HTTPUtil(null, URL, credentials);
			ResponseEntity<String> response = httpConn.doGet();
			HttpStatus statusCode = response.getStatusCode();
			if (statusCode.is2xxSuccessful())
				if(response.getBody().contains(name))
					isPresent = true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return isPresent;
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
	
	public boolean saveKVM(KVMConfig config) throws ItorixException {
		try {
			List obj = (ArrayList) getKVMs(config);

			if (obj.size() > 0) {
				Query query = new Query(Criteria.where("org").is(config.getOrg()).and("env").is(config.getEnv())
						.and("name").is(config.getName()).and("type").is(config.getType()));
				Update update = new Update();
				update.set("activeFlag", false);
				UpdateResult result = mongoTemplate.updateMulti(query, update, KVMConfig.class);
			}
			baseRepository.save(config);
			return true;

		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	
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
	
	public Object createApigeeKVM(KVMConfig config, User user) throws ItorixException {
		try {
			@SuppressWarnings("unchecked")
			List<KVMConfig> data = (ArrayList) getAllActiveKVMs(config);
			KVMConfig KVMConfig = data.get(0);
			if (isKVMResourceAvailable(KVMService.getKVMURL(KVMConfig),
					apigeeUtil.getApigeeCredentials(KVMConfig.getOrg(), KVMConfig.getType()), KVMConfig.getName())) {
				deleteApigeeKVM(config, user);
			} 
				ApigeeKVM kvm = KVMService.getKVMBody(KVMConfig);
				String URL = KVMService.getKVMURL(KVMConfig);
				ObjectMapper mapper = new ObjectMapper();
				
				System.out.println(mapper.writeValueAsString(kvm) + " ");
				HTTPUtil httpConn = new HTTPUtil(kvm, URL,
						apigeeUtil.getApigeeCredentials(KVMConfig.getOrg(), KVMConfig.getType()));
				ResponseEntity<String> response = httpConn.doPost();
				HttpStatus statusCode = response.getStatusCode();
				if (statusCode.is2xxSuccessful())
				{
					addKVMData(KVMConfig, apigeeUtil.getApigeeCredentials(KVMConfig.getOrg(), KVMConfig.getType()));
					return true;
				}
				else if (statusCode.value() >= 401 && statusCode.value() <= 403)
					throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1020"), "Configuration-1020");
				else if (statusCode.value() == 409)
					throw new ItorixException("Request resource already available in Apigee " + statusCode.value(),
							"Configuration-1007");
				else
					throw new ItorixException("invalid request data " + statusCode.value(), "Configuration-1000");
			
		} catch (ItorixException ex) {
			throw ex;
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

	private Object deleteApigeeKVM(KVMConfig config, User user) throws ItorixException {
		try {
			String URL = KVMService.getUpdateKVMURL(config);
			HTTPUtil httpConn = new HTTPUtil(URL,
					apigeeUtil.getApigeeCredentials(config.getOrg(), config.getType()));
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
	
	private void addKVMData(KVMConfig config, String credential) throws ItorixException{
		try {
			List<KVMEntry> entries = config.getEntry();
			List<ApigeeXEnvironment> envs = apigeeUtil.getEnvList(config.getOrg(), config.getType());
			ApigeeXEnvironment env = envs.stream().filter(p-> p.getName().equalsIgnoreCase(config.getEnv())).collect(Collectors.toList()).get(0);
			String URL = env.getKvmProxyEndpoint() 
					+"/v1/organizations/" + config.getOrg() + "/environments/" + config.getEnv() + "/keyvaluemaps/" + config.getName() + "/entries";
			
			for(KVMEntry entry: entries){
				Map<String, String> entryMap = new HashMap<String, String>();
				entryMap.put("key", entry.getName());
				entryMap.put("value", entry.getValue());
				
				HTTPUtil httpConn = new HTTPUtil(entryMap, URL,credential);
				if(httpConn.getHeaders() == null){
					HttpHeaders headers = new HttpHeaders();
					if(apigeeUtil.getHostHeader() != null)
						headers.set("Host", apigeeUtil.getHostHeader());
					httpConn.setHeaders(headers);
				}else{
					HttpHeaders headers = httpConn.getHeaders();
					if(apigeeUtil.getHostHeader() != null)
						headers.set("Host", apigeeUtil.getHostHeader());
					httpConn.setHeaders(headers);
				}
				ResponseEntity<String> response = httpConn.doPost();
				HttpStatus statusCode = response.getStatusCode();
				if (!statusCode.is2xxSuccessful()) 
				if (statusCode.value() >= 401 && statusCode.value() <= 403)
					throw new ItorixException("Request validation failed. Exception connecting to apigee connector. "
							+ statusCode.value(), "Configuration-1006");
				else
					throw new ItorixException("invalid request data " + statusCode.value(), "Configuration-1000");
			}
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}
	
}
