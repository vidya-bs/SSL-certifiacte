package com.itorix.apiwiz.configmanagement.serviceImpl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.common.model.configmanagement.CacheConfig;
import com.itorix.apiwiz.common.model.configmanagement.KVMConfig;
import com.itorix.apiwiz.common.model.configmanagement.ProductConfig;
import com.itorix.apiwiz.common.model.configmanagement.TargetConfig;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.configmanagement.model.Constants;
import com.itorix.apiwiz.configmanagement.service.ConfigManagement;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;

@CrossOrigin
@RestController
public class ConfigManagementService implements ConfigManagement {

	private static final Logger logger = LoggerFactory.getLogger(ConfigManagementService.class);

	@Autowired
	private com.itorix.apiwiz.configmanagement.dao.ConfigManagementDao configManagementDAO;

	@Autowired
	private IdentityManagementDao identityManagementDao;

	@Override
	public ResponseEntity<List<TargetConfig>> getTargetSummary(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid) throws Exception {
		logger.info("inside getTargetSummary(): Start");
		return new ResponseEntity<List<TargetConfig>>(configManagementDAO.getTargetSummary(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> addTarget(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid, @RequestBody TargetConfig config)
			throws Exception {
		User user = new com.itorix.apiwiz.identitymanagement.model.User();// commonServices.getUserDetailsFromSessionID(jsessionid);
		config.setCreatedUser(user.getFirstName() + Constants.SPACE + user.getLastName());
		config.setCreatedDate(Instant.now().toString());
		config.setModifiedUser(user.getFirstName() + Constants.SPACE + user.getLastName());
		config.setModifiedDate(Instant.now().toString());
		config.setActiveFlag(Boolean.TRUE);
		configManagementDAO.saveTarget(config);
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Void> updateTarget(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid, @RequestBody TargetConfig config)
			throws Exception {

		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		config.setCreatedUser(user.getFirstName() + " " + user.getLastName());
		config.setCreatedDate(Instant.now().toString());
		config.setModifiedUser(user.getFirstName() + " " + user.getLastName());
		config.setModifiedDate(Instant.now().toString());
		configManagementDAO.updateTarget(config);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Void> deleteTargetbyResource(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid, @RequestBody TargetConfig config)
			throws Exception {
		configManagementDAO.deleteTarget(config);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);

	}

	@Override
	public ResponseEntity<Void> deleteTarget(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("target") String target)
			throws Exception {
		configManagementDAO.deleteTarget(target);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/targets")
	public ResponseEntity<Object> getTarget(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		return new ResponseEntity<Object>(configManagementDAO.getTargetList(), HttpStatus.OK);
	}

//	@UnSecure
	@Override
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/targets/{target}")
	public ResponseEntity<Object> getTarget(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("target") String target, @RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "type", required=false) String type) throws Exception {
		if (target != null && org != null && env != null) {
			TargetConfig config = new TargetConfig();
			config.setName(target);
			config.setOrg(org);
			config.setEnv(env);
			config.setType(type);
			List data = (ArrayList) configManagementDAO.getAllActiveTargets(config);
			if (data.size() == 0)
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Configuration-1022"),
						config.getName(), config.getOrg(), config.getEnv()), "Configuration-1022");
			return new ResponseEntity<Object>(data.get(0), HttpStatus.OK);
		} else {
			return new ResponseEntity<Object>(configManagementDAO.getTarget(target), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Void> createApigeeTarget(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("target") String target, @RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "type", required=false) String type,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		if (!target.equals("") && !org.equals("") && !env.equals("")) {
			TargetConfig config = new TargetConfig();
			config.setName(target);
			config.setOrg(org);
			config.setEnv(env);
			config.setType(type);
			User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
			configManagementDAO.createApigeeTarget(config, user);
			return new ResponseEntity<Void>(HttpStatus.CREATED);
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1024"), "Configuration-1024");
		}
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/config/apigee/targets/{target}")
	public ResponseEntity<Void> updateApigeeTarget(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("target") String target, @RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "type", required=false) String type,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		if (!target.equals("") && !org.equals("") && !env.equals("")) {
			TargetConfig config = new TargetConfig();
			config.setName(target);
			config.setOrg(org);
			config.setEnv(env);
			config.setType(type);
			User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
			configManagementDAO.updateApigeeTarget(config, user);
			return new ResponseEntity<Void>(HttpStatus.CREATED);
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1024"), "Configuration-1024");
		}
	}

	@Override
	@RequestMapping(method = RequestMethod.POST, value = "/v1/config/caches")
	public ResponseEntity<Void> addCache(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody CacheConfig config) throws Exception {
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		config.setCreatedUser(user.getFirstName() + " " + user.getLastName());
		config.setCreatedDate(Instant.now().toString());
		config.setModifiedUser(user.getFirstName() + " " + user.getLastName());
		config.setModifiedDate(Instant.now().toString());
		config.setActiveFlag(Boolean.TRUE);
		configManagementDAO.saveCache(config);
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	@Override
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/config/caches")
	public ResponseEntity<Void> updateCache(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody CacheConfig config) throws Exception {
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		
		config.setCreatedUser(user.getFirstName() + " " + user.getLastName());
		config.setCreatedDate(Instant.now().toString());
		config.setModifiedUser(user.getFirstName() + " " + user.getLastName());
		config.setModifiedDate(Instant.now().toString());
		configManagementDAO.updateCache(config);
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Void> deleteCacheResource(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody CacheConfig config) throws Exception {
		configManagementDAO.deleteCache(config);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Void> deleteCache(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("cache") String cache)
			throws Exception {
		configManagementDAO.deleteCache(cache);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> getCacheSummary(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		return new ResponseEntity<Object>(configManagementDAO.getCacheSummary(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getCache(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		return new ResponseEntity<Object>(configManagementDAO.getCacheList(), HttpStatus.OK);
	}

//	@UnSecure
	@Override
	public ResponseEntity<Object> getCache(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("cache") String cache, @RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "type", required=false) String type) throws Exception {
		if (cache != null && org != null && env != null) {
			CacheConfig config = new CacheConfig();
			config.setName(cache);
			config.setOrg(org);
			config.setEnv(env);
			config.setType(type);
			List<CacheConfig> data = (ArrayList<CacheConfig>) configManagementDAO.getAllActiveCaches(config);
			if (data.size() == 0)
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Configuration-1012"),
						config.getName(), config.getOrg(), config.getEnv()), "Configuration-1012");
			return new ResponseEntity<Object>(data.get(0), HttpStatus.OK);
		} else {
			return new ResponseEntity<Object>(configManagementDAO.getCache(cache), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Void> createApigeeCache(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("cache") String cache, @RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "type", required=false) String type,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		if (cache != null && org != null && env != null) {
			CacheConfig config = new CacheConfig();
			config.setName(cache);
			config.setOrg(org);
			config.setEnv(env);
			config.setType(type);
			User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
			configManagementDAO.createApigeeCache(config, user);
			return new ResponseEntity<Void>(HttpStatus.CREATED);
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1014"), "Configuration-1014");
		}
	}

	@Override
	public ResponseEntity<Void> updateApigeeCache(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("cache") String cache, @RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "type", required=false) String type,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		if (cache != null && org != null && env != null) {
			CacheConfig config = new CacheConfig();
			config.setName(cache);
			config.setOrg(org);
			config.setEnv(env);
			config.setType(type);
			User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
			configManagementDAO.updateApigeeCache(config, user);
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1014"), "Configuration-1014");
		}
	}

	@Override
	public ResponseEntity<Void> addKVM(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody KVMConfig config) throws Exception {
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		config.setCreatedUser(user.getFirstName() + " " + user.getLastName());
		config.setCreatedDate(Instant.now().toString());
		config.setModifiedUser(user.getFirstName() + " " + user.getLastName());
		config.setModifiedDate(Instant.now().toString());
		config.setActiveFlag(Boolean.TRUE);
		configManagementDAO.saveKVM(config);
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Void> updateKVM(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody KVMConfig config) throws Exception {
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		config.setCreatedUser(user.getFirstName() + " " + user.getLastName());
		config.setCreatedDate(Instant.now().toString());
		config.setModifiedUser(user.getFirstName() + " " + user.getLastName());
		config.setModifiedDate(Instant.now().toString());
		configManagementDAO.updateKVM(config);
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Void> deleteKVMResource(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody KVMConfig config) throws Exception {
		configManagementDAO.deleteKVM(config);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Void> deleteKVM(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("kvm") String kvm) throws Exception {
		configManagementDAO.deleteKVM(kvm);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> getKVMSummary(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		return new ResponseEntity<Object>(configManagementDAO.getKVMSummary(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getKVMs(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		return new ResponseEntity<Object>(configManagementDAO.getKVMList(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getKVM(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("kvm") String kvm, @RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "type", required=false) String type) throws Exception {
		if (kvm != null && org != null && env != null) {
			KVMConfig config = new KVMConfig();
			config.setName(kvm);
			config.setOrg(org);
			config.setEnv(env);
			config.setType(type);
			List data = (ArrayList) configManagementDAO.getKVMs(config);
			if (data.size() == 0)
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Configuration-1017"),
						config.getName(), config.getOrg(), config.getEnv()), "Configuration-1017");
			return new ResponseEntity<Object>(data.get(0), HttpStatus.OK);
		} else {
			return new ResponseEntity<Object>(configManagementDAO.getKVM(kvm), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Void> createApigeeKVM(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("kvm") String kvm, @RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "type", required=false) String type,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		if (kvm != null && org != null && env != null) {
			KVMConfig config = new KVMConfig();
			config.setName(kvm);
			config.setOrg(org);
			config.setEnv(env);
			config.setType(type);
			User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
			configManagementDAO.createApigeeKVM(config, user);
			return new ResponseEntity<Void>(HttpStatus.CREATED);
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1019"), "Configuration-1019");
		}
	}

	@Override
	public ResponseEntity<Void> updateApigeeKVM(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("kvm") String kvm, @RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "type", required=false) String type,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		if (kvm != null && org != null && env != null) {
			KVMConfig config = new KVMConfig();
			config.setName(kvm);
			config.setOrg(org);
			config.setEnv(env);
			config.setType(type);
			User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
			configManagementDAO.updateApigeeKVM(config, user);
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} else {
			throw new ItorixException(ErrorCodes.errorMessage.get("Configuration-1019"), "Configuration-1019");
		}
	}

	@Override
	public ResponseEntity<Void> addProduct(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody ProductConfig config)
			throws Exception {
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		config.setCreatedUser(user.getFirstName() + " " + user.getLastName());
		config.setCreatedDate(Instant.now().toString());
		config.setModifiedUser(user.getFirstName() + " " + user.getLastName());
		config.setModifiedDate(Instant.now().toString());
		config.setActiveFlag(Boolean.TRUE);
		configManagementDAO.saveProduct(config);
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Void> updateProduct(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody ProductConfig config)
			throws Exception {
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		config.setCreatedUser(user.getFirstName() + " " + user.getLastName());
		config.setCreatedDate(Instant.now().toString());
		config.setModifiedUser(user.getFirstName() + " " + user.getLastName());
		config.setModifiedDate(Instant.now().toString());
		configManagementDAO.updateProduct(config);
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Void> deleteProduct(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@PathVariable("product_name") String productname,
			@RequestParam(value = "org", required=false) String org,
			@RequestParam(value = "type", required=false) String type)
			throws Exception {
		if(org!=null && type!=null)
			configManagementDAO.deleteProduct(productname, org, type);
		else
			configManagementDAO.deleteProduct(productname);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> getProduct(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		return new ResponseEntity<Object>(configManagementDAO.getProductList(), HttpStatus.OK);
	}

//	@UnSecure
	@Override
	public ResponseEntity<List<ProductConfig>> getProductByName(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("product_name") String productname) throws Exception {
		ProductConfig config = new ProductConfig();
		config.setName(productname);
		List<ProductConfig> data = (ArrayList) configManagementDAO.getProducts(config);
		return new ResponseEntity<List<ProductConfig>>(data, HttpStatus.OK);
	}

//	@UnSecure
	@Override
	public ResponseEntity<ProductConfig> getProductByOrg(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("product_name") String productname, @RequestParam("org") String org,
			@RequestParam(value="type",required=false) String type) throws Exception {
		ProductConfig config = new ProductConfig();
		config.setName(productname);
		config.setOrg(org);
		config.setType(type);
		List<ProductConfig> data = (ArrayList) configManagementDAO.getProducts(config);
		return new ResponseEntity<ProductConfig>(data.get(0), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> createApigeeProduct(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("product_name") String productname, @RequestParam(value = "org", required = false) String org,
			@RequestParam(value = "type", required = false) String type,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		if (productname != null && org != null) {
			ProductConfig config = new ProductConfig();
			config.setOrg(org);
			config.setName(productname);
			config.setType(type);
			User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
			configManagementDAO.createApigeeProduct(config, user);
			return new ResponseEntity<Void>(HttpStatus.CREATED);
		} else {
			throw new ItorixException("Insufficient Data in the Request ", "Configuration-1005");
		}
	}

	@Override
	public ResponseEntity<Void> updateApigeeProduct(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("product_name") String productname, @RequestParam(value = "org", required = false) String org,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		if (productname != null && org != null) {
			ProductConfig config = new ProductConfig();
			config.setName(productname);
			config.setOrg(org);
			User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
			configManagementDAO.updateApigeeProduct(config, user);
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} else {
			throw new ItorixException("Insufficient Data in the Request ", "Configuration-1005");
		}
	}

	@Override
	public ResponseEntity<Object> getHistory(@RequestHeader(value="interactionid",required=false)String interactionid,
			@PathVariable(value = "serviceRequestType") String serviceRequestType,
			@RequestParam(value = "org") String org, 
			@RequestParam(value = "env", required = false) String env,
			@RequestParam(value = "type") String type,
			@RequestParam(value = "name") String name,
			@RequestHeader(value="JSESSIONID") String jsessionid) throws Exception {
		return new ResponseEntity<Object>(configManagementDAO.getHistory(serviceRequestType, org, env, type, name), HttpStatus.OK);
		
	}

	@Override
	public ResponseEntity<Void> revertConfig(@RequestHeader(value="interactionid",required=false)String interactionid,
			@PathVariable(value = "serviceRequestType") String serviceRequestType,
			@PathVariable(value = "requestId") String requestId, 
			@RequestHeader(value="JSESSIONID") String jsessionid) throws Exception {
		configManagementDAO.revertConfig(serviceRequestType,requestId);
		return new ResponseEntity<Void>( HttpStatus.NO_CONTENT);
	}
		
		
		public ResponseEntity<Object> configCacheSearch(
				@RequestHeader(value = "interactionid", required = false) String interactionid,
				@RequestParam(value = "name") String name, @RequestParam(value = "limit") int limit) throws Exception {

			return new ResponseEntity<Object>(configManagementDAO.configCacheSearch(name, limit), HttpStatus.OK);
		}

		public ResponseEntity<Object> configKvmSearch(
				@RequestHeader(value = "interactionid", required = false) String interactionid,
				@RequestParam(value = "name") String name, @RequestParam(value = "limit") int limit) throws Exception {
			return new ResponseEntity<Object>(configManagementDAO.configKvmSearch(name, limit), HttpStatus.OK);
		}

		public ResponseEntity<Object> configTargetServerSearch(
				@RequestHeader(value = "interactionid", required = false) String interactionid,
				@RequestParam(value = "name") String name, @RequestParam(value = "limit") int limit) throws Exception {
			return new ResponseEntity<Object>(configManagementDAO.configTargetServerSearch(name, limit), HttpStatus.OK);
		}

		public ResponseEntity<Object> configProductServerSearch(@RequestHeader(value="interactionid",required=false)String interactionid,
				@RequestParam(value = "name") String name,
				@RequestParam(value = "limit") int limit) throws Exception{
			return new ResponseEntity<Object>(configManagementDAO.configProductSearch(name, limit), HttpStatus.OK);
		}

}