package com.itorix.apiwiz.devstudio.service;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.proxystudio.Category;
import com.itorix.apiwiz.common.model.proxystudio.CodeGenHistory;
import com.itorix.apiwiz.common.model.proxystudio.OrgEnv;
import com.itorix.apiwiz.common.model.proxystudio.OrgEnvs;
import com.itorix.apiwiz.common.model.proxystudio.ProxyArtifacts;
import com.itorix.apiwiz.devstudio.model.PromoteSCM;
import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@CrossOrigin
@RestController
public interface ProxyStudio {

	@RequestMapping(method = RequestMethod.POST, value = "/v1/buildconfig/codegen/generate", produces = {
	"application/json" })
	public ResponseEntity<?> generate(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody CodeGenHistory codeGen,
			@RequestHeader HttpHeaders headers, HttpServletRequest request, HttpServletResponse response)
					throws Exception ;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/buildconfig/codegen/getproxyflownames", consumes = {
			"multipart/form-data", "multipart/mixed" }, produces = { "application/json" })
	public ResponseEntity<?> getProxyOperations(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam("file") MultipartFile file,
			@RequestParam("type") String type, @RequestHeader HttpHeaders headers, HttpServletRequest request,
			HttpServletResponse response) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/buildconfig/codegen/getproxyflownames", produces = {
	"application/json" })
	public ResponseEntity<?> getProxyOperations(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas,
			@RequestParam("swaggername") String swaggername,
			@RequestParam("revision") Integer revision, @RequestHeader HttpHeaders headers, HttpServletRequest request,
			HttpServletResponse response) throws Exception ;
			
	@RequestMapping(method = RequestMethod.GET, value = "/v1/buildconfig/codegen/gettargetflownames", produces = {
	"application/json" })
	public ResponseEntity<?> getTargetOperations(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@RequestHeader(value = "oas", required = false) String oas,
			@RequestParam("swaggername") String swaggername,
			@RequestParam("revision") Integer revision, @RequestHeader HttpHeaders headers, HttpServletRequest request,
			HttpServletResponse response) throws Exception ;
			
	@RequestMapping(method = RequestMethod.POST, value = "/v1/buildconfig/codegen/gettargetflownames", consumes = {
			"multipart/form-data", "multipart/mixed" }, produces = { "application/json", "application/xml" })
	public ResponseEntity<?> getTargetOperations(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam("file") MultipartFile file,
			@RequestParam("type") String type, @RequestHeader HttpHeaders headers, HttpServletRequest request,
			HttpServletResponse response) throws Exception;
			

	@RequestMapping(method = RequestMethod.GET, value = "/v1/buildconfig/proxies/history", produces = {
	"application/json" })
	public ResponseEntity<?> gethistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize) throws Exception;
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1/buildconfig/proxies", produces = {
	"application/json" })
	public ResponseEntity<?> getProxies(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1/buildconfig/proxies/{Proxy}/associateapigeeorg", produces = {
	"application/json" })
	public ResponseEntity<?> saveAssociatedOrg(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody OrgEnvs orgEnvs,
			@PathVariable("Proxy") String proxy, @RequestHeader HttpHeaders headers, HttpServletRequest request,
			HttpServletResponse response) throws Exception ;
			
	@UnSecure
	@RequestMapping(method = RequestMethod.POST, value = "/v1/buildconfig/proxies/{Proxy}/proxyartifacts", produces = {
	"application/json" })
	public ResponseEntity<?> saveProxyArtifacts(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody ProxyArtifacts proxyArtifacts, @PathVariable("Proxy") String proxy,
			@RequestHeader HttpHeaders headers, HttpServletRequest request, HttpServletResponse response)
					throws Exception ;
					
	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/buildconfig/proxies/{Proxy}/proxyartifacts", produces = {
	"application/json" })
	public ResponseEntity<?> getProxyArtifacts(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody @PathVariable("Proxy") String proxy, @RequestHeader HttpHeaders headers,
			HttpServletRequest request, HttpServletResponse response) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/buildconfig/proxies/{Proxy}/associateapigeeorg", produces = {
	"application/json" })
	public ResponseEntity<?> getAssociatedOrg(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("Proxy") String proxy,
			HttpServletRequest request, HttpServletResponse response) throws Exception ;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/buildconfig/proxies/{Proxy}/details", produces = {
	"application/json" })
	public ResponseEntity<?> getAssociatedApigeeDetails(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@PathVariable("Proxy") String proxy,
			@RequestParam(value = "type", required=false) String type,
			@RequestParam(value = "refresh",required=false) boolean refresh)
					throws Exception ;
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/buildconfig/proxies/{Proxy}", produces = {
	"application/json" })
	public ResponseEntity<?> removeApigeeProxy(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@PathVariable("Proxy") String proxy)
					throws Exception ;
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1/buildconfig/proxies/{proxyName:.+}/proxyconnections")
	public ResponseEntity<?> createProjectProxyConnections(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID" , required = false) String jsessionid, 
			@PathVariable("proxyName") String proxyName,
			@RequestBody OrgEnv orgEnv) throws Exception;
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1/buildconfig/codegen/promote-code", produces = {
	"application/json" })
	public ResponseEntity<?> promoteApigeeProxy(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@RequestBody PromoteSCM scmConfig)throws Exception ;
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1/buildconfig/proxies/category")
	public ResponseEntity<?> addCategory(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@RequestBody List<Category> categories,
			HttpServletRequest request, HttpServletResponse response) throws Exception ;
			
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/buildconfig/proxies/category")
	public ResponseEntity<?> updateCategory(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody List<Category> categories,
			HttpServletRequest request, HttpServletResponse response) throws Exception ;
			
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/buildconfig/proxies/category")
	public ResponseEntity<?> deleteCategory(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody Category category,
			HttpServletRequest request, HttpServletResponse response) throws Exception; 
			
	@RequestMapping(method = RequestMethod.GET, value = "/v1/buildconfig/proxies/category")
	public ResponseEntity<?> getCategories(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			HttpServletResponse response) throws Exception ;
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{Proxy}/revisions/{Revision}/getassociatedproxy", produces = {
	"application/json" })
	public ResponseEntity<?> getProxies(
			@PathVariable("Proxy") String proxy,
			@PathVariable("Revision") String revision,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;
			
	@UnSecure
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/template/file")
	public ResponseEntity<?> getquery(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response)
					throws Exception ;

	@UnSecure
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/api/template/file")
	public ResponseEntity<?> deleteFile(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam("fileName") String fileName, HttpServletRequest request, HttpServletResponse response)
					throws Exception ;

//	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/template/{sourceType}")
//	public ResponseEntity<?> getFiles(@RequestHeader(value = "interactionid", required = false) String interactionid,
//			@PathVariable("sourceType") String sourceType, HttpServletRequest request, HttpServletResponse response)
//					throws Exception ;

//	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/template/{sourceType}/{resourceType:.+}")
//	public ResponseEntity<?> getFiles(@RequestHeader(value = "interactionid", required = false) String interactionid,
//			@PathVariable("sourceType") String sourceType, @PathVariable("resourceType") String resourceType,
//			HttpServletRequest request, HttpServletResponse response) throws Exception ;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/template")
	public ResponseEntity<?> getFiles(@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request, HttpServletResponse response) throws Exception ;
	@UnSecure
	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/template/{sourceType}/{resourceType}")
	public ResponseEntity<?> addFile(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam("file") MultipartFile file, @PathVariable("sourceType") String sourceType,
			@PathVariable("resourceType") String resourceType, HttpServletRequest request, HttpServletResponse response)
					throws Exception ;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/api/template/uploadtemplates")
	public ResponseEntity<?> uploadTemplates(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response)
					throws Exception ;

//	@RequestMapping(method = RequestMethod.GET, value = "/v1/api/template/{sourceType}/{resourceType}/{file:.+}")
//	public ResponseEntity<?> insertFile(@RequestHeader(value = "interactionid", required = false) String interactionid,
//			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("file") String file,
//			@PathVariable("sourceType") String sourceType, @PathVariable("resourceType") String resourceType,
//			@RequestHeader HttpHeaders headers, HttpServletRequest request, HttpServletResponse response)
//					throws Exception ;

	@UnSecure
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/api/template/{sourceType}/{resourceType}/{file:.+}", produces = {
	"application/json" })
	public ResponseEntity<?> removeFile(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("file") String file, @PathVariable("sourceType") String sourceType,
			@PathVariable("resourceType") String resourceType, HttpServletRequest request, HttpServletResponse response)
					throws Exception ;
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1/buildconfig/search", produces = {
	"application/json" })
	public ResponseEntity<?> search(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,@RequestParam("name") String name,@RequestParam("limit") int limit) throws ItorixException, JsonProcessingException ;
	
	@RequestMapping(value="/v1/api/template/**", method=RequestMethod.GET)
	public ResponseEntity<?> locateResouce(@RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest httpServletRequest) throws Exception;

}
