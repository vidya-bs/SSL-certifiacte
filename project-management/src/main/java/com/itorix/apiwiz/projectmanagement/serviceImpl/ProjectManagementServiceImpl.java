package com.itorix.apiwiz.projectmanagement.serviceImpl;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.projectmanagement.BranchType;
import com.itorix.apiwiz.common.model.projectmanagement.Organization;
import com.itorix.apiwiz.common.model.projectmanagement.Project;
import com.itorix.apiwiz.common.model.projectmanagement.ProjectFile;
import com.itorix.apiwiz.common.model.projectmanagement.ScmPromote;
import com.itorix.apiwiz.common.model.projectmanagement.ServiceRegistry;
import com.itorix.apiwiz.common.model.proxystudio.OrgEnv;
import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.projectmanagement.businessImpl.ProjectBusinessImpl;
import com.itorix.apiwiz.projectmanagement.dao.ProjectManagementDao;
import com.itorix.apiwiz.projectmanagement.service.ProjectManagementService;

@CrossOrigin
@RestController
public class ProjectManagementServiceImpl implements ProjectManagementService {

	@Autowired
	ProjectManagementDao projectManagementDao;
	
	@Autowired
	ProjectBusinessImpl projectBusinessImpl;

	public ResponseEntity<Object> createNewProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody Project project, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException{
		return new ResponseEntity<Object>(
				projectManagementDao.createNewProject(project, jsessionid), HttpStatus.CREATED);

	}

	public ResponseEntity<Object> projectsList(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid
			) throws ItorixException{
		return new ResponseEntity<Object>(projectManagementDao.getAllProjectNames(), HttpStatus.OK);
	}

	public ResponseEntity<Object> getParticularProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("projectName") String projectName, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws  ItorixException{
		return new ResponseEntity<Object>(projectManagementDao.findProjectByName(projectName),
				HttpStatus.OK);
	}
	public ResponseEntity<Object> getOrganistaionsForProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("projectName") String projectName, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException{
		return new ResponseEntity<Object>(
				projectManagementDao.findOrganistionsOfProject(projectName), HttpStatus.OK);
	}
	public ResponseEntity<Object> getOrganistaionsForProxyForProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("projectName") String projectName, @PathVariable("proxiesName") String proxiesName,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException{

		return new ResponseEntity<Object>(
				projectManagementDao.findOrganistionsOfProxyOfProject(projectName, proxiesName),
				HttpStatus.OK);
	}

	public ResponseEntity<Object> getProxiesForProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("projectName") String projectName, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException{
		return new ResponseEntity<Object>(projectManagementDao.findProxiesForProject(projectName),
				HttpStatus.OK);
	}

	public ResponseEntity<Object> getProductsForProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("projectName") String projectName, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException{
		return new ResponseEntity<Object>(projectManagementDao.findProductsForProject(projectName),
				HttpStatus.OK);
	}
	public ResponseEntity<Object> getProductsForProxyForProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("projectName") String projectName, @PathVariable("proxiesName") String proxiesName,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException{
		return new ResponseEntity<Object>(
				projectManagementDao.findProductsForProxyForProject(projectName, proxiesName),
				HttpStatus.OK);
	}
	public ResponseEntity<Object> getAppsForProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("projectName") String projectName, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException{
		return new ResponseEntity<Object>(projectManagementDao.findAppsForProject(projectName),
				HttpStatus.OK);
	}
	public ResponseEntity<Object> getStatusOfProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("projectName") String projectName, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException {
		return new ResponseEntity<Object>(projectManagementDao.findStatusOfProject(projectName),
				HttpStatus.OK);
	}

