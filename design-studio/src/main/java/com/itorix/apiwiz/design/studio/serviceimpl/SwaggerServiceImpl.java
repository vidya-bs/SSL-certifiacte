package com.itorix.apiwiz.design.studio.serviceimpl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.common.factory.IntegrationHelper;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.StorageIntegration;
import com.itorix.apiwiz.common.util.artifatory.JfrogUtilImpl;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.common.util.s3.S3Connection;
import com.itorix.apiwiz.common.util.s3.S3Utils;
import com.itorix.apiwiz.common.util.scm.ScmMinifiedUtil;
import com.itorix.apiwiz.common.util.scm.ScmUtilImpl;
import com.itorix.apiwiz.design.studio.business.NotificationBusiness;
import com.itorix.apiwiz.design.studio.business.SwaggerBusiness;
import com.itorix.apiwiz.design.studio.businessimpl.Swagger3SDK;
import com.itorix.apiwiz.design.studio.businessimpl.ValidateSchema;
import com.itorix.apiwiz.design.studio.businessimpl.XlsUtil;
import com.itorix.apiwiz.design.studio.dao.ApiRatingsDao;
import com.itorix.apiwiz.design.studio.dao.ComplianceScannerSqlDao;
import com.itorix.apiwiz.design.studio.dao.SupportedCodeGenLangDao;
import com.itorix.apiwiz.design.studio.dto.ComplicanceScannerExecutorEntity;
import com.itorix.apiwiz.design.studio.model.*;
import com.itorix.apiwiz.design.studio.model.swagger.sync.DictionarySwagger;
import com.itorix.apiwiz.design.studio.model.swagger.sync.SwaggerDictionary;
import com.itorix.apiwiz.design.studio.service.SwaggerService;
import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContextHolder;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.mongodb.client.result.DeleteResult;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
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
import java.util.stream.Collectors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

/**
 * The type Swagger service.
 */
@CrossOrigin
@RestController
@Slf4j
public class SwaggerServiceImpl implements SwaggerService {

	private static final Logger logger = LoggerFactory.getLogger(SwaggerServiceImpl.class);

	private static final String CREATE = "Create";
	private static final String UPDATE = "Update";

	/**
	 * The Clients.
	 */
	static List<String> clients = new ArrayList<String>();
	/**
	 * The Servers.
	 */
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

	/**
	 * The Swagger business.
	 */
	@Autowired
	SwaggerBusiness swaggerBusiness;

	/**
	 * The Application properties.
	 */
	@Autowired
	ApplicationProperties applicationProperties;

	/**
	 * The Rest template.
	 */
	@Autowired
	RestTemplate restTemplate;

	@Value("${compliance.scanner.uri:}")
	private String scannerUri;

	@Value("${linting.api.url:}")
	private String lintingUrl;

	@Value("${linting.api.lintSwagger:null}")
	private String lintSwagger;

	/**
	 * The Xls util.
	 */
	@Autowired
	XlsUtil xlsUtil;

	/**
	 * The Jfrog util.
	 */
	@Autowired
	JfrogUtilImpl jfrogUtilImpl;

	/**
	 * The Swagger subscription dao.
	 */
	@Autowired
	SwaggerSubscriptionDao swaggerSubscriptionDao;

	@Autowired
	private S3Connection s3Connection;

	@Autowired
	private S3Utils s3Utils;

	@Autowired
	private ScmUtilImpl scmUtilImpl;

	@Autowired
	private SupportedCodeGenLangDao codeGenLangDao;

	@Autowired
	private IntegrationHelper integrationHelper;

	/**
	 * The Api ratings dao.
	 */
	@Autowired
	ApiRatingsDao apiRatingsDao;

	@Autowired
	ComplianceScannerSqlDao complianceScannerSqlDao;

	@Autowired
	NotificationBusiness notificationBusiness;

	@Autowired
	private ScmMinifiedUtil scmMinifiedUtil;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/puls")
	public String checkPuls(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			HttpServletResponse response) {
		return "I am in Live :)";
	}

	private final String COMMIT_MESSAGE = "Pushed latest changes from Apiwiz platform";

	public ResponseEntity<Object> importSwaggers(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestHeader(value = "oas") String oas,
			@RequestParam(value = "file", required = false) MultipartFile file,
			@RequestParam(value = "type", required = true) String type,
			@RequestParam(value = "gitURI", required = false) String gitURI,
			@RequestParam(value = "branch", required = false) String branch,
			@RequestParam(value = "authType", required = false) String authType,
			@RequestParam(value = "userName", required = false) String userName,
			@RequestParam(value = "password", required = false) String password,
			@RequestParam(value = "personalToken", required = false) String personalToken) throws Exception {

		List<SwaggerImport> listSwaggers = swaggerBusiness.importSwaggers(file, type, gitURI, branch, authType,
				userName, password, personalToken, oas);
		ScannerDTO scannerDTO = new ScannerDTO();
		scannerDTO.setSwaggerId(listSwaggers.stream().map(swaggerImport -> {
			return swaggerImport.getSwaggerId();
		}).collect(Collectors.toList()));
		scannerDTO.setTenantId(getWorkspaceId());
		scannerDTO.setOperation(CREATE);

		if (!ObjectUtils.isEmpty(scannerDTO)) {
			callScannerAPI(scannerDTO, jsessionid);
		}
		return new ResponseEntity<Object>(listSwaggers, HttpStatus.OK);
	}

