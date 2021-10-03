package com.itorix.apiwiz.design.studio.serviceimpl;

import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.integrations.s3.S3Integration;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.artifatory.JfrogUtilImpl;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.common.util.s3.S3Connection;
import com.itorix.apiwiz.common.util.s3.S3Utils;
import com.itorix.apiwiz.common.util.scm.ScmUtilImpl;
import com.itorix.apiwiz.design.studio.business.SwaggerBusiness;
import com.itorix.apiwiz.design.studio.businessimpl.Swagger3SDK;
import com.itorix.apiwiz.design.studio.businessimpl.ValidateSchema;
import com.itorix.apiwiz.design.studio.businessimpl.XlsUtil;
import com.itorix.apiwiz.design.studio.model.*;
import com.itorix.apiwiz.design.studio.model.swagger.sync.DictionarySwagger;
import com.itorix.apiwiz.design.studio.model.swagger.sync.SwaggerDictionary;
import com.itorix.apiwiz.design.studio.service.SwaggerService;
import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContextHolder;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.codegen.Codegen;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.CodegenType;
import io.swagger.generator.exception.ApiException;
import io.swagger.generator.model.GeneratorInput;
import io.swagger.generator.model.ResponseCode;
import io.swagger.generator.online.Generator;
import io.swagger.models.Swagger;
import org.apache.commons.io.FileUtils;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@CrossOrigin
@RestController
/**
 * To use to generate the new swagger and update the existing swagger.
 *
 * @author itorix.inc
 */
public class SwaggerServiceImpl implements SwaggerService {
	private static final Logger logger = LoggerFactory.getLogger(SwaggerServiceImpl.class);

	static List<String> clients = new ArrayList<String>();
	static List<String> servers = new ArrayList<String>();

	static {
		List<CodegenConfig> extensions = Codegen.getExtensions();
		for (CodegenConfig config : extensions) {
			if (config.getTag().equals(CodegenType.CLIENT) || config.getTag().equals(CodegenType.DOCUMENTATION)) {
				clients.add(config.getName());
			} else if (config.getTag().equals(CodegenType.SERVER)) {
				servers.add(config.getName());
			}
		}
		Collections.sort(clients, String.CASE_INSENSITIVE_ORDER);
		Collections.sort(servers, String.CASE_INSENSITIVE_ORDER);
	}

	@Autowired
	SwaggerBusiness swaggerBusiness;

	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired
	XlsUtil xlsUtil;

	@Autowired
	JfrogUtilImpl jfrogUtilImpl;

	@Autowired
	private S3Connection s3Connection;

	@Autowired
	private S3Utils s3Utils;

	@Autowired
	private ScmUtilImpl scmUtilImpl;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/puls")
	public String checkPuls(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			HttpServletResponse response) {
		return "I am in Live :)";
	}

	private final String COMMIT_MESSAGE = "Pushed latest changes from Apiwiz platform";