	public ResponseEntity<Object> getContactsOfProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("projectName") String projectName, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException{
		return new ResponseEntity<Object>(projectManagementDao.findContactsOfProject(projectName),
				HttpStatus.OK);
	}
	public ResponseEntity<Object> getOverviewOfProjects(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset) throws ItorixException{
		return new ResponseEntity<Object>(projectManagementDao.getOverview(offset), HttpStatus.OK);
	}
	public ResponseEntity<Object> getAppsForProxyForProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("projectName") String projectName, @PathVariable("proxiesName") String proxiesName,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException{
		return new ResponseEntity<Object>(
				projectManagementDao.findAppsForProxyForProject(projectName, proxiesName),
				HttpStatus.OK);
	}
	public ResponseEntity<Object> updateProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody Project project, @PathVariable("projectName") String projectName,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException{
		return new ResponseEntity<Object>(projectManagementDao.updateProject(project, jsessionid),
				HttpStatus.NO_CONTENT);
	}
	public ResponseEntity<Object> deleteProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("projectName") String projectName, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException{
		return new ResponseEntity<Object>(
				projectManagementDao.deleteProject(projectName, jsessionid),
				HttpStatus.NO_CONTENT);
	}
	public ResponseEntity<Object> getProjectStats(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam(value = "timeunit", required = false) String timeunit,
			@RequestParam(value = "timerange", required = false) String timerange) throws ParseException{
		return new ResponseEntity<Object>(projectManagementDao.getProjectStats(timeunit, timerange),
				HttpStatus.OK);
	}

