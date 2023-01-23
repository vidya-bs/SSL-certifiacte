package com.itorix.apiwiz.design.studio.businessimpl;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.itorix.apiwiz.common.model.SearchItem;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.proxystudio.ProxyData;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.Date.DateUtil;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.common.util.mail.EmailTemplate;
import com.itorix.apiwiz.common.util.mail.MailUtil;
import com.itorix.apiwiz.common.util.scm.ScmUtilImpl;
import com.itorix.apiwiz.common.util.zip.ZIPUtil;
import com.itorix.apiwiz.design.studio.business.SwaggerBusiness;
import com.itorix.apiwiz.design.studio.model.*;
import com.itorix.apiwiz.design.studio.model.dto.MetadataErrorDTO;
import com.itorix.apiwiz.design.studio.model.swagger.sync.DictionarySwagger;
import com.itorix.apiwiz.design.studio.model.swagger.sync.DictionarySwagger.Status;
import com.itorix.apiwiz.design.studio.model.swagger.sync.SchemaInfo;
import com.itorix.apiwiz.design.studio.model.swagger.sync.SwaggerData;
import com.itorix.apiwiz.design.studio.model.swagger.sync.SwaggerDictionary;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.model.Pagination;
import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContextHolder;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.DeleteResult;
import io.swagger.generator.util.SwaggerUtil;
import io.swagger.models.*;
import io.swagger.parser.OpenAPIParser;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariables;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSON;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpMethod;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjuster;
import java.util.*;
import java.util.stream.Collectors;

import static com.itorix.apiwiz.identitymanagement.model.AbstractObject.LABEL_CREATED_TIME;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * The type Swagger business.
 */
@Service
@Slf4j
public class SwaggerBusinessImpl implements SwaggerBusiness {

	private static final Logger logger = LoggerFactory.getLogger(SwaggerBusinessImpl.class);
	/**
	 * The Base repository.
	 */
	@Autowired
	BaseRepository baseRepository;
	/**
	 * The Application properties.
	 */
	@Autowired
	ApplicationProperties applicationProperties;
	@Autowired
	private MongoTemplate mongoTemplate;

	@Qualifier("masterMongoTemplate")
	@Autowired
	private MongoTemplate masterMongoTemplate;

	@Autowired
	private MailUtil mailUtil;
	@Autowired
	private ScmUtilImpl scmImpl;

	@Autowired
	private ApicUtil apicUtil;

	@Autowired
	private RestTemplate restTemplate;

	private static final String STATUS_VALUE = "status";

	private static final String PUBLISH_STATUS = "Publish";

	public SwaggerVO createSwagger(SwaggerVO swaggerVO) {
		log("createSwagger", swaggerVO.getInteractionid(), swaggerVO);
		swaggerVO.setRevision(1);
		swaggerVO.setStatus("Draft");
		swaggerVO.setLock(false);
		swaggerVO.setId(null);
		swaggerVO.setSwaggerId(UUID.randomUUID().toString().replaceAll("-", ""));

		try {
			Swagger swagger = convertToSwagger(swaggerVO.getSwagger());
			if (swagger.getVendorExtensions() != null
					&& swagger.getVendorExtensions().get("x-ibm-configuration") != null) {
				swaggerVO.setSwagger(apicUtil.getPolicyTemplates(swaggerVO.getSwagger()));
			}

		} catch (Exception e) {
			log.error("Exception occurred", e);
		}

		SwaggerVO details = baseRepository.save(swaggerVO);
		log("createSwagger", swaggerVO.getInteractionid(), details);
		return details;
	}

	public Swagger3VO createSwagger(Swagger3VO swaggerVO) {
		log("createSwagger", swaggerVO.getInteractionid(), swaggerVO);
		swaggerVO.setRevision(1);
		swaggerVO.setStatus("Draft");
		swaggerVO.setLock(false);
		swaggerVO.setId(null);
		swaggerVO.setSwaggerId(UUID.randomUUID().toString().replaceAll("-", ""));
		Swagger3VO details = baseRepository.save(swaggerVO);
		log("createSwagger", swaggerVO.getInteractionid(), details);
		return details;
	}

