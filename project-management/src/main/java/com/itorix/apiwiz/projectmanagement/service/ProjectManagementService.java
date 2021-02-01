package com.itorix.apiwiz.projectmanagement.service;

import java.text.ParseException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.projectmanagement.Organization;
import com.itorix.apiwiz.common.model.projectmanagement.Project;
import com.itorix.apiwiz.common.model.projectmanagement.ScmPromote;
import com.itorix.apiwiz.common.model.projectmanagement.ServiceRegistry;
import com.itorix.apiwiz.common.model.proxystudio.OrgEnv;
import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;

@CrossOrigin
@RestController
public interface ProjectManagementService {
	@RequestMapping(method = RequestMethod.POST, value = "/v1/projects", consumes = { "application/json" }, produces = {
	"application/json" })
	public ResponseEntity<Object> createNewProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody Project project, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException;
	@RequestMapping(method = RequestMethod.GET, value = "/v1/projects", produces = { "application/json" })
	public ResponseEntity<Object> projectsList(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid
			) throws ItorixException;
	@RequestMapping(method = RequestMethod.GET, value = "/v1/projects/{projectName:.+}", produces = { "application/json" })
	public ResponseEntity<Object> getParticularProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("projectName") String projectName, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws  ItorixException ;
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1/projects/{projectName:.+}/organizations", produces = {
	"application/json" })
	public ResponseEntity<Object> getOrganistaionsForProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("projectName") String projectName, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException;
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1/projects/{projectName:.+}/{proxiesName}/organizations", produces = {
	"application/json" })
	public ResponseEntity<Object> getOrganistaionsForProxyForProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("projectName") String projectName, @PathVariable("proxiesName") String proxiesName,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException;
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1/projects/{projectName:.+}/proxies", produces = {
	"application/json" })
	public ResponseEntity<Object> getProxiesForProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("projectName") String projectName, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException;
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1/projects/{projectName:.+}/products", produces = {
	"application/json" })
	public ResponseEntity<Object> getProductsForProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("projectName") String projectName, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException ;
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1/projects/{projectName:.+}/{proxiesName}/products", produces = {
	"application/json" })
	public ResponseEntity<Object> getProductsForProxyForProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("projectName") String projectName, @PathVariable("proxiesName") String proxiesName,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException;
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1/projects/{projectName}/apps", produces = {
	"application/json" })
	public ResponseEntity<Object> getAppsForProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("projectName") String projectName, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException;
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1/projects/{projectName:.+}/status", produces = {
	"application/json" })
	public ResponseEntity<Object> getStatusOfProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("projectName") String projectName, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException ;
	@RequestMapping(method = RequestMethod.GET, value = "/v1/projects/{projectName:.+}/contacts", produces = {
	"application/json" })
	public ResponseEntity<Object> getContactsOfProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("projectName") String projectName, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException;
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1/projects/overview", produces = { "application/json" })
	public ResponseEntity<Object> getOverviewOfProjects(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset) throws ItorixException ;
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1/projects/{projectName:.+}/{proxiesName}/apps", produces = {
	"application/json" })
	public ResponseEntity<Object> getAppsForProxyForProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("projectName") String projectName, @PathVariable("proxiesName") String proxiesName,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException ;
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/projects/{projectName:.+}", consumes = {
	"application/json" }, produces = { "application/json" })
	public ResponseEntity<Object> updateProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody Project project, @PathVariable("projectName") String projectName,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException;
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/projects/{projectName:.+}", produces = {
	"application/json" })
	public ResponseEntity<Object> deleteProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("projectName") String projectName, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException;
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1/projects/stats")
	public ResponseEntity<Object> getProjectStats(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam(value = "timeunit", required = false) String timeunit,
			@RequestParam(value = "timerange", required = false) String timerange) throws ParseException;
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1/projects/{projectName:.+}/proxy/{proxyName:.+}/upload", consumes = {
			"multipart/form-data", "multipart/mixed" }, produces = { "application/json" })
	public ResponseEntity<?> uploadProxyDocument(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "type") String type,
			@PathVariable("projectName") String projectName,
			@PathVariable("proxyName") String proxyName) throws Exception;
	
	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/projects/{projectName:.+}/proxy/{proxyName:.+}/wsdl/{fileName:.+}")
	public ResponseEntity<?> getWSDLDocument(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@PathVariable("projectName") String projectName,
			@PathVariable("proxyName") String proxyName,
			@PathVariable("fileName") String fileName) throws Exception;
	
	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/projects/{projectName:.+}/proxy/{proxyName:.+}/xsd/{fileName:.+}")
	public ResponseEntity<?> getXSDDocument(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@PathVariable("projectName") String projectName,
			@PathVariable("proxyName") String proxyName,
			@PathVariable("fileName") String fileName) throws Exception;
	
	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/projects/{projectName:.+}/proxy/{proxyName:.+}/attachment/{fileName:.+}")
	public ResponseEntity<?> getAttachment(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@PathVariable("projectName") String projectName,
			@PathVariable("proxyName") String proxyName,
			@PathVariable("fileName") String fileName) throws Exception;
	
	@UnSecure
	@RequestMapping(method = RequestMethod.POST, value = "/v1/projects/{proxyName:.+}/proxyconnections")
	public ResponseEntity<?> createProxyConnections(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID" , required = false) String jsessionid, 
			@PathVariable("proxyName") String proxyName,
			@RequestBody OrgEnv orgEnv) throws Exception;
	@UnSecure
	@RequestMapping(method = RequestMethod.POST, value = "/v1/projects/{projectName:.+}/proxies/{proxyName:.+}/proxyconnections")
	public ResponseEntity<?> createProjectProxyConnections(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID" , required = false) String jsessionid, 
			@PathVariable("proxyName") String proxyName,
			@PathVariable("projectName") String projectName,
			@RequestBody OrgEnv orgEnv) throws Exception;
	
	@RequestMapping(method = RequestMethod.GET, value = {"/v1/projects/{projectName:.+}/proxies/{proxyName:.+}/service-registry/{serviceName}",
			"/v1/projects/{projectName:.+}/proxies/{proxyName:.+}/service-registry"})
	public ResponseEntity<?> getServiceRegistry(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@PathVariable("projectName") String projectName,
			@PathVariable("proxyName") String proxyName,
			@PathVariable(value = "serviceName", required = false) String serviceName) throws Exception;
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1/projects/{projectName:.+}/proxies/{proxyName:.+}/service-registry")
	public ResponseEntity<?> createServiceRegistry(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@PathVariable("projectName") String projectName,
			@PathVariable("proxyName") String proxyName,
			@RequestBody ServiceRegistry serviceRegistry) throws Exception;
	
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/projects/{projectName:.+}/proxies/{proxyName:.+}/service-registry/{serviceName}")
	public ResponseEntity<?> updateServiceRegistry(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@PathVariable("projectName") String projectName,
			@PathVariable("proxyName") String proxyName,
			@PathVariable("serviceName") String serviceName,
			@RequestBody ServiceRegistry serviceRegistry) throws Exception;
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/projects/{projectName:.+}/proxies/{proxyName:.+}/service-registry/{serviceName}")
	public ResponseEntity<?> deleteServiceRegistry(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@PathVariable("projectName") String projectName,
			@PathVariable("proxyName") String proxyName,
			@PathVariable("serviceName") String serviceName) throws Exception;
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1/projects/{projectName:.+}/service-migration")
	public ResponseEntity<?> serviceMigration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@PathVariable("projectName") String projectName,
			@RequestParam("file") MultipartFile file) throws Exception;
	
	@RequestMapping(method = RequestMethod.POST, value = "v1/projects/migration-data/import")
	public ResponseEntity<?> importData(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@RequestParam("file") MultipartFile file) throws Exception;
	
	@RequestMapping(method = RequestMethod.POST, value = "v1/projects/{projectName:.+}/proxies/{proxyName:.+}/generate")
	public ResponseEntity<?> generateProxy(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@PathVariable("projectName") String projectName,
			@PathVariable("proxyName") String proxyName) throws Exception;
	
	@RequestMapping(method = RequestMethod.POST, value = "v1/projects/{projectName:.+}/proxies/{proxyName:.+}/promote")
	public ResponseEntity<?> promoteToMaster(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@PathVariable("projectName") String projectName,
			@PathVariable("proxyName") String proxyName,
			@RequestBody ScmPromote scmPromote) throws Exception;
	
	@RequestMapping(method = RequestMethod.POST, value = "v1/projects/{projectName:.+}/proxies/{proxyName:.+}/service-registry/{registryName}/publish")
	public ResponseEntity<?> publishEndpoint(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@PathVariable("projectName") String projectName,
			@PathVariable("proxyName") String proxyName,
			@PathVariable("registryName") String registryName,
			@RequestBody Organization organization) throws Exception;
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1/projects/branchtypes", produces = { "application/json" })
	public ResponseEntity<Object> getBranchTypes(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException ;
	
	@RequestMapping(method = RequestMethod.GET, value = "v1/projects/search", produces = { "application/json" })
	public ResponseEntity<?> projectSearch(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,@RequestParam("name") String name,@RequestParam("limit") int limit) throws ItorixException ;

	
}