	public ResponseEntity<?> uploadProxyDocument(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "type") String type,
			@PathVariable("projectName") String projectName,
			@PathVariable("proxyName") String proxyName) throws Exception{
		ProjectFile projectFile = new  ProjectFile();
		projectFile.setFileName(file.getOriginalFilename());
		projectFile.setProjectName(projectName);
		projectFile.setProxyName(proxyName);
		projectFile.setType(type.toUpperCase());
		return new ResponseEntity<Object>(projectManagementDao.uploadProjectFile(file, projectFile),
				HttpStatus.OK);
	}
	
	@UnSecure
	public ResponseEntity<?> getWSDLDocument(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid, 
			@PathVariable("projectName") String projectName,
			@PathVariable("proxyName") String proxyName,
			@PathVariable("fileName") String fileName) throws Exception{
		ProjectFile projectFile = new  ProjectFile();
		projectFile.setFileName(fileName);
		projectFile.setProjectName(projectName);
		projectFile.setProxyName(proxyName);
		projectFile.setType("WSDL");
		String data = projectManagementDao.readProjectFile(projectFile);
		ByteArrayResource resource = new ByteArrayResource(data.getBytes("UTF-8"));
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                .contentLength(data.getBytes().length) 
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
	}
	
	@UnSecure
	public ResponseEntity<?> getXSDDocument(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid, 
			@PathVariable("projectName") String projectName,
			@PathVariable("proxyName") String proxyName,
			@PathVariable("fileName") String fileName) throws Exception{
		ProjectFile projectFile = new  ProjectFile();
		projectFile.setFileName(fileName);
		projectFile.setProjectName(projectName);
		projectFile.setProxyName(proxyName);
		projectFile.setType("XSD");
		String data = projectManagementDao.readProjectFile(projectFile);
		ByteArrayResource resource = new ByteArrayResource(data.getBytes("UTF-8"));
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                .contentLength(data.getBytes().length) 
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
	}
	
	@UnSecure
	public ResponseEntity<?> getAttachment(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid, 
			@PathVariable("projectName") String projectName,
			@PathVariable("proxyName") String proxyName,
			@PathVariable("fileName") String fileName) throws Exception{
		ProjectFile projectFile = new  ProjectFile();
		projectFile.setFileName(fileName);
		projectFile.setProjectName(projectName);
		projectFile.setProxyName(proxyName);
		projectFile.setType("ATTACHMENT");
		String data = projectManagementDao.readProjectFile(projectFile);
		ByteArrayResource resource = new ByteArrayResource(data.getBytes("UTF-8"));
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                .contentLength(data.getBytes().length) 
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
	}
	
	@UnSecure
	public ResponseEntity<?> createProxyConnections(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID" , required = false) String jsessionid, 
			@PathVariable("proxyName") String proxyName,
			@RequestBody OrgEnv orgEnv) throws Exception{
		projectManagementDao.createProxyConnections(proxyName, orgEnv);
		return new ResponseEntity<Object>(
				HttpStatus.CREATED);
	}
	
	@UnSecure
	public ResponseEntity<?> createProjectProxyConnections(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID" , required = false) String jsessionid, 
			@PathVariable("proxyName") String proxyName,
			@PathVariable("projectName") String projectName,
			@RequestBody OrgEnv orgEnv) throws Exception{
		projectManagementDao.createProxyConnections(projectName, proxyName, orgEnv);
		return new ResponseEntity<Object>(
				HttpStatus.CREATED);
	}
	 
	public ResponseEntity<?> getServiceRegistry(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@PathVariable("projectName") String projectName,
			@PathVariable("proxyName") String proxyName,
			@PathVariable(value = "serviceName" , required = false) String serviceName) throws Exception{
		if(serviceName != null && serviceName != "")
		return new ResponseEntity<Object>(projectManagementDao.getServiceRegistry(projectName, proxyName, serviceName),
				HttpStatus.OK);
		else
			return new ResponseEntity<Object>(projectManagementDao.getServiceRegistry(projectName, proxyName),
					HttpStatus.OK);
	}
	
	public ResponseEntity<?> createServiceRegistry(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@PathVariable("projectName") String projectName,
			@PathVariable("proxyName") String proxyName,
			@RequestBody ServiceRegistry serviceRegistry) throws Exception{
		projectManagementDao.createUpdateServiceRegistry(projectName, proxyName, serviceRegistry);
		return new ResponseEntity<Object>(HttpStatus.CREATED);
	}
	
	public ResponseEntity<?> updateServiceRegistry(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@PathVariable("projectName") String projectName,
			@PathVariable("proxyName") String proxyName,
			@PathVariable("serviceName") String serviceName,
			@RequestBody ServiceRegistry serviceRegistry) throws Exception{
		projectManagementDao.createUpdateServiceRegistry(projectName, proxyName, serviceRegistry);
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
	
	public ResponseEntity<?> deleteServiceRegistry(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@PathVariable("projectName") String projectName,
			@PathVariable("proxyName") String proxyName,
			@PathVariable("serviceName") String serviceName) throws Exception{
		projectManagementDao.deleteServiceRegistry(projectName, proxyName, serviceName);
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
	
	public ResponseEntity<?> serviceMigration(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@PathVariable("projectName") String projectName,
			@RequestParam("file") MultipartFile file) throws Exception{
		projectManagementDao.loadProxyData(file, projectName, jsessionid);
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
	
	public ResponseEntity<?> importData(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@RequestParam("file") MultipartFile file) throws Exception{
		return new ResponseEntity<Object>(projectManagementDao.loadExcelData(file, jsessionid),
				HttpStatus.OK);
	}
	
	public ResponseEntity<?> generateProxy(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@PathVariable("projectName") String projectName,
			@PathVariable("proxyName") String proxyName) throws Exception{
		
		return new ResponseEntity<Object>(projectManagementDao.generateProxy(projectName, proxyName, jsessionid),
				HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> promoteToMaster(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@PathVariable("projectName") String projectName,
			@PathVariable("proxyName") String proxyName,
			@RequestBody ScmPromote scmPromote) throws Exception {
		projectManagementDao.promoteToMaster(scmPromote, projectName, proxyName, jsessionid);
		return new ResponseEntity<Object>("",
				HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<?> publishEndpoint(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, 
			@PathVariable("projectName") String projectName,
			@PathVariable("proxyName") String proxyName,
			@PathVariable("registryName") String registryName,
			@RequestBody Organization organization) throws Exception{
		projectBusinessImpl.createServiceConfig(organization, projectName, proxyName, registryName, jsessionid);
		return new ResponseEntity<Object>("",
				HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object> getBranchTypes(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException {
		return new ResponseEntity<Object>(BranchType.values(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> projectSearch(String interactionid, String jsessionid, String name, int limit)
			throws ItorixException {
		return new ResponseEntity<Object>(projectManagementDao.searchProject(name, limit), HttpStatus.OK);
	}
	
}