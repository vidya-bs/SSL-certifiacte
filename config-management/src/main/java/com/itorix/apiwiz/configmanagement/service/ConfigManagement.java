package com.itorix.apiwiz.configmanagement.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;

@CrossOrigin
@RestController
public interface ConfigManagement {

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/targets/summary")
	public ResponseEntity<List<TargetConfig>> getTargetSummary(
			@RequestHeader(value="interactionid",required=false)String interactionid,
			@RequestHeader(value="JSESSIONID", required=false) String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/config/targets")
	public ResponseEntity<Void> addTarget(
			@RequestHeader(value="interactionid",required=false)String interactionid,
			@RequestHeader(value="JSESSIONID", required=false) String jsessionid,
			@RequestBody TargetConfig config) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/config/targets")
	public ResponseEntity<Void> updateTarget(
			@RequestHeader(value="interactionid",required=false)String interactionid,
			@RequestHeader(value="JSESSIONID",required=false) String jsessionid,
			@RequestBody TargetConfig config) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = { RequestMethod.DELETE, RequestMethod.PATCH}, value = "/v1/config/targets")
	public ResponseEntity<Void> deleteTargetbyResource(
			@RequestHeader(value="interactionid",required=false)String interactionid,
			@RequestHeader(value="JSESSIONID") String jsessionid,
			@RequestBody TargetConfig config) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/config/targets/{target}")
	public ResponseEntity<Void> deleteTarget(@RequestHeader(value="interactionid",required=false)String interactionid,
			@RequestHeader(value="JSESSIONID") String jsessionid,
			@PathVariable("target") String target) throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/targets")
	public ResponseEntity<Object> getTarget(
			@RequestHeader(value="interactionid",required=false)String interactionid,
			@RequestHeader(value="JSESSIONID") String jsessionid) throws Exception;

//	@UnSecure
	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/targets/{target}")
	public ResponseEntity<Object> getTarget(@RequestHeader(value="interactionid",required=false)String interactionid,
			@PathVariable("target") String target,
			@RequestParam(value = "org", required=false ) String org,
			@RequestParam(value = "env", required=false) String env,
			@RequestParam(value = "type", required=false) String type) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/config/apigee/targets/{target}")
	public ResponseEntity<Void> createApigeeTarget(@RequestHeader(value="interactionid",required=false)String interactionid,
			@PathVariable("target") String target,
			@RequestParam(value ="org", required=false) String org,
			@RequestParam(value = "env", required=false) String env,
			@RequestParam(value = "type", required=false) String type,
			@RequestHeader(value="JSESSIONID") String jsessionid) throws Exception;
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/config/apigee/targets/{target}")
	public ResponseEntity<Void> updateApigeeTarget(@RequestHeader(value="interactionid",required=false)String interactionid,
			@PathVariable("target") String target,
			@RequestParam(value = "org", required=false) String org,
			@RequestParam(value = "env", required=false) String env,
			@RequestParam(value = "type", required=false) String type,
			@RequestHeader(value="JSESSIONID") String jsessionid) throws Exception;
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/config/caches")
	public ResponseEntity<Void> addCache(@RequestHeader(value="interactionid",required=false)String interactionid,
			@RequestHeader(value="JSESSIONID") String jsessionid,
			@RequestBody CacheConfig config) throws Exception;
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/config/caches")
	public ResponseEntity<Void> updateCache(@RequestHeader(value="interactionid",required=false)String interactionid,
			@RequestHeader(value="JSESSIONID") String jsessionid,
			@RequestBody CacheConfig config) throws Exception;
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = { RequestMethod.DELETE, RequestMethod.PATCH}, value = "/v1/config/caches")
	public ResponseEntity<Void> deleteCacheResource(@RequestHeader(value="interactionid",required=false)String interactionid,
			@RequestHeader(value="JSESSIONID") String jsessionid,
			@RequestBody CacheConfig config) throws Exception;
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/config/caches/{cache}")
	public ResponseEntity<Void> deleteCache(@RequestHeader(value="interactionid",required=false)String interactionid,
			@RequestHeader(value="JSESSIONID") String jsessionid,
			@PathVariable("cache") String cache) throws Exception;
	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/caches/summary")
	public ResponseEntity<Object> getCacheSummary(@RequestHeader(value="interactionid",required=false)String interactionid,
			@RequestHeader(value="JSESSIONID") String jsessionid) throws Exception;
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/caches")
	public ResponseEntity<Object> getCache(@RequestHeader(value="interactionid",required=false)String interactionid,@RequestHeader(value="JSESSIONID") String jsessionid
			) throws Exception;
