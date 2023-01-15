package com.itorix.apiwiz.datapower.service;

import com.itorix.apiwiz.datapower.model.PromoteProxyRequest;
import com.itorix.apiwiz.datapower.model.ProxySearchRequest;
import com.itorix.apiwiz.datapower.model.proxy.GenerateProxyRequestDTO;
import com.itorix.apiwiz.datapower.model.proxy.Proxy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@CrossOrigin
@RestController
public interface DatapowerService {
	
	
	@PostMapping(value = "/v1/datapower/proxies", consumes = {
			"application/json"}, produces = {"application/json"})
	ResponseEntity<Object> createProxy(@RequestBody Proxy proxy,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PutMapping(value = "/v1/datapower/proxies/{proxyId}", consumes = {
			"application/json"})
	ResponseEntity<Object> updateProxy(@RequestBody Proxy proxy,
			@PathVariable(value = "proxyId") String proxyId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;
	
	@GetMapping(value = { "/v1/datapower/proxies" , "/v1/datapower/proxies/{proxyId}" }
	, produces = {"application/json"})
	ResponseEntity<Object> getProxy(
			@PathVariable(value = "proxyId", required = false) String proxyId,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize) throws Exception;
	
	@DeleteMapping(value = "/v1/datapower/proxies/{proxyId}", produces = {"application/json"})
	ResponseEntity<Object> deleteProxy(
			@PathVariable(value = "proxyId") String proxyId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;
	
	@PostMapping(value = "/v1/datapower/import", consumes = {"multipart/form-data"})
	ResponseEntity<Object> importDataExcel(@RequestPart(value = "file", required = true) MultipartFile file,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PostMapping(value = "/v1/datapower/proxies/{proxyId}/generateProxy", consumes = {
			MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE}, produces = {
			"application/json"})
	ResponseEntity<Object> generateDatapowerProxy(
			@PathVariable(value = "proxyId") String proxyId,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestPart(value = "requests", required = false) GenerateProxyRequestDTO requests)
			throws Exception;

	@PostMapping(value = "/v1/datapower/proxies/search", produces = {
			"application/json"})
	ResponseEntity<Object> searchDatapowerProxy(@RequestBody ProxySearchRequest proxySearchRequest,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PostMapping(value = "/v1/datapower/proxies/promote", produces = {
			"application/json"})
	ResponseEntity<Object> promoteDatapowerProxy(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody PromoteProxyRequest promoteProxyRequest) throws Exception;
	

}


