package com.itorix.apiwiz.design.studio.dao;

import static com.itorix.apiwiz.identitymanagement.model.Constants.STATUS;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;
import static org.springframework.data.mongodb.core.aggregation.ArrayOperators.Filter.filter;
import static org.springframework.data.mongodb.core.aggregation.ComparisonOperators.Eq.valueOf;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.common.util.scm.ScmUtilImpl;
import com.itorix.apiwiz.common.util.zip.ZIPUtil;
import com.itorix.apiwiz.design.studio.businessimpl.SyncBusiness;
import com.itorix.apiwiz.design.studio.model.*;
import com.itorix.apiwiz.design.studio.model.swagger.sync.StatusHistory;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bson.Document;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Component
@Slf4j
public class AsyncApiDao {
	public static final String ASYNC = "async-api";
	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	private ScmUtilImpl scmImpl;

	@Autowired
	ApplicationProperties applicationProperties;

	@Qualifier("masterMongoTemplate")
	@Autowired
	MongoTemplate masterMongoTemplate;

	@Autowired
	BaseRepository baseRepository;

	@Autowired
  IdentityManagementDao identityManagementDao;

	@Autowired
	RestTemplate restTemplate;
	@Autowired
	SyncBusiness syncBusiness;

	@Value("${linting.api.url:}")
	private String lintingUrl;

	@Value("${linting.api.lintAsync:null}")
	private String lintAsync;

	public void createAsyncApiOrPushToDesignStudio(AsyncApi asyncApiObj,String jsessionId)
			throws ItorixException {
		List<AsyncApi> asyncApiList = findAsyncApiWithName(asyncApiObj.getName());
		if (!asyncApiList.isEmpty()) {
			createAsyncApiRevision(jsessionId, asyncApiList.get(0).getAsyncApiId(), asyncApiObj.getAsyncApi(),null);
		} else {
			asyncApiObj.setRevision(1);
			asyncApiObj.setAsyncApiId(UUID.randomUUID().toString().replaceAll("-", ""));
			UserSession user = getUser(jsessionId);
			asyncApiObj.setLock(true);
			asyncApiObj.setStatus(Status.Draft.getStatus());
			asyncApiObj.setHistory(List.of(new StatusHistory(Status.Draft.getStatus(), String.format("New Async Api - %s is created", asyncApiObj.getName()))));
			asyncApiObj.setCts(System.currentTimeMillis());
			asyncApiObj.setMts(System.currentTimeMillis());
			asyncApiObj.setLockedBy(user.getUsername());
			asyncApiObj.setCreatedBy(user.getUserId());
			asyncApiObj.setCreatedUserName(user.getUsername());
			mongoTemplate.save(asyncApiObj);
			if (asyncApiObj.isEnableScm()) {
				try {
					syncBusiness.sync2Repo(asyncApiObj, null, ASYNC);
				} catch (Exception exception) {
					log.error("Error while syncing Async API:{} revision:{} to repo.", asyncApiObj.getId(), asyncApiObj.getRevision(), exception);
				}
			}
		}
	}