	/**
	 * Save swagger boolean.
	 *
	 * @param swaggerVO the swagger vo
	 * @return the boolean
	 * @throws ItorixException the itorix exception
	 */
	public boolean saveSwagger(SwaggerVO swaggerVO) throws ItorixException {
		SwaggerVO vo = findSwagger(swaggerVO);
		if (vo != null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1002"), swaggerVO.getName()),
					"Swagger-1002");
		}
		swaggerVO.setRevision(1);
		swaggerVO.setStatus("Draft");
		swaggerVO.setLock(false);
		swaggerVO.setId(null);
		swaggerVO.setSwaggerId(UUID.randomUUID().toString().replaceAll("-", ""));
		baseRepository.save(swaggerVO);
		return true;
	}

	/**
	 * Save swagger boolean.
	 *
	 * @param swaggerVO the swagger vo
	 * @return the boolean
	 * @throws ItorixException the itorix exception
	 */
	public boolean saveSwagger(Swagger3VO swaggerVO) throws ItorixException {
		Swagger3VO vo = findSwagger(swaggerVO);
		if (vo != null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1002"), swaggerVO.getName()),
					"Swagger-1002");
		}
		swaggerVO.setRevision(1);
		swaggerVO.setStatus("Draft");
		swaggerVO.setLock(false);
		swaggerVO.setId(null);
		swaggerVO.setSwaggerId(UUID.randomUUID().toString().replaceAll("-", ""));
		baseRepository.save(swaggerVO);
		return true;
	}

	private void raiseException(List<String> fileds) throws ItorixException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			String message = mapper.writeValueAsString(fileds);
			message = message.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\"", "").replaceAll(",", ", ");
			message = "Invalid request data! Missing mandatory data: " + message;
			throw new ItorixException(message, "General-1001");
		} catch (Exception e) {
			throw new ItorixException(ErrorCodes.errorMessage.get("General-1001"), "General-1001");
		}
	}

	public List<SwaggerImport> importSwaggers(MultipartFile zipFile, String type, String gitURI, String branch,
			String authType, String userName, String password, String personalToken, String oas) throws Exception {
		RSAEncryption rsaEncryption = new RSAEncryption();
		String fileLocation = null;
		ZIPUtil unZip = new ZIPUtil();
		List<String> missingFields = new ArrayList<>();
		if (type == null || type.isEmpty()) {
			throw new ItorixException("Invalid request data! Missing mandatory field: type", "General-1001");
		}
		if (type.equals("git")) {
			if (gitURI == null || gitURI.isEmpty()) {
				missingFields.add("gitURI");
			}
			if (branch == null || branch.isEmpty()) {
				missingFields.add("branch");
			}
			if (authType == null || authType.isEmpty()) {
				missingFields.add("authType");
			}
			if (authType != null && !authType.isEmpty() && authType.equalsIgnoreCase("basic")) {
				if (userName == null || userName.isEmpty()) {
					missingFields.add("userName");
				}
				if (password == null || password.isEmpty()) {
					missingFields.add("password");
				}
			} else if (personalToken == null || personalToken.isEmpty()) {
				missingFields.add("personalToken");
			}
		} else if (type.equals("file") && zipFile == null) {
			missingFields.add("zipFile");
		}
		if (missingFields.size() > 0) {
			raiseException(missingFields);
		}
		if (type.equals("git")) {
			if (authType.equalsIgnoreCase("basic")) {
				fileLocation = scmImpl.cloneRepo(gitURI, branch, rsaEncryption.decryptText(userName),
						rsaEncryption.decryptText(password));
			} else if (authType != null) {
				fileLocation = scmImpl.cloneRepoBasedOnAuthToken(gitURI, branch,
						rsaEncryption.decryptText(personalToken));
			}
		} else if (type.equals("file")) {
			try {
				fileLocation = applicationProperties.getTempDir() + "swaggerImport";
				String file = applicationProperties.getTempDir() + zipFile.getOriginalFilename();
				File targetFile = new File(file);
				File cloningDirectory = new File(fileLocation);
				cloningDirectory.mkdirs();
				zipFile.transferTo(targetFile);
				unZip.unzip(file, fileLocation);
			} catch (Exception e) {
				log.error("Exception occurred : {}", e.getMessage());
			}
		} else {
			String message = "Invalid request data! Invalid type provided supported values - git, file";
			throw new ItorixException(message, "General-1001");
		}
		List<File> files = unZip.getJsonFiles(fileLocation);
		if (files.isEmpty()) {
			String message = "Invalid request data! Invalid file type";
			throw new ItorixException(message, "General-1001");
		} else {
			List<SwaggerImport> listSwaggers = new ArrayList<SwaggerImport>();
			try {
				listSwaggers = importSwaggersFromFiles(files, oas);
			} catch (Exception e) {
				throw new ItorixException(e.getMessage(), "General-1000");
			} finally {
				FileUtils.cleanDirectory(new File(fileLocation));
				FileUtils.deleteDirectory(new File(fileLocation));
			}
			return listSwaggers;
		}
	}

	private List<SwaggerImport> importSwaggersFromFiles(List<File> files, String oas) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		List<SwaggerImport> listSwaggers = new ArrayList<SwaggerImport>();
		String fileList = new String();
		String message = "Swagger Version doesn't match : ";
		for (File file : files) {
			String filecontent = new String();
			if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("yaml")) {
				filecontent = convertYamlToJson(FileUtils.readFileToString(file));
			} else {
				filecontent = FileUtils.readFileToString(file);
			}

			JsonNode swaggerObject = mapper.readTree(filecontent);
			String version = getVersion(swaggerObject);
			boolean isVersion2 = false;
			if (version != null && (version.startsWith("\"1") || version.startsWith("1"))) {
				isVersion2 = true;
			} else if (version != null && (version.startsWith("\"2") || version.startsWith("2"))) {
				isVersion2 = true;
			}
			if (isVersion2) {
				if (oas != null && !StringUtils.equalsIgnoreCase("2.0", oas)) {
					if (!fileList.isEmpty())
						fileList = fileList.concat("," + file.getName());
					else
						fileList = fileList.concat(file.getName());
				}
			} else {
				if (oas != null && !StringUtils.equalsIgnoreCase("3.0", oas)) {
					if (!fileList.isEmpty())
						fileList = fileList.concat(", " + file.getName());
					else
						fileList = fileList.concat(file.getName());
				}
			}
		}
		if (!fileList.isEmpty()) {
			throw new ItorixException(message + fileList, "General-1000");
		}
		for (File file : files) {
			try {
				String filecontent;
				if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("yaml")) {
					filecontent = convertYamlToJson(FileUtils.readFileToString(file));
				} else {
					filecontent = FileUtils.readFileToString(file);
				}
				JsonNode swaggerObject = mapper.readTree(filecontent);
				boolean isVersion2 = false;
				String version = getVersion(swaggerObject);
				if (version != null && (version.startsWith("\"1") || version.startsWith("1"))) {
					isVersion2 = true;
				} else if (version != null && (version.startsWith("\"2") || version.startsWith("2"))) {
					isVersion2 = true;
				}
				if (isVersion2) {
					String reason = null;
					SwaggerImport swagger = new SwaggerImport();
					try {
						String basepath = swaggerObject.path("basePath").asText();
						List<Swagger2BasePath> mappings = getSwagger2BasePaths();
						for (int i = 0; i < mappings.size(); i++) {
							if (mappings.get(i).getBasePath().equals(basepath)) {
								throw new ItorixException(ErrorCodes.errorMessage.get("Swagger-1000"),
										"Swagger-1000");
							}
						}
					JsonNode info = swaggerObject.path("info");
					if (info != null) {
						String swaggerName = info.get("title").asText();
						SwaggerVO swaggerVO = new SwaggerVO();
						swaggerVO.setName(swaggerName);
						swaggerVO.setSwagger(filecontent);
						swagger.setLoaded(false);
						swagger.setName(swaggerName);
						swagger.setPath(file.getAbsolutePath());
						try {
							saveSwagger(swaggerVO);
							updateSwaggerBasePath(swaggerName, swaggerVO);
							swagger.setSwaggerId(swaggerVO.getSwaggerId());
							swagger.setLoaded(true);
							reason = "swagger loaded";
						} catch (ItorixException e) {
							if (e.errorCode.equals("Swagger-1002")) {
								reason = "swagger with same name exists";
								createSwaggerWithNewRevision(swaggerVO, null);
							}
							swagger.setReason(reason);
						}
						listSwaggers.add(swagger);
					} else {
						new ItorixException("invalid JSON file");
					}
					} catch (Exception e) {
						reason = "swagger with same basePath exists";
						swagger.setName(file.getName());
						swagger.setLoaded(false);
						swagger.setReason(reason);
						listSwaggers.add(swagger);
					}

				} else {
					String reason = null;
					SwaggerImport swagger = new SwaggerImport();
					try {
						SwaggerParseResult swaggerParseResult = new OpenAPIParser().readContents(swaggerObject.toString(), null, null);
						List<Server> servers = swaggerParseResult.getOpenAPI().getServers();
						Set<String> basePaths = new HashSet();
						for (Server server : servers) {
							String urlStr = getReplacedURLStr(server);
							try {
								URL url = new URL(urlStr);
								basePaths.add(url.getPath());
							} catch (MalformedURLException e) {
								logger.error("Error while getting basePath for Swagger: {} URL {} ", e.getMessage());
							}
						}
						//String basepath = swaggerObject.path("basePath").asText();
						List<Swagger3BasePath> mappings = getSwagger3BasePaths() ;
						for (int i = 0; i < mappings.size(); i++) {
							for(int j=0;j<mappings.get(i).getBasePath().size();j++) {
								if(basePaths.contains(mappings.get(i).getBasePath().get(j))) {
									throw new ItorixException(ErrorCodes.errorMessage.get("Swagger-1000"),
											"Swagger-1000");
								}
							}
						}
					JsonNode info = swaggerObject.path("info");
					if (info != null) {
						String swaggerName = info.get("title").asText();
						Swagger3VO swaggerVO = new Swagger3VO();
						swaggerVO.setName(swaggerName);
						swaggerVO.setSwagger(filecontent);
						swagger.setLoaded(false);
						swagger.setName(swaggerName);
						swagger.setPath(file.getAbsolutePath());
						try {
							updateSwagger3BasePath(swaggerName,swaggerVO);
							saveSwagger(swaggerVO);
							swagger.setSwaggerId(swaggerVO.getSwaggerId());
							swagger.setLoaded(true);
						} catch (ItorixException e) {
							if (e.errorCode.equals("Swagger-1002")) {
								reason = "swagger with same name exists";
								createSwaggerWithNewRevision(swaggerVO, null);
							}
							swagger.setReason(reason);
						}
						listSwaggers.add(swagger);
					} else {
						new ItorixException("invalid JSON file");
					}
				} catch (Exception e) {
					reason = "swagger with same basePath exists";
					swagger.setName(file.getName());
					swagger.setLoaded(false);
					swagger.setReason(reason);
					listSwaggers.add(swagger);
				}
				}
			} catch (Exception e) {
				SwaggerImport swagger = new SwaggerImport();
				swagger.setLoaded(false);
				swagger.setName(file.getName());
				swagger.setReason("invalid JSON file");
				listSwaggers.add(swagger);
				log.error("Exception occurred", e);
			}
		}
		return listSwaggers;
	}

	private String getVersion(JsonNode node) {
		try {
			if (node == null) {
				return null;
			}
			JsonNode version = node.get("openapi");
			if (version != null) {
				return version.toString();
			}
			version = node.get("swagger");
			if (version != null) {
				return version.toString();
			}
			version = node.get("swaggerVersion");
			if (version != null) {
				return version.toString();
			}
		} catch (Exception ex) {
			log.error("Exception occurred", ex);
		}
		return null;
	}

	private String convertYamlToJson(String yaml) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
		Object obj = yamlReader.readValue(yaml, Object.class);
		ObjectMapper jsonWriter = new ObjectMapper();
		return jsonWriter.writeValueAsString(obj);
	}

	public SwaggerVO findSwagger(SwaggerVO swaggerVO) {
		log("findSwagger", swaggerVO.getInteractionid(), swaggerVO);
		return baseRepository.findOne("name", swaggerVO.getName(), SwaggerVO.class);
	}

	public Swagger3VO findSwagger(Swagger3VO swaggerVO) {
		log("findSwagger", swaggerVO.getInteractionid(), swaggerVO);
		return baseRepository.findOne("name", swaggerVO.getName(), Swagger3VO.class);
	}

	public SwaggerVO findSwagger(String name, String interactionid) throws ItorixException {
		log("findSwagger", interactionid, name);
		SwaggerVO vo = baseRepository.findOne("id", name, SwaggerVO.class);
		if (vo == null) {
			vo = baseRepository.findOne("swaggerId", name, SwaggerVO.class);
		}
		if (vo == null) {
			vo = baseRepository.findOne("name", name, SwaggerVO.class);
		}
		return vo;
	}

	public Swagger3VO findSwagger3(String name, String interactionid) throws ItorixException {
		log("findSwagger", interactionid, name);
		Swagger3VO vo = baseRepository.findOne("id", name, Swagger3VO.class);
		if (vo == null) {
			vo = baseRepository.findOne("swaggerId", name, Swagger3VO.class);
		}
		if (vo == null) {
			vo = baseRepository.findOne("name", name, Swagger3VO.class);
		}
		return vo;
	}

	public SwaggerVO createSwaggerWithNewRevision(SwaggerVO swaggerVO, String jsessionid) throws ItorixException {
		log("createSwaggerWithNewRevision", swaggerVO.getInteractionid(), swaggerVO);
		SwaggerVO vo = findSwagger(swaggerVO.getName(), swaggerVO.getInteractionid());
		if (vo != null) {
			swaggerVO.setName(vo.getName());
		}
		List<Revision> revisions = getListOfRevisions(swaggerVO.getName(), swaggerVO.getInteractionid());
		if (revisions != null && revisions.size() > 0) {
			Revision revision = Collections.max(revisions);
			Integer newRevision = revision.getRevision() + 1;
			swaggerVO.setRevision(newRevision);
				swaggerVO.setStatus("Draft");
			swaggerVO.setLock(false);
			swaggerVO.setId(null);
			swaggerVO.setSwaggerId(vo.getSwaggerId());
			SwaggerVO details = baseRepository.save(swaggerVO);
			log("createSwaggerWithNewRevision", swaggerVO.getInteractionid(), details);
			return details;
		}
		return null;
	}

	public Swagger3VO createSwaggerWithNewRevision(Swagger3VO swaggerVO, String jsessionid) throws ItorixException {
		log("createSwaggerWithNewRevision", swaggerVO.getInteractionid(), swaggerVO);
		Swagger3VO vo = findSwagger3(swaggerVO.getName(), swaggerVO.getInteractionid());
		if (vo != null) {
			swaggerVO.setName(vo.getName());
		}
		List<Revision> revisions = getListOf3Revisions(swaggerVO.getName(), swaggerVO.getInteractionid());
		if (revisions != null && revisions.size() > 0) {
			Revision revision = Collections.max(revisions);
			Integer newRevision = revision.getRevision() + 1;
			swaggerVO.setRevision(newRevision);
			swaggerVO.setStatus("Draft");
			swaggerVO.setLock(false);
			swaggerVO.setId(null);
			swaggerVO.setSwaggerId(vo.getSwaggerId());
			Swagger3VO details = baseRepository.save(swaggerVO);
			log("createSwaggerWithNewRevision", swaggerVO.getInteractionid(), details);
			return details;
		}
		return null;
	}

	public SwaggerVO updateSwagger(SwaggerVO vo) {
		log("updateSwagger", vo.getInteractionid(), vo);
		SwaggerVO details = baseRepository.save(vo);
		updateSwaggerBasePath(details.getName(), details);
		log("updateSwagger", vo.getInteractionid(), details);
		return details;
	}

	public Swagger3VO updateSwagger(Swagger3VO vo) {
		log("updateSwagger", vo.getInteractionid(), vo);
		Swagger3VO details = baseRepository.save(vo);
		updateSwagger3BasePath(details.getName(), details);
		log("updateSwagger", vo.getInteractionid(), details);
		return details;
	}

	public SwaggerVO findSwagger(SwaggerVO swaggerVO, Integer revision) {
		log("findSwagger", swaggerVO.getInteractionid(), swaggerVO, revision);
		return baseRepository.findOne("name", swaggerVO.getName(), "revision", revision, SwaggerVO.class);
	}

	public Swagger3VO findSwagger(Swagger3VO swaggerVO, Integer revision) {
		log("findSwagger", swaggerVO.getInteractionid(), swaggerVO, revision);
		return baseRepository.findOne("name", swaggerVO.getName(), "revision", revision, Swagger3VO.class);
	}

	public SwaggerVO findSwagger(String swaggername, Integer revision, String interactionid) {
		// log("findSwagger", interactionid, swaggername, revision);
		return baseRepository.findOne("name", swaggername, "revision", revision, SwaggerVO.class);
	}

	public List<Revision> getListOfRevisions(String name, String interactionid) {
		// log("getListOfRevisions", interactionid, name);
		List<SwaggerVO> swaggers;
		swaggers = baseRepository.find("id", name, SwaggerVO.class);
		if (swaggers != null && swaggers.size() > 0) {
			name = swaggers.get(0).getName();
		}
		swaggers = baseRepository.find("name", name, SwaggerVO.class);
		if(swaggers.isEmpty()) {
			swaggers = baseRepository.find("swaggerId", name, SwaggerVO.class);
		}
		List<Revision> versions = new ArrayList<Revision>();
		for (SwaggerVO swagger : swaggers) {
			Revision version = new Revision();
			version.setRevision(swagger.getRevision());
			version.setStatus(swagger.getStatus());
			version.setId(swagger.getSwaggerId() != null ? swagger.getSwaggerId() : swagger.getId());
			versions.add(version);
		}
		// log("getListOfRevisions", interactionid, versions);
		return versions;
	}

	public List<Revision> getListOf3Revisions(String name, String interactionid) {
		// log("getListOfRevisions", interactionid, name);
		List<Swagger3VO> swaggers;
		swaggers = baseRepository.find("id", name, Swagger3VO.class);
		if (swaggers != null && swaggers.size() > 0) {
			name = swaggers.get(0).getName();
		}
		swaggers = baseRepository.find("name", name, Swagger3VO.class);
		if(swaggers.isEmpty()) {
			swaggers = baseRepository.find("swaggerId", name, Swagger3VO.class);
		}
		List<Revision> versions = new ArrayList<Revision>();
		for (Swagger3VO swagger : swaggers) {
			Revision version = new Revision();
			version.setRevision(swagger.getRevision());
			version.setStatus(swagger.getStatus());
			version.setId(swagger.getSwaggerId() != null ? swagger.getSwaggerId() : swagger.getId());
			versions.add(version);
		}
		// log("getListOfRevisions", interactionid, versions);
		return versions;
	}

	/**
	 * Gets list of revisions.
	 *
	 * @param name          the name
	 * @param status        the status
	 * @param interactionid the interactionid
	 * @return the list of revisions
	 */
	public List<Revision> getListOfRevisions(String name, String status, String interactionid) {
		// log("getListOfRevisions", interactionid, name);
		List<SwaggerVO> swaggers = mongoTemplate
				.find(new Query(Criteria.where("name").is(name).and(STATUS_VALUE).is(status)), SwaggerVO.class);
		if(swaggers.isEmpty()){
			swaggers = mongoTemplate.find(new Query(Criteria.where("swaggerId").is(name).and(STATUS_VALUE).is(status)), SwaggerVO.class);
		}
		// baseRepository.find("name", name, SwaggerVO.class);
		List<Revision> versions = new ArrayList<Revision>();
		if (swaggers != null && swaggers.size() > 0) {
			for (SwaggerVO swagger : swaggers) {
				Revision version = new Revision();
				version.setRevision(swagger.getRevision());
				version.setStatus(swagger.getStatus());
				version.setId(swagger.getId());
				versions.add(version);
				// versions.add(new Revision(swagger.getRevision(),
				// swagger.getStatus()));
			}
		}
		log("getListOfRevisions", interactionid, versions);
		return versions;
	}


	public List<Revision> getListOf3Revisions(String name, String status, String interactionid) {
		// log("getListOfRevisions", interactionid, name);
		List<Swagger3VO> swaggers = mongoTemplate
				.find(new Query(Criteria.where("name").is(name).and(STATUS_VALUE).is(status)), Swagger3VO.class);
		if(swaggers.isEmpty()){
			swaggers = mongoTemplate.find(new Query(Criteria.where("swaggerId").is(name).and(STATUS_VALUE).is(status)), Swagger3VO.class);
		}
		// baseRepository.find("name", name, SwaggerVO.class);
		List<Revision> versions = new ArrayList<Revision>();
		if (swaggers != null && swaggers.size() > 0) {
			for (Swagger3VO swagger : swaggers) {
				Revision version = new Revision();
				version.setRevision(swagger.getRevision());
				version.setStatus(swagger.getStatus());
				version.setId(swagger.getId());
				versions.add(version);
				// versions.add(new Revision(swagger.getRevision(),
				// swagger.getStatus()));
			}
		}
		log("getListOfRevisions", interactionid, versions);
		return versions;
	}
	public List<Revision> getListOfSwagger3Revisions(String name, String interactionid) {
		log("getListOfRevisions", interactionid, name);
		List<Swagger3VO> swaggers = baseRepository.find("name", name, Swagger3VO.class);
		List<Revision> versions = new ArrayList<Revision>();
		for (Swagger3VO swagger : swaggers) {
			Revision version = new Revision();
			version.setRevision(swagger.getRevision());
			version.setStatus(swagger.getStatus());
			version.setId(swagger.getId());
			versions.add(version);
		}
		log("getListOfRevisions", interactionid, versions);
		return versions;
	}

	public List<String> getListOfSwaggerNames(String interactionid) throws ItorixException {
		log("getListOfSwaggerNames", interactionid, "");
		List<String> names = baseRepository.findDistinctValuesByColumnName(SwaggerVO.class, "name");
		log("getListOfSwaggerNames", interactionid, names);
		return names;
	}

	public List<SwaggerVO> getSwaggerNames(String page,String jsessionId) throws ItorixException {
		List<SwaggerVO> swaggerNames = new ArrayList<SwaggerVO>();

		List<String> names = new ArrayList<>();

		if (StringUtils.equalsIgnoreCase("Proxy", page) || StringUtils.equalsIgnoreCase("Kong", page)) {
			Query postReleaseQuery = new Query(
					Criteria.where("status").in(Arrays.asList("Approved", "Publish")));
			names = mongoTemplate.findDistinct(postReleaseQuery,"name", SwaggerVO.class, String.class);
			 retrieveSwaggerNames(swaggerNames, names);
		}
		if (StringUtils.equalsIgnoreCase("Virtualisation", page) || StringUtils.equalsIgnoreCase(
				"TestSuite", page)) {
			Query preReleaseQuery = new Query(
					Criteria.where("status").not().in(Arrays.asList("Deprecate", "Retired")));
			names = mongoTemplate.findDistinct(preReleaseQuery,"name", SwaggerVO.class, String.class);
			 retrieveSwaggerNames(swaggerNames, names);
		} else {
			names = baseRepository.findDistinctValuesByColumnName(SwaggerVO.class, "name");
			 retrieveSwaggerNames(swaggerNames, names);
		}
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User user = getUserDetailsFromSessionID(jsessionId);
		boolean isAdmin = false;
		if (user != null && userSessionToken != null){
			isAdmin = user.isWorkspaceAdmin(userSessionToken.getWorkspaceId());
		}
		if (isAdmin){
			return swaggerNames;
		}
		List<SwaggerVO> responseList = new ArrayList<SwaggerVO>();
		Map<String, Set<String>> swaggerRoles = getSwaggerPermissions("2.0", user);
		Set<String> allSwaggers = new HashSet<>();
		allSwaggers.addAll(swaggerRoles.keySet());
		Map<String, Object> filterFieldsAndValues = new HashMap<>();
		filterFieldsAndValues.put("createdBy", user.getId());
		List<String> trimList = baseRepository.filterAndGroupBySwaggerName(filterFieldsAndValues,
				SwaggerVO.class, null);
		allSwaggers.addAll(trimList);

		for(String i:allSwaggers){
			SwaggerVO found = baseRepository.findOne("name", i, SwaggerVO.class);
			if(found!=null){
				SwaggerVO swaggerVO = new SwaggerVO();
				swaggerVO.setName(found.getName());
				swaggerVO.setId(found.getId());
				responseList.add(swaggerVO);
			}
		}
		return responseList;
	}

	private List<SwaggerVO> retrieveSwaggerNames(List<SwaggerVO> swaggerNames, List<String> names) {
		for (String name : names) {
			SwaggerVO swaggerVO = baseRepository.findOne("name", name, SwaggerVO.class);
			SwaggerVO swagger = new SwaggerVO();
			swagger.setId(swaggerVO.getSwaggerId());
			swagger.setName(swaggerVO.getName());
			swagger.setDescription(swaggerVO.getDescription());
			swaggerNames.add(swagger);
		}
		return swaggerNames;
	}

	public List<Swagger3VO> getSwagger3Names(String page,String jsessionId) throws ItorixException {
		List<Swagger3VO> swaggerNames = new ArrayList<Swagger3VO>();
		List<String> names = new ArrayList<>();

		if (StringUtils.equalsIgnoreCase("Proxy", page) || StringUtils.equalsIgnoreCase("Kong", page)) {
			Query postReleaseQuery = new Query(
					Criteria.where("status").in(Arrays.asList("Approved", "Publish")));
			names = mongoTemplate.findDistinct(postReleaseQuery,"name", Swagger3VO.class, String.class);
			 retrieveSwagger3Names(swaggerNames, names);
		}
		if (StringUtils.equalsIgnoreCase("Virtualisation", page) || StringUtils.equalsIgnoreCase(
				"TestSuite", page)) {
			Query preReleaseQuery = new Query(
					Criteria.where("status").not().in(Arrays.asList("Deprecate", "Retired")));
			names = mongoTemplate.findDistinct(preReleaseQuery,"name", Swagger3VO.class, String.class);
			 retrieveSwagger3Names(swaggerNames, names);
		} else {
			names = baseRepository.findDistinctValuesByColumnName(Swagger3VO.class, "name");
			 retrieveSwagger3Names(swaggerNames, names);
		}

		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User user = getUserDetailsFromSessionID(jsessionId);
		boolean isAdmin = false;
		if (user != null && userSessionToken != null){
			isAdmin = user.isWorkspaceAdmin(userSessionToken.getWorkspaceId());
		}
		if (isAdmin){
			return swaggerNames;
		}
		List<Swagger3VO> responseList = new ArrayList<Swagger3VO>();
		Map<String, Set<String>> swaggerRoles = getSwaggerPermissions("3.0", user);
		Set<String> allSwaggers = new HashSet<>();
		allSwaggers.addAll(swaggerRoles.keySet());
		Map<String, Object> filterFieldsAndValues = new HashMap<>();
		filterFieldsAndValues.put("createdBy", user.getId());
		List<String> trimList = baseRepository.filterAndGroupBySwaggerName(filterFieldsAndValues,
				Swagger3VO.class, null);
		allSwaggers.addAll(trimList);

		for(String i:allSwaggers){
			Swagger3VO found = baseRepository.findOne("name", i, Swagger3VO.class);
			if(found!=null){
				Swagger3VO swagger3VO = new Swagger3VO();
				swagger3VO.setName(found.getName());
				swagger3VO.setId(found.getId());
				responseList.add(swagger3VO);
			}
		}
		return responseList;
	}

	private List<Swagger3VO> retrieveSwagger3Names(List<Swagger3VO> swaggerNames, List<String> names) {
		for (String name : names) {
			Swagger3VO swaggerVO = baseRepository.findOne("name", name, Swagger3VO.class);
			Swagger3VO swagger = new Swagger3VO();
			swagger.setId(swaggerVO.getSwaggerId());
			swagger.setName(swaggerVO.getName());
			swagger.setDescription(swaggerVO.getDescription());
			swaggerNames.add(swagger);
		}
		return swaggerNames;
	}

	public List<String> getListOfSwagger3Names(String interactionid) throws ItorixException {
		log("getListOfSwagger3Names", interactionid, "");
		List<String> names = baseRepository.findDistinctValuesByColumnName(Swagger3VO.class, "name");
		log("getListOfSwagger3Names", interactionid, names);
		return names;
	}

	private void getSwaggers() {
		Aggregation aggregation = newAggregation(Aggregation.match(Criteria.where(STATUS_VALUE).is("Draft")),
				Aggregation.group("name").last("mts").as("mts").max("revision").as("revision"),
				Aggregation.sort(Sort.Direction.DESC, "mts"));
		AggregationResults<Object> result = mongoTemplate.aggregate(aggregation, SwaggerVO.class, Object.class);
		List<Object> swaggers = result.getMappedResults();
		int size = swaggers.size();
	}

	private List<String> getList(DistinctIterable<String> iterable) {
		MongoCursor<String> cursor = iterable.iterator();
		List<String> list = new ArrayList<>();
		while (cursor.hasNext()) {
			list.add(cursor.next());
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public SwaggerHistoryResponse getListOfSwaggerDetails(String status, String modifiedDate, String interactionid,
			String jsessionid, int offset, String oas, String swagger, int pageSize, String sortByModifiedDate)
			throws ItorixException, JsonProcessingException, IOException {
		log("getListOfSwaggerDetails", interactionid, jsessionid);
		// getSwaggers();
		Map<String, Object> filterFieldsAndValues = new HashMap<>();
		filterFieldsAndValues.put(STATUS_VALUE, status);
		filterFieldsAndValues.put("modified_date", modifiedDate);

		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User user = getUserDetailsFromSessionID(jsessionid);
		boolean isAdmin = user.isWorkspaceAdmin(userSessionToken.getWorkspaceId());
		List<String> roles = user.getUserWorkspace(userSessionToken.getWorkspaceId()).getRoles();

		List<SwaggerVO> list = new ArrayList<SwaggerVO>();
		SwaggerHistoryResponse response = new SwaggerHistoryResponse();
		List<String> names = new ArrayList<String>();
		int total = 1;
		if (swagger == null) {
			names = baseRepository.filterAndGroupBySwaggerName(filterFieldsAndValues, SwaggerVO.class,
					sortByModifiedDate);
			total = names.size();
			names = trimList(names, offset, pageSize);
		} else {
			try {
				SwaggerVO swaggervo = getSwagger(swagger, interactionid);
				if (swaggervo != null) {
					swagger = swaggervo.getName();
					names.add(swaggervo.getName());
				} else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Swagger-1000"), "Swagger-1000");
				}
			} catch (Exception e) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Swagger-1000"), "Swagger-1000");
			}
		}
		if (isAdmin) {
			for (String name : names) {
				List<Revision> versions;
				if (status != null && (!status.equals(""))) {
					versions = getListOfRevisions(name, status, interactionid);
				} else {
					versions = getListOfRevisions(name, interactionid);
				}
				SwaggerVO vo = null;
				if (versions != null && versions.size() > 0) {
					Revision revision = Collections.max(versions);
					vo = baseRepository.findOne("name", name, "revision", revision.getRevision(), SwaggerVO.class);
					SwaggerMetadata swaggerMetadata = getSwaggerMetadata(name, oas);
					if (swaggerMetadata != null) {
						vo.setTeams(swaggerMetadata.getTeams());
					}
				}
				if (vo != null) {
					ObjectMapper mapper = new ObjectMapper();
					JsonNode swaggerJson = mapper.readTree(vo.getSwagger());
					if (swaggerJson != null) {
						JsonNode infoNode = swaggerJson.get("info");
						if (infoNode != null) {
							try {
								vo.setDescription(infoNode.get("description").asText());
							} catch (Exception e) {
								vo.setDescription("N/A");
							}
						}
					}
					vo.setSwagger(null);
					roles = Arrays.asList("Admin", "Write", "Read");
					vo.setRoles(roles);
					list.add(vo);
				}
			}
			Pagination pagination = new Pagination();
			pagination.setOffset(offset);
			pagination.setTotal((long) total);
			pagination.setPageSize(pageSize);
			response.setPagination(pagination);
			response.setData(list);
		} else {
			Map<String, Set<String>> swaggerRoles = getSwaggerPermissions("2.0", user);
			Set<String> SwaggerNames = new HashSet<>();
			SwaggerNames.addAll(swaggerRoles.keySet());
			filterFieldsAndValues.put("createdBy", user.getId());
			List<String> trimList = trimList(baseRepository.filterAndGroupBySwaggerName(filterFieldsAndValues,
					SwaggerVO.class, sortByModifiedDate), offset, pageSize);
			SwaggerNames.addAll(trimList);
			if (swagger != null) {
				if (SwaggerNames.contains(swagger)) {
					SwaggerNames = new HashSet<>();
					SwaggerNames.add(swagger);
				} else {
					SwaggerNames = new HashSet<>();
				}
			}
			if (SwaggerNames != null) {
				names = trimList(new ArrayList<>(SwaggerNames), offset, pageSize);
				for (String name : names) {
					List<Revision> versions;
					if (status != null && (!status.equals(""))) {
						versions = getListOfRevisions(name, status, interactionid);
					} else {
						versions = getListOfRevisions(name, interactionid);
					}
					SwaggerVO vo = null;
					if (versions != null && versions.size() > 0) {
						Revision revision = Collections.max(versions);
						vo = baseRepository.findOne("name", name, "revision", revision.getRevision(), SwaggerVO.class);
						SwaggerMetadata swaggerMetadata = getSwaggerMetadata(name, oas);
						if (swaggerMetadata != null) {
							vo.setTeams(swaggerMetadata.getTeams());
						}
					}
					if (vo != null) {
						// if (vo.getTeams() == null || isMailidExist(vo,
						// user.getEmail())) {
						ObjectMapper mapper = new ObjectMapper();
						JsonNode swaggerJson = mapper.readTree(vo.getSwagger());
						if (swaggerJson != null) {
							JsonNode infoNode = swaggerJson.get("info");
							if (infoNode != null) {
								vo.setDescription(infoNode.get("description") == null
										? "N/A"
										: infoNode.get("description").asText());
							}
						}
						vo.setSwagger(null);
						vo.setRoles(new ArrayList<>(swaggerRoles.get(name) == null
								? Arrays.asList("Admin", "Write", "Read")
								: swaggerRoles.get(name))); // TODO
						// not
						// null
						list.add(vo);
					}
				}
				Pagination pagination = new Pagination();
				int totalByUser = SwaggerNames.size();
				pagination.setOffset(offset);
				pagination.setTotal((long) totalByUser);
				pagination.setPageSize(pageSize);
				response.setPagination(pagination);
				response.setData(list);
			}
		}
		log("getListOfSwaggerDetails", interactionid, list);
		return response;
	}

	public SwaggerHistoryResponse getSwaggerDetailsByproduct(List<String> products, String interactionid,
			String jsessionid, int offset, String oas, String swagger, int pageSize)
			throws ItorixException, JsonProcessingException, IOException {
		SwaggerHistoryResponse response = new SwaggerHistoryResponse();
		Query query = new Query(Criteria.where("products").in(products).and("oas").is(oas));
		List<SwaggerMetadata> metadataList = mongoTemplate.find(query, SwaggerMetadata.class);
		if (oas.equals("2.0")) {
			List<SwaggerVO> list = new ArrayList<SwaggerVO>();
			for (SwaggerMetadata swaggerMetadata : metadataList) {
				SwaggerVO vo = getSwaggerDetails(swaggerMetadata.getSwaggerName());
				vo.setTeams(swaggerMetadata.getTeams());
				list.add(vo);
			}
			response.setData(list);
		} else {
			List<Swagger3VO> list = new ArrayList<Swagger3VO>();
			for (SwaggerMetadata swaggerMetadata : metadataList) {
				Swagger3VO vo = getSwagger3Details(swaggerMetadata.getSwaggerName());
				vo.setTeams(swaggerMetadata.getTeams());
				list.add(vo);
			}
			response.setData(list);
		}
		return response;
	}

	private SwaggerVO getSwaggerDetails(String swagger) throws JsonMappingException, JsonProcessingException {
		List<Revision> versions = getListOfRevisions(swagger, null);
		SwaggerVO vo = null;
		if (versions != null && versions.size() > 0) {
			Revision revision = Collections.max(versions);
			vo = baseRepository.findOne("name", swagger, "revision", revision.getRevision(), SwaggerVO.class);
		}
		if (vo != null) {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode swaggerJson = mapper.readTree(vo.getSwagger());
			if (swaggerJson != null) {
				JsonNode infoNode = swaggerJson.get("info");
				if (infoNode != null) {
					try {
						vo.setDescription(infoNode.get("description").asText());
					} catch (Exception e) {
						vo.setDescription("N/A");
					}
				}
			}
			vo.setSwagger(null);
		}
		return vo;
	}

	private Swagger3VO getSwagger3Details(String swagger) throws JsonMappingException, JsonProcessingException {
		List<Revision> versions = getListOfSwagger3Revisions(swagger, null);
		Revision revision = Collections.max(versions);
		Swagger3VO vo = baseRepository.findOne("name", swagger, "revision", revision.getRevision(), Swagger3VO.class);
		if (vo != null) {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode swaggerJson = mapper.readTree(vo.getSwagger());
			if (swaggerJson != null) {
				JsonNode infoNode = swaggerJson.get("info");
				if (infoNode != null) {
					try {
						vo.setDescription(infoNode.get("description").asText());
					} catch (Exception e) {
						vo.setDescription("N/A");
					}
				}
			}
			vo.setSwagger(null);
		}
		return vo;
	}

	@SuppressWarnings("unchecked")
	public int getSwaggerCount(String status) {
		List<String> names;
		if (status != null) {
			Query query = new Query(new Criteria(STATUS_VALUE).is(status));
			names = getList(mongoTemplate.getCollection(mongoTemplate.getCollectionName(SwaggerVO.class))
					.distinct("name", query.getQueryObject(), String.class));
		} else {
			names = baseRepository.findDistinctValuesByColumnName(SwaggerVO.class, "name");
		}
		return names.size();
	}

	@SuppressWarnings("unchecked")
	public int getSwagger3Count(String status) {
		List<String> names;
		if (status != null) {
			names = getList(mongoTemplate.getCollection(mongoTemplate.getCollectionName(Swagger3VO.class))
					.distinct("name", new Query(new Criteria(STATUS_VALUE).is(status)).getQueryObject(), String.class));
		} else {
			names = baseRepository.findDistinctValuesByColumnName(Swagger3VO.class, "name");
		}
		return names.size();
	}

	private List<String> trimList(List<String> names, int offset, int pageSize) {
		List<String> swaggerNames = new ArrayList<String>();
		int i = offset > 0 ? ((offset - 1) * pageSize) : 0;
		int end = i + pageSize;
		for (; i < names.size() && i < end; i++) {
			swaggerNames.add(names.get(i));
		}
		return swaggerNames;
	}

	@SuppressWarnings("unchecked")
	public SwaggerHistoryResponse getListOfSwagger3Details(String status, String modifiedDate, String interactionid,
			String jsessionid, int offset, String oas, String swagger, int pageSize, String sortByModifiedDate)
			throws ItorixException, JsonProcessingException, IOException {
		log("getListOfSwaggerDetails", interactionid, jsessionid);
		Map<String, Object> filterFieldsAndValues = new HashMap<>();
		filterFieldsAndValues.put(STATUS_VALUE, status);
		filterFieldsAndValues.put("modified_date", modifiedDate);

		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User user = getUserDetailsFromSessionID(jsessionid);
		boolean isAdmin = user.isWorkspaceAdmin(userSessionToken.getWorkspaceId());
		List<String> roles = user.getUserWorkspace(userSessionToken.getWorkspaceId()).getRoles();

		List<Swagger3VO> list = new ArrayList<Swagger3VO>();
		SwaggerHistoryResponse response = new SwaggerHistoryResponse();
		List<String> names = new ArrayList<String>();
		int total = 1;
		if (swagger == null) {
			names = baseRepository.filterAndGroupBySwaggerName(filterFieldsAndValues, Swagger3VO.class,
					sortByModifiedDate);
			total = names.size();
			names = trimList(names, offset, pageSize);
		} else {
			try {
				Swagger3VO swaggervo = getSwagger3(swagger, interactionid);
				if (swaggervo != null) {
					swagger = swaggervo.getName();
					names.add(swaggervo.getName());
				} else {
					swagger = "";
					throw new ItorixException(ErrorCodes.errorMessage.get("Swagger-1000"), "Swagger-1000");
				}
			} catch (Exception e) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Swagger-1000"), "Swagger-1000");
			}
		}

		if (isAdmin) {
			for (String name : names) {
				List<Revision> versions = getListOfSwagger3Revisions(name, interactionid);
				Revision revision = Collections.max(versions);
				Swagger3VO vo = baseRepository.findOne("name", name, "revision", revision.getRevision(),
						Swagger3VO.class);
				SwaggerMetadata swaggerMetadata = getSwaggerMetadata(name, oas);
				if (swaggerMetadata != null) {
					vo.setTeams(swaggerMetadata.getTeams());
				}
				if (vo != null) {
					ObjectMapper mapper = new ObjectMapper();
					JsonNode swaggerJson = mapper.readTree(vo.getSwagger());
					if (swaggerJson != null) {
						JsonNode infoNode = swaggerJson.get("info");
						if (infoNode != null) {
							try {
								vo.setDescription(infoNode.get("description").asText());
							} catch (Exception e) {
								vo.setDescription("N/A");
							}
						}
					}
					vo.setSwagger(null);
					vo.setRoles(Arrays.asList("Admin", "Write", "Read"));
					list.add(vo);
				}
			}
			Pagination pagination = new Pagination();
			pagination.setOffset(offset);
			pagination.setTotal((long) total);
			pagination.setPageSize(pageSize);

			response.setPagination(pagination);
			response.setData(list);
		} else {
			filterFieldsAndValues.put("createdBy", user.getId());
			Map<String, Set<String>> swaggerRoles = getSwaggerPermissions("3.0", user);
			Set<String> SwaggerNames = new HashSet<>();
			SwaggerNames.addAll(swaggerRoles.keySet());
			List<String> trimList = trimList(baseRepository.filterAndGroupBySwaggerName(filterFieldsAndValues,
					Swagger3VO.class, sortByModifiedDate), offset, pageSize);
			SwaggerNames.addAll(trimList);
			if (swagger != null) {
				if (SwaggerNames.contains(swagger)) {
					SwaggerNames = new HashSet<>();
					SwaggerNames.add(swagger);
				} else {
					SwaggerNames = new HashSet<>();
				}
			}
			if (SwaggerNames != null) {
				names = trimList(new ArrayList<String>(SwaggerNames), offset, pageSize);
				for (String name : names) {
					List<Revision> versions = getListOfSwagger3Revisions(name, interactionid);
					// System.out.println(name);
					if (versions != null && versions.size() > 0) {
						Revision revision = Collections.max(versions);
						Swagger3VO vo = baseRepository.findOne("name", name, "revision", revision.getRevision(),
								Swagger3VO.class);
						SwaggerMetadata swaggerMetadata = getSwaggerMetadata(name, oas);
						if (swaggerMetadata != null) {
							vo.setTeams(swaggerMetadata.getTeams());
						}

						if (vo != null) {
							if (vo.getTeams() == null || isMailidExist(vo, user.getEmail())) {

								ObjectMapper mapper = new ObjectMapper();
								JsonNode swaggerJson = mapper.readTree(vo.getSwagger());
								if (swaggerJson != null) {
									JsonNode infoNode = swaggerJson.get("info");
									if (infoNode != null) {
										try {
											vo.setDescription(infoNode.get("description").asText());
										} catch (Exception e) {
											vo.setDescription("N/A");
										}
									}
								}
								vo.setSwagger(null);
								vo.setRoles(new ArrayList<>(swaggerRoles.get(name) == null
										? Arrays.asList("Admin", "Write", "Read")
										: swaggerRoles.get(name)));
								list.add(vo);
							}
						}
					}
				}
				Pagination pagination = new Pagination();
				int totalByUser = SwaggerNames.size();
				pagination.setOffset(offset);
				pagination.setTotal((long) totalByUser);
				pagination.setPageSize(pageSize);
				response.setPagination(pagination);
				response.setData(list);
			}
		}
		log("getListOfSwaggerDetails", interactionid, list);
		return response;
	}

	private List<SwaggerTeam> getUserTeams(User user) {
		Query query = new Query().addCriteria(Criteria.where("contacts.email").is(user.getEmail()));
		return baseRepository.find(query, SwaggerTeam.class);
	}

	private Set<String> getSwaggerNames(List<SwaggerTeam> teams, String oas) {
		Set<String> swaggerNames = new HashSet<String>();
		for (SwaggerTeam team : teams) {
			if (oas.equals("2.0")) {
				if (team.getSwaggers() != null) {
					swaggerNames.addAll(team.getSwaggers());
				} else if (oas.equals("3.0")) {
					if (team.getSwagger3() != null) {
						swaggerNames.addAll(team.getSwagger3());
					}
				}
			}
		}
		return swaggerNames;
	}

	private Map<String, Set<String>> getSwaggerPermissions(String oas, User user) {
		Map<String, Set<String>> swaggerRoles = new HashMap<String, Set<String>>();
		List<SwaggerTeam> teams = getUserTeams(user);
		if (teams != null) {
			for (SwaggerTeam team : teams) {
				Set<String> swaggers = null;
				if (oas.equals("2.0")) {
					swaggers = team.getSwaggers();
				}
				if (oas.equals("3.0")) {
					swaggers = team.getSwagger3();
				}
				if (swaggers != null) {
					for (String name : swaggers) {
						Set<String> roles = swaggerRoles.get(name);
						if (roles == null) {
							roles = new HashSet<String>();
						}
						for (SwaggerContacts contact : team.getContacts()) {
							if (user.getEmail().equals(contact.getEmail())) {
								List<String> role = contact.getRole();
								roles.addAll(role);
								swaggerRoles.put(name, roles);
								break;
							}
						}
					}
				}
			}
		}
		return swaggerRoles;
	}

	public ArrayNode getListOfPublishedSwaggerDetails(String interactionid, String jsessionid,
			String status,
			List<String> partners, List<String> products)
			throws ItorixException, JsonProcessingException, IOException {
		log("getListOfPublishedSwaggerDetails", interactionid, jsessionid);
		List<SwaggerVO> list = baseRepository.find(STATUS_VALUE, status, SwaggerVO.class);
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode arrayNode = mapper.createArrayNode();


		if (partners.isEmpty() && products.isEmpty()) {
			list.forEach(vo -> {
				try {
					getPublishedSwaggerDetails(vo, arrayNode);
				} catch (JsonProcessingException | ItorixException e) {
					log.error("Exception in getListOfPublishedSwaggerDetails : {}", e.getMessage());
				}
			});
		} else {
			List<String> swaggerNames = getAllFilteredSwaggerDetails(partners, products, "2.0");
			List<SwaggerVO> swaggerVos = list.stream().filter(swaggerVO -> {
				if (swaggerNames.contains(swaggerVO.getName())) {
					return true;
				} else {
					return false;
				}
			}).collect(Collectors.toList());
			swaggerVos.forEach(vo -> {
				try {
					getPublishedSwaggerDetails(vo, arrayNode);
				} catch (JsonProcessingException | ItorixException e) {
					log.error("Exception in getListOfPublishedSwaggerDetails : {}", e.getMessage());
				}
			});
		}
		log("getListOfSwaggerDetails", interactionid, list);
		return arrayNode;
	}

  private List<String> getAllFilteredSwaggerDetails(List<String> partners, List<String> products,
			String oas) {
    Criteria overviewCriteria = new Criteria();
    List<Criteria> criteriaList = new ArrayList<>();
    if (!products.isEmpty()) {
      Criteria productCriteria = Criteria.where("products").in(products);
      criteriaList.add(productCriteria);
    }
    if (!partners.isEmpty()) {
      Criteria partnerCriteria = Criteria.where("partners").in(partners);
      criteriaList.add(partnerCriteria);
    }

    Query query = Query.query(Criteria.where("oas").is(oas));
    if (!criteriaList.isEmpty()) {
      overviewCriteria.andOperator(criteriaList.stream().toArray(Criteria[]::new));
      query.addCriteria(overviewCriteria);
    }
    return mongoTemplate.find(query, SwaggerMetadata.class).stream()
        .map(swaggerMetadata -> swaggerMetadata.getSwaggerName()).collect(
            Collectors.toList());
  }

	private boolean isPartnerAsociated(SwaggerVO vo, String partnerId) {
		List<Revision> revisions = getListOfRevisions(vo.getName(), null);
		if (null != revisions) {
			Revision revision = revisions.stream().min((x, y) -> x.getRevision() - y.getRevision()).get();
			try {
				SwaggerVO swaggerVo = getSwaggerWithVersionNumber(vo.getSwaggerId(), revision.getRevision(), null);
				Set<String> partners = swaggerVo.getPartners();
				if (null != partners) {
					if (partners.contains(partnerId)) {
						return true;
					}
				}
			} catch (ItorixException e) {
				log.error("Exception occurred", e);
			}
		}
		return false;
	}

	private void getPublishedSwaggerDetails(SwaggerVO vo, ArrayNode arrayNode)
			throws JsonMappingException, JsonProcessingException, ItorixException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = null;
		for (int i = 0; i < arrayNode.size(); i++) {
			jsonNode = arrayNode.get(i);
			if (vo != null && jsonNode != null) {
				if (vo.getName().equals(jsonNode.get("title").asText())) {
					arrayNode.remove(i);
				}
			}
		}
		JSONObject swaggerJsonObject = new JSONObject(vo.getSwagger());

    JSONObject infoObject =
        swaggerJsonObject.has("info") && swaggerJsonObject.get("info") != null && !swaggerJsonObject.get("info").toString().isEmpty()
            ? (JSONObject) swaggerJsonObject.get("info") : null;
