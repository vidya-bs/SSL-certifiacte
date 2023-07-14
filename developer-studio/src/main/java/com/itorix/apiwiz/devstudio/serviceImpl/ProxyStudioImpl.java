package com.itorix.apiwiz.devstudio.serviceImpl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.proxystudio.Category;
import com.itorix.apiwiz.common.model.proxystudio.CodeGenHistory;
import com.itorix.apiwiz.common.model.proxystudio.OrgEnv;
import com.itorix.apiwiz.common.model.proxystudio.OrgEnvs;
import com.itorix.apiwiz.common.model.proxystudio.ProxyArtifacts;
import com.itorix.apiwiz.devstudio.businessImpl.CodeGenService;
import com.itorix.apiwiz.devstudio.model.Operations;
import com.itorix.apiwiz.devstudio.model.PromoteSCM;
import com.itorix.apiwiz.devstudio.service.ProxyStudio;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;

@CrossOrigin
@RestController
public class ProxyStudioImpl implements ProxyStudio {

	@Autowired
	private CodeGenService codeGenService;

	@Autowired
	private IdentityManagementDao commonServices;

	@Autowired
	private Operations operations;

	@Autowired
	private ObjectMapper mapper;

	@Value("${server.contextPath}")
	private String context;

	@Override
	public ResponseEntity<?> generate(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody CodeGenHistory codeGen,
			@RequestHeader HttpHeaders headers, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String folderPath = request.getServletContext().getRealPath("/");
		Operations operations = new Operations();
		operations.setDir(folderPath);
		operations.setjSessionid(jsessionid);
		User user = commonServices.getUserDetailsFromSessionID(jsessionid);
		operations.setUser(user);
		Object obj = codeGenService.processCodeGen(codeGen, operations, null);
		response.setContentType("application/json");
		return new ResponseEntity<>(obj, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> updateProxyDetails(String interactionid, String jsessionid,
			CodeGenHistory codeGen, HttpHeaders headers, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String folderPath = request.getServletContext().getRealPath("/");
		Operations operations = new Operations();
		operations.setDir(folderPath);
		operations.setjSessionid(jsessionid);
		User user = commonServices.getUserDetailsFromSessionID(jsessionid);
		operations.setUser(user);
		Object obj = codeGenService.processCodeGen(codeGen, operations, null);
		response.setContentType("application/json");
		return new ResponseEntity<>(obj, HttpStatus.OK);
	}
	@Override
	public ResponseEntity<?> getProxyOperations(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam("file") MultipartFile file,
			@RequestParam("type") String type, @RequestHeader HttpHeaders headers, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Operations operations = new Operations();
		operations.setFile(file);
		operations.setType(type);
		operations.setFileName(file.getOriginalFilename());
		Object object = codeGenService.proxyOperations(operations);
		return new ResponseEntity<>(object, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getProxyOperations(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @RequestParam("swaggername") String swaggername,
			@RequestParam("revision") Integer revision, @RequestHeader HttpHeaders headers, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Operations operations = new Operations();
		operations.setType("swagger");
		operations.setFileName(swaggername);
		operations.setVersion(revision);
		operations.setSwaggerInDB(true);
		operations.setOas(oas);
		Object object = codeGenService.proxyOperations(operations);
		return new ResponseEntity<>(object, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getTargetOperations(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @RequestParam("swaggername") String swaggername,
			@RequestParam("revision") Integer revision, @RequestHeader HttpHeaders headers, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Operations operations = new Operations();
		operations.setType("swagger");
		operations.setFileName(swaggername);
		operations.setVersion(revision);
		operations.setSwaggerInDB(true);
		operations.setOas(oas);
		Object object = codeGenService.targetOperations(operations);
		String jsonResponse = mapper.writeValueAsString(object).replace("\\\"", "&quot;");
		return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getTargetOperations(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam("file") MultipartFile file,
			@RequestParam("type") String type, @RequestHeader HttpHeaders headers, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Operations operations = new Operations();
		operations.setFile(file);
		operations.setType(type);
		operations.setFileName(file.getOriginalFilename());
		Object object = codeGenService.targetOperations(operations);
		ObjectMapper mapper = new ObjectMapper();
		String jsonResponse = mapper.writeValueAsString(object).replace("\\\"", "&quot;");
		return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> gethistory(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "proxy", required = false) String proxy) throws Exception {
		return new ResponseEntity<>(codeGenService.getHistory(offset, pageSize, proxy), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getProxies(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		return new ResponseEntity<>(codeGenService.getProxies(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> saveAssociatedOrg(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody OrgEnvs orgEnvs,
			@PathVariable("Proxy") String proxy, @RequestHeader HttpHeaders headers, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		operations.setUser(commonServices.getUserDetailsFromSessionID(jsessionid));
		codeGenService.saveAssociatedOrgs(proxy, orgEnvs);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> saveProxyArtifacts(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody ProxyArtifacts proxyArtifacts, @PathVariable("Proxy") String proxy,
			@RequestHeader HttpHeaders headers, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		return new ResponseEntity<>(codeGenService.saveProxyArtifacts(proxy, proxyArtifacts), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getProxyArtifacts(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody @PathVariable("Proxy") String proxy, @RequestHeader HttpHeaders headers,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		return new ResponseEntity<>(codeGenService.getProxyArtifacts(proxy), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAssociatedOrg(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("Proxy") String proxy,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		operations.setUser(commonServices.getUserDetailsFromSessionID(jsessionid));
		return new ResponseEntity<>(codeGenService.getAssociatedOrgs(proxy), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAssociatedApigeeDetails(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("Proxy") String proxy,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "refresh", required = false) boolean refresh) throws Exception {
		return new ResponseEntity<>(codeGenService.getApigeeDetails(proxy, refresh, type), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> removeApigeeProxy(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("Proxy") String proxy)
			throws Exception {
		codeGenService.removeProxy(proxy);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1/buildconfig/proxies/{proxyName:.+}/proxyconnections")
	public ResponseEntity<?> createProjectProxyConnections(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
			@PathVariable("proxyName") String proxyName, @RequestBody OrgEnv orgEnv) throws Exception {
		codeGenService.publishProxyConnections(proxyName, orgEnv);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> promoteApigeeProxy(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody PromoteSCM scmConfig)
			throws Exception {
		codeGenService.promoteProxy(scmConfig);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> addCategory(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody List<Category> categories,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		codeGenService.saveCategory(categories);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> updateCategory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody List<Category> categories,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		codeGenService.saveCategory(categories);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> deleteCategory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody Category category,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		codeGenService.deleteCategory(category);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getCategories(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "swaggerid", required = false) String swaggerid,
			@RequestParam(value = "revision", required = false, defaultValue = "1") int revision,
			@RequestParam(value = "oas", required = false, defaultValue = "2.0") String oas, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		if (null != swaggerid) {
			return new ResponseEntity<>(codeGenService.getCategories(swaggerid, revision, oas), HttpStatus.OK);
		}
		return new ResponseEntity<>(codeGenService.getCategories(), HttpStatus.OK);
	}

	// @Override
	// public ResponseEntity<?> getProxies(
	// @PathVariable("Proxy") String proxy,
	// @PathVariable("Revision") String revision,
	// @RequestHeader(value = "interactionid", required = false) String
	// interactionid,
	// @RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception
	// {
	// return new ResponseEntity<> (codeGenService.getProxies(proxy,revision) ,
	// HttpStatus.OK);
	// }

	@UnSecure
	@Override
	public ResponseEntity<?> getquery(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		return new ResponseEntity<>(codeGenService.insertFile(file), HttpStatus.OK);
	}

	@UnSecure
	@Override
	public ResponseEntity<?> deleteFile(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam("fileName") String fileName, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		return new ResponseEntity<>(codeGenService.removeFile(fileName), HttpStatus.OK);
	}

	// @Override
	// public ResponseEntity<?> getFiles(
	// @RequestHeader(value = "interactionid", required = false) String
	// interactionid,
	// @PathVariable("sourceType") String sourceType,
	// HttpServletRequest request, HttpServletResponse response)
	// throws Exception {
	// return new ResponseEntity<>(codeGenService.getFolders("API/" + sourceType
	// + "/"),
	// HttpStatus.OK);
	// }
	//
	// @Override
	// public ResponseEntity<?> getFiles(
	// @RequestHeader(value = "interactionid", required = false) String
	// interactionid,
	// @PathVariable("sourceType") String sourceType,
	// @PathVariable("resourceType") String resourceType,
	// HttpServletRequest request, HttpServletResponse response) throws
	// Exception {
	// Object returnData = codeGenService.getFolders("API/" + sourceType + "/" +
	// resourceType);
	// if (returnData == null) {
	// returnData = codeGenService.getFile(resourceType);
	// }
	// return new ResponseEntity<>(returnData, HttpStatus.OK);
	// }

	@Override
	public ResponseEntity<?> getFiles(@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		return new ResponseEntity<>(codeGenService.getFolders("API/"), HttpStatus.OK);
	}

	@UnSecure
	@Override
	public ResponseEntity<?> addFile(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam("file") MultipartFile file, @PathVariable("sourceType") String sourceType,
			@PathVariable("resourceType") String resourceType, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		codeGenService.insertFile(file);
		codeGenService.addFolderResource("API/" + sourceType + "/" + resourceType, file.getOriginalFilename(), false);
		return new ResponseEntity<>(codeGenService.getFolders("API/" + sourceType + "/" + resourceType), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> uploadTemplates(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		return new ResponseEntity<>(codeGenService.uploadTemplates(file), HttpStatus.OK);
	}
	@Override
	public ResponseEntity<?> downloadTemplates(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable(value = "name",required = false) String fileName, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		return codeGenService.downloadTemplates(fileName);
	}

	// @SuppressWarnings("deprecation")
	// @Override
	// public ResponseEntity<?> insertFile(
	// @RequestHeader(value = "interactionid", required = false) String
	// interactionid,
	// @RequestHeader(value = "JSESSIONID") String jsessionid,
	// @PathVariable("file") String file,
	// @PathVariable("sourceType") String sourceType,
	// @PathVariable("resourceType") String resourceType,
	// @RequestHeader HttpHeaders headers, HttpServletRequest request,
	// HttpServletResponse response)
	// throws Exception {
	// String content = codeGenService.getFile(file);
	// return new ResponseEntity<>(IOUtils.toByteArray(content), HttpStatus.OK);
	// }

	@UnSecure
	@Override
	public ResponseEntity<?> removeFile(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("file") String file, @PathVariable("sourceType") String sourceType,
			@PathVariable("resourceType") String resourceType, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		codeGenService.removeFile(file);
		codeGenService.removeFile("API/" + sourceType + "/" + resourceType, file);
		return new ResponseEntity<>(codeGenService.getFolders("API/" + sourceType + "/" + resourceType), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> search(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam("name") String name,
			@RequestParam("limit") int limit) throws JsonProcessingException, ItorixException {
		return new ResponseEntity<Object>(codeGenService.proxySearch(interactionid, name, limit), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> locateResouce(@RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest httpServletRequest) throws Exception {
		String uri = httpServletRequest.getRequestURI().replaceAll(context, "");
		uri = uri.replaceAll("/v1/api/template", "");
		Object returnData = codeGenService.getFolders("API" + uri);
		if (returnData == null) {
			String[] resources = uri.split("/");
			int length = resources.length;
			returnData = codeGenService.getFile(resources[length - 1]);
		}
		return new ResponseEntity<>(returnData, HttpStatus.OK);
	}
}
