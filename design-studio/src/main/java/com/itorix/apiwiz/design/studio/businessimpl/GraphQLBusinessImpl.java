package com.itorix.apiwiz.design.studio.businessimpl;

import static com.itorix.apiwiz.identitymanagement.model.Constants.COUNT;
import static com.itorix.apiwiz.identitymanagement.model.Constants.GRAPHQL_ID;
import static com.itorix.apiwiz.identitymanagement.model.Constants.GRAPHQL_PROJECTION_FIELDS;
import static com.itorix.apiwiz.identitymanagement.model.Constants.MAX_REVISION;
import static com.itorix.apiwiz.identitymanagement.model.Constants.ORIGINAL_DOC;
import static com.itorix.apiwiz.identitymanagement.model.Constants.REVISION;
import static com.itorix.apiwiz.identitymanagement.model.Constants.STATUS;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;
import static org.springframework.data.mongodb.core.aggregation.ArrayOperators.Filter.filter;
import static org.springframework.data.mongodb.core.aggregation.ComparisonOperators.Eq.valueOf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.common.util.scm.ScmUtilImpl;
import com.itorix.apiwiz.common.util.zip.ZIPUtil;
import com.itorix.apiwiz.design.studio.business.GraphQLBusiness;
import com.itorix.apiwiz.design.studio.model.GraphQL;
import com.itorix.apiwiz.design.studio.model.GraphQLImport;
import com.itorix.apiwiz.design.studio.model.Revision;
import com.itorix.apiwiz.design.studio.model.Stat;
import com.itorix.apiwiz.design.studio.model.Status;
import com.itorix.apiwiz.design.studio.model.SwaggerHistoryResponse;
import com.itorix.apiwiz.design.studio.model.SwaggerLockResponse;
import com.itorix.apiwiz.design.studio.model.swagger.sync.StatusHistory;
import com.itorix.apiwiz.identitymanagement.model.Pagination;
import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContextHolder;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import graphql.GraphQLException;
import graphql.parser.Parser;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators.Cond;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GraphQLBusinessImpl implements GraphQLBusiness {

  private static final Logger logger = LoggerFactory.getLogger(GraphQLBusinessImpl.class);

  @Autowired
  private MongoTemplate mongoTemplate;

  @Qualifier("masterMongoTemplate")
  @Autowired
  private MongoTemplate masterTemplate;

  @Autowired
  private ScmUtilImpl scmImpl;

  @Autowired
  ApplicationProperties applicationProperties;

  @Override
  public void create(GraphQL graphQL) {
    graphQL.setRevision(1);
    graphQL.setStatus(Status.Draft);
    graphQL.setLock(false);
    graphQL.setId(null);
    graphQL.setGraphQLId(UUID.randomUUID().toString().replaceAll("-", ""));
    updateUserDetails(graphQL);
    mongoTemplate.save(graphQL);
    logger.info("Successfully created a new GraphQL Schema");
  }

  @Override
  public void updateWithRevision(String graphQLId,Integer revision,String graphqlSchema) throws ItorixException{
    GraphQL graphQL = new GraphQL();
    graphQL.setGraphQLId(graphQLId);
    graphQL.setRevision(revision);
    GraphQL checkGraphQL = findGraphQL(graphQL);
    if(checkGraphQL!=null){
      checkGraphQL.setGraphQLSchema(graphqlSchema);
      updateUserDetails(checkGraphQL);
      mongoTemplate.save(checkGraphQL);
      logger.info("Successfully updated the GraphQL schema for Id - {} and revision - {}",graphQLId,revision);
    }else{
      logger.error("No Data found for Id - {} and revision - {}",graphQLId,revision);
      throw new ItorixException(String.format(ErrorCodes.errorMessage.get("GraphQL-1000"),graphQLId,revision), "GraphQL-1000");
    }
  }

  @Override
  public GraphQL getWithRevision(String graphQLId, Integer revision) throws ItorixException {
    GraphQL graphQL = mongoTemplate.findOne(new Query(Criteria.where(GRAPHQL_ID).is(graphQLId)
        .and(REVISION).is(revision)),GraphQL.class);
    if(graphQL != null){
      return graphQL;
    }else{
      logger.error("No Data found for Id - {} and revision - {}",graphQLId,revision);
      throw new ItorixException(String.format(ErrorCodes.errorMessage.get("GraphQL-1000"),graphQLId,revision), "GraphQL-1000");
    }
  }

  @Override
  public void deleteWithRevision(String graphQLId, Integer revision) throws ItorixException{
    GraphQL graphQL = mongoTemplate.findOne(new Query(Criteria.where(GRAPHQL_ID).is(graphQLId)
        .and(REVISION).is(revision)),GraphQL.class);
    if(graphQL != null){
      mongoTemplate.remove(graphQL);
      logger.info("Successfully deleted the GraphQL schema for Id - {} and revision - {}",graphQLId,revision);
    }else{
      logger.error("No Data found for Id - {} and revision - {}",graphQLId,revision);
      throw new ItorixException(String.format(ErrorCodes.errorMessage.get("GraphQL-1000"),graphQLId,revision), "GraphQL-1000");
    }
  }

  @Override
  public void createNewRevisionWithName(GraphQL graphQL) throws ItorixException {
    GraphQL existingGraphQL = findGraphQL(graphQL);
    if(existingGraphQL!=null) {
      List<Revision> revisions = getListOfRevisions(existingGraphQL.getGraphQLId());
      if (revisions != null && revisions.size() > 0) {
        Revision revision = Collections.max(revisions);
        Integer newRevision = revision.getRevision() + 1;
        graphQL.setRevision(newRevision);
        graphQL.setStatus(Status.Draft);
        graphQL.setLock(false);
        graphQL.setId(null);
        graphQL.setGraphQLId(existingGraphQL.getGraphQLId());
        updateUserDetails(graphQL);
        mongoTemplate.save(graphQL);
        logger.info("Successfully created new revision of the GraphQL schema for Name - {}",graphQL.getName());
      }
    }else{
      logger.error("No Data found for Name - {}",graphQL.getName());
      throw new ItorixException(String.format(ErrorCodes.errorMessage.get("GraphQL-1001"),graphQL.getName()), "GraphQL-1001");
    }
  }

  @Override
  public void createNewRevisionWithId(GraphQL graphQL) throws ItorixException {
    GraphQL checkGraphQL = findGraphQL(graphQL);
    if(checkGraphQL!=null) {
      List<Revision> revisions = getListOfRevisions(checkGraphQL.getGraphQLId());
      if (revisions != null && revisions.size() > 0) {
        Revision revision = Collections.max(revisions);
        Integer newRevision = revision.getRevision() + 1;
        graphQL.setRevision(newRevision);
        graphQL.setStatus(Status.Draft);
        graphQL.setLock(false);
        graphQL.setId(null);
        graphQL.setGraphQLId(checkGraphQL.getGraphQLId());
        graphQL.setName(checkGraphQL.getName());
        updateUserDetails(graphQL);
        mongoTemplate.save(graphQL);
        logger.info("Successfully created new revision of the GraphQL schema for Id - {}",graphQL.getGraphQLId());
      }
    }else{
      logger.error("No Data found for Id - {}",graphQL.getGraphQLId());
      throw new ItorixException(String.format(ErrorCodes.errorMessage.get("GraphQL-1001"),graphQL.getGraphQLId()), "GraphQL-1001");
    }
  }

  @Override
  public void changeStatusWithRevision(String graphQLId, Integer revision, StatusHistory statusHistory)
      throws ItorixException {
    GraphQL checkGraphQL = new GraphQL();
    checkGraphQL.setGraphQLId(graphQLId);
    checkGraphQL.setRevision(revision);
    GraphQL graphQL = findGraphQL(checkGraphQL);
    if(graphQL!=null) {
      List<StatusHistory> history = graphQL.getHistory();
      if (history == null) {
        history = new ArrayList<>();
      }
      if(graphQL.getStatus().equals(statusHistory.getStatus())){
        throw new ItorixException(ErrorCodes.errorMessage.get("GraphQL-1002"), "GraphQL-1002");
      }
      statusHistory.setMts(System.currentTimeMillis());
      statusHistory.setUserName(ServiceRequestContextHolder.getContext().getUserSessionToken().getUsername());
      history.add(statusHistory);
      graphQL.setHistory(history);
      Status statusObject = statusHistory.getStatus();
      if (statusObject.equals(Status.Publish)) {
        GraphQL publishedGraphQL = new GraphQL() ;
        publishedGraphQL = mongoTemplate.findOne(new Query(Criteria.where(GRAPHQL_ID).is(graphQLId)
            .and(STATUS).is(statusObject)), GraphQL.class);
        if (publishedGraphQL != null) {
          List<StatusHistory> publishedGraphQLHistoryList = publishedGraphQL.getHistory();
          if (publishedGraphQLHistoryList == null) {
            publishedGraphQLHistoryList = new ArrayList<>();
          }
          StatusHistory publishedStatusHistory = new StatusHistory();
          publishedStatusHistory.setStatus(Status.Draft);
          publishedStatusHistory.setMts(System.currentTimeMillis());
          publishedStatusHistory.setMessage(String.format("Moved to Draft from Publish since revision %s got Published",revision));
          publishedStatusHistory.setUserName("System Job");
          publishedGraphQLHistoryList.add(publishedStatusHistory);
          publishedGraphQL.setHistory(publishedGraphQLHistoryList);
          publishedGraphQL.setStatus(Status.Draft);
          updateUserDetails(publishedGraphQL);
          mongoTemplate.save(publishedGraphQL);
        }
      }
        graphQL.setStatus(statusObject);
        updateUserDetails(graphQL);
        mongoTemplate.save(graphQL);
        logger.info("Successfully updated the status of the GraphQL schema for Id - {} and revision - {}",graphQLId,revision);
    }else{
      logger.error("No Data found for Id - {} and revision - {}",graphQLId,revision);
      throw new ItorixException(String.format(ErrorCodes.errorMessage.get("GraphQL-1000"),graphQLId,revision), "GraphQL-1000");
    }
  }

  @Override
  public List<GraphQL> getAllRevisionsWithId(String graphQLId) throws ItorixException {
    List<GraphQL> graphQLList = mongoTemplate.find(new Query(Criteria.where(GRAPHQL_ID).is(graphQLId)),GraphQL.class);
    if(!graphQLList.isEmpty()){
      return graphQLList;
    }else{
      logger.error("No Data found for Id - {}",graphQLId);
      throw new ItorixException(String.format(ErrorCodes.errorMessage.get("GraphQL-1001"),graphQLId), "GraphQL-1001");
    }
  }

  @Override
  public void deleteAllRevisionsWithId(String graphQLId) throws ItorixException {
    List<GraphQL> graphQLList = mongoTemplate.find(new Query(Criteria.where(GRAPHQL_ID).is(graphQLId)),GraphQL.class);
    if(!graphQLList.isEmpty()){
      mongoTemplate.remove(new Query(Criteria.where(GRAPHQL_ID).is(graphQLId)),GraphQL.class);
      logger.info("Successfully deleted all the GraphQL schema for Id - {}",graphQLId);
    }else{
      logger.error("No Data found for Id - {}",graphQLId);
      throw new ItorixException(String.format(ErrorCodes.errorMessage.get("GraphQL-1001"),graphQLId), "GraphQL-1001");
    }

  }

  @Override
  public Object getHistory(String jsessionId,int offset, int pagesize, String status, String sortByMts,String name,int limit) {

    SwaggerHistoryResponse response = new SwaggerHistoryResponse();
    Map<String, String> filterFieldsAndValues = new HashMap<>();
    if(status != null) {
      filterFieldsAndValues.put(STATUS, status);
    }
    if(name!=null) {
      filterFieldsAndValues.put("name", name);
    }

    ProjectionOperation projectRequiredFields = project(GRAPHQL_PROJECTION_FIELDS);

    GroupOperation groupByMaxRevision = group(GRAPHQL_ID).max(REVISION)
        .as(MAX_REVISION).push("$$ROOT")
        .as(ORIGINAL_DOC);

    ProjectionOperation filterMaxRevision = project().and(
            filter(ORIGINAL_DOC).as("doc").by(valueOf(MAX_REVISION).equalToValue("$$doc.revision")))
        .as(ORIGINAL_DOC);


    UnwindOperation unwindOperation = unwind(ORIGINAL_DOC);
    ProjectionOperation projectionOperation = project("originalDoc.name")
        .andInclude("originalDoc.graphQLId",
            "originalDoc.revision", "originalDoc.status", "originalDoc.createdBy",
            "originalDoc.cts","originalDoc.createdUserName","originalDoc.modifiedBy","originalDoc.modifiedUserName","originalDoc.mts",
            "originalDoc._id");

    MatchOperation matchOperation = getMatchOperation(filterFieldsAndValues);

    SortOperation sortOperation = getSortOperationForModifiedTime(sortByMts);
    AggregationResults<Document> results = null;
    if(matchOperation != null) {
      results = mongoTemplate.aggregate(newAggregation(projectRequiredFields, groupByMaxRevision, filterMaxRevision,
              unwindOperation, projectionOperation, sortOperation, matchOperation), GraphQL.class, Document.class);
    }else{
      results = mongoTemplate.aggregate(newAggregation(projectRequiredFields, groupByMaxRevision, filterMaxRevision,
          unwindOperation, projectionOperation, sortOperation), GraphQL.class, Document.class);
    }
    List<Document> graphQLList = results.getMappedResults();
    if(name != null){
      graphQLList = trimList(graphQLList,1,limit);
      return graphQLList;
    }else {
      graphQLList = trimList(graphQLList, offset, pagesize);
      Pagination pagination = new Pagination();
      pagination.setOffset(offset);
      pagination.setTotal((long) results.getMappedResults().size());
      pagination.setPageSize(graphQLList.size());
      response.setPagination(pagination);
      response.setData(graphQLList);
      return response;
    }

  }

  @Override
  public List<Stat> getStats() {
    List<Stat> statsList = new ArrayList<>();

    ProjectionOperation projectRequiredFields = project(GRAPHQL_ID, STATUS, REVISION);

    GroupOperation groupByMaxRevision = group(GRAPHQL_ID).max(REVISION).as(MAX_REVISION).push("$$ROOT")
        .as(ORIGINAL_DOC);

    ProjectionOperation filterMaxRevision = project()
        .and(filter(ORIGINAL_DOC).as("doc").by(valueOf(MAX_REVISION).equalToValue("$$doc.revision")))
        .as(ORIGINAL_DOC);

    UnwindOperation unwindOperation = unwind(ORIGINAL_DOC);

    ProjectionOperation projectionOperation = project("originalDoc.status","originalDoc.graphQLId");

    Cond condition = ConditionalOperators.when(Criteria.where(STATUS)).then(1).otherwise(0);

    GroupOperation groupByName = group(STATUS).sum(condition).as(COUNT);

    ProjectionOperation projectionOperation1 = project().andExclude("_id").andInclude(COUNT).and("_id").as(STATUS);
    AggregationResults<Document> results = mongoTemplate.aggregate(
        newAggregation(projectRequiredFields, groupByMaxRevision, filterMaxRevision,
            unwindOperation, projectionOperation, groupByName, projectionOperation1),
        GraphQL.class, Document.class);
    results.forEach(document -> {
      Stat stats = new Stat();
      stats.setName(document.get(STATUS).toString());
      stats.setCount(document.get(COUNT).toString());
      statsList.add(stats);
    });

    return statsList;
  }

  @Override
  public List<GraphQLImport> importFile(MultipartFile zipFile, String type, String gitURI, String branch,
      String authType, String userName, String password, String personalToken)throws Exception {
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
        fileLocation = applicationProperties.getTempDir() + "graphQLImport";
        String file = applicationProperties.getTempDir() + zipFile.getOriginalFilename();
        File targetFile = new File(file);
        File cloningDirectory = new File(fileLocation);
        cloningDirectory.mkdirs();
        zipFile.transferTo(targetFile);
        unZip.unzip(file, fileLocation);
      } catch (Exception e) {
        logger.error("Exception occurred : {}", e.getMessage());
      }
    } else {
      String message = "Invalid request data! Invalid type provided supported values - git, file";
      throw new ItorixException(message, "General-1001");
    }
    List<File> files = unZip.getGrapgQLFiles(fileLocation);
    if (files.isEmpty()) {
      String message = "Invalid request data! Invalid file type";
      throw new ItorixException(message, "General-1001");
    } else {
      List<GraphQLImport> listGraphQL = new ArrayList<>();
      try {
        listGraphQL = importGraphQLSchemasFromFiles(files);
      } catch (Exception e) {
        throw new ItorixException(e.getMessage(), "General-1000");
      } finally {
        FileUtils.cleanDirectory(new File(fileLocation));
        FileUtils.deleteDirectory(new File(fileLocation));
      }
      return listGraphQL;
    }
  }

  public SwaggerLockResponse getLockStatus(GraphQL graphQL) throws ItorixException {
    SwaggerLockResponse lockResponse = new SwaggerLockResponse();
    GraphQL checkGraphQL = findGraphQL(graphQL);
    if(checkGraphQL != null){
      Boolean lockStatus = checkGraphQL.getLock();
      if (lockStatus != null) {
        lockResponse.setLockStatus(lockStatus);
        if (lockStatus) {
          lockResponse.setLockedBy(checkGraphQL.getLockedBy());
          lockResponse.setLockedAt(checkGraphQL.getLockedAt());
          lockResponse.setLockedByUserId(checkGraphQL.getLockedByUserId());
        }
        return lockResponse;
      } else {
        logger.error("No Data found for Id - {} and revision - {}",graphQL.getGraphQLId(),graphQL.getRevision());
        throw new ItorixException(String.format(ErrorCodes.errorMessage.get("GraphQL-1000"),graphQL.getGraphQLId(),graphQL.getRevision()), "GraphQL-1001");
      }
    }else{
      logger.error("No Data found for Id - {} and revision - {}",graphQL.getGraphQLId(),graphQL.getRevision());
      throw new ItorixException(String.format(ErrorCodes.errorMessage.get("GraphQL-1000"),graphQL.getGraphQLId(),graphQL.getRevision()), "GraphQL-1001");
    }
  }

  public void updateLockStatus(GraphQL graphQL,String jsessionId) throws ItorixException {
    GraphQL checkGraphQL = findGraphQL(graphQL);
    if(checkGraphQL != null){
      if (graphQL.getLock()) {
        UserSession userSessionToken = masterTemplate.findById(jsessionId, UserSession.class);
        User user = masterTemplate.findById(userSessionToken.getUserId(), User.class);
        checkGraphQL.setLockedBy(user.getFirstName() + " " + user.getLastName());
        checkGraphQL.setLockedAt(System.currentTimeMillis());
        checkGraphQL.setLockedByUserId(user.getId());

      } else {
        checkGraphQL.setLockedBy(null);
        checkGraphQL.setLockedAt(null);
        checkGraphQL.setLockedByUserId(null);
      }
      checkGraphQL.setLock(graphQL.getLock());
      updateUserDetails(checkGraphQL);
      mongoTemplate.save(checkGraphQL);
    }else{
      logger.error("No Data found for Id - {} and revision - {}",graphQL.getGraphQLId(),graphQL.getRevision());
      throw new ItorixException(String.format(ErrorCodes.errorMessage.get("GraphQL-1000"),graphQL.getGraphQLId(),graphQL.getRevision()), "GraphQL-1001");
    }
  }

  @Override
  public GraphQL findGraphQL(GraphQL graphQL) {
    Query query = new Query();
    if(graphQL.getGraphQLId()!=null){
      query.addCriteria(Criteria.where(GRAPHQL_ID).is(graphQL.getGraphQLId()));
      if(graphQL.getRevision()!=null){
        query.addCriteria(Criteria.where(REVISION).is(graphQL.getRevision()));
      }
      return mongoTemplate.findOne(query,GraphQL.class);
    } else if(graphQL.getName()!=null){
      query.addCriteria(Criteria.where("name").is(graphQL.getName()));
      return mongoTemplate.findOne(query,GraphQL.class);
    }
    return null;
  }

  public List<Revision> getListOfRevisions(String graphQLId) {
    List<GraphQL> graphQLList = mongoTemplate.find(new Query(Criteria.where(GRAPHQL_ID).is(graphQLId)), GraphQL.class);
    List<Revision> versions = new ArrayList<Revision>();
    for (GraphQL graphQl : graphQLList) {
      Revision version = new Revision();
      version.setRevision(graphQl.getRevision());
      version.setStatus(graphQl.getStatus().getStatus());
      version.setId(graphQl.getGraphQLId() != null ? graphQl.getGraphQLId() : graphQl.getId());
      versions.add(version);
    }
    return versions;
  }

  public void updateUserDetails(GraphQL graphQL){
    String userId = null;
    String username = null;
    try {
      UserSession userSession = UserSession.getCurrentSessionToken();
      userId = userSession.getUserId();
      username = userSession.getUsername();
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
    String id = graphQL.getId();
    long timestamp = System.currentTimeMillis();
    graphQL.setMts(timestamp);
    graphQL.setModifiedBy(userId);
    graphQL.setModifiedUserName(username);
    if (id == null || id == "") {
      graphQL.setCts(timestamp);
      graphQL.setCreatedBy(userId);
      graphQL.setCreatedUserName(username);
    }
  }

  private List<Document> trimList(List<Document> graphQLList, int offset, int pageSize) {
    List<Document> responseList = new ArrayList<>();
    int i = offset > 0 ? ((offset - 1) * pageSize) : 0;
    int end = i + pageSize;
    for (; i < graphQLList.size() && i < end; i++) {
      responseList.add(graphQLList.get(i));
    }
    return responseList;
  }

  private MatchOperation getMatchOperation(Map<String, String> filterFieldsAndValues) {
    List<Criteria> criteriaList = new ArrayList<>();
    if(filterFieldsAndValues.containsKey("status")){
      criteriaList.add(Criteria.where("status").in(filterFieldsAndValues.get("status")));
    }
    if(filterFieldsAndValues.containsKey("name")){
      criteriaList.add(Criteria.where("name").regex(filterFieldsAndValues.get("name")));
    }
    Criteria criteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()]));
    return !criteriaList.isEmpty() ? match(criteria) : null;
  }

  private SortOperation getSortOperationForModifiedTime(String sortByModifiedTS) {
    SortOperation sortOperation = null;
    if (sortByModifiedTS != null && sortByModifiedTS.equalsIgnoreCase("ASC")) {
      sortOperation = sort(Sort.Direction.ASC, "mts");
    } else if (sortByModifiedTS != null && sortByModifiedTS.equalsIgnoreCase("DESC")) {
      sortOperation = sort(Sort.Direction.DESC, "mts");
    } else {
      sortOperation = sort(Sort.Direction.ASC, "name");
    }

    return sortOperation;
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

  private List<GraphQLImport> importGraphQLSchemasFromFiles(List<File> files) throws Exception {
    List<GraphQLImport> listGraphQL = new ArrayList<>();
    for (File file : files) {
      String reason = null;
      GraphQLImport graphQLImport = new GraphQLImport();
      String filecontent = FileUtils.readFileToString(file);
      try {
        Parser parser = new Parser();
        parser.parseDocument(filecontent);
      }catch(GraphQLException e){
        reason = "Not a valid graphql schema";
        graphQLImport.setName(file.getName());
        graphQLImport.setLoaded(false);
        graphQLImport.setReason(reason);
        listGraphQL.add(graphQLImport);
        continue;
      }
      String graphQLName = null;
      graphQLName = file.getName();
      if(graphQLName!=null){
        graphQLName = FilenameUtils.removeExtension(file.getName());
      }else{
        reason = "File Name is not present";
        graphQLImport.setName("");
        graphQLImport.setLoaded(false);
        graphQLImport.setReason(reason);
        listGraphQL.add(graphQLImport);
        continue;
      }
      GraphQL graphQL = new GraphQL();
      graphQL.setName(graphQLName);
      graphQL.setGraphQLSchema(filecontent);
      graphQLImport.setLoaded(false);
      graphQLImport.setName(graphQLName);
      GraphQL checkGraphQL = findGraphQL(graphQL);
      if(checkGraphQL == null){
        create(graphQL);
        graphQLImport.setLoaded(true);
        graphQLImport.setGraphQLId(graphQL.getGraphQLId());
        reason = "New GraphQLSchema created";
      }else{
        createNewRevisionWithName(graphQL);
        graphQLImport.setLoaded(true);
        graphQLImport.setGraphQLId(graphQL.getGraphQLId());
        reason = String.format("New Revision of %s GraphQLSchema created",graphQL.getName());
      }
      graphQLImport.setReason(reason);
      listGraphQL.add(graphQLImport);
    }
    return listGraphQL;
  }

}