//    log.info("The swagger Info object is: {}",
//        infoObject.keySet().contains("description") ? infoObject.get("description") : "");

		if (null == jsonNode) {
			ObjectNode rootNode = mapper.createObjectNode();
			JsonNode swaggerJson = null;
			rootNode.put("title", vo.getName());
			ArrayNode items = mapper.createArrayNode();
			ObjectNode itemNode = mapper.createObjectNode();
			itemNode.put("title", vo.getName());
			itemNode.put("swaggerId", vo.getSwaggerId());
			itemNode.put("swaggerVersion", "2.0");
			itemNode.put("swaggerDescription",
					infoObject!=null&&infoObject.keySet().contains("description") ? infoObject.get("description") != null
							? infoObject.get("description").toString() : "" : "");
			ArrayNode revision = mapper.createArrayNode();

			SwaggerMetadata metadata = getSwaggerMetadata(vo.getName(), "2.0");
			if (metadata != null) {
				if (metadata.getTeams() != null) {
					ArrayNode teams = mapper.valueToTree(metadata.getTeams());
					itemNode.putArray("teams").addAll(teams);
				}
				if (metadata.getProducts() != null) {
					ArrayNode products = mapper.valueToTree(getswaggerProductsName(metadata.getProducts()));
					itemNode.putArray("products").addAll(products);
				}
				if (metadata.getPartners() != null) {
					ArrayNode partners = mapper.valueToTree(getswaggerPartnersName(metadata.getPartners()));
					itemNode.putArray("partners").addAll(partners);
				}
			}


			ObjectNode revisionNode = mapper.createObjectNode();
			if (vo != null && vo.getSwagger() != null) {
				swaggerJson = mapper.readTree(vo.getSwagger());
				if (swaggerJson != null) {
					JsonNode infoNode = swaggerJson.get("info");
					if (infoNode != null) {
						revisionNode.put("name", infoNode.get("version").asText());
					}
				}
			}
			String url = applicationProperties.getAppUrl() + applicationProperties.getAppDomain() + "/v1/swaggers/"
					+ vo.getSwaggerId() + "/revisions/" + vo.getRevision();
			revisionNode.put("url", url);
			ArrayNode resources = mapper.createArrayNode();
			if (swaggerJson != null) {
				JsonNode pathsNode = swaggerJson.get("paths");
				if (pathsNode != null) {
					Iterator<Map.Entry<String, JsonNode>> it = pathsNode.fields();
					// Iterator<JsonNode> it =pathsNode.elements();
					while (it.hasNext()) {
						Map.Entry<String, JsonNode> entry = it.next();
						JsonNode methodsNode = entry.getValue();
						Iterator<Map.Entry<String, JsonNode>> methods = methodsNode.fields();
						while (methods.hasNext()) {
							ObjectNode resource = mapper.createObjectNode();
							Map.Entry<String, JsonNode> method = methods.next();
							String httpMethod = method.getKey();
							if (org.springframework.http.HttpMethod.resolve(httpMethod.toUpperCase()) != null) {
								JsonNode methodNode = method.getValue();
								resource.put("method", method.getKey());
								resource.put("path", entry.getKey());
								if (methodNode.get("summary") != null) {
									resource.put("summary", methodNode.get("summary").asText());
								}
								if (methodNode.get("operationId") != null) {
									resource.put("operationId", methodNode.get("operationId").asText());
								}
								ArrayNode tagsNode = (ArrayNode) methodNode.get("tags");
								resource.set("tags", tagsNode);
								resources.add(resource);
							}
						}
					}
				}
			}
			revisionNode.set("resources", resources);
			revision.add(revisionNode);
			itemNode.set("versions", revision);
			items.add(itemNode);
			rootNode.set("items", items);
			arrayNode.add(rootNode);
		} else {
			ObjectNode rootNode = mapper.createObjectNode();
			JsonNode swaggerJson = null;
			rootNode.put("title", vo.getName());
			ArrayNode items = mapper.createArrayNode();
			ObjectNode itemNode = mapper.createObjectNode();
			itemNode.put("title", vo.getName());
			itemNode.put("swaggerId", vo.getSwaggerId());
			itemNode.put("swaggerVersion", "2.0");
			itemNode.put("swaggerDescription",
					infoObject!=null&&infoObject.keySet().contains("description") ? infoObject.get("description") != null
							? infoObject.get("description").toString() : "" : "");
			ArrayNode revision = mapper.createArrayNode();
			SwaggerMetadata metadata = getSwaggerMetadata(vo.getName(), "2.0");
			if (metadata != null) {
				if (metadata.getTeams() != null) {
					ArrayNode teams = mapper.valueToTree(metadata.getTeams());
					itemNode.putArray("teams").addAll(teams);
				}
				if (metadata.getProducts() != null) {
					ArrayNode products = mapper.valueToTree(getswaggerProductsName(metadata.getProducts()));
					itemNode.putArray("products").addAll(products);
				}
				if (metadata.getPartners() != null) {
					ArrayNode partners = mapper.valueToTree(getswaggerPartnersName(metadata.getPartners()));
					itemNode.putArray("partners").addAll(partners);
				}
			}


			ObjectNode revisionNode = mapper.createObjectNode();
			if (vo != null && vo.getSwagger() != null) {
				swaggerJson = mapper.readTree(vo.getSwagger());
				if (swaggerJson != null) {
					JsonNode infoNode = swaggerJson.get("info");
					if (infoNode != null) {
						revisionNode.put("name", infoNode.get("version").asText());
					}
				}
			}
			String url = applicationProperties.getAppUrl() + applicationProperties.getAppDomain() + "/v1/swaggers/"
					+ vo.getSwaggerId() + "/revisions/" + vo.getRevision();
			revisionNode.put("url", url);
			ArrayNode resources = mapper.createArrayNode();
			if (swaggerJson != null) {
				JsonNode pathsNode = swaggerJson.get("paths");
				if (pathsNode != null) {
					Iterator<Map.Entry<String, JsonNode>> it = pathsNode.fields();
					// Iterator<JsonNode> it =pathsNode.elements();
					while (it.hasNext()) {
						Map.Entry<String, JsonNode> entry = it.next();
						JsonNode methodsNode = entry.getValue();
						Iterator<Map.Entry<String, JsonNode>> methods = methodsNode.fields();
						while (methods.hasNext()) {
							ObjectNode resource = mapper.createObjectNode();
							Map.Entry<String, JsonNode> method = methods.next();
							String httpMethod = method.getKey();
							if (org.springframework.http.HttpMethod.resolve(httpMethod.toUpperCase()) != null) {
								JsonNode methodNode = method.getValue();
								resource.put("method", method.getKey());
								resource.put("path", entry.getKey());
								if (methodNode.get("summary") != null) {
									resource.put("summary", methodNode.get("summary").asText());
								}
								if (methodNode.get("operationId") != null) {
									resource.put("operationId", methodNode.get("operationId").asText());
								}
								ArrayNode tagsNode = (ArrayNode) methodNode.get("tags");
								resource.set("tags", tagsNode);
								resources.add(resource);
							}
						}
					}
				}
			}
			revisionNode.set("resources", resources);
			revision.add(revisionNode);
			itemNode.set("versions", revision);
			items.add(itemNode);
			rootNode.set("items", items);
			arrayNode.add(rootNode);
		}
	}

	public ArrayNode getListOfPublishedSwagger3Details(String interactionid, String jsessionid, String status,
			List<String> partners, List<String> products) throws ItorixException, JsonProcessingException, IOException {
		log("getListOfPublishedSwaggerDetails", interactionid, jsessionid);
		List<Swagger3VO> list = baseRepository.find(STATUS_VALUE, status, Swagger3VO.class);
		ObjectMapper mapper = new ObjectMapper();

		ArrayNode arrayNode = mapper.createArrayNode();
		if (partners.isEmpty() && products.isEmpty()) {
			list.forEach(vo -> {
				try {
					getPublishedSwaggerDetails(vo, arrayNode);
				} catch (JsonProcessingException | ItorixException e) {
					log.error("Exception in getListOfPublishedSwaggerDetails : {}", e.getMessage());
				}
			});
		} else {
			List<String> swaggerNames = getAllFilteredSwaggerDetails(partners, products, "3.0");
			List<Swagger3VO> swaggerVos = list.stream().filter(swaggerVO -> {
				if (swaggerNames.contains(swaggerVO.getName())) {
					return true;
				} else {
					return false;
				}
			}).collect(Collectors.toList());
			swaggerVos.forEach(vo -> {
				try {
					getPublishedSwaggerDetails(vo, arrayNode);
				} catch (JsonProcessingException | ItorixException e) {
					log.error("Exception in getListOfPublishedSwaggerDetails : {}", e.getMessage());
				}
			});
		}
		log("getListOfSwaggerDetails", interactionid, list);
		return arrayNode;
	}

	private boolean isPartnerAsociated(Swagger3VO vo, String partnerId) {
		Set<String> partners = vo.getPartners();
		if (null != partners) {
			if (partners.contains(partnerId)) {
				return true;
			}
		}
		return false;
	}

	private void getPublishedSwaggerDetails(Swagger3VO vo, ArrayNode arrayNode)
			throws JsonMappingException, JsonProcessingException, ItorixException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = null;

		for (int i = 0; i < arrayNode.size(); i++) {
			jsonNode = arrayNode.get(i);
			if (vo != null && jsonNode != null) {
				if (vo.getName().equals(jsonNode.get("title").asText())) {
					arrayNode.remove(i);
				}
			}
		}
		JSONObject swaggerJsonObject = new JSONObject(vo.getSwagger());
    JSONObject infoObject =
        swaggerJsonObject.has("info") && swaggerJsonObject.get("info") != null && !swaggerJsonObject.get("info").toString().isEmpty()
            ? (JSONObject) swaggerJsonObject.get("info") : null;