	/**
	 * This method is used to create the swagger.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param json
	 * @return
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
			@RequestHeader(value="x-editor" ,required = false) boolean editor,
			@RequestBody String json) throws Exception {

		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
		HttpHeaders headers = new HttpHeaders();
		ScannerDTO scannerDTO = new ScannerDTO();
		if (oas.equals("2.0")) {
			if (!swaggerBusiness.oasCheck(json).startsWith("2")) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Swagger-1009"), "Swagger-1009");
			}

			SwaggerVO swaggerVO = new SwaggerVO();
			swaggerVO.setName(swaggername);
			swaggerVO.setInteractionid(interactionid);
			SwaggerVO vo = swaggerBusiness.findSwagger(swaggerVO);
			if (vo != null) {
				swaggerBusiness.checkSwaggerTeams(jsessionid, swaggerVO.getName(), "2.0");
				swaggerVO.setSwagger(json);
				swaggerVO=swaggerBusiness.createSwaggerWithNewRevision(swaggerVO, jsessionid,interactionid);
			} else {
				swaggerVO.setSwagger(json);
				swaggerVO = swaggerBusiness.createSwagger(swaggerVO);
			}

			swaggerBusiness.updateSwaggerBasePath(swaggerVO.getName(), swaggerVO); // update
			// the
			// base
			// path
			// collection

			scannerDTO.setSwaggerId(Arrays.asList(swaggerVO.getSwaggerId()));
			scannerDTO.setOperation(CREATE);
			scannerDTO.setTenantId(getWorkspaceId());

			headers.add("Access-Control-Expose-Headers", "X-Swagger-Version, X-Swagger-id");
			headers.add("X-Swagger-Version", swaggerVO.getRevision() + "");
			headers.add("X-Swagger-id", swaggerVO.getSwaggerId());
			NotificationDetails notificationDetails = new NotificationDetails();
			notificationDetails.setNotification("Swagger has been created " .concat(swaggerVO.getName()));
			notificationDetails.setUserId(Arrays.asList(swaggerVO.getCreatedBy()));
			notificationDetails.setType(NotificationType.fromValue("Swagger"));
			notificationBusiness.createNotification(notificationDetails,jsessionid);
			if(editor) {
				initiateLinting(jsessionid, swaggerVO.getSwaggerId(), swaggerVO.getRevision(), "2.0",
						swaggerVO.getRuleSetIds());
			}
		} else if (oas.equals("3.0")) {
			if (!swaggerBusiness.oasCheck(json).startsWith("3")) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Swagger-1009"), "Swagger-1009");
			}

			Swagger3VO swaggerVO = new Swagger3VO();
			swaggerVO.setName(swaggername);
			swaggerVO.setInteractionid(interactionid);
			Swagger3VO vo = swaggerBusiness.findSwagger(swaggerVO);
			if (vo != null) {
				swaggerBusiness.checkSwaggerTeams(jsessionid, swaggerVO.getName(), "3.0");
				swaggerVO.setSwagger(json);
				swaggerVO=swaggerBusiness.createSwaggerWithNewRevision(swaggerVO, jsessionid,interactionid);
			} else {
				swaggerVO.setSwagger(json);
				swaggerVO = swaggerBusiness.createSwagger(swaggerVO);
			}

			swaggerBusiness.updateSwagger3BasePath(swaggerVO.getName(), swaggerVO);

			headers.add("Access-Control-Expose-Headers", "X-Swagger-Version, X-Swagger-id");
			headers.add("X-Swagger-Version", swaggerVO.getRevision() + "");
			headers.add("X-Swagger-id", swaggerVO.getSwaggerId());
			scannerDTO.setSwaggerId(Arrays.asList(swaggerVO.getSwaggerId()));
			scannerDTO.setTenantId(getWorkspaceId());
			scannerDTO.setOperation(CREATE);
			NotificationDetails notificationDetails = new NotificationDetails();
			notificationDetails.setNotification("Swagger has been created " .concat(swaggerVO.getName()));
			notificationDetails.setUserId(Arrays.asList(swaggerVO.getCreatedBy()));
			notificationDetails.setType(NotificationType.fromValue("Swagger"));
			notificationBusiness.createNotification(notificationDetails,jsessionid);
			if(editor) {
				initiateLinting(jsessionid, swaggerVO.getSwaggerId(), swaggerVO.getRevision(), "3.0",
						swaggerVO.getRuleSetIds());
			}
		}

		if (!ObjectUtils.isEmpty(scannerDTO)) {
			callScannerAPI(scannerDTO, jsessionid);
		}

		return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	}

	/**
	 * Using this we can subscribe to APIs
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggerSubscriptionReq
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "API Subscription", notes = "", response = Void.class)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Subscribed to API successfully", response = Void.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "System Error", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/swaggers/subscribe")
	public ResponseEntity<Void> swaggerSubscribe(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody SwaggerSubscriptionReq swaggerSubscriptionReq) throws Exception {
		Subscriber subscriber = new Subscriber();
		subscriber.setName(swaggerSubscriptionReq.getName());
		subscriber.setEmailId(swaggerSubscriptionReq.getEmailId());
		subscriber.setType(swaggerSubscriptionReq.getType());
		swaggerSubscriptionDao.swaggerSubscribe(swaggerSubscriptionReq.getSwaggerId(),
				swaggerSubscriptionReq.getSwaggerName(), swaggerSubscriptionReq.getOas(), subscriber);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	/**
	 * Using this we can unsubscribe to APIs
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggerId
	 * @param emailId
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "API Unsubscription", notes = "", response = Void.class)
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Unsubscribed to API successfully", response = Void.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "System Error", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/swaggers/unsubscribe/{swaggerid}/{emailid}")
	public ResponseEntity<Void> swaggerUnsubscribe(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("swaggerid") String swaggerId,
			@PathVariable("emailid") String emailId) throws Exception {
		swaggerSubscriptionDao.swaggerUnsubscribe(swaggerId, emailId);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	/**
	 * Using this we get all the subscribers list of an API
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggerId
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "Get API Subscribers", notes = "", response = Void.class)
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Got the list of subscribers successfully", response = Void.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "System Error", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/getsubscribers/{swaggerid}")
	public ResponseEntity<Set<Subscriber>> swaggerSubscribers(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("swaggerid") String swaggerId)
			throws Exception {
		Set<Subscriber> subscribers = swaggerSubscriptionDao.swaggerSubscribers(swaggerId);
		return new ResponseEntity<Set<Subscriber>>(subscribers, HttpStatus.OK);
	}
	/**
	 * This method returns if the user is subscriber of particular Swagger
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggerId
	 * @param emailId
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "Check if user is a subscriber", notes = "", response = Void.class)
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returned subscriber successfully", response = Void.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorObj.class),
			@ApiResponse(code = 500, message = "System Error", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swaggers/checksubscriber/{swaggerid}/{emailid}")
	public ResponseEntity<IsSubscribedUser> checkSubscriber(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("swaggerid") String swaggerId,
			@PathVariable("emailid") String emailId) throws Exception {
		return new ResponseEntity<IsSubscribedUser>(swaggerSubscriptionDao.checkSubscriber(swaggerId, emailId),
				HttpStatus.OK);
	}

	/**
	 * Using this we can update are change the swagger version.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param create
	 * @param swaggername
	 * @param json
	 * @return
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
			@RequestParam(value = CREATE, required = false, defaultValue = "new") String create,
			@PathVariable("swaggername") String swaggername, @RequestBody String json) throws Exception {
		RSAEncryption rsaEncryption = new RSAEncryption();
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
		HttpHeaders headers = new HttpHeaders();
		ScannerDTO scannerDTO = new ScannerDTO();
		if (oas.equals("2.0")) {
			SwaggerVO swaggerVO = new SwaggerVO();
			swaggerVO.setName(swaggername);
			swaggerVO.setInteractionid(interactionid);
			swaggerVO.setSwagger(json);
			swaggerVO = swaggerBusiness.createSwaggerWithNewRevision(swaggerVO, jsessionid,interactionid);
			swaggerBusiness.updateSwaggerBasePath(swaggerVO.getName(), swaggerVO); // update
			// the
			// base
			// path
			// collection
			SwaggerIntegrations integrations = swaggerBusiness.getGitIntegrations(interactionid, jsessionid,
					swaggerVO.getName(), oas);
			uploadFilesToGit(integrations, swaggerVO, oas, json,
					rsaEncryption);
			
			headers.add("X-Swagger-Version", swaggerVO.getRevision() + "");
			headers.add("X-Swagger-id", swaggerVO.getSwaggerId());

			scannerDTO.setTenantId(getWorkspaceId());
			scannerDTO.setSwaggerId(Arrays.asList(swaggerVO.getSwaggerId()));
			scannerDTO.setOperation(UPDATE);

			NotificationDetails notificationDetails = new NotificationDetails();
			notificationDetails.setNotification("Swagger has been created  with new Revision" .concat(swaggerVO.getName()));
			notificationDetails.setUserId(Arrays.asList(swaggerVO.getCreatedBy()));
			notificationDetails.setType(NotificationType.fromValue("Swagger"));
			notificationBusiness.createNotification(notificationDetails,jsessionid);
			initiateLinting(jsessionid, swaggerVO.getSwaggerId(), swaggerVO.getRevision(), "2.0",
					swaggerVO.getRuleSetIds());
		} else if (oas.equals("3.0")) {
			Swagger3VO swaggerVO = new Swagger3VO();
			swaggerVO.setName(swaggername);
			swaggerVO.setInteractionid(interactionid);
			swaggerVO.setSwagger(json);
			swaggerVO = swaggerBusiness.createSwaggerWithNewRevision(swaggerVO, jsessionid,interactionid);
			swaggerBusiness.updateSwagger3BasePath(swaggerVO.getName(), swaggerVO); // update
			// the
			// base
			// path
			// collection
			SwaggerIntegrations integrations = swaggerBusiness.getGitIntegrations(interactionid, jsessionid,
					swaggerVO.getName(), oas);
			uploadFilesToGit(integrations, swaggerVO, oas,
					json,
					rsaEncryption);
			scannerDTO.setTenantId(getWorkspaceId());
			scannerDTO.setSwaggerId(Arrays.asList(swaggerVO.getSwaggerId()));
			scannerDTO.setOperation(UPDATE);
			headers.add("X-Swagger-Version", swaggerVO.getRevision() + "");
			headers.add("X-Swagger-id", swaggerVO.getSwaggerId());
			NotificationDetails notificationDetails = new NotificationDetails();
			notificationDetails.setNotification("Swagger has been created  with new Revision" .concat(swaggerVO.getName()));
			notificationDetails.setUserId(Arrays.asList(swaggerVO.getCreatedBy()));
			notificationDetails.setType(NotificationType.fromValue("Swagger"));
			notificationBusiness.createNotification(notificationDetails,jsessionid);
			initiateLinting(jsessionid, swaggerVO.getSwaggerId(), swaggerVO.getRevision(), "3.0",
					swaggerVO.getRuleSetIds());
		}

		if (!ObjectUtils.isEmpty(scannerDTO)) {
			callScannerAPI(scannerDTO, jsessionid);
		}
		return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	}

	private void initiateLinting(String jsessionid,
			String swaggerId, Integer revision, String oas, List<String> ruleSetIds) {
		try {
			String globalRule=swaggerBusiness.getGolbalRule(oas);
			if(globalRule!=null&&ruleSetIds!=null&&!ruleSetIds.contains(globalRule))
			{
				ruleSetIds.add(globalRule);
			}
			else if(globalRule!=null&&ruleSetIds==null){
				ruleSetIds=new ArrayList<String>();
				ruleSetIds.add(globalRule);
			}
			SwaggerLintingInfo swaggerLintingInfo = new SwaggerLintingInfo();
			swaggerLintingInfo.setSwaggerId(swaggerId);
			swaggerLintingInfo.setRevision(revision);
			swaggerLintingInfo.setOasVersion(oas);
			swaggerLintingInfo.setRuleSetIds(ruleSetIds);
			callLintingAPI(swaggerLintingInfo, jsessionid);
		} catch (Exception ex) {
			logger.error("Error while calling linting API {} ", ex.getMessage());
		}
	}
	private void uploadFilesToGit(SwaggerIntegrations integrations, Object swaggerVO, String oas,
			String json,
			RSAEncryption rsaEncryption) throws Exception {
		if (integrations != null && integrations.getScm_authorizationType().equalsIgnoreCase("basic")) {
			File file = getFile(integrations, swaggerVO, json);
			scmUtilImpl.pushFilesToSCM(file, integrations.getScm_repository(),
					integrations.getScm_username(),
					integrations.getScm_password(), integrations.getScm_url(),
					integrations.getScm_type(), integrations.getScm_branch(), COMMIT_MESSAGE);
		} else if (integrations != null && swaggerVO != null) {
			File file = getFile(integrations, swaggerVO, json);
			scmUtilImpl.pushFilesToSCMBase64(file, integrations.getScm_repository(),
					integrations.getScm_authorizationType(),
					integrations.getScm_token(),
					integrations.getScm_url(), integrations.getScm_type(), integrations.getScm_branch(),
					COMMIT_MESSAGE);
		}
	}

	private File getFile(SwaggerIntegrations integrations, Object swaggerVO, String json)
			throws IOException {

		File file = createSwaggerFile(
				swaggerVO instanceof SwaggerVO ? ((SwaggerVO) swaggerVO).getName()
						: ((Swagger3VO) swaggerVO).getName(), json, integrations.getScm_folder(),
				swaggerVO instanceof SwaggerVO ? ((SwaggerVO) swaggerVO).getRevision()
						: ((Swagger3VO) swaggerVO).getRevision());
		return file;
	}

	/**
	 * Using this we can update the swagger version.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param swaggername
	 * @param revision
	 * @param json
	 * @return
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
		HttpHeaders headers = new HttpHeaders();
		ScannerDTO scannerDTO = new ScannerDTO();
		ObjectMapper om = new ObjectMapper();
		om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		om.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
			try{
				if (integrations != null && !integrations.getScm_authorizationType().toUpperCase().contains("TOKEN")&&integrations.isEnableScm()) {
					File file = createSwaggerFile(swaggerVO.getName(), om.readTree(vo.getSwagger()).toPrettyString(), integrations.getScm_folder(),
							swaggerVO.getRevision());

					scmMinifiedUtil.pushFilesToSCM(file, integrations.getScm_repository(),
							integrations.getScm_username(),
							integrations.getScm_password(), integrations.getScm_url(),
							integrations.getScm_type(), integrations.getScm_branch(), COMMIT_MESSAGE);

				} else if (integrations != null && integrations.getScm_authorizationType() != null&&integrations.isEnableScm()) {
					File file = createSwaggerFile(swaggerVO.getName(), om.readTree(vo.getSwagger()).toPrettyString(), integrations.getScm_folder(),
							swaggerVO.getRevision());

					scmMinifiedUtil.pushFilesToSCMBase64(file, integrations.getScm_repository(), "TOKEN",
							integrations.getScm_token(), integrations.getScm_url(),
							integrations.getScm_type(), integrations.getScm_branch(), COMMIT_MESSAGE);
				}
			}catch (Exception ex){
				logger.error("Could not sync swagger with SCM:" + ex.getMessage());
			}


			scannerDTO.setTenantId(getWorkspaceId());
			scannerDTO.setSwaggerId(Arrays.asList(swaggerVO.getSwaggerId()));
			scannerDTO.setOperation(UPDATE);

			headers.add("X-Swagger-Version", swaggerVO.getRevision() + "");
			NotificationDetails notificationDetails = new NotificationDetails();
			notificationDetails.setNotification("Swagger has been Updated" .concat(swaggerVO.getName()));
			notificationDetails.setUserId(Arrays.asList(swaggerVO.getCreatedBy()));
			notificationDetails.setType(NotificationType.fromValue("Swagger"));
			notificationBusiness.createNotification(notificationDetails,jsessionid);
			initiateLinting(jsessionid, vo.getSwaggerId(), vo.getRevision(), "2.0",
					vo.getRuleSetIds());
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
			try{
				if (integrations != null && !integrations.getScm_authorizationType().toUpperCase().contains("TOKEN")&&integrations.isEnableScm()) {
					File file = createSwaggerFile(swaggerVO.getName(), om.readTree(vo.getSwagger()).toPrettyString(), integrations.getScm_folder(),
							swaggerVO.getRevision());
					scmMinifiedUtil.pushFilesToSCM(file, integrations.getScm_repository(),
							integrations.getScm_username(),
							integrations.getScm_password(), integrations.getScm_url(),
							integrations.getScm_type(), integrations.getScm_branch(), COMMIT_MESSAGE);
				} else if (integrations != null && integrations.getScm_authorizationType() != null&&integrations.isEnableScm()) {
					File file = createSwaggerFile(swaggerVO.getName(), om.readTree(vo.getSwagger()).toPrettyString(), integrations.getScm_folder(),
							swaggerVO.getRevision());
					scmMinifiedUtil.pushFilesToSCMBase64(file, integrations.getScm_repository(), "TOKEN",
							integrations.getScm_token(), integrations.getScm_url(),
							integrations.getScm_type(), integrations.getScm_branch(), COMMIT_MESSAGE);
				}
			}catch (Exception ex){
				logger.error("Could not sync swagger with SCM:" + ex.getMessage());
			}

			headers.add("X-Swagger-Version", swaggerVO.getRevision() + "");

			scannerDTO.setTenantId(getWorkspaceId());
			scannerDTO.setSwaggerId(Arrays.asList(swaggerVO.getSwaggerId()));
			scannerDTO.setOperation(UPDATE);
			NotificationDetails notificationDetails = new NotificationDetails();
			notificationDetails.setNotification("Swagger has been updated" .concat(swaggerVO.getName()));
			notificationDetails.setUserId(Arrays.asList(swaggerVO.getCreatedBy()));
			notificationDetails.setType(NotificationType.fromValue("Swagger"));
			notificationBusiness.createNotification(notificationDetails,jsessionid);
			initiateLinting(jsessionid, vo.getSwaggerId(), vo.getRevision(), "3.0",
					vo.getRuleSetIds());
		}

		if (!ObjectUtils.isEmpty(scannerDTO)) {
			callScannerAPI(scannerDTO, jsessionid);
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
	 * @return
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
			String page,
			@RequestHeader(value = "oas", required = false) String oas,
			@PathVariable("swaggername") String swaggername)
			throws Exception {
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
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
		if (StringUtils.equalsIgnoreCase("Virtualisation", page)||StringUtils.equalsIgnoreCase("TestSuite", page)) {
			list = list.stream().filter(
							revision -> (!StringUtils.equalsIgnoreCase("Deprecate", revision.getStatus())
									&& !StringUtils.equalsIgnoreCase("Retired", revision.getStatus())))
					.collect(Collectors.toList());
		}
		if (StringUtils.equalsIgnoreCase("Proxy", page)||StringUtils.equalsIgnoreCase("Kong", page)) {
			list = list.stream().filter(
							revision -> (StringUtils.equalsIgnoreCase("Approved", revision.getStatus())
									|| StringUtils.equalsIgnoreCase("Publish", revision.getStatus())))
					.collect(Collectors.toList());
		}
		if(list!=null){
			list = list.stream().sorted(Comparator.comparing(Revision::getRevision)).collect(Collectors.toList());
		}
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getListOfSwaggerNames(String interactionid, String oas,
			String page,String jsessionid) throws Exception {
		{
			if (oas == null || oas.trim().equals("")) {
				oas = "2.0";
			}
			JSONObject jsonObject = new JSONObject();
			if (oas.equals("2.0")) {
				List<SwaggerVO> swaggers = swaggerBusiness.getSwaggerNames(page,jsessionid);
				jsonObject.accumulate("swaggers", swaggers);
			} else if (oas.equals("3.0")) {
				List<Swagger3VO> swagger3s = swaggerBusiness.getSwagger3Names(page,jsessionid);
				jsonObject.accumulate("swaggers", swagger3s);
			}
			return new ResponseEntity<Object>(jsonObject.toString(), HttpStatus.OK);
		}
	}

	/**
	 * Using this we will get all the Swagger's.
	 *
	 * @param request
	 * @param response
	 * @param interactionid
	 * @param jsessionid
	 * @param page
	 * @param oas
	 * @return
	 * @throws ItorixException
	 */