//	@UnSecure
	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/caches/{cache}")
	public ResponseEntity<Object> getCache(@RequestHeader(value="interactionid",required=false)String interactionid,
			@PathVariable("cache") String cache,
			@RequestParam(value = "org", required=false) String org,
			@RequestParam(value = "env", required=false) String env,
			@RequestParam(value = "type", required=false) String type) throws Exception;
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/config/apigee/caches/{cache}")
	public ResponseEntity<Void> createApigeeCache(@RequestHeader(value="interactionid",required=false)String interactionid,
			@PathVariable("cache") String cache,
			@RequestParam(value = "org", required=false) String org,
			@RequestParam(value = "env", required=false) String env,
			@RequestParam(value = "type", required=false) String type,
			@RequestHeader(value="JSESSIONID") String jsessionid) throws Exception;
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/config/apigee/caches/{cache}")
	public ResponseEntity<Void> updateApigeeCache(@RequestHeader(value="interactionid",required=false)String interactionid,
			@PathVariable("cache") String cache,
			@RequestParam(value = "org", required=false) String org,
			@RequestParam(value = "env", required=false) String env,
			@RequestParam(value = "type", required=false) String type,
			@RequestHeader(value="JSESSIONID") String jsessionid) throws Exception;
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/config/kvms")
	public ResponseEntity<Void> addKVM(@RequestHeader(value="interactionid",required=false)String interactionid,
			@RequestHeader(value="JSESSIONID") String jsessionid,
			@RequestBody KVMConfig config) throws Exception;
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/config/kvms")
	public ResponseEntity<Void> updateKVM(@RequestHeader(value="interactionid",required=false)String interactionid,
			@RequestHeader(value="JSESSIONID") String jsessionid,
			@RequestBody KVMConfig config) throws Exception;
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = { RequestMethod.DELETE, RequestMethod.PATCH}, value = "/v1/config/kvms")
	public ResponseEntity<Void> deleteKVMResource(@RequestHeader(value="interactionid",required=false)String interactionid,
			@RequestHeader(value="JSESSIONID") String jsessionid,
			@RequestBody KVMConfig config) throws Exception;
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/config/kvms/{kvm}")
	public ResponseEntity<Void> deleteKVM(@RequestHeader(value="interactionid",required=false)String interactionid,
			@RequestHeader(value="JSESSIONID") String jsessionid,
			@PathVariable("kvm") String kvm) throws Exception;
	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/kvms/summary")
	public ResponseEntity<Object> getKVMSummary(@RequestHeader(value="interactionid",required=false)String interactionid,
			@RequestHeader(value="JSESSIONID") String jsessionid) throws Exception;
	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/kvms")
	public ResponseEntity<Object> getKVMs(@RequestHeader(value="interactionid",required=false)String interactionid,
			@RequestHeader(value="JSESSIONID") String jsessionid) throws Exception;