	private UserSession getUser(String jsessionId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(jsessionId));
		return masterMongoTemplate.findOne(query,UserSession.class);
	}

	public AsyncApi getExistingAsync(String name) {
		Query query = new Query();
		query.addCriteria(Criteria.where("name").is(name));
		return mongoTemplate.findOne(query,AsyncApi.class);
	}

	public AsyncApi getExistingAsyncById(String id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("asyncApiId").is(id));
		return mongoTemplate.findOne(query,AsyncApi.class);
	}

	public AsyncApi getExistingAsyncByIdAndRevision(String id,int revision) {
		Query query = new Query();
		query.addCriteria(Criteria.where("asyncApiId").is(id));
		query.addCriteria(Criteria.where("revision").is(revision));
		return mongoTemplate.findOne(query,AsyncApi.class);
	}

	public void updateAsyncApi(String id,String asyncApi, String jsessionid) throws ItorixException {
		AsyncApi existing = getExistingAsyncById(id);
		if(existing == null){
			throw new ItorixException(ErrorCodes.errorMessage.get("AsyncApi-1012"),"AsyncApi-1012");
		}
		UserSession user = getUser(jsessionid);
		existing.setModifiedUserName(user.getUsername());
		existing.setModifiedBy(user.getUserId());
		existing.setMts(System.currentTimeMillis());
		existing.setAsyncApi(asyncApi);
		mongoTemplate.save(existing);
		initiateLinting(jsessionid, existing.getAsyncApiId(), existing.getRevision(),
				existing.getRuleSetIds());
	}

	private int groupByAsyncIdAndGetMaxRevision(String asyncApiId) {
		GroupOperation groupByMaxRevision = group("asyncApiId")
				.max("revision").as("maxRevision");
		MatchOperation matchOperation = match(Criteria.where("asyncApiId").is(asyncApiId));
		Aggregation aggregation = newAggregation(matchOperation,groupByMaxRevision);
		Document document = mongoTemplate.aggregate(aggregation,AsyncApi.class,
				Document.class).getUniqueMappedResult();
		if(document!=null && document.get("maxRevision")!=null)
			return Integer.parseInt(document.get("maxRevision").toString());
		return 1;
	}

	private User getUserDetailsFromSessionID(String jsessionid) {
		UserSession userSessionToken = masterMongoTemplate.findById(jsessionid, UserSession.class);
		User user = masterMongoTemplate.findById(userSessionToken.getUserId(), User.class);
		return user;
	}

	public Object getAllAsyncApis(String jsessionid,int offset,int pageSize,
			Optional<String> name, Optional<String> sortBy, Optional<String> status){

		String[] PROJECTION_FIELDS = {"id", "name", "revision", "asyncApiId", "status", "asyncApi", "createdBy",
				"createdUserName", "modifiedBy", "modifiedUserName", "cts", "mts", "lock", "lockedBy", "lockedAt",
				"lockedByUserId"};
		ProjectionOperation projectRequiredFields = project(PROJECTION_FIELDS);

		GroupOperation groupByMaxRevision = group("$asyncApiId").max("revision")
				.as("maxRevision").push("$$ROOT")
				.as("originalDoc");

		ProjectionOperation filterMaxRevision = project().and(
				filter("originalDoc").as("doc").by(valueOf("maxRevision").equalToValue("$$doc.revision")))
				.as("originalDoc");
		SortOperation sortOperation = sort(Direction.ASC, "name");
		UnwindOperation unwindOperation = unwind("originalDoc");
		ProjectionOperation projectionOperation = project("originalDoc.name").andInclude("originalDoc.asyncApiId",
				"originalDoc.revision", "originalDoc.status", "originalDoc.createdBy", "originalDoc.createdUserName",
				"originalDoc.modifiedBy", "originalDoc.modifiedUserName", "originalDoc.cts", "originalDoc.mts",
				"originalDoc._id", "originalDoc.lock", "originalDoc.lockedBy", "originalDoc.lockedAt",
				"originalDoc.lockedByUserId");

		MatchOperation searchOperation = null;
		MatchOperation statusOperation = null;
		if(name.isPresent()){
			searchOperation = new MatchOperation(Criteria.where("name").regex(name.get()));
		}
		if(status.isPresent()){
			statusOperation = new MatchOperation(Criteria.where("status").regex(status.get()));
		}
		if(sortBy.isPresent()) {
			if(sortBy.get().equalsIgnoreCase("asc")) {
				sortOperation = sort(Direction.ASC, "mts");
			}
			else if(sortBy.get().equalsIgnoreCase("desc")) {
				sortOperation = sort(Direction.DESC, "mts");
			}
		}
		List<AsyncApi> results;
		if(searchOperation!=null && statusOperation!=null){
			results= mongoTemplate.aggregate(
					newAggregation(projectRequiredFields, groupByMaxRevision, filterMaxRevision,
							unwindOperation, projectionOperation, sortOperation,searchOperation,statusOperation),
					AsyncApi.class, AsyncApi.class).getMappedResults();
		}
		else if(searchOperation!=null){
			results= mongoTemplate.aggregate(
					newAggregation(projectRequiredFields, groupByMaxRevision, filterMaxRevision,
							unwindOperation, projectionOperation, sortOperation, searchOperation),
					AsyncApi.class, AsyncApi.class).getMappedResults();
		}
		else if(statusOperation!=null){
			results= mongoTemplate.aggregate(
					newAggregation(projectRequiredFields, groupByMaxRevision, filterMaxRevision,
							unwindOperation, projectionOperation, sortOperation,statusOperation),
					AsyncApi.class, AsyncApi.class).getMappedResults();
		}
		else {
			results= mongoTemplate.aggregate(
					newAggregation(projectRequiredFields, groupByMaxRevision, filterMaxRevision,
							unwindOperation, projectionOperation, sortOperation),
					AsyncApi.class, AsyncApi.class).getMappedResults();
		}

		Long counter = (long) results.size();
      results = trimList(results, offset, pageSize);
      Pagination pagination = new Pagination();
      pagination.setOffset(offset);
      pagination.setTotal(counter);
      pagination.setPageSize(pageSize);
      PaginatedResponse paginatedResponse = new PaginatedResponse();
      paginatedResponse.setPagination(pagination);
      paginatedResponse.setData(results);
      return paginatedResponse;
	}
	private List<AsyncApi> trimList(List<AsyncApi> asyncApis, int offset, int pageSize) {
		List<AsyncApi> dictionaryIds = new ArrayList<>();
		int i = offset > 0 ? ((offset - 1) * pageSize) : 0;
		int end = i + pageSize;
		for (; i < asyncApis.size() && i < end; i++) {
			dictionaryIds.add(asyncApis.get(i));
		}
		return dictionaryIds;
	}

	public List<AsyncApi> getAllRevisions(String asyncapiId) {
		Query query = new Query(Criteria.where("asyncApiId").is(asyncapiId));
		List<AsyncApi> documents = mongoTemplate.find(query,AsyncApi.class);
		if(documents.size()>0)
			return documents;
		return new ArrayList<>();
	}

	public void deleteAsyncApi(String id) {
		Query query = new Query(Criteria.where("asyncApiId").is(id));
		mongoTemplate.findAllAndRemove(query,AsyncApi.class);
 	}

	public AsyncApi getAsyncApiRevision(String asyncapiId, int revison) {
		Query query = new Query();
		query.addCriteria(Criteria.where("asyncApiId").is(asyncapiId));
		query.addCriteria(Criteria.where("revision").is(revison));
		return mongoTemplate.findOne(query,AsyncApi.class);
	}

	public Object lockStatus(String asyncapiId, int revison, AsyncApi asyncApi)
			throws ItorixException {
		AsyncApi existing = getAsyncApiRevision(asyncapiId,revison);
		if(existing==null)
			throw new ItorixException(ErrorCodes.errorMessage.get("AsyncApi-1013"),"AsyncApi-1013");
		existing.setLock(asyncApi.getLock());
		return mongoTemplate.save(existing);
	}

	public List<AsyncApi> search(String name) {
		String[] PROJECTION_FIELDS = {"name", "revision", "asyncApiId"};
		ProjectionOperation projectRequiredFields = project(PROJECTION_FIELDS);

		GroupOperation groupByMaxRevision = group("$asyncApiId").max("revision")
				.as("maxRevision").push("$$ROOT")
				.as("originalDoc");

		ProjectionOperation filterMaxRevision = project().and(
						filter("originalDoc").as("doc").by(valueOf("maxRevision").equalToValue("$$doc.revision")))
				.as("originalDoc");
		SortOperation sortOperation = sort(Direction.ASC, "name");
		UnwindOperation unwindOperation = unwind("originalDoc");
		ProjectionOperation projectionOperation = project("originalDoc.name")
				.andInclude("originalDoc.asyncApiId",
						"originalDoc.revision");
		ProjectionOperation excludeFields = project().andExclude("id" , "enableScm");
		MatchOperation searchOperation = new MatchOperation(Criteria.where("name").regex(".*"+name+".*","i"));
		return mongoTemplate.aggregate(
					newAggregation(projectRequiredFields, searchOperation, excludeFields,
							groupByMaxRevision, filterMaxRevision, unwindOperation, projectionOperation, sortOperation),
					AsyncApi.class, AsyncApi.class).getMappedResults();
	}

	public List<AsyncapiImport> importAsyncApis(MultipartFile zipFile, String type, String gitURI, String branch, String authType, String userName, String password,
			String personalToken,String jsessionid) throws Exception {
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
		}
		else if (type.equals("file")) {
			try {
				fileLocation = applicationProperties.getTempDir() + "/asyncapiImport";
				String file = applicationProperties.getTempDir() + zipFile.getOriginalFilename();
				File targetFile = new File(file);
				File cloningDirectory = new File(fileLocation);
				cloningDirectory.mkdirs();
				zipFile.transferTo(targetFile);
				unZip.unzip(file, fileLocation);
			} catch (Exception e) {
				log.error("Exception occurred : {}", e.getMessage());
			}
		}
		else {
			String message = "Invalid request data! Invalid type provided supported values - git, file";
			throw new ItorixException(message, "General-1001");
		}
		List<File> files = unZip.getJsonFiles(fileLocation);
		if (files.isEmpty()) {
			String message = "Invalid request data! Invalid file type";
			throw new ItorixException(message, "General-1001");
		} else {
			List<AsyncapiImport> asyncapiImports = new ArrayList<AsyncapiImport>();
			try {
				asyncapiImports = importAsyncApisFromFiles(files,jsessionid);
			} catch (Exception e) {
				log.error("error occurred while importing async apis", e);
				throw new ItorixException(e.getMessage(), "General-1000");
			} finally {
				FileUtils.cleanDirectory(new File(fileLocation));
				FileUtils.deleteDirectory(new File(fileLocation));
			}
			return asyncapiImports;
		}
	}

	private List<AsyncapiImport> importAsyncApisFromFiles(List<File> files,String jessionid) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		List<AsyncapiImport> asyncapiImports = new ArrayList<AsyncapiImport>();
		for (File file : files) {
			try {
				String filecontent;
				if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("yaml")) {
					filecontent = convertYamlToJson(FileUtils.readFileToString(file));
				} else {
					filecontent = FileUtils.readFileToString(file);
				}
				String reason = null;
				AsyncapiImport asyncapiImport = new AsyncapiImport();
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				JsonNode asyncApiObject = mapper.readTree(filecontent);
				if(asyncApiObject.get("asyncapi")!=null){
					try {
						JsonNode info = asyncApiObject.path("info");
						if (info != null) {
							String asyncapiName = null;
							try {
								asyncapiName = info.get("title").asText();
							}catch(NullPointerException e){
								reason = "Title tag not found";
								asyncapiImport.setName(file.getName());
								asyncapiImport.setLoaded(false);
								asyncapiImport.setReason(reason);
								continue;
							}
							if(asyncapiName.isEmpty()){
								asyncapiName = FilenameUtils.removeExtension(file.getName());
							}
							AsyncApi asyncApi = new AsyncApi();
							asyncApi.setName(asyncapiName);
							asyncApi.setAsyncApi(filecontent);
							asyncapiImport.setLoaded(false);
							asyncapiImport.setName(asyncapiName);
							asyncapiImport.setPath(file.getAbsolutePath());
							try {
								createAsyncApiOrPushToDesignStudio(asyncApi,jessionid);
								asyncapiImport.setAsyncapiId(asyncApi.getAsyncApiId());
								asyncapiImport.setLoaded(true);
							} catch (ItorixException e) {
								log.error("Exception occured : {}",e.getMessage(), e);
								asyncapiImport.setReason(e.getMessage());
							}
							asyncapiImports.add(asyncapiImport);
						} else {
							new ItorixException("invalid JSON file");
						}
					} catch (Exception e) {
						log.error(e.getMessage(), e);
						asyncapiImport.setName(file.getName());
						asyncapiImport.setLoaded(false);
						asyncapiImport.setReason(e.getMessage());
						asyncapiImports.add(asyncapiImport);
					}
				}
				else{
					asyncapiImport.setLoaded(false);
					asyncapiImport.setName(file.getName());
					asyncapiImport.setReason("invalid JSON file");
					asyncapiImports.add(asyncapiImport);
					log.error("Invalid json data");
				}
			}
			catch (Exception e) {
				AsyncapiImport asyncapiImport = new AsyncapiImport();
				asyncapiImport.setLoaded(false);
				asyncapiImport.setName(file.getName());
				asyncapiImport.setReason("invalid JSON file");
				asyncapiImports.add(asyncapiImport);
				log.error("Exception occurred", e);
			}
		}
		return asyncapiImports;
	}

	private String getReplacedURLStr(HashMap<String,Object> server) {
		String urlStr = server.get("url").toString();

		if (server.get("variables") != null) {
			HashMap<String,Object> variables = new ObjectMapper().convertValue(server.get("variables"), new TypeReference<>() {});
			for (String k : variables.keySet()) {
				if (urlStr.contains("{" + k + "}")) {
					HashMap<String,Object> port = new ObjectMapper().convertValue(variables.get(k), new TypeReference<>() {});
					if (port != null) {
						urlStr = urlStr.replace("{" + k + "}", port.get("default").toString());
					}
				}
			}
		}
		return urlStr;
	}


	public List<Revision> getListOfAsyncRevisions(String name, String interactionid) {
		// log("getListOfRevisions", interactionid, name);
		List<AsyncApi> ascynApis;
		ascynApis = baseRepository.find("id", name, AsyncApi.class);
		if (ascynApis != null && ascynApis.size() > 0) {
			name = ascynApis.get(0).getName();
		}
		ascynApis = baseRepository.find("name", name, AsyncApi.class);
		if(ascynApis.isEmpty()) {
			ascynApis = baseRepository.find("asyncApiId", name, AsyncApi.class);
		}
		List<Revision> versions = new ArrayList<Revision>();
		for (AsyncApi asyncApi : ascynApis) {
			Revision version = new Revision();
			version.setRevision(asyncApi.getRevision());
			version.setStatus(asyncApi.getStatus());
			version.setId(asyncApi.getAsyncApiId() != null ? asyncApi.getAsyncApiId() : asyncApi.getId());
			versions.add(version);
		}
		return versions;
	}

	private void publishAsyncApiBasepaths() throws Exception {
		List<String> names = baseRepository.findDistinctValuesByColumnName(AsyncApi.class, "name");
		for (String name : names) {
			List<Revision> versions = getListOfAsyncRevisions(name, null);
			AsyncApi asyncApi = null;
			if (versions != null && versions.size() > 0) {
				Revision revision = Collections.max(versions);
				asyncApi = baseRepository.findOne("name", name, "revision", revision.getRevision(),
						AsyncApi.class);
			}
			if (asyncApi != null) {
				updateAsyncBasePath(name, asyncApi);
			}
		}
	}
	private List<AsyncApiBasePath> getAsyncApiBasePaths() throws Exception {
		List<AsyncApiBasePath> mappings = null;
		try {
			mappings = mongoTemplate.findAll(AsyncApiBasePath.class);
		} catch (Exception ex) {
			log.error("Error while finding Asyncapi base path {} ", ex.getMessage(), ex);
		}
		if (mappings == null || mappings.size() == 0) {
			publishAsyncApiBasepaths();
			mappings = mongoTemplate.findAll(AsyncApiBasePath.class);
		}
		return mappings;
	}

	public void updateAsyncBasePath(String name, AsyncApi asyncApi) throws Exception {
		log.info("Updating Async api BasePath for URL {}", asyncApi.getName());
		Set<String> basePaths = new HashSet();

		JsonNode jsonNode = new ObjectMapper().readTree(asyncApi.getAsyncApi());
		//JsonNode jsonNode = new ObjectMapper().convertValue(asyncApi.getAsyncApi(), new TypeReference<>() {});;
		//HashMap<String,Object> dataModel = new ObjectMapper().convertValue(jsonNode, new TypeReference<>() {});
		HashMap<String,Object> servers = new ObjectMapper().convertValue(jsonNode.get("servers"), new TypeReference<>() {});
		for (Map.Entry<String, Object> entry : servers.entrySet()) {
			String key = entry.getKey();
			HashMap<String,Object> server = new ObjectMapper().convertValue(entry.getValue(), new TypeReference<>() {});
			String urlStr = getReplacedURLStr(server);
			try {
				URL url = new URL(urlStr);
				if(!url.getPath().equals(""))
					basePaths.add(url.getPath());
			} catch (MalformedURLException e) {
				log.error("Error while getting basePath for Asyncapi : {} URL {} ", e.getMessage(),urlStr, e);
			}
			// ...
		}
		if (!basePaths.isEmpty()) {
			AsyncApiBasePath basePath = new AsyncApiBasePath();
			basePath.setName(name);
			basePath.setBasePath(new LinkedList<>(basePaths));
			saveAsyncApiBasePath(basePath);
		}
	}

	private void saveAsyncApiBasePath(AsyncApiBasePath basePath) {
		Query query = new Query(Criteria.where("name").is(basePath.getName()));
		Update update = new Update();
		update.set("basePath", basePath.getBasePath());
		mongoTemplate.upsert(query, update, AsyncApiBasePath.class);
	}

	private String convertYamlToJson(String yaml) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
		Object obj = yamlReader.readValue(yaml, Object.class);
		ObjectMapper jsonWriter = new ObjectMapper();
		return jsonWriter.writeValueAsString(obj);
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

	public void createAsyncApiRevision(String jsessionId,String asyncId, String asyncapi,Integer revision)
			throws ItorixException{
		AsyncApi existing;
		if(revision!=null){
			Query query=new Query();
			query.addCriteria(Criteria.where("asyncApiId").is(asyncId)).addCriteria(Criteria.where("revision").is(revision));
			existing=mongoTemplate.findOne(query, AsyncApi.class);
		}else {
			existing = getExistingAsyncById(asyncId);
		}
		if(existing!=null){
			AsyncApi asyncApiObj = new AsyncApi();
			UserSession user = getUser(jsessionId);
			JSONObject jsonObject = new JSONObject(asyncapi);
			JSONObject info = (JSONObject) jsonObject.get("info");
			asyncApiObj.setName(info.get("title").toString());
			asyncApiObj.setLock(true);
			asyncApiObj.setStatus(Status.Draft.getStatus());
			asyncApiObj.setLockedBy(user.getUsername());
			asyncApiObj.setHistory(List.of(new StatusHistory(Status.Draft.getStatus(), String.format("New Async Api - %s is created", asyncApiObj.getName()))));
			asyncApiObj.setCts(System.currentTimeMillis());
			asyncApiObj.setMts(System.currentTimeMillis());
			asyncApiObj.setCreatedBy(user.getUserId());
			asyncApiObj.setCreatedUserName(user.getUsername());
			asyncApiObj.setAsyncApi(asyncapi);
			asyncApiObj.setEnableScm(existing.isEnableScm());
			asyncApiObj.setRepoName(existing.getRepoName());
			asyncApiObj.setBranch(existing.getBranch());
			asyncApiObj.setHostUrl(existing.getHostUrl());
			asyncApiObj.setFolderName(existing.getFolderName());
			asyncApiObj.setToken(existing.getToken());
			asyncApiObj.setScmSource(existing.getScmSource());
			asyncApiObj.setUsername(existing.getUsername());
			asyncApiObj.setPassword(existing.getPassword());
			asyncApiObj.setAuthType(existing.getAuthType());
			int maxRevision = groupByAsyncIdAndGetMaxRevision(asyncId);
			asyncApiObj.setRevision(maxRevision+1);
			asyncApiObj.setAsyncApiId(existing.getAsyncApiId());
			mongoTemplate.save(asyncApiObj);
			if (asyncApiObj.isEnableScm()) {
				try {
					syncBusiness.sync2Repo(asyncApiObj, null, ASYNC);
				} catch (Exception exception) {
					log.error("Error while syncing Async API:{} revision:{} to repo.", asyncApiObj.getId(), asyncApiObj.getRevision(), exception);
				}
			}
			initiateLinting(jsessionId, asyncApiObj.getAsyncApiId(), asyncApiObj.getRevision(),
					existing.getRuleSetIds());
		}
		else
			throw new ItorixException(ErrorCodes.errorMessage.get("AsyncApi-1012"),"AsyncApi-1012");
	}


	public void updateAsyncApiRevision(String jsessionId,String asyncId, AsyncApi asyncapi,int revision)
			throws ItorixException {
		AsyncApi existing = getExistingAsyncByIdAndRevision(asyncId,revision);
		if(existing!=null){
			existing.setMts(System.currentTimeMillis());
			existing.setModifiedBy(ServiceRequestContextHolder.getContext().getUserSessionToken().getUserId());
			existing.setModifiedUserName(ServiceRequestContextHolder.getContext().getUserSessionToken().getUsername());
			existing.setAsyncApi(asyncapi.getAsyncApi());
			mongoTemplate.save(existing);
			if (existing.isEnableScm()) {
				try {
					syncBusiness.sync2Repo(existing, null, ASYNC);
				} catch (Exception exception) {
					log.error("Error while syncing Async API:{} revision:{} to repo.", existing.getId(), existing.getRevision(), exception);
				}
			}
			initiateLinting(jsessionId, existing.getAsyncApiId(), existing.getRevision(),
					existing.getRuleSetIds());
		}
		else
			throw new ItorixException(ErrorCodes.errorMessage.get("AsyncApi-1012"),"AsyncApi-1012");
	}

	public void deleteAsyncApiRevision(String asyncId,int revision)
			throws ItorixException {
		AsyncApi existing = getExistingAsyncByIdAndRevision(asyncId,revision);
		if(existing!=null){
			mongoTemplate.remove(existing);
		}
		else
			throw new ItorixException(ErrorCodes.errorMessage.get("AsyncApi-1012"),"AsyncApi-1012");
	}
	private void initiateLinting(String jsessionid,
			String asyncApiId, Integer revision, List<String> ruleSetIds) {
		try {
			String globalRule=getGolbalRule();
			if(globalRule!=null&&ruleSetIds!=null&&!ruleSetIds.contains(globalRule))
			{
				ruleSetIds.add(globalRule);
			}
			else if(globalRule!=null&&ruleSetIds==null){
				ruleSetIds=new ArrayList<String>();
				ruleSetIds.add(globalRule);
			}
			AsyncLintingInfo asyncLintingInfo = new AsyncLintingInfo();
			asyncLintingInfo.setAsyncApiId(asyncApiId);
			asyncLintingInfo.setRevision(revision);
			asyncLintingInfo.setRuleSetIds(ruleSetIds);
			callLintingAPI(asyncLintingInfo, jsessionid);
		} catch (Exception ex) {
			log.error("Error while calling linting API. ", ex);
		}
	}

	public String getGolbalRule() {
		Query query = Query.query(Criteria.where("ruleSetType").is("async").and("isGlobalRuleSet").is(true));
		Document ruleset = mongoTemplate.findOne(query, Document.class,"Linter.RuleSet");
		return ruleset!=null?ruleset.get("_id").toString():null;
	}

	private void callLintingAPI(AsyncLintingInfo asyncLintingInfo, String jsessionid) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.set("jsessionid", jsessionid);
		HttpEntity<AsyncLintingInfo> entity = new HttpEntity<>(asyncLintingInfo, httpHeaders);

		try {
			restTemplate.exchange(lintingUrl+ lintAsync, HttpMethod.POST, entity, String.class).getBody();
		} catch (Exception e) {
			log.error("Error while calling linting API. ", e);
		}

	}

	public void updateStatus(String jsessionId, String asyncId, StatusHistory statusHistory, int revision) throws ItorixException {
		AsyncApi asyncApi = getExistingAsyncByIdAndRevision(asyncId,revision);
		if(asyncApi == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("AsyncApi-1012"),"AsyncApi-1012");
		}
		if (asyncApi.getStatus().equals(statusHistory.getStatus())) {
			throw new ItorixException(ErrorCodes.errorMessage.get("AsyncApi-1014"),"AsyncApi-1014");
		}
		if (statusHistory.getStatus().equals(Status.Publish.getStatus())) {
			AsyncApi tempAsyncApi = mongoTemplate.findOne(new Query(Criteria.where("asyncApiId").is(asyncId)
					.and(STATUS).is(Status.Publish.getStatus())), AsyncApi.class);
			if (tempAsyncApi != null) {
				tempAsyncApi.setStatus(Status.Draft.getStatus());
				tempAsyncApi.getHistory().add(new StatusHistory(Status.Draft.getStatus(), String.format("Moved to Draft from Publish since revision %s got Published",revision)));
				tempAsyncApi.setMts(System.currentTimeMillis());
				tempAsyncApi.setModifiedUserName(ServiceRequestContextHolder.getContext().getUserSessionToken().getUsername());
				mongoTemplate.save(tempAsyncApi);
			}
		}
		List<StatusHistory> history = asyncApi.getHistory();
		if (history == null) {
			history = new ArrayList<>();
		}
		statusHistory.setCts(System.currentTimeMillis());
		statusHistory.setLastModifiedBy(ServiceRequestContextHolder.getContext().getUserSessionToken().getUsername());
		history.add(statusHistory);
		asyncApi.setStatus(statusHistory.getStatus());
		asyncApi.setHistory(history);
		mongoTemplate.save(asyncApi);
		initiateLinting(jsessionId, asyncApi.getAsyncApiId(), asyncApi.getRevision(),
				asyncApi.getRuleSetIds());
	}

	public Object getAllAsyncApisStats(String jsessionid) {
		String[] PROJECTION_FIELDS = {"id","revision", "asyncApiId", "status"};
		ProjectionOperation projectRequiredFields = project(PROJECTION_FIELDS);
		GroupOperation groupByMaxRevision = group("$asyncApiId").max("revision")
				.as("maxRevision").push("$$ROOT")
				.as("originalDoc");
		ProjectionOperation filterMaxRevision = project().and(
						filter("originalDoc").as("doc").by(valueOf("maxRevision")
								.equalToValue("$$doc.revision"))).as("originalDoc");
		UnwindOperation unwindOperation = unwind("originalDoc");
		ProjectionOperation projectionOperation = project()
				.and("$originalDoc.status").as("status");
		GroupOperation groupOperation = group("$status").count().as("count");
		ProjectionOperation finalProjection = project("count").and("$_id").as("status");

		Aggregation aggregation = newAggregation(projectRequiredFields,groupByMaxRevision
				,filterMaxRevision,unwindOperation,projectionOperation,groupOperation,finalProjection);

		List<Document> documents = mongoTemplate.aggregate(aggregation,AsyncApi.class,Document.class)
				.getMappedResults();
		List<Stat> statsList = new ArrayList<>();
		if(documents.size()>0){
			documents.forEach(document -> {
				Stat stats = new Stat();
				stats.setName(document.get("status").toString());
				stats.setCount(document.get("count").toString());
				statsList.add(stats);
			});
		}
		return statsList;
	}

	public Object getAllAsyncApisNames() {

		String[] PROJECTION_FIELDS = {"id", "name", "revision", "asyncApiId", "status","asyncApi",
				"createdBy", "modifiedBy", "cts", "mts","lock"};
		ProjectionOperation projectRequiredFields = project(PROJECTION_FIELDS);

		GroupOperation groupByMaxRevision = group("$asyncApiId").max("revision")
				.as("maxRevision").push("$$ROOT")
				.as("originalDoc");

		ProjectionOperation filterMaxRevision = project().and(
						filter("originalDoc").as("doc").by(valueOf("maxRevision").
								equalToValue("$$doc.revision"))).as("originalDoc");
		SortOperation sortOperation = sort(Direction.ASC, "name");
		UnwindOperation unwindOperation = unwind("originalDoc");
		ProjectionOperation projectionOperation = project("originalDoc.name")
				.andInclude("originalDoc.asyncApiId", "originalDoc._id");

		return mongoTemplate.aggregate(
					newAggregation(projectRequiredFields, groupByMaxRevision, filterMaxRevision,
							unwindOperation, projectionOperation, sortOperation),
					AsyncApi.class, AsyncApi.class).getMappedResults();
	}
	private List<AsyncApi> findAsyncApiWithName(String name) {
		Query query = new Query();
		query.addCriteria(Criteria.where("name").is(name));
		return mongoTemplate.find(query, AsyncApi.class);
	}
}