//		log.info("The swagger Info object is: {}",
//				infoObject.keySet().contains("description") ? infoObject.get("description") : "");

		if (null == jsonNode) {
			ObjectNode rootNode = mapper.createObjectNode();
			JsonNode swaggerJson = null;
			rootNode.put("title", vo.getName());
			ArrayNode items = mapper.createArrayNode();
			ObjectNode itemNode = mapper.createObjectNode();
			itemNode.put("title", vo.getName());
			itemNode.put("id", vo.getId());
			itemNode.put("swaggerId", vo.getSwaggerId());
			itemNode.put("swaggerVersion", "3.0");
			itemNode.put("swaggerDescription",
					infoObject!=null &&infoObject.keySet().contains("description") ? infoObject.get("description") != null
							? infoObject.get("description").toString() : "" : "");
			ArrayNode revision = mapper.createArrayNode();
			SwaggerMetadata metadata = getSwaggerMetadata(vo.getName(), "3.0");
			if (metadata != null) {
				if (metadata.getTeams() != null) {
					ArrayNode teams = mapper.valueToTree(metadata.getTeams());
					itemNode.putArray("teams").addAll(teams);
				}
				if (metadata.getProducts() != null) {
					ArrayNode products = mapper.valueToTree(getswaggerProductsName(metadata.getProducts()));
					itemNode.putArray("products").addAll(products);
				}
				if (metadata.getPartners() != null) {
					ArrayNode partners = mapper.valueToTree(getswaggerPartnersName(metadata.getPartners()));
					itemNode.putArray("partners").addAll(partners);
				}
			}
			ObjectNode revisionNode = mapper.createObjectNode();
			if (vo != null && vo.getSwagger() != null) {
				swaggerJson = mapper.readTree(vo.getSwagger());
				if (swaggerJson != null) {
					JsonNode infoNode = swaggerJson.get("info");
					if (infoNode != null) {
						revisionNode.put("name", infoNode.get("version").asText());
					}
				}
			}
			String url = applicationProperties.getAppUrl() + applicationProperties.getAppDomain() + "/v1/swaggers/"
					+ vo.getSwaggerId() + "/revisions/" + vo.getRevision();
			revisionNode.put("url", url);
			ArrayNode resources = mapper.createArrayNode();
			if (swaggerJson != null) {
				JsonNode pathsNode = swaggerJson.get("paths");
				if (pathsNode != null) {
					Iterator<Map.Entry<String, JsonNode>> it = pathsNode.fields();
					// Iterator<JsonNode> it =pathsNode.elements();
					while (it.hasNext()) {
						Map.Entry<String, JsonNode> entry = it.next();
						JsonNode methodsNode = entry.getValue();
						Iterator<Map.Entry<String, JsonNode>> methods = methodsNode.fields();
						while (methods.hasNext()) {
							ObjectNode resource = mapper.createObjectNode();
							Map.Entry<String, JsonNode> method = methods.next();
							String httpMethod = method.getKey();
							if (org.springframework.http.HttpMethod.resolve(httpMethod.toUpperCase()) != null) {
								JsonNode methodNode = method.getValue();
								resource.put("method", method.getKey());
								resource.put("path", entry.getKey());
								if (methodNode.get("summary") != null) {
									resource.put("summary", methodNode.get("summary").asText());
								}
								if (methodNode.get("operationId") != null) {
									resource.put("operationId", methodNode.get("operationId").asText());
								}
								ArrayNode tagsNode = (ArrayNode) methodNode.get("tags");
								resource.set("tags", tagsNode);
								resources.add(resource);
							}
						}
					}
				}
			}
			revisionNode.set("resources", resources);
			revision.add(revisionNode);
			itemNode.set("versions", revision);
			items.add(itemNode);
			rootNode.set("items", items);
			arrayNode.add(rootNode);
		} else {
			ObjectNode rootNode = mapper.createObjectNode();
			JsonNode swaggerJson = null;
			rootNode.put("title", vo.getName());
			ArrayNode items = mapper.createArrayNode();
			ObjectNode itemNode = mapper.createObjectNode();
			itemNode.put("title", vo.getName());
			itemNode.put("id", vo.getId());
			itemNode.put("swaggerId", vo.getSwaggerId());
			itemNode.put("swaggerVersion", "3.0");
			itemNode.put("swaggerDescription",
					infoObject!=null && infoObject.keySet().contains("description") ? infoObject.get("description") != null
							? infoObject.get("description").toString() : "" : "");
			ArrayNode revision = mapper.createArrayNode();
			SwaggerMetadata metadata = getSwaggerMetadata(vo.getName(), "3.0");
			if (metadata != null) {
				if (metadata.getTeams() != null) {
					ArrayNode teams = mapper.valueToTree(metadata.getTeams());
					itemNode.putArray("teams").addAll(teams);
				}
				if (metadata.getProducts() != null) {
					ArrayNode products = mapper.valueToTree(getswaggerProductsName(metadata.getProducts()));
					itemNode.putArray("products").addAll(products);
				}
				if (metadata.getPartners()!= null) {
					ArrayNode partners = mapper.valueToTree(getswaggerPartnersName(metadata.getPartners()));
					itemNode.putArray("partners").addAll(partners);
				}
			}
			ObjectNode revisionNode = mapper.createObjectNode();
			if (vo != null && vo.getSwagger() != null) {
				swaggerJson = mapper.readTree(vo.getSwagger());
				if (swaggerJson != null) {
					JsonNode infoNode = swaggerJson.get("info");
					if (infoNode != null) {
						revisionNode.put("name", infoNode.get("version").asText());
					}
				}
			}
			String url = applicationProperties.getAppUrl() + applicationProperties.getAppDomain() + "/v1/swaggers/"
					+ vo.getSwaggerId() + "/revisions/" + vo.getRevision();
			revisionNode.put("url", url);
			ArrayNode resources = mapper.createArrayNode();
			if (swaggerJson != null) {
				JsonNode pathsNode = swaggerJson.get("paths");
				if (pathsNode != null) {
					Iterator<Map.Entry<String, JsonNode>> it = pathsNode.fields();
					// Iterator<JsonNode> it =pathsNode.elements();
					while (it.hasNext()) {
						Map.Entry<String, JsonNode> entry = it.next();
						JsonNode methodsNode = entry.getValue();
						Iterator<Map.Entry<String, JsonNode>> methods = methodsNode.fields();
						while (methods.hasNext()) {
							ObjectNode resource = mapper.createObjectNode();
							Map.Entry<String, JsonNode> method = methods.next();
							String httpMethod = method.getKey();
							if (org.springframework.http.HttpMethod.resolve(httpMethod.toUpperCase()) != null) {
								JsonNode methodNode = method.getValue();
								resource.put("method", method.getKey());
								resource.put("path", entry.getKey());
								if (methodNode.get("summary") != null) {
									resource.put("summary", methodNode.get("summary").asText());
								}
								if (methodNode.get("operationId") != null) {
									resource.put("operationId", methodNode.get("operationId").asText());
								}
								ArrayNode tagsNode = (ArrayNode) methodNode.get("tags");
								resource.set("tags", tagsNode);
								resources.add(resource);
							}
						}
					}
				}
			}
			revisionNode.set("resources", resources);
			revision.add(revisionNode);
			itemNode.set("versions", revision);
			items.add(itemNode);
			rootNode.set("items", items);
			arrayNode.add(rootNode);
		}
	}

	private boolean isMailidExist(SwaggerVO vo, String email) {
		if (vo.getTeams() != null && vo.getTeams().size() > 0) {
			for (String teamName : vo.getTeams()) {
				SwaggerTeam team = baseRepository.findOne("name", teamName, SwaggerTeam.class);
				for (SwaggerContacts contact : team.getContacts()) {
					boolean value = email.equals(contact.getEmail());
					if (value) {
						return true;
					}
				}
			}
		} else {
			return true;
		}
		return false;
	}

	private boolean isMailidExist(Swagger3VO vo, String email) {
		if (vo.getTeams() != null && vo.getTeams().size() > 0) {
			for (String teamName : vo.getTeams()) {
				SwaggerTeam team = baseRepository.findOne("name", teamName, SwaggerTeam.class);
				for (SwaggerContacts contact : team.getContacts()) {
					boolean value = email.equals(contact.getEmail());
					if (value) {
						return true;
					}
				}
			}
		} else {
			return true;
		}
		return false;
	}

	public SwaggerVO getSwagger(String name, String interactionid) {
		log("getSwagger", interactionid, name);
		SwaggerVO vo = null;
		vo = baseRepository.findOne("id", name, SwaggerVO.class);
		if (vo == null) {
			vo = baseRepository.findOne("swaggerId", name, SwaggerVO.class);
		}
		if (vo != null) {
			name = vo.getName();
		}
		List<SwaggerVO> swaggers = baseRepository.find("name", name, SwaggerVO.class);
		List<Revision> versions = new ArrayList<Revision>();
		for (SwaggerVO swaggerVO : swaggers) {
			versions.add(new Revision(swaggerVO.getRevision(), swaggerVO.getStatus()));
		}
		if (versions.size() > 0) {
			Revision version = Collections.max(versions);
			for (SwaggerVO swaggerVO : swaggers) {
				if (swaggerVO.getRevision() == version.getRevision()) {
					return swaggerVO;
				}
			}
		}
		log("getSwagger", interactionid, vo);
		return vo;
	}

	public Swagger3VO getSwagger3(String name, String interactionid) {
		log("getSwagger", interactionid, name);
		Swagger3VO vo = null;
		vo = baseRepository.findOne("id", name, Swagger3VO.class);
		if (vo == null) {
			vo = baseRepository.findOne("swaggerId", name, Swagger3VO.class);
		}
		if (vo != null) {
			name = vo.getName();
		}
		List<Swagger3VO> swaggers = baseRepository.find("name", name, Swagger3VO.class);
		List<Revision> versions = new ArrayList<Revision>();
		for (Swagger3VO swaggerVO : swaggers) {
			versions.add(new Revision(swaggerVO.getRevision(), swaggerVO.getStatus()));
		}
		if (swaggers.size() > 0) {
			Revision version = Collections.max(versions);
			for (Swagger3VO swaggerVO : swaggers) {
				if (swaggerVO.getRevision() == version.getRevision()) {
					return swaggerVO;
				}
			}
		}
		log("getSwagger", interactionid, vo);
		return vo;
	}

	public SwaggerVO getSwaggerWithVersionNumber(String name, Integer revision, String interactionid)
			throws ItorixException {
		log("getSwaggerWithVersionNumber", interactionid, name, revision);
		SwaggerVO swaggerVO = baseRepository.findOne("id", name, SwaggerVO.class);
		if (swaggerVO == null) {
			swaggerVO = baseRepository.findOne("swaggerId", name, SwaggerVO.class);
		}
		if (swaggerVO == null) {
			swaggerVO = baseRepository.findOne("name", name, "revision", revision, SwaggerVO.class);
		} else {
			name = swaggerVO.getName();
			swaggerVO = baseRepository.findOne("name", name, "revision", revision, SwaggerVO.class);
		}
		log("getSwaggerWithVersionNumber", interactionid, swaggerVO);
		return swaggerVO;
	}

	public Swagger3VO getSwagger3WithVersionNumber(String name, Integer revision, String interactionid)
			throws ItorixException {
		log("getSwaggerWithVersionNumber", interactionid, name, revision);
		Swagger3VO swaggerVO = baseRepository.findOne("id", name, Swagger3VO.class);
		if (swaggerVO == null) {
			swaggerVO = baseRepository.findOne("swaggerId", name, Swagger3VO.class);
		}
		if (swaggerVO == null) {
			swaggerVO = baseRepository.findOne("name", name, "revision", revision, Swagger3VO.class);
		} else {
			name = swaggerVO.getName();
			swaggerVO = baseRepository.findOne("name", name, "revision", revision, Swagger3VO.class);
		}
		log("getSwaggerWithVersionNumber", interactionid, swaggerVO);
		return swaggerVO;
	}

	public List<SwaggerComment> getSwaggerComments(String name, Integer revision, String interactionid) {
		log("getSwaggerComments", interactionid, name, revision);
		List<SwaggerComment> comments = baseRepository.find("swaggerName", name, "swaggerRevision", revision,
				SwaggerComment.class);
		log("getSwaggerComments", interactionid, comments);
		return comments;
	}

	public List<Swagger3Comment> getSwagger3Comments(String name, Integer revision, String interactionid) {
		log("getSwaggerComments", interactionid, name, revision);
		List<Swagger3Comment> comments = baseRepository.find("swaggerName", name, "swaggerRevision", revision,
				Swagger3Comment.class);
		log("getSwaggerComments", interactionid, comments);
		return comments;
	}

	public Boolean getLockStatus(String name, Integer revision, String interactionid) {
		log("getLockStatus", interactionid, name, revision);
		SwaggerVO swaggerVO = baseRepository.findOne("name", name, "revision", revision, SwaggerVO.class);
		log("getLockStatus", interactionid, swaggerVO);
		return swaggerVO.getLock();
	}

	public Boolean getSwagger3LockStatus(String name, Integer revision, String interactionid) {
		log("getLockStatus", interactionid, name, revision);
		Swagger3VO swaggerVO = baseRepository.findOne("name", name, "revision", revision, Swagger3VO.class);
		log("getLockStatus", interactionid, swaggerVO);
		return swaggerVO.getLock();
	}

	public SwaggerVO updateStatus(String name, Integer revision, String json, String interactionid, String jsessionid)
			throws MessagingException, JSONException, ItorixException {
		log("updateStatus", interactionid, name, json, revision);
		SwaggerVO swaggerVO = findSwagger(name, interactionid);
		name = swaggerVO.getName();
		JSONObject jsonObject = new JSONObject(json);
		String status = jsonObject.getString("state");
		if (status.equals(SwaggerStatus.PUBLISH.getStatus())) {
			swaggerVO = baseRepository.findOne("name", name, STATUS_VALUE, status, SwaggerVO.class);
			if (swaggerVO != null) {
				swaggerVO.setStatus(SwaggerStatus.DRAFT.getStatus());
				baseRepository.save(swaggerVO);
			}
		}
		swaggerVO = baseRepository.findOne("name", name, "revision", revision, SwaggerVO.class);
		swaggerVO.setJsessionid(jsessionid);
		swaggerVO.setStatus(getStatus(status, swaggerVO.getStatus(), swaggerVO));

		baseRepository.save(swaggerVO);
		log("updateStatus", interactionid, swaggerVO);
		return swaggerVO;
	}

	public List<String> getSwaggerRoles(String name, String oas, String interactionid, String jsessionid)
			throws ItorixException {
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User user = getUserDetailsFromSessionID(jsessionid);
		boolean isAdmin = user.isWorkspaceAdmin(userSessionToken.getWorkspaceId());
		List<String> roles = user.getUserWorkspace(userSessionToken.getWorkspaceId()).getRoles();
		List<String> swaggerRoles = new ArrayList<>();
		if (isAdmin) {
			swaggerRoles = Arrays.asList("Admin", "Write", "Read");
		} else {
			Map<String, Set<String>> swaggerRolesMap = getSwaggerPermissions(oas, user);
			swaggerRoles = swaggerRolesMap.get(name) != null
					? new ArrayList<>(swaggerRolesMap.get(name))
					: new ArrayList<>();;
		}
		return swaggerRoles;
	}

	public Swagger3VO updateSwagger3Status(String name, Integer revision, String json, String interactionid,
			String jsessionid) throws MessagingException, JSONException, ItorixException {
		log("updateStatus", interactionid, name, json, revision);
		Swagger3VO swaggerVO = findSwagger3(name, interactionid);
		name = swaggerVO.getName();
		JSONObject jsonObject = new JSONObject(json);
		String status = jsonObject.getString("state");
		if (status.equals(SwaggerStatus.PUBLISH.getStatus())) {
			swaggerVO = baseRepository.findOne("name", name, STATUS_VALUE, status, Swagger3VO.class);
			if (swaggerVO != null) {
				swaggerVO.setStatus(SwaggerStatus.DRAFT.getStatus());
				baseRepository.save(swaggerVO);
			}
		}
		swaggerVO = baseRepository.findOne("name", name, "revision", revision, Swagger3VO.class);
		swaggerVO.setJsessionid(jsessionid);
		swaggerVO.setStatus(getStatus(status, swaggerVO.getStatus(), swaggerVO));
		baseRepository.save(swaggerVO);
		log("updateStatus", interactionid, swaggerVO);
		return swaggerVO;
	}

	private String getStatus(String status, String previousStatus, SwaggerVO vo) throws MessagingException {
		if (status.equals(SwaggerStatus.DRAFT.getStatus())) {
			return "Draft";
		} else if (status.equals(SwaggerStatus.REVIEW.getStatus())) {
			sendEmails(vo, status, previousStatus);
			return "Review";
		} else if (status.equals(SwaggerStatus.CHANGE_REQUIRED.getStatus())) {
			sendEmails(vo, status, previousStatus);
			return "Change Required";
		} else if (status.equals(SwaggerStatus.APPROVED.getStatus())) {
			sendEmails(vo, status, previousStatus);
			return "Approved";
		} else if (status.equals(SwaggerStatus.PUBLISH.getStatus())) {
			sendEmails(vo, status, previousStatus);
			return "Publish";
		} else if (status.equals(SwaggerStatus.DEPRECATE.getStatus())) {
			sendEmails(vo, status, previousStatus);
			return "Deprecate";
		} else if (status.equals(SwaggerStatus.RETIRED.getStatus())) {
			sendEmails(vo, status, previousStatus);
			return "Retired";
		}
		return "Draft";
	}

	private String getStatus(String status, String previousStatus, Swagger3VO vo) throws MessagingException {

		if (status.equals(SwaggerStatus.DRAFT.getStatus())) {
			return "Draft";
		} else if (status.equals(SwaggerStatus.REVIEW.getStatus())) {
			sendEmails(vo, status, previousStatus);
			return "Review";
		} else if (status.equals(SwaggerStatus.CHANGE_REQUIRED.getStatus())) {
			sendEmails(vo, status, previousStatus);
			return "Change Required";
		} else if (status.equals(SwaggerStatus.APPROVED.getStatus())) {
			sendEmails(vo, status, previousStatus);
			return "Approved";
		} else if (status.equals(SwaggerStatus.PUBLISH.getStatus())) {
			sendEmails(vo, status, previousStatus);
			return "Publish";
		} else if (status.equals(SwaggerStatus.DEPRECATE.getStatus())) {
			sendEmails(vo, status, previousStatus);
			return "Deprecate";
		} else if (status.equals(SwaggerStatus.RETIRED.getStatus())) {
			sendEmails(vo, status, previousStatus);
			return "Retired";
		}
		return "Draft";
	}

	private List<SwaggerTeam> getTeamsBySwaggerName(String swaggerName, String oas) {
		Query query = new Query();
		if (oas.equals("2.0")) {
			query.addCriteria(Criteria.where("swaggers").is(swaggerName));
		} else if (oas.equals("3.0")) {
			query.addCriteria(Criteria.where("swagger3").is(swaggerName));
		}
		List<SwaggerTeam> teamlist = baseRepository.find(query, SwaggerTeam.class);
		return teamlist;
	}

	private void sendEmails(SwaggerVO vo, String status, String previousStatus) throws MessagingException {
		String subject = MessageFormat.format(applicationProperties.getSwaggerChangeStatusSubject(), vo.getName());
		User user = getUserDetailsFromSessionID(ServiceRequestContextHolder.getContext().getUserSessionToken().getId());
		String userName = "";
		if (user != null) {
			userName = user.getFirstName() + " " + user.getLastName();
		}
		String body = MessageFormat.format(applicationProperties.getSwaggerChangeStatusBody(), vo.getName(), status,
				getSwaggerCountbyStatus(status), previousStatus, getSwaggerCountbyStatus(previousStatus), userName,
				getSwaggerCountbyUser(userName), vo.getRevision(), getSwaggerRevisionCount(vo.getName()));
		List<SwaggerTeam> teams = getTeamsBySwaggerName(vo.getName(), "2.0");
		if (teams != null) {
			for (SwaggerTeam team : teams) {
				// SwaggerTeam team = baseRepository.findOne("name", teamName,
				// SwaggerTeam.class);
				if (team != null) {
					EmailTemplate emailTemplate = new EmailTemplate();
					emailTemplate.setToMailId(getCOntactForTeam(team));
					emailTemplate.setSubject(subject);
					emailTemplate.setBody(body);
					// emailTemplate.setFotter(signature);
					mailUtil.sendEmail(emailTemplate);
				}
			}
		} else {
			EmailTemplate emailTemplate = new EmailTemplate();
			emailTemplate.setToMailId(Arrays.asList(user.getEmail()));
			emailTemplate.setSubject(subject);
			emailTemplate.setBody(body);
			mailUtil.sendEmail(emailTemplate);
		}
	}

	private int getSwaggerCountbyStatus(String status) {
		try {
			Query query = new Query(Criteria.where(STATUS_VALUE).is(status));
			int count = mongoTemplate.query(SwaggerVO.class).distinct("name").as(String.class).matching(query).all()
					.size();
			return count;
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return 0;
	}

	private int getSwaggerCountbyUser(String userId) {
		try {
			Query query = new Query(Criteria.where("createdUserName").is(userId));
			int count = mongoTemplate.query(SwaggerVO.class).distinct("name").as(String.class).matching(query).all()
					.size();
			return count;
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return 0;
	}

	private int getSwaggerRevisionCount(String swaggerName) {
		try {
			Query query = new Query(Criteria.where("name").is(swaggerName));
			int count = mongoTemplate.query(SwaggerVO.class).distinct("revision").as(Integer.class).matching(query)
					.all().size();
			return count;
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return 0;
	}

	private int getSwagger3CountbyStatus(String status) {
		try {
			Query query = new Query(Criteria.where(STATUS_VALUE).is(status));
			int count = mongoTemplate.query(Swagger3VO.class).distinct("name").as(String.class).matching(query).all()
					.size();
			return count;
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return 0;
	}

	private int getSwagger3CountbyUser(String userId) {
		try {
			Query query = new Query(Criteria.where("createdUserName").is(userId));
			int count = mongoTemplate.query(Swagger3VO.class).distinct("name").as(String.class).matching(query).all()
					.size();
			return count;
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return 0;
	}

	private int getSwagger3RevisionCount(String swaggerName) {
		try {
			Query query = new Query(Criteria.where("name").is(swaggerName));
			int count = mongoTemplate.query(Swagger3VO.class).distinct("revision").as(Integer.class).matching(query)
					.all().size();
			return count;
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return 0;
	}

	private void sendEmails(Swagger3VO vo, String status, String previousStatus) throws MessagingException {
		String subject = MessageFormat.format(applicationProperties.getSwaggerChangeStatusSubject(), vo.getName());
		User user = getUserDetailsFromSessionID(ServiceRequestContextHolder.getContext().getUserSessionToken().getId());
		String userName = "";
		if (user != null) {
			userName = user.getFirstName() + " " + user.getLastName();
		}
		// String body =
		// MessageFormat.format(applicationProperties.getSwaggerChangeStatusBody(),
		// status, vo.getStatus(), userName);
		String body = MessageFormat.format(applicationProperties.getSwaggerChangeStatusBody(), vo.getName(), status,
				getSwagger3CountbyStatus(status), previousStatus, getSwagger3CountbyStatus(previousStatus), userName,
				getSwagger3CountbyUser(userName), vo.getRevision(), getSwagger3RevisionCount(vo.getName()));
		List<SwaggerTeam> teams = getTeamsBySwaggerName(vo.getName(), "3.0");
		if (teams != null) {
			for (SwaggerTeam team : teams) {
				// SwaggerTeam team = baseRepository.findOne("name", teamName,
				// SwaggerTeam.class);
				if (team != null) {
					EmailTemplate emailTemplate = new EmailTemplate();
					emailTemplate.setToMailId(getCOntactForTeam(team));
					emailTemplate.setSubject(subject);
					emailTemplate.setBody(body);
					mailUtil.sendEmail(emailTemplate);
				}
			}
		} else {
			EmailTemplate emailTemplate = new EmailTemplate();
			emailTemplate.setToMailId(Arrays.asList(user.getEmail()));
			emailTemplate.setSubject(subject);
			emailTemplate.setBody(body);
			mailUtil.sendEmail(emailTemplate);
		}
	}

	public void updateComment(SwaggerComment comment) {
		log("updateComment", comment.getInteractionid(), comment);
		comment = baseRepository.save(comment);
	}

	public void updateSwagger3Comment(Swagger3Comment comment) {
		log("updateComment", comment.getInteractionid(), comment);
		comment = baseRepository.save(comment);
	}

	public void updateLockStatus(SwaggerVO swaggerVO, String jsessionid) {
		log("updateLockStatus", swaggerVO.getInteractionid(), swaggerVO);
		SwaggerVO vo = baseRepository.findOne("name", swaggerVO.getName(), "revision", swaggerVO.getRevision(),
				SwaggerVO.class);
		if (swaggerVO.getLock()) {
			User user = getUserDetailsFromSessionID(jsessionid);
			vo.setLockedBy(user.getFirstName() + " " + user.getLastName());
			vo.setLockedAt(System.currentTimeMillis());
			vo.setLockedByUserId(user.getId());

		} else {
			vo.setLockedBy(null);
			vo.setLockedAt(null);
			vo.setLockedByUserId(null);
		}
		vo.setLock(swaggerVO.getLock());
		swaggerVO = baseRepository.save(vo);
	}

	public void updateSwagger3LockStatus(Swagger3VO swaggerVO, String jsessionid) {
		log("updateLockStatus", swaggerVO.getInteractionid(), swaggerVO);
		Swagger3VO vo = baseRepository.findOne("name", swaggerVO.getName(), "revision", swaggerVO.getRevision(),
				Swagger3VO.class);
		if (swaggerVO.getLock()) {
			User user = getUserDetailsFromSessionID(jsessionid);
			vo.setLockedBy(user.getFirstName() + " " + user.getLastName());
			vo.setLockedAt(System.currentTimeMillis());
			vo.setLockedByUserId(user.getId());
		} else {
			vo.setLockedBy(null);
			vo.setLockedAt(null);
			vo.setLockedByUserId(null);
		}
		vo.setLock(swaggerVO.getLock());
		swaggerVO = baseRepository.save(vo);
	}

	public SwaggerVO deprecate(SwaggerVO swaggerVO) {
		log("deprecate", swaggerVO.getInteractionid(), swaggerVO);
		SwaggerVO vo = baseRepository.findOne("name", swaggerVO.getName(), "revision", swaggerVO.getRevision(),
				SwaggerVO.class);
		vo.setLock(swaggerVO.getLock());
		return swaggerVO = baseRepository.save(vo);
	}

	public Swagger3VO deprecate(Swagger3VO swaggerVO) {
		log("deprecate", swaggerVO.getInteractionid(), swaggerVO);
		Swagger3VO vo = baseRepository.findOne("name", swaggerVO.getName(), "revision", swaggerVO.getRevision(),
				Swagger3VO.class);
		vo.setLock(swaggerVO.getLock());
		return swaggerVO = baseRepository.save(vo);
	}

	public void updateProxies(SwaggerVO swaggerVO) {
		log("updateProxies", swaggerVO.getInteractionid(), swaggerVO);
		SwaggerVO vo = baseRepository.findOne("name", swaggerVO.getName(), "revision", swaggerVO.getRevision(),
				SwaggerVO.class);
		Map<String, String> proxies = vo.getProxies();
		proxies.putAll(swaggerVO.getProxies());
		vo.setProxies(proxies);
		swaggerVO = baseRepository.save(vo);
	}

	public String genarateXpath(MultipartFile xsdFile, String elementName, String interactionid) throws Exception {
		log("genarateXpath", interactionid, elementName);
		long timeStamp = System.currentTimeMillis();
		String xsdFileBackUpLocation = applicationProperties.getBackupDir() + timeStamp;
		if (!new File(xsdFileBackUpLocation).exists()) {
			new File(xsdFileBackUpLocation).mkdirs();
		}

		String xsdFileLocation = xsdFileBackUpLocation + "/" + xsdFile.getOriginalFilename();
		writeToFile(xsdFile.getInputStream(), xsdFileLocation);
		XPathGen g = new XPathGen();
		String fileLocation = xsdFileBackUpLocation + "/xpath.csv";
		g.generateXPathForElement(xsdFileLocation, elementName, fileLocation);
		log("genarateXpath", interactionid, fileLocation);
		return fileLocation;
	}

	public SwaggerVO genarateSwaggerDefinations(SwaggerVO swaggerVO, MultipartFile xpathFile, String sheetName,
			Integer revision) throws Exception {
		log("genarateSwaggerDefinations", swaggerVO.getInteractionid(), swaggerVO);
		long timeStamp = System.currentTimeMillis();
		String xpathFileBackUpLocation = applicationProperties.getBackupDir() + timeStamp;
		if (!new File(xpathFileBackUpLocation).exists()) {
			new File(xpathFileBackUpLocation).mkdirs();
		}
		String xpathFileLocation = xpathFileBackUpLocation + "/" + xpathFile.getOriginalFilename();
		writeToFile(xpathFile.getInputStream(), xpathFileLocation);
		XlsUtil xlsReader = new XlsUtil();
		List<RowData> rowDataList = xlsReader.readExcel(xpathFileLocation, sheetName);
		PopulateSwaggerDefination populateSwaggerDefination = new PopulateSwaggerDefination();
		Swagger swagger = populateSwaggerDefination.populateDefinitons(rowDataList, new Swagger());
		if (revision == null) {
			swaggerVO.setRevision(1);
			swaggerVO.setStatus("Draft");
			swaggerVO.setLock(false);
			swaggerVO.setId(null);
			Info info = new Info();
			info.setTitle(swaggerVO.getName());
			swagger.setInfo(info);
			ObjectMapper mapper = new ObjectMapper();
			String swaggerString = mapper.writeValueAsString(swagger);
			swaggerVO.setSwagger(swaggerString);
		} else {
			swaggerVO = baseRepository.findOne("name", swaggerVO.getName(), "revision", revision, SwaggerVO.class);
			if (swaggerVO != null) {
				Swagger s = new SwaggerParser().parse(swaggerVO.getSwagger().toString());
				Map<String, Model> m = s.getDefinitions();
				m.putAll(swagger.getDefinitions());
				s.setDefinitions(m);
				ObjectMapper mapper = new ObjectMapper();
				String swaggerString = mapper.writeValueAsString(s);
				swaggerVO.setSwagger(swaggerString);
			}
		}
		SwaggerVO details = baseRepository.save(swaggerVO);
		log("genarateSwaggerDefinations", swaggerVO.getInteractionid(), details);
		return details;
	}

	public SwaggerVO genarateSwaggerJsonDefinations(SwaggerVO swaggerVO, List<RowData> rowDataList, Integer revision)
			throws Exception {
		log("genarateSwaggerJsonDefinations", swaggerVO.getInteractionid(), swaggerVO);
		SwaggerVO dbswaggerVO = getSwagger(swaggerVO.getName(), "");
		if (dbswaggerVO != null) {
			swaggerVO.setName(dbswaggerVO.getName());
		}
		SwaggerVO details = null;
		PopulateSwaggerDefination populateSwaggerDefination = new PopulateSwaggerDefination();
		Swagger swagger = populateSwaggerDefination.populateDefinitons(rowDataList, new Swagger());
		swagger = removeResponseSchema(swagger);
		if (revision == null) {
			swaggerVO.setRevision(1);
			swaggerVO.setStatus("Draft");
			swaggerVO.setLock(false);
			swaggerVO.setId(null);
			Info info = new Info();
			info.setTitle(swaggerVO.getName());
			swagger.setInfo(info);
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			String swaggerString = mapper.writeValueAsString(swagger);
			swaggerVO.setSwagger(swaggerString);
			// swaggerVO.setSwagger(swagger);
			details = baseRepository.save(swaggerVO);
		} else {
			swaggerVO = baseRepository.findOne("name", swaggerVO.getName(), "revision", revision, SwaggerVO.class);
			if (swaggerVO != null) {
				Swagger s = new SwaggerParser().parse(swaggerVO.getSwagger().toString());
				Map<String, Model> m = s.getDefinitions();
				m.putAll(swagger.getDefinitions());
				s.setDefinitions(m);
				ObjectMapper mapper = new ObjectMapper();
				mapper.setSerializationInclusion(Include.NON_NULL);
				s = removeResponseSchema(s);
				String swaggerString = mapper.writeValueAsString(s);
				swaggerString = SwaggerUtil.removeResponseSchemaTag(swaggerString);
				swaggerVO.setSwagger(swaggerString);
				details = baseRepository.save(swaggerVO);
			}
		}
		log("genarateSwaggerJsonDefinations", swaggerVO.getInteractionid(), details);
		return details;
	}

	private Swagger removeResponseSchema(Swagger swagger) {
		try {
			Map<String, Path> swaggerPaths = swagger.getPaths();
			if (swaggerPaths != null) {
				Set<String> pathsSet = swaggerPaths.keySet();
				for (String pathStr : pathsSet) {
					Path path = swaggerPaths.get(pathStr);
					List<Operation> operationList = path.getOperations();
					if (operationList != null) {
						for (Operation operation : operationList) {
							Map<String, Response> responseMap = operation.getResponses();
							if (responseMap != null) {
								Set<String> responseSet = responseMap.keySet();
								for (String key : responseSet) {
									Response response = responseMap.get(key);
									if (response != null) {
										response.setResponseSchema(null);
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return swagger;
	}

	public Swagger3VO genarateSwaggerJsonDefinations(Swagger3VO swaggerVO, List<RowData> rowDataList, Integer revision)
			throws Exception {
		log("genarateSwaggerJsonDefinations", swaggerVO.getInteractionid(), swaggerVO);
		Swagger3VO dbswaggerVO = getSwagger3(swaggerVO.getName(), "");
		if (dbswaggerVO != null) {
			swaggerVO.setName(dbswaggerVO.getName());
		}
		Swagger3VO details = null;
		PopulateSwaggerDefination populateSwaggerDefination = new PopulateSwaggerDefination();
		OpenAPI swagger = populateSwaggerDefination.populateDefinitons(rowDataList, new OpenAPI());
		if (revision == null) {
			swaggerVO.setRevision(1);
			swaggerVO.setStatus("Draft");
			swaggerVO.setLock(false);
			swaggerVO.setId(null);
			io.swagger.v3.oas.models.info.Info info = new io.swagger.v3.oas.models.info.Info();
			info.setTitle(swaggerVO.getName());
			swagger.setInfo(info);
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			String swaggerString = mapper.writeValueAsString(swagger);
			swaggerVO.setSwagger(swaggerString);
			// swaggerVO.setSwagger(swagger);
			details = baseRepository.save(swaggerVO);
		} else {
			swaggerVO = baseRepository.findOne("name", swaggerVO.getName(), "revision", revision, Swagger3VO.class);
			if (swaggerVO != null) {
				// OpenAPI s = getOpenAPI(swaggerVO.getSwagger().toString());
				// Components components = s.getComponents();
				// Map<String, Schema> schemas = components.getSchemas();
				// schemas.putAll(swagger.getComponents().getSchemas());
				// components.setSchemas(schemas);
				// s.setComponents(components);
				// ObjectMapper mapper = new ObjectMapper();
				// mapper.setSerializationInclusion(Include.NON_NULL);
				String swaggerString = populateSchemas(swaggerVO.getSwagger().toString(),
						swagger.getComponents().getSchemas());
				// String swaggerString = mapper.writeValueAsString(s);
				// System.out.println(swaggerString);
				swaggerVO.setSwagger(swaggerString);
				details = baseRepository.save(swaggerVO);
			}
		}
		log("genarateSwaggerJsonDefinations", swaggerVO.getInteractionid(), details);
		return details;
	}

	private String populateSchemas(String swagger, Map<String, Schema> schemas) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		OpenAPI oAPI = getOpenAPI(swagger);
		Components components = oAPI.getComponents();
		Map<String, Schema> apischemas = components.getSchemas();
		apischemas.putAll(schemas);
		components.setSchemas(apischemas);
		String componentsStr = "";
		try {
			componentsStr = mapper.writeValueAsString(components);
		} catch (JsonProcessingException e1) {
			log.error("Exception occurred", e1);
		}

		try {
			ObjectNode objNode = (ObjectNode) mapper.readTree(swagger);
			// ObjectNode compNode = (ObjectNode)
			// mapper.readTree(componentsStr);
			// objNode.put("components", compNode);
			ObjectNode componentNode = (ObjectNode) objNode.get("components");
			componentNode.putPOJO("schemas", apischemas);
			objNode.put("components", componentNode);

			// System.out.println(mapper.writeValueAsString(objNode));
			String swaggerStr = mapper.writeValueAsString(objNode).replaceAll("/definitions/", "/components/schemas/");
			swaggerStr = swaggerStr.replaceAll(",\"extensions\":\\{}", "");
			System.out.println(swaggerStr);
			return swaggerStr;
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JSONObject swaggerObj = new JSONObject(swagger);
		if (componentsStr != "") {
			JSONObject componentsObj = new JSONObject(componentsStr);
			swaggerObj.put("components", componentsObj);
		}
		// JSONObject componentsObj = swaggerObj.getJSONObject("components");
		// JSONObject schemasObj;
		// if(componentsObj == null){
		// componentsObj = new JSONObject();
		// schemasObj = new JSONObject();
		// componentsObj.put("schemas", schemasObj);
		// }
		// else
		// schemasObj = componentsObj.getJSONObject("schemas");
		// for(String key: schemas.keySet()){
		// schemasObj.put(key, schemas.get(key));
		// }
		// componentsObj.put("schemas", schemasObj);
		// swaggerObj.put("components", componentsObj);
		try {
			return mapper.writeValueAsString(swaggerObj);
		} catch (JsonProcessingException e) {
			log.error("Exception occurred", e);
		}
		return swagger;
	}

	private OpenAPI getOpenAPI(String swagger) {
		ParseOptions options = new ParseOptions();
		options.setResolve(true);
		options.setResolveCombinators(false);
		options.setResolveFully(true);
		return new OpenAPIV3Parser().readContents(swagger, null, options).getOpenAPI();
	}

	public void createReview(SwaggerReview swaggerReview) throws MessagingException {
		log("createReview", swaggerReview.getInteractionid(), swaggerReview);
		swaggerReview = baseRepository.save(swaggerReview);
		for (String teamName : swaggerReview.getTeamNames()) {
			SwaggerTeam team = baseRepository.findOne("name", teamName, SwaggerTeam.class);
			if (team != null) {
				EmailTemplate emailTemplate = new EmailTemplate();
				emailTemplate.setToMailId(getCOntactForTeam(team));
				// TODO need to add dynamic message.
				emailTemplate.setSubject(
						"-- Subject : Review Alert - for the following Swagger :" + swaggerReview.getSwaggerName());
				emailTemplate.setBody("Please finish the review and update the comments ASAP");
				emailTemplate.setFotter("Thanks, Support Team");
				mailUtil.sendEmail(emailTemplate);
			}
		}
	}

	public void createOrUpdateReviewComment(SwaggerReviewComents swaggerReviewComents) throws ItorixException {
		log("createOrUpdateReviewComment", swaggerReviewComents.getInteractionid(), swaggerReviewComents);
		if (swaggerReviewComents.getExternalFlag() != null && swaggerReviewComents.getExternalFlag().equals("true")) {
			swaggerReviewComents = baseRepository.save(swaggerReviewComents);
		} else {
			swaggerReviewComents = baseRepository.save(swaggerReviewComents);
		}
	}

	public void createOrUpdateReviewComment(Swagger3ReviewComents swaggerReviewComents) throws ItorixException {
		log("createOrUpdateReviewComment", swaggerReviewComents.getInteractionid(), swaggerReviewComents);
		swaggerReviewComents = baseRepository.save(swaggerReviewComents);
	}

	private User findByEmail(String email) {
		Query query = new Query();
		query.addCriteria(Criteria.where(User.LABEL_EMAIL).is(email));
		return masterMongoTemplate.findOne(query, User.class);
	}

	public void updateReviewComment(SwaggerReviewComents swaggerReviewComents) throws Exception {
		log("updateReviewComment", swaggerReviewComents.getInteractionid(), swaggerReviewComents);
		SwaggerReviewComents sc = baseRepository.findById(swaggerReviewComents.getCommentId(),
				SwaggerReviewComents.class);
		if (sc != null) {
			sc.setComment(swaggerReviewComents.getComment());
			swaggerReviewComents = baseRepository.save(sc);
		} else {
			throw new Exception();
		}
	}

	public void updateReviewComment(Swagger3ReviewComents swaggerReviewComents) throws Exception {
		log("updateReviewComment", swaggerReviewComents.getInteractionid(), swaggerReviewComents);
		Swagger3ReviewComents sc = baseRepository.findById(swaggerReviewComents.getId(), Swagger3ReviewComents.class);
		if (sc != null) {
			sc.setComment(swaggerReviewComents.getComment());
			swaggerReviewComents = baseRepository.save(sc);
		} else {
			throw new Exception();
		}
	}

	public ObjectNode getReviewComment(SwaggerReviewComents swaggerReviewComents)
			throws MessagingException, ItorixException {
		log("getReviewComment", swaggerReviewComents.getInteractionid(), swaggerReviewComents);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		ArrayNode arrayNode = mapper.createArrayNode();
		List<SwaggerReviewComents> list = getReviewComments(swaggerReviewComents);
		for (SwaggerReviewComents sc : list) {
			ObjectNode objectNode = mapper.createObjectNode();
			objectNode.put("id", sc.getId());
			objectNode.put("swaggerName", sc.getSwaggerName());
			objectNode.put("revision", sc.getRevision());
			objectNode.put("comment", sc.getComment());
			if (sc.getExternalFlag() != null) {
				objectNode.put("externalFlag", sc.getExternalFlag());
			}
			User u1 = null;
			u1 = getUser(sc.getCreatedBy());
			if (u1 != null) {
				objectNode.put("createdUserEmail", u1.getEmail());
				u1 = null;
			}

			u1 = getUser(sc.getModifiedBy());
			if (u1 != null) {
				objectNode.put("modifiedUserEmail", u1.getEmail());
				u1 = null;
			}
			objectNode.put("cts", sc.getCts());
			objectNode.put("createdBy", sc.getCreatedUserName());
			objectNode.put("modifiedBy", sc.getModifiedUserName());
			objectNode.put("mts", sc.getMts());
			sc.setCommentId(sc.getId());
			List<SwaggerReviewComents> list2 = getReviewComments(sc);
			ArrayNode replayNode = mapper.createArrayNode();
			for (SwaggerReviewComents sc2 : list2) {
				ObjectNode objectNode1 = mapper.createObjectNode();
				objectNode1.put("id", sc2.getId());
				objectNode1.put("swaggerName", sc2.getSwaggerName());
				objectNode1.put("revision", sc2.getRevision());
				objectNode1.put("comment", sc2.getComment());
				if (sc2.getExternalFlag() != null) {
					objectNode1.put("externalFlag", sc2.getExternalFlag());
				}
				User u2 = null;
				u2 = getUser(sc2.getCreatedBy());
				if (u2 != null) {
					objectNode1.put("createdUserEmail", u2.getEmail());
					u2 = null;
				}

				u2 = getUser(sc2.getModifiedBy());
				if (u2 != null) {
					objectNode1.put("modifiedUserEmail", u2.getEmail());
					u2 = null;
				}
				objectNode1.put("cts", sc2.getCts());
				objectNode1.put("createdBy", sc2.getCreatedUserName());
				objectNode1.put("modifiedBy", sc2.getModifiedUserName());
				objectNode1.put("mts", sc2.getMts());
				sc2.setCommentId(sc2.getId());

				replayNode.add(objectNode1);
				addReplayNodes(mapper, sc2, replayNode);
			}
			objectNode.set("tags", replayNode);
			arrayNode.add(objectNode);
		}
		rootNode.set("reviews", arrayNode);
		return rootNode;
	}

	public ObjectNode getReviewComment(Swagger3ReviewComents swaggerReviewComents)
			throws MessagingException, ItorixException {
		log("getReviewComment", swaggerReviewComents.getInteractionid(), swaggerReviewComents);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		ArrayNode arrayNode = mapper.createArrayNode();
		List<Swagger3ReviewComents> list = getReviewComments(swaggerReviewComents);
		for (Swagger3ReviewComents sc : list) {
			ObjectNode objectNode = mapper.createObjectNode();
			objectNode.put("id", sc.getId());
			objectNode.put("swaggerName", sc.getSwaggerName());
			objectNode.put("revision", sc.getRevision());
			objectNode.put("comment", sc.getComment());
			if (sc.getExternalFlag() != null) {
				objectNode.put("externalFlag", sc.getExternalFlag());
			}
			User u1 = null;
			u1 = getUser(sc.getCreatedBy());
			if (u1 != null) {
				objectNode.put("createdUserEmail", u1.getEmail());
				u1 = null;
			}

			u1 = getUser(sc.getModifiedBy());
			if (u1 != null) {
				objectNode.put("modifiedUserEmail", u1.getEmail());
				u1 = null;
			}
			objectNode.put("cts", sc.getCts());
			objectNode.put("createdBy", sc.getCreatedUserName());
			objectNode.put("modifiedBy", sc.getModifiedUserName());
			objectNode.put("mts", sc.getMts());
			sc.setCommentId(sc.getId());
			List<Swagger3ReviewComents> list2 = getReviewComments(sc);
			ArrayNode replayNode = mapper.createArrayNode();
			for (Swagger3ReviewComents sc2 : list2) {
				ObjectNode objectNode1 = mapper.createObjectNode();
				objectNode1.put("id", sc2.getId());
				objectNode1.put("swaggerName", sc2.getSwaggerName());
				objectNode1.put("revision", sc2.getRevision());
				objectNode1.put("comment", sc2.getComment());
				if (sc2.getExternalFlag() != null) {
					objectNode1.put("externalFlag", sc2.getExternalFlag());
				}
				User u2 = null;
				u2 = getUser(sc2.getCreatedBy());
				if (u2 != null) {
					objectNode1.put("createdUserEmail", u2.getEmail());
					u2 = null;
				}

				u2 = getUser(sc2.getModifiedBy());
				if (u2 != null) {
					objectNode1.put("modifiedUserEmail", u2.getEmail());
					u2 = null;
				}
				objectNode1.put("cts", sc2.getCts());
				objectNode1.put("createdBy", sc2.getCreatedUserName());
				objectNode1.put("modifiedBy", sc2.getModifiedUserName());
				objectNode1.put("mts", sc2.getMts());
				sc2.setCommentId(sc2.getId());

				replayNode.add(objectNode1);
				addReplayNodes(mapper, sc2, replayNode);
			}
			objectNode.set("tags", replayNode);
			arrayNode.add(objectNode);
		}
		rootNode.set("reviews", arrayNode);
		return rootNode;
	}

	public void addReplayNodes(ObjectMapper mapper, SwaggerReviewComents sc, ArrayNode replayNode) {
		List<SwaggerReviewComents> list = getReviewComments(sc);
		if (list != null) {
			for (SwaggerReviewComents sc2 : list) {
				ObjectNode objectNode1 = mapper.createObjectNode();
				objectNode1.put("id", sc2.getId());
				objectNode1.put("swaggerName", sc2.getSwaggerName());
				objectNode1.put("revision", sc2.getRevision());
				objectNode1.put("comment", sc2.getComment());
				User u2 = null;
				u2 = getUser(sc2.getCreatedBy());
				if (u2 != null) {
					objectNode1.put("createdUserEmail", u2.getEmail());
					u2 = null;
				}

				u2 = getUser(sc2.getModifiedBy());
				if (u2 != null) {
					objectNode1.put("modifiedUserEmail", u2.getEmail());
					u2 = null;
				}
				objectNode1.put("cts", sc2.getCts());
				objectNode1.put("createdBy", sc2.getCreatedUserName());
				objectNode1.put("modifiedBy", sc2.getModifiedUserName());
				objectNode1.put("mts", sc2.getMts());
				sc2.setCommentId(sc2.getId());
				addReplayNodes(mapper, sc2, replayNode);
				replayNode.add(objectNode1);
			}
		}
	}

	/**
	 * Add replay nodes.
	 *
	 * @param mapper     the mapper
	 * @param sc         the sc
	 * @param replayNode the replay node
	 */
	public void addReplayNodes(ObjectMapper mapper, Swagger3ReviewComents sc, ArrayNode replayNode) {
		List<Swagger3ReviewComents> list = getReviewComments(sc);
		if (list != null) {
			for (Swagger3ReviewComents sc2 : list) {
				ObjectNode objectNode1 = mapper.createObjectNode();
				objectNode1.put("id", sc2.getId());
				objectNode1.put("swaggerName", sc2.getSwaggerName());
				objectNode1.put("revision", sc2.getRevision());
				objectNode1.put("comment", sc2.getComment());
				User u2 = null;
				u2 = getUser(sc2.getCreatedBy());
				if (u2 != null) {
					objectNode1.put("createdUserEmail", u2.getEmail());
					u2 = null;
				}

				u2 = getUser(sc2.getModifiedBy());
				if (u2 != null) {
					objectNode1.put("modifiedUserEmail", u2.getEmail());
					u2 = null;
				}
				objectNode1.put("cts", sc2.getCts());
				objectNode1.put("createdBy", sc2.getCreatedUserName());
				objectNode1.put("modifiedBy", sc2.getModifiedUserName());
				objectNode1.put("mts", sc2.getMts());
				sc2.setCommentId(sc2.getId());
				addReplayNodes(mapper, sc2, replayNode);
				replayNode.add(objectNode1);
			}
		}
	}

	private List<SwaggerReviewComents> getReviewComments(SwaggerReviewComents sc) {
		SwaggerVO swaggerVO = getSwagger(sc.getSwaggerName(), "");
		if (swaggerVO != null) {
			List<SwaggerReviewComents> list = baseRepository.find("swaggerName", swaggerVO.getName(), "revision",
					sc.getRevision(), "commentId", sc.getCommentId(), SwaggerReviewComents.class);
			return list;
		}
		return null;
	}

	private List<Swagger3ReviewComents> getReviewComments(Swagger3ReviewComents sc) {
		Swagger3VO swaggerVO = getSwagger3(sc.getSwaggerName(), "");
		if (swaggerVO != null) {
			List<Swagger3ReviewComents> list = baseRepository.find("swaggerName", swaggerVO.getName(), "revision",
					sc.getRevision(), "commentId", sc.getCommentId(), Swagger3ReviewComents.class);
			return list;
		}
		return null;
	}

	private List<String> getCOntactForTeam(SwaggerTeam team) {
		List<String> contactList = team.getContacts().stream().filter(o -> !o.getEmail().isEmpty())
				.map(o -> o.getEmail()).collect(Collectors.toList());
		// for (SwaggerContacts contact : team.getContacts())
		// contactList.add(contact.getEmail());
		return contactList;
	}

	public void deleteSwagger(String name, String interactionid) {
		log("deleteSwagger", interactionid, name);
		baseRepository.delete("swaggerName", name, SwaggerReviewComents.class);
		baseRepository.delete("name", name, SwaggerVO.class);
	}

	public void deleteSwagger3(String name, String interactionid) {
		log("deleteSwagger", interactionid, name);
		baseRepository.delete("swaggerName", name, Swagger3ReviewComents.class);
		baseRepository.delete("name", name, Swagger3VO.class);
	}

	public void deleteSwaggerVersion(String name, Integer revision, String interactionid) {
		log("deleteSwaggerVersion", interactionid, name, revision);
		baseRepository.delete("swaggerName", name, "revision", revision, SwaggerReviewComents.class);
		baseRepository.delete("name", name, "revision", revision, SwaggerVO.class);
	}

	public void deleteSwagger3Version(String name, Integer revision, String interactionid) {
		log("deleteSwaggerVersion", interactionid, name, revision);
		baseRepository.delete("swaggerName", name, "revision", revision, Swagger3ReviewComents.class);
		baseRepository.delete("name", name, "revision", revision, Swagger3VO.class);
	}

	private void log(String methodName, String interactionid, Object... body) {

		logger.debug("SwaggerService." + methodName + " | CorelationId=" + interactionid + " | request/response Body ="
				+ body);
	}

	@SuppressWarnings("unused")
	private Set<String> symmetricDifference(Set<String> existing, Set<String> newSet) {
		Set<String> result = new HashSet<String>(existing);
		for (String element : newSet) {
			if (!result.add(element)) {
				result.remove(element);
			}
		}
		return result;
	}

	@SuppressWarnings("resource")
	private void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) {
		File file = null;
		try {
			OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
			int read = 0;
			byte[] bytes = new byte[1024];
			file = new File(uploadedFileLocation);
			out = new FileOutputStream(file);
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			log.error("Exception occurred", e);
		}
	}

	public SwaggerObjectResponse getSwaggerStats(String timeunit, String timerange)
			throws ParseException, ItorixException {
		log("getSwaggerStats", timeunit, timerange);
		// TODO: Verify and remove blocks of code
		// ObjectMapper mapper = new ObjectMapper();
		SwaggerObjectResponse swaggerObjectResponse = new SwaggerObjectResponse();

		boolean populateSwaggers = false;
		if (timeunit != null && timerange != null) {
			populateSwaggers = true;
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			String[] dates = timerange.split("~");
			Date startDate = null;
			Date endDate = null;
			if (dates != null && dates.length > 0) {
				startDate = dateFormat.parse(dates[0]);
				endDate = dateFormat.parse(dates[1]);
			}
			Metrics metricsNode = new Metrics();
			metricsNode.setType(timeunit);
			HashMap<String, Object> valuesNode = new HashMap<>();
			while (startDate.compareTo(endDate) <= 0) {
				Query query = new Query();
				query.addCriteria(Criteria.where(LABEL_CREATED_TIME)
						.gte(new Long(getStartOfDay(startDate).getTime() + ""))
						.lt(new Long(getEndOfDay(startDate).getTime() + "")));
				List<SwaggerVO> list = baseRepository.find(query, SwaggerVO.class);
				if (list != null && list.size() > 0) {
					valuesNode.put("timestamp", getStartOfDay(startDate).getTime() + "");
					valuesNode.put("value", list.size());

				}
				startDate = DateUtil.addDays(startDate, 1);
			}
			metricsNode.setValues(valuesNode);
			swaggerObjectResponse.setMetrics(metricsNode);
		}

		Aggregation aggregation = newAggregation(group(STATUS_VALUE).count().as("count"),
				project("count").and(STATUS_VALUE).previousOperation());
		AggregationResults aggregationResults = baseRepository.addAggregation(aggregation, "Design.Swagger.List",
				SwaggerVO.class);
		ArrayList<Document> documentlist = (ArrayList) aggregationResults.getRawResults().get("results");
		List<Stat> statsList = new ArrayList<>();
		documentlist.forEach(document -> {
			Stat stats = new Stat();
			stats.setName(document.get("status").toString());
			stats.setCount(document.get("count").toString());
			statsList.add(stats);
		});
		swaggerObjectResponse.setStats(statsList);
		log.debug("swaggerObjectResponse : {}", swaggerObjectResponse);

		// TODO: Verify and remove blocks of code
		/*
		 * if (distinctList != null && distinctList.size() > 0) { for (String
		 * status : distinctList) { Query query = new Query();
		 * query.addCriteria(Criteria.where("status").is(status));
		 * List<SwaggerVO> publishList = baseRepository.find(query,
		 * SwaggerVO.class); ObjectNode statNode = mapper.createObjectNode();
		 * ArrayNode swaggersNode = mapper.createArrayNode();
		 * statNode.put("name", status); if (publishList != null &&
		 * publishList.size() > 0) { for (SwaggerVO vo : publishList) {
		 * ObjectNode swaggerNode = mapper.createObjectNode();
		 * swaggerNode.put("name", vo.getName()); swaggerNode.put("revision",
		 * vo.getRevision()); swaggersNode.add(swaggerNode); }
		 * statNode.put("count",aggregationResults.getRawResults().get("results"
		 * ).toString()); } else { statNode.put("count", 0); } if
		 * (populateSwaggers == true) statNode.set("swaggers", swaggersNode);
		 * statsNode.add(statNode); } } rootNode.set("stats",
		 * SwaggerObjectResponse);
		 */
		return swaggerObjectResponse;
	}

	public ObjectNode getSwagger3Stats(String timeunit, String timerange) throws ParseException, ItorixException {
		log("getSwagger3Stats", timeunit, timerange);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		boolean populateSwaggers = false;
		if (timeunit != null && timerange != null) {
			populateSwaggers = true;
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			String[] dates = timerange.split("~");

			Date startDate = null;
			Date endDate = null;
			if (dates != null && dates.length > 0) {
				startDate = dateFormat.parse(dates[0]);
				endDate = dateFormat.parse(dates[1]);
			}
			ObjectNode metricsNode = mapper.createObjectNode();
			metricsNode.put("type", timeunit);
			ArrayNode valuesNode = mapper.createArrayNode();
			while (startDate.compareTo(endDate) <= 0) {
				Query query = new Query();
				query.addCriteria(Criteria.where(LABEL_CREATED_TIME)
						.gte(new Long(getStartOfDay(startDate).getTime() + ""))
						.lt(new Long(getEndOfDay(startDate).getTime() + "")));
				List<Swagger3VO> list = baseRepository.find(query, Swagger3VO.class);
				if (list != null && list.size() > 0) {
					ObjectNode valueNode = mapper.createObjectNode();
					valueNode.put("timestamp", getStartOfDay(startDate).getTime() + "");
					valueNode.put("value", list.size());
					valuesNode.add(valueNode);
				}
				startDate = DateUtil.addDays(startDate, 1);
			}
			metricsNode.set("values", valuesNode);
			rootNode.set("metrics", metricsNode);
		}
		ArrayNode statsNode = mapper.createArrayNode();
		List<String> distinctList = baseRepository.findDistinctValuesByColumnName(Swagger3VO.class, STATUS_VALUE);
		if (distinctList != null && distinctList.size() > 0) {
			for (String status : distinctList) {
				Query query = new Query();
				query.addCriteria(Criteria.where(STATUS_VALUE).is(status));
				List<Swagger3VO> publishList = baseRepository.find(query, Swagger3VO.class);
				ObjectNode statNode = mapper.createObjectNode();
				ArrayNode swaggersNode = mapper.createArrayNode();
				statNode.put("name", status);
				if (publishList != null && publishList.size() > 0) {
					for (Swagger3VO vo : publishList) {
						ObjectNode swaggerNode = mapper.createObjectNode();
						swaggerNode.put("name", vo.getName());
						swaggerNode.put("revision", vo.getRevision());
						swaggersNode.add(swaggerNode);
					}
					statNode.put("count", publishList.size());
				} else {
					statNode.put("count", 0);
				}
				if (populateSwaggers == true) {
					statNode.set("swaggers", swaggersNode);
				}
				statsNode.add(statNode);
			}
		}

		rootNode.set("stats", statsNode);

		return rootNode;
	}

	public ObjectNode getTeamStats(String timeunit, String timerange) throws ParseException, ItorixException {
		log("getTeamStats", timeunit, timerange);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		String[] dates = timerange.split("~");

		Date startDate = null;
		Date endDate = null;
		if (dates != null && dates.length > 0) {
			startDate = dateFormat.parse(dates[0]);
			endDate = dateFormat.parse(dates[1]);
		}
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		ObjectNode metricsNode = mapper.createObjectNode();
		metricsNode.put("type", timeunit);
		ArrayNode valuesNode = mapper.createArrayNode();
		while (startDate.compareTo(endDate) <= 0) {
			Query query = new Query();
			query.addCriteria(Criteria.where(LABEL_CREATED_TIME)
					.gte(new Long(getStartOfDay(startDate).getTime() + ""))
					.lt(new Long(getEndOfDay(startDate).getTime() + "")));
			List<SwaggerTeam> list = baseRepository.find(query, SwaggerTeam.class);
			if (list != null && list.size() > 0) {
				ObjectNode valueNode = mapper.createObjectNode();
				valueNode.put("timestamp", getStartOfDay(startDate).getTime() + "");
				valueNode.put("value", list.size());
				valuesNode.add(valueNode);
			}
			startDate = DateUtil.addDays(startDate, 1);
		}
		metricsNode.set("values", valuesNode);
		ArrayNode statsNode = mapper.createArrayNode();

		List<String> distinctList = baseRepository.findDistinctValuesByColumnName(SwaggerTeam.class, "name");
		if (distinctList != null && distinctList.size() > 0) {
			for (String name : distinctList) {

				SwaggerTeam team = baseRepository.findOne("name", name, SwaggerTeam.class);
				ObjectNode statNode = mapper.createObjectNode();
				statNode.put("name", name);
				if (team != null && team.getContacts() != null) {
					statNode.put("contactscount", team.getContacts().size());
				} else {
					statNode.put("contactscount", 0);
				}
				if (team != null && team.getSwaggers() != null) {
					statNode.put("swaggerscount", team.getSwaggers().size());
				} else {
					statNode.put("swaggerscount", 0);
				}
				statsNode.add(statNode);
			}
		}
		rootNode.set("metrics", metricsNode);
		rootNode.set("stats", statsNode);

		return rootNode;
	}

	public void associateProduct(String swaggerName, Set<String> productSet, String oas) throws ItorixException {
		Query query = new Query(Criteria.where("swaggerName").is(swaggerName).and("oas").is(oas));
		SwaggerMetadata metadata = mongoTemplate.findOne(query, SwaggerMetadata.class);
		if (metadata == null) {
			metadata = new SwaggerMetadata();
			metadata.setSwaggerName(swaggerName);
			metadata.setOas(oas);
		}
		metadata.setProducts(productSet);
		mongoTemplate.save(metadata);
	}

	public SwaggerMetadata getSwaggerMetadata(String swaggerName, String oas) throws ItorixException {
		Query query = new Query(Criteria.where("swaggerName").is(swaggerName).and("oas").is(oas));
		SwaggerMetadata metadata = mongoTemplate.findOne(query, SwaggerMetadata.class);
		return metadata;
	}

	public void assoiateTeamsToProject(String team_name, Set<String> projectSet, String interactionId)
			throws ItorixException {
		SwaggerTeam team = baseRepository.findOne("name", team_name, SwaggerTeam.class);
		if (team != null) {
			if (team.getProjects() == null) {
				team.setProjects(new HashSet<>());
			}
			team.setProjects(projectSet);
			baseRepository.save(team);
		} else {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Teams-1001"), team_name),
					"Teams-1001");
		}
	}

	public void associatePortfolio(String swaggerName, Set<String> portfolioSet, String interactionId)
			throws ItorixException {
		SwaggerVO vo = baseRepository.findOne("name", swaggerName, SwaggerVO.class);
		if (vo != null) {
			vo.setPortfolios(portfolioSet);
			baseRepository.save(vo);
		} else {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
		}
	}

	public SwaggerTeam findSwaggerTeam(String team_name, String interactionid) throws ItorixException {
		return baseRepository.findOne("name", team_name, SwaggerTeam.class);
	}

	private void publishBasepaths() {
		List<String> names = baseRepository.findDistinctValuesByColumnName(SwaggerVO.class, "name");
		for (String name : names) {
			List<Revision> versions = getListOfRevisions(name, null);
			SwaggerVO vo = null;
			if (versions != null && versions.size() > 0) {
				Revision revision = Collections.max(versions);
				vo = baseRepository.findOne("name", name, "revision", revision.getRevision(), SwaggerVO.class);
			}
			if (vo != null) {
				updateSwaggerBasePath(name, vo);
			}
		}
	}

	private void publishSwagger3Basepaths() {
		List<String> names = baseRepository.findDistinctValuesByColumnName(Swagger3VO.class, "name");
		for (String name : names) {
			List<Revision> versions = getListOf3Revisions(name, null);
			Swagger3VO vo = null;
			if (versions != null && versions.size() > 0) {
				Revision revision = Collections.max(versions);
				vo = baseRepository.findOne("name", name, "revision", revision.getRevision(), Swagger3VO.class);
			}
			if (vo != null) {
				updateSwagger3BasePath(name, vo);
			}
		}
	}

	public void updateSwaggerBasePath(String name, SwaggerVO vo) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode swaggerJson = null;
		try {
			swaggerJson = mapper.readTree(vo.getSwagger());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (swaggerJson != null) {
			String basepath = getBasepath(swaggerJson);
			Swagger2BasePath basePath = new Swagger2BasePath();
			basePath.setName(name);
			basePath.setBasePath(basepath);
			saveSwagger2BasePath(basePath);
		}
	}

	public void updateSwagger3BasePath(String name, Swagger3VO vo) {
		logger.info("Updating Swagger3BasePath for URL {}", vo.getName());
		SwaggerParseResult swaggerParseResult = new OpenAPIParser().readContents(vo.getSwagger(), null, null);
		List<Server> servers = swaggerParseResult.getOpenAPI().getServers();
		Set<String> basePaths = new HashSet();

		for (Server server : servers) {
			String urlStr = getReplacedURLStr(server);
			try {
				URL url = new URL(urlStr);
				basePaths.add(url.getPath());
			} catch (MalformedURLException e) {
				logger.error("Error while getting basePath for Swagger: {} URL {} ", vo.getName(), e.getMessage());
			}
		}
		if (!basePaths.isEmpty()) {
			Swagger3BasePath basePath = new Swagger3BasePath();
			basePath.setName(name);
			basePath.setBasePath(new LinkedList<>(basePaths));
			saveSwagger3BasePath(basePath);
		}
	}

	private String getReplacedURLStr(Server server) {
		ServerVariables variables = server.getVariables();
		String urlStr = server.getUrl();

		if (variables != null) {
			for (String k : variables.keySet()) {
				if (server.getUrl().contains("{" + k + "}")) {
					urlStr = urlStr.replace("{" + k + "}", variables.get(k).getDefault());
				}
			}
		}
		return urlStr;
	}

	/**
	 * Save swagger 2 base path.
	 *
	 * @param basePath the base path
	 */
	public void saveSwagger2BasePath(Swagger2BasePath basePath) {
		Query query = new Query(Criteria.where("name").is(basePath.getName()));
		Update update = new Update();
		update.set("basePath", basePath.getBasePath());
		mongoTemplate.upsert(query, update, Swagger2BasePath.class);
	}

	/**
	 * Save swagger 3 base path.
	 *
	 * @param basePath the base path
	 */
	public void saveSwagger3BasePath(Swagger3BasePath basePath) {
		Query query = new Query(Criteria.where("name").is(basePath.getName()));
		Update update = new Update();
		update.set("basePath", basePath.getBasePath());
		mongoTemplate.upsert(query, update, Swagger3BasePath.class);
	}

	private List<Swagger2BasePath> getSwagger2BasePaths() {
		List<Swagger2BasePath> mappings = null;
		try {
			mappings = mongoTemplate.findAll(Swagger2BasePath.class);
		} catch (Exception ex) {
		}
		if (mappings == null || mappings.size() == 0) {
			publishBasepaths();
			mappings = mongoTemplate.findAll(Swagger2BasePath.class);
		}
		return mappings;
	}

	private List<Swagger3BasePath> getSwagger3BasePaths() {
		List<Swagger3BasePath> mappings = null;
		try {
			mappings = mongoTemplate.findAll(Swagger3BasePath.class);
		} catch (Exception ex) {
			logger.error("Error while finding Swagger3BasePath {} ", ex.getMessage());
		}
		if (mappings == null || mappings.size() == 0) {
			publishSwagger3Basepaths();
			mappings = mongoTemplate.findAll(Swagger3BasePath.class);
		}
		return mappings;
	}

	public Object getSwagger2BasePathsObj() {
		List<Swagger2BasePath> mappings = getSwagger2BasePaths();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode objectNode = mapper.createObjectNode();
		objectNode.set("swaggerBasePathMapping", getJsonNode(mappings));
		objectNode.set("basePaths", getJsonNode(getBasePaths(mappings)));
		return objectNode;
	}

	public Object getSwagger3BasePathsObj() {
		List<Swagger3BasePath> mappings = getSwagger3BasePaths();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode objectNode = mapper.createObjectNode();
		objectNode.set("swaggerBasePathMapping", getJsonNode(mappings));
		objectNode.set("basePaths", getJsonNode(getSwagger3BasePaths(mappings)));
		return objectNode;
	}

	private Set<String> getBasePaths(List<Swagger2BasePath> mappings) {
		Set<String> basepaths = new HashSet<String>();
		for (Swagger2BasePath swagger2BasePath : mappings) {
			basepaths.add(swagger2BasePath.getBasePath());
		}
		return basepaths;
	}

	private Set<String> getSwagger3BasePaths(List<Swagger3BasePath> mappings) {
		Set<String> basepaths = new HashSet<String>();
		for (Swagger3BasePath swagger3BasePath : mappings) {
			basepaths.addAll(swagger3BasePath.getBasePath());
		}
		return basepaths;
	}

	private String getBasepath(JsonNode swaggerJson) {
		try {
			return swaggerJson.get("basePath").textValue();
		} catch (Exception e) {
			// logger.error(e);
			// logger.info(swaggerJson);
			return "/";
		}
	}

	private JsonNode getJsonNode(Object mappings) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = null;
		try {
			jsonNode = mapper.readTree(mapper.writeValueAsString(mappings));
		} catch (JsonProcessingException e) {
			log.error("Exception occurred", e);
		} catch (IOException e) {
			log.error("Exception occurred", e);
		}
		return jsonNode;
	}

	/**
	 * Gets end of day.
	 *
	 * @param date the date
	 * @return the end of day
	 */
	public static Date getEndOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 00);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}

	/**
	 * Gets start of day.
	 *
	 * @param date the date
	 * @return the start of day
	 */
	public static Date getStartOfDay(Date date) {
		LocalDateTime localDateTime = dateToLocalDateTime(date);
		LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
		return localDateTimeToDate(startOfDay);
	}

	/**
	 * Gets particular hour of day.
	 *
	 * @param date     the date
	 * @param adjuster the adjuster
	 * @return the particular hour of day
	 */
	public static Date getParticularHourOfDay(Date date, TemporalAdjuster adjuster) {
		LocalDateTime localDateTime = dateToLocalDateTime(date);
		LocalDateTime startOfDay = localDateTime.with(adjuster);
		return localDateTimeToDate(startOfDay);
	}

	private static Date localDateTimeToDate(LocalDateTime startOfDay) {
		return Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
	}

	private static LocalDateTime dateToLocalDateTime(Date date) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
	}

	private User getUserDetailsFromSessionID(String jsessionid) {
		UserSession userSessionToken = masterMongoTemplate.findById(jsessionid, UserSession.class);
		User user = masterMongoTemplate.findById(userSessionToken.getUserId(), User.class);
		return user;
	}

	/**
	 * Gets user.
	 *
	 * @param userId the user id
	 * @return the user
	 */
	public User getUser(String userId) {
		User user = masterMongoTemplate.findById(userId, User.class);
		if (user != null) {
			return user;
		} else {
			return null;
		}
	}

	private Swagger convertToSwagger(String data) throws IOException {
		ObjectMapper mapper;

		if (data.trim().startsWith("{")) {
			mapper = Json.mapper();
		} else {
			mapper = Yaml.mapper();
		}
		// mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
		// false);
		JsonNode rootNode = mapper.readTree(data);
		// must have swagger node set
		JsonNode swaggerNode = rootNode.get("swagger");
		if (swaggerNode == null) {
			throw new IllegalArgumentException("Swagger String has an invalid format.");
		} else {
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			return mapper.convertValue(rootNode, Swagger.class);
		}
	}

	public String oasCheck(String data) throws IOException {
		ObjectMapper mapper;

		if (data.trim().startsWith("{")) {
			mapper = Json.mapper();
		} else {
			mapper = Yaml.mapper();
		}

		JsonNode rootNode = mapper.readTree(data);
		JsonNode swaggerNode = rootNode.get("openapi");
		if (swaggerNode == null) {
			swaggerNode = rootNode.get("swagger");
			if (swaggerNode != null) {
				return swaggerNode.asText();
			} else {
				throw new IllegalArgumentException("Swagger String has an invalid format.");
			}
		} else {
			return swaggerNode.asText();
		}
	}

	public Object swaggerSearch(String interactionid, String name, int limit,String jsessionid)
			throws ItorixException, JsonProcessingException {
		log("searchSwagger", interactionid, "");
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User user = getUserDetailsFromSessionID(jsessionid);
		boolean isAdmin = user.isWorkspaceAdmin(userSessionToken.getWorkspaceId());
		BasicQuery query = new BasicQuery("{\"name\": {$regex : '" + name + "', $options: 'i'}}");
		query.limit(limit > 0 ? limit : 10);
		List<String> allSwaggers = new ArrayList<>();
		if(isAdmin) {
			allSwaggers = getList(mongoTemplate.getCollection("Design.Swagger.List").distinct("name",
					query.getQueryObject(), String.class));
		}
		else{
			Map<String, Set<String>> swaggerRoles = getSwaggerPermissions("2.0", user);
			Set<String> SwaggerNames = new HashSet<>();
			SwaggerNames.addAll(swaggerRoles.keySet());
			Map<String, Object> filterFieldsAndValues = new HashMap<>();
			filterFieldsAndValues.put("createdBy", user.getId());
			List<String> trimList = baseRepository.filterAndGroupBySwaggerName(filterFieldsAndValues,
					SwaggerVO.class, null);
			SwaggerNames.addAll(trimList);
			allSwaggers = SwaggerNames.stream()
					.filter(names -> names.contains(name))
					.collect(Collectors.toList());
			allSwaggers = trimList(allSwaggers,0,limit);
		}
		Collections.sort(allSwaggers);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode response = mapper.createObjectNode();
		ArrayNode responseFields = mapper.createArrayNode();
		for (String swaggerName : allSwaggers) {
			SearchItem item = new SearchItem();
			SwaggerVO swaggerVo = baseRepository.findOne("name", swaggerName, SwaggerVO.class);
			if(swaggerVo!=null) {
				item.setId(swaggerVo.getSwaggerId());
				item.setName(swaggerName);
				responseFields.addPOJO(item);
			}
		}
		response.set("swaggers", responseFields);
		return response;
	}

	public Object swagger3Search(String interactionid, String name, int limit,String jsessionid)
			throws ItorixException, JsonProcessingException {
		log("searchSwagger", interactionid, "");
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User user = getUserDetailsFromSessionID(jsessionid);
		boolean isAdmin = user.isWorkspaceAdmin(userSessionToken.getWorkspaceId());
		BasicQuery query = new BasicQuery("{\"name\": {$regex : '" + name + "', $options: 'i'}}");
		query.limit(limit > 0 ? limit : 10);
		List<String> allSwaggers = new ArrayList<>();
		if(isAdmin) {
			allSwaggers = getList(mongoTemplate.getCollection("Design.Swagger3.List").distinct("name",
					query.getQueryObject(), String.class));
		}
		else{
			Map<String, Set<String>> swaggerRoles = getSwaggerPermissions("3.0", user);
			Set<String> SwaggerNames = new HashSet<>();
			SwaggerNames.addAll(swaggerRoles.keySet());
			Map<String, Object> filterFieldsAndValues = new HashMap<>();
			filterFieldsAndValues.put("createdBy", user.getId());
			List<String> trimList = baseRepository.filterAndGroupBySwaggerName(filterFieldsAndValues,
					Swagger3VO.class, null);
			SwaggerNames.addAll(trimList);
			allSwaggers = SwaggerNames.stream()
					.filter(names -> names.contains(name))
					.collect(Collectors.toList());
			allSwaggers = trimList(allSwaggers,0,limit);
		}
		Collections.sort(allSwaggers);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode response = mapper.createObjectNode();
		ArrayNode responseFields = mapper.createArrayNode();
		for (String swaggerName : allSwaggers) {
			SearchItem item = new SearchItem();
			Swagger3VO swaggerVo = baseRepository.findOne("name", swaggerName, Swagger3VO.class);
			if(swaggerVo!=null) {
				item.setId(swaggerVo.getSwaggerId());
				item.setName(swaggerName);
				responseFields.addPOJO(item);
			}
		}
		response.set("swaggers", responseFields);
		return response;
	}

	public void createOrUpdateGitIntegrations(String interactionid, String jsessionid, String swaggerid, String oas,
			SwaggerIntegrations swaggerIntegrations) throws ItorixException {

		String swaggerName = null;
		if (oas.equals("3.0")) {
			Swagger3VO vo = getSwagger3(swaggerid, null);
			swaggerName = vo != null ? vo.getName() : null;
		} else {
			SwaggerVO vo = getSwagger(swaggerid, null);
			swaggerName = vo != null ? vo.getName() : null;
		}
		if (swaggerName == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
		}
		swaggerIntegrations.isValid();
		SwaggerIntegrations integrations = baseRepository.findOne("swaggerName", swaggerName, "oas", oas,
				SwaggerIntegrations.class);
		if (integrations != null) {
			swaggerIntegrations.setId(integrations.getId());
		}
		swaggerIntegrations.setSwaggerId(swaggerid);
		swaggerIntegrations.setSwaggerName(swaggerName);
		swaggerIntegrations.setOas(oas);
		baseRepository.save(swaggerIntegrations);
	}

	public SwaggerIntegrations getGitIntegrations(String interactionid, String jsessionid, String swaggerid, String oas)
			throws ItorixException {
		String swaggerName = null;
		if (oas.equals("3.0")) {
			Swagger3VO vo = getSwagger3(swaggerid, null);
			swaggerName = vo != null ? vo.getName() : null;
		} else {
			SwaggerVO vo = getSwagger(swaggerid, null);
			swaggerName = vo != null ? vo.getName() : null;
		}
		if (swaggerName == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
		}
		SwaggerIntegrations integrations = baseRepository.findOne("swaggerName", swaggerName, "oas", oas,
				SwaggerIntegrations.class);
		return integrations;
	}

	public void deleteGitIntegrations(String interactionid, String jsessionid, String swaggerid, String oas)
			throws ItorixException {
		String swaggerName = null;
		if (oas.equals("3.0")) {
			Swagger3VO vo = getSwagger3(swaggerid, null);
			swaggerName = vo != null ? vo.getName() : null;
		} else {
			SwaggerVO vo = getSwagger(swaggerid, null);
			swaggerName = vo != null ? vo.getName() : null;
		}
		if (swaggerName == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
		}
		SwaggerIntegrations integrations = baseRepository.findOne("swaggerName", swaggerName, "oas", oas,
				SwaggerIntegrations.class);
		mongoTemplate.remove(integrations);
	}

	/**
	 * Load swagger open api.
	 *
	 * @param swagger the swagger
	 * @return the open api
	 */
	public OpenAPI loadSwagger(String swagger) {
		ParseOptions options = new ParseOptions();
		options.setResolve(true);
		options.setResolveCombinators(false);
		options.setResolveFully(true);
		return new OpenAPIV3Parser().readContents(swagger, null, options).getOpenAPI();
	}

	@SneakyThrows
	@Override
	public Map<String, Object> getSwaggerInfo(String jsessionid, String swaggerid, String oas) {
		if (oas.equals("3.0")) {
			Swagger3VO vo = getSwagger3(swaggerid, null);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1001"), swaggerid, "-"), "Swagger-1001");
			}
			Map<String, Object> json = parseSwaggerInfoNodes(vo.getSwagger(), oas);
			json.put("swaggerId", vo.getSwaggerId());
			json.put(STATUS_VALUE, vo.getStatus());
			json.put("createdBy", vo.getCreatedBy());
			json.put("createdUsername", vo.getCreatedUserName());
			return json;
		} else {
			SwaggerVO vo = getSwagger(swaggerid, null);
			if (vo == null) {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1001"), swaggerid, "-"), "Swagger-1001");
			}
			Map<String, Object> json = parseSwaggerInfoNodes(vo.getSwagger(), oas);
			json.put("swaggerId", vo.getSwaggerId());
			json.put(STATUS_VALUE, vo.getStatus());
			json.put("createdBy", vo.getCreatedBy());
			json.put("createdUsername", vo.getCreatedUserName());
			return json;
		}
	}

	@SneakyThrows
	private Map<String, Object> parseSwaggerInfoNodes(String swaggerJson, String oas) {
		Object version = null;
		Object name = null;
		Object description = null;
		Object basePath = null;

		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode swaggerNode = objectMapper.readTree(swaggerJson);
		JsonNode swaggerInfoNode = swaggerNode.get("info");
		name = swaggerInfoNode.get("title");
		version = swaggerInfoNode.get("version");
		description = swaggerInfoNode.get("description");

		if (oas.equals("2.0")) {
			basePath = swaggerNode.get("basePath");
		} else {
			List<Swagger3BasePath> swagger3BasePaths = getSwagger3BasePaths();
			String swaggerName = swaggerInfoNode.get("title").asText();
			Optional<Swagger3BasePath> swagger3BasePath = swagger3BasePaths.stream()
					.filter(s -> s.getName().equals(swaggerName)).findAny();
			basePath = swagger3BasePath.isPresent() ? swagger3BasePath.get().getBasePath() : null;
		}

		if (null == name) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1001")), "Swagger-1001");
		}

		Map<String, Object> json = new HashMap<>();
		json.put("name", name);
		json.put("description", description);
		json.put("version", version);
		json.put("oas", oas);
		if (null != basePath) {
			json.put("basePath", basePath);
		}
		return json;
	}

	@SneakyThrows
	@Override
	public String cloneSwagger(SwaggerCloneDetails swaggerCloneDetails, String oas) {
		// find existing swagger
		String isSwaggerCloneSuccess = null;
		if ("3.0".equals(oas)) {
			isSwaggerCloneSuccess = cloneSwagger3(swaggerCloneDetails);
		} else {
			isSwaggerCloneSuccess = cloneSwagger2(swaggerCloneDetails);
		}
		return isSwaggerCloneSuccess;
	}

	@SneakyThrows
	private String cloneSwagger2(SwaggerCloneDetails swaggerCloneDetails) throws ItorixException {
		boolean isSwaggerCloneSuccess;
		// Check Swagger Already Exists with the same name of clone
		SwaggerVO swaggerObj = getSwagger(swaggerCloneDetails.getName(), null);
		if (swaggerObj != null) {
			throw new ItorixException(
					String.format(ErrorCodes.errorMessage.get("Swagger-1002"), swaggerCloneDetails.getName()),
					"Swagger-1002");
		}
		SwaggerVO vo = null;

		if (null != swaggerCloneDetails.getRevision()) {
			vo = baseRepository.findOne("swaggerId", swaggerCloneDetails.getCurrentSwaggerID(), "revision",
					swaggerCloneDetails.getRevision(), SwaggerVO.class);
		} else {
			vo = getSwagger(swaggerCloneDetails.getCurrentSwaggerID(), null);
		}

		if (vo == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1001")), "Swagger-1001");
		}

		SwaggerVO newSwaggerForClone = new SwaggerVO();
		SwaggerUtil.copyAllSwaggerFields(newSwaggerForClone, vo);
		SwaggerUtil.setCloneDetailsFromReq(newSwaggerForClone, swaggerCloneDetails, vo.getSwagger());
		isSwaggerCloneSuccess = baseRepository.save(newSwaggerForClone) != null ? true : false;
		if (isSwaggerCloneSuccess) {
			updateSwaggerBasePath(newSwaggerForClone.getName(), newSwaggerForClone);
		}
		return isSwaggerCloneSuccess ? newSwaggerForClone.getSwaggerId() : null;
	}

	private String cloneSwagger3(SwaggerCloneDetails swaggerCloneDetails) throws ItorixException {
		boolean isSwaggerCloneSuccess;
		// Check Swagger Already Exists with the same name of clone
		Swagger3VO swaggerObj = getSwagger3(swaggerCloneDetails.getName(), null);
		if (swaggerObj != null) {
			throw new ItorixException(
					String.format(ErrorCodes.errorMessage.get("Swagger-1002"), swaggerCloneDetails.getName()),
					"Swagger-1002");
		}

		Swagger3VO vo = null;
		if (null != swaggerCloneDetails.getRevision()) {
			vo = baseRepository.findOne("swaggerId", swaggerCloneDetails.getCurrentSwaggerID(), "revision",
					swaggerCloneDetails.getRevision(), Swagger3VO.class);
		} else {
			vo = getSwagger3(swaggerCloneDetails.getCurrentSwaggerID(), null);
		}

		if (vo == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1001"),
					swaggerCloneDetails.getCurrentSwaggerID()), "Swagger-1001");
		}

		Swagger3VO newSwaggerForClone = new Swagger3VO();
		SwaggerUtil.copyAllSwaggerFields(newSwaggerForClone, vo);
		SwaggerUtil.setCloneDetailsFromReq(newSwaggerForClone, swaggerCloneDetails, vo.getSwagger());
		isSwaggerCloneSuccess = baseRepository.save(newSwaggerForClone) != null ? true : false;
		if (isSwaggerCloneSuccess) {
			updateSwagger3BasePath(newSwaggerForClone.getName(), newSwaggerForClone);
		}
		return isSwaggerCloneSuccess ? newSwaggerForClone.getSwaggerId() : null;
	}

	public List<String> getProxies(String swagger, String oas) {
		List<String> proxyNames = new ArrayList<String>();
		Query query = new Query();
		query.addCriteria(Criteria.where("codeGenHistory")
				.elemMatch(Criteria.where("proxy.buildProxyArtifact").is(swagger).and("proxy.oas").is(oas)))
				.with(Sort.by(Sort.Direction.DESC, "mts"));
		query.fields().include("proxyName");
		List<ProxyData> proxies = mongoTemplate.find(query, ProxyData.class);
		if (null != proxies) {
			for (ProxyData proxy : proxies) {
				proxyNames.add(proxy.getProxyName());
			}
		}
		return proxyNames;
	}

	public void createPartner(SwaggerPartner partner) {
		if (null == getPartnerbyName(partner)) {
			mongoTemplate.save(partner);
		}
	}

	public void updatePartner(SwaggerPartner partner) {
		SwaggerPartner swaggerPartner = getPartnerById(partner);
		if (null == swaggerPartner) {
			if (null == getPartnerbyName(partner)) {
				partner.setId(new ObjectId().toString());
				mongoTemplate.save(partner);
			}
		}else{
			if(partner.getIsDefault() != null){
				swaggerPartner.setIsDefault(partner.getIsDefault());
			}
			if(StringUtils.isNotBlank(partner.getPartnerDescription())){
				swaggerPartner.setPartnerDescription(partner.getPartnerDescription());
			}
			if(StringUtils.isNotBlank(partner.getPartnerDisplayName())){
				swaggerPartner.setPartnerDisplayName(partner.getPartnerDisplayName());
			}
			mongoTemplate.save(swaggerPartner);
		}
	}

	private SwaggerPartner getPartnerById(SwaggerPartner partner) {
		if (StringUtils.isNotEmpty(partner.getId())) {
			return mongoTemplate.findById(partner.getId(), SwaggerPartner.class);
		} else {
			return null;
		}
	}

	public void deletePartner(String partnerId) {
		Query query = new Query(Criteria.where("id").is(partnerId));
		mongoTemplate.remove(query, SwaggerPartner.class);
	}


	/**
	 * Gets partnerby name.
	 *
	 * @param partner the partner
	 * @return the partnerby name
	 */
	public SwaggerPartner getPartnerbyName(SwaggerPartner partner) {
		Query query = new Query(Criteria.where("partnerName").is(partner.getPartnerName()));
		SwaggerPartner swaggerPartner = mongoTemplate.findOne(query, SwaggerPartner.class);
		return swaggerPartner;
	}

	public List<SwaggerPartner> getPartners() {
		return mongoTemplate.findAll(SwaggerPartner.class);
	}

	public void associatePartners(String swaggerId, String oas, Set<String> partners) {
		if (oas.equals("3.0")) {
			Swagger3VO vo = getSwagger3(swaggerId, null);
			if (null != vo) {
				Query query = Query.query(
						Criteria.where("swaggerName").is(vo.getName()).and("oas").is(oas));
				SwaggerMetadata metadata = mongoTemplate.findOne(query, SwaggerMetadata.class);
				if(null== metadata){
					metadata = new SwaggerMetadata();
					metadata.setSwaggerId(swaggerId);
					metadata.setSwaggerName(vo.getName());
					metadata.setOas("3.0");
				}
				metadata.setPartners(partners);
				mongoTemplate.save(metadata);
			}
		} else {
			SwaggerVO vo = getSwagger(swaggerId, null);
			if (null != vo) {
				Query query = Query.query(
						Criteria.where("swaggerName").is(vo.getName()).and("oas").is(oas));
				SwaggerMetadata metadata = mongoTemplate.findOne(query, SwaggerMetadata.class);
				if(null== metadata){
					metadata = new SwaggerMetadata();
					metadata.setSwaggerId(swaggerId);
					metadata.setSwaggerName(vo.getName());
					metadata.setOas("2.0");
				}
				metadata.setPartners(partners);
				mongoTemplate.save(metadata);
			}
		}
	}

	public List<SwaggerPartner> getAssociatedPartners(String swaggerId, String oas) {
		log.debug("getAssociatedPartners : {}", swaggerId);
		if (oas.equals("3.0")) {
			Swagger3VO vo = getSwagger3(swaggerId, null);
			if (null != vo) {
				Query query = Query.query(
						Criteria.where("swaggerName").is(vo.getName()).and("oas").is(oas));
				SwaggerMetadata metadata = mongoTemplate.findOne(query, SwaggerMetadata.class);
				if(metadata != null ) {
					Query partnerQuery = new Query(
							Criteria.where("_id").in(metadata.getPartners()!=null && !metadata.getPartners().isEmpty() ? metadata.getPartners().stream().collect(
									Collectors.toList()) : Collections.emptyList()));
					return mongoTemplate.find(partnerQuery, SwaggerPartner.class);

				}
				else
					return Collections.emptyList();
			}
		} else {
			SwaggerVO vo = getSwagger(swaggerId, null);
			if (null != vo) {
				Query query = Query.query(
						Criteria.where("swaggerName").is(vo.getName()).and("oas").is(oas));
				SwaggerMetadata metadata = mongoTemplate.findOne(query, SwaggerMetadata.class);
				if(metadata != null ) {
					Query partnerQuery = new Query(
							Criteria.where("_id").in(metadata.getPartners()!=null && !metadata.getPartners().isEmpty() ? metadata.getPartners().stream().collect(
									Collectors.toList()) : Collections.emptyList()));
					return mongoTemplate.find(partnerQuery, SwaggerPartner.class);
				}
				else
					return Collections.emptyList();
			}
		}
		return new ArrayList<SwaggerPartner>();
	}

	private List<SwaggerPartner> getswaggerPartners(Set<String> partnerId) {
		List<SwaggerPartner> partners = new ArrayList<>();
		try {
			List<SwaggerPartner> dbPartners = mongoTemplate.findAll(SwaggerPartner.class);
			for (String partner : partnerId) {
				try {
					partners.add(
							dbPartners.stream().filter(p -> p.getId().equals(partner)).findFirst().get());
				} catch (Exception e) {
					log.error("Exception occurred", e);
				}
			}
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return partners;
	}

	private List<String> getswaggerProductsName(Set<String> productId) {
		try {
			Query query = new Query(Criteria.where("_id").in(productId));
			List<SwaggerProduct> dbProducts = mongoTemplate.find(query, SwaggerProduct.class);
			return dbProducts.stream().map(swaggerProduct -> swaggerProduct.getProductName())
					.collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return Collections.emptyList();
	}

	private List<String> getswaggerPartnersName(Set<String> partnerId) {
		try {
			Query query = new Query(Criteria.where("_id").in(partnerId));
			List<SwaggerPartner> dbPartners = mongoTemplate.find(query, SwaggerPartner.class);
			return dbPartners.stream().map(swaggerPartner -> swaggerPartner.getPartnerDisplayName())
					.collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return Collections.emptyList();
	}

	@Override
	public void updateSwaggerDictionary(SwaggerDictionary swaggerDictionary) {
		SwaggerDictionary swaggerDictForUpdate = baseRepository.findOne("swaggerId", swaggerDictionary.getSwaggerId(),
				"revision", swaggerDictionary.getRevision(), "oasVersion", swaggerDictionary.getOasVersion(),
				SwaggerDictionary.class);
		log.debug("updateSwaggerDictionary : {}",swaggerDictForUpdate);
		if (swaggerDictForUpdate != null) {
			swaggerDictForUpdate.setDictionary(swaggerDictionary.getDictionary());
			baseRepository.save(swaggerDictForUpdate);
		} else { // Save a new Swagger Dictionary Obj
			baseRepository.save(swaggerDictionary);
		}

	}

	@Override
	public SwaggerDictionary getSwaggerDictionary(String swaggerId, Integer revision) {
		log.debug("getSwaggerDictionary:{}",swaggerId);
		return baseRepository.findOne("swaggerId", swaggerId, "revision", revision, SwaggerDictionary.class);
	}

	@Override
	public DictionarySwagger getSwaggerAssociatedWithDictionary(String dictionaryId, String modelId,
			Integer revision) {
		List<Document> documents = null;
		if (modelId == null || "".equals(modelId)) {
			documents = baseRepository.getSwaggerAssociatedWithDictionary(dictionaryId,
					SwaggerDictionary.class);
		} else {
			documents = baseRepository.getSwaggerAssociatedWithSchemaName(dictionaryId, modelId, revision,
					SwaggerDictionary.class);
		}
		log.debug("getSwaggerAssociatedWithDictionary:{}", documents);
		DictionarySwagger dictionarySwagger = new DictionarySwagger();

		if (documents.size() > 0) {
			Document dictionaryObj = documents.get(0).get("dictionary", Document.class);
			dictionarySwagger.setId(dictionaryObj.get("_id", ObjectId.class).toString());
			dictionarySwagger.setName(dictionaryObj.getString("name"));
			dictionarySwagger.setRevision(dictionaryObj.getInteger("revision"));
			dictionarySwagger.setStatus(Status.valueOf(dictionaryObj.getString(STATUS_VALUE)));
		} else {
			return null;
		}

		for (Document doc : documents) {
			Document dictionaryObj = doc.get("dictionary", Document.class);
			Document modelsObj = dictionaryObj.get("models", Document.class);
			String modelName = modelsObj.getString("name");
			String modelID = modelsObj.getString("modelId");
			Integer modelRevision = modelsObj.getInteger("revision");
			if (dictionarySwagger.getSchemas() != null && dictionarySwagger.getSchemas().size() > 0) {
				Optional<SchemaInfo> schemaInfoOptional = dictionarySwagger.getSchemas().stream()
						.filter(s -> s.getName().equals(modelName)).findFirst();
				if (schemaInfoOptional.isPresent()) {
					SwaggerData swaggerData = getSwaggerData(doc);
					schemaInfoOptional.get().getSwaggers().add(swaggerData);
				} else {
					ArrayList<SwaggerData> swaggers = new ArrayList<>();
					SwaggerData swaggerData = getSwaggerData(doc);
					SchemaInfo schemaInfo = new SchemaInfo();
					schemaInfo.setName(modelName);
					schemaInfo.setModelId(modelID);
					schemaInfo.setRevision(modelRevision);
					schemaInfo.setStatus(SchemaInfo.Status.valueOf(modelsObj.getString(STATUS_VALUE)));
					swaggers.add(swaggerData);
					schemaInfo.setSwaggers(swaggers);
					dictionarySwagger.getSchemas().add(schemaInfo);

				}
			} else {
				ArrayList<SchemaInfo> schemaInfos = new ArrayList<>();
				SchemaInfo schemaInfo = new SchemaInfo();
				schemaInfo.setName(modelName);
				schemaInfo.setModelId(modelID);
				schemaInfo.setRevision(modelRevision);
				schemaInfo.setStatus(SchemaInfo.Status.valueOf(modelsObj.getString(STATUS_VALUE)));
				ArrayList<SwaggerData> swaggers = new ArrayList<>();
				SwaggerData swaggerData = getSwaggerData(doc);
				swaggers.add(swaggerData);
				schemaInfo.setSwaggers(swaggers);
				schemaInfos.add(schemaInfo);
				dictionarySwagger.setSchemas(schemaInfos);
			}

		}
		return dictionarySwagger;
	}

	@Override
	public List<String> loadSwaggersToScan(String interactionid, String jsessionid) {

		List<String> swaggersList = mongoTemplate.findDistinct("swaggerId", SwaggerVO.class, String.class);

		swaggersList.addAll(mongoTemplate.findDistinct("swaggerId", Swagger3VO.class, String.class));

		return swaggersList;
	}

	@Override
	public DeleteResult deleteSwagger2BasePath(SwaggerVO vo) {
		log.debug("delete Swagger2BasePath :{} ", vo.getName());
		Criteria criteriaWithSwaggerId = Criteria.where("swaggerId").is(vo.getSwaggerId());
		Criteria criteriaWithSwaggerName = Criteria.where("name").is(vo.getName());
		Query query = new Query(new Criteria().orOperator(criteriaWithSwaggerId, criteriaWithSwaggerName));
		return removeBasePath(query, Swagger2BasePath.class);
	}

	@Override
	public DeleteResult deleteSwagger3BasePath(Swagger3VO vo) {
		log.debug("delete Swagger2BasePath :{} ", vo.getName());
		Criteria criteriaWithSwaggerId = Criteria.where("swaggerId").is(vo.getSwaggerId());
		Criteria criteriaWithSwaggerName = Criteria.where("name").is(vo.getName());
		Query query = new Query(new Criteria().orOperator(criteriaWithSwaggerId, criteriaWithSwaggerName));
		return removeBasePath(query, Swagger3BasePath.class);
	}

	@Override
	public Long findSwaggersCount(String swaggerId) {
		Query query = new Query(Criteria.where("swaggerId").is(swaggerId));
		return mongoTemplate.count(query, SwaggerVO.class);
	}

	@Override
	public Long findSwaggers3VOCount(String swaggerId) {
		Query query = new Query(Criteria.where("swaggerId").is(swaggerId));
		return mongoTemplate.count(query, Swagger3VO.class);
	}

	@Override
	public SwaggerProduct createProduct(SwaggerProduct swaggerProduct) throws ItorixException {
		Query query = Query.query(Criteria.where("productName").is(swaggerProduct.getProductName()));
		if (mongoTemplate.count(query, SwaggerProduct.class) > 1) {
			log.error("Product already exists : {}", swaggerProduct.getProductName());
			throw new ItorixException(
					String.format(ErrorCodes.errorMessage.get("General-1001"), "Empty SwaggerProductId"),
					"General-1001");
		} else {
			return mongoTemplate.save(swaggerProduct);
		}
	}

	public SwaggerProduct updateProduct(SwaggerProduct swaggerProduct, String productId) throws ItorixException {
		if (StringUtils.isEmpty(productId)) {
			log.info("Empty Product id : {}", swaggerProduct.getId());
			throw new ItorixException(
					String.format(ErrorCodes.errorMessage.get("General-1001"), "Empty SwaggerProductId"),
					"General-1001");
		}
		Query query = Query.query(Criteria.where("_id").is(productId));
		Update update = new Update();
		update.set("productName", swaggerProduct.getProductName());
		update.set("productDescription", swaggerProduct.getProductDescription());
		log.debug("Query for Update for Product : {}", query);
		return mongoTemplate.findAndReplace(query, swaggerProduct, new FindAndReplaceOptions().returnNew());
	}


	public Boolean deleteProduct(String productId) {
		Query query = Query.query(Criteria.where("_id").is(productId));
		log.error("productId : {}", productId);
		return mongoTemplate.remove(query, SwaggerProduct.class).wasAcknowledged();
	}

	@Override
	public List<SwaggerProduct> getProductGroups(String interactionid, String jsessionid) {
		log.info("interactionid : {}", interactionid);
		return mongoTemplate.findAll(SwaggerProduct.class);
	}

	@Override
	public void manageSwaggerProducts(String swaggerId,
			String oas, AsociateSwaggerProductRequest swaggerProductRequest)
			throws ItorixException {
		log.info("Managing Swagger Products : {}, Swagger :{}, SwaggerRevision :{}, Oas: {}",
				swaggerProductRequest, swaggerId, oas);
		Query swaggerQuery = new Query(
				Criteria.where("swaggerId").is(swaggerId));
		Query metadataQuery = new Query(Criteria.where("oas").is(oas));
		if (StringUtils.equalsIgnoreCase("2.0", oas)) {
			SwaggerVO vo = mongoTemplate.findOne(swaggerQuery, SwaggerVO.class);
			metadataQuery.addCriteria(Criteria.where("swaggerName").is(vo.getName()));
			SwaggerMetadata swaggerMetadata = mongoTemplate.findOne(metadataQuery, SwaggerMetadata.class);
			if (swaggerMetadata!=null) {
				swaggerMetadata.setProducts(swaggerProductRequest.getProductId().stream().collect(Collectors.toSet()));
			} else {
				swaggerMetadata = new SwaggerMetadata();
				swaggerMetadata.setSwaggerName(vo.getName());
				swaggerMetadata.setSwaggerId(vo.getSwaggerId());
				swaggerMetadata.setOas("2.0");
				swaggerMetadata.setProducts(
						swaggerProductRequest.getProductId().stream().collect(Collectors.toSet()));
			}
			mongoTemplate.save(swaggerMetadata);
		} else if (StringUtils.equalsIgnoreCase("3.0", oas)) {
			Swagger3VO vo = mongoTemplate.findOne(swaggerQuery, Swagger3VO.class);
			metadataQuery.addCriteria(Criteria.where("swaggerName").is(vo.getName()));
			SwaggerMetadata swaggerMetadata = mongoTemplate.findOne(metadataQuery, SwaggerMetadata.class);
			if (swaggerMetadata!=null) {
				swaggerMetadata.setProducts(swaggerProductRequest.getProductId().stream().collect(Collectors.toSet()));
			} else {
				swaggerMetadata = new SwaggerMetadata();
				swaggerMetadata.setSwaggerName(vo.getName());
				swaggerMetadata.setSwaggerId(vo.getSwaggerId());
				swaggerMetadata.setOas("3.0");
				swaggerMetadata.setProducts(
						swaggerProductRequest.getProductId().stream().collect(Collectors.toSet()));
			}
			mongoTemplate.save(swaggerMetadata);
		} else {
			log.error("Invalid oas : {}", oas);
			throw new ItorixException(
					String.format(ErrorCodes.errorMessage.get("General-1001"), "Empty SwaggerProductId"),
					"General-1001");
		}
	}

	@Override
	public List<SwaggerProduct> getSwaggerProducts(String swaggerId, String oas, String interactionid,
			String jsessionid, int offset, int pageSize) throws ItorixException {
		log.info("Get Swagger Products : Swagger :{}, SwaggerRevision :{}, Oas: {}",
				 swaggerId, oas);

		Query swaggerQuery = new Query(Criteria.where("swaggerId").is(swaggerId));

		Query metadataQuery = new Query(Criteria.where("oas").is(oas));

		List<SwaggerMetadata> swaggerMetadata = Collections.emptyList();
		if(StringUtils.equals("2.0",oas)){
			SwaggerVO vo = mongoTemplate.findOne(swaggerQuery, SwaggerVO.class);
			metadataQuery.addCriteria(Criteria.where("swaggerName").is(vo.getName()));
			swaggerMetadata = mongoTemplate.find(metadataQuery, SwaggerMetadata.class);
		}
		if(StringUtils.equals("3.0",oas)){
			Swagger3VO vo = mongoTemplate.findOne(swaggerQuery, Swagger3VO.class);
			metadataQuery.addCriteria(Criteria.where("swaggerName").is(vo.getName()));
			swaggerMetadata = mongoTemplate.find(metadataQuery, SwaggerMetadata.class);
		}else if (!oas.equals("3.0") && !oas.equals("2.0")){
			log.error("Invalid oas : {}", oas);
			throw new ItorixException(
					String.format(ErrorCodes.errorMessage.get("General-1001"), "Invalid oas"),
					"General-1001");
		}

		List<String> products = new ArrayList<String>();
		for(SwaggerMetadata metadata : swaggerMetadata){
			if(metadata.getProducts() != null)
			products.addAll(metadata.getProducts().stream().collect(Collectors.toList()));
		}
		Query productQuery = new Query(Criteria.where("_id").in(products));
		log.error("productIds : {}", products.size());
		List<SwaggerProduct> swaggerProducts = mongoTemplate.find(productQuery, SwaggerProduct.class);

		return swaggerProducts;
	}

	/**
	 * @param partnerIds
	 * @return
	 */
	@Override
	public List<SwaggerProduct> getProductGroupsByPartnerIds(List<String> partnerIds) {
		Query query = Query.query(Criteria.where("productId").in(partnerIds));
		log.debug("query : {}", query);

		List<Set<String>> result = mongoTemplate.find(query, SwaggerMetadata.class).stream()
				.map(swaggerMetadata -> {
					return swaggerMetadata.getProducts();
				}).collect(Collectors.toList());
		Set<String> productId = new HashSet<>();
		for (Set<String> product : result) {
			for (String partnerId : product) {
				productId.add(partnerId);
			}
		}

		Query productQuery = new Query(
				Criteria.where("_id").in(productId.stream().collect(Collectors.toList())));
		log.debug("productQuery : {}", productQuery);
		return mongoTemplate.find(productQuery, SwaggerProduct.class);
	}

	@Override
	public void updatePartners(List<SwaggerPartner> swaggerPartners) {
		swaggerPartners.stream().forEach(partner -> {
			updatePartner(partner);
		});
	}

	@Override
	public String getGolbalRule(String oas) {
		Query query = Query.query(Criteria.where("ruleSetType").is("swagger").and("oasVersion").is(oas).and("isGlobalRuleSet").is(true));
		Document ruleset = mongoTemplate.findOne(query, Document.class,"Linter.RuleSet");
		return ruleset!=null?ruleset.get("_id").toString():null;
	}

	public void checkSwaggerTeams(String jsessionid, String swaggerName, String oasVersion) throws ItorixException {
		log.debug("Checking for swagger teams ...");
		if (swaggerName == null) {
			log.error("Swagger name cannot be empty");
			throw new ItorixException(ErrorCodes.errorMessage.get("Swagger-1000"), "Swagger-1000");
		}
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User user = getUserDetailsFromSessionID(jsessionid);
		if (user.isWorkspaceAdmin(userSessionToken.getWorkspaceId())){
			return;
		}
		List<SwaggerTeam> userTeams = getUserTeams(user);
		boolean teamFound = false;
		Set<String> swaggerTeams = new HashSet<>();
		Query query = new Query(Criteria.where("swaggerName").is(swaggerName).and("oas").is(oasVersion));
		SwaggerMetadata metadata = mongoTemplate.findOne(query, SwaggerMetadata.class);
		if (metadata != null) {
			swaggerTeams = metadata.getTeams();
		}
		if (!swaggerTeams.isEmpty()) {
			if (!userTeams.isEmpty()) {
				for (SwaggerTeam team : userTeams) {
					if (swaggerTeams.contains(team.getName())) {
						teamFound = true;
						break;
					}
				}
			}
		} else {
			teamFound = true;
		}
		if (!teamFound) {
			log.error("Swagger with the same name is already present in the workspace with different team.");
			throw new ItorixException(ErrorCodes.errorMessage.get("Swagger-1011"), "Swagger-1011");
		}
	}

	@Override
	public List<MetadataErrorDTO> checkMetadataSwagger(String oas, String swaggerString) throws ItorixException {
		if (StringUtils.isEmpty(swaggerString)){
			log.error("Swagger string is empty.");
			throw new ItorixException("Swagger string is empty", "General-1001");
		}
		String missingError = "%s is missing in %s";
		Set<String> metadataList = new HashSet<>(List.of("x-metadata"));
		List<MetadataErrorDTO> response = new ArrayList<>();
		Map<String, Object> metadata = new HashMap<>();
		Set<String> swaggerPaths = new HashSet<>();
		Set<String> swaggerDefinition = new HashSet<>();
		String definitionsEnum = null;
		if(oas.startsWith("2")) {
			definitionsEnum = "Definitions";
			SwaggerParser swaggerParser = new SwaggerParser();
			Swagger swagger = swaggerParser.parse(swaggerString);
			try {
				swaggerPaths = swagger.getPaths().keySet();
			}catch (Exception exception) {
				log.error("Paths is empty.");
			}
			try {
				swaggerDefinition = swagger.getDefinitions().keySet();
			}catch (Exception exception) {
				log.error("Definition is empty.");
			}
			if (!swagger.getVendorExtensions().isEmpty()) {
				swagger.getVendorExtensions().forEach((key, value) -> {
					if (metadataList.contains(key)) {
						if (key.equalsIgnoreCase("x-metadata")) {
							try {
								metadata.put("metadata", new ObjectMapper().convertValue(value, Map.class).get("metadata"));
							} catch (Exception e) {
								log.error("metadata is empty");
							}
						} else {
							metadata.put(key, value);
						}
					}
				});
			}
		} else if (oas.startsWith("3")) {
			definitionsEnum = "Components-schema";
			OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
			OpenAPI swagger = openAPIV3Parser.readContents(swaggerString).getOpenAPI();
			try {
				swaggerPaths = swagger.getPaths().keySet();
			} catch (Exception e) {
				log.error("Paths is empty");
			}
			try {
				swaggerDefinition = swagger.getComponents().getSchemas().keySet();
			} catch (Exception e) {
				log.error("Components is empty");
			}
			try {
				swagger.getExtensions().forEach((key, value) -> {
					if (metadataList.contains(key)) {
						if (key.equalsIgnoreCase("x-metadata")) {
							try {
								metadata.put("metadata", new ObjectMapper().convertValue(value, Map.class).get("metadata"));
							} catch (Exception e) {
								log.error(e.getMessage());
							}
						} else {
							metadata.put(key, value);
						}
					}
				});
			}catch (Exception e){
				log.error("Error while parsing swagger.");
			}
		}
		Set<String> metadataPaths = new HashSet<>();
		Set<String> metadataDefinitions = new HashSet<>();
		if (!metadata.isEmpty()) {
			ObjectMapper m = new ObjectMapper();
			List<Map<String, Object>> categoryMetadataList = null;
			try {
				categoryMetadataList = m.convertValue(m.convertValue(metadata.get("metadata"), Map.class).get("category"), List.class);
			} catch (Exception e) {
				log.error("Error converting metadata/category");
			}
			if (!categoryMetadataList.isEmpty()) {
				for (Map<String, Object> categoryMetadata : categoryMetadataList) {
					Set<String> metadataObject = m.convertValue(categoryMetadata.get("paths"), Set.class);
					if (metadataObject != null) {
						metadataPaths.addAll(metadataObject);
					}
					metadataObject = m.convertValue(categoryMetadata.get("definitions"), Set.class);
					if (metadataObject != null) {
						metadataDefinitions.addAll(metadataObject);
					}
				}
			}
		}
		response = checkDifference(swaggerPaths, metadataPaths, response, missingError, "Paths", "x-metadata-paths");
		response = checkDifference(metadataPaths, swaggerPaths, response, missingError, "x-metadata-paths", "Paths");
		response = checkDifference(swaggerDefinition, metadataDefinitions, response, missingError, definitionsEnum, "x-metadata-Definitions");
		response = checkDifference(metadataDefinitions, swaggerDefinition, response, missingError, "x-metadata-Definitions", definitionsEnum);
		return response;
	}

	private List<MetadataErrorDTO> checkDifference(Collection source, Collection target,List<MetadataErrorDTO> response, String error, String errorSource, String errorTarget){
		List<String> difference = new ArrayList<>(CollectionUtils.subtract(source, target));
		if (!difference.isEmpty()){
			for (String object : difference) {
				response.add(new MetadataErrorDTO(errorSource, null, object, String.format(error, object, errorTarget)));
			}
		}
		return response;
	}
	private DeleteResult removeBasePath(Query query, Class clazz) {
		log.debug("removeBasePath : {}", query);
		return mongoTemplate.remove(query, clazz);
	}
	private SwaggerData getSwaggerData(Document doc) {
		SwaggerData swaggerData = new SwaggerData();
		swaggerData.setId(doc.getString("swaggerId"));
		swaggerData.setName(doc.getString("name"));
		swaggerData.setOasVersion(doc.getString("oasVersion"));
		swaggerData.setRevision(doc.getInteger("revision"));
		swaggerData.setStatus(doc.getString(STATUS_VALUE));
		return swaggerData;
	}


	public SwaggerObjectResponse getSwaggerStatsV2(String timeunit, String timerange,String jsessionid)
			throws ParseException {
		log("getSwaggerStatsV2", timeunit, timerange);
		// TODO: Verify and remove blocks of code
		// ObjectMapper mapper = new ObjectMapper();
		SwaggerObjectResponse swaggerObjectResponse = new SwaggerObjectResponse();
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User user = getUserDetailsFromSessionID(jsessionid);
		Map<String, Object> filterFieldsAndValues = new HashMap<>();
		boolean isAdmin = user.isWorkspaceAdmin(userSessionToken.getWorkspaceId());
		List<Stat> statsList = new ArrayList<>();
		boolean populateSwaggers = false;
		if (timeunit != null && timerange != null) {
			populateSwaggers = true;
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			String[] dates = timerange.split("~");
			Date startDate = null;
			Date endDate = null;
			if (dates != null && dates.length > 0) {
				startDate = dateFormat.parse(dates[0]);
				endDate = dateFormat.parse(dates[1]);
			}
			Metrics metricsNode = new Metrics();
			metricsNode.setType(timeunit);
			HashMap<String, Object> valuesNode = new HashMap<>();
			while (startDate.compareTo(endDate) <= 0) {
				Query query = new Query();
				query.addCriteria(Criteria.where(LABEL_CREATED_TIME)
						.gte(new Long(getStartOfDay(startDate).getTime() + ""))
						.lt(new Long(getEndOfDay(startDate).getTime() + "")));
				List<SwaggerVO> list = baseRepository.find(query, SwaggerVO.class);
				if (list != null && list.size() > 0) {
					valuesNode.put("timestamp", getStartOfDay(startDate).getTime() + "");
					valuesNode.put("value", list.size());

				}
				startDate = DateUtil.addDays(startDate, 1);
			}
			metricsNode.setValues(valuesNode);
			swaggerObjectResponse.setMetrics(metricsNode);
		}
		if(!isAdmin){
			Map<String, Set<String>> swaggerRoles = getSwaggerPermissions("2.0", user);
			Set<String> SwaggerNames = new HashSet<>();
			SwaggerNames.addAll(swaggerRoles.keySet());
			filterFieldsAndValues.put("createdBy", user.getId());
			List<String> trimList = baseRepository.filterAndGroupBySwaggerName(filterFieldsAndValues,
					SwaggerVO.class, null);
			SwaggerNames.addAll(trimList);
			List<String> swaggerIds=new ArrayList<>();
			for (String name : SwaggerNames) {
				List<Revision> versions;
				versions = getListOfRevisions(name, jsessionid);
				SwaggerVO vo = null;
				if (versions != null && versions.size() > 0) {
					Revision revision = Collections.max(versions);
					vo = baseRepository.findOne("name", name, "revision", revision.getRevision(), SwaggerVO.class);
					swaggerIds.add(vo.getSwaggerId());
				}
			}
			filterFieldsAndValues.put("swaggerId",swaggerIds);
			filterFieldsAndValues.remove("createdBy");
			AggregationResults<Document> documents = baseRepository.filterAndGroupBySwaggerNameV2(
					SwaggerVO.class, filterFieldsAndValues);
			ArrayList<Document> documentlist = (ArrayList) documents.getRawResults().get("results");
			documentlist.forEach(document -> {
				Stat stats = new Stat();
				stats.setName(document.get("status").toString());
				stats.setCount(document.get("count").toString());
				statsList.add(stats);
			});
		}
		else {
			AggregationResults<Document> documents = baseRepository.filterAndGroupBySwaggerNameV2(
					SwaggerVO.class, filterFieldsAndValues);
			ArrayList<Document> documentlist = (ArrayList) documents.getRawResults().get("results");
			documentlist.forEach(document -> {
				Stat stats = new Stat();
				stats.setName(document.get("status").toString());
				stats.setCount(document.get("count").toString());
				statsList.add(stats);
			});
		}
		swaggerObjectResponse.setStats(statsList);
		log.debug("swaggerObjectResponseV2 : {}", swaggerObjectResponse);
		return swaggerObjectResponse;
	}

	public SwaggerObjectResponse getSwagger3Statsv2(String timeunit, String timerange,String jsessionid) throws ParseException, ItorixException {
		log("getSwagger3StatsV2", timeunit, timerange);
		SwaggerObjectResponse swaggerObjectResponse = new SwaggerObjectResponse();
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User user = getUserDetailsFromSessionID(jsessionid);
		Map<String, Object> filterFieldsAndValues = new HashMap<>();
		boolean isAdmin = user.isWorkspaceAdmin(userSessionToken.getWorkspaceId());
		List<Stat> statsList = new ArrayList<>();
		boolean populateSwaggers = false;
		if (timeunit != null && timerange != null) {
			populateSwaggers = true;
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			String[] dates = timerange.split("~");

			Date startDate = null;
			Date endDate = null;
			if (dates != null && dates.length > 0) {
				startDate = dateFormat.parse(dates[0]);
				endDate = dateFormat.parse(dates[1]);
			}
			Metrics metricsNode = new Metrics();
			metricsNode.setType(timeunit);
			HashMap<String, Object> valuesNode = new HashMap<>();
			while (startDate.compareTo(endDate) <= 0) {
				Query query = new Query();
				query.addCriteria(Criteria.where(LABEL_CREATED_TIME)
						.gte(new Long(getStartOfDay(startDate).getTime() + ""))
						.lt(new Long(getEndOfDay(startDate).getTime() + "")));
				List<Swagger3VO> list = baseRepository.find(query, Swagger3VO.class);
				if (list != null && list.size() > 0) {
					valuesNode.put("timestamp", getStartOfDay(startDate).getTime() + "");
					valuesNode.put("value", list.size());
				}
				startDate = DateUtil.addDays(startDate, 1);
			}
			metricsNode.setValues(valuesNode);
			swaggerObjectResponse.setMetrics(metricsNode);
		}
		if(!isAdmin){
			Map<String, Set<String>> swaggerRoles = getSwaggerPermissions("3.0", user);
			Set<String> SwaggerNames = new HashSet<>();
			SwaggerNames.addAll(swaggerRoles.keySet());
			filterFieldsAndValues.put("createdBy", user.getId());
			List<String> trimList = baseRepository.filterAndGroupBySwaggerName(filterFieldsAndValues,
					Swagger3VO.class, null);
			SwaggerNames.addAll(trimList);
			List<String> swaggerIds=new ArrayList<>();
			for (String name : SwaggerNames) {
				List<Revision> versions;
				versions = getListOf3Revisions(name, jsessionid);
				Swagger3VO vo = null;
				if (versions != null && versions.size() > 0) {
					Revision revision = Collections.max(versions);
					vo = baseRepository.findOne("name", name, "revision", revision.getRevision(), Swagger3VO.class);
					swaggerIds.add(vo.getSwaggerId());
				}
			}
			filterFieldsAndValues.put("swaggerId",swaggerIds);
			filterFieldsAndValues.remove("createdBy");
			AggregationResults<Document> documents = baseRepository.filterAndGroupBySwaggerNameV2(
					Swagger3VO.class, filterFieldsAndValues);
			ArrayList<Document> documentlist = (ArrayList) documents.getRawResults().get("results");
			documentlist.forEach(document -> {
				Stat stats = new Stat();
				stats.setName(document.get("status").toString());
				stats.setCount(document.get("count").toString());
				statsList.add(stats);
			});
		}
		else {
			AggregationResults<Document> documents = baseRepository.filterAndGroupBySwaggerNameV2(
					Swagger3VO.class, filterFieldsAndValues);
			ArrayList<Document> documentlist = (ArrayList) documents.getRawResults().get("results");
			documentlist.forEach(document -> {
				Stat stats = new Stat();
				stats.setName(document.get("status").toString());
				stats.setCount(document.get("count").toString());
				statsList.add(stats);
			});
		}
		swaggerObjectResponse.setStats(statsList);
		log.debug("swaggerObjectResponseV2 : {}", swaggerObjectResponse);
		return swaggerObjectResponse;
	}


	public MatchOperation findFilter(String timerange,String ruleSetId,String status)
			throws ParseException{
		MatchOperation matchOperation ;
		if(ruleSetId==null && timerange==null){
			matchOperation = getCriteria(null,null,status);
		}
		else if(ruleSetId!=null && timerange==null){
			matchOperation = Aggregation.match(Criteria.where("ruleSetId").is(ruleSetId));
		}
		else if(ruleSetId == null){
			matchOperation = getCriteria(timerange,null,status);
		}
		else{
			matchOperation = getCriteria(timerange,ruleSetId,status);
		}
		return matchOperation;
	}

	public MatchOperation getCriteria(String timerange ,String ruleSetId,String status) throws ParseException{
		List<Criteria> criteriaList = new ArrayList<>();
		if(ruleSetId !=null)
			criteriaList.add(Criteria.where("ruleSetId").is(ruleSetId));
		if(status!=null)
			criteriaList.add(Criteria.where("status").is(status));

		Criteria criteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()]));
		return !criteriaList.isEmpty() ? match(criteria) : Aggregation.match(new Criteria());
	}

	public SwaggerHistoryResponse getListOfSwagger3DetailsV2(String status, String modifiedDate, String interactionid,
			String jsessionid, int offset, String oas, String swagger, int pageSize, String sortByModifiedDate)
			throws ItorixException, JsonProcessingException, IOException {
		log("getListOfSwagger3DetailsV2", interactionid, jsessionid);
		// getSwaggers();
		Map<String, Object> filterFieldsAndValues = new HashMap<>();
		filterFieldsAndValues.put(STATUS_VALUE, status);
		filterFieldsAndValues.put("modified_date", modifiedDate);
		Map<String, Object> filterFieldsAndValuesForSwaggerId = new HashMap<>();
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User user = getUserDetailsFromSessionID(jsessionid);
		boolean isAdmin = user.isWorkspaceAdmin(userSessionToken.getWorkspaceId());
		List<String> roles = user.getUserWorkspace(userSessionToken.getWorkspaceId()).getRoles();

		List<Swagger3VO> list = new ArrayList<Swagger3VO>();
		SwaggerHistoryResponse response = new SwaggerHistoryResponse();
		List<String> names = new ArrayList<String>();
		int total = 1;
		if (swagger == null) {
			names = baseRepository.filterAndGroupBySwaggerNameHistory(filterFieldsAndValues,filterFieldsAndValuesForSwaggerId, Swagger3VO.class,
					sortByModifiedDate);
			total = names.size();
			names = trimList(names, offset, pageSize);
		} else {
			try {
				Swagger3VO swaggervo = getSwagger3(swagger, interactionid);
				if (swaggervo != null) {
					swagger = swaggervo.getName();
					names.add(swaggervo.getName());
				} else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Swagger-1000"), "Swagger-1000");
				}
			} catch (Exception e) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Swagger-1000"), "Swagger-1000");
			}
		}
		if (isAdmin) {
			for (String name : names) {
				List<Revision> versions;
				if (status != null && (!status.equals(""))) {
					versions = getListOf3Revisions(name, status, interactionid);
				} else {
					versions = getListOf3Revisions(name, interactionid);
				}
				Swagger3VO vo = null;
				if (versions != null && versions.size() > 0) {
					Revision revision = Collections.max(versions);
					vo = baseRepository.findOne("name", name, "revision", revision.getRevision(), Swagger3VO.class);
					if(vo == null) {
						vo = baseRepository.findOne("swaggerId", name, "revision", revision.getRevision(),
								Swagger3VO.class);
					}
					SwaggerMetadata swaggerMetadata = getSwaggerMetadata(vo.getName(), oas);
					if (swaggerMetadata != null) {
						vo.setTeams(swaggerMetadata.getTeams());
					}
				}
				if (vo != null) {
					ObjectMapper mapper = new ObjectMapper();
					JsonNode swaggerJson = mapper.readTree(vo.getSwagger());
					if (swaggerJson != null) {
						JsonNode infoNode = swaggerJson.get("info");
						if (infoNode != null) {
							try {
								vo.setDescription(infoNode.get("description").asText());
							} catch (Exception e) {
								vo.setDescription("N/A");
							}
						}
					}
					vo.setSwagger(null);
					roles = Arrays.asList("Admin", "Write", "Read");
					vo.setRoles(roles);
					list.add(vo);
				}
			}
			Pagination pagination = new Pagination();
			pagination.setOffset(offset);
			pagination.setTotal((long) total);
			pagination.setPageSize(pageSize);
			response.setPagination(pagination);
			response.setData(list);
		} else {
			Map<String, Set<String>> swaggerRoles = getSwaggerPermissions("3.0", user);
			Set<String> SwaggerNames = new HashSet<>();
			SwaggerNames.addAll(swaggerRoles.keySet());
			filterFieldsAndValues.put("createdBy", user.getId());
			List<String> trimList = baseRepository.filterAndGroupBySwaggerName(filterFieldsAndValues,
					Swagger3VO.class, sortByModifiedDate);
			SwaggerNames.addAll(trimList);
			if (swagger != null) {
				if (SwaggerNames.contains(swagger)) {
					SwaggerNames = new HashSet<>();
					SwaggerNames.add(swagger);
				} else {
					SwaggerNames = new HashSet<>();
				}
			}
			if (SwaggerNames != null) {
				names = new ArrayList<>(SwaggerNames);
				List<String> swaggerIds = new ArrayList<>();
				for (String name : names) {
					List<Revision> versions;
					if (status != null && (!status.equals(""))) {
						versions = getListOf3Revisions(name, status, interactionid);
					} else {
						versions = getListOf3Revisions(name, interactionid);
					}
					Swagger3VO vo = null;
					if (versions != null && versions.size() > 0) {
						Revision revision = Collections.max(versions);
						vo = baseRepository.findOne("name", name, "revision", revision.getRevision(),
								Swagger3VO.class);
						swaggerIds.add(vo.getSwaggerId());
					}
				}
				filterFieldsAndValuesForSwaggerId.put("swaggerId",swaggerIds);
				filterFieldsAndValues.remove("createdBy");
				names = baseRepository.filterAndGroupBySwaggerNameHistory(filterFieldsAndValues,filterFieldsAndValuesForSwaggerId, Swagger3VO.class,
						sortByModifiedDate);
				Long size = Long.valueOf(names.size());
				names=trimList(names,offset,pageSize);
				for(String name : names) {
					List<Revision> versions;
					if (status != null && (!status.equals(""))) {
						versions = getListOf3Revisions(name, status, interactionid);
					} else {
						versions = getListOf3Revisions(name, interactionid);
					}
					Swagger3VO vo = null;
					if (versions != null && versions.size() > 0) {
						Revision revision = Collections.max(versions);
						vo = baseRepository.findOne("name", name, "revision", revision.getRevision(),
								Swagger3VO.class);
						if (vo == null) {
							vo = baseRepository.findOne("swaggerId", name, "revision", revision.getRevision(),
									Swagger3VO.class);
						}
					}
					if (vo != null) {
						SwaggerMetadata swaggerMetadata = getSwaggerMetadata(vo.getName(), oas);
						if (swaggerMetadata != null) {
							vo.setTeams(swaggerMetadata.getTeams());
						}
						// if (vo.getTeams() == null || isMailidExist(vo,
						// user.getEmail())) {
						ObjectMapper mapper = new ObjectMapper();
						JsonNode swaggerJson = mapper.readTree(vo.getSwagger());
						if (swaggerJson != null) {
							JsonNode infoNode = swaggerJson.get("info");
							if (infoNode != null) {
								vo.setDescription(infoNode.get("description") == null
										? "N/A"
										: infoNode.get("description").asText());
							}
						}
						vo.setSwagger(null);
						vo.setRoles(new ArrayList<>(swaggerRoles.get(vo.getName()) == null
								? Arrays.asList("Admin", "Write", "Read")
								: swaggerRoles.get(vo.getName()))); // TODO
						// not
						// null
						list.add(vo);
					}
				}
				Pagination pagination = new Pagination();
//				int totalByUser = SwaggerNames.size();
				pagination.setOffset(offset);
				pagination.setTotal(size);
				pagination.setPageSize(pageSize);
				response.setPagination(pagination);
				response.setData(list);
			}
		}
		log("getListOfSwagger3Details", interactionid, list);
		return response;
	}



	public SwaggerHistoryResponse getListOfSwaggerDetailsV2(String status, String modifiedDate, String interactionid,
			String jsessionid, int offset, String oas, String swagger, int pageSize, String sortByModifiedDate)
			throws ItorixException, IOException {
		log("getListOfSwaggerDetailsV2", interactionid, jsessionid);
		Map<String, Object> filterFieldsAndValues = new HashMap<>();
		filterFieldsAndValues.put(STATUS_VALUE, status);
		filterFieldsAndValues.put("modified_date", modifiedDate);
		Map<String, Object> filterFieldsAndValuesForSwaggerId = new HashMap<>();
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User user = getUserDetailsFromSessionID(jsessionid);
		boolean isAdmin = user.isWorkspaceAdmin(userSessionToken.getWorkspaceId());
		List<String> roles = user.getUserWorkspace(userSessionToken.getWorkspaceId()).getRoles();

		List<SwaggerVO> list = new ArrayList<SwaggerVO>();
		SwaggerHistoryResponse response = new SwaggerHistoryResponse();
		List<String> names = new ArrayList<String>();
		int total = 1;
		if (swagger == null) {
			names = baseRepository.filterAndGroupBySwaggerNameHistory(filterFieldsAndValues,filterFieldsAndValuesForSwaggerId, SwaggerVO.class,
					sortByModifiedDate);
			total = names.size();
			names = trimList(names, offset, pageSize);
		} else {
			try {
				SwaggerVO swaggervo = getSwagger(swagger, interactionid);
				if (swaggervo != null) {
					swagger = swaggervo.getName();
					names.add(swaggervo.getName());
				} else {
					throw new ItorixException(ErrorCodes.errorMessage.get("Swagger-1000"), "Swagger-1000");
				}
			} catch (Exception e) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Swagger-1000"), "Swagger-1000");
			}
		}
		if (isAdmin) {
			for (String name : names) {
				List<Revision> versions;
				if (status != null && (!status.equals(""))) {
					versions = getListOfRevisions(name, status, interactionid);
				} else {
					versions = getListOfRevisions(name, interactionid);
				}
				SwaggerVO vo = null;
				if (versions != null && versions.size() > 0) {
					Revision revision = Collections.max(versions);
					vo = baseRepository.findOne("name", name, "revision", revision.getRevision(), SwaggerVO.class);
					if(vo == null) {
						vo = baseRepository.findOne("swaggerId", name, "revision", revision.getRevision(),
								SwaggerVO.class);
					}
					SwaggerMetadata swaggerMetadata = getSwaggerMetadata(vo.getName(), oas);
					if (swaggerMetadata != null) {
						vo.setTeams(swaggerMetadata.getTeams());
					}
				}
				if (vo != null) {
					ObjectMapper mapper = new ObjectMapper();
					JsonNode swaggerJson = mapper.readTree(vo.getSwagger());
					if (swaggerJson != null) {
						JsonNode infoNode = swaggerJson.get("info");
						if (infoNode != null) {
							try {
								vo.setDescription(infoNode.get("description").asText());
							} catch (Exception e) {
								vo.setDescription("N/A");
							}
						}
					}
					vo.setSwagger(null);
					roles = Arrays.asList("Admin", "Write", "Read");
					vo.setRoles(roles);
					list.add(vo);
				}
			}
			Pagination pagination = new Pagination();
			pagination.setOffset(offset);
			pagination.setTotal((long) total);
			pagination.setPageSize(pageSize);
			response.setPagination(pagination);
			response.setData(list);
		} else {
			Map<String, Set<String>> swaggerRoles = getSwaggerPermissions("2.0", user);
			Set<String> SwaggerNames = new HashSet<>();
			SwaggerNames.addAll(swaggerRoles.keySet());
			filterFieldsAndValues.put("createdBy", user.getId());
			List<String> trimList = baseRepository.filterAndGroupBySwaggerName(filterFieldsAndValues,
					SwaggerVO.class, sortByModifiedDate);
			SwaggerNames.addAll(trimList);
			if (swagger != null) {
				if (SwaggerNames.contains(swagger)) {
					SwaggerNames = new HashSet<>();
					SwaggerNames.add(swagger);
				} else {
					SwaggerNames = new HashSet<>();
				}
			}
			if (SwaggerNames != null) {
				names = new ArrayList<>(SwaggerNames);
				List<String> swaggerIds = new ArrayList<>();
				for (String name : names) {
					List<Revision> versions;
					if (status != null && (!status.equals(""))) {
						versions = getListOfRevisions(name, status, interactionid);
					} else {
						versions = getListOfRevisions(name, interactionid);
					}
					SwaggerVO vo = null;
					if (versions != null && versions.size() > 0) {
						Revision revision = Collections.max(versions);
						vo = baseRepository.findOne("name", name, "revision", revision.getRevision(),
								SwaggerVO.class);
						swaggerIds.add(vo.getSwaggerId());
					}
				}
				filterFieldsAndValuesForSwaggerId.put("swaggerId",swaggerIds);
				filterFieldsAndValues.remove("createdBy");
				names = baseRepository.filterAndGroupBySwaggerNameHistory(filterFieldsAndValues,filterFieldsAndValuesForSwaggerId, SwaggerVO.class,
						sortByModifiedDate);
				Long size = Long.valueOf(names.size());
				names=trimList(names,offset,pageSize);
				for(String name : names) {
					List<Revision> versions;
					if (status != null && (!status.equals(""))) {
						versions = getListOfRevisions(name, status, interactionid);
					} else {
						versions = getListOfRevisions(name, interactionid);
					}
					SwaggerVO vo = null;
					if (versions != null && versions.size() > 0) {
						Revision revision = Collections.max(versions);
						vo = baseRepository.findOne("name", name, "revision", revision.getRevision(),
								SwaggerVO.class);
						if (vo == null) {
							vo = baseRepository.findOne("swaggerId", name, "revision", revision.getRevision(),
									SwaggerVO.class);
						}
					}
					if (vo != null) {
						SwaggerMetadata swaggerMetadata = getSwaggerMetadata(vo.getName(), oas);
						if (swaggerMetadata != null) {
							vo.setTeams(swaggerMetadata.getTeams());
						}
						// if (vo.getTeams() == null || isMailidExist(vo,
						// user.getEmail())) {
						ObjectMapper mapper = new ObjectMapper();
						JsonNode swaggerJson = mapper.readTree(vo.getSwagger());
						if (swaggerJson != null) {
							JsonNode infoNode = swaggerJson.get("info");
							if (infoNode != null) {
								vo.setDescription(infoNode.get("description") == null
										? "N/A"
										: infoNode.get("description").asText());
							}
						}
						vo.setSwagger(null);
						vo.setRoles(new ArrayList<>(swaggerRoles.get(vo.getName()) == null
								? Arrays.asList("Admin", "Write", "Read")
								: swaggerRoles.get(vo.getName()))); // TODO
						// not
						// null
						list.add(vo);
					}
				}
				Pagination pagination = new Pagination();
				pagination.setOffset(offset);
				pagination.setTotal(size);
				pagination.setPageSize(pageSize);
				response.setPagination(pagination);
				response.setData(list);
			}
		}
		log("getListOfSwaggerDetailsV2", interactionid, list);
		return response;
	}
}