//	@UnSecure
	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/kvms/{kvm}")
	public ResponseEntity<Object> getKVM(@RequestHeader(value="interactionid",required=false)String interactionid,
			@PathVariable("kvm") String kvm,
			@RequestParam(value = "org", required=false ) String org,
			@RequestParam(value = "env", required=false) String env,
			@RequestParam(value = "type", required=false) String type) throws Exception;
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/config/apigee/kvms/{kvm}")
	public ResponseEntity<Void> createApigeeKVM(@RequestHeader(value="interactionid",required=false)String interactionid,
			@PathVariable("kvm") String kvm,
			@RequestParam(value = "org", required=false) String org,
			@RequestParam(value = "env", required=false) String env,
			@RequestParam(value = "type", required=false) String type,
			@RequestHeader(value="JSESSIONID") String jsessionid) throws Exception;
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/config/apigee/kvms/{kvm}")
	public ResponseEntity<Void> updateApigeeKVM(@RequestHeader(value="interactionid",required=false)String interactionid,
			@PathVariable("kvm") String kvm,
			@RequestParam(value = "org", required=false) String org,
			@RequestParam(value = "env", required=false) String env,
			@RequestParam(value = "type", required=false) String type,
			@RequestHeader(value="JSESSIONID") String jsessionid) throws Exception;
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/config/products")
	public ResponseEntity<Void> addProduct(@RequestHeader(value="interactionid",required=false)String interactionid,
			@RequestHeader(value="JSESSIONID") String jsessionid,
			@RequestBody ProductConfig config) throws Exception;
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/config/products")
	public ResponseEntity<Void> updateProduct(@RequestHeader(value="interactionid",required=false)String interactionid,
			@RequestHeader(value="JSESSIONID") String jsessionid,
			@RequestBody ProductConfig config) throws Exception;
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = {RequestMethod.DELETE, RequestMethod.PATCH}, value = "/v1/config/products/{product_name}")
	public ResponseEntity<Void> deleteProduct(@RequestHeader(value="interactionid",required=false)String interactionid,
			@RequestHeader(value="JSESSIONID") String jsessionid,
			@PathVariable("product_name") String productname,
			@RequestParam(value = "org", required=false) String org,
			@RequestParam(value = "type", required=false) String type) throws Exception;
	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/products")
	public ResponseEntity<Object> getProduct(@RequestHeader(value="interactionid",required=false)String interactionid,
			@RequestHeader(value="JSESSIONID") String jsessionid) throws Exception;
//	@UnSecure
	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/products/{product_name}")
	public ResponseEntity<List<ProductConfig>> getProductByName(@RequestHeader(value="interactionid",required=false)String interactionid,
			@PathVariable("product_name") String productname) throws Exception;
	@UnSecure
	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/product/{product_name}")
	public ResponseEntity<ProductConfig> getProductByOrg(@RequestHeader(value="interactionid",required=false)String interactionid,
			@PathVariable("product_name") String productname,
			@RequestParam("org") String org,
			@RequestParam(value="type",required=false) String type) throws Exception;
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/config/apigee/products/{product_name}")
	public ResponseEntity<Void> createApigeeProduct(@RequestHeader(value="interactionid",required=false)String interactionid,
			@PathVariable("product_name") String productname,
			@RequestParam(value = "org", required=false) String org,
			@RequestParam(value="type",required=false) String type,
			@RequestHeader(value="JSESSIONID") String jsessionid) throws Exception;
	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/config/apigee/products/{product_name}")
	public ResponseEntity<Void> updateApigeeProduct(@RequestHeader(value="interactionid",required=false)String interactionid,
			@PathVariable("product_name") String productname,
			@RequestParam(value = "org", required=false) String org,
			@RequestHeader(value="JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/{serviceRequestType}/history")
	public ResponseEntity<Object> getHistory(@RequestHeader(value="interactionid",required=false)String interactionid,
			@PathVariable(value = "serviceRequestType") String serviceRequestType,
			@RequestParam(value = "org") String org,
			@RequestParam(value = "env",required = false) String env,
			@RequestParam(value = "name") String name,
			@RequestParam(value = "type") String type,
			@RequestHeader(value="JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.POST, value = "/v1/config/{serviceRequestType}/{requestId}/revert")
	public ResponseEntity<Void> revertConfig(@RequestHeader(value="interactionid",required=false)String interactionid,
			@PathVariable(value = "serviceRequestType") String serviceRequestType,
			@PathVariable(value = "requestId") String requestId,
			@RequestHeader(value="JSESSIONID") String jsessionid) throws Exception;


	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/caches/search")
	public ResponseEntity<Object> configCacheSearch(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "name") String name, @RequestParam(value = "limit") int limit) throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/kvm/search")
	public ResponseEntity<Object> configKvmSearch(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "name") String name, @RequestParam(value = "limit") int limit) throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/targetserver/search")
	public ResponseEntity<Object> configTargetServerSearch(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "name") String name, @RequestParam(value = "limit") int limit) throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/config/productserver/search")
	public ResponseEntity<Object> configProductServerSearch(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "name") String name, @RequestParam(value = "limit") int limit) throws Exception;


}