	/**
	 * Using this we will get the swagger name along with version and state.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
		String json = "";
		List<String> products = null;
		if (null != product) {
			products = Arrays.asList(product.split(","));
		}
		if (oas.equals("2.0")) {
			SwaggerHistoryResponse response;
			if (null != products) {
				response = swaggerBusiness.getSwaggerDetailsByproduct(products, interactionid, jsessionid, offset, oas,
						swagger, pageSize);
			} else {
				response = swaggerBusiness.getListOfSwaggerDetails(status, modifiedDate, interactionid, jsessionid,
						offset, oas, swagger, pageSize, sortByModifiedDate);
			}
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			json = mapper.writeValueAsString(response);
		} else if (oas.equals("3.0")) {
			SwaggerHistoryResponse response;
			if (null != products) {
				response = swaggerBusiness.getSwaggerDetailsByproduct(products, interactionid, jsessionid, offset, oas,
						swagger, pageSize);
			} else {
				response = swaggerBusiness.getListOfSwagger3Details(status, modifiedDate, interactionid, jsessionid,
						offset, oas, swagger, pageSize, sortByModifiedDate);
			}
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			json = mapper.writeValueAsString(response);
		}
		return new ResponseEntity<Object>(json, HttpStatus.OK);
	}

	/**
	 * We will get when the swagger state is published.
	 *
	 * @param request
	 * @param response
	 * @param interactionid
	 * @param jsessionid
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
			@RequestParam Map<String, String> filterParams) throws Exception {


		List<String> partners = new ArrayList<>(0);
		List<String> products = new ArrayList<>(0);
		String status = "";
		if (filterParams != null && filterParams.get("status") != null && !filterParams.get("status")
				.isEmpty()) {
			status = filterParams.get("status");
		}

		if (filterParams.get("partnerNames") != null && !filterParams.get(
				"partnerNames").isEmpty()) {
			partners = swaggerBusiness.getPartners().stream().filter(
					swaggerPartner -> filterParams.get("partnerNames")
							.contains(swaggerPartner.getId())).map(SwaggerPartner::getId).collect(
					Collectors.toList());
			if (partners.isEmpty()) {
				return ResponseEntity.ok(Collections.EMPTY_SET);
			}
		}else if(filterParams.get("partnerNames") != null && filterParams.get(
				"partnerNames").isEmpty()){
			return ResponseEntity.ok(Collections.EMPTY_SET);
		}

		if (filterParams.get("productNames") != null && !filterParams.get(
				"productNames").isEmpty()) {
			products = swaggerBusiness.getProductGroups(interactionid, jsessionid).stream().filter(
					swaggerProduct -> filterParams.get("productNames")
							.contains(swaggerProduct.getProductName())).map(SwaggerProduct::getId).collect(
					Collectors.toList());
			if (products.isEmpty()) {
				return ResponseEntity.ok(Collections.EMPTY_SET);
			}
		}

		String json = "";
		ArrayNode node = swaggerBusiness.getListOfPublishedSwaggerDetails(interactionid, jsessionid,
				status,
				partners, products);
		ArrayNode node3 = swaggerBusiness.getListOfPublishedSwagger3Details(interactionid, jsessionid,
				status,
				partners, products);
		if (node3 != null && node3.size() > 0) {
			for (JsonNode nodeElement : node3) {
				node.add(nodeElement);
			}
		}
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
	 * @return
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
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
	 * @return
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
		ScannerDTO scannerDTO = new ScannerDTO();
		if (oas.equals("2.0")) {
			SwaggerVO vo = swaggerBusiness.findSwagger(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}

			swaggerBusiness.deleteSwagger(vo.getName(), interactionid);
			DeleteResult result = swaggerBusiness.deleteSwagger2BasePath(vo);
			logger.debug("Result of Deletion of the basePath : " + result.getDeletedCount());
			scannerDTO.setTenantId(getWorkspaceId());
			scannerDTO.setOperation("Delete");
			scannerDTO.setSwaggerId(Arrays.asList(vo.getSwaggerId()));
			NotificationDetails notificationDetails = new NotificationDetails();
			notificationDetails.setNotification("Swagger has been Deleted" .concat(vo.getName()));
			notificationDetails.setUserId(Arrays.asList(vo.getCreatedBy()));
			notificationDetails.setType(NotificationType.fromValue("Swagger"));
			notificationBusiness.createNotification(notificationDetails,jsessionid);
		} else if (oas.equals("3.0")) {
			Swagger3VO vo = swaggerBusiness.findSwagger3(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			swaggerBusiness.deleteSwagger3(vo.getName(), interactionid);
			DeleteResult result = swaggerBusiness.deleteSwagger3BasePath(vo);
			logger.debug("Result of Deletion of the basePath : " + result.getDeletedCount());
			scannerDTO.setTenantId(getWorkspaceId());
			scannerDTO.setOperation("Delete");
			scannerDTO.setSwaggerId(Arrays.asList(vo.getSwaggerId()));
			NotificationDetails notificationDetails = new NotificationDetails();
			notificationDetails.setNotification("Swagger has been Deleted" .concat(vo.getName()));
			notificationDetails.setUserId(Arrays.asList(vo.getCreatedBy()));
			notificationDetails.setType(NotificationType.fromValue("Swagger"));
			notificationBusiness.createNotification(notificationDetails,jsessionid);
		}

		if (!ObjectUtils.isEmpty(scannerDTO)) {
			callScannerAPI(scannerDTO, jsessionid);
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
	 * @return
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
		ScannerDTO scannerDTO = new ScannerDTO();
		if (oas.equals("2.0")) {
			SwaggerVO vo = swaggerBusiness.findSwagger(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			swaggerBusiness.deleteSwaggerVersion(vo.getName(), revision, interactionid);
			NotificationDetails notificationDetails = new NotificationDetails();
			notificationDetails.setNotification(String.format("Swagger %s - revision %s has been Deleted ",vo.getName(),vo.getRevision()));
			notificationDetails.setUserId(Arrays.asList(vo.getCreatedBy()));
			notificationDetails.setType(NotificationType.fromValue("Swagger"));
			notificationBusiness.createNotification(notificationDetails,jsessionid);
			if (swaggerBusiness.findSwaggersCount(vo.getSwaggerId()) < 1) {
				swaggerBusiness.deleteSwagger2BasePath(vo);
				scannerDTO.setTenantId(getWorkspaceId());
				scannerDTO.setOperation("Delete");
				scannerDTO.setSwaggerId(Arrays.asList(vo.getSwaggerId()));

			} else {
				scannerDTO.setTenantId(getWorkspaceId());
				scannerDTO.setOperation(UPDATE);
				scannerDTO.setSwaggerId(Arrays.asList(vo.getSwaggerId()));
			}
		} else if (oas.equals("3.0")) {
			Swagger3VO vo = swaggerBusiness.findSwagger3(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			swaggerBusiness.deleteSwagger3Version(vo.getName(), revision, interactionid);
			NotificationDetails notificationDetails = new NotificationDetails();
			notificationDetails.setNotification(String.format("Swagger %s revision - %s has been Deleted ",vo.getName(),vo.getRevision()));
			notificationDetails.setUserId(Arrays.asList(vo.getCreatedBy()));
			notificationDetails.setType(NotificationType.fromValue("Swagger"));
			notificationBusiness.createNotification(notificationDetails,jsessionid);
			if (swaggerBusiness.findSwaggers3VOCount(vo.getSwaggerId()) < 1) {
				swaggerBusiness.deleteSwagger3BasePath(vo);
				scannerDTO.setTenantId(getWorkspaceId());
				scannerDTO.setOperation("Delete");
				scannerDTO.setSwaggerId(Arrays.asList(vo.getSwaggerId()));
			} else {
				scannerDTO.setTenantId(getWorkspaceId());
				scannerDTO.setOperation(UPDATE);
				scannerDTO.setSwaggerId(Arrays.asList(vo.getSwaggerId()));
			}

		}

		if (!ObjectUtils.isEmpty(scannerDTO)) {
			callScannerAPI(scannerDTO, jsessionid);
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
		ScannerDTO scannerDTO = new ScannerDTO();
		if (oas.equals("2.0")) {
			SwaggerVO vo = swaggerBusiness.updateStatus(swaggername, revision, json, interactionid, jsessionid);
			scannerDTO.setTenantId(getWorkspaceId());
			scannerDTO.setOperation(UPDATE);
			scannerDTO.setSwaggerId(Arrays.asList(vo.getSwaggerId()));
			NotificationDetails notificationDetails = new NotificationDetails();
			notificationDetails.setNotification("Swagger status has been updated" .concat(vo.getName()!=null ? vo.getName() : ""));
			notificationDetails.setUserId(Arrays.asList(vo.getCreatedBy()));
			notificationDetails.setType(NotificationType.fromValue("Swagger"));
			notificationBusiness.createNotification(notificationDetails,jsessionid);
			initiateLinting(jsessionid, vo.getSwaggerId(), vo.getRevision(), "2.0",
					vo.getRuleSetIds());
		} else if (oas.equals("3.0")) {
			Swagger3VO vo = swaggerBusiness.updateSwagger3Status(swaggername, revision, json, interactionid,
					jsessionid);
			scannerDTO.setTenantId(getWorkspaceId());
			scannerDTO.setOperation(UPDATE);
			scannerDTO.setSwaggerId(Arrays.asList(vo.getSwaggerId()));
			NotificationDetails notificationDetails = new NotificationDetails();
			notificationDetails.setNotification("Swagger status has been updated" .concat(vo.getName()!=null ? vo.getName() : ""));
			notificationDetails.setUserId(Arrays.asList(vo.getCreatedBy()));
			notificationDetails.setType(NotificationType.fromValue("Swagger"));
			notificationBusiness.createNotification(notificationDetails,jsessionid);
			initiateLinting(jsessionid, vo.getSwaggerId(), vo.getRevision(), "3.0",
					vo.getRuleSetIds());
		}
		if (!ObjectUtils.isEmpty(scannerDTO)) {
			callScannerAPI(scannerDTO, jsessionid);

		}

		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<Object> getRoles(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas, @PathVariable("swaggername") String swaggername)
			throws Exception {
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
		if (oas.equals("2.0")) {
			SwaggerVO vo = swaggerBusiness.findSwagger(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
			swaggername = vo.getName();
		} else if (oas.equals("3.0")) {
			Swagger3VO vo = swaggerBusiness.findSwagger3(swaggername, interactionid);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
			}
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
		if (oas.equals("2.0")) {
			comment.setSwaggerRevision(revision);
			comment.setSwaggerName(swaggername);
			comment.setInteractionid(interactionid);
			swaggerBusiness.updateComment(comment);
			NotificationDetails notificationDetails = new NotificationDetails();
			notificationDetails.setNotification("Swagger comment has been updated  ." .concat( comment.getComment()));
			notificationDetails.setUserId(Arrays.asList(comment.getCreatedBy()));
			notificationDetails.setType(NotificationType.fromValue("Swagger"));
			notificationBusiness.createNotification(notificationDetails,jsessionid);

		} else if (oas.equals("3.0")) {
			Swagger3Comment swagger3Comment = new Swagger3Comment();
			swagger3Comment.setSwaggerRevision(revision);
			swagger3Comment.setSwaggerName(swaggername);
			swagger3Comment.setInteractionid(interactionid);
			swagger3Comment.setComment(comment.getComment());
			swaggerBusiness.updateSwagger3Comment(swagger3Comment);
			NotificationDetails notificationDetails = new NotificationDetails();
			notificationDetails.setNotification("Swagger comment has been updated  .".concat( comment.getComment()));
			notificationDetails.setUserId(Arrays.asList(comment.getCreatedBy()));
			notificationDetails.setType(NotificationType.fromValue("Swagger"));
			notificationBusiness.createNotification(notificationDetails,jsessionid);
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
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
			NotificationDetails notificationDetails = new NotificationDetails();
			notificationDetails.setNotification("Swagger has been Deprecate" .concat(vo.getName()!=null ? vo.getName() : ""));
			notificationDetails.setUserId(Arrays.asList(vo.getCreatedBy()));
			notificationDetails.setType(NotificationType.fromValue("Swagger"));
			notificationBusiness.createNotification(notificationDetails,jsessionid);
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
			NotificationDetails notificationDetails = new NotificationDetails();
			notificationDetails.setNotification("Swagger has been Deprecate" .concat(vo.getName()!=null ? vo.getName() : ""));
			notificationDetails.setUserId(Arrays.asList(vo.getCreatedBy()));
			notificationDetails.setType(NotificationType.fromValue("Swagger"));
			notificationBusiness.createNotification(notificationDetails,jsessionid);
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
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
	 * @return
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
	 * @return
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
	 * @return
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
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

		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
		if (oas.equals("2.0")) {
			return new ResponseEntity<Object>(swaggerBusiness.getSwaggerStats(timeunit, timerange), HttpStatus.OK);
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

	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.GET, value = "/v1/swagger-gen/clients/servers")
	public @ResponseBody ResponseEntity<Object> getClientsServers(
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "oas", required = false) String oas) throws Exception {
		Map clientsServer = new HashMap();
		if (oas == null || oas.isEmpty()) {
			oas = "2.0";
		}
		try {
			clientsServer.put("clients", codeGenLangDao.getSupportedLanguages("client", oas));
			clientsServer.put("servers", codeGenLangDao.getSupportedLanguages("server", oas));
		} catch (Exception ex) {
			clientsServer.put("clients", Clients.values());
			clientsServer.put("servers", Servers.values());
		}

		return new ResponseEntity<Object>(clientsServer, HttpStatus.OK);
	}

	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swagger-gen/clients/servers/{framework}")
	public @ResponseBody ResponseEntity<Object> createLangSupport(
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("framework") String framework, @RequestBody SupportedCodeGenLang langData) throws Exception {

		SupportedCodeGenLang res = codeGenLangDao.addLang(langData);
		return new ResponseEntity<Object>(res, HttpStatus.OK);
	}

	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/swagger-gen/clients/servers/{framework}")
	public @ResponseBody ResponseEntity<Object> updateLangSupport(
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("framework") String framework, @RequestBody SupportedCodeGenLang langData) throws Exception {

		SupportedCodeGenLang res = codeGenLangDao.updateLang(framework, langData);
		return new ResponseEntity<Object>(res, HttpStatus.OK);
	}

	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/swagger-gen/clients/servers/{lang}")
	public @ResponseBody ResponseEntity<Void> removeLangSupport(
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("lang") String lang) throws Exception {
		codeGenLangDao.removeLang(lang);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Using this we will get the swagger name along with version and state.
	 *
	 * @param interactionid
	 * @param jsessionid
	 * @param request
	 * @param response
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
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
			String outputFolder =
					applicationProperties.getSwageerGenDir() + "clients" + File.separator + framework
							+ File.separator + genrateClientRequest.getSwaggerName() + "_client";
			options.put("outputFolder", outputFolder);
			generatorInput.setOptions(options);
			String filename = Generator.generateClient(framework, generatorInput);
			String downloadURI = null;
			ResponseCode responseCode = new ResponseCode();
			try {
				File file = new File(filename);
				StorageIntegration storageIntegration = integrationHelper.getIntegration();
				downloadURI = storageIntegration.uploadFile("swaggerClients/" + framework + "/" + System.currentTimeMillis() + "/" + file.getName(), filename);
				new File(filename).delete();
				responseCode.setLink(downloadURI);
			} catch (Exception e) {
				throw new ItorixException("Invalid storage connector credentials", "General-1000");
			}
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
			ResponseCode responseCode = new ResponseCode();
			try {
				File file = new File(filename);
				StorageIntegration storageIntegration = integrationHelper.getIntegration();
				downloadURI = storageIntegration.uploadFile("swaggerClients/" + framework + "/" + System.currentTimeMillis() + "/" + file.getName(), filename);
				new File(filename).delete();
				responseCode.setLink(downloadURI);
			} catch (Exception e) {
				throw new ItorixException("Invalid storage connector credentials", "General-1000");
			}
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
	 * @return
	 */
	@ApiOperation(value = "Genrate Server", notes = "", code = 200)
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
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
			ResponseCode responseCode = new ResponseCode();
			try {
				File file = new File(filename);
				StorageIntegration storageIntegration = integrationHelper.getIntegration();
				downloadURI = storageIntegration.uploadFile("swaggerClients/" + framework + "/" + System.currentTimeMillis() + "/" + file.getName(), filename);
			}catch (Exception e) {
				throw new ItorixException("Invalid storage connector credentials", "General-1000");
			}
			new File(filename).delete();
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
			String downloadURI = null;
			ResponseCode responseCode = new ResponseCode();
			try {
				File file = new File(filename);
				StorageIntegration storageIntegration = integrationHelper.getIntegration();
				downloadURI = storageIntegration.uploadFile("swaggerClients/" + framework + "/" + System.currentTimeMillis() + "/" + file.getName(), filename);
			}catch (Exception e) {
				throw new ItorixException("Invalid storage connector credentials", "General-1000");
			}
			new File(filename).delete();
			responseCode.setLink(downloadURI);
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
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
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-10"
						+ "00")), "Swagger-1000");
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
	 * @return
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
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
		return ResponseEntity.ok(Collections.EMPTY_SET);
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
	 * @return
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
	 * @return
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
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
		if (oas.equals("2.0")) {
			response = swaggerBusiness.swaggerSearch(interactionid, name, limit,jsessionid);
		} else if (oas.equals("3.0")) {
			response = swaggerBusiness.swagger3Search(interactionid, name, limit,jsessionid);
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
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.addAll("Access-Control-Expose-Headers", Arrays.asList("x-created-by", "x-created-user-name"));
		httpHeaders.set("x-created-by", String.valueOf(swaggerInfo.remove("createdBy")));
		httpHeaders.set("x-created-user-name", String.valueOf(swaggerInfo.remove("createdUsername")));
		ResponseEntity<Object> responseEntity = new ResponseEntity<>(swaggerInfo, httpHeaders, HttpStatus.OK);
		return responseEntity;
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
	public ResponseEntity<?> createOrUpdatePartnerGroup(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody List<SwaggerPartner> swaggerPartners)
			throws Exception {
		swaggerBusiness.updatePartners(swaggerPartners);
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
		swaggerBusiness.associatePartners(swaggerId, oas, swaggerPartnerRequest.getPartnerId().stream().collect(
				Collectors.toSet()));
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
		log.info("Update Swagger dictionary");
		swaggerBusiness.updateSwaggerDictionary(swaggerDictionary);
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<?> getSwaggerDictionary(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("swaggerId") String swaggerId, @PathVariable("revision") Integer revision) {
		log.info("Get Assoiated Swagger dictionary");
		return new ResponseEntity<>(swaggerBusiness.getSwaggerDictionary(swaggerId, revision), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getSwaggerAssociatedWithDataDictionary(
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable String dictionaryId) {
		DictionarySwagger swaggerAssociatedWithDictionary = swaggerBusiness
				.getSwaggerAssociatedWithDictionary(dictionaryId, null,null);
		if (swaggerAssociatedWithDictionary != null) {
			return new ResponseEntity<>(swaggerAssociatedWithDictionary, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<?> getSwaggerAssociatedWithSchemaName(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable String dictionaryId, @PathVariable String modelId) {
		DictionarySwagger swaggerAssociatedWithDictionary = swaggerBusiness
				.getSwaggerAssociatedWithDictionary(dictionaryId, modelId,null);
		if (swaggerAssociatedWithDictionary != null) {
			return new ResponseEntity<>(swaggerAssociatedWithDictionary, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<?> getSwaggerAssociatedWithModelId(@RequestHeader(value = "JSESSIONID") String jsessionid,
															 @PathVariable String dictionaryId, @PathVariable String modelId, @PathVariable Integer revision) {
		log.info("Get Swagger Asoociated with Model Id");
		DictionarySwagger swaggerAssociatedWithDictionary = swaggerBusiness
				.getSwaggerAssociatedWithDictionary(dictionaryId, modelId , revision);
		if (swaggerAssociatedWithDictionary != null) {
			return new ResponseEntity<>(swaggerAssociatedWithDictionary, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<?> postRating(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = true, defaultValue = "2.0") String oas, String swaggerId,
			ApiRatings apiRatings) {
		return apiRatingsDao.postRating(swaggerId, apiRatings);
	}

	@Override
	public ResponseEntity<?> editRating(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = true, defaultValue = "2.0") String oas, String swaggerId,
			ApiRatings apiRatings) {
		return apiRatingsDao.editRating(swaggerId, apiRatings);
	}

	@Override
	public ResponseEntity<?> getRatingSummary(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = true, defaultValue = "2.0") String oas, String swaggerId,
			int revison) {
		return new ResponseEntity<>(apiRatingsDao.getRatingSummary(swaggerId, oas, revison), HttpStatus.OK);

	}

	@Override
	public ResponseEntity<?> getAllRatings(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "email") String email,
			@RequestHeader(value = "oas", required = true, defaultValue = "2.0") String oas, String swaggerId,
			int revision) {
		return new ResponseEntity<>(apiRatingsDao.getRatings(swaggerId, oas, revision, email), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> deleteRatingAdmin(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "ratingId") String ratingId,
			@RequestHeader(value = "oas", required = true, defaultValue = "2.0") String oas, String swaggerId,
			int revision) {
		return new ResponseEntity<>(apiRatingsDao.deleteRatingadmin(swaggerId, revision, oas, ratingId),
				HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> deleteRating(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "email") String email, @RequestHeader(value = "ratingId") String ratingId,
			@RequestHeader(value = "oas", required = true, defaultValue = "2.0") String oas, String swaggerId,
			int revision) {
		return new ResponseEntity<>(apiRatingsDao.deleteRating(swaggerId, revision, oas, email, ratingId), HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<?> loadSwaggersToScan(String interactionid, String jsessionid) throws ItorixException {
		List<String> swaggersList = swaggerBusiness.loadSwaggersToScan(interactionid, jsessionid);
		ScannerDTO scannerDTO = new ScannerDTO();
		scannerDTO.setOperation(CREATE);
		scannerDTO.setTenantId(getWorkspaceId());
		scannerDTO.setSwaggerId(swaggersList);
		callScannerAPI(scannerDTO, jsessionid);
		return ResponseEntity.ok().body("Syncing " + swaggersList.size() + " Swaggers.");

	}

	@Override
	public ResponseEntity<?> createProduct(String jsessionid, String interactionid,
			SwaggerProduct swaggerProduct) throws ItorixException {
		return ResponseEntity.ok(swaggerBusiness.createProduct(swaggerProduct));
	}

	public ResponseEntity<?> updateProduct(String productId, String interactionid,
			String jsessionid, SwaggerProduct swaggerProduct) throws ItorixException {
		return ResponseEntity.ok(swaggerBusiness.updateProduct(swaggerProduct, productId));
	}

	@Override
	public ResponseEntity<?> deleteProduct(String productId, String interactionid,
			String jsessionid) throws ItorixException {
		return ResponseEntity.status(HttpStatus.NO_CONTENT)
				.body(swaggerBusiness.deleteProduct(productId));
	}

	@Override
	public ResponseEntity<?> getProductGroups(String interactionid, String jsessionid)
			throws ItorixException {
		return ResponseEntity.ok(swaggerBusiness.getProductGroups(interactionid, jsessionid));
	}

	/**
	 * @param interactionid
	 * @param jsessionid
	 * @param partnerIds
	 * @return
	 * @throws ItorixException
	 */
	@Override
	public ResponseEntity<?> getProductGroupsByPartnerIds(String interactionid, String jsessionid,
			Map<String, String> partnerIds) throws ItorixException {
		List<String> partners = partnerIds.get("partnerIds") != null && !partnerIds.get("partnerIds").isEmpty() ? Arrays.asList(
				partnerIds.get("partnerIds").split(",")) : Collections.emptyList();
		logger.info("getProductGroupsByPartnerIds");
		return ResponseEntity.ok().body(swaggerBusiness.getProductGroupsByPartnerIds(partners));
	}

	@Override
	public ResponseEntity<?> manageSwaggerProducts(String swaggerId,
			String interactionid,
			String jsessionid, String oas, AsociateSwaggerProductRequest swaggerProductRequest)
			throws ItorixException {
		swaggerBusiness.manageSwaggerProducts(swaggerId, oas, swaggerProductRequest);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@Override
	public ResponseEntity<?> getSwaggerProducts(String swaggerId,
			String interactionid,
			String jsessionid, int offset, int pageSize, String oas) throws ItorixException {
		return ResponseEntity.ok(
				swaggerBusiness.getSwaggerProducts(swaggerId, oas, interactionid,
						jsessionid, offset, pageSize));
	}

	private void callScannerAPI(ScannerDTO scannerDTO, String jsessionid) throws ItorixException {
		for(String swaggerId : scannerDTO.getSwaggerId()){
			String executionEventId = swaggerBusiness.createExecutionEvent(swaggerId, scannerDTO.getOperation(), scannerDTO.getTenantId());
			log.info("Compliance Scanner Execution Id - {}", executionEventId);
			complianceScannerSqlDao.insertIntoComplianceExecutorEntity(scannerDTO.getTenantId(), executionEventId,
					ComplicanceScannerExecutorEntity.STATUSES.SCHEDULED.getValue(), null, scannerDTO.getOperation());
		}
		log.debug("Inserted {} events into postgres successfully", scannerDTO.getSwaggerId().size());
	}

	private void callLintingAPI(SwaggerLintingInfo swaggerLintingInfo, String jsessionid) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.set("jsessionid", jsessionid);
		HttpEntity<SwaggerLintingInfo> entity = new HttpEntity<>(swaggerLintingInfo, httpHeaders);

		try {
			restTemplate.exchange(lintingUrl+lintSwagger, HttpMethod.POST, entity, String.class).getBody();
		} catch (Exception e) {
			logger.error("Error while calling linting API {} ", e.getMessage());
		}

	}

	@ApiOperation(value = "Get Swagger Revison's", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = SwaggerVO.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v2/swaggers/stats")
	public ResponseEntity<Object> getSwaggerStatsV2(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas,
			@RequestParam(value = "timeunit", required = false) String timeunit,
			@RequestParam(value = "timerange", required = false) String timerange) throws Exception {
		SwaggerObjectResponse obj = null;
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
		if (oas.equals("2.0")) {
			return new ResponseEntity<Object>(swaggerBusiness.getSwaggerStatsV2(timeunit, timerange,jsessionid), HttpStatus.OK);
		} else if (oas.equals("3.0")) {
			obj = swaggerBusiness.getSwagger3Statsv2(timeunit, timerange,jsessionid);
		}
		return new ResponseEntity<Object>(obj, HttpStatus.OK);
	}

	@ApiOperation(value = "Get List Of Swagger Details", notes = "", code = 200)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = SwaggerVO.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal server error. Please contact support for further instructions.", response = ErrorObj.class)})
	@RequestMapping(method = RequestMethod.GET, value = "/v2/swaggers/history", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Object> getListOfSwaggerDetailsV2(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "oas", required = false) String oas,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "swagger", required = false) String swagger,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "product", required = false) String product,
			@RequestParam(value = "modifieddate", required = false) String modifiedDate,
			@RequestParam(value = "sortbymodifieddate", required = false) String sortByModifiedDate) throws Exception {
		if (oas == null || oas.trim().equals("")) {
			oas = "2.0";
		}
		String json = "";
		List<String> products = null;
		if (null != product) {
			products = Arrays.asList(product.split(","));
		}
		if (oas.equals("2.0")) {
			SwaggerHistoryResponse response;
			if (null != products) {
				response = swaggerBusiness.getSwaggerDetailsByproduct(products, interactionid, jsessionid, offset, oas,
						swagger, pageSize);
			} else {
				response = swaggerBusiness.getListOfSwaggerDetailsV2(status, modifiedDate, interactionid, jsessionid,
						offset, oas, swagger, pageSize, sortByModifiedDate);
			}
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			json = mapper.writeValueAsString(response);
		} else if (oas.equals("3.0")) {
			SwaggerHistoryResponse response;
			if (null != products) {
				response = swaggerBusiness.getSwaggerDetailsByproduct(products, interactionid, jsessionid, offset, oas,
						swagger, pageSize);
			} else {
				response = swaggerBusiness.getListOfSwagger3DetailsV2(status, modifiedDate, interactionid, jsessionid,
						offset, oas, swagger, pageSize, sortByModifiedDate);
			}
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			json = mapper.writeValueAsString(response);
		}
		return new ResponseEntity<Object>(json, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> checkSwaggerMetadata(String interactionid, String oas, String jsessionid, String swagger) throws Exception {
		return new ResponseEntity<>(swaggerBusiness.checkMetadataSwagger(oas, swagger), HttpStatus.OK);
	}
	@Override
	public ResponseEntity<Object> sync2Repo(String swaggerId, String revisionNo, String interactionid,
			String oas, String jsessionid, ScmUpload scmUpload) throws Exception {
		try {
			swaggerBusiness.sync2Repo(swaggerId, revisionNo, interactionid, oas, jsessionid, scmUpload);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} catch (Exception ex) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	@Override
	public ResponseEntity<Object> saveSwaggerScmDeatils(String swaggerId, String revisionNo, String interactionid,
			String oas, String jsessionid, ScmUpload scmUpload) throws Exception {
		try {
			swaggerBusiness.saveScmDetails(swaggerId, revisionNo, interactionid, oas, jsessionid, scmUpload);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} catch (Exception ex) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

  @Override
  public ResponseEntity<List<Document>> getListOfSwaggersForConnectors(String jsessionid, String oas){
    log.info("Getting swaggers list for connectors");
    return new ResponseEntity<>(swaggerBusiness.getSwaggersForConnectors(oas),HttpStatus.OK);
  }
}