	public ResponseEntity<Object> importSwaggers(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "file", required = false) MultipartFile file,
			@RequestParam(value = "type", required = true) String type,
			@RequestParam(value = "gitURI", required = false) String gitURI,
			@RequestParam(value = "branch", required = false) String branch,
			@RequestParam(value = "authType", required = false) String authType,
			@RequestParam(value = "userName", required = false) String userName,
			@RequestParam(value = "password", required = false) String password,
			@RequestParam(value = "personalToken", required = false) String personalToken) throws Exception {
		return new ResponseEntity<Object>(
				swaggerBusiness.importSwaggers(file, type, gitURI, branch, authType, userName, password, personalToken),
				HttpStatus.OK);
	}

	/**
	 * This method is used to create the swagger.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param json
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 * @throws Exception
	 */
	@ApiOperation(value = "Create Swagger", notes = "", response = Void.class)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Swagger Created sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "System Error", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/{swaggername}")
	public ResponseEntity<Void> createSwagger(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@RequestBody String json) throws Exception {
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		HttpHeaders headers = new HttpHeaders();
		if (oas.equals("2.0")) {
			SwaggerVO swaggerVO = new SwaggerVO();
			swaggerVO.setName(swaggername);
			swaggerVO.setInteractionid(interactionid);
			SwaggerVO vo = swaggerBusiness.findSwagger(swaggerVO);
			if (vo != null) {
				swaggerVO.setSwagger(json);
				swaggerBusiness.createSwaggerWithNewRevision(swaggerVO, jsessionid);
			} else {
				swaggerVO.setSwagger(json);
				swaggerVO = swaggerBusiness.createSwagger(swaggerVO);
			}

			swaggerBusiness.updateSwaggerBasePath(swaggerVO.getName(), swaggerVO); // update
			// the
			// base
			// path
			// collection

			headers.add("Access-Control-Expose-Headers", "X-Swagger-Version, X-Swagger-id");
			headers.add("X-Swagger-Version", swaggerVO.getRevision() + "");
			headers.add("X-Swagger-id", swaggerVO.getSwaggerId());
		} else if (oas.equals("3.0")) {
			Swagger3VO swaggerVO = new Swagger3VO();
			swaggerVO.setName(swaggername);
			swaggerVO.setInteractionid(interactionid);
			Swagger3VO vo = swaggerBusiness.findSwagger(swaggerVO);
			if (vo != null) {
				swaggerVO.setSwagger(json);
				swaggerBusiness.createSwaggerWithNewRevision(swaggerVO, jsessionid);
			} else {
				swaggerVO.setSwagger(json);
				swaggerVO = swaggerBusiness.createSwagger(swaggerVO);
			}

			swaggerBusiness.updateSwagger3BasePath(swaggerVO.getName(), swaggerVO);

			headers.add("Access-Control-Expose-Headers", "X-Swagger-Version, X-Swagger-id");
			headers.add("X-Swagger-Version", swaggerVO.getRevision() + "");
			headers.add("X-Swagger-id", swaggerVO.getSwaggerId());
		}
		return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	}

	/**
	 * Using this we can update are change the swagger version.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param create
	 * @param swaggername
	 * @param json
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 * @throws Exception
	 */
	@ApiOperation(value = "Create Swagger With new Revison", notes = "")
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Swagger Created sucessfully", response = Void.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "System Error", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/{swaggername}/revisions")
	public ResponseEntity<Void> createSwaggerWithNewRevision(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas,
			@RequestParam(value = "create", required = false, defaultValue = "new") String create,
			@PathVariable("swaggername") String swaggername, @RequestBody String json) throws Exception {
		RSAEncryption rsaEncryption = new RSAEncryption();
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		HttpHeaders headers = new HttpHeaders();
		if (oas.equals("2.0")) {
			SwaggerVO swaggerVO = new SwaggerVO();
			swaggerVO.setName(swaggername);
			swaggerVO.setInteractionid(interactionid);
			swaggerVO.setSwagger(json);
			swaggerVO = swaggerBusiness.createSwaggerWithNewRevision(swaggerVO, jsessionid);
			swaggerBusiness.updateSwaggerBasePath(swaggerVO.getName(), swaggerVO); // update
			// the
			// base
			// path
			// collection
			SwaggerIntegrations integrations = swaggerBusiness.getGitIntegrations(interactionid, jsessionid,
					swaggerVO.getName(), oas);
			if (integrations != null && integrations.getScm_authorizationType().equalsIgnoreCase("basic")) {
				File file = createSwaggerFile(swaggerVO.getName(), json, integrations.getScm_folder(),
						swaggerVO.getRevision());
				scmUtilImpl.pushFilesToSCM(file, integrations.getScm_repository(),
						rsaEncryption.decryptText(integrations.getScm_username()),
						rsaEncryption.decryptText(integrations.getScm_password()), integrations.getScm_url(),
						integrations.getScm_type(), integrations.getScm_branch(), COMMIT_MESSAGE);
			} else if (integrations != null && integrations.getScm_authorizationType() != null) {
				File file = createSwaggerFile(swaggerVO.getName(), json, integrations.getScm_folder(),
						swaggerVO.getRevision());
				scmUtilImpl.pushFilesToSCMBase64(file, integrations.getScm_repository(),
						integrations.getScm_authorizationType(), rsaEncryption.decryptText(integrations.getScm_token()),
						integrations.getScm_url(), integrations.getScm_type(), integrations.getScm_branch(),
						COMMIT_MESSAGE);
			}
			headers.add("X-Swagger-Version", swaggerVO.getRevision() + "");
			headers.add("X-Swagger-id", swaggerVO.getSwaggerId());
		} else if (oas.equals("3.0")) {
			Swagger3VO swaggerVO = new Swagger3VO();
			swaggerVO.setName(swaggername);
			swaggerVO.setInteractionid(interactionid);
			swaggerVO.setSwagger(json);
			swaggerVO = swaggerBusiness.createSwaggerWithNewRevision(swaggerVO, jsessionid);
			swaggerBusiness.updateSwagger3BasePath(swaggerVO.getName(), swaggerVO); // update
			// the
			// base
			// path
			// collection
			SwaggerIntegrations integrations = swaggerBusiness.getGitIntegrations(interactionid, jsessionid,
					swaggerVO.getName(), oas);
			if (integrations != null && integrations.getScm_authorizationType().equalsIgnoreCase("basic")) {
				File file = createSwaggerFile(swaggerVO.getName(), json, integrations.getScm_folder(),
						swaggerVO.getRevision());
				scmUtilImpl.pushFilesToSCM(file, integrations.getScm_repository(),
						rsaEncryption.decryptText(integrations.getScm_username()),
						rsaEncryption.decryptText(integrations.getScm_password()), integrations.getScm_url(),
						integrations.getScm_type(), integrations.getScm_branch(), COMMIT_MESSAGE);
			} else if (integrations != null && integrations.getScm_authorizationType() != null) {
				File file = createSwaggerFile(swaggerVO.getName(), json, integrations.getScm_folder(),
						swaggerVO.getRevision());
				scmUtilImpl.pushFilesToSCMBase64(file, integrations.getScm_repository(),
						integrations.getScm_authorizationType(), rsaEncryption.decryptText(integrations.getScm_token()),
						integrations.getScm_url(), integrations.getScm_type(), integrations.getScm_branch(),
						COMMIT_MESSAGE);
			}
			headers.add("X-Swagger-Version", swaggerVO.getRevision() + "");
			headers.add("X-Swagger-id", swaggerVO.getSwaggerId());
		}
		return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	}

	/**
	 * Using this we can update the swagger version.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param json
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 * @throws Exception
	 */
	@ApiOperation(value = "Update Swagger With new Revison", notes = "")
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Swagger Updated sucessfully", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "System Error", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/swaggers/{swaggername}/revisions/{revision}")
	public ResponseEntity<Void> updateSwagger(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision, @RequestBody String json) throws Exception {
		RSAEncryption rsaEncryption = new RSAEncryption();
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		HttpHeaders headers = new HttpHeaders();
		if (oas.equals("2.0")) {
			SwaggerVO swaggerVO = new SwaggerVO();
			swaggerVO.setName(swaggername);
			swaggerVO.setInteractionid(interactionid);
			SwaggerVO vo = swaggerBusiness.findSwagger(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			String name = vo.getName();
			vo = swaggerBusiness.getSwaggerWithVersionNumber(swaggername, revision, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1001"), name, revision),
						"Swagger-1001");
			}
			vo.setSwagger(json);
			swaggerVO = swaggerBusiness.updateSwagger(vo);
			SwaggerIntegrations integrations = swaggerBusiness.getGitIntegrations(interactionid, jsessionid,
					swaggername, oas);
			if (integrations != null && integrations.getScm_authorizationType().equalsIgnoreCase("basic")) {
				File file = createSwaggerFile(swaggerVO.getName(), json, integrations.getScm_folder(),
						swaggerVO.getRevision());
				scmUtilImpl.pushFilesToSCM(file, integrations.getScm_repository(),
						rsaEncryption.decryptText(integrations.getScm_username()),
						rsaEncryption.decryptText(integrations.getScm_password()), integrations.getScm_url(),
						integrations.getScm_type(), integrations.getScm_branch(), COMMIT_MESSAGE);
			} else if (integrations != null && integrations.getScm_authorizationType() != null) {
				File file = createSwaggerFile(swaggerVO.getName(), json, integrations.getScm_folder(),
						swaggerVO.getRevision());
				scmUtilImpl.pushFilesToSCMBase64(file, integrations.getScm_repository(),
						integrations.getScm_authorizationType(), rsaEncryption.decryptText(integrations.getScm_token()),
						integrations.getScm_url(), integrations.getScm_type(), integrations.getScm_branch(),
						COMMIT_MESSAGE);
			}
			headers.add("X-Swagger-Version", swaggerVO.getRevision() + "");
		} else if (oas.equals("3.0")) {
			Swagger3VO swaggerVO = new Swagger3VO();
			swaggerVO.setName(swaggername);
			swaggerVO.setInteractionid(interactionid);
			Swagger3VO vo = swaggerBusiness.findSwagger3(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			String name = vo.getName();
			vo = swaggerBusiness.getSwagger3WithVersionNumber(swaggername, revision, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1001"), name, revision),
						"Swagger-1001");
			}
			vo.setSwagger(json);
			swaggerVO = swaggerBusiness.updateSwagger(vo);
			SwaggerIntegrations integrations = swaggerBusiness.getGitIntegrations(interactionid, jsessionid,
					swaggername, oas);
			if (integrations != null && integrations.getScm_authorizationType().equalsIgnoreCase("basic")) {
				File file = createSwaggerFile(swaggerVO.getName(), json, integrations.getScm_folder(),
						swaggerVO.getRevision());
				scmUtilImpl.pushFilesToSCM(file, integrations.getScm_repository(),
						rsaEncryption.decryptText(integrations.getScm_username()),
						rsaEncryption.decryptText(integrations.getScm_password()), integrations.getScm_url(),
						integrations.getScm_type(), integrations.getScm_branch(), COMMIT_MESSAGE);
			} else if (integrations != null && vo.getScm_authorizationType() != null) {
				File file = createSwaggerFile(swaggerVO.getName(), json, integrations.getScm_folder(),
						swaggerVO.getRevision());
				scmUtilImpl.pushFilesToSCMBase64(file, integrations.getScm_repository(),
						integrations.getScm_authorizationType(), rsaEncryption.decryptText(integrations.getScm_token()),
						integrations.getScm_url(), integrations.getScm_type(), integrations.getScm_branch(),
						COMMIT_MESSAGE);
			}
			headers.add("X-Swagger-Version", swaggerVO.getRevision() + "");
		}
		return new ResponseEntity<Void>(headers, HttpStatus.NO_CONTENT);
	}

	private File createSwaggerFile(String swaggerName, String swagger, String folder, int revision) throws IOException {
		String separatorChar = String.valueOf(File.separatorChar);
		String revStr = separatorChar + "swagger" + separatorChar + swaggerName + separatorChar
				+ String.valueOf(revision);
		folder = folder != null && !folder.isEmpty() ? folder + revStr : "Swagger" + revStr;
		String location = applicationProperties.getTempDir() + System.currentTimeMillis();
		String fileLocation = location + separatorChar + folder + separatorChar + swaggerName + ".json";
		File file = new File(fileLocation);
		file.getParentFile().mkdirs();
		file.createNewFile();
		Files.write(Paths.get(fileLocation), swagger.getBytes());
		return new File(location);
	}

	/**
	 * Using this we will get all the list of version's.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 * @throws Exception
	 */
	@ApiOperation(value = "Get Swagger Revison's", notes = "")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = Revision.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swaggername}/revisions", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<List<Revision>> getListOfRevisions(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername)
			throws Exception {
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		List<Revision> list = null;
		if (oas.equals("2.0")) {
			SwaggerVO vo = swaggerBusiness.findSwagger(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			list = swaggerBusiness.getListOfRevisions(vo.getName(), interactionid);
		} else if (oas.equals("3.0")) {
			Swagger3VO vo = swaggerBusiness.findSwagger3(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			list = swaggerBusiness.getListOf3Revisions(vo.getName(), interactionid);
		}
		return new ResponseEntity<List<Revision>>(list, HttpStatus.OK);
	}

	/**
	 * Using this we will get all the Swagger's.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	@ApiOperation(value = "Get List Of Swagger Names", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> getListOfSwaggerNames(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas) throws Exception {
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		JSONObject jsonObject = new JSONObject();
		if (oas.equals("2.0")) {
			List<SwaggerVO> swaggers = swaggerBusiness.getSwaggerNames();
			jsonObject.accumulate("swaggers", swaggers);
		} else if (oas.equals("3.0")) {
			List<Swagger3VO> swagger3s = swaggerBusiness.getSwagger3Names();
			jsonObject.accumulate("swaggers", swagger3s);
		}
		return new ResponseEntity<Object>(jsonObject.toString(), HttpStatus.OK);
	}

	/**
	 * Using this we will get the swagger name along with version and state.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return @throws IOException @throws ItorixException @throws
	 */
	@ApiOperation(value = "Get List Of Swagger Details", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = SwaggerVO.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/history", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> getListOfSwaggerDetails(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "swagger", required = false) String swagger,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "product", required = false) String product,
			@RequestParam(value = "modifieddate", required = false) String modifiedDate,
			@RequestParam(value = "sortbymodifieddate", required = false) String sortByModifiedDate) throws Exception {
		// offset = offset == null? 0: offset;
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		String json = "";
		List<String> products = null;
		if (null != product) {
			products = Arrays.asList(product.split(","));
		}
		if (oas.equals("2.0")) {
			SwaggerHistoryResponse response;
			if (null != products)
				response = swaggerBusiness.getSwaggerDetailsByproduct(products, interactionid, jsessionid, offset, oas,
						swagger, pageSize);
			else
				response = swaggerBusiness.getListOfSwaggerDetails(status, modifiedDate, interactionid, jsessionid,
						offset, oas, swagger, pageSize, sortByModifiedDate);
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			json = mapper.writeValueAsString(response);
		} else if (oas.equals("3.0")) {
			SwaggerHistoryResponse response;
			if (null != products)
				response = swaggerBusiness.getSwaggerDetailsByproduct(products, interactionid, jsessionid, offset, oas,
						swagger, pageSize);
			else
				response = swaggerBusiness.getListOfSwagger3Details(status, modifiedDate, interactionid, jsessionid,
						offset, oas, swagger, pageSize, sortByModifiedDate);
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			json = mapper.writeValueAsString(response);
		}
		return new ResponseEntity<Object>(json, HttpStatus.OK);
	}

	/**
	 * We will get when the swagger state is published.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param status
	 * @param request
	 * @param response
	 * 
	 * @return @throws IOException @throws ItorixException @throws
	 */
	@ApiOperation(value = "Get List Of Published Swagger Details", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = SwaggerDocumentationVO.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/documentation", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> getListOfPublishedSwaggerDetails(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = true, defaultValue = "2.0") String oas,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "partnerID", required = false) String partnerID) throws Exception {

		String json = "";
		ArrayNode node = swaggerBusiness.getListOfPublishedSwaggerDetails(interactionid, jsessionid, status, partnerID);
		ArrayNode node3 = swaggerBusiness.getListOfPublishedSwagger3Details(interactionid, jsessionid, status,
				partnerID);
		if (node3 != null && node3.size() > 0)
			for (JsonNode nodeElement : node3)
				node.add(nodeElement);
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		json = mapper.writeValueAsString(node);
		return new ResponseEntity<Object>(json, HttpStatus.OK);
	}

	/**
	 * We need to pass the particular swagger name and we will get the
	 * respective.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws ItorixException,Exception
	 */
	@ApiOperation(value = "Get Swagger", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = Swagger.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swaggername}", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> getSwagger(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername)
			throws Exception {
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			SwaggerVO vo = swaggerBusiness.findSwagger(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}

			SwaggerVO swaggerVO = swaggerBusiness.getSwagger(swaggername, interactionid);
			if (swaggerVO != null) {
				String swagger = swaggerVO.getSwagger();
				ObjectMapper mapper = new ObjectMapper();
				mapper.setSerializationInclusion(Include.NON_NULL);
				JsonNode json = mapper.readTree(swagger);
				HttpHeaders headers = new HttpHeaders();
				headers.add("X-Swagger-Version", swaggerVO.getRevision() + "");
				return new ResponseEntity<Object>(json, headers, HttpStatus.OK);
			} else {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
		} else if (oas.equals("3.0")) {
			Swagger3VO vo = swaggerBusiness.findSwagger3(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}

			Swagger3VO swaggerVO = swaggerBusiness.getSwagger3(swaggername, interactionid);
			if (swaggerVO != null) {
				String swagger = swaggerVO.getSwagger();
				ObjectMapper mapper = new ObjectMapper();
				mapper.setSerializationInclusion(Include.NON_NULL);
				JsonNode json = mapper.readTree(swagger);
				HttpHeaders headers = new HttpHeaders();
				headers.add("X-Swagger-Version", swaggerVO.getRevision() + "");
				return new ResponseEntity<Object>(json, headers, HttpStatus.OK);
			} else {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000"), swaggername),
						"Swagger-1000");
			}
		}
		return new ResponseEntity<Object>(null, null, HttpStatus.OK);
	}

	/**
	 * If we pass particular swagger name if it exist in the system it will
	 * delete.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws ItorixException,Exception
	 */
	@ApiOperation(value = "Delete Swagger", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Ok", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/swaggers/{swaggername}")
	public ResponseEntity<Void> deleteSwagger(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername)
			throws Exception {
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			SwaggerVO vo = swaggerBusiness.findSwagger(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			swaggerBusiness.deleteSwagger(vo.getName(), interactionid);
		} else if (oas.equals("3.0")) {
			Swagger3VO vo = swaggerBusiness.findSwagger3(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			swaggerBusiness.deleteSwagger3(vo.getName(), interactionid);
		}
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Using this method we will delete the respective revision.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	@ApiOperation(value = "delete Swagger based on Revison", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Swagger deleted sucessfully", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/swaggers/{swaggername}/revisions/{revision}")
	public ResponseEntity<Void> deleteSwaggerVersion(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision, HttpServletRequest request, HttpServletResponse response)
			throws ItorixException {
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			SwaggerVO vo = swaggerBusiness.findSwagger(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			swaggerBusiness.deleteSwaggerVersion(vo.getName(), revision, interactionid);
		} else if (oas.equals("3.0")) {
			Swagger3VO vo = swaggerBusiness.findSwagger3(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			swaggerBusiness.deleteSwagger3Version(vo.getName(), revision, interactionid);
		}
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	/**
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Get Swagger With revision", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = SwaggerVO.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swaggername}/revisions/{revision}", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> getSwaggerWithrevision(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision) throws Exception {
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			SwaggerVO vo = swaggerBusiness.findSwagger(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			SwaggerVO swaggerVO = swaggerBusiness.getSwaggerWithVersionNumber(swaggername, revision, interactionid);
			if (swaggerVO != null) {
				HttpHeaders headers = new HttpHeaders();
				headers.add("X-Swagger-Version", swaggerVO.getRevision() + "");
				String swagger = (String) swaggerVO.getSwagger();
				ObjectMapper mapper = new ObjectMapper();
				mapper.setSerializationInclusion(Include.NON_NULL);
				JsonNode actualObj = mapper.readTree(swagger);
				return new ResponseEntity<Object>(actualObj, headers, HttpStatus.OK);
			} else {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
		} else if (oas.equals("3.0")) {
			Swagger3VO vo = swaggerBusiness.findSwagger3(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			Swagger3VO swaggerVO = swaggerBusiness.getSwagger3WithVersionNumber(swaggername, revision, interactionid);
			if (swaggerVO != null) {
				HttpHeaders headers = new HttpHeaders();
				headers.add("X-Swagger-Version", swaggerVO.getRevision() + "");
				String swagger = (String) swaggerVO.getSwagger();
				ObjectMapper mapper = new ObjectMapper();
				mapper.setSerializationInclusion(Include.NON_NULL);
				JsonNode actualObj = mapper.readTree(swagger);
				return new ResponseEntity<Object>(actualObj, headers, HttpStatus.OK);
			} else {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
		}
		throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
	}

	/**
	 * Using this we can update the status of swagger.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param json
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Update Status", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Swagger Updated sucessfully", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/swaggers/{swaggername}/revisions/{revision}/status")
	public ResponseEntity<Void> updateStatus(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision, @RequestBody String json) throws Exception {
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			swaggerBusiness.updateStatus(swaggername, revision, json, interactionid, jsessionid);
		} else if (oas.equals("3.0")) {
			swaggerBusiness.updateSwagger3Status(swaggername, revision, json, interactionid, jsessionid);
		}
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<Object> getRoles(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername)
			throws Exception {
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			SwaggerVO vo = swaggerBusiness.findSwagger(swaggername, interactionid);
			if (vo == null)
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			swaggername = vo.getName();
		} else if (oas.equals("3.0")) {
			Swagger3VO vo = swaggerBusiness.findSwagger3(swaggername, interactionid);
			if (vo == null)
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			swaggername = vo.getName();
		}
		List<String> roles = swaggerBusiness.getSwaggerRoles(swaggername, oas, interactionid, jsessionid);
		return new ResponseEntity<Object>(roles, HttpStatus.OK);
	}

	/**
	 * Update the swagger comment.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param comment
	 * @param request
	 * @param response
	 */
	@ApiOperation(value = "Update Comment", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "Swagger Updated sucessfully", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/swaggers/{swaggername}/revisions/{revision}/comment")
	public ResponseEntity<Void> updateComment(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision, @RequestBody SwaggerComment comment) throws Exception {
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			comment.setSwaggerRevision(revision);
			comment.setSwaggerName(swaggername);
			comment.setInteractionid(interactionid);
			swaggerBusiness.updateComment(comment);
		} else if (oas.equals("3.0")) {
			Swagger3Comment swagger3Comment = new Swagger3Comment();
			swagger3Comment.setSwaggerRevision(revision);
			swagger3Comment.setSwaggerName(swaggername);
			swagger3Comment.setInteractionid(interactionid);
			swagger3Comment.setComment(comment.getComment());
			swaggerBusiness.updateSwagger3Comment(swagger3Comment);
		}
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	/**
	 * To get the particular swagger comments.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Get Swagger Comments", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Swagger Updated sucessfully", response = SwaggerComment.class, responseContainer = "list"),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swaggername}/revisions/{revision}/comments/history", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> getSwaggerComments(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision) throws Exception {
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			SwaggerVO vo = swaggerBusiness.findSwagger(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			String name = vo.getName();
			SwaggerVO swaggerVO = swaggerBusiness.getSwaggerWithVersionNumber(swaggername, revision, interactionid);
			if (swaggerVO == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1001"), name, revision),
						"Swagger-1001");
			}
			List<SwaggerComment> comments = swaggerBusiness.getSwaggerComments(swaggername, revision, interactionid);
			HttpHeaders headers = new HttpHeaders();
			headers.add("X-Swagger-Version", revision + "");
			return new ResponseEntity<Object>(comments, headers, HttpStatus.OK);
		} else if (oas.equals("3.0")) {
			Swagger3VO vo = swaggerBusiness.findSwagger3(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			String name = vo.getName();
			Swagger3VO swaggerVO = swaggerBusiness.getSwagger3WithVersionNumber(swaggername, revision, interactionid);
			if (swaggerVO == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1001"), name, revision),
						"Swagger-1001");
			}
			List<Swagger3Comment> comments = swaggerBusiness.getSwagger3Comments(swaggername, revision, interactionid);
			HttpHeaders headers = new HttpHeaders();
			headers.add("X-Swagger-Version", revision + "");
			return new ResponseEntity<Object>(comments, headers, HttpStatus.OK);
		}
		throw new ItorixException(String.format("invalid Swagger version specified, supported versions 2.0 and 3.0 "),
				"Swagger-1000");
	}

	/**
	 * Using this we can update the lock status.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param swaggerVO
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Update Lock Status", notes = "", code = 204)
	@ApiResponses(value = {
			@ApiResponse(code = 204, message = "Swagger Lock Updated sucessfully", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/swaggers/{swaggername}/revisions/{revision}/lockstatus")
	public ResponseEntity<Void> updateLockStatus(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision, @RequestBody SwaggerVO swaggerVO) throws Exception {
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			SwaggerVO vo = swaggerBusiness.findSwagger(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			String name = vo.getName();
			vo = swaggerBusiness.getSwaggerWithVersionNumber(vo.getName(), revision, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1001"), name, revision),
						"Swagger-1001");
			}
			swaggerVO.setRevision(revision);
			swaggerVO.setName(vo.getName());
			swaggerVO.setInteractionid(interactionid);
			swaggerBusiness.updateLockStatus(swaggerVO, jsessionid);
		} else if (oas.equals("3.0")) {
			Swagger3VO vo = swaggerBusiness.findSwagger3(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			String name = vo.getName();
			vo = swaggerBusiness.getSwagger3WithVersionNumber(swaggername, revision, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1001"), name, revision),
						"Swagger-1001");
			}
			Swagger3VO swagger3VO = new Swagger3VO();
			swagger3VO.setRevision(revision);
			swagger3VO.setName(vo.getName());
			swagger3VO.setLock(swaggerVO.getLock());
			swagger3VO.setInteractionid(interactionid);
			swaggerBusiness.updateSwagger3LockStatus(swagger3VO, jsessionid);
		}
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Using this we can deprecate the particular swagger.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param swaggerVO
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Deprecate Swagger", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Swagger Updated sucessfully", response = SwaggerVO.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/swaggers/{swaggername}/revisions/{revision}/deprecate")
	public ResponseEntity<Object> deprecate(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision, @RequestBody SwaggerVO swaggerVO) throws Exception {
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			SwaggerVO vo = swaggerBusiness.findSwagger(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			String name = vo.getName();
			vo = swaggerBusiness.getSwaggerWithVersionNumber(swaggername, revision, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1001"), name, revision),
						"Swagger-1001");
			}
			swaggerVO.setRevision(revision);
			swaggerVO.setName(swaggername);
			swaggerVO.setInteractionid(interactionid);
			swaggerVO = swaggerBusiness.deprecate(swaggerVO);
			return new ResponseEntity<Object>(swaggerVO, HttpStatus.OK);
		} else if (oas.equals("3.0")) {
			Swagger3VO vo = swaggerBusiness.findSwagger3(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			String name = vo.getName();
			vo = swaggerBusiness.getSwagger3WithVersionNumber(swaggername, revision, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1001"), name, revision),
						"Swagger-1001");
			}
			Swagger3VO swagger3VO = new Swagger3VO();
			swagger3VO.setRevision(revision);
			swagger3VO.setName(swaggername);
			swagger3VO.setInteractionid(interactionid);
			swagger3VO.setLock(swaggerVO.getLock());
			swagger3VO = swaggerBusiness.deprecate(swagger3VO);
			return new ResponseEntity<Object>(swagger3VO, HttpStatus.OK);
		}
		throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1001"), swaggername, revision),
				"Swagger-1001");
	}

	/**
	 * Using this we can update the proxies for that swagger.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Update Proxies", notes = "", code = 204)
	@ApiResponses(value = {
			@ApiResponse(code = 204, message = "Swagger Proxies Updated sucessfully", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/swaggers/{swaggername}/revisions/{revision}/proxies")
	public ResponseEntity<Void> updateProxies(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision) throws Exception {
		SwaggerVO vo = swaggerBusiness.findSwagger(swaggername, interactionid);
		if (vo == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
		}
		String name = vo.getName();
		vo = swaggerBusiness.getSwaggerWithVersionNumber(swaggername, revision, interactionid);
		if (vo == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1001"), name, revision),
					"Swagger-1001");
		}
		SwaggerVO swaggerVO = new SwaggerVO();
		swaggerVO.setRevision(revision);
		swaggerVO.setName(swaggername);
		swaggerVO.setInteractionid(interactionid);
		swaggerBusiness.updateProxies(swaggerVO);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Using this we will get the lock status of that swagger.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Get LockStatus", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = Boolean.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swaggername}/revisions/{revision}/lockstatus", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> getLockStatus(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision) throws Exception {
		SwaggerLockResponse lockResponse = new SwaggerLockResponse();
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			SwaggerVO vo = swaggerBusiness.findSwagger(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			vo = swaggerBusiness.getSwaggerWithVersionNumber(swaggername, revision, interactionid);
			Boolean lockStatus = vo.getLock(); // swaggerBusiness.getLockStatus(vo.getName(),
			// revision, interactionid);
			if (lockStatus != null) {
				lockResponse.setLockStatus(lockStatus);
				if (lockStatus) {
					lockResponse.setLockedBy(vo.getLockedBy());
					lockResponse.setLockedAt(vo.getLockedAt());
					lockResponse.setLockedByUserId(vo.getLockedByUserId());
				}
				HttpHeaders headers = new HttpHeaders();
				headers.add("X-Swagger-Version", revision + "");
				return new ResponseEntity<Object>(lockResponse, headers, HttpStatus.OK);
			} else {
				throw new ItorixException(
						String.format(ErrorCodes.errorMessage.get("Swagger-1001"), swaggername, revision),
						"Swagger-1001");
			}
		} else if (oas.equals("3.0")) {
			Swagger3VO vo = swaggerBusiness.findSwagger3(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			String name = vo.getName();
			vo = swaggerBusiness.getSwagger3WithVersionNumber(swaggername, revision, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1001"), name, revision),
						"Swagger-1001");
			}
			Boolean lockStatus = swaggerBusiness.getSwagger3LockStatus(vo.getName(), revision, interactionid);
			// Boolean lockStatus =
			// vo.getLock();//swaggerBusiness.getLockStatus(vo.getName(),
			// revision,
			// interactionid);
			if (lockStatus != null) {
				lockResponse.setLockStatus(lockStatus);
				if (lockStatus) {
					lockResponse.setLockedBy(vo.getLockedBy());
					lockResponse.setLockedAt(vo.getLockedAt());
					lockResponse.setLockedByUserId(vo.getLockedByUserId());
				}
				HttpHeaders headers = new HttpHeaders();
				headers.add("X-Swagger-Version", revision + "");
				return new ResponseEntity<Object>(lockResponse, headers, HttpStatus.OK);
			} else {
				throw new ItorixException(
						String.format(ErrorCodes.errorMessage.get("Swagger-1001"), swaggername, revision),
						"Swagger-1001");
			}
		}
		throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1001"), swaggername, revision),
				"Swagger-1001");
	}

	/**
	 * Using this we will generate the xpath.
	 *
	 * @param interactionid
	 * @param xsdFile
	 * @param elementName
	 * @param type
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	@ApiOperation(value = "Genarate Xpath", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = XmlSchemaVo.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/genaratexpath", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> genarateXpath(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam("xsdfile") MultipartFile xsdFile, @RequestParam("elementname") String elementName,
			@RequestParam("type") String type, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception {
		JSONObject jsonObject = new JSONObject();
		if (xsdFile == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Swagger-1002"), "Swagger-1002");
		}
		if (elementName == null || elementName.trim().length() <= 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Swagger-1003"), "Swagger-1003");
		}
		String fileLocation = swaggerBusiness.genarateXpath(xsdFile, elementName, interactionid);
		ColumnPositionMappingStrategy<XmlSchemaVo> strategy = new ColumnPositionMappingStrategy<>();
		strategy.setType(XmlSchemaVo.class);
		CsvToBean<XmlSchemaVo> csvToBean = new CsvToBean<>();
		String[] columns = new String[]{"include", "xpath", "minOccurs", "maxOccurs", "xsdType", "jsonType",
				"jsonFormat", "enums", "minLength", "maxLength", "length", "pattern", "documentation"};
		strategy.setColumnMapping(columns);
		CSVReader csvReader = null;
		try {
			csvReader = new CSVReader(new FileReader(fileLocation), '|');
		} catch (Exception ex) {
			logger.error("SwaggerController.genarateXpath : CorelationId= " + interactionid + " : jsessionid="
					+ jsessionid + " : ERROR =" + ex.getMessage());
			throw new ItorixException(String.format(ErrorCodes.Xpath1001.message(), elementName), "Xpath-1001");
		}
		List<XmlSchemaVo> xmlSchemaVos = csvToBean.parse(strategy, csvReader);
		if (type.equals("XLS")) {
			jsonObject = xlsUtil.writeExcel(xmlSchemaVos, elementName);
			return new ResponseEntity<Object>(jsonObject.toString(), HttpStatus.OK);
		} else {
			return new ResponseEntity<Object>(xmlSchemaVos, HttpStatus.OK);
		}
	}

	/**
	 * Using this we will get the swagger definitions.
	 *
	 * @param interactionid
	 * @param xpathFile
	 * @param sheetName
	 * @param swaggername
	 * @param revision
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	@ApiOperation(value = "GenarateSwaggerDefinations", notes = "", code = 201)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Created", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/{swaggername}/definitions", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Void> genarateSwaggerDefinations(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam("xpathfile") MultipartFile xpathFile, @RequestParam("sheetname") String sheetName,
			@PathVariable("swaggername") String swaggername, @RequestParam("revision") Integer revision,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		if (xpathFile == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Swagger-1004"), "Swagger-1004");
		}
		if (sheetName == null || sheetName.trim().length() <= 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Swagger-1005"), "Swagger-1005");
		}
		swaggerBusiness.getSwagger(swaggername, interactionid);
		SwaggerVO swaggerVO = swaggerBusiness.getSwagger(swaggername, interactionid);
		swaggerVO.setInteractionid(interactionid);
		SwaggerVO swagger = swaggerBusiness.genarateSwaggerDefinations(swaggerVO, xpathFile, sheetName, revision);
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Swagger-Version", swagger.getRevision() + "");
		return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	}

	/**
	 * To get the swagger Json definitions.
	 *
	 * @param interactionid
	 * @param json
	 * @param swaggername
	 * @param revision
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	@ApiOperation(value = "Genarate Swagger Json Definations", notes = "", code = 201)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Created", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/{swaggername}/jsondefinitions", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Void> genarateSwaggerJsonDefinations(
			@RequestHeader(value = "interactionid", required = false) String interactionid, @RequestBody String json,
			@PathVariable("swaggername") String swaggername, @RequestParam("revision") Integer revision,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas) throws Exception {
		int swaggerRevision = 0;
		ObjectMapper mapper = new ObjectMapper();
		List<RowData> rowDataList = mapper.readValue(json, new TypeReference<List<RowData>>() {
		});
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			SwaggerVO swaggerVO = new SwaggerVO();
			swaggerVO.setName(swaggername);
			swaggerVO.setInteractionid(interactionid);
			SwaggerVO swagger = swaggerBusiness.genarateSwaggerJsonDefinations(swaggerVO, rowDataList, revision);
			swaggerRevision = swagger.getRevision();
		} else if (oas.equals("3.0")) {
			Swagger3VO swaggerVO = new Swagger3VO();
			swaggerVO.setName(swaggername);
			swaggerVO.setInteractionid(interactionid);
			Swagger3VO swagger = swaggerBusiness.genarateSwaggerJsonDefinations(swaggerVO, rowDataList, revision);
			swaggerRevision = swagger.getRevision();
		}

		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Swagger-Version", swaggerRevision + "");
		return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	}

	/**
	 * Using this we can create the review.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param swaggerReview
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Create Review", notes = "", code = 201)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Created", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/{swaggername}/revisions/{revision}/review")
	public ResponseEntity<Void> createReview(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision, @RequestBody SwaggerReview swaggerReview) throws Exception {
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			swaggerReview.setInteractionid(interactionid);
			swaggerReview.setJsessionid(jsessionid);
			SwaggerVO vo = swaggerBusiness.findSwagger(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			swaggername = vo.getName();
			vo = swaggerBusiness.getSwaggerWithVersionNumber(swaggername, revision, interactionid);
			if (vo == null) {
				throw new ItorixException(
						String.format(
								String.format(ErrorCodes.errorMessage.get("Swagger-1001"), swaggername, revision)),
						"Swagger-1001");
			}
			swaggerReview.setSwaggerName(swaggername);
			swaggerReview.setRevision(revision);
			swaggerBusiness.createReview(swaggerReview);
		}
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	/**
	 * Using this we can write the review comments.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggerReviewComments
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Create Review Comment", notes = "", code = 201)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Created", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/reviews")
	public ResponseEntity<Void> createReviewComment(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas,
			@RequestBody SwaggerReviewComents swaggerReviewComments) throws Exception {

		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			swaggerReviewComments.setInteractionid(interactionid);
			swaggerReviewComments.setJsessionid(jsessionid);
			SwaggerVO vo = swaggerBusiness.findSwagger(swaggerReviewComments.getSwaggerName(), interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			swaggerReviewComments.setSwaggerName(vo.getName());
			String name = vo.getName();
			vo = swaggerBusiness.getSwaggerWithVersionNumber(swaggerReviewComments.getSwaggerName(),
					swaggerReviewComments.getRevision(), interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(String.format(ErrorCodes.errorMessage.get("Swagger-1001"), name,
						swaggerReviewComments.getRevision())), "Swagger-1001");
			}
			swaggerBusiness.createOrUpdateReviewComment(swaggerReviewComments);
		} else if (oas.equals("3.0")) {
			Swagger3ReviewComents swagger3ReviewComments = new Swagger3ReviewComents();
			swagger3ReviewComments.setComment(swaggerReviewComments.getComment());
			swagger3ReviewComments.setExternalFlag(swaggerReviewComments.getExternalFlag());
			swagger3ReviewComments.setUserName(swaggerReviewComments.getUserName());
			swagger3ReviewComments.setUserEmailId(swaggerReviewComments.getUserEmailId());
			swagger3ReviewComments.setSwaggerName(swaggerReviewComments.getSwaggerName());
			swagger3ReviewComments.setRevision(swaggerReviewComments.getRevision());
			swagger3ReviewComments.setCommentedBy(swaggerReviewComments.getCommentedBy());
			swagger3ReviewComments.setInteractionid(interactionid);
			swagger3ReviewComments.setJsessionid(jsessionid);
			Swagger3VO vo = swaggerBusiness.findSwagger3(swagger3ReviewComments.getSwaggerName(), interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			swaggerReviewComments.setSwaggerName(vo.getName());
			swagger3ReviewComments.setSwaggerName(swaggerReviewComments.getSwaggerName());
			vo = swaggerBusiness.getSwagger3WithVersionNumber(swaggerReviewComments.getSwaggerName(),
					swaggerReviewComments.getRevision(), interactionid);
			if (vo == null) {
				throw new ItorixException(
						String.format(String.format(ErrorCodes.errorMessage.get("Swagger-1001"),
								swaggerReviewComments.getSwaggerName(), swaggerReviewComments.getRevision())),
						"Swagger-1001");
			}
			swaggerBusiness.createOrUpdateReviewComment(swagger3ReviewComments);
		}

		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	/**
	 * We can update the review comments.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggerReviewComments
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Update Review Comment", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content", response = Void.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/swaggers/reviews")
	public ResponseEntity<Void> updateReviewComment(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas,
			@RequestBody SwaggerReviewComents swaggerReviewComments) throws Exception {
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			swaggerReviewComments.setInteractionid(interactionid);
			swaggerReviewComments.setJsessionid(jsessionid);
			SwaggerVO vo = swaggerBusiness.findSwagger(swaggerReviewComments.getSwaggerName(), interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			swaggerReviewComments.setSwaggerName(vo.getName());
			vo = swaggerBusiness.getSwaggerWithVersionNumber(swaggerReviewComments.getSwaggerName(),
					swaggerReviewComments.getRevision(), interactionid);
			if (vo == null) {
				throw new ItorixException(
						String.format(String.format(ErrorCodes.errorMessage.get("Swagger-1001"),
								swaggerReviewComments.getSwaggerName(), swaggerReviewComments.getRevision())),
						"Swagger-1001");
			}
			swaggerBusiness.updateReviewComment(swaggerReviewComments);
		} else if (oas.equals("3.0")) {
			Swagger3ReviewComents swagger3ReviewComments = new Swagger3ReviewComents();
			swagger3ReviewComments.setId(swaggerReviewComments.getId());
			swagger3ReviewComments.setCommentId(swaggerReviewComments.getCommentId());
			swagger3ReviewComments.setComment(swaggerReviewComments.getComment());
			swagger3ReviewComments.setExternalFlag(swaggerReviewComments.getExternalFlag());
			swagger3ReviewComments.setUserName(swaggerReviewComments.getUserName());
			swagger3ReviewComments.setUserEmailId(swaggerReviewComments.getUserEmailId());
			swagger3ReviewComments.setSwaggerName(swaggerReviewComments.getSwaggerName());
			swagger3ReviewComments.setRevision(swaggerReviewComments.getRevision());
			swagger3ReviewComments.setCommentedBy(swaggerReviewComments.getCommentedBy());
			swagger3ReviewComments.setInteractionid(interactionid);
			swagger3ReviewComments.setJsessionid(jsessionid);
			Swagger3VO vo = swaggerBusiness.findSwagger3(swagger3ReviewComments.getSwaggerName(), interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			swaggerReviewComments.setSwaggerName(vo.getName());
			swagger3ReviewComments.setSwaggerName(vo.getName());
			vo = swaggerBusiness.getSwagger3WithVersionNumber(swaggerReviewComments.getSwaggerName(),
					swaggerReviewComments.getRevision(), interactionid);
			if (vo == null) {
				throw new ItorixException(
						String.format(String.format(ErrorCodes.errorMessage.get("Swagger-1001"),
								swaggerReviewComments.getSwaggerName(), swaggerReviewComments.getRevision())),
						"Swagger-1001");
			}
			swaggerBusiness.updateReviewComment(swagger3ReviewComments);
		}

		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Using this we can replay the review comment.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param reviewid
	 * @param swaggerReviewComments
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Review Comment Replay", notes = "", code = 201)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Created", response = Void.class),
			@ApiResponse(code = 404, message = "No records found for selected review id - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/reviews/{reviewid}/comment")
	public ResponseEntity<Void> reviewCommentReplay(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("reviewid") String reviewid,
			@RequestBody SwaggerReviewComents swaggerReviewComments) throws Exception {
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			swaggerReviewComments.setInteractionid(interactionid);
			swaggerReviewComments.setJsessionid(jsessionid);
			SwaggerVO vo = swaggerBusiness.findSwagger(swaggerReviewComments.getSwaggerName(), interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			swaggerReviewComments.setSwaggerName(vo.getName());
			vo = swaggerBusiness.getSwaggerWithVersionNumber(swaggerReviewComments.getSwaggerName(),
					swaggerReviewComments.getRevision(), interactionid);
			if (vo == null) {
				throw new ItorixException(
						String.format(String.format(ErrorCodes.errorMessage.get("Swagger-1001"),
								swaggerReviewComments.getSwaggerName(), swaggerReviewComments.getRevision())),
						"Swagger-1001");
			}
			swaggerReviewComments.setCommentId(reviewid);
			swaggerBusiness.createOrUpdateReviewComment(swaggerReviewComments);
		} else if (oas.equals("3.0")) {
			Swagger3ReviewComents swagger3ReviewComments = new Swagger3ReviewComents();
			swagger3ReviewComments.setCommentId(reviewid);
			swagger3ReviewComments.setComment(swaggerReviewComments.getComment());
			swagger3ReviewComments.setExternalFlag(swaggerReviewComments.getExternalFlag());
			swagger3ReviewComments.setUserName(swaggerReviewComments.getUserName());
			swagger3ReviewComments.setUserEmailId(swaggerReviewComments.getUserEmailId());
			swagger3ReviewComments.setSwaggerName(swaggerReviewComments.getSwaggerName());
			swagger3ReviewComments.setRevision(swaggerReviewComments.getRevision());
			swagger3ReviewComments.setCommentedBy(swaggerReviewComments.getCommentedBy());
			swagger3ReviewComments.setInteractionid(interactionid);
			swagger3ReviewComments.setJsessionid(jsessionid);
			Swagger3VO vo = swaggerBusiness.findSwagger3(swagger3ReviewComments.getSwaggerName(), interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			swaggerReviewComments.setSwaggerName(vo.getName());
			swagger3ReviewComments.setSwaggerName(swaggerReviewComments.getSwaggerName());
			vo = swaggerBusiness.getSwagger3WithVersionNumber(swaggerReviewComments.getSwaggerName(),
					swaggerReviewComments.getRevision(), interactionid);
			if (vo == null) {
				throw new ItorixException(
						String.format(String.format(ErrorCodes.errorMessage.get("Swagger-1001"),
								swaggerReviewComments.getSwaggerName(), swaggerReviewComments.getRevision())),
						"Swagger-1001");
			}
			swaggerBusiness.createOrUpdateReviewComment(swagger3ReviewComments);
		}

		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	/**
	 * To get the review comment.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param versionnumber
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Update Swagger With new Revison", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = SwaggerReviewComents.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 404, message = "Resource not found. Resource not found. No records found for selected swagger name - %s with following revision - %s.", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swaggername}/revisions/{revision}/reviews")
	public ResponseEntity<Object> getReviewComment(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@PathVariable("revision") Integer revision) throws Exception {
		ObjectNode obj = null;
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			SwaggerReviewComents swaggerReviewComments = new SwaggerReviewComents();
			swaggerReviewComments.setInteractionid(interactionid);
			swaggerReviewComments.setJsessionid(jsessionid);
			swaggerReviewComments.setSwaggerName(swaggername);
			swaggerReviewComments.setRevision(revision);
			obj = swaggerBusiness.getReviewComment(swaggerReviewComments);
		} else if (oas.equals("3.0")) {
			Swagger3ReviewComents swaggerReviewComments = new Swagger3ReviewComents();
			swaggerReviewComments.setInteractionid(interactionid);
			swaggerReviewComments.setJsessionid(jsessionid);
			swaggerReviewComments.setSwaggerName(swaggername);
			swaggerReviewComments.setRevision(revision);
			obj = swaggerBusiness.getReviewComment(swaggerReviewComments);
		}
		return new ResponseEntity<Object>(obj, HttpStatus.OK);
	}

	@ApiOperation(value = "Get Swagger Revison's", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = SwaggerVO.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/stats")
	public ResponseEntity<Object> getSwaggerStats(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas,
			@RequestParam(value = "timeunit", required = false) String timeunit,
			@RequestParam(value = "timerange", required = false) String timerange) throws Exception {
		ObjectNode obj = null;
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			obj = swaggerBusiness.getSwaggerStats(timeunit, timerange);
		} else if (oas.equals("3.0")) {
			obj = swaggerBusiness.getSwagger3Stats(timeunit, timerange);
		}
		return new ResponseEntity<Object>(obj, HttpStatus.OK);
	}

	@ApiOperation(value = "Get Team Stats", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = SwaggerVO.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/teams/stats")
	public ResponseEntity<Object> getTeamStats(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "timeunit", required = false) String timeunit,
			@RequestParam(value = "timerange", required = false) String timerange) throws Exception {
		ObjectNode obj = swaggerBusiness.getTeamStats(timeunit, timerange);
		return new ResponseEntity<Object>(obj, HttpStatus.OK);
	}

	@UnSecure
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swagger-gen/clients/servers")
	public @ResponseBody ResponseEntity<Object> getClientsServers(
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid) throws Exception {
		Map clientsServer = new HashMap();
		clientsServer.put("clients", Clients.values());
		clientsServer.put("servers", Servers.values());
		return new ResponseEntity<Object>(clientsServer, HttpStatus.OK);
	}

	/**
	 * Using this we will get the swagger name along with version and state.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Genrate client", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = SwaggerVO.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swagger-gen/clients/{framework}", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> genrateClient(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("framework") String framework,
			@RequestBody GenrateClientRequest genrateClientRequest) throws Exception {
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			if (!clients.contains(framework)) {
				throw new ItorixException(
						String.format(ErrorCodes.errorMessage.get("Swagger-1001"),
								genrateClientRequest.getSwaggerName(), genrateClientRequest.getRevision()),
						"Swagger-1001");
			}
			SwaggerVO vo = null;
			vo = swaggerBusiness.getSwaggerWithVersionNumber(genrateClientRequest.getSwaggerName(),
					genrateClientRequest.getRevision(), interactionid);
			if (vo == null) {
				throw new ItorixException(
						String.format(ErrorCodes.errorMessage.get("Swagger-1001"),
								genrateClientRequest.getSwaggerName(), genrateClientRequest.getRevision()),
						"Swagger-1001");
			}
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			JsonNode json = mapper.readTree(vo.getSwagger());
			GeneratorInput generatorInput = new GeneratorInput();
			generatorInput.setSpec(json);
			Map<String, String> options = new HashMap<>();
			String outputFolder = applicationProperties.getSwageerGenDir() + "clients" + File.separator + framework
					+ File.separator + genrateClientRequest.getSwaggerName() + "_client";
			options.put("outputFolder", outputFolder);
			generatorInput.setOptions(options);
			String filename = Generator.generateClient(framework, generatorInput);
			String downloadURI = null;
			try {
				S3Integration s3Integration = s3Connection.getS3Integration();
				if (null != s3Integration) {
					File file = new File(filename);
					downloadURI = s3Utils.uplaodFile(s3Integration.getKey(), s3Integration.getDecryptedSecret(),
							Regions.fromName(s3Integration.getRegion()), s3Integration.getBucketName(),
							"swaggerClients/" + framework + "/" + System.currentTimeMillis() + "/" + file.getName(),
							filename);
				} else {
					org.json.JSONObject obj = null;
					obj = jfrogUtilImpl.uploadFiles(filename,
							"/" + getWorkspaceId() + "/swaggerClients/" + framework + "/" + System.currentTimeMillis());
					downloadURI = obj.getString("downloadURI");
					new File(filename).delete();
				}
			} catch (Exception e) {

			}
			ResponseCode responseCode = new ResponseCode();
			responseCode.setLink(downloadURI);
			return new ResponseEntity<Object>(responseCode, HttpStatus.OK);
		} else if (oas.equals("3.0")) {
			Swagger3VO vo = null;
			vo = swaggerBusiness.getSwagger3WithVersionNumber(genrateClientRequest.getSwaggerName(),
					genrateClientRequest.getRevision(), interactionid);
			if (vo == null) {
				throw new ItorixException(
						String.format(ErrorCodes.errorMessage.get("Swagger-1001"),
								genrateClientRequest.getSwaggerName(), genrateClientRequest.getRevision()),
						"Swagger-1001");
			}
			String filename = generateSwagger3SDK(vo, framework);
			String downloadURI = null;
			try {
				S3Integration s3Integration = s3Connection.getS3Integration();
				if (null != s3Integration) {
					File file = new File(filename);
					downloadURI = s3Utils.uplaodFile(s3Integration.getKey(), s3Integration.getDecryptedSecret(),
							Regions.fromName(s3Integration.getRegion()), s3Integration.getBucketName(),
							"swaggerClients/" + framework + "/" + System.currentTimeMillis() + "/" + file.getName(),
							filename);
				} else {
					org.json.JSONObject obj = null;
					obj = jfrogUtilImpl.uploadFiles(filename,
							"/" + getWorkspaceId() + "/swaggerClients/" + framework + "/" + System.currentTimeMillis());
					downloadURI = obj.getString("downloadURI");
					new File(filename).delete();
				}
			} catch (Exception e) {

			}
			ResponseCode responseCode = new ResponseCode();
			responseCode.setLink(downloadURI);
			return new ResponseEntity<Object>(responseCode, HttpStatus.OK);
		}
		return new ResponseEntity<Object>("", HttpStatus.OK);
	}

	private String getWorkspaceId() {
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		return userSessionToken.getWorkspaceId();
	}

	private String generateSwagger3SDK(Swagger3VO swaggerVO, String framework) throws IOException, ApiException {
		String outputFolder = applicationProperties.getSwageerGenDir() + "clients" + File.separator + framework
				+ File.separator + swaggerVO.getName().replaceAll(" ", "") + "_client";
		String openapiLocation = applicationProperties.getSwageerGenDir() + "swagger" + File.separator
				+ swaggerVO.getName().replaceAll(" ", "") + ".json";
		FileUtils.writeStringToFile(new File(openapiLocation), swaggerVO.getSwagger());
		String name = Swagger3SDK.generate(framework, openapiLocation, outputFolder);
		FileUtils.deleteQuietly(new File(openapiLocation));
		return name;
	}

	/**
	 * Using this we will get the swagger name along with version and state.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
	 * 
	 * @return
	 */
	@ApiOperation(value = "Genrate client", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = SwaggerVO.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swagger-gen/servers/{framework}", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<ResponseCode> genrateServer(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("framework") String framework,
			@RequestBody GenrateClientRequest genrateClientRequest) throws Exception {
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			if (!servers.contains(framework)) {
				throw new ItorixException(
						String.format(ErrorCodes.errorMessage.get("Swagger-1001"),
								genrateClientRequest.getSwaggerName(), genrateClientRequest.getRevision()),
						"Swagger-1001");
			}
			SwaggerVO vo = null;
			vo = swaggerBusiness.getSwaggerWithVersionNumber(genrateClientRequest.getSwaggerName(),
					genrateClientRequest.getRevision(), interactionid);
			if (vo == null) {
				throw new ItorixException(
						String.format(ErrorCodes.errorMessage.get("Swagger-1001"),
								genrateClientRequest.getSwaggerName(), genrateClientRequest.getRevision()),
						"Swagger-1001");
			}
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			JsonNode json = mapper.readTree(vo.getSwagger());
			GeneratorInput generatorInput = new GeneratorInput();
			generatorInput.setSpec(json);
			Map<String, String> options = new HashMap<>();
			String outputFolder = applicationProperties.getSwageerGenDir() + "servers" + File.separator + framework
					+ File.separator + genrateClientRequest.getSwaggerName() + "_server";
			options.put("outputFolder", outputFolder);
			generatorInput.setOptions(options);
			String filename = Generator.generateServer(framework, generatorInput);
			String downloadURI = null;
			try {
				S3Integration s3Integration = s3Connection.getS3Integration();
				if (null != s3Integration) {
					File file = new File(filename);
					downloadURI = s3Utils.uplaodFile(s3Integration.getKey(), s3Integration.getDecryptedSecret(),
							Regions.fromName(s3Integration.getRegion()), s3Integration.getBucketName(),
							"swaggerClients/" + framework + "/" + System.currentTimeMillis() + "/" + file.getName(),
							filename);
				} else {
					org.json.JSONObject obj = null;
					obj = jfrogUtilImpl.uploadFiles(filename,
							"/" + getWorkspaceId() + "/swaggerClients/" + framework + "/" + System.currentTimeMillis());
					downloadURI = obj.getString("downloadURI");
					new File(filename).delete();
				}
			} catch (Exception e) {

			}
			new File(filename).delete();
			ResponseCode responseCode = new ResponseCode();
			responseCode.setLink(downloadURI);
			return new ResponseEntity<ResponseCode>(responseCode, HttpStatus.OK);
		} else if (oas.equals("3.0")) {
			Swagger3VO vo = null;
			vo = swaggerBusiness.getSwagger3WithVersionNumber(genrateClientRequest.getSwaggerName(),
					genrateClientRequest.getRevision(), interactionid);
			if (vo == null) {
				throw new ItorixException(
						String.format(ErrorCodes.errorMessage.get("Swagger-1001"),
								genrateClientRequest.getSwaggerName(), genrateClientRequest.getRevision()),
						"Swagger-1001");
			}
			String filename = generateSwagger3SDK(vo, framework);
			org.json.JSONObject obj = null;
			obj = jfrogUtilImpl.uploadFiles(filename,
					"/" + getWorkspaceId() + "/" + framework + "/" + System.currentTimeMillis());
			new File(filename).delete();
			ResponseCode responseCode = new ResponseCode();
			responseCode.setLink(obj.getString("downloadURI"));
			return new ResponseEntity<ResponseCode>(responseCode, HttpStatus.OK);
		}
		return new ResponseEntity<ResponseCode>(new ResponseCode(), HttpStatus.OK);
	}

	/**
	 * Using this we can associate the swagger with team.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param swaggerVO
	 * @param request
	 * @param response
	 * 
	 * @throws ItorixException,Exception
	 */
	@ApiOperation(value = "Assoiate Product", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/{swaggername}/associate-products")
	public ResponseEntity<Void> assoiateProduct(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername,
			@RequestBody SwaggerVO swaggerVO) throws Exception {
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			SwaggerVO vo = swaggerBusiness.findSwagger(swaggername, interactionid);
			if (vo != null) {
				Set<String> products = swaggerVO.getProducts();
				swaggerBusiness.associateProduct(vo.getName(), products, "2.0");
			} else {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
		} else if (oas.equals("3.0")) {
			Swagger3VO vo = swaggerBusiness.findSwagger3(swaggername, interactionid);
			if (vo != null) {
				Set<String> products = swaggerVO.getProducts();
				swaggerBusiness.associateProduct(vo.getName(), products, "3.0");
			} else {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
		}
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	/**
	 * To get the assoiated teams.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws ItorixException,Exception
	 */
	@ApiOperation(value = "Get Assoiated Products", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swaggername}/associated-products")
	public ResponseEntity<Set<String>> getAssoiatedProducts(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername)
			throws Exception {
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			SwaggerVO vo = null;
			vo = swaggerBusiness.findSwagger(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			SwaggerMetadata metadata = swaggerBusiness.getSwaggerMetadata(vo.getName(), oas);
			Set<String> responseSet = responseSet = (metadata != null) ? metadata.getProducts() : new HashSet<String>();
			return new ResponseEntity<Set<String>>(responseSet, HttpStatus.OK);
		} else if (oas.equals("3.0")) {
			Swagger3VO vo = null;
			vo = swaggerBusiness.findSwagger3(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			SwaggerMetadata metadata = swaggerBusiness.getSwaggerMetadata(vo.getName(), oas);
			Set<String> responseSet = responseSet = (metadata != null) ? metadata.getProducts() : new HashSet<String>();
			return new ResponseEntity<Set<String>>(responseSet, HttpStatus.OK);
		}
		return new ResponseEntity<Set<String>>(new HashSet<String>(), HttpStatus.OK);
	}

	/**
	 * Using this we can associate the swagger with team.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param swaggerVO
	 * @param request
	 * @param response
	 */
	@ApiOperation(value = "Assoiate Projects", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/teams/{teamname}/associate-projects")
	public ResponseEntity<Void> assoiateTeamsToProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("teamname") String teamname,
			@RequestBody SwaggerTeam swaggerTeam) throws Exception {
		Set<String> projects = swaggerTeam.getProjects();
		swaggerBusiness.assoiateTeamsToProject(teamname, projects, interactionid);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	/**
	 * To get the assoiated teams.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws ItorixException,Exception
	 */
	@ApiOperation(value = "Get Assoiated Projects", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/teams/{teamname}/associated-projects")
	public ResponseEntity<Set<String>> getassoiateTeamsToProjects(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("teamname") String teamname)
			throws Exception {
		Set<String> responseSet = new HashSet<>();
		SwaggerTeam vo = swaggerBusiness.findSwaggerTeam(teamname, interactionid);
		if (vo == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Teams-1001"), teamname), "Teams-1001");
		}
		responseSet = vo.getProjects();
		return new ResponseEntity<Set<String>>(responseSet, HttpStatus.OK);
	}

	/**
	 * Using this we can associate the swagger with Portfolio.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param swaggerVO
	 * @param request
	 * @param response
	 * 
	 * @throws ItorixException,Exception
	 */
	@ApiOperation(value = "Assoiate Product", notes = "", code = 204)
	@ApiResponses(value = {@ApiResponse(code = 204, message = "No Content"),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/{swaggername}/associated-portfolio")
	public ResponseEntity<Void> assoiatePortfolio(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("swaggername") String swaggername,
			@RequestBody SwaggerVO swaggerVO) throws Exception {
		Set<String> portfolioSet = swaggerVO.getPortfolios();
		swaggerBusiness.associatePortfolio(swaggername, portfolioSet, interactionid);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	/**
	 * To get the assoiated Portfolios.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param request
	 * @param response
	 * 
	 * @return
	 * 
	 * @throws ItorixException,Exception
	 */
	@ApiOperation(value = "Get Assoiated Portfolios", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "Resource not found. No records found for selected swagger name - %s", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swaggername}/associated-portfolio")
	public ResponseEntity<Set<String>> getAssoiatedPortfolios(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("swaggername") String swaggername)
			throws Exception {
		Set<String> responseSet = new HashSet<>();
		SwaggerVO vo = null;
		vo = swaggerBusiness.findSwagger(swaggername, interactionid);
		if (vo == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
		}
		responseSet = vo.getPortfolios();
		return new ResponseEntity<Set<String>>(responseSet, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "v1/swaggers/search", produces = {"application/json"})
	public ResponseEntity<Object> swaggerSearch(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam("name") String name,
			@RequestHeader(value = "oas", required = false) String oas, @RequestParam("limit") int limit)
			throws ItorixException, JsonProcessingException {
		Object response = null;
		if (oas == null || oas.trim().equals(""))
			oas = "2.0";
		if (oas.equals("2.0")) {
			response = swaggerBusiness.swaggerSearch(interactionid, name, limit);
		} else if (oas.equals("3.0")) {
			response = swaggerBusiness.swagger3Search(interactionid, name, limit);
		}
		return new ResponseEntity<Object>(response, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/validate")
	public ResponseEntity<?> validateSwagger(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @RequestBody String swaggerStr)
			throws Exception {
		ValidationResponse output = new ValidateSchema().debugByContent(swaggerStr);
		return new ResponseEntity<Object>(output, HttpStatus.OK);
	}

	public ResponseEntity<Object> getAssoiatedBasePaths(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas) throws Exception {
		if (oas != null && "3.0".equalsIgnoreCase(oas)) {
			return new ResponseEntity<Object>(swaggerBusiness.getSwagger3BasePathsObj(), HttpStatus.OK);
		} else {
			return new ResponseEntity<Object>(swaggerBusiness.getSwagger2BasePathsObj(), HttpStatus.OK);
		}

	}

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/swaggers/{swagger-id}/git-integrations")
	public ResponseEntity<Void> createOrUpdateGitIntegrations(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "oas", required = false, defaultValue = "2.0") String oas,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("swagger-id") String swaggerid,
			@Valid @RequestBody SwaggerIntegrations swaggerIntegrations) throws Exception {
		swaggerBusiness.createOrUpdateGitIntegrations(interactionid, jsessionid, swaggerid, oas, swaggerIntegrations);
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/{swagger-id}/git-integrations")
	public ResponseEntity<SwaggerIntegrations> getGitIntegrations(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false, defaultValue = "2.0") String oas,
			@PathVariable("swagger-id") String swaggerid) throws Exception {
		SwaggerIntegrations integrations = swaggerBusiness.getGitIntegrations(interactionid, jsessionid, swaggerid,
				oas);
		return new ResponseEntity<SwaggerIntegrations>(integrations, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/swaggers/{swagger-id}/git-integrations")
	public ResponseEntity<Void> deleteGitIntegrations(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false, defaultValue = "2.0") String oas,
			@PathVariable("swagger-id") String swaggerid) throws Exception {
		swaggerBusiness.deleteGitIntegrations(interactionid, jsessionid, swaggerid, oas);
		return new ResponseEntity<Void>(HttpStatus.ACCEPTED);
	}

	@ApiOperation(value = "Get Info of Swagger", notes = "", code = 200)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = Object.class),
			@ApiResponse(code = 404, message = "Resource not found", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/info", produces = {"application/json"})
	public ResponseEntity<Object> getSwaggerInfo(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = true, defaultValue = "2.0") String oas,
			@RequestParam("id") String swaggerid) throws Exception {
		Map swaggerInfo = swaggerBusiness.getSwaggerInfo(jsessionid, swaggerid, oas);
		return new ResponseEntity<Object>(swaggerInfo, HttpStatus.OK);
	}

	@ApiOperation(value = "Clone existing Swagger. Creates a new clone based on the request details ", notes = "", code = 201)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Swagger Cloned successfully", response = Void.class),
			@ApiResponse(code = 404, message = "Resource not found", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/clone")
	public ResponseEntity<?> cloneSwagger(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = true, defaultValue = "2.0") String oas,
			@RequestBody SwaggerCloneDetails swaggerCloneDetails) throws Exception {
		String swaggerId = swaggerBusiness.cloneSwagger(swaggerCloneDetails, oas);
		HttpStatus httpStatus = swaggerId != null ? HttpStatus.CREATED : HttpStatus.INTERNAL_SERVER_ERROR;
		HttpHeaders headers = new HttpHeaders();
		headers.add("Access-Control-Expose-Headers", "X-Swagger-id");
		headers.add("X-Swagger-id", swaggerId);
		return new ResponseEntity<Void>(headers, httpStatus);
	}

	@Override
	public ResponseEntity<?> getProxies(@PathVariable("swagger") String swagger,
			@PathVariable("revision") String revision,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false, defaultValue = "2.0") String oas) throws Exception {
		return new ResponseEntity<Object>(swaggerBusiness.getProxies(swagger, oas), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createPartnerGroup(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody SwaggerPartner swaggerPartner)
			throws Exception {
		swaggerPartner.setId(new ObjectId().toString());
		swaggerBusiness.createPartner(swaggerPartner);
		return new ResponseEntity<Object>(HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> updatePartnerGroup(@PathVariable("partnerId") String partnerId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody SwaggerPartner swaggerPartner)
			throws Exception {
		swaggerPartner.setId(partnerId);
		swaggerBusiness.updatePartner(swaggerPartner);
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> deletePartnerGroup(@PathVariable("partnerId") String partnerId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		swaggerBusiness.deletePartner(partnerId);
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getPartnerGroups(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		return new ResponseEntity<Object>(swaggerBusiness.getPartners(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> manageSwaggerPartners(@PathVariable("swaggerId") String swaggerId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = true, defaultValue = "2.0") String oas,
			@RequestBody AsociateSwaggerPartnerRequest swaggerPartnerRequest) throws Exception {
		swaggerBusiness.associatePartners(swaggerId, oas, swaggerPartnerRequest.getPartnerId());
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getSwaggerPartners(@PathVariable("swaggerId") String swaggerId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = true, defaultValue = "2.0") String oas) throws Exception {
		return new ResponseEntity<Object>(swaggerBusiness.getAssociatedPartners(swaggerId, oas), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> updateSwaggerDictionary(@RequestHeader String jsessionid,
			@RequestBody SwaggerDictionary swaggerDictionary) {
		swaggerBusiness.updateSwaggerDictionary(swaggerDictionary);
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<?> getSwaggerDictionary(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("swaggerId") String swaggerId, @PathVariable("revision") Integer revision) {
		return new ResponseEntity<>(swaggerBusiness.getSwaggerDictionary(swaggerId, revision), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getSwaggerAssociatedWithDataDictionary(
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable String dictionaryId) {
		DictionarySwagger swaggerAssociatedWithDictionary = swaggerBusiness
				.getSwaggerAssociatedWithDictionary(dictionaryId, null);
		if (swaggerAssociatedWithDictionary != null) {
			return new ResponseEntity<>(swaggerAssociatedWithDictionary, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<?> getSwaggerAssociatedWithSchemaName(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable String dictionaryId, @PathVariable String schemaName) {
		DictionarySwagger swaggerAssociatedWithDictionary = swaggerBusiness
				.getSwaggerAssociatedWithDictionary(dictionaryId, schemaName);
		if (swaggerAssociatedWithDictionary != null) {
			return new ResponseEntity<>(swaggerAssociatedWithDictionary, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

}